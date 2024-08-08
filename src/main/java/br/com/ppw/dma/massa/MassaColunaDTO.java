package br.com.ppw.dma.massa;

import br.com.ppw.dma.configQuery.ColumnInfo;
import br.com.ppware.api.FormatoMassa;
import br.com.ppware.api.MassaColunaInfo;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.annotation.Nullable;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.experimental.FieldDefaults;

import static lombok.AccessLevel.PRIVATE;

@Valid
@Data
@NoArgsConstructor
@FieldDefaults(level = PRIVATE)
public class MassaColunaDTO implements MassaColunaInfo {

	@NotEmpty
	String nome;

//	@NotNull @Min(0)
//	Integer tamanho;
//
//	@NotNull @Min(0)
//	Integer escala;
//
//	@NotNull @Min(0)
//	Integer precisao;

	@NotNull
	@JsonFormat(shape = JsonFormat.Shape.STRING)
	FormatoMassa formato;

	String opcao = "";

	@Nullable
	@JsonIgnore
	ColumnInfo metadados;


	public MassaColunaDTO(@NonNull MassaColuna entidade) {
		this.nome = entidade.getNome();
		this.formato = entidade.getFormato();
		this.opcao = entidade.getOpcao();
//		this.tamanho = info.length();
//		this.precisao = info.precision();
//		this.escala = info.scale();
	}

//	public MassaColunaDTO(@NonNull MassaColuna entidade, @NonNull ColumnInfo info) {
//		this.nome = entidade.getNome();
//		this.formato = entidade.getFormato();
//		this.opcao = entidade.getOpcao();
//		this.tamanho = info.length();
//		this.precisao = info.precision();
//		this.escala = info.scale();
//	}

	@Override
	@JsonIgnore
	public int getTamanho() {
		return metadados.getTamanho();
	}

	@Override
	@JsonIgnore
	public int getPrecisao() {
		return metadados.getPrecisao();
	}

	@Override
	@JsonIgnore
	public int getEscala() {
		return metadados.getEscala();
	}
}
