package br.com.ppw.dma.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class BearerTokenException  extends ResponseStatusException {

    private static final String MESSAGE = "Request's 'authorization' header doesn't contain 'bearer'";

    public BearerTokenException()
    {
        super(HttpStatus.UNAUTHORIZED, MESSAGE);
    }
}
