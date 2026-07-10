package com.utn.ProgIII.service.interfaces;

import java.util.Map;

public interface AuditService {
    Map<String, Object> getRawAuditRow(String cat, Long rev);
}
