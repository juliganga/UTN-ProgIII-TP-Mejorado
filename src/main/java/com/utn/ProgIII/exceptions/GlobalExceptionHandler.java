package com.utn.ProgIII.exceptions;

import io.jsonwebtoken.ExpiredJwtException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.springframework.data.mapping.PropertyReferenceException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.List;

/**
 * Clase para gestionar de manera global las excepciones.
 * <p>
 */

@ControllerAdvice
public class GlobalExceptionHandler {


    @ExceptionHandler({SupplierNotFoundException.class})
    public ResponseEntity<Object> supplierNotFoundException(SupplierNotFoundException ex)
    {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ex.getMessage());
    }

    @ExceptionHandler({AddressNotFoundException.class})
    public ResponseEntity<Object> addressNotFoundException(AddressNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ex.getMessage());
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<String> handleConstraintViolationException(ConstraintViolationException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(getViolatedConstraints(ex));
    }

    @ExceptionHandler(DuplicateEntityException.class)
        public ResponseEntity<String> handleDuplicateEntityException(DuplicateEntityException ex){
        return ResponseEntity
                .status(HttpStatus.CONFLICT).
                body(ex.getMessage());
    }

    @ExceptionHandler({UserNotFoundException.class})
    public ResponseEntity<Object> handleUserNotFoundException(UserNotFoundException ex) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ex.getMessage());
    }
    @ExceptionHandler({CredentialNotFoundException.class})
    public ResponseEntity<Object> handleCredentialNotFoundException(CredentialNotFoundException ex) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ex.getMessage());
    }

    @ExceptionHandler({InvalidRequestException.class})
    public ResponseEntity<Object> invalidRequestException(InvalidRequestException ex)
    {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    @ExceptionHandler({NullAddressException.class})
    public ResponseEntity<Object> nullAddressException(NullAddressException ex)
    {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    @ExceptionHandler({NullCredentialsException.class})
    public ResponseEntity<Object> nullCredentialsException(NullCredentialsException ex)
    {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    private String getViolatedConstraints(ConstraintViolationException ex) {
        List<ConstraintViolation<?>> constraintViolations = ex.getConstraintViolations().stream().toList();
        StringBuilder constraintViolationMessages = new StringBuilder("Restricciones vulneradas: \n");
        for (ConstraintViolation<?> constraintViolation: constraintViolations) {
            String message = constraintViolation.getMessageTemplate();
            constraintViolationMessages.append(message);
            constraintViolationMessages.append("\n");
        }

        return constraintViolationMessages.toString();
    }

    @ExceptionHandler(DuplicateRelationshipException.class)
    public ResponseEntity<String> handleDuplicateRelationshipException(DuplicateRelationshipException ex){
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
    }

    @ExceptionHandler(ProductSupplierNotExistException.class)
    public ResponseEntity<String> handleProductSupplierNotExistException(ProductSupplierNotExistException ex){
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    @ExceptionHandler(ProductNotFoundException.class)
    public ResponseEntity<String> productNotFoundException(ProductNotFoundException ex){
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ex.getMessage());
    }

    @ExceptionHandler(InvalidProductStatusException.class)
    public ResponseEntity<String> invalidProductStatusException (InvalidProductStatusException ex){
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                         .body(ex.getMessage());
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<String> handleAuthException(AuthenticationException e){
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("No se encontró el usuario");
    }

    @ExceptionHandler(UnexpectedServerErrorException.class)
    public ResponseEntity<String> UnexpectedErrorException(UnexpectedServerErrorException e) {
        if(e.getHttpcode() != -1)
        {
            return ResponseEntity.status(e.getHttpcode()).body(e.getMessage());
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @ExceptionHandler(ExpiredJwtException.class)
    public ResponseEntity<String> ExpiredTokenException(ExpiredJwtException e)
    {
        return ResponseEntity.status(419).body("La sesión ha expirado. Por favor, iniciar sesión de nuevo");
    }

    @ExceptionHandler(SelfDeleteUserException.class)
    public ResponseEntity<String> SelfDeleteUserProtection(SelfDeleteUserException e)
    {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
    }

    @ExceptionHandler(PropertyReferenceException.class)
    public ResponseEntity<String> wrongFieldSortException(PropertyReferenceException e)
    {
        return ResponseEntity.badRequest().body(e.getMessage());
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<String> handleMissingParams(MissingServletRequestParameterException ex) {
        String name = ex.getParameterName();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("El parámetro \"" + name + "\" está ausente");
    }

    @ExceptionHandler(ForbiddenModificationException.class)
    public ResponseEntity<String> forbiddenModificationException(ForbiddenModificationException e)
    {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<String> methodArgumentNotValidException(MethodArgumentNotValidException e)
    {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(handleMethodArgumentNotValid(e));
    }

    private String handleMethodArgumentNotValid(MethodArgumentNotValidException e)
    {
         List<FieldError> errors = e.getFieldErrors();
         String message = "Error en pedido:\n";
         for(FieldError error : errors)
         {
             message = message.concat("- " + error.getDefaultMessage() + "\n");
         }

         return message;
    }

    @ExceptionHandler(NonExistentRelationshipException.class)
    public ResponseEntity<String> handleNonExistentRelationshipException(NonExistentRelationshipException ex){
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

}
