package br.com.jcaguiar.cinephiles.movie;

import br.com.jcaguiar.cinephiles.enums.*;
import br.com.jcaguiar.cinephiles.master.MasterDtoRequest;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.Transient;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Value
@RequiredArgsConstructor
@SuperBuilder(toBuilder = true)
public final class MoviePostRequest extends MovieModel implements MasterDtoRequest {

    @Transient
    @JsonIgnore
    String logo = null;
    @Transient
    @JsonIgnore
    String posters = null;
    @Transient
    @JsonIgnore
    String font = null;
    @Transient
    @JsonIgnore
    String position = null;
    @Transient
    @JsonIgnore
    String fit = null;
    @Transient
    @JsonIgnore
    Duration duration = null;

}
