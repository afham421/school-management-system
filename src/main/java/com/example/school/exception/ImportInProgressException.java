package com.example.school.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when an import operation is attempted while another import is already in progress.
 */
@ResponseStatus(HttpStatus.CONFLICT)
public class ImportInProgressException extends RuntimeException {

    public ImportInProgressException(String message) {
        super(message);
    }

    public ImportInProgressException(String message, Throwable cause) {
        super(message, cause);
    }
}
