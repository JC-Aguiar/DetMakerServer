package br.com.ppw.dma.domain.master;

import jakarta.annotation.Nullable;
import lombok.Builder;
import lombok.NonNull;

import java.util.Set;
import java.util.stream.Collectors;

@Builder
public record DbTable(
	@NonNull String tabela,
	@Nullable String alias,
	@NonNull Set<DbColumn> colunas) {

	public Set<String> getColumnsNames() {
		return colunas.parallelStream()
			.map(DbColumn::name)
			.collect(Collectors.toSet());
	}

}
