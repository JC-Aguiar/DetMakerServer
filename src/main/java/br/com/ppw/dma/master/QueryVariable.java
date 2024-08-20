package br.com.ppw.dma.master;

import lombok.NonNull;

public record QueryVariable(@NonNull String variable, boolean array) {

	@Override
	public boolean equals(Object obj) {
		if(obj instanceof QueryVariable other) {
			return variable.equals(other.variable) && array == other.array;
		}
		return false;
	}
}
