package br.com.ppw.dma.user;

import br.com.ppw.dma.master.MasterEntity;
import br.com.ppw.dma.master.MasterRecord;
import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;
import org.springframework.security.core.GrantedAuthority;

import javax.persistence.*;

@Data
@NoArgsConstructor
@SuperBuilder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity(name = "roles")
@Table(name = "roles")
public class RoleEntity implements MasterEntity, GrantedAuthority {

    @Id @GeneratedValue(strategy = GenerationType.AUTO)
    Integer id;
    String role;

    @ToString.Exclude
    @JsonBackReference
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "users_id", nullable = false)
    UserEntity user;

    @Embedded
    MasterRecord data;

    @Override
    public String getAuthority() {
        return role;
    }
}
