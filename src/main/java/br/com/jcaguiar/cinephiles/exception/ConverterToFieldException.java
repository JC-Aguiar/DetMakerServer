package br.com.jcaguiar.cinephiles.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class ConverterToFieldException extends ResponseStatusException {

    private static final String MESSAGE = "Unexpected error while converting Json to Field";

    public ConverterToFieldException() {
        super(HttpStatus.INTERNAL_SERVER_ERROR, MESSAGE);
    }
}
