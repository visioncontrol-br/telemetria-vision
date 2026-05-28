package visioncontrol.mensageria.telemetria.business.consumer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import visioncontrol.mensageria.telemetria.business.consumer.dto.PayloadRastreamentoDTO;
import visioncontrol.mensageria.telemetria.infrastructure.entity.LatLongEmbeddable;
import visioncontrol.mensageria.telemetria.infrastructure.entity.PayloadEntity;
import visioncontrol.mensageria.telemetria.infrastructure.entity.PosicoesEntity;
import visioncontrol.mensageria.telemetria.infrastructure.entity.TelemetriaEntity;
import visioncontrol.mensageria.telemetria.infrastructure.entity.VeiculosEntity;
import visioncontrol.mensageria.telemetria.infrastructure.repository.PayloadRepository;
import visioncontrol.mensageria.telemetria.infrastructure.repository.PosicoesRepository;
import visioncontrol.mensageria.telemetria.infrastructure.repository.TelemetriaRepository;
import visioncontrol.mensageria.telemetria.infrastructure.repository.VeiculosRepository;

import java.time.LocalDateTime;

@Component
@Slf4j
@RequiredArgsConstructor
public class RastreioConsumer {

    private final VeiculosRepository veiculoRepository;
    private final PayloadRepository payloadRepository;
    private final PosicoesRepository posicoesRepository;
    private final TelemetriaRepository telemetriaRepository;
    private final ObjectMapper objectMapper;

    @RabbitListener(queues = "dados-telemetria")
    public void receber(String messageText) {
        try {
            PayloadRastreamentoDTO dto = objectMapper.readValue(messageText, PayloadRastreamentoDTO.class);

            // 1ª Regra de Corte: O payload tem uma placa preenchida?
            if (dto.getPlate() == null || dto.getPlate().trim().isEmpty()) {
                log.warn("Payload recebido sem Placa. Desviando para a tabela payload...");
                salvarComoPayload(messageText);
                return;
            }

            // Tratamento preventivo para as variações de string vindas de fornecedores
            String placaTratada = dto.getPlate().split(" ")[0].trim();

            // Resolução Dinâmica de Tenant pela Placa do Veículo
            VeiculosEntity veiculo = veiculoRepository.findByPlate(placaTratada).orElse(null);

            // 2ª Regra de Corte: A placa existe no nosso banco?
            if (veiculo == null) {
                log.warn("Telemetria Rejeitada: A placa {} não está cadastrada. Desviando payload...", placaTratada);
                salvarComoPayload(messageText);
                return;
            }

            // O DTO já faz o parse automático para LocalDateTime! Usamos direto.
            LocalDateTime dataEvento = dto.getDate() != null ? dto.getDate() : LocalDateTime.now();

            // =========================================================================
            // ESTRATÉGIA DE RAMIFICAÇÃO DE FLUXO (FILTRADO PELO SEU DTO REAL)
            // =========================================================================
            if (dto.getTelemetria() != null) {
                log.info("Roteando evento {} da placa {} para a tabela telemetria", dto.getEvent(), placaTratada);
                salvarTelemetriaAvancada(dto, dataEvento);
            } else {
                log.info("Roteando evento {} da placa {} para a tabela posicoes", dto.getEvent(), placaTratada);
                salvarPosicaoSimples(dto, dataEvento);
            }

        } catch (Exception e) {
            log.error("Falha ao processar telemetria. Desviando para a tabela payload. Motivo: {}", e.getMessage(), e);
            e.printStackTrace();
            salvarComoPayload(messageText);
        }
    }

    private void salvarPosicaoSimples(PayloadRastreamentoDTO dto, LocalDateTime dataEvento) {
        PosicoesEntity entity = new PosicoesEntity();
        entity.setDate(dataEvento);
        entity.setEvent(dto.getEvent());
        entity.setPlate(dto.getPlate());
        entity.setDriver(dto.getDriver());
        entity.setGpsValid(dto.getGpsValid());
        entity.setIgnition(dto.getIgnition());
        entity.setIdTracking(dto.getIdTracking());

        // Conversões explícitas e seguras de BigDecimal vindo do seu DTO
        entity.setSpeed(dto.getSpeed() != null ? dto.getSpeed().intValue() : null);
        entity.setOdometer(dto.getOdometer() != null ? dto.getOdometer().longValue() : null);
        entity.setBatteryVoltage(dto.getBatteryVoltage() != null ? dto.getBatteryVoltage().doubleValue() : null);

        if (dto.getLatLong() != null) {
            LatLongEmbeddable embeddable = new LatLongEmbeddable();
            embeddable.setLatitude(dto.getLatLong().getLatitude() != null ? dto.getLatLong().getLatitude().doubleValue() : null);
            embeddable.setLongitude(dto.getLatLong().getLongitude() != null ? dto.getLatLong().getLongitude().doubleValue() : null);
            entity.setLatLong(embeddable);
        }

        posicoesRepository.save(entity);
    }

