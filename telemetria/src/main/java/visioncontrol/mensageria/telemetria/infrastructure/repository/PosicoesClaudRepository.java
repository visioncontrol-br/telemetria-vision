package visioncontrol.mensageria.telemetria.infrastructure.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import visioncontrol.mensageria.telemetria.infrastructure.entity.PosicoesClaudEntity;

import java.util.UUID;

public interface PosicoesClaudRepository extends JpaRepository<PosicoesClaudEntity, UUID> {
}
