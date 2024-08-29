package br.com.ppw.dma.exception;

import jakarta.validation.ConstraintViolationException;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.time.Instant;
import java.util.NoSuchElementException;

import static br.com.ppw.dma.util.FormatString.LINHA_HORINZONTAL;

@ControllerAdvice
public class HandlerException {

    //TODO: mover a coleta dessas mensagens para arquivo properties/yaml

    public static final String ERRO_PADRAO = "Erro inesperado ocorreu durante o processo.\n" +
        "Veja no log para mais detalhes (%s).\n" +
        "Mensagem do exception: %s.";

    public static final String ERRO_VALOR_NULO = "Alguma informação importante durante o processo " +
        "não foi encontrada ou está nula.";

    public static final String ERRO_ALVO_AUSENTE = "Não existe nenhum registro na base de dados " +
        "para o que foi solicitado";

    public static final String ERRO_INTEGRIDADE = "A requisição viola uma ou mais regras de " +
        "integridade no banco de dados.\n" +
        "Motivo: ";

    public static final String ERRO_DUPLICIDADE = "O registro que queres salvar no banco já existe.";

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> mainHandler(Throwable throwable, WebRequest request) {
        String id = "";
        String message = "";
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;

        //DataAccessException: Unable to acquire JDBC Connection -> problema de conexão ao DB-Server

        switch(throwable) {
            case NoSuchElementException clazz -> {
                message = ERRO_ALVO_AUSENTE;
                status = HttpStatus.NOT_FOUND;
            }
            case ConstraintViolationException clazz -> {
                message = ERRO_INTEGRIDADE + throwable.getMessage() + ".";
                status = HttpStatus.CONFLICT;
            }
            case DuplicatedRecordException clazz -> {
                message = ERRO_DUPLICIDADE;
                status = HttpStatus.CONFLICT;
            }
            default -> {
                id = "#" + Instant.now().toEpochMilli();
                message = String.format(ERRO_PADRAO, id, throwable.getMessage());
                System.err.printf("ERRO %s %s%s%s \n",
                    id, LINHA_HORINZONTAL, LINHA_HORINZONTAL, LINHA_HORINZONTAL);
                throwable.printStackTrace();
            }
        }
        return new ResponseEntity<>(message, status);
    }

    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<?> dataException(DataAccessException exception) {
        return new ResponseEntity<>(exception.getLocalizedMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

}
