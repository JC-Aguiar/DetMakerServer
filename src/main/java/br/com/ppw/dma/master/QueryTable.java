package br.com.ppw.dma.master;

import jakarta.annotation.Nullable;
import lombok.Builder;
import lombok.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Builder
public record QueryTable(
	@NonNull String table,
	@Nullable String alias,
	List<QueryColumn> columns) {

	@Override
	public List<QueryColumn> columns() {
		return Optional.ofNullable(columns)
			.orElseGet(ArrayList::new);
	}

	public Set<String> getColumnsNames() {
		return columns().parallelStream()
			.map(QueryColumn::column)
			.collect(Collectors.toSet());
	}

}
