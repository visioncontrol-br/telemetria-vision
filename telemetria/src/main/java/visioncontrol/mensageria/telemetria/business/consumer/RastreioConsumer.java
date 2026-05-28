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
            log.info("Nova mensagem capturada da fila RabbitMQ: {}", messageText);

            // Deserialização (O @JsonFormat adicionado no DTO resolve o parse da data com barras)
                PayloadRastreamentoDTO dto = objectMapper.readValue(messageText, PayloadRastreamentoDTO.class);
            log.info("JSON convertido em DTO com sucesso. Placa recebida: '{}', Evento: '{}'", dto.getPlate(), dto.getEvent());

            // =========================================================================
            // 1ª REGRA DE CORTE: Validação de presença da Placa
            // =========================================================================
            if (dto.getPlate() == null || dto.getPlate().trim().isEmpty()) {
                log.warn("Payload rejeitado na 1ª Regra: Propriedade 'plate' veio vazia ou nula. Enviando para fallback...");
                salvarComoPayload(messageText);
                return;
            }

            // Tratamento preventivo para isolar o texto da placa (Ex: "SCT5G36 - TELEMETRIA" -> "SCT5G36")
            String placaTratada = dto.getPlate().split(" ")[0].trim();
            log.info("Efetuando busca de Tenant no banco de dados pela placa tratada: '{}'", placaTratada);

            // =========================================================================
            // 2ª REGRA DE CORTE: Verificação de existência no Cadastro de Veículos
            // =========================================================================
            VeiculosEntity veiculo = veiculoRepository.findByPlate(placaTratada).orElse(null);

            if (veiculo == null) {
                log.warn("Payload rejeitado na 2ª Regra: A placa '{}' NÃO possui cadastro no sistema. Enviando para fallback...", placaTratada);
                salvarComoPayload(messageText);
                return;
            }

            log.info("Veículo validado! Vinculado à Empresa (Tenant ID): {}", veiculo.getEmpresaId());

            // Captura a data tratada do DTO. Caso venha nula por alguma anomalia, adota o horário atual.
            LocalDateTime dataEvento = dto.getDate() != null ? dto.getDate() : LocalDateTime.now();

            // =========================================================================
            // DIRECIONAMENTO DINÂMICO DE FLUXO (ESTRUTURA BASEADA NAS SUAS ENTITIES REAIS)
            // =========================================================================
            if (dto.getTelemetria() != null) {
                log.info("Roteando dados de Telemetria Completa da placa {} para a tabela 'telemetria'...", placaTratada);
                salvarTelemetriaAvancada(dto, dataEvento);
            } else {
                log.info("Roteando dados de Posição Simples da placa {} para a tabela 'posicao_veiculo'...", placaTratada);
                salvarPosicaoSimples(dto, dataEvento);
            }

        } catch (Exception e) {
            // =========================================================================
            // 3ª REGRA DE CORTE: Erros genéricos de runtime (Tabelas erradas, colunas inválidas)
            // =========================================================================
            log.error("Erro crítico no fluxo de processamento. Desviando payload. Motivo: {}", e.getMessage());
            e.printStackTrace(); // Cospe a pilha de erro detalhada no console para debug
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

        // Conversões numéricas seguras de BigDecimal para tipos primitivos/wrappers
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
        log.info("Posição geográfica registrada com sucesso no banco de dados.");
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

        // Conversões numéricas seguras
        entity.setSpeed(dto.getSpeed() != null ? dto.getSpeed().intValue() : null);
        entity.setOdometer(dto.getOdometer() != null ? dto.getOdometer().longValue() : null);
        entity.setBatteryVoltage(dto.getBatteryVoltage() != null ? dto.getBatteryVoltage().doubleValue() : null);

        if (dto.getLatLong() != null) {
            LatLongEmbeddable embeddable = new LatLongEmbeddable();
            embeddable.setLatitude(dto.getLatLong().getLatitude() != null ? dto.getLatLong().getLatitude().doubleValue() : null);
            embeddable.setLongitude(dto.getLatLong().getLongitude() != null ? dto.getLatLong().getLongitude().doubleValue() : null);
            entity.setLatLong(embeddable);
        }

        // Mapeamento das propriedades do sub-bloco "telemetria"
        PayloadRastreamentoDTO.TelemetriaInternalDTO telDto = dto.getTelemetria();
        entity.setTelLoaded(telDto.getLoaded());
        entity.setTelStatusFreio(telDto.getStatusFreio());
        entity.setTelEventoFrenagem(telDto.getEventoFrenagem());
        entity.setTelEventoAceleracao(telDto.getEventoAceleracao());

        // Conversão dos BigDecimals internos da telemetria
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
        log.info("Registro completo de Telemetria Mecânica gravado com sucesso no banco de dados.");
    }

    private void salvarComoPayload(String rawJsonText) {
        try {
            JsonNode jsonGenerico = objectMapper.readTree(rawJsonText);

            PayloadEntity payloadEntity = new PayloadEntity();
            payloadEntity.setDadosBrutos(jsonGenerico);

            payloadRepository.save(payloadEntity);
            log.info("[FALLBACK] Dados persistidos cruamente na tabela genérica 'payload'.");
        } catch (Exception ex) {
            log.error("Falha crítica de Sintaxe: O payload recebido não é sequer um formato JSON válido. Descartando pacote.");
        }
    }
}