package br.com.ppw.dma.job;

import jakarta.validation.constraints.NotBlank;
import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JobRepository extends JpaRepository<Job, Long> {

    Job findByNome(@NotBlank String nome);

    List<Job> findByNomeIn(@NonNull List<String> nomes);

}
