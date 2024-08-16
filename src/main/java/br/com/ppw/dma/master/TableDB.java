package br.com.ppw.dma.master;

import jakarta.annotation.Nullable;
import lombok.Builder;
import lombok.NonNull;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

@Builder
public record TableDB(
	@NonNull String tabela,
	@Nullable String alias,
	@NonNull Collection<ColumnDB> colunas) {

	public Set<String> getColumnsNames() {
		return colunas.parallelStream()
			.map(ColumnDB::nome)
			.collect(Collectors.toSet());
	}

}
