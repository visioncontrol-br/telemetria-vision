package visioncontrol.mensageria.telemetria.business.consumer.service;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import visioncontrol.mensageria.telemetria.business.consumer.dto.RastreioPayloadDTO;
import visioncontrol.mensageria.telemetria.business.consumer.dto.VeiculoCacheDTO;
import visioncontrol.mensageria.telemetria.infrastructure.cache.VeiculosCacheResolver;
import visioncontrol.mensageria.telemetria.infrastructure.entity.*;
import visioncontrol.mensageria.telemetria.infrastructure.repository.PosicoesVeiculosRepository;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Slf4j
@Service
@RequiredArgsConstructor
public class RastreioService {

    private final PosicoesVeiculosRepository posicoesRepository;
    private final EntityManager entityManager;
    private final VeiculosCacheResolver cacheResolver;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");

    @Transactional
    public void processarPosicao(RastreioPayloadDTO payload) {
        VeiculoCacheDTO veiculo = cacheResolver.buscarEstruturaVeiculo(payload.plate());
        PosicoesVeiculosEntity posicao = new PosicoesVeiculosEntity();

        // 1. Relacionamentos (Proxies) e Snapshots
        posicao.setVeiculo(entityManager.getReference(VeiculosEntity.class, veiculo.id()));
        posicao.setEmpresa(entityManager.getReference(EmpresaEntity.class, veiculo.empresaId()));
        if (veiculo.motoristaId() != null) {
            posicao.setMotorista(entityManager.getReference(PerfilEntity.class, veiculo.motoristaId()));
        }

        posicao.setPlacaSnapshot(veiculo.placaText());
        posicao.setMotoristaSnapshot(veiculo.motoristaNome());

        // 2. Dados Dinâmicos do GPS
        posicao.setDataEvento(extrairDataEvento(payload.date()));
        posicao.setEvento(payload.event());
        posicao.setGpsValido(payload.gpsValid());
        posicao.setIdRastreamento(payload.idTracking());
        posicao.setIgnicao(payload.ignition() != null ? payload.ignition() : false);
        posicao.setLatitude(payload.latitude());
        posicao.setLongitude(payload.longitude());
        posicao.setVelocidade(payload.speed());
        posicao.setTensaoBateria(payload.batteryVoltage());
        posicao.setHodometro(payload.hodometro());
        posicao.setCodigoExterno(payload.externalCode());
        posicao.setNomeAreaSegura(payload.safeAreaName());

        posicoesRepository.save(posicao);
    }

    private Instant extrairDataEvento(String date) {
        LocalDateTime localDateTime = LocalDateTime.parse(date, FORMATTER);
        return localDateTime.atZone(ZoneId.of("America/Sao_Paulo")).toInstant();
    }
}