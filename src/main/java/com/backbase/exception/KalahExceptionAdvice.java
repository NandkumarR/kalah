package com.backbase.exception;

import com.backbase.model.ErrorResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * @author nandk
 * Controller Advicer that maps custom error exception {@link KalahValidationException} object to custom JSON Response object {@link ErrorResponse}
 */
@RestControllerAdvice
@Order(value = Ordered.HIGHEST_PRECEDENCE)
public class KalahExceptionAdvice {

    @ExceptionHandler(KalahValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleCustomException(KalahValidationException ex) {
        return new ErrorResponse(ex.getMessage());
    }
}
