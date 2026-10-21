package com.kozikwlodarski.backendgymflex.service;

import com.kozikwlodarski.backendgymflex.repository.FitnessClassRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Serwis zarządzający grafikiem zajęć fitness.
 */
@Service
@RequiredArgsConstructor
public class FitnessClassService {

    private final FitnessClassRepository fitnessClassRepository;

    /**
     * Odwoływanie zajęć przez administratora.
     * Metoda nie zawiera skomplikowanej logiki w Javie, ponieważ deleguje całe zadanie
     * do zdefiniowanej w bazie PostgreSQL procedury składowanej 'cancel_class'.
     *
     * Zgodnie z dokumentacją projektu, procedura ta wykonuje się atomowo w bazie danych i:
     * 1. Weryfikuje rolę p_admin_id (sprawdza czy to faktycznie ADMIN).
     * 2. Oznacza zajęcia jako is_active = false (miękkie usuwanie).
     * 3. Zmienia statusy potwierdzonych rezerwacji na 'CANCELLED'.
     * 4. Tworzy wpis w audit_logs ze zliczoną liczbą anulowanych rezerwacji.
     *
     * @param classId identyfikator odwoływanych zajęć
     * @param adminId identyfikator administratora wykonującego akcję
     */
    @Transactional
    public void cancelClass(Long classId, Long adminId) {
        fitnessClassRepository.cancelClass(classId, adminId);
    }
}