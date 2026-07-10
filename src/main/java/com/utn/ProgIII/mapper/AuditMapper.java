package com.utn.ProgIII.mapper;

import com.utn.ProgIII.dto.AuditLogDTO;
import com.utn.ProgIII.model.Audit.AuditLog;
import org.springframework.stereotype.Component;

@Component
public class AuditMapper {

    public AuditLogDTO entityToDTO(AuditLog auditLog)
    {
        return new AuditLogDTO(auditLog.getRevisionId(),
                auditLog.getRevisionDate(),
                auditLog.getUserId(),
                auditLog.getCategory(),
                auditLog.getRevisionType(),
                auditLog.getEntityId());
    }
}
