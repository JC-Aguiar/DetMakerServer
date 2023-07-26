package br.com.ppw.dma.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public abstract class BusinessRulesException extends ResponseStatusException {

    public BusinessRulesException(HttpStatus status, String reason) {
        super(status, reason);
    }

}
