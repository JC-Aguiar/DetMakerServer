package br.com.jcaguiar.cinephiles.access;

import br.com.jcaguiar.cinephiles.master.MasterServiceResult;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

import javax.persistence.MappedSuperclass;
import javax.validation.constraints.NotBlank;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Data
@SuperBuilder
@MappedSuperclass
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AccessLogModel {

    String name;
    String status;
    String log;
    Instant startTime;
    Long duration;

}
