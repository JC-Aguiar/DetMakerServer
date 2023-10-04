package br.com.ppw.dma.master;

import br.com.ppw.dma.exception.ProcessException;
import br.com.ppw.dma.util.ConsoleLogAspect;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import org.springframework.core.NestedExceptionUtils;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoField;

/**
 * Contains the official structure for all service`s processes, and they possibly result.
 * The {@link ConsoleLogAspect}.serviceAspect trace any service`s process and compile
 * then inside this class.
 * This structure is necessary for {@link ConsoleLogAspect}.controllerAspect to know how
 * much processes succeed or failed and identify them in the report log.
 */
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@FieldDefaults(level = AccessLevel.PRIVATE)
public final class MasterServiceResult {

    final String serviceName;                       //the service name
    final String methodName;                        //the method name
    final boolean error;                            //if the service encounters an error
    final Object result;                            //the returned service value or exception
    final Instant startTime;                        //the moment this service starts
    final Duration duration;                        //the service process duration
    final String log;                               //the service message log

    final static String FORMAT_SUCCESS =            //success message patter to be filled
            "%s.%s [SUCCESSES]: returned response %s after %d milliseconds";

    final static String FORMAT_ERROR =              //error message patter to be filled
            "%s.%s [FAIL]: returned %s after %d milliseconds./nCause: %s";

    /**
     * Private constructor created by static {@link MasterServiceResult}.success
     * @param serviceName {@link String} service name
     * @param result {@link Object} the returned object from service method
     */
    private MasterServiceResult(
        @NotBlank String serviceName,
        @NotBlank String methodName,
        @NotNull Instant startTime,
        @NotNull Object result) {
        //---------------------------------
        this.serviceName = serviceName;
        this.methodName = methodName;
        this.error = false;
        this.startTime = startTime;
        this.result = result;
        this.duration = convertInstantToDuration(startTime);
        this.log = buildLogMessage();
    }

    /**
     * Private constructor created by static {@link MasterServiceResult}.error
     * @param serviceName {@link String} service name
     * @param e {@link Exception} exception
     */
    private MasterServiceResult(
        @NotBlank String serviceName,
        @NotBlank String methodName,
        @NotNull Instant startTime,
        @NotNull Exception e) {
        //---------------------------------
        this.serviceName = serviceName;
        this.methodName = methodName;
        this.error = true;
        this.startTime = startTime;
        this.result = e;
        this.duration = convertInstantToDuration(startTime);
        final String cause = NestedExceptionUtils.getMostSpecificCause(e).getMessage();
        this.log = buildLogMessage(cause);
    }

    public static MasterServiceResult success(
        @NotBlank String serviceName, @NotBlank String methodName,
        @NotNull Instant startTime, @NotNull Object result) {
        //--------------------------------------------------------
        return new MasterServiceResult(serviceName, methodName, startTime, result);
    }

    public static MasterServiceResult error(
        @NotBlank String serviceName, @NotBlank String methodName,
        @NotNull Instant startTime, @NotNull Exception e) {
        //--------------------------------------------------------
        return new MasterServiceResult(serviceName, methodName, startTime, e);
    }

    private String buildLogMessage() {
        return String.format(
            FORMAT_SUCCESS,
            serviceName,
            methodName,
            result.getClass().toString(),
            duration.toMillis());
    }

    private String buildLogMessage(@NotBlank String cause) {
        return String.format(
            FORMAT_ERROR,
            serviceName,
            methodName,
            result.getClass().toString(),
            duration.toMillis(),
            handleExceptionMessage(cause));
    }

    //TODO: move to an Util class (separation of concerns)
    private static String handleExceptionMessage(@NotBlank String message) {
        return message.replace("\n", ". ");
    }

    public boolean isOk() {
        return !error;
    }

    public boolean isError() {
        return error;
    }

    public void checkStatus() {
        if(error) throw new ProcessException(log);
    }

    //TODO: move to an Util class (separation of concerns)
    private static Duration convertInstantToDuration(@NotNull Instant instant) {
        return Duration.ofMillis(instant.getLong(ChronoField.MILLI_OF_SECOND));
    }

}