    private void salvarTelemetriaAvancada(PayloadRastreamentoDTO dto, LocalDateTime dataEvento) {
        TelemetriaEntity entity = new TelemetriaEntity();
        entity.setDate(dataEvento);
        entity.setEvent(dto.getEvent());
        entity.setPlate(dto.getPlate());
        entity.setDriver(dto.getDriver());
        entity.setGpsValid(dto.getGpsValid());
        entity.setIgnition(dto.getIgnition());
        entity.setIdTracking(dto.getIdTracking());

        // Conversões explícitas e seguras de BigDecimal
        entity.setSpeed(dto.getSpeed() != null ? dto.getSpeed().intValue() : null);
        entity.setOdometer(dto.getOdometer() != null ? dto.getOdometer().longValue() : null);
        entity.setBatteryVoltage(dto.getBatteryVoltage() != null ? dto.getBatteryVoltage().doubleValue() : null);

        if (dto.getLatLong() != null) {
            LatLongEmbeddable embeddable = new LatLongEmbeddable();
            embeddable.setLatitude(dto.getLatLong().getLatitude() != null ? dto.getLatLong().getLatitude().doubleValue() : null);
            embeddable.setLongitude(dto.getLatLong().getLongitude() != null ? dto.getLatLong().getLongitude().doubleValue() : null);
            entity.setLatLong(embeddable);
        }

        // Mapeamento do sub-bloco existente "telemetria" para a sua TelemetriaEntity
        PayloadRastreamentoDTO.TelemetriaInternalDTO telDto = dto.getTelemetria();
        entity.setTelLoaded(telDto.getLoaded());
        entity.setTelStatusFreio(telDto.getStatusFreio());
        entity.setTelEventoFrenagem(telDto.getEventoFrenagem());
        entity.setTelEventoAceleracao(telDto.getEventoAceleracao());

        // Trata os BigDecimals internos existentes na TelemetriaInternalDTO
        entity.setTelRpm(telDto.getRpm() != null ? telDto.getRpm().intValue() : null);
        entity.setTelTemperaturaOleo(telDto.getTemperaturaOleo() != null ? telDto.getTemperaturaOleo().intValue() : null);
        entity.setTelNivelCombustivel(telDto.getNivelCombustivel() != null ? telDto.getNivelCombustivel().intValue() : null);
        entity.setTelPosicaoPedalFreio(telDto.getPosicaoPedalFreio() != null ? telDto.getPosicaoPedalFreio().intValue() : null);
        entity.setTelPosicaoPedalAcelerador(telDto.getPosicaoPedalAcelerador() != null ? telDto.getPosicaoPedalAcelerador().intValue() : null);

        entity.setTelTensao(telDto.getTensao() != null ? telDto.getTensao().doubleValue() : null);

        entity.setTelHodometro(telDto.getHodometro() != null ? telDto.getHodometro().longValue() : null);
        entity.setTelHorimetro(telDto.getHorimetro() != null ? telDto.getHorimetro().longValue() : null);
        entity.setTelTotalCombustivel(telDto.getTotalCombustivel() != null ? telDto.getTotalCombustivel().longValue() : null);

        telemetriaRepository.save(entity);
    }

    private void salvarComoPayload(String rawJsonText) {
        try {
            JsonNode jsonGenerico = objectMapper.readTree(rawJsonText);

            PayloadEntity payloadEntity = new PayloadEntity();
            payloadEntity.setDadosBrutos(jsonGenerico);

            payloadRepository.save(payloadEntity);
            log.info("Payload genérico salvo com sucesso na tabela payload.");
        } catch (Exception ex) {
            log.error("Falha CRÍTICA: A mensagem recebida no RabbitMQ não é um JSON válido. Descartando: {}", rawJsonText);
        }
    }
}