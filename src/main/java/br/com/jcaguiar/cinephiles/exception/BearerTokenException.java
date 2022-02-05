package br.com.jcaguiar.cinephiles.exception;

import org.springframework.http.HttpStatus;

import javax.servlet.http.HttpServletRequest;

public class BearerTokenException extends ErrorResponse{

    private final static String MESSAGE = "Request's 'authorization' header doesn't contain 'bearer'";

    public BearerTokenException(HttpStatus status, String path) {
        super(status, MESSAGE, path);
    }

    public BearerTokenException(HttpStatus status, HttpServletRequest request) {
        super(status, MESSAGE, request);
    }
}
