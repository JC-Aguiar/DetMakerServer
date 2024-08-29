package br.com.ppw.dma.domain.master;

import br.com.ppware.api.ColunaDataInfo;
import br.com.ppware.api.TipoColuna;
import lombok.Builder;
import lombok.NonNull;

@Builder
public record DbColumnMetadata(@NonNull TipoColuna type, int length, int precision, int scale)
implements ColunaDataInfo {

	@Override
	public int getTamanho() {
		return length;
	}

	@Override
	public int getPrecisao() {
		return precision;
	}

	@Override
	public int getEscala() {
		return scale;
	}

	@Override
	public boolean equals(Object obj) {
		if(obj instanceof DbColumnMetadata other) {
			return this.type.equals(other.type)
				&& this.length == other.length
				&& this.precision == other.precision
				&& this.scale == other.scale;
		}
		return false;
	}

}
