package com.utn.ProgIII.exceptions;

public class NonExistentRelationshipException extends RuntimeException {
    public NonExistentRelationshipException(String msg) {
        super(msg);
    }
}
