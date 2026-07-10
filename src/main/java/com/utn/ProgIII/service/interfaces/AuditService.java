package com.utn.ProgIII.service.interfaces;

import com.utn.ProgIII.dto.AuditLogDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AuditService {
    Page<AuditLogDTO> getAuditLogs(String category, Integer type, Pageable pageable);
}
