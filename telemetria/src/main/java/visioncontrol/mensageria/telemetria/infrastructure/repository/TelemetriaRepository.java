package visioncontrol.mensageria.telemetria.infrastructure.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import visioncontrol.mensageria.telemetria.infrastructure.entity.TelemetriaEntity;

import java.util.UUID;

@Repository
public interface TelemetriaRepository extends JpaRepository<TelemetriaEntity, UUID> {
}
