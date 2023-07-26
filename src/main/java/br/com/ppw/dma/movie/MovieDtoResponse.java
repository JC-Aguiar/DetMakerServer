package br.com.ppw.dma.movie;

import br.com.ppw.dma.master.MasterDtoResponse;
import lombok.NoArgsConstructor;
import lombok.Value;
import lombok.experimental.SuperBuilder;

@Value
@NoArgsConstructor
@SuperBuilder(toBuilder = true)
public class MovieDtoResponse extends MovieModel implements MasterDtoResponse {
}
