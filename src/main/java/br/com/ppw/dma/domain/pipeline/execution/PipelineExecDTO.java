package br.com.ppw.dma.domain.pipeline.execution;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import javax.management.InvalidAttributeValueException;
import java.util.*;
import java.util.stream.Collectors;

import static lombok.AccessLevel.PRIVATE;

@Valid
@Data
@NoArgsConstructor
@FieldDefaults(level = PRIVATE)
public class PipelineExecDTO {

    //TODO:
    // 1. `UserInfoDTO user` deverá ser substituído pelos dados no JWT
    // 2. `Long clienteId` deverá ser `String nomeCliente`
    // 3. `Long ambienteId` deverá ser `String nomeAmbiente`
    // 4. Criar query nativa para obter a entidade Ambiente com base no `nomeCliente + nomeAmbiente`
    // 5. `Long pipelineId` deverá ser `String pipelineNome`
    // 6. Obter entidade Pipeline com base no `pipelineNome + clienteNome`
    // 7. Remover `AtividadeInfoDTO.testeTipo`

    @NotNull @Min(0) Long clienteId;
    @NotNull @Min(0) Long ambienteId;
    @NotNull @Min(0) Long pipelineId;
    @NotBlank String user;
    Set<String> massas = new HashSet<>();
    @NotEmpty List<PipelineJobInputDTO> jobs = new ArrayList<>();
    Map<String, String> configuracoes = new HashMap<>();
    boolean resetMassaNoFim = true;
    boolean sobrescreverVariaveis = false;


    public List<Long> getJobsId() {
        return getJobs()
            .stream()
            .map(PipelineJobInputDTO::getId)
            .toList();
    }

    @JsonIgnore
    public Map<String, String> getConfiguracoesDinamicas() {
        return configuracoes.entrySet()
            .stream()
            .filter(variavel -> variavel.getValue().matches("^\\$[^.]*\\..*"))
            .collect(Collectors.toMap(
               Map.Entry::getKey,
               Map.Entry::getValue
            ));
    }

    @JsonIgnore
    public Map<PipelineJobInputDTO, String> getConfiguracoesConflitantes() {
        var conflitos = new HashMap<PipelineJobInputDTO, String>();
        for(var job : jobs) {
            job.getVariaveis()
                .keySet()
                .stream()
                .filter(configuracoes::containsKey)
                .forEach(variavelNome -> conflitos.put(job, variavelNome));
        }
        return conflitos;
    }

    public void validar() throws InvalidAttributeValueException {
        if(sobrescreverVariaveis) return;

        var variaveisRepetidas = getConfiguracoesConflitantes();
        if(!variaveisRepetidas.isEmpty()) return;

        var detalhes = variaveisRepetidas.keySet()
            .stream()
            .map(job -> String.format("[Job %d] '%s'", job.getOrdem(), variaveisRepetidas.get(job)))
            .collect(Collectors.joining(", "));
        throw new InvalidAttributeValueException(
            "Variáveis conflitantes com as Configurações da Pipeline: " + detalhes);
    }


    /*
    Supondo que a Massa ID 2 é seja a DELQMST e os campos DMACCT e DMACCTG constem mapeados, abaixo exemplo
    de como mapear Massa no campo `configuracoes`:
        ...
        "massas": [2, 11],
        "configuracoes": {
            "grupo": "$DELQMST.DMACCT",
            "contrato": "$DELQMST.DMACCTG"
        }
        ...
    Se o valor nas `configuracoes` não for definido usando `${ }`, o valor será considerado um literal.
    Se a table ou a coluna da table especificada não estiver disponível na massa produzida, a operação
    toda será cancelada.
     */


}
