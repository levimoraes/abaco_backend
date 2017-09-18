package br.com.basis.abaco.repository;

import br.com.basis.abaco.domain.Organizacao;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Spring Data JPA repository for the Organizacao entity.
 */
@SuppressWarnings("unused")
public interface OrganizacaoRepository extends JpaRepository<Organizacao, Long> {

}
