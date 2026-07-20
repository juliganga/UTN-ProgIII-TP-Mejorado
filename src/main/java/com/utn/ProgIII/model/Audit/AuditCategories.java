package com.utn.ProgIII.model.Audit;

public enum AuditCategories {

    ADDRESS,
    CREDENTIAL,
    PRODUCT,
    PRODUCT_SUPPLIER,
    SUPPLIER,
    USER;

    public static boolean isValid(String category) {
        for (AuditCategories cat : AuditCategories.values()) {
            if (cat.name().equalsIgnoreCase(category)) {
                return true;
            }
        }
        return false;
    }

}
