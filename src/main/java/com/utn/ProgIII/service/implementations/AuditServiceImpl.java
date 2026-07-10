package com.utn.ProgIII.service.implementations;

import com.querydsl.core.BooleanBuilder;
import com.utn.ProgIII.dto.AuditLogDTO;
import com.utn.ProgIII.mapper.AuditMapper;
import com.utn.ProgIII.model.Audit.QAuditLog;
import com.utn.ProgIII.repository.AuditLogRepository;
import com.utn.ProgIII.service.interfaces.AuditService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class AuditServiceImpl implements AuditService {

    final private AuditLogRepository auditLogRepository;
    final private AuditMapper auditMapper;


    public AuditServiceImpl(AuditLogRepository auditLogRepository, AuditMapper auditMapper) {
        this.auditLogRepository = auditLogRepository;
        this.auditMapper = auditMapper;
    }

    @Override
    public Page<AuditLogDTO> getAuditLogs(String category, Integer type, Pageable pageable) {
        QAuditLog qAuditLog = QAuditLog.auditLog;
        BooleanBuilder predicate = new BooleanBuilder();

        if (category != null && !category.isEmpty()) {
            predicate.and(qAuditLog.category.eq(category));
        }
        if (type != null) {
            predicate.and(qAuditLog.revisionType.eq(type));
        }

        return auditLogRepository.findAll(predicate, pageable).map(auditMapper::entityToDTO);
    }
}
