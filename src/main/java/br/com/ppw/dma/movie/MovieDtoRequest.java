package br.com.ppw.dma.movie;

import br.com.ppw.dma.master.MasterDtoRequest;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.Transient;

import java.time.Duration;
import java.util.List;

@Value
@RequiredArgsConstructor
@SuperBuilder(toBuilder = true)
public final class MovieDtoRequest extends MovieModel implements MasterDtoRequest {

    @Transient
    @JsonIgnore
    String logo = null;
    @Transient
    @JsonIgnore
    List<PostersEntity> posters = null;
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
