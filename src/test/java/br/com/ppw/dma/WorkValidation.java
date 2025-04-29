//package br.com.ppw.dma;
//
//import jakarta.annotation.Nullable;
//import lombok.Data;
//import lombok.NoArgsConstructor;
//import lombok.NonNull;
//import lombok.ToString;
//
//import java.util.Optional;
//
//@Data
////@Entity
//@NoArgsConstructor
//public class WorkValidation {
//
////    @Id
////    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
//
//    //nullable = false
//    private Integer ordem;
//
//    //nullable = false
//    private WorkValidationFilter filtro;
//
//    //nullable = false
//    private String valorEsperado;
//
//    @Nullable
//    private String variavel;
//
//
//    public WorkValidation(@NonNull WorkValidationFilter filtro, @NonNull String valorEsperado) {
//        this(filtro, valorEsperado, null);
//    }
//
//    public WorkValidation(@NonNull WorkValidationFilter filtro, @NonNull String valorEsperado, String variavel) {
//        this.filtro = filtro;
//        this.valorEsperado = valorEsperado;
//        this.variavel = variavel;
//    }
//
//    public Optional<String> getVariavel() {
//        return Optional.ofNullable(variavel);
//    }
//
//    public Optional<String> check(String text) {
//        return Optional.ofNullable(filtro.action.apply(text, valorEsperado));
//    }
//
//    public String getUnapprovedMessage(String valorObtido) {
//        return "Validação nº%d não aprovada. Filtro: %s. Esperava: %s. Obteve: %s.".formatted(
//            (ordem+1),
//            filtro,
//            valorEsperado,
//            valorObtido
//        );
//    }
//
//}
