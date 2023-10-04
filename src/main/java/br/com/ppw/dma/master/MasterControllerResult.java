package br.com.ppw.dma.master;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import org.springframework.core.NestedExceptionUtils;
import org.springframework.lang.Nullable;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoField;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
public final class MasterControllerResult {

    final Object response;
    final String log;
    public enum LogStatus {
        SUCCESSES,
        PARCIAL,
        FAIL,
        ERROR,
        EMPTY
    }
    @JsonIgnore final static String ERROR_MESSAGE =
        """  
            PROCESS SUMMARY: [%s]
                Controller %s
                Exception occur in the controller level after %d milliseconds
                Caused by %s: %s
                Call TI support to see the Stack-Trace or in the Log-Summary
                Log-Name: ...
        """;
    @JsonIgnore final static String DEFAULT_MESSAGE =
        """
            PROCESS SUMMARY: [%s]
                Controller %s
                Called %d service(s)
                Contains %d success(s) and %d fail(s)
                Processed after %d milliseconds
        """;

//    private MasterControllerResult(@NotBlank String name, List<MasterServiceResult> servicesLog) {
//        this.name = name;
//        this.errors = (int) servicesLog.stream().filter(MasterServiceResult::isError).count();
//        services.addAll(servicesLog);
//        this.status = checkFinalStatus();
//    }

    public MasterControllerResult(@Nullable Object response, @NotBlank String log) {
        this.response = response;
        this.log = log;
    }

    public static MasterControllerResult of(@Nullable Object response, @NotBlank String log) {
        return new MasterControllerResult(response, log);
    }

    public static String buildControllerLog(
        @NotBlank String controllerName,
        @NotNull List<MasterServiceResult> services,
        @NotNull Instant startTime,
        @Nullable Exception exception) {
        //--------------------------------------------------
        final AtomicInteger successes = new AtomicInteger();
        final AtomicInteger errors = new AtomicInteger();
        final long duration = convertInstantToDuration(startTime).toMillis();

        services.forEach(service -> {
            if(service.isOk()) successes.incrementAndGet();
            else errors.incrementAndGet();
        });
        if(exception != null) {
            final LogStatus status = LogStatus.ERROR;
            return errorLogMessage(
                status.toString(),
                controllerName,
                duration,
                exception);
        }
        final LogStatus status = checkFinalStatus(services.size(), errors.get());
        return defaultLogMessage(
            status.toString(),
            controllerName,
            services.size(),
            successes.get(),
            errors.get(),
            duration);
    }

    private static LogStatus checkFinalStatus(@NotNull int servicesSize, @NotNull int errors) {
        if (servicesSize == 0) return LogStatus.EMPTY;
        if (errors == 0) return LogStatus.SUCCESSES;
        return (errors >= servicesSize ? LogStatus.FAIL : LogStatus.PARCIAL);
    }

    private static String errorLogMessage(
        @NotBlank String status,
        @NotBlank String name,
        @NotNull long duration,
        @NotNull Exception exception) {
        //--------------------------------------
        final String cause = NestedExceptionUtils.getMostSpecificCause(exception).getMessage();
        return String.format(ERROR_MESSAGE, status, name, duration, exception.getClass(), cause);
    }

    private static String defaultLogMessage(
        @NotBlank String status,
        @NotBlank String name,
        @NotNull int servicesSize,
        @NotNull int successes,
        @NotNull int errors,
        @NotNull long duration) {
        //--------------------------------------
        return String.format(DEFAULT_MESSAGE, status, name, servicesSize, successes, errors, duration);
    }

    //TODO: move this to an Util class (separation of concerns)
    private static Duration convertInstantToDuration(@NotNull Instant instant) {
        return Duration.ofMillis(instant.getLong(ChronoField.MILLI_OF_SECOND));
    }

}
