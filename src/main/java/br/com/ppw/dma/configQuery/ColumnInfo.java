package br.com.ppw.dma.configQuery;

import br.com.ppware.api.ColunaDataInfo;

public record ColumnInfo(int length, int precision, int scale)
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
}
