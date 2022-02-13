package br.com.jcaguiar.cinephiles.people;

import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

import javax.persistence.*;

@Data
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@ToString(callSuper = true)
@Entity(name= "peoples")
@Table(name= "peoples")
public class PeopleEntity extends PeopleModel {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    Integer id;

}
