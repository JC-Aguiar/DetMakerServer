package br.com.jcaguiar.cinephiles.master;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertTrue;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProcessLine<OBJ> {

    Optional<OBJ> object;
    boolean error;
    String errorCause;
    Class<OBJ> classObject;

    private ProcessLine(@NotNull OBJ object) {
        this.object = Optional.of(object);
        this.error = false;
        this.errorCause = "";
        this.classObject = (Class<OBJ>) this.object.get().getClass();
    }

    private ProcessLine(@NotBlank String errorCause) {
        this.object = Optional.empty();
        this.error = true;
        this.errorCause = errorCause;
        this.classObject = null;
    }

    public void compareObjects(@NotNull Class<?> obj) {
        final String message = String.format(
            "ProcessLine's object (%s) isn't what caller method expect",
            classObject);
        assertTrue(obj.equals(classObject), message);
    }

    public static ProcessLine success(@NotNull Object object) {
        return new ProcessLine<>(object);
    }

    public static ProcessLine error(@NotBlank String cause) {
        return new ProcessLine<>(cause);
    }


}
