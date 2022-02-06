package br.com.jcaguiar.cinephiles.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class JsonToListException extends ResponseStatusException {

    private static final String MESSAGE = "Unexpected error while parsing Json to List<String>";

    public JsonToListException() {
        super(HttpStatus.INTERNAL_SERVER_ERROR, MESSAGE);
    }
}
