package br.com.jcaguiar.cinephiles.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class AuthorizationHeaderException extends ResponseStatusException {

    private static final String MESSAGE = "Request without 'authorization' header";

    public AuthorizationHeaderException()
    {
        super(HttpStatus.UNAUTHORIZED, MESSAGE);
    }
}
