package br.com.ppw.dma.master;

import lombok.NonNull;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public record QueryColumn(@NonNull String column, @NonNull Set<QueryVariable> variables) {

	@Override
	public boolean equals(Object obj) {
		if(obj instanceof QueryColumn other) {
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
