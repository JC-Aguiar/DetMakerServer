package br.com.ppw.dma.master;

import br.com.ppware.api.ColunaDataInfo;
import br.com.ppware.api.TipoColuna;
import lombok.Builder;
import lombok.NonNull;

@Builder
public record ColumnDB(
	@NonNull String nome,
	@NonNull TipoColuna tipo,
	int tamanho,
	int precisao,
   	int escala)
implements ColunaDataInfo {

	@Override
	public int getTamanho() {
		return tamanho;
	}

	@Override
	public int getPrecisao() {
		return precisao;
	}

	@Override
	public int getEscala() {
		return escala;
	}
}
