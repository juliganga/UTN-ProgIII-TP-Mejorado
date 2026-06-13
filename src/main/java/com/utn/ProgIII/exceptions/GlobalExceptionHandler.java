package com.utn.ProgIII.exceptions;

import io.jsonwebtoken.ExpiredJwtException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.data.mapping.PropertyReferenceException;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Clase para gestionar de manera global las excepciones.
 * <p>
 */

@ControllerAdvice
public class GlobalExceptionHandler {

    // Bad constraints (from jakarta)
    @ExceptionHandler(ConstraintViolationException.class)
    public ProblemDetail handleConstraintViolationException(ConstraintViolationException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, getViolatedConstraints(ex).toString());
        problemDetail.setTitle("Restricciones vulneradas");
        problemDetail.setProperty("timestamp", Instant.now().toString());

        return problemDetail;
    }

    private List<String> getViolatedConstraints(ConstraintViolationException ex) {
        List<ConstraintViolation<?>> constraintViolations = ex.getConstraintViolations().stream().toList();

        List<String> violations = new ArrayList<String>();
        for (ConstraintViolation<?> constraintViolation: constraintViolations) {
            String message = constraintViolation.getMessageTemplate();
            violations.add(message);
        }

        return violations;
    }

    @ExceptionHandler(ExpiredJwtException.class)
    public ProblemDetail ExpiredTokenException(ExpiredJwtException e)
    {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatusCode.valueOf(419),"La sesión ha expirado. Por favor, iniciar sesión de nuevo");
        problemDetail.setProperty("timestamp", Instant.now().toString());
        problemDetail.setTitle("Sesión Expirada");
        return problemDetail;
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ProblemDetail handleMissingParams(MissingServletRequestParameterException ex) {
        String name = ex.getParameterName();

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST,"El parámetro \"" + name + "\" está ausente");
        problemDetail.setTitle("Parametro faltante");
        problemDetail.setProperty("timestamp", Instant.now().toString());

        return problemDetail;
    }

    @ExceptionHandler(PropertyReferenceException.class)
    public ProblemDetail wrongFieldSortException(PropertyReferenceException e)
    {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, e.getMessage());
        problemDetail.setTitle("Propiedad inexistente");
        problemDetail.setProperty("timestamp",Instant.now());
        return problemDetail;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail methodArgumentNotValidException(MethodArgumentNotValidException e)
    {
        ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        problemDetail.setProperty("timestamp",Instant.now().toString());
        return problemDetail;
    }

    private ProblemDetail handleMethodArgumentNotValid(MethodArgumentNotValidException e)
    {
        List<String> errors = e.getFieldErrors().stream().map(DefaultMessageSourceResolvable::getDefaultMessage).toList();
        String message = "Error en pedido:";

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, errors.toString());
        problemDetail.setTitle(message);
        problemDetail.setProperty("timestamp", Instant.now().toString());

        return problemDetail;
    }

    /* CUSTOM MADE EXCEPTIONS */

    @ExceptionHandler(UnexpectedServerErrorException.class)
    public ProblemDetail UnexpectedErrorException(UnexpectedServerErrorException e) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.valueOf(e.getHttpcode()),e.getMessage());
        problemDetail.setProperty("timestamp", Instant.now().toString());


        return problemDetail;
    }

    @ExceptionHandler({NotFoundException.class})
    private ProblemDetail handleNotFoundException(NotFoundException e)
    {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.getMessage());
        problemDetail.setProperty("timestamp", Instant.now().toString());

        return problemDetail;
    }

    @ExceptionHandler({ConflictException.class})
    private ProblemDetail handleConflictException(ConflictException e)
    {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, e.getMessage());
        problemDetail.setProperty("timestamp", Instant.now().toString());

        return problemDetail;
    }

    @ExceptionHandler({BadRequestException.class})
    private ProblemDetail handleBadRequestException(BadRequestException e)
    {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, e.getMessage());
        problemDetail.setProperty("timestamp", Instant.now().toString());

        return problemDetail;
    }

    @ExceptionHandler({InvalidActionException.class})
    private ProblemDetail handleInvalidActionException(InvalidActionException e)
    {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, e.getMessage());
        problemDetail.setProperty("timestamp", Instant.now().toString());

        return problemDetail;
    }

    @ExceptionHandler({InternalServerError.class})
    private ProblemDetail handleInternalServerError(InternalServerError e)
    {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        problemDetail.setProperty("timestamp", Instant.now().toString());

        return problemDetail;
    }

    @ExceptionHandler({ForbiddenActionException.class})
    private ProblemDetail handleForbiddenActionException(ForbiddenActionException e)
    {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, e.getMessage());
        problemDetail.setProperty("timestamp", Instant.now().toString());

        return problemDetail;
    }

    /* From Spring Security */
    @ExceptionHandler({AuthenticationException.class})
    private ProblemDetail handleAuthenticationException(AuthenticationException e) {
        ProblemDetail problemDetail =  ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, e.getMessage());
        problemDetail.setTitle("Error de Autenticación");
        problemDetail.setDetail("Compruebe usuario y contraseña");
        problemDetail.setProperty("timestamp", Instant.now().toString());
        return problemDetail;
    }

}
