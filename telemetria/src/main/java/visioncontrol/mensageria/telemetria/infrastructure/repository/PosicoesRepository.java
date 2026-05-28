package visioncontrol.mensageria.telemetria.infrastructure.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import visioncontrol.mensageria.telemetria.infrastructure.entity.PosicoesEntity;

import java.util.UUID;

public interface PosicoesRepository extends JpaRepository<PosicoesEntity,UUID>{}
