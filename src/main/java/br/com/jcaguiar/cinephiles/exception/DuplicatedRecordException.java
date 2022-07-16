package br.com.jcaguiar.cinephiles.exception;

public class DuplicatedRecordException extends Throwable {

    private static final String MESSAGE = "The record you want to save in the Database already exist.";

    public DuplicatedRecordException() {
        super(MESSAGE);
    }

}
