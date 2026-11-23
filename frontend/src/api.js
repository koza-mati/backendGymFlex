const baseUrl = import.meta.env.VITE_API_URL || ''

export function setToken(token) {
  if (token) localStorage.setItem('gymflex-token', token)
  else localStorage.removeItem('gymflex-token')
}

export function getToken() {
  return localStorage.getItem('gymflex-token')
}

export async function api(path, options = {}) {
  const headers = new Headers(options.headers || {})
  const token = getToken()
  if (token) headers.set('Authorization', `Bearer ${token}`)
  if (options.body && !(options.body instanceof FormData)) headers.set('Content-Type', 'application/json')

  const response = await fetch(`${baseUrl}${path}`, { ...options, headers })
  const refreshedToken = response.headers.get('X-Auth-Token')
  if (refreshedToken) setToken(refreshedToken)

  if (!response.ok) {
    const body = await response.text()
    let message = body || `Błąd HTTP ${response.status}`
    try {
      const parsed = JSON.parse(body)
      message = parsed.message || parsed.error || message
    } catch { }
    throw new Error(message)
  }
  if (response.status === 204) return null
  const text = await response.text()
  return text ? JSON.parse(text) : null
}

export const json = (method, body) => ({ method, body: JSON.stringify(body) })
