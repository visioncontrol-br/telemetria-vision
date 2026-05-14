package visioncontrol.mensageria.telemetria.infrastructure.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import visioncontrol.mensageria.telemetria.infrastructure.entity.TelemetriaEntity;


import java.util.UUID;

public interface TelemetriaRepository extends JpaRepository<TelemetriaEntity, UUID> {
}
