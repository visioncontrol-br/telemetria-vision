package visioncontrol.mensageria.telemetria.infrastructure.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import visioncontrol.mensageria.telemetria.infrastructure.entity.DepartamentoEntity;

import java.util.List;
import java.util.UUID;

@Repository
public interface DepartamentoRepository extends JpaRepository<DepartamentoEntity, UUID> {

    // Busca os departamentos pertencentes a uma empresa específica
    List<DepartamentoEntity> findByEmpresaId(UUID empresaId);

}
