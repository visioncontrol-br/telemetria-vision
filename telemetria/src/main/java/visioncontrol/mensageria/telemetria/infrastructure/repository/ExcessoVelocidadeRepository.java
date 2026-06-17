package visioncontrol.mensageria.telemetria.infrastructure.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import visioncontrol.mensageria.telemetria.infrastructure.entity.ExcessoVelocidadeEntity;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface ExcessoVelocidadeRepository extends JpaRepository<ExcessoVelocidadeEntity, UUID> {

    // Método para o painel de gestores: Ver infrações do dia em uma empresa
    List<ExcessoVelocidadeEntity> findByEmpresaIdAndDataOcorrenciaOrderByVelocidadeMaximaAtingidaDesc(UUID empresaId, LocalDate data);

}
