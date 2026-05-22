package com.hera.atendeja.exception;

import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;
import java.util.List;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessRuleException.class)
    ResponseEntity<ProblemDetail> handleBusinessRule(
            BusinessRuleException exception,
            HttpServletRequest request
    ) {
        ProblemDetail problem = buildProblem(
                HttpStatus.BAD_REQUEST,
                "Regra de negócio inválida",
                exception.getMessage(),
                request
        );
        problem.setProperty("code", "BUSINESS_RULE_VIOLATION");
        return ResponseEntity.badRequest().body(problem);
    }

    @ExceptionHandler(AppointmentConflictException.class)
    ResponseEntity<ProblemDetail> handleAppointmentConflict(
            AppointmentConflictException exception,
            HttpServletRequest request
    ) {
        ProblemDetail problem = buildProblem(
                HttpStatus.CONFLICT,
                "Conflito de agenda",
                exception.getMessage(),
                request
        );
        problem.setProperty("code", "APPOINTMENT_TIME_CONFLICT");
        return ResponseEntity.status(HttpStatus.CONFLICT).body(problem);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    ResponseEntity<ProblemDetail> handleResourceNotFound(
            ResourceNotFoundException exception,
            HttpServletRequest request
    ) {
        ProblemDetail problem = buildProblem(
                HttpStatus.NOT_FOUND,
                "Recurso não encontrado",
                exception.getMessage(),
                request
        );
        problem.setProperty("code", "RESOURCE_NOT_FOUND");
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(problem);
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    ResponseEntity<ProblemDetail> handleInvalidCredentials(
            InvalidCredentialsException exception,
            HttpServletRequest request
    ) {
        ProblemDetail problem = buildProblem(
                HttpStatus.UNAUTHORIZED,
                "Credenciais inválidas",
                exception.getMessage(),
                request
        );
        problem.setProperty("code", "INVALID_CREDENTIALS");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(problem);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ProblemDetail> handleValidation(
            MethodArgumentNotValidException exception,
            HttpServletRequest request
    ) {
        List<ValidationError> errors = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(this::toValidationError)
                .toList();

        ProblemDetail problem = buildProblem(
                HttpStatus.BAD_REQUEST,
                "Erro de validação",
                "Revise os campos enviados.",
                request
        );
        problem.setProperty("code", "VALIDATION_ERROR");
        problem.setProperty("errors", errors);
        return ResponseEntity.badRequest().body(problem);
    }

    @ExceptionHandler({
            HttpMessageNotReadableException.class,
            MethodArgumentTypeMismatchException.class
    })
    ResponseEntity<ProblemDetail> handleBadRequest(Exception exception, HttpServletRequest request) {
        ProblemDetail problem = buildProblem(
                HttpStatus.BAD_REQUEST,
                "Requisição inválida",
                "Revise o formato dos dados enviados.",
                request
        );
        problem.setProperty("code", "BAD_REQUEST");
        return ResponseEntity.badRequest().body(problem);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    ResponseEntity<ProblemDetail> handleDataIntegrity(
            DataIntegrityViolationException exception,
            HttpServletRequest request
    ) {
        ProblemDetail problem = buildProblem(
                HttpStatus.CONFLICT,
                "Conflito de dados",
                "Já existe um registro com dados únicos informados.",
                request
        );
        problem.setProperty("code", "DATA_INTEGRITY_VIOLATION");
        return ResponseEntity.status(HttpStatus.CONFLICT).body(problem);
    }

    private ProblemDetail buildProblem(
            HttpStatus status,
            String title,
            String detail,
            HttpServletRequest request
    ) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setTitle(title);
        problem.setInstance(URI.create(request.getRequestURI()));
        return problem;
    }

    private ValidationError toValidationError(FieldError fieldError) {
        return new ValidationError(fieldError.getField(), fieldError.getDefaultMessage());
    }
}
