package visioncontrol.mensageria.telemetria.business.consumer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import visioncontrol.mensageria.telemetria.business.dto.PayloadRastreamentoDTO;
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

    // Formatter estático para processar o padrão de data com barras "yyyy/MM/dd HH:mm:ss"
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");

    @RabbitListener(queues = "dados-telemetria")
    public void receber(String messageText) {
        try {
            log.info("Nova mensagem capturada da fila RabbitMQ: {}", messageText);

            // Leitura como árvore genérica para desativar a validação automática do DTO
            JsonNode rootNode = objectMapper.readTree(messageText);

            // Extração manual de segurança das propriedades essenciais
            String plateRaw = rootNode.has("plate") ? rootNode.get("plate").asText() : null;
            String event = rootNode.has("event") ? rootNode.get("event").asText() : null;
            String dateRaw = rootNode.has("date") ? rootNode.get("date").asText() : null;

            // =========================================================================
            // 1ª REGRA DE CORTE: Validação de presença da Placa
            // =========================================================================
            if (plateRaw == null || plateRaw.trim().isEmpty()) {
                log.warn("Payload rejeitado na 1ª Regra: Propriedade 'plate' veio vazia ou nula. Enviando para fallback...");
                salvarComoPayload(messageText);
                return;
            }

            // Isolamento do texto da placa
            String placaTratada = plateRaw.split(" ")[0].trim();
            log.info("Efetuando busca de Tenant no banco de dados pela placa tratada: '{}'", placaTratada);

            // =========================================================================
            // 2ª REGRA DE CORTE: Verificação de existência no Cadastro de Veículos
            // =========================================================================
            VeiculosEntity veiculo = veiculoRepository.findByPlate(placaTratada).orElse(null);

            if (veiculo == null) {
                log.warn("Payload rejeitado na 2ª Regra: A placa '{}' NAO possui cadastro no sistema. Enviando para fallback...", placaTratada);
                salvarComoPayload(messageText);
                return;
            }

            log.info("Veiculo validado! Vinculado a Empresa (Tenant ID): {}", veiculo.getEmpresaId());

            // =========================================================================
            // CONVERSÃO MANUAL DA DATA (Ignora o tipo configurado no DTO antigo)
            // =========================================================================
            LocalDateTime dataEvento;
            if (dateRaw != null && !dateRaw.trim().isEmpty()) {
                dataEvento = LocalDateTime.parse(dateRaw, DATE_FORMATTER);
            } else {
                dataEvento = LocalDateTime.now();
            }

            // Mapeia o restante dos dados dinâmicos do JSON para o DTO de forma interna
            PayloadRastreamentoDTO dto = objectMapper.readValue(messageText, PayloadRastreamentoDTO.class);

            // =========================================================================
            // PROCESSAMENTO UNIFICADO (Persistência centralizada em TelemetriaEntity)
            // =========================================================================
            log.info("Persistindo evento '{}' da placa '{}' na tabela unica de telemetria...", event, placaTratada);
            salvarDadosConsolidados(dto, veiculo, dataEvento);

        } catch (Exception e) {
            // =========================================================================
            // 3ª REGRA DE CORTE: Erros genéricos de runtime
            // =========================================================================
            log.error("Erro critico no fluxo de processamento. Desviando payload. Motivo: {}", e.getMessage());
            e.printStackTrace();
            salvarComoPayload(messageText);
        }
    }

    private void salvarDadosConsolidados(PayloadRastreamentoDTO dto, VeiculosEntity veiculo, LocalDateTime dataEvento) {
        TelemetriaEntity entity = new TelemetriaEntity();

        // Atribuição das chaves de relacionamento e Multi-Tenant (Tipos Integer alinhados)
        if (veiculo.getEmpresaId() != null) {
            entity.setEmpresaId(veiculo.getEmpresaId());
        }
        if (veiculo.getId() != null) {
            entity.setVeiculoId(veiculo.getId());
        }

        // Dados Base do Rastreamento
        entity.setDate(dataEvento);
        entity.setEvent(dto.getEvent());
        entity.setPlate(veiculo.getPlate()); // Garante o salvamento da placa padronizada do banco
        entity.setDriver(dto.getDriver());
        entity.setGpsValid(dto.getGpsValid());
        entity.setIgnition(dto.getIgnition());
        entity.setIdTracking(dto.getIdTracking());

        // Conversões numéricas seguras de BigDecimal para tipos primitivos/wrappers
        entity.setSpeed(dto.getSpeed() != null ? dto.getSpeed().intValue() : null);
        entity.setOdometer(dto.getOdometer() != null ? dto.getOdometer().longValue() : null);
        entity.setBatteryVoltage(dto.getBatteryVoltage() != null ? dto.getBatteryVoltage().doubleValue() : null);

        // Mapeamento de Coordenadas GPS Embutidas
        if (dto.getLatLong() != null) {
            LatLongEmbeddable embeddable = new LatLongEmbeddable();
            embeddable.setLatitude(dto.getLatLong().getLatitude() != null ? dto.getLatLong().getLatitude().doubleValue() : null);
            embeddable.setLongitude(dto.getLatLong().getLongitude() != null ? dto.getLatLong().getLongitude().doubleValue() : null);
            entity.setLatLong(embeddable);
        }

        // Condicional: Se houver telemetria avançada no JSON, os campos específicos são populados
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
        log.info("[SUCESSO] Linha do tempo atualizada na tabela 'telemetria' para a placa {}.", veiculo.getPlate());
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