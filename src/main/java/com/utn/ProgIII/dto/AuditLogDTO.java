package com.utn.ProgIII.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

public record AuditLogDTO(
        Long revisionId,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
        LocalDateTime revisionDate,
        Long userId,
        String category,
        Integer revisionType,
        Long entityId
) {
}
