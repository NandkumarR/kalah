package com.backbase.exception;

/**
 * @author nandk on 07/03/2021.
 * Custom exception used for different validations.
 */
public class KalahValidationException extends RuntimeException {
    public KalahValidationException(String messageError){
        super(messageError);
    }
}
