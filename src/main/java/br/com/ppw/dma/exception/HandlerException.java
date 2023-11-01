package br.com.ppw.dma.exception;

import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.time.Instant;

import static br.com.ppw.dma.util.FormatString.LINHA_HORINZONTAL;

@ControllerAdvice
public class HandlerException {

    public static final String ERRO_PADRAO = "Erro inesperado ocorreu durante o processo.\n" +
        "Veja no log para mais detalhes (%s).\n" +
        "Mensagem do erro: %s.";

    public static final String ERRO_ALVO_AUSENTE = "Não existe nenhum registro na base de dados " +
        "para o que foi solicitado";

    public static final String ERRO_INTEGRIDADE = "A requisição viola uma ou mais regras de " +
        "integridade no banco de dados.\n" +
        "Motivo: ";

    public static final String ERRO_DUPLICIDADE = "O registro que queres salvar no banco já existe.";

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> mainHandler(Exception exception, WebRequest request) {
        String id = "";
        String message = "";
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;

        switch(exception.getClass().getSimpleName()) {
            case "NoSuchElementException" -> {
                message = ERRO_ALVO_AUSENTE;
                status = HttpStatus.NOT_FOUND;
            }
            case "ConstraintViolationException" -> {
                message = ERRO_INTEGRIDADE + exception.getMessage() + ".";
                status = HttpStatus.CONFLICT;
            }
            case "DuplicatedRecordException" -> {
                message = ERRO_DUPLICIDADE;
                status = HttpStatus.CONFLICT;
            }
            default -> {
                id = "#" + Instant.now().toEpochMilli();
                message = String.format(ERRO_PADRAO, id, exception.getMessage());
                System.err.printf("ERRO %s %s%s%s \n",
                    id, LINHA_HORINZONTAL, LINHA_HORINZONTAL, LINHA_HORINZONTAL);
                exception.printStackTrace();
            }
        }
        return new ResponseEntity<>(message, status);
    }

    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<?> dataException(DataAccessException exception) {
        return new ResponseEntity<>(exception.getLocalizedMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

}
