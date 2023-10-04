package br.com.ppw.dma.master;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoField;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class MasterLogTimer {

    final private static Map<String, Timer> TIMER_STORE = new HashMap<>();
    private static final class Timer {
        private Instant startTime = Instant.now();
        private Duration duration;
        public Timer() {
            System.out.println("TIMER-startTime: " + this.startTime);
        }
        public Duration stop() {
            this.duration = convertInstantToDuration(startTime);
            System.out.println("TIMER-duration: " + this.duration);
            return this.duration;
        }
    }

    public static void start(@NotBlank String hashName) {
        TIMER_STORE.put(hashName, new Timer());
    }

    public static boolean check(@NotBlank String hashName) {
        return TIMER_STORE.containsKey(hashName);
    }

    public static Duration stop(@NotBlank String hashName) {
        final Timer timer = TIMER_STORE.get(hashName);
        return timer != null ? timer.stop() : Duration.ZERO;
    }

    private Duration getDuration(@NotBlank String hashName) {
        return stop(hashName);
    }

    public static Optional<Timer> getTimer(@NotBlank String hashName) {
        return Optional.ofNullable(TIMER_STORE.get(hashName)).or(Optional::empty);
    }

    public static Duration convertInstantToDuration(@NotNull Instant instant) {
        return Duration.ofMillis(instant.getLong(ChronoField.MILLI_OF_SECOND));
    }

}
