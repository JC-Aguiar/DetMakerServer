package br.com.jcaguiar.cinephiles.movie;

import br.com.jcaguiar.cinephiles.master.MasterDtoRequest;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Value;
import lombok.experimental.SuperBuilder;

@Value
@NoArgsConstructor
@Builder(toBuilder = true)
@SuperBuilder
public class MoviePostRequest extends MovieModel implements MasterDtoRequest {
}
