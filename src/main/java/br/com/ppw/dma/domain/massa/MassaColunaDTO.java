package br.com.ppw.dma.domain.massa;

import br.com.ppw.dma.domain.master.DbColumnMetadata;
import br.com.ppw.dma.domain.master.DbColumn;
import br.com.ppw.dma.domain.master.DbTable;
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

import java.util.Optional;

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

	//TODO: mover atributos da classe aqui para a raiz?
	@Nullable
	@JsonIgnore
	DbColumnMetadata metadados;


	public MassaColunaDTO(@NonNull MassaColuna entidade) {
		this.nome = entidade.getNome();
		this.formato = entidade.getFormato();
		this.opcao = entidade.getOpcao();
//		this.tamanho = info.length();
//		this.precisao = info.precision();
//		this.escala = info.scale();
	}

	/**
	 * Atualiza seus metadados com base numa extração de banco, desde que os nomes das column sejam iguais.
	 * Recomendamos que esse método não seja chamado externamente e sim pelo
	 * {@link MassaTabelaDTO#atualizar(DbTable)}.
	 * @param colunaBanco {@link DbColumn} extração de uma coluna de um banco
	 * @return <b>boolean</b>: sim ou não, para indicar se esse objeto foi atualizado com sucesso.
	 */
	public boolean atualizar(@NonNull DbColumn colunaBanco) {
		if(!nome.equalsIgnoreCase(colunaBanco.name())) return false;

		var tamanhoAtual = Optional.ofNullable(metadados)
			.map(DbColumnMetadata::length)
			.orElse(Integer.MAX_VALUE);
		var precisaoAtual = Optional.ofNullable(metadados)
			.map(DbColumnMetadata::precision)
			.orElse(Integer.MAX_VALUE);
		var escalaAtual = Optional.ofNullable(metadados)
			.map(DbColumnMetadata::scale)
			.orElse(Integer.MAX_VALUE);

		metadados = new DbColumnMetadata(
			colunaBanco.metadata().type(),
			Math.min(tamanhoAtual, colunaBanco.getTamanho()),
			Math.min(precisaoAtual, colunaBanco.getPrecisao()),
			Math.min(escalaAtual, colunaBanco.getEscala())
		);
		return true;
	}

//	public MassaColunaDTO(@NonNull MassaColuna entidade, @NonNull DbColumnMetadata info) {
//		this.name = entidade.getNome();
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
