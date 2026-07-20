package com.utn.ProgIII.repository;

import com.utn.ProgIII.exceptions.BadRequestException;
import com.utn.ProgIII.model.Audit.AuditCategories;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.persistence.Tuple;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;

@Repository
public class AuditLogRepositoryCustomImpl implements AuditLogRepositoryCustom{

    private final EntityManager entityManager;

    public AuditLogRepositoryCustomImpl(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public Map<String, Object> getRow(String category, Long rev) {

        if(!AuditCategories.isValid(category)){
            throw new BadRequestException("Invalid category");
        }

        String tableName = category.toLowerCase() + "_audit";

        String sql = String.format("SELECT * FROM %s WHERE revision = :rev", tableName);

        try {
            Query query = entityManager.createNativeQuery(sql, Tuple.class);

            query.setParameter("rev", rev);
            Tuple singleResult = (Tuple) query.getSingleResult();
            HashMap<String, Object> result = new HashMap<>();

            singleResult.getElements().forEach(element -> {
               result.put(element.getAlias(), singleResult.get(element));
            });

            return result;

        } catch (Exception e) {
            return Map.of("error", "No se encontró el registro de auditoría");
        }
    }
}
