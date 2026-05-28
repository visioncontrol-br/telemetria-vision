package visioncontrol.mensageria.telemetria.business.consumer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import visioncontrol.mensageria.telemetria.business.consumer.dto.PayloadRastreamentoDTO;
import visioncontrol.mensageria.telemetria.infrastructure.entity.*;

import visioncontrol.mensageria.telemetria.infrastructure.repository.PayloadRepository;

import visioncontrol.mensageria.telemetria.infrastructure.repository.PosicoesRepository;
import visioncontrol.mensageria.telemetria.infrastructure.repository.TelemetriaRepository;
import visioncontrol.mensageria.telemetria.infrastructure.repository.VeiculosRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
@Slf4j
@RequiredArgsConstructor
public class RastreioConsumer {

    private final VeiculosRepository veiculoRepository;
    private final PayloadRepository payloadRepository;
    private final TelemetriaRepository telemetriaRepository;
    private final PosicoesRepository posicaoRepository; // <-- Novo repositório injetado
    private final ObjectMapper objectMapper;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");

    @RabbitListener(queues = "dados-telemetria")
    public void receber(String messageText) {
        try {
            log.info("Nova mensagem capturada da fila RabbitMQ.");
            JsonNode rootNode = objectMapper.readTree(messageText);

            // =========================================================================
            // MOTOR DE ROTEAMENTO POR ESTRUTURA DO PAYLOAD
            // =========================================================================
            if (isEstruturaTelemetriaAvancada(rootNode)) {
                log.info("[ROTEAMENTO] Estrutura identificada: Módulo de Telemetria Avançada.");
                processarComoTelemetria(rootNode, messageText);
            }
            else if (isEstruturaPosicaoSimples(rootNode)) {
                log.info("[ROTEAMENTO] Estrutura identificada: Módulo de Rastreio Veicular (Posições).");
                processarComoPosicao(rootNode, messageText);
            }
            else {
                log.warn("[ROTEAMENTO] Estrutura desconhecida! Desviando para Fallback.");
                salvarComoPayload(messageText);
            }

        } catch (Exception e) {
            log.error("Erro critico no fluxo de processamento. Desviando payload. Motivo: {}", e.getMessage());
            e.printStackTrace();
            salvarComoPayload(messageText);
        }
    }

    // =========================================================================
    // IDENTIFICAÇÃO DE ASSINATURAS DO JSON
    // =========================================================================
    private boolean isEstruturaTelemetriaAvancada(JsonNode node) {
        return node.has("plate") && node.has("latLong") && node.has("telemetria");
    }

    private boolean isEstruturaPosicaoSimples(JsonNode node) {
        return node.has("plate") && node.has("latLong") && !node.has("telemetria");
    }

    // =========================================================================
    // PROCESSADORES ESPECÍFICOS POR MÓDULO
    // =========================================================================

    // 1. MÓDULO DE TELEMETRIA
    private void processarComoTelemetria(JsonNode rootNode, String messageText) throws Exception {
        String placaTratada = rootNode.get("plate").asText().split(" ")[0].trim();
        VeiculosEntity veiculo = veiculoRepository.findByPlate(placaTratada).orElse(null);

        if (veiculo == null) {
            log.warn("Placa '{}' não encontrada no sistema. Desviando Telemetria para fallback...", placaTratada);
            salvarComoPayload(messageText);
            return;
        }

        PayloadRastreamentoDTO dto = objectMapper.readValue(messageText, PayloadRastreamentoDTO.class);
        LocalDateTime dataEvento = parseData(dto.getDate());

        TelemetriaEntity entity = new TelemetriaEntity();
        if (veiculo.getEmpresaId() != null) entity.setEmpresaId(veiculo.getEmpresaId());
        if (veiculo.getId() != null) entity.setVeiculoId(veiculo.getId());

        entity.setDate(dataEvento);
        entity.setEvent(dto.getEvent());
        entity.setPlate(veiculo.getPlate());
        entity.setDriver(dto.getDriver());
        entity.setGpsValid(dto.getGpsValid());
        entity.setIgnition(dto.getIgnition());
        entity.setIdTracking(dto.getIdTracking());

        entity.setSpeed(dto.getSpeed() != null ? dto.getSpeed().intValue() : null);
        entity.setOdometer(dto.getOdometer() != null ? dto.getOdometer().longValue() : null);
        entity.setBatteryVoltage(dto.getBatteryVoltage() != null ? dto.getBatteryVoltage().doubleValue() : null);

        if (dto.getLatLong() != null) {
            LatLongEmbeddable embeddable = new LatLongEmbeddable();
            embeddable.setLatitude(dto.getLatLong().getLatitude() != null ? dto.getLatLong().getLatitude().doubleValue() : null);
            embeddable.setLongitude(dto.getLatLong().getLongitude() != null ? dto.getLatLong().getLongitude().doubleValue() : null);
            entity.setLatLong(embeddable);
        }

        if (dto.getTelemetria() != null) {
            PayloadRastreamentoDTO.TelemetriaInternalDTO telDto = dto.getTelemetria();
            entity.setTelLoaded(telDto.getLoaded());
            entity.setTelStatusFreio(telDto.getStatusFreio());
            entity.setTelEventoFrenagem(telDto.getEventoFrenagem());
            entity.setTelEventoAceleracao(telDto.getEventoAceleracao());
            entity.setTelRpm(telDto.getRpm() != null ? telDto.getRpm().intValue() : null);
            entity.setTelTemperaturaOleo(telDto.getTemperaturaOleo() != null ? telDto.getTemperaturaOleo().intValue() : null);
            entity.setTelNivelCombustivel(telDto.getNivelCombustivel() != null ? telDto.getNivelCombustivel().intValue() : null);
            entity.setTelPosicaoPedalFreio(telDto.getPosicaoPedalFreio() != null ? telDto.getPosicaoPedalFreio().intValue() : null);
            entity.setTelPosicaoPedalAcelerador(telDto.getPosicaoPedalAcelerador() != null ? telDto.getPosicaoPedalAcelerador().intValue() : null);
            entity.setTelTensao(telDto.getTensao() != null ? telDto.getTensao().doubleValue() : null);
            entity.setTelHodometro(telDto.getHodometro() != null ? telDto.getHodometro().longValue() : null);
            entity.setTelHorimetro(telDto.getHorimetro() != null ? telDto.getHorimetro().longValue() : null);
            entity.setTelTotalCombustivel(telDto.getTotalCombustivel() != null ? telDto.getTotalCombustivel().longValue() : null);
        }

        telemetriaRepository.save(entity);
        log.info("[SUCESSO] Dados salvos na tabela oficial de TELEMETRIA para a placa {}.", veiculo.getPlate());
    }

