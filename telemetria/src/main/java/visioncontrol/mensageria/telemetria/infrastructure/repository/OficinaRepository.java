package visioncontrol.mensageria.telemetria.infrastructure.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import visioncontrol.mensageria.telemetria.infrastructure.entity.OficinaEntity;

import java.util.List;
import java.util.UUID;

@Repository
public interface OficinaRepository extends JpaRepository<OficinaEntity, UUID>{

        // Lista o histórico de manutenção de um carro
        List<OficinaEntity> findByVeiculoIdOrderByDataRegistroDesc(UUID veiculoId);

}
