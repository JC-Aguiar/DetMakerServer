package br.com.ppw.dma.master;

import br.com.ppware.api.ColunaDataInfo;
import lombok.Builder;
import lombok.NonNull;

import java.util.*;
import java.util.stream.Collectors;

@Builder
public record DbColumn(
	@NonNull String name,
	@NonNull DbColumnMetadata metadata,
	@NonNull Set<QueryVariable> variables)
implements ColunaDataInfo {

	public boolean addVariable(@NonNull QueryFilter queryFilter) {
		if(!this.name.equalsIgnoreCase(queryFilter.column()))
			return false;

		this.variables.addAll(queryFilter.variables());
		return true;
	}

	public Map<String, String> variablesWithRandomValues() {
		return variables().parallelStream().collect(Collectors.toMap(
			QueryVariable::name,
			variable -> metadata.type().valorAleatorio(this)
		));
	}

	@Override
	public int getTamanho() {
		return metadata.length();
	}

	@Override
	public int getPrecisao() {
		return metadata.precision();
	}

	@Override
	public int getEscala() {
		return metadata.scale();
	}

	@Override
	public boolean equals(Object obj) {
		if(obj instanceof DbColumn other) {
			var sameName = this.name.equals(other.name);
			var sameVariablesSize = this.variables().size() == other.variables().size();
			var sameMetadata = Objects.equals(this.metadata, other.metadata);
			if(!sameName || !sameVariablesSize || !sameMetadata) return false;
			return this.variables.parallelStream().allMatch(
				myVar -> other.variables.parallelStream().anyMatch(myVar::equals)
			);
		}
		return false;
	}
}
