package br.com.jcaguiar.cinephiles.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class UrlException extends ResponseStatusException {

    private static final String MESSAGE = "The URL exist, but the final path is invalid";

    public UrlException() {
        super(HttpStatus.NOT_FOUND, MESSAGE);
    }

}
