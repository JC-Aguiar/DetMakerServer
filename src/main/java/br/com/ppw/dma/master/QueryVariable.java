package br.com.ppw.dma.master;

import lombok.NonNull;

public record QueryVariable(@NonNull String name, boolean array) {

	@Override
	public boolean equals(Object obj) {
		if(obj instanceof QueryVariable other) {
			return this.name.equals(other.name) && array == other.array;
		}
		return false;
	}
}
