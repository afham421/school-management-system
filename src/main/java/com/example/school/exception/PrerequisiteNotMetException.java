package com.example.school.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class PrerequisiteNotMetException extends RuntimeException {
    
    public PrerequisiteNotMetException(String message) {
        super(message);
    }
    
    public PrerequisiteNotMetException(String resourceName, String fieldName, Object fieldValue) {
        super(String.format("Prerequisite not met for %s with %s : '%s'", resourceName, fieldName, fieldValue));
    }
}
