package visioncontrol.mensageria.telemetria.infrastructure.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import visioncontrol.mensageria.telemetria.infrastructure.entity.RastreioEntity;


import java.util.UUID;

public interface RastreioRepository extends JpaRepository<RastreioEntity, UUID> {
}
