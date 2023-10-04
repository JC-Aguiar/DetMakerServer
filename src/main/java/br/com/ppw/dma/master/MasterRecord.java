package br.com.ppw.dma.master;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.hibernate.type.NumericBooleanConverter;

import java.time.LocalDateTime;

@NoArgsConstructor
//@SuperBuilder(toBuilder = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
final public class MasterRecord {

    @Convert(converter = NumericBooleanConverter.class)
    @Column(name = "ATIVO")
    Boolean ativo = true;

    @Column(name = "DATA_CRIACAO")
    final LocalDateTime dataCriacao = LocalDateTime.now();

    @Column(name = "ULTIMA_ATUALIZACAO")
    final LocalDateTime ultimaAtualizacao = LocalDateTime.now();

    @Column(name = "DATA_INATIVACAO")
    LocalDateTime dataInativacao;

    @Column(name = "DATA_ATIVACAO")
    LocalDateTime dataAtivacao;
}

