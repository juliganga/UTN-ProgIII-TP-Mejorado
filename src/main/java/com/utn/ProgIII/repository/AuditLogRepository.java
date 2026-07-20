package com.utn.ProgIII.repository;

import com.utn.ProgIII.model.Audit.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long>, QuerydslPredicateExecutor<AuditLog>, AuditLogRepositoryCustom {
}