package msa.emaia.exceptions;


import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.security.SignatureException;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.BadRequestException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AccountStatusException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<String> handleDataIntegrityViolationException(DataIntegrityViolationException ex) {
        if (ex.getCause() instanceof org.hibernate.exception.ConstraintViolationException constraintEx) {
            String constraintName = constraintEx.getConstraintName();
            if ("uk_users_email".equals(constraintName)) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body("Email already exists.");
            }
        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body("Data integrity violation occurred.");
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleException(Exception exception) {
        ProblemDetail errorDetail = null;

        // TODO send this stack trace to an observability tool

        if (exception instanceof BadCredentialsException) {
            errorDetail = ProblemDetail.forStatusAndDetail(HttpStatusCode.valueOf(401), "The email or password is incorrect");
            errorDetail.setProperty("description", "The email or password is incorrect");
        }

        if (exception instanceof AccountStatusException) {
            errorDetail = ProblemDetail.forStatusAndDetail(HttpStatusCode.valueOf(403), "The account is locked");
            errorDetail.setProperty("description", "The account is locked");
        }

        if (exception instanceof AccessDeniedException) {
            errorDetail = ProblemDetail.forStatusAndDetail(HttpStatusCode.valueOf(403), "You are not authorized to access this resource");
            errorDetail.setProperty("description", "You are not authorized to access this resource");
        }

        if (exception instanceof SignatureException) {
            errorDetail = ProblemDetail.forStatusAndDetail(HttpStatusCode.valueOf(403), "The JWT signature is invalid");
            errorDetail.setProperty("description", "The JWT signature is invalid");
        }

        if (exception instanceof ExpiredJwtException) {
            errorDetail = ProblemDetail.forStatusAndDetail(HttpStatusCode.valueOf(403), "The JWT token has expired");
            errorDetail.setProperty("description", "The JWT token has expired");
        }

        if (exception instanceof RequiredException) {
            errorDetail = ProblemDetail.forStatusAndDetail(HttpStatusCode.valueOf(200), exception.getMessage());
            errorDetail.setProperty("description", exception.getMessage());
        }

        if (exception instanceof BadRequestException) {
            errorDetail = ProblemDetail.forStatusAndDetail(HttpStatusCode.valueOf(500), exception.getMessage());
            errorDetail.setProperty("description", exception.getMessage());
        }

        if (errorDetail == null) {
            log.error(exception.getMessage(), exception);
            errorDetail = ProblemDetail.forStatusAndDetail(HttpStatusCode.valueOf(500), "An error occurred, please try again later.");
            errorDetail.setProperty("description", "Unknown internal server error.");
        }

        if(exception instanceof InvalidException) {
            errorDetail = ProblemDetail.forStatusAndDetail(HttpStatusCode.valueOf(400), exception.getMessage());
            errorDetail.setProperty("description", exception.getMessage());
        }

        if(exception instanceof ProcessingException) {
            errorDetail = ProblemDetail.forStatusAndDetail(HttpStatusCode.valueOf(400), exception.getMessage());
            errorDetail.setProperty("description", exception.getMessage());
        }

        return errorDetail;
    }
}