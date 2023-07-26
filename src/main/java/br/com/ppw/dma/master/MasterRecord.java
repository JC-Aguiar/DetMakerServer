package br.com.ppw.dma.master;

import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

import javax.persistence.Embeddable;
import javax.persistence.MappedSuperclass;
import java.time.LocalDateTime;

@NoArgsConstructor
@SuperBuilder(toBuilder = true)
@Embeddable
@FieldDefaults(level = AccessLevel.PRIVATE)
final public class MasterRecord {

    Boolean active = true;
    final LocalDateTime creationDate = LocalDateTime.now();
    final LocalDateTime lastUpdate = LocalDateTime.now();
    LocalDateTime inactivationDate;
    LocalDateTime activationDate;
}

