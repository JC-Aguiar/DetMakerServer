package br.com.ppw.dma.exception;


import jakarta.validation.constraints.NotBlank;

public class ProcessException extends RuntimeException {

    final static String MESSAGE = "Internal error. Process has failed   ";

    public ProcessException() {
        super(MESSAGE);
    }

    public ProcessException(@NotBlank String message) {
        super(message);
    }

}
