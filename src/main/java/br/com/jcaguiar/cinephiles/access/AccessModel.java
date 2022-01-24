package br.com.jcaguiar.cinephiles.access;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import javax.persistence.MappedSuperclass;

@NoArgsConstructor
@Builder(toBuilder = true)
@FieldDefaults(level = AccessLevel.PROTECTED)
@MappedSuperclass
public class AccessModel {

    String ip;
    String os;
    String device;

}
