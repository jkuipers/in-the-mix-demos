package inthemix.errorhandling;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;

import java.net.http.HttpTimeoutException;
import java.util.concurrent.TimeoutException;

import static org.springframework.web.context.request.RequestAttributes.SCOPE_REQUEST;

@RestControllerAdvice
public class ErrorHandlingAdvice {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @ExceptionHandler({ TimeoutException.class, HttpTimeoutException.class })
    @ResponseStatus(HttpStatus.GATEWAY_TIMEOUT)
    void handle(ServletWebRequest request) {
        var cause = request.getAttribute("org.springframework.boot.web.servlet.error.DefaultErrorAttributes.ERROR", SCOPE_REQUEST);
        logger.warn("Timout while handling {}", request.getRequest().getRequestURI(), cause);
    }

    // ... imagine more exception handler methods here













//    @ExceptionHandler
    ProblemDetail handleUncaughtException(ServletWebRequest request, RuntimeException e) {
        logger.warn("Unexpected error while handling {}", request.getRequest().getRequestURI(), e);
        return ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
    }
}
