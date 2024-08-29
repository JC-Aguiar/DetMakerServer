package br.com.ppw.dma.domain.master;

import lombok.NonNull;

import java.util.Set;

public record QueryFilter(@NonNull String column, @NonNull Set<QueryVariable> variables) {

	@Override
	public boolean equals(Object obj) {
		if(obj instanceof QueryFilter other) {
			var sameName = this.column.equals(other.column);
			var sameVariablesSize = this.variables.size() == other.variables.size();
			if(!sameName || !sameVariablesSize) return false;
			return this.variables.parallelStream().allMatch(
				myVar -> other.variables.parallelStream().anyMatch(myVar::equals)
			);
		}
		return false;
	}
}
