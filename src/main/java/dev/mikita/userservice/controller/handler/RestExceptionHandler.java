package dev.mikita.userservice.controller.handler;

import com.google.firebase.auth.FirebaseAuthException;
import dev.mikita.userservice.exception.NotFoundException;
import jakarta.security.auth.message.AuthException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import java.util.List;
import java.util.stream.Collectors;

/**
 * The type Rest exception handler.
 */
@ControllerAdvice
public class RestExceptionHandler {
    private static final Logger LOG = LoggerFactory.getLogger(RestExceptionHandler.class);

    private static void logException(Exception exception) {
        LOG.error("Exception caught:", exception);
    }

    private static ErrorInfo errorInfo(HttpServletRequest request, Throwable e) {
        return new ErrorInfo(e.getMessage(), request.getRequestURI());
    }

    /**
     * Illegal state exception response entity.
     *
     * @param request the request
     * @param e       the e
     * @return the response entity
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorInfo> illegalStateException(HttpServletRequest request, IllegalStateException e) {
        logException(e);
        return new ResponseEntity<>(errorInfo(request, e), HttpStatus.BAD_REQUEST);
    }

    /**
     * Auth exception response entity.
     *
     * @param request the request
     * @param e       the e
     * @return the response entity
     */
    @ExceptionHandler(AuthException.class)
    public ResponseEntity<ErrorInfo> authException(HttpServletRequest request, AuthException e) {
        logException(e);
        return new ResponseEntity<>(errorInfo(request, e), HttpStatus.UNAUTHORIZED);
    }

    /**
     * Resource not found response entity.
     *
     * @param request the request
     * @param e       the e
     * @return the response entity
     */
    @ExceptionHandler(FirebaseAuthException.class)
    public ResponseEntity<ErrorInfo> resourceNotFound(HttpServletRequest request, FirebaseAuthException e) {
        logException(e);
        return new ResponseEntity<>(errorInfo(request, e), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Method argument not valid exception response entity.
     *
     * @param request the request
     * @param e       the e
     * @return the response entity
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorInfo> methodArgumentNotValidException(HttpServletRequest request, MethodArgumentNotValidException e) {
        logException(e);

        List<FieldError> fieldErrors = e.getBindingResult().getFieldErrors();

        List<String> errorMessages = fieldErrors.stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .collect(Collectors.toList());

        ErrorInfo errorInfo = new ErrorInfo("Validation failed", request.getRequestURI(), errorMessages);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorInfo);
    }

    /**
     * Resource not found response entity.
     *
     * @param request the request
     * @param e       the e
     * @return the response entity
     */
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorInfo> resourceNotFound(HttpServletRequest request, NotFoundException e) {
        return new ResponseEntity<>(errorInfo(request, e), HttpStatus.NOT_FOUND);
    }
}
