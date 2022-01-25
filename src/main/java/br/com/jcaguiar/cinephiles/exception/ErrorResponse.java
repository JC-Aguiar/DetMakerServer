package br.com.jcaguiar.cinephiles.exception;

import br.com.jcaguiar.cinephiles.master.MasterDtoResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public final class ErrorResponse extends Throwable implements MasterDtoResponse {

    LocalDateTime date = LocalDateTime.now();
    HttpStatus status;
    String error = status.toString();
    String message;
    String path;

}
