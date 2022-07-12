package br.com.jcaguiar.cinephiles.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.util.NoSuchElementException;

@ControllerAdvice
public class HandlerException {

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<?> mainHandler(RuntimeException exception, WebRequest request) {
        String message = "";
        HttpStatus status = HttpStatus.BAD_REQUEST;
        switch(exception.getCause().getClass().toString()) {
            case "NoSuchElementException"    -> {
                message = "Sorry. We couldn't find what you are looking for..."; //TODO: NOT WORKING!
                status = HttpStatus.NOT_FOUND;
            }
        };
        return new ResponseEntity<>(message, status);
    }

}
