package br.com.ppw.dma.massa;

import br.com.ppware.api.MassaTabelaInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.experimental.FieldDefaults;

import java.util.ArrayList;
import java.util.List;

import static lombok.AccessLevel.PRIVATE;

@Valid
@Data
@NoArgsConstructor
@FieldDefaults(level = PRIVATE)
public class MassaTabelaDTO implements MassaTabelaInfo {

	@NotEmpty
	String nome;

	List<MassaColunaDTO> colunas = new ArrayList<>();

	@NotNull
	Boolean usaPessoa;



	public MassaTabelaDTO(@NonNull MassaTabela entidade) {
		this.nome = entidade.getNome();
		this.colunas = entidade.getColunas()
			.parallelStream()
			.map(MassaColunaDTO::new)
			.toList();
		this.usaPessoa = entidade.getUsaPessoa();
//
//		this.nome = entidade.getNome();
//		this.colunas = entidade.getColunas()
//			.parallelStream()
//			.map(MassaColunaDTO::new);
//		this.opcao = entidade.getOpcao();
//		this.tamanho = info.length();
//		this.precisao = info.precision();
//		this.escala = info.scale();
	}

	@Override
	@JsonIgnore
	public boolean isUsaPessoa() {
		return usaPessoa;
	}


}
