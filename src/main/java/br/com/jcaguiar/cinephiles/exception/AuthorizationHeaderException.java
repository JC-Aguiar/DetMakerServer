package br.com.jcaguiar.cinephiles.exception;

import org.springframework.http.HttpStatus;

import javax.servlet.http.HttpServletRequest;

final public class AuthorizationHeaderException extends ErrorResponse {

    private final static String MESSAGE = "Request without 'Authorization' header";

    AuthorizationHeaderException(HttpStatus status, String path) {
        super(status, MESSAGE, path);
    }

    AuthorizationHeaderException(HttpStatus status, HttpServletRequest request) {
        super(status, MESSAGE, request);
    }
}
