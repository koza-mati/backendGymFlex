package com.kozikwlodarski.backendgymflex.controller;

import com.kozikwlodarski.backendgymflex.entity.AuditLogEntity;
import com.kozikwlodarski.backendgymflex.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Kontroler REST do przeglądania dziennika zdarzeń (Audit Logs).
 * W docelowej aplikacji endpoint ten powinien być zabezpieczony wyłącznie dla roli ADMIN.
 */
@RestController
@RequestMapping("/api/audit-logs")
@RequiredArgsConstructor
public class AuditLogController {

    private final AuditLogService auditLogService;

    @GetMapping
    public ResponseEntity<List<AuditLogEntity>> getAllLogs() {
        return ResponseEntity.ok(auditLogService.getAllLogs());
    }
}