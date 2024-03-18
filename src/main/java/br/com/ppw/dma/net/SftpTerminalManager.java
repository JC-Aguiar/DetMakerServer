package br.com.ppw.dma.net;

import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.util.ArrayList;
import java.util.List;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SftpTerminalManager {

    final List<String> consoleLog = new ArrayList<>();
    int exitCode;

    public SftpTerminalManager addPrintedLine(@NotNull String info) {
        consoleLog.add(info);
        return this;
    }

    public SftpTerminalManager addPrintedLine(@NotNull List<String> info) {
        consoleLog.addAll(info);
        return this;
    }

}
