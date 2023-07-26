package br.com.ppw.dma.access;

import br.com.ppw.dma.master.MasterEntity;
import br.com.ppw.dma.user.UserEntity;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

import javax.persistence.*;

@Data
@NoArgsConstructor
@SuperBuilder
@FieldDefaults(level = AccessLevel.PRIVATE)
@ToString(callSuper = true)
@Entity(name = "access")
@Table(name = "access")
final public class AccessEntity extends AccessModel implements MasterEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    Integer id;

    @ToString.Exclude
    @JsonManagedReference
    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, optional = false)
    @JoinColumn(name = "users_id", nullable = false)
    UserEntity user;

}
