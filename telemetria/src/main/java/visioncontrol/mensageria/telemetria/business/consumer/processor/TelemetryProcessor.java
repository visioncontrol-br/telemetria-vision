package visioncontrol.mensageria.telemetria.business.consumer.processor;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import visioncontrol.mensageria.telemetria.business.consumer.dto.PayloadRastreamentoDTO;
import visioncontrol.mensageria.telemetria.infrastructure.entity.*;
import visioncontrol.mensageria.telemetria.infrastructure.repository.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@Slf4j
@RequiredArgsConstructor
public class TelemetryProcessor {

    private final VeiculosRepository veiculoRepository;
    private final TelemetriaRepository telemetriaRepository;
    private final PosicoesRepository posicaoRepository;
    private final PayloadRepository payloadRepository;
    private final ObjectMapper objectMapper;

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");

    @Transactional(rollbackFor = Exception.class)
    public void processTelemetria(JsonNode rootNode, String messageText) throws Exception {
        String placaTratada = extrairPlaca(rootNode);
        VeiculosEntity veiculo = veiculoRepository.findByPlate(placaTratada).orElse(null);

        if (veiculo == null) {
            log.warn("Placa '{}' não cadastrada. Desviando payload de telemetria para fallback.", placaTratada);
            processUnknown(messageText);
            return;
        }

        PayloadRastreamentoDTO dto = objectMapper.readValue(messageText, PayloadRastreamentoDTO.class);
        TelemetriaEntity entity = new TelemetriaEntity();

        preencherDadosBase(entity, veiculo, dto);

        if (dto.getTelemetria() != null) {
            PayloadRastreamentoDTO.TelemetriaInternalDTO t = dto.getTelemetria();
            entity.setTelLoaded(t.getLoaded());
            entity.setTelStatusFreio(t.getStatusFreio());
            entity.setTelEventoFrenagem(t.getEventoFrenagem());
            entity.setTelEventoAceleracao(t.getEventoAceleracao());
            entity.setTelRpm(t.getRpm() != null ? t.getRpm().intValue() : null);
            entity.setTelTemperaturaOleo(t.getTemperaturaOleo() != null ? t.getTemperaturaOleo().intValue() : null);
            entity.setTelNivelCombustivel(t.getNivelCombustivel() != null ? t.getNivelCombustivel().intValue() : null);
            entity.setTelPosicaoPedalFreio(t.getPosicaoPedalFreio() != null ? t.getPosicaoPedalFreio().intValue() : null);
            entity.setTelPosicaoPedalAcelerador(t.getPosicaoPedalAcelerador() != null ? t.getPosicaoPedalAcelerador().intValue() : null);
            entity.setTelTensao(t.getTensao() != null ? t.getTensao().doubleValue() : null);
            entity.setTelHodometro(t.getHodometro() != null ? t.getHodometro().longValue() : null);
            entity.setTelHorimetro(t.getHorimetro() != null ? t.getHorimetro().longValue() : null);
            entity.setTelTotalCombustivel(t.getTotalCombustivel() != null ? t.getTotalCombustivel().longValue() : null);
        }

        telemetriaRepository.save(entity);
        log.info("[SUCESSO] Telemetria salva para a placa {}.", veiculo.getPlate());
    }

    @Transactional(rollbackFor = Exception.class)
    public void processPosicao(JsonNode rootNode, String messageText) throws Exception {
        String placaTratada = extrairPlaca(rootNode);
        VeiculosEntity veiculo = veiculoRepository.findByPlate(placaTratada).orElse(null);

        if (veiculo == null) {
            log.warn("Placa '{}' não cadastrada. Desviando payload de posição para fallback.", placaTratada);
            processUnknown(messageText);
            return;
        }

        PayloadRastreamentoDTO dto = objectMapper.readValue(messageText, PayloadRastreamentoDTO.class);
        PosicoesEntity entity = new PosicoesEntity();

        preencherDadosBasePosicao(entity, veiculo, dto);

        posicaoRepository.save(entity);
        log.info("[SUCESSO] Posição salva para a placa {}.", veiculo.getPlate());
    }

    @Transactional
    public void processUnknown(String rawJsonText) {
        try {
            JsonNode json = objectMapper.readTree(rawJsonText);
            PayloadEntity payloadEntity = new PayloadEntity();
            payloadEntity.setDadosBrutos(json);
            payloadRepository.save(payloadEntity);
            log.info("[FALLBACK] Payload salvo na tabela genérica.");
        } catch (Exception ex) {
            log.error("Falha crítica ao salvar no fallback: payload não é um JSON válido.", ex);
        }
    }

    // =========================================================================
    // MÉTODOS AUXILIARES (Para evitar repetição de código)
    // =========================================================================

    private String extrairPlaca(JsonNode rootNode) {
        if (rootNode.hasNonNull("plate")) {
            return rootNode.get("plate").asText().split(" ")[0].trim();
        }
        return "";
    }

    private void preencherDadosBase(TelemetriaEntity entity, VeiculosEntity veiculo, PayloadRastreamentoDTO dto) {
        if (veiculo.getEmpresaId() != null) entity.setEmpresaId(veiculo.getEmpresaId());
        if (veiculo.getId() != null) entity.setVeiculoId(veiculo.getId());

        entity.setDate(parseData(dto.getDate()));
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
            LatLongEmbeddable emb = new LatLongEmbeddable();
            emb.setLatitude(dto.getLatLong().getLatitude() != null ? dto.getLatLong().getLatitude().doubleValue() : null);
            emb.setLongitude(dto.getLatLong().getLongitude() != null ? dto.getLatLong().getLongitude().doubleValue() : null);
            entity.setLatLong(emb);
        }
    }

    private void preencherDadosBasePosicao(PosicoesEntity entity, VeiculosEntity veiculo, PayloadRastreamentoDTO dto) {
        if (veiculo.getEmpresaId() != null) entity.setEmpresaId(veiculo.getEmpresaId());
        if (veiculo.getId() != null) entity.setVeiculoId(veiculo.getId());

        entity.setDate(parseData(dto.getDate()));
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
            LatLongEmbeddable emb = new LatLongEmbeddable();
            emb.setLatitude(dto.getLatLong().getLatitude() != null ? dto.getLatLong().getLatitude().doubleValue() : null);
            emb.setLongitude(dto.getLatLong().getLongitude() != null ? dto.getLatLong().getLongitude().doubleValue() : null);
            entity.setLatLong(emb);
        }
    }

    private LocalDateTime parseData(String dateRaw) {
        if (dateRaw != null && !dateRaw.trim().isEmpty()) {
            return LocalDateTime.parse(dateRaw, DATE_FORMATTER);
        }
        return LocalDateTime.now();
    }
}