package com.example.school.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class InvalidGradeException extends RuntimeException {
    
    public InvalidGradeException(String message) {
        super(message);
    }
    
    public InvalidGradeException(String message, Throwable cause) {
        super(message, cause);
    }
}
