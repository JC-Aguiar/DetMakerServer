package br.com.jcaguiar.cinephiles.master;

import br.com.jcaguiar.cinephiles.exception.ProcessException;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import org.springframework.core.NestedExceptionUtils;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoField;
import java.util.Optional;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProcessLine<OBJ> {

    Optional<OBJ> object;
    boolean error;
    String log;
    Optional<Class> objectClass;
    Duration duration;
    final static String FORMAT_SUCCESS = "[OK]: Success - processed in %d milliseconds";
    final static String FORMAT_ERROR = "[ERROR]: %s - processed in %d milliseconds";

    private ProcessLine(@NotNull OBJ object, @NotNull Duration duration) {
        this.object = Optional.of(object);
        this.error = false;
        this.log = "";
        this.objectClass = Optional.of(this.object.get().getClass());
        this.duration = duration;
    }

    private ProcessLine(@NotBlank String log, @NotNull Duration duration) {
        this.object = Optional.empty();
        this.error = true;
        this.log = log;
        this.objectClass = Optional.empty();
        this.duration = duration;
    }

    public static ProcessLine success(@NotNull Instant startTime, @NotNull Object object) {
        return new ProcessLine<>(object, convertTimeToDuration(startTime));
    }

    public static ProcessLine error(@NotNull Instant startTime, @NotBlank String cause) {
        return new ProcessLine<>(cause, convertTimeToDuration(startTime));
    }

    public static ProcessLine error(@NotNull Instant startTime, Exception e) {
        e.printStackTrace();
        final String message = NestedExceptionUtils.getMostSpecificCause(e).getMessage();
        return new ProcessLine<>(message, convertTimeToDuration(startTime));
    }

    private static Duration convertTimeToDuration(@NotNull Instant startTime) {
        return Duration.ofMillis(startTime.getLong(ChronoField.MILLI_OF_SECOND));
    }

    public boolean isOk() {
        return !error;
    }

    public OBJ getObject() {
        return object.orElseThrow();
    }

    public Class<?> getObjectClass() {
        final String message = "ProcessLine without object/class available";
        return objectClass.orElseThrow(() -> new ProcessException(message));
    }

    public void checkStatus() {
        if(error) throw new ProcessException(log);
    }

    public OBJ compareAndGet(@NotNull Class<?> expectedClass) {
        final String classSrting = getObjectClass().getSimpleName();
        final String message = String.format(
            "ProcessLine expects an object of class %s, but receives %s",
            classSrting, expectedClass.getSimpleName());
        if (!expectedClass.equals(getObjectClass())) throw new ProcessException(message);
        return object.orElseThrow();
    }

    public void validade(@NotNull Class<?> expectedClass) {
        checkStatus();
        compareAndGet(expectedClass);
    }

    public String getFullLog() {
        return error ? getErrorLog() : getSuccessLog();
    }

    private String getSuccessLog() {
        return String.format(FORMAT_SUCCESS, duration.toMillis());
    }

    private String getErrorLog() {
        return String.format(FORMAT_ERROR, log, duration.toMillis());
    }

    public ProcessLine generallyse(){
        return this;
    }

}
