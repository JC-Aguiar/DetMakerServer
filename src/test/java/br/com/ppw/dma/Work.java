//package br.com.ppw.dma;
//
//import jakarta.persistence.Column;
//import jakarta.persistence.EnumType;
//import jakarta.persistence.Enumerated;
//import lombok.Data;
//import lombok.NoArgsConstructor;
//import lombok.NonNull;
//
//import java.util.*;
//import java.util.stream.Collectors;
//import java.util.stream.IntStream;
//
//@Data
////@Entity
//@NoArgsConstructor
//public class Work {
//
//    public enum Protocol { SSH, HTTP }
//
//    public enum ResponseType { TEXT, XML, JSON }
//
//    //jobs[index].sucesso.is: true
//    //job[index].versao.is: "..."
//    //jobs[index].before.sql[index].sucesso.is: true
//    //jobs[index].before.sql[index].resultado.size: 0
//    //jobs[index].before.sql[index].resultado.is: "..."
//    //jobs[index].before.sql[index].resultado.isNot: "..."
//    //jobs[index].before.sql[index].resultado.contains: "..."
//    //jobs[index].before.sql[index].resultado.notContains: "..."
//    //jobs[index].before.sql[index].resultado.regex: "..."
//
//    /*
//
//    Atividade
//        Projeto projeto
//        String nome
//        AtividadeModoValidacao modo (EACH_JOB, EACH_PIPELINE, AFTER_ALL)
//        List<CriterioBanco> criteriosBanco
//        List<CriterioArquivo> criteriosArquivo
//
//    CriterioBanco
//        String nome
//        String tabela (aceita máscara)
//        String coluna (aceita máscara)
//        CriterioModoComparacao comparacao (PRESENTE, AUSENTE, IGUAL, DIFERENTE, CONTEM, NAO_CONTEM, REGEX)
//        String regex
//        String valorEsperado
//
//    CriterioArquivo
//        String nome
//        String arquivo (aceita máscara)
//        String linhas  (Primeira linha:  "1" | Última linha:  "-1" | Linhas dois, três e quatro:  "2,3,4" | Linhas cinco a nove: "5~9" | Linha iniciando com 'catalogo': "catalogo" | Todas: "0" ou null)
//        String colunas (Primeira coluna: "1" | Última coluna: "-1" | Colunas dois, três e quatro: "2,3,4" | Colunas três a nove: "3~9" | Coluna iniciada com 'DMSTATUS': "DMSTATUS" | Todas: "0" ou null)
//        Integer tamanhoColuna (null = ilimitado)
//        CriterioModoComparacao comparacao (PRESENTE, AUSENTE, IGUAL, DIFERENTE, CONTEM, NAO_CONTEM, REGEX)
//        String regex
//        String valorEsperado
//
//     */
//
////    @Id
////    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
//
//    @Enumerated(EnumType.STRING)
//    @Column(nullable = false)
//    private Protocol protocolo;
//
//    @Column(nullable = false)
//    private String comando; // Ex.: "ls -la", "curl http://example.com"
//
//    @Column(nullable = false)
//    private ResponseType tipoRetorno;
//
//    private List<WorkValidation> validacoes = new ArrayList<>();
//
////    @Column
////    private OffsetDateTime createdAt = OffsetDateTime.now(RELOGIO);
//
//    public static Work ssh(@NonNull String command, @NonNull ResponseType type, WorkValidation...validations) {
//        var work = new Work();
//        work.protocolo = Protocol.SSH;
//        work.comando = command;
//        work.tipoRetorno = type;
//        work.validacoes = collectAndSortValidations(validations);
//
//        return work;
//    }
//
//    public static Work http(@NonNull String command, @NonNull ResponseType type, WorkValidation...validations) {
//        var work = new Work();
//        work.protocolo = Protocol.HTTP;
//        work.comando = command;
//        work.tipoRetorno = type;
//        work.validacoes = collectAndSortValidations(validations);
//        return work;
//    }
//
//    private static List<WorkValidation> collectAndSortValidations(WorkValidation[] validations) {
//        return IntStream.range(0, validations.length)
//            .mapToObj(index -> Map.entry(index, validations[index]))
//            .map(entry -> {
//                var index = entry.getKey();
//                var validation = entry.getValue();
//                validation.setOrdem(index);
//                return validation;
//            })
//            .sorted(Comparator.comparing(WorkValidation::getOrdem))
//            .collect(Collectors.toList());
//    }
//
//}
