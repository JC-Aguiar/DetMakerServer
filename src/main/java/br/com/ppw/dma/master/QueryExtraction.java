package br.com.ppw.dma.master;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public record QueryExtraction(Set<String> tables, Set<QueryColumn> columns) {

	@Override
	public Set<String> tables() {
		return Optional.ofNullable(tables)
			.orElseGet(HashSet::new);
	}

	@Override
	public Set<QueryColumn> columns() {
		return Optional.ofNullable(columns)
			.orElseGet(HashSet::new);
	}

}
