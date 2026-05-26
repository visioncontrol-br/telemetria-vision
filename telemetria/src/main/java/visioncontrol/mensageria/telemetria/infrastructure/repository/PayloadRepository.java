package visioncontrol.mensageria.telemetria.infrastructure.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import visioncontrol.mensageria.telemetria.infrastructure.entity.PayloadEntity;

import java.util.UUID;

public interface PayloadRepository extends JpaRepository<PayloadEntity, UUID> {
}
