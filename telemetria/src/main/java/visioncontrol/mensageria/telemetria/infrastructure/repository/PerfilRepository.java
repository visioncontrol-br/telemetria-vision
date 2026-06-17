package visioncontrol.mensageria.telemetria.infrastructure.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import visioncontrol.mensageria.telemetria.infrastructure.entity.PerfilEntity;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PerfilRepository extends JpaRepository<PerfilEntity, UUID> {

    // Método essencial para rotinas de login
    Optional<PerfilEntity> findByEmail(String email);

}
