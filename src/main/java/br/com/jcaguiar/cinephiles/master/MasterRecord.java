package br.com.jcaguiar.cinephiles.master;

import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

import javax.persistence.Embeddable;
import javax.persistence.MappedSuperclass;
import java.time.LocalDateTime;

@Builder(toBuilder = true)
@SuperBuilder
@Embeddable
@FieldDefaults(level = AccessLevel.PRIVATE)
final public class MasterRecord {

    Boolean active = true;
    final LocalDateTime creationDate = LocalDateTime.now();
    LocalDateTime inactivationDate;
    LocalDateTime activationDate;
}

