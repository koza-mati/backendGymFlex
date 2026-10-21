package com.kozikwlodarski.backendgymflex.service;

import com.kozikwlodarski.backendgymflex.entity.AuditLogEntity;
import com.kozikwlodarski.backendgymflex.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    /**
     * Pobiera całą historię operacji w systemie.
     * Tabela AUDIT_LOGS jest strukturą typu append-only (tylko do dopisywania).
     */
    public List<AuditLogEntity> getAllLogs() {
        return auditLogRepository.findAll();
    }
}