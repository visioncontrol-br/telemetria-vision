package visioncontrol.mensageria.telemetria.business.consumer.service;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// 👇 Imports adicionados para usar o novo DTO e o Resolver do Caffeine
import visioncontrol.mensageria.telemetria.business.consumer.dto.RastreioPayloadDTO;
import visioncontrol.mensageria.telemetria.business.consumer.dto.VeiculoCacheDTO;
import visioncontrol.mensageria.telemetria.infrastructure.cache.VeiculosCacheResolver;

import visioncontrol.mensageria.telemetria.infrastructure.entity.EmpresaEntity;
import visioncontrol.mensageria.telemetria.infrastructure.entity.PosicoesVeiculosEntity;
import visioncontrol.mensageria.telemetria.infrastructure.entity.VeiculosEntity;
import visioncontrol.mensageria.telemetria.infrastructure.entity.PerfilEntity;
import visioncontrol.mensageria.telemetria.infrastructure.repository.PosicoesVeiculosRepository;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Slf4j
@Service
@RequiredArgsConstructor
public class RastreioService {

    private final PosicoesVeiculosRepository posicoesRepository;
    private final EntityManager entityManager;

    // 👇 Injetamos o nosso novo componente de Cache em vez do repositório de veículos!
    private final VeiculosCacheResolver cacheResolver;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");

    @Transactional
    public void processarPosicao(RastreioPayloadDTO payload) {

        // 1. Busca do Cache usando o Caffeine (Resolve a concorrência e o TTL)
        VeiculoCacheDTO veiculoCached = cacheResolver.buscarEstruturaVeiculo(payload.plate());

        // 2. Converte a data string para Instant
        LocalDateTime localDateTime = LocalDateTime.parse(payload.date(), FORMATTER);
        var dataEvento = localDateTime.atZone(ZoneId.of("America/Sao_Paulo")).toInstant();

        // 3. Monta a entidade de Posição
        PosicoesVeiculosEntity posicao = new PosicoesVeiculosEntity();

        // 🔥 MÁGICA DO HIBERNATE: getReference cria proxies ocos. O Hibernate preenche a FK no INSERT sem dar SELECT!
        posicao.setVeiculo(entityManager.getReference(VeiculosEntity.class, veiculoCached.id()));
        posicao.setEmpresa(entityManager.getReference(EmpresaEntity.class, veiculoCached.empresaId()));

        if (veiculoCached.motoristaId() != null) {
            posicao.setMotorista(entityManager.getReference(PerfilEntity.class, veiculoCached.motoristaId()));
        }

        // 4. Preenche os dados dinâmicos do GPS
        posicao.setEvento(payload.event());
        posicao.setGpsValido(payload.gpsValid());
        posicao.setIdRastreamento(payload.idTracking());
        posicao.setIgnicao(payload.ignition() != null && payload.ignition() == 1);

        if (payload.latLong() != null) {
            posicao.setLatitude(payload.latLong().latitude());
            posicao.setLongitude(payload.latLong().longitude());
        }

        posicao.setVelocidade(payload.speed());
        posicao.setTensaoBateria(payload.batteryVoltage());
        posicao.setHodometro(payload.odometer());
        posicao.setCodigoExterno(payload.externalCode());
        posicao.setNomeAreaSegura(payload.safeAreaName());
        posicao.setDataEvento(dataEvento);

        // 5. Preenche os Snapshots usando os valores estáticos salvos com segurança no cache
        posicao.setPlacaSnapshot(veiculoCached.placaText());
        posicao.setMotoristaSnapshot(veiculoCached.motoristaNome());

        // 6. Salva no banco de dados (Apenas o INSERT será executado!)
        posicoesRepository.save(posicao);
    }
}