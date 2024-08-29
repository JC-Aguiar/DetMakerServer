package br.com.ppw.dma.domain.master;

import lombok.NonNull;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public record QueryExtraction(
	@NonNull Set<String> tables,
	@NonNull Set<String> columns,
	@NonNull List<QueryFilter> filters) {


	public static QueryExtraction empty() {
		return new QueryExtraction(Set.of(), Set.of(), List.of());
	}

	public Set<String> getAllColumnNames() {
		var allColumns = new HashSet<>(columns);
		filters.parallelStream()
			.map(QueryFilter::column)
			.forEach(allColumns::add);
		return allColumns;
	}

}
