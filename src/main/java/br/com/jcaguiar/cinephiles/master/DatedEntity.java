package br.com.jcaguiar.cinephiles.master;

import lombok.*;
import lombok.experimental.FieldDefaults;

import javax.persistence.MappedSuperclass;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@Builder(toBuilder = true)
@FieldDefaults(level = AccessLevel.PROTECTED)
@MappedSuperclass
public class DatedEntity {
    Boolean active = false;
    LocalDateTime creationDate = LocalDateTime.now();
    LocalDateTime inactivationDate;
    LocalDateTime activationDate;
}

