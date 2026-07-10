package com.utn.ProgIII.service.interfaces;

import com.utn.ProgIII.model.Audit.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AuditService {
    Page<AuditLog> getAuditLogs(String category, Integer type, Pageable pageable);
}
