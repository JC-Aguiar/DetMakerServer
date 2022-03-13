package br.com.jcaguiar.cinephiles.movie;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PostersRepository extends JpaRepository<PostersEntity, Integer> {
}
