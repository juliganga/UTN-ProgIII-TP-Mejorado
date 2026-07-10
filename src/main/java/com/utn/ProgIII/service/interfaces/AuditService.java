package com.utn.ProgIII.service.interfaces;

import java.util.Map;
import com.utn.ProgIII.dto.AuditLogDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AuditService {
    Map<String, Object> getRawAuditRow(String cat, Long rev);
    Page<AuditLogDTO> getAuditLogs(String category, Integer type, Pageable pageable);
}
