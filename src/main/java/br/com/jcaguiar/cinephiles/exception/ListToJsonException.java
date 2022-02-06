package br.com.jcaguiar.cinephiles.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class ListToJsonException extends ResponseStatusException {

    private static final String MESSAGE = "Unexpected error while parsing List<String> to Json";

    public ListToJsonException(HttpStatus status) {
        super(status, MESSAGE);
    }

}
