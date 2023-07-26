package br.com.ppw.dma.user;

import br.com.ppw.dma.access.AccessEntity;
import br.com.ppw.dma.master.MasterEntity;
import br.com.ppw.dma.master.MasterRecord;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Data
@NoArgsConstructor
@SuperBuilder
@FieldDefaults(level = AccessLevel.PRIVATE)
@ToString(callSuper = true)
@Entity(name = "users")
@Table(name = "users")
final public class UserEntity extends UserModel implements UserDetails, MasterEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    Integer id;
    String token;
    LocalDateTime tokenExpiration;
    LocalDateTime lastLogin;

    @ToString.Exclude
    @JsonBackReference
    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "id")
    @Column(name = "access_id")
    final List<AccessEntity> acesses = new ArrayList<>();

    @ToString.Exclude
    @JsonManagedReference
    @OneToMany( fetch = FetchType.EAGER,cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "id")
    @Column(name = "roles_id")
    final List<RoleEntity> authorities = new ArrayList<>();

    @ToString.Exclude
    @JsonBackReference
    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "id")
    @Column(name = "watchpoints_id")
    final List<WatchpointsEntity> moviesWatchpoints = new ArrayList<>();

    @Embedded
    MasterRecord data;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getUsername() {
        return getEmail();
    }

    @Override
    public boolean isAccountNonExpired() {
        return false;
    }

    @Override
    public boolean isAccountNonLocked() {
        return false;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
