package visioncontrol.mensageria.telemetria.infrastructure.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import visioncontrol.mensageria.telemetria.infrastructure.entity.PayloadEntity;

import java.util.UUID;

@Repository
public interface PayloadRepository extends JpaRepository<PayloadEntity, UUID> {
}
