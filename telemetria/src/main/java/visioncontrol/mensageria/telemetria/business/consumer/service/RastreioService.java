package visioncontrol.mensageria.telemetria.business.consumer.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import visioncontrol.mensageria.telemetria.business.consumer.dto.RastreioPayloadDTO;
import visioncontrol.mensageria.telemetria.infrastructure.entity.PosicoesVeiculosEntity;
import visioncontrol.mensageria.telemetria.infrastructure.entity.VeiculosEntity;
import visioncontrol.mensageria.telemetria.infrastructure.repository.PosicoesVeiculosRepository;
import visioncontrol.mensageria.telemetria.infrastructure.repository.VeiculosRepository;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Slf4j
@Service
@RequiredArgsConstructor
public class RastreioService {

    private final PosicoesVeiculosRepository posicoesRepository;
    private final VeiculosRepository veiculoRepository;

    // O formato exato que a Rastreamos manda: "2025/03/26 14:43:34"
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");

    @Transactional
    public void processarPosicao(RastreioPayloadDTO payload) {

        // 1. Busca o veículo pela placa
        VeiculosEntity veiculo = veiculoRepository.findByPlaca(payload.plate())
                .orElseThrow(() -> new RuntimeException("Veículo não encontrado para a placa: " + payload.plate()));

        // 2. Converte a data string para Instant (Assumindo fuso de Brasília)
        LocalDateTime localDateTime = LocalDateTime.parse(payload.date(), FORMATTER);
        var dataEvento = localDateTime.atZone(ZoneId.of("America/Sao_Paulo")).toInstant();

        // 3. Monta a entidade
        PosicoesVeiculosEntity posicao = new PosicoesVeiculosEntity();
        posicao.setEmpresa(veiculo.getEmpresa()); // Propaga a empresa automaticamente
        posicao.setVeiculo(veiculo);
        posicao.setMotorista(veiculo.getMotorista()); // Vincula o motorista atual do carro

        posicao.setEvento(payload.event());
        posicao.setGpsValido(payload.gpsValid());
        posicao.setIdRastreamento(payload.idTracking());
        posicao.setIgnicao(payload.ignition() != null && payload.ignition() == 1); // 1 = true, 0 = false

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

        // Snapshots (Fotografia do momento para o histórico não quebrar se o carro mudar de placa)
        posicao.setPlacaSnapshot(veiculo.getPlaca());
        if (veiculo.getMotorista() != null) {
            posicao.setMotoristaSnapshot(veiculo.getMotorista().getNomeCompleto());
        }

        // 4. Salva no banco
        posicoesRepository.save(posicao);
        log.info("Posição salva com sucesso! Veículo: {} | Empresa: {}", veiculo.getPlaca(), veiculo.getEmpresa().getNomeEmpresa());
    }

}
