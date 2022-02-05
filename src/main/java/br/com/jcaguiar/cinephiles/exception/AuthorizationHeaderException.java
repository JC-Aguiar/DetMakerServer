package br.com.jcaguiar.cinephiles.exception;

import org.springframework.http.HttpStatus;

import javax.servlet.http.HttpServletRequest;

final public class AuthorizationHeaderException extends ErrorResponse {

    private final static String MESSAGE = "Request without 'authorization' header";

    public AuthorizationHeaderException(HttpStatus status, String path) {
        super(status, MESSAGE, path);
    }

    public AuthorizationHeaderException(HttpStatus status, HttpServletRequest request) {
        super(status, MESSAGE, request);
    }
}
