package com.example.school.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class CapacityExceededException extends RuntimeException {
    
    public CapacityExceededException(String message) {
        super(message);
    }
    
    public CapacityExceededException(String resourceName, int capacity) {
        super(String.format("Capacity of %d exceeded for %s", capacity, resourceName));
    }
}
