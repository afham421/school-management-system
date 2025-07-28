package com.example.school.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class EnrollmentNotActiveException extends RuntimeException {
    
    public EnrollmentNotActiveException(String message) {
        super(message);
    }
    
    public EnrollmentNotActiveException(Long enrollmentId) {
        super("Enrollment with id " + enrollmentId + " is not active");
    }
}
