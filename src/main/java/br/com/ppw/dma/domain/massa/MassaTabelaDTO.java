package br.com.ppw.dma.domain.massa;

import br.com.ppw.dma.domain.master.DbTable;
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
			.stream()
			.map(MassaColunaDTO::new)
			.toList();
		this.usaPessoa = entidade.getUsaPessoa();
//
//		this.name = entidade.getNome();
//		this.column = entidade.getColunas()
//			.stream()
//			.map(MassaColunaDTO::new);
//		this.opcao = entidade.getOpcao();
//		this.tamanho = info.length();
//		this.precisao = info.precision();
//		this.escala = info.scale();
	}

	/**
	 * Atualiza seus metadados com base numa extração de banco, desde que os nomes das tabelas sejam iguais.
	 * @param tabelaBanco {@link DbTable} extração de uma table de um banco
	 * @return <b>boolean</b>: sim ou não, para indicar se esse objeto foi atualizado com sucesso.
	 */
	public boolean atualizar(@NonNull DbTable tabelaBanco) {
		if(!nome.equalsIgnoreCase(tabelaBanco.tabela())) return false;
		return tabelaBanco.colunas().stream().allMatch(
			colunaDb -> this.colunas.stream().anyMatch(
				col -> col.atualizar(colunaDb)
			)
		);
	}

	@Override
	@JsonIgnore
	public boolean isUsaPessoa() {
		return usaPessoa;
	}


}
