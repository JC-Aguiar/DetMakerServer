package br.com.ppw.dma.configQuery;

import br.com.ppware.api.TipoColuna;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.lang.Nullable;

import java.io.Serializable;

@Valid
@Setter
@Getter
@ToString
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FiltroSql implements Serializable {

    @Nullable
    Long id;

    @NotBlank
    String tabela;

    @NotBlank
    String coluna;

    @NotBlank
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    TipoColuna tipo;

    @NotNull @Min(0)
    Integer index;

    @NotNull
    Boolean array;

    @NotBlank
    String variavel;

    @Nullable
    ColumnInfo metaDados;


//    public FiltroSql(String coluna, String nome, boolean array, int index) {
//        this.coluna = coluna;
//        this.nome = nome;
////        this.tipo = tipo;
//        this.array = array;
//        this.index = index;
//    }

    public FiltroSql(@NonNull ConfigQueryVar queryVar) {
        this.id = queryVar.getId();
        this.coluna = queryVar.getColuna();
        this.tipo = queryVar.getTipo();
        this.variavel = queryVar.getNome();
        this.index = queryVar.getIndex();
        this.array = queryVar.getArray();
        this.metaDados = new ColumnInfo(
            queryVar.getTamanho(),
            queryVar.getPrecisao(),
            queryVar.getEscala()
        );
    }

    public String gerarValorAleatorio() {
        var valor = tipo.valorAleatorio(metaDados);
        if(!array) return valor;
        return valor + ", " + tipo.valorAleatorio(metaDados);
    }

}
