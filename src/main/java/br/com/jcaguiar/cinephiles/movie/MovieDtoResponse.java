package br.com.jcaguiar.cinephiles.movie;

import br.com.jcaguiar.cinephiles.master.MasterDtoResponse;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Value;
import lombok.experimental.SuperBuilder;

@Value
@NoArgsConstructor
@SuperBuilder(toBuilder = true)
public class MovieDtoResponse extends MovieModel implements MasterDtoResponse {
}
