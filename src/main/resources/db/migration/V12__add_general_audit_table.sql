CREATE OR REPLACE VIEW general_audit_log AS
SELECT r.rev AS revision_id, r.date AS revision_date, r.id_user AS user_id, 'ADDRESS' AS category, a.revision_type AS revision_type, a.id_address AS entity_id
FROM revinfo r INNER JOIN address_audit a ON r.rev = a.revision
UNION ALL
SELECT r.rev, r.date, r.id_user, 'CREDENTIAL', c.revision_type, c.id_credential
FROM revinfo r INNER JOIN credential_audit c ON r.rev = c.revision
UNION ALL
SELECT r.rev, r.date, r.id_user, 'PRODUCT', p.revision_type, p.id_product
FROM revinfo r INNER JOIN product_audit p ON r.rev = p.revision
UNION ALL
SELECT r.rev, r.date, r.id_user, 'PRODUCT_SUPPLIER', ps.revision_type, ps.id_product_supplier
FROM revinfo r INNER JOIN product_supplier_audit ps ON r.rev = ps.revision
UNION ALL
SELECT r.rev, r.date, r.id_user, 'SUPPLIER', s.revision_type, s.id_supplier
FROM revinfo r INNER JOIN supplier_audit s ON r.rev = s.revision
UNION ALL
SELECT r.rev, r.date, r.id_user, 'USER', u.revision_type, u.id_user
FROM revinfo r INNER JOIN user_audit u ON r.rev = u.revision;