package br.com.ppw.dma.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class ConverterToJsonException extends ResponseStatusException {

    private static final String MESSAGE = "Unexpected error while converting Field to Json";

    public ConverterToJsonException() {
        super(HttpStatus.INTERNAL_SERVER_ERROR, MESSAGE);
    }

}
