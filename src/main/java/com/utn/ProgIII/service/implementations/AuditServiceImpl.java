package com.utn.ProgIII.service.implementations;

import com.utn.ProgIII.service.interfaces.AuditService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.hibernate.query.NativeQuery;
import org.hibernate.transform.Transformers;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class AuditServiceImpl implements AuditService {

    @PersistenceContext
    private EntityManager entityManager;

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
