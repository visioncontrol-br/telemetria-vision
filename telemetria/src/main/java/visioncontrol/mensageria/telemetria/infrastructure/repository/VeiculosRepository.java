package visioncontrol.mensageria.telemetria.infrastructure.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import visioncontrol.mensageria.telemetria.infrastructure.entity.VeiculosEntity;

import java.util.Optional;
import java.util.OptionalInt;
import java.util.UUID;

public interface VeiculosRepository extends JpaRepository<VeiculosEntity, UUID> {

    Optional<VeiculosEntity> findByPlate(String plate);

}
