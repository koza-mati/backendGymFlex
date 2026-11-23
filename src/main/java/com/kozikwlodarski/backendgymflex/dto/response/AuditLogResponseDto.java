package com.kozikwlodarski.backendgymflex.dto.response;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class AuditLogResponseDto {
    private Long id;
    private Long userId;
    private String action;
    private String entityName;
    private Long entityId;
    private String details;
    private LocalDateTime createdAt;
}
