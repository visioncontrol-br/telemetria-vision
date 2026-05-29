package visioncontrol.mensageria.telemetria.business.consumer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import visioncontrol.mensageria.telemetria.business.consumer.dto.PayloadRastreamentoDTO;
import visioncontrol.mensageria.telemetria.infrastructure.entity.*;
import visioncontrol.mensageria.telemetria.infrastructure.repository.*;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Component
@Slf4j
@RequiredArgsConstructor
public class RastreioConsumer {

    private final VeiculosRepository veiculoRepository;
    private final PayloadRepository payloadRepository;
    private final TelemetriaRepository telemetriaRepository;
    private final PosicoesRepository posicaoRepository;
    private final ObjectMapper objectMapper;

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");

    // Campos internos de controle que não vêm no payload
    private static final Set<String> CAMPOS_IGNORADOS = Set.of(
            "id", "criadoEm", "empresaId", "veiculoId"
    );

    @RabbitListener(queues = "dados-telemetria")
    public void receber(String messageText) {
        try {
            log.info("Nova mensagem capturada da fila RabbitMQ.");
            JsonNode rootNode = objectMapper.readTree(messageText);

            // Extrai todos os campos do payload recebido (incluindo campos aninhados)
            Set<String> camposPayload = extrairCampos(rootNode);
            log.info("[ROTEAMENTO] Campos identificados no payload: {}", camposPayload);

            // Obtém os campos mapeáveis de cada entity
            Set<String> camposTelemetria = getCamposEntity(TelemetriaEntity.class);
            Set<String> camposPosicao    = getCamposEntity(PosicoesEntity.class);

            // Verifica compatibilidade: todos os campos do payload devem existir na entity
            boolean matchTelemetria = camposTelemetria.containsAll(camposPayload);
            boolean matchPosicao    = camposPosicao.containsAll(camposPayload);

            if (matchTelemetria) {
                log.info("[ROTEAMENTO] Match com TelemetriaEntity. Processando...");
                processarComoTelemetria(rootNode, messageText);
            } else if (matchPosicao) {
                log.info("[ROTEAMENTO] Match com PosicoesEntity. Processando...");
                processarComoPosicao(rootNode, messageText);
            } else {
                log.warn("[ROTEAMENTO] Nenhuma entity compatível. Campos não mapeados: {}",
                        calcularCamposDesconhecidos(camposPayload, camposTelemetria, camposPosicao));
                salvarComoPayload(messageText);
            }

        } catch (Exception e) {
            log.error("Erro critico no processamento. Motivo: {}", e.getMessage());
            salvarComoPayload(messageText);
        }
    }

    // =========================================================================
    // EXTRAÇÃO DE CAMPOS DO JSON (incluindo objetos aninhados como latLong)
    // =========================================================================
    private Set<String> extrairCampos(JsonNode node) {
        Set<String> campos = new HashSet<>();
        node.fieldNames().forEachRemaining(campo -> {
            JsonNode filho = node.get(campo);
            if (filho.isObject()) {
                // Adiciona os campos do objeto aninhado diretamente
                filho.fieldNames().forEachRemaining(campos::add);
            } else {
                campos.add(campo);
            }
        });
        return campos;
    }

    // =========================================================================
    // EXTRAÇÃO DE CAMPOS DA ENTITY VIA REFLEXÃO
    // =========================================================================
    private Set<String> getCamposEntity(Class<?> entityClass) {
        Set<String> campos = new HashSet<>();
        Class<?> atual = entityClass;

        while (atual != null && atual != Object.class) {
            for (Field field : atual.getDeclaredFields()) {
                String nome = field.getName();
                if (!CAMPOS_IGNORADOS.contains(nome)) {
                    // Normaliza nomes com prefixo "tel" para campos de telemetria
                    // ex: telRpm → rpm, telStatusFreio → statusFreio
                    if (nome.startsWith("tel") && nome.length() > 3) {
                        String nomeSemPrefixo = Character.toLowerCase(nome.charAt(3))
                                + nome.substring(4);
                        campos.add(nomeSemPrefixo);
                    }
                    campos.add(nome);
                }
            }
            atual = atual.getSuperclass();
        }
        return campos;
    }

    private Set<String> calcularCamposDesconhecidos(Set<String> payload,
                                                    Set<String> telemetria,
                                                    Set<String> posicao) {
        Set<String> todos = new HashSet<>();
        todos.addAll(telemetria);
        todos.addAll(posicao);
        return payload.stream()
                .filter(c -> !todos.contains(c))
                .collect(Collectors.toSet());
    }

    // =========================================================================
    // PROCESSADORES
    // =========================================================================
    private void processarComoTelemetria(JsonNode rootNode, String messageText) throws Exception {
        String placaTratada = rootNode.get("plate").asText().split(" ")[0].trim();
        VeiculosEntity veiculo = veiculoRepository.findByPlate(placaTratada).orElse(null);

        if (veiculo == null) {
            log.warn("Placa '{}' não cadastrada. Desviando para fallback.", placaTratada);
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
            LatLongEmbeddable emb = new LatLongEmbeddable();
            emb.setLatitude(dto.getLatLong().getLatitude() != null ? dto.getLatLong().getLatitude().doubleValue() : null);
            emb.setLongitude(dto.getLatLong().getLongitude() != null ? dto.getLatLong().getLongitude().doubleValue() : null);
            entity.setLatLong(emb);
        }

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

    private void processarComoPosicao(JsonNode rootNode, String messageText) throws Exception {
        String placaTratada = rootNode.get("plate").asText().split(" ")[0].trim();
        VeiculosEntity veiculo = veiculoRepository.findByPlate(placaTratada).orElse(null);

        if (veiculo == null) {
            log.warn("Placa '{}' não cadastrada. Desviando para fallback.", placaTratada);
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
            LatLongEmbeddable emb = new LatLongEmbeddable();
            emb.setLatitude(dto.getLatLong().getLatitude() != null ? dto.getLatLong().getLatitude().doubleValue() : null);
            emb.setLongitude(dto.getLatLong().getLongitude() != null ? dto.getLatLong().getLongitude().doubleValue() : null);
            entity.setLatLong(emb);
        }

        posicaoRepository.save(entity);
        log.info("[SUCESSO] Posição salva para a placa {}.", veiculo.getPlate());
    }

    // =========================================================================
    // UTILITÁRIOS
    // =========================================================================
    private LocalDateTime parseData(String dateRaw) {
        if (dateRaw != null && !dateRaw.trim().isEmpty()) {
            return LocalDateTime.parse(dateRaw, DATE_FORMATTER);
        }
        return LocalDateTime.now();
    }

    private void salvarComoPayload(String rawJsonText) {
        try {
            JsonNode json = objectMapper.readTree(rawJsonText);
            PayloadEntity payloadEntity = new PayloadEntity();
            payloadEntity.setDadosBrutos(json);
            payloadRepository.save(payloadEntity);
            log.info("[FALLBACK] Payload salvo na tabela genérica.");
        } catch (Exception ex) {
            log.error("Falha crítica: payload não é um JSON válido.");
        }
    }
}