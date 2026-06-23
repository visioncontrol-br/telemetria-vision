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
import visioncontrol.mensageria.telemetria.infrastructure.repository.TelemetriaRepository;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Slf4j
@Service
@RequiredArgsConstructor
public class TelemetriaService {

    private final TelemetriaRepository telemetriaRepository;
    private final EntityManager entityManager;
    private final VeiculosCacheResolver cacheResolver;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");

    @Transactional
    public void processarTelemetria(RastreioPayloadDTO payload) {
        VeiculoCacheDTO veiculo = cacheResolver.buscarEstruturaVeiculo(payload.plate());
        TelemetriaEntity tel = new TelemetriaEntity();

        // 1. Relacionamentos (Proxies) e Snapshots
        tel.setVeiculo(entityManager.getReference(VeiculosEntity.class, veiculo.id()));
        tel.setEmpresa(entityManager.getReference(EmpresaEntity.class, veiculo.empresaId()));
        if (veiculo.motoristaId() != null) {
            tel.setMotorista(entityManager.getReference(PerfilEntity.class, veiculo.motoristaId()));
        }

        tel.setPlacaSnapshot(veiculo.placaText());
        tel.setMotoristaSnapshot(veiculo.motoristaNome());

        // 2. Dados Básicos e Posição
        tel.setDataEvento(extrairDataEvento(payload.date()));
        tel.setIdRastreamento(payload.idTracking());
        tel.setEvento(payload.event());
        tel.setCodigoExterno(payload.externalCode());
        tel.setNomeAreaSegura(payload.safeAreaName());

        tel.setLatitude(payload.latitude());
        tel.setLongitude(payload.longitude());
        tel.setVelocidade(payload.speed());
        tel.setAngulo(payload.angulo());
        tel.setProximidade(payload.proximidade());
        tel.setGpsValido(payload.gpsValid());

        // 3. Diagnóstico
        tel.setIgnicao(payload.ignition() != null ? payload.ignition() : false);
        tel.setRpm(payload.rpm());
        tel.setTensaoBateria(payload.batteryVoltage());
        tel.setTelLuz(payload.telLuz());
        tel.setTelChuva(payload.telChuva());
        tel.setTelMarcha(payload.telMarcha());
        tel.setTelPortas(payload.telPortas());
        tel.setTelCargaBateria(payload.telCargaBateria());
        tel.setTelTemperaturaCabine(payload.telTemperaturaCabine());
        tel.setTelTemperaturaExterna(payload.telTemperaturaExterna());
        tel.setTelTemperaturaArrefecimento(payload.telTemperaturaArrefecimento());
        tel.setNivelCombustivel(payload.nivelCombustivel());
        tel.setTotalCombustivel(payload.totalCombustivel());
        tel.setTemperaturaOleo(payload.temperaturaOleo());
        tel.setHodometro(payload.hodometro());
        tel.setHorimetro(payload.horimetro());
        tel.setTelPesoSegundoEixo(payload.telPesoSegundoEixo());
        tel.setTelPesoTerceiroEixo(payload.telPesoTerceiroEixo());

        // 4. Comportamento
        tel.setStatusFreio(payload.statusFreio());
        tel.setPosicaoPedalFreio(payload.posicaoPedalFreio());
        tel.setPosicaoPedalAcelerador(payload.posicaoPedalAcelerador());
        tel.setEventoFrenagem(payload.telEventoFrenagem());
        tel.setEventoAceleracao(payload.telEventoAceleracao());
        tel.setTelEventoRpmIn(payload.telEventoRpmIn());
        tel.setTelEventoRpmOut(payload.telEventoRpmOut());
        tel.setTelEventoSpeedIn(payload.telEventoSpeedIn());
        tel.setTelEventoSpeedOut(payload.telEventoSpeedOut());
        tel.setTelEventoOciosoIn(payload.telEventoOciosoIn());
        tel.setTelEventoOciosoOut(payload.telEventoOciosoOut());
        tel.setTelEventoBanguelaIn(payload.telEventoBanguelaIn());
        tel.setTelEventoBanguelaOut(payload.telEventoBanguelaOut());
        tel.setTelEventoChuvaSpeedIn(payload.telEventoChuvaSpeedIn());
        tel.setTelEventoChuvaSpeedOut(payload.telEventoChuvaSpeedOut());

        telemetriaRepository.save(tel);
    }

    private Instant extrairDataEvento(String date) {
        LocalDateTime localDateTime = LocalDateTime.parse(date, FORMATTER);
        return localDateTime.atZone(ZoneId.of("America/Sao_Paulo")).toInstant();
    }
}