    // 2. MÓDULO DE RASTREIO VEICULAR
    private void processarComoPosicao(JsonNode rootNode, String messageText) throws Exception {
        String placaTratada = rootNode.get("plate").asText().split(" ")[0].trim();
        VeiculosEntity veiculo = veiculoRepository.findByPlate(placaTratada).orElse(null);

        if (veiculo == null) {
            log.warn("Placa '{}' não encontrada no sistema. Desviando Posição para fallback...", placaTratada);
            salvarComoPayload(messageText);
            return;
        }

        PayloadRastreamentoDTO dto = objectMapper.readValue(messageText, PayloadRastreamentoDTO.class);
        LocalDateTime dataEvento = parseData(dto.getDate());

        PosicoesEntity entity = new PosicoesEntity();
        if (veiculo.getEmpresaId() != null) entity.setEmpresaId(veiculo.getEmpresaId());
        if (veiculo.getId() != null) entity.setVeiculoId(veiculo.getId());

        entity.setDate(dataEvento);
        entity.setEvent(dto.getEvent());
        entity.setPlate(veiculo.getPlate());
        entity.setDriver(dto.getDriver());
        entity.setGpsValid(dto.getGpsValid());
        entity.setIgnition(dto.getIgnition());
        entity.setIdTracking(dto.getIdTracking());

        entity.setSpeed(dto.getSpeed() != null ? dto.getSpeed().intValue() : null);
        entity.setOdometer(dto.getOdometer() != null ? dto.getOdometer().longValue() : null);
        entity.setBatteryVoltage(dto.getBatteryVoltage() != null ? dto.getBatteryVoltage().doubleValue() : null);

        if (dto.getLatLong() != null) {
            LatLongEmbeddable embeddable = new LatLongEmbeddable();
            embeddable.setLatitude(dto.getLatLong().getLatitude() != null ? dto.getLatLong().getLatitude().doubleValue() : null);
            embeddable.setLongitude(dto.getLatLong().getLongitude() != null ? dto.getLatLong().getLongitude().doubleValue() : null);
            entity.setLatLong(embeddable);
        }

        posicaoRepository.save(entity);
        log.info("[SUCESSO] Dados salvos na tabela oficial de POSIÇÕES (Rastreio) para a placa {}.", veiculo.getPlate());
    }

    // =========================================================================
    // UTILITÁRIOS E FALLBACK
    // =========================================================================

    private LocalDateTime parseData(String dateRaw) {
        if (dateRaw != null && !dateRaw.trim().isEmpty()) {
            return LocalDateTime.parse(dateRaw, DATE_FORMATTER);
        }
        return LocalDateTime.now();
    }

    private void salvarComoPayload(String rawJsonText) {
        try {
            JsonNode jsonGenerico = objectMapper.readTree(rawJsonText);
            PayloadEntity payloadEntity = new PayloadEntity();
            payloadEntity.setDadosBrutos(jsonGenerico);
            payloadRepository.save(payloadEntity);
            log.info("[FALLBACK] Dados persistidos cruamente na tabela generica 'payload'.");
        } catch (Exception ex) {
            log.error("Falha critica de Sintaxe: O payload recebido nao e um formato JSON valido.");
        }
    }
}