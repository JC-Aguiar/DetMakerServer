package br.com.ppw.dma.user;

import br.com.ppw.dma.master.MasterEntity;
import br.com.ppw.dma.master.MasterRecord;
import br.com.ppw.dma.movie.MovieEntity;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import javax.persistence.*;

@Data
@NoArgsConstructor
@SuperBuilder
@Entity(name = "watchpoints")
@Table(name = "watchpoints")
public class WatchpointsEntity implements MasterEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    Integer id;

    @ToString.Exclude
    @JsonManagedReference
    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "users_id")
    UserEntity user;

    @ToString.Exclude
    @JsonManagedReference
    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "movies_id")
    MovieEntity movie;

    @Embedded
    MasterRecord data;

}
