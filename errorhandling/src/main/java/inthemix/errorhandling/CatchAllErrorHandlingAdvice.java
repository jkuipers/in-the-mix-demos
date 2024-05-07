package inthemix.errorhandling;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;

//@RestControllerAdvice
@Order // defaults to max Integer, ensure other advices have explicit lower value!
public class CatchAllErrorHandlingAdvice {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @ExceptionHandler
    ProblemDetail handleUncaughtException(ServletWebRequest request, RuntimeException e) {
        logger.warn("Unexpected error while handling {}", request.getRequest().getRequestURI(), e);
        return ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
    }

}
