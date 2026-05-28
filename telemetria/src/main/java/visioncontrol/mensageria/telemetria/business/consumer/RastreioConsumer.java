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
import visioncontrol.mensageria.telemetria.infrastructure.entity.TelemetriaEntity;
import visioncontrol.mensageria.telemetria.infrastructure.entity.VeiculosEntity;
import visioncontrol.mensageria.telemetria.infrastructure.repository.PayloadRepository;
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
                log.info("[ROTEAMENTO] Estrutura identificada: TELEMETRIA AVANCADA.");
                processarComoTelemetria(rootNode, messageText);
            }
            else if (isEstruturaPosicaoSimples(rootNode)) {
                log.info("[ROTEAMENTO] Estrutura identificada: POSICAO SIMPLES.");
                processarComoTelemetria(rootNode, messageText); // Aqui você pode criar outro método/entidade no futuro
            }
            // Adicione novos 'else if' aqui para novas tabelas e entidades no futuro!
            else {
                log.warn("[ROTEAMENTO] Estrutura desconhecida! Nenhum padrao de Entidade atendeu aos requisitos.");
                salvarComoPayload(messageText, "Estrutura de JSON nao compativel com as entidades conhecidas.");
            }

        } catch (Exception e) {
            log.error("Erro critico no fluxo de processamento. Desviando payload. Motivo: {}", e.getMessage());
            salvarComoPayload(messageText, "Falha critica durante o roteamento: " + e.getMessage());
        }
    }

    // =========================================================================
    // REGRAS DE IDENTIFICAÇÃO DE ESTRUTURAS (ASSINATURAS DO JSON)
    // =========================================================================

    private boolean isEstruturaTelemetriaAvancada(JsonNode node) {
        // Para ser considerada Avançada, PRECISA ter o nó "telemetria" e dados básicos
        return node.has("plate") && node.has("latLong") && node.has("telemetria");
    }

    private boolean isEstruturaPosicaoSimples(JsonNode node) {
        // Para ser Posição Simples, tem que ter latitude/longitude, mas NÃO tem telemetria
        return node.has("plate") && node.has("latLong") && !node.has("telemetria");
    }

    // =========================================================================
    // PROCESSADORES ESPECÍFICOS
    // =========================================================================

    private void processarComoTelemetria(JsonNode rootNode, String messageText) throws Exception {
        String placaTratada = rootNode.get("plate").asText().split(" ")[0].trim();

        // ⚠️ ALERTA DE ARQUITETURA SAAS:
        // Mesmo roteando por estrutura, precisamos vincular ao Veículo/Empresa.
        // Se a placa não existir no sistema, os dados ficam "órfãos" e quebram o Multi-Tenant.
        VeiculosEntity veiculo = veiculoRepository.findByPlate(placaTratada).orElse(null);
        if (veiculo == null) {
            log.warn("Payload com estrutura valida, mas a placa '{}' nao existe no banco. Desviando para fallback...", placaTratada);
            salvarComoPayload(messageText, "Placa invalida ou nao cadastrada: " + placaTratada);
            return;
        }

        // Parse da Data
        String dateRaw = rootNode.has("date") ? rootNode.get("date").asText() : null;
        LocalDateTime dataEvento = (dateRaw != null && !dateRaw.trim().isEmpty())
                ? LocalDateTime.parse(dateRaw, DATE_FORMATTER)
                : LocalDateTime.now();

        // Agora sim convertemos para o DTO específico desta rota
        PayloadRastreamentoDTO dto = objectMapper.readValue(messageText, PayloadRastreamentoDTO.class);

        salvarDadosConsolidados(dto, veiculo, dataEvento);
    }

    private void salvarDadosConsolidados(PayloadRastreamentoDTO dto, VeiculosEntity veiculo, LocalDateTime dataEvento) {
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
        log.info("[SUCESSO] Linha do tempo atualizada na tabela oficial para a placa {}.", veiculo.getPlate());
    }

    private void salvarComoPayload(String rawJsonText, String motivo) {
        try {
            JsonNode jsonGenerico = objectMapper.readTree(rawJsonText);
            PayloadEntity payloadEntity = new PayloadEntity();

            // Você pode até salvar o motivo no banco futuramente para ajudar no debug
            log.warn("[FALLBACK DISPARADO] {}", motivo);
            payloadEntity.setDadosBrutos(jsonGenerico);

            payloadRepository.save(payloadEntity);
            log.info("[FALLBACK] Dados persistidos cruamente na tabela generica 'payload'.");
        } catch (Exception ex) {
            log.error("Falha critica de Sintaxe: O payload recebido nao e um formato JSON valido.");
        }
    }
}