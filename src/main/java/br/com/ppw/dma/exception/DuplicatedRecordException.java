package br.com.ppw.dma.exception;

public class DuplicatedRecordException extends RuntimeException { //Exception {

    private static final String MESSAGE = "The record you want to save in the Database already exist.";

    public DuplicatedRecordException() {
        super(MESSAGE);
    }

}
