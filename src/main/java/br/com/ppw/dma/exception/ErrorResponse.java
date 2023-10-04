package br.com.ppw.dma.exception;

import br.com.ppw.dma.master.MasterDtoResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

@Getter
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
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
        this.error = status.getReasonPhrase();
    }

    ErrorResponse(@NotNull HttpStatus status, @NotBlank String message, @NotBlank HttpServletRequest request) {
        this.status = status;
        this.message = message;
        this.path = request.getContextPath() + request.getServletPath();
        this.error = status.getReasonPhrase();
    }

}
