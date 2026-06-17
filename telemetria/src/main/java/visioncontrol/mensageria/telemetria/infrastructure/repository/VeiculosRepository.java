package visioncontrol.mensageria.telemetria.infrastructure.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import visioncontrol.mensageria.telemetria.infrastructure.entity.VeiculosEntity;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface VeiculosRepository extends JpaRepository<VeiculosEntity, UUID> {

    // Busca um veículo específico pela placa dentro da mesma empresa
    Optional<VeiculosEntity> findByEmpresaIdAndPlaca(UUID empresaId, String placa);

    // Adicione isto dentro do VeiculoRepository
    Optional<VeiculosEntity> findByPlaca(String placa);

}
