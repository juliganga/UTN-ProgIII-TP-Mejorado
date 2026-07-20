package com.utn.ProgIII.repository;

import java.util.Map;

public interface AuditLogRepositoryCustom {

    Map<String, Object> getRow(String category, Long rev);
}
