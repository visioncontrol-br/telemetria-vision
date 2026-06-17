package visioncontrol.mensageria.telemetria.infrastructure.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import visioncontrol.mensageria.telemetria.infrastructure.entity.PosicoesVeiculosEntity;

import java.util.List;
import java.util.UUID;

@Repository
public interface PosicoesVeiculosRepository extends JpaRepository<PosicoesVeiculosEntity, UUID> {

    List<PosicoesVeiculosEntity> findByVeiculoIdOrderByDataEventoDesc(UUID veiculoId);

}
