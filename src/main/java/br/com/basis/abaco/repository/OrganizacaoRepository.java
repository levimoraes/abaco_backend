package br.com.basis.abaco.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.basis.abaco.domain.Organizacao;

/**
 * Spring Data JPA repository for the Organizacao entity.
 */
@SuppressWarnings("unused")
public interface OrganizacaoRepository extends JpaRepository<Organizacao, Long> {

    public List<Organizacao> findByAtivoTrue();
    Optional<Organizacao> findOneByNome(String nome);
    Optional<Organizacao> findOneByCnpj(String cnpj);
}
