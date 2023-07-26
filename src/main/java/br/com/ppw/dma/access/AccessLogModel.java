package br.com.ppw.dma.access;

import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

import javax.persistence.MappedSuperclass;
import java.time.Instant;

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
