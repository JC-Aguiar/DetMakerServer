package br.com.ppw.dma;

import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Data
//@Entity
@NoArgsConstructor
public class Work {

    public enum Protocol { SSH, HTTP }

    public enum ResponseType { TEXT, XML, JSON }


//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Protocol protocolo;

    @Column(nullable = false)
    private String comando; // Ex.: "ls -la", "curl http://example.com"

    @Column(nullable = false)
    private ResponseType tipoRetorno;

    private List<WorkValidation> validacoes = new ArrayList<>();

//    @Column
//    private OffsetDateTime createdAt = OffsetDateTime.now(RELOGIO);

    public static Work ssh(@NonNull String command, @NonNull ResponseType type, WorkValidation...validations) {
        var work = new Work();
        work.protocolo = Protocol.SSH;
        work.comando = command;
        work.tipoRetorno = type;
        work.validacoes = collectAndSortValidations(validations);

        return work;
    }

    public static Work http(@NonNull String command, @NonNull ResponseType type, WorkValidation...validations) {
        var work = new Work();
        work.protocolo = Protocol.HTTP;
        work.comando = command;
        work.tipoRetorno = type;
        work.validacoes = collectAndSortValidations(validations);
        return work;
    }

    private static List<WorkValidation> collectAndSortValidations(WorkValidation[] validations) {
        return IntStream.range(0, validations.length)
            .mapToObj(index -> Map.entry(index, validations[index]))
            .map(entry -> {
                var index = entry.getKey();
                var validation = entry.getValue();
                validation.setOrdem(index);
                return validation;
            })
            .sorted(Comparator.comparing(WorkValidation::getOrdem))
            .collect(Collectors.toList());
    }

}
