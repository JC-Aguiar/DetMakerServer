package br.com.ppw.dma.job;

import br.com.ppw.dma.evidencia.Evidencia;
import jakarta.validation.constraints.NotNull;
import lombok.NonNull;

public record JobPreparation(
    @NotNull JobInfoDTO jobInfo,
    @NotNull JobExecuteDTO jobInputs) {

    public String comandoShell() {
        return "ksh "
            + jobInfo.pathShell()
            + " "
            + jobInputs.getArgumentos();
    }

}
