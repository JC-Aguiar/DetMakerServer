package br.com.jcaguiar.cinephiles.people;

import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

@Data
@NoArgsConstructor
@SuperBuilder(toBuilder = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
@ToString(callSuper = true)
@Entity(name = "producers")
@Table(name = "producers")
public class ProducerEntity {

    @EmbeddedId
    PeopleEntityId id;
}
