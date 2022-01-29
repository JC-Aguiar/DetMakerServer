package br.com.jcaguiar.cinephiles.user;

import br.com.jcaguiar.cinephiles.master.MasterRecord;
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
@Entity(name = "role")
public class RoleModel {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer id;
    String role;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    UserEntity user;

    @Embedded
    MasterRecord data;

}
