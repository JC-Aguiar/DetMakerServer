package br.com.ppw.dma.system;

import lombok.Getter;

import java.util.Arrays;

@Getter
public enum ExitCodes {

    SUCCESS(0, "Success"),
    GENERAL_ERROR(1, "General Error"),
    MISUSE(2, "Misuse of Shell Built-in"),
    PERMISSION_DENIED(3, "Permission Denied"),
    INVALID_INPUT(4, "Invalid Input"),
    CONFIGURATION_ERROR(5, "Configuration Error"),
    CANNOT_EXECUTE(126, "Cannot Execute"),
    USER_INTERRUPT(130, "User Interrupt");

    public final int code;
    public final String description;

    ExitCodes(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public static String getDescriptionFromCode(int code) {
        return Arrays.stream(ExitCodes.values())
            .filter(exc -> exc.code == code)
            .map(ExitCodes::getDescription)
            .findFirst()
            .orElse("??? (No description registry)");
    }

}
