package br.com.jcaguiar.cinephiles.exception;

import br.com.jcaguiar.cinephiles.master.MasterDtoResponse;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Value
public class ErrorResponse extends Throwable implements MasterDtoResponse {

    LocalDateTime date = LocalDateTime.now();
    HttpStatus status;
    String error;
    String message;
    String path;

    ErrorResponse(@NotNull HttpStatus status, @NotBlank String message, @NotBlank String path) {
        this.status = status;
        this.message = message;
        this.path = path;
        this.error = status.toString();
    }

}
