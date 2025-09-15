package com.bookapi.book_api.exception;

import com.bookapi.book_api.dto.generated.Error;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

@ControllerAdvice // Makes the class a global handler for the whole application.
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class) // Marks the class as handler for this exception
        // Note - only handles the exception if it isn't caught elsewhere
    public ResponseEntity<Error> handleResourceNotFoundException(
            ResourceNotFoundException ex, WebRequest request
    ) { // Create a custom Error DTO from the generated Error DTO
        Error errorDetails = new Error();
        errorDetails.setError(ex.getMessage());

        // Return the custom DTO along with the 404 response
        return new ResponseEntity<>(errorDetails, HttpStatus.NOT_FOUND);
    }
}
