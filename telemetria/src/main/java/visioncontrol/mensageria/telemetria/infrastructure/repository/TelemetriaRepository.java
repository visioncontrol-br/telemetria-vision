package visioncontrol.mensageria.telemetria.infrastructure.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import visioncontrol.mensageria.telemetria.infrastructure.entity.TelemetriaEntity;

public interface TelemetriaRepository extends JpaRepository<TelemetriaEntity, String> {
}
