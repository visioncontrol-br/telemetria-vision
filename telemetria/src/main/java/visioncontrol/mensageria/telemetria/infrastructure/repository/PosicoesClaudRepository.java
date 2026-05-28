package visioncontrol.mensageria.telemetria.infrastructure.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PosicoesClaudRepository extends JpaRepository<PosicoesClaudEntity, UUID> {
}
