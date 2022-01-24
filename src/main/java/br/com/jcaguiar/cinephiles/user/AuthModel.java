package br.com.jcaguiar.cinephiles.user;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import javax.persistence.*;

@Data
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity(name = "authority")
public class AuthModel {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer id;
    String role;

    @ManyToOne
    UserModel user;
}
