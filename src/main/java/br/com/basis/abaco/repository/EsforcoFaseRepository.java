package br.com.basis.abaco.repository;

import br.com.basis.abaco.domain.EsforcoFase;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Spring Data JPA repository for the EsforcoFase entity.
 */
@SuppressWarnings("unused")
public interface EsforcoFaseRepository extends JpaRepository<EsforcoFase, Long> {

}
