package com.kozikwlodarski.backendgymflex.service;

import com.kozikwlodarski.backendgymflex.entity.AuditLogEntity;
import com.kozikwlodarski.backendgymflex.dto.response.AuditLogResponseDto;
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
    public List<AuditLogResponseDto> getAllLogs() {
        return auditLogRepository.findAll().stream().map(entity -> {
            AuditLogResponseDto dto = new AuditLogResponseDto();
            dto.setId(entity.getId());
            dto.setUserId(entity.getUsers() == null ? null : entity.getUsers().getId());
            dto.setAction(entity.getAction());
            dto.setEntityName(entity.getEntityName());
            dto.setEntityId(entity.getEntityId());
            dto.setDetails(entity.getDetails());
            dto.setCreatedAt(entity.getCreatedAt());
            return dto;
        }).toList();
    }
}
