package com.utn.ProgIII.service.implementations;

import com.querydsl.core.BooleanBuilder;
import com.utn.ProgIII.dto.AuditLogDTO;
import com.utn.ProgIII.mapper.AuditMapper;
import com.utn.ProgIII.model.Audit.QAuditLog;
import com.utn.ProgIII.repository.AuditLogRepository;
import com.utn.ProgIII.service.interfaces.AuditService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.hibernate.query.NativeQuery;
import org.hibernate.transform.Transformers;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class AuditServiceImpl implements AuditService {

    final private AuditLogRepository auditLogRepository;
    final private AuditMapper auditMapper;

    @PersistenceContext
    private EntityManager entityManager;


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

    public Map<String, Object> getRawAuditRow(String category, Long rev) {
        String tableName = category.toLowerCase() + "_audit";

        String idColumnName = "id_" + category.toLowerCase();

        String sql = String.format("SELECT * FROM %s WHERE revision = :rev", tableName, idColumnName);

        try {
            Query query = entityManager.createNativeQuery(sql);
            query.unwrap(NativeQuery.class).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP);

            query.setParameter("rev", rev);

            return (Map<String, Object>) query.getSingleResult();
        } catch (Exception e) {
            return Map.of("error", "No se encontró el registro de auditoría");
        }
    }
}
