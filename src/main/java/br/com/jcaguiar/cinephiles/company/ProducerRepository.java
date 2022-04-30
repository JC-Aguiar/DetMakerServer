package br.com.jcaguiar.cinephiles.company;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import javax.validation.constraints.NotBlank;

@Repository
public interface ProducerRepository extends JpaRepository<ProducerEntity, Integer> {

    ProducerEntity findByName (@NotBlank String name);

}
