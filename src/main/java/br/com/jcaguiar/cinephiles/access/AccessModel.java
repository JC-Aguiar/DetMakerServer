package br.com.jcaguiar.cinephiles.access;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

import javax.persistence.MappedSuperclass;
import javax.validation.constraints.NotBlank;

@Data
@NoArgsConstructor
@SuperBuilder
@FieldDefaults(level = AccessLevel.PRIVATE)
@MappedSuperclass
public class AccessModel {

    @NotBlank(message = "'IP' cant be empty")
    String ip;

    @NotBlank(message = "'O.S' cant be empty")
    String os;

    @NotBlank(message = "'Device' cant be empty")
    String device;

}
