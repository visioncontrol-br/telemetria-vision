package visioncontrol.mensageria.telemetria.business.consumer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import visioncontrol.mensageria.telemetria.business.dto.PayloadRastreamentoDTO;
import visioncontrol.mensageria.telemetria.infrastructure.entity.PayloadEntity;
import visioncontrol.mensageria.telemetria.infrastructure.entity.RastreioEntity;
import visioncontrol.mensageria.telemetria.infrastructure.entity.VeiculosEntity;
import visioncontrol.mensageria.telemetria.infrastructure.repository.PayloadRepository;
import visioncontrol.mensageria.telemetria.infrastructure.repository.RastreioRepository;
import visioncontrol.mensageria.telemetria.infrastructure.repository.VeiculosRepository;

import java.time.LocalDateTime;

@Component
@Slf4j
@RequiredArgsConstructor
public class RastreioConsumer {

    private final RastreioRepository repository;
    private final VeiculosRepository veiculoRepository;
    private final PayloadRepository payloadRepository;
    private final ObjectMapper objectMapper;

    @RabbitListener(queues = "dados-telemetria")
    public void receber(String messageText) {
        try {
            PayloadRastreamentoDTO dto = objectMapper.readValue(messageText, PayloadRastreamentoDTO.class);

            // 1ª Regra de Corte: O payload tem uma placa preenchida?
            if (dto.getPlate() == null || dto.getPlate().trim().isEmpty()) {
                log.warn("Payload recebido sem Placa. Desviando para a tabela payload_desconhecido...");
                salvarComoPayload(messageText);
                return;
            }

            // Resolução Dinâmica de Tenant pela Placa do Veículo
            VeiculosEntity veiculo = veiculoRepository.findByPlate(dto.getPlate()).orElse(null);

            // 2ª Regra de Corte: A placa existe no nosso banco?
            if (veiculo == null) {
                log.warn("Telemetria rejeitada: A placa {} não está cadastrada. Desviando payload...", dto.getPlate());
                salvarComoPayload(messageText);
                return;
            }

            RastreioEntity entity = new RastreioEntity();
            entity.setEmpresaId(veiculo.getEmpresaId()); // Carimba o ID da empresa correta
            entity.setDate(LocalDateTime.now());
            entity.setPlate(dto.getPlate());
            entity.setEvent(dto.getEvent());
            entity.setIdTracking(dto.getIdTracking());
            entity.setGpsValid(dto.getGpsValid());
            entity.setIgnition(dto.getIgnition());
            entity.setOdometer(dto.getOdometer());
            entity.setBatteryVoltage(dto.getBatteryVoltage());
            entity.setDriver(dto.getDriver());

            if (dto.getLatLong() != null) {
                entity.setLatitude(dto.getLatLong().getLatitude());
                entity.setLongitude(dto.getLatLong().getLongitude());
            }

            if (dto.getTelemetria() != null) {
                PayloadRastreamentoDTO.TelemetriaInternalDTO telDto = dto.getTelemetria();
                entity.setRpm(telDto.getRpm());
                entity.setTensao(telDto.getTensao());
                entity.setLoaded(telDto.getLoaded());
                entity.setHodometro(telDto.getHodometro());
                entity.setHorimetro(telDto.getHorimetro());
                entity.setStatusFreio(telDto.getStatusFreio());
                entity.setEventoFrenagem(telDto.getEventoFrenagem());
                entity.setEventoAceleracao(telDto.getEventoAceleracao());
                entity.setTemperaturaOleo(telDto.getTemperaturaOleo());
                entity.setNivelCombustivel(telDto.getNivelCombustivel());
                entity.setTotalCombustivel(telDto.getTotalCombustivel());
                entity.setPosicaoPedalFreio(telDto.getPosicaoPedalFreio());
                entity.setPosicaoPedalAcelerador(telDto.getPosicaoPedalAcelerador());
                entity.setExtra(telDto.getAny()); // Mapeia campos extras dinâmicos no JSONB
            }

            repository.save(entity);
            log.info("Telemetria estruturada da placa {} (Empresa: {}) salva no Supabase!", entity.getPlate(), entity.getEmpresaId());

        } catch (Exception e) {
            // 3ª Regra de Corte: Erro de conversão de JSON
            log.error("Falha de conversão ao processar telemetria. Desviando para a tabela payload_desconhecido. Motivo: {}", e.getMessage());
            salvarComoPayload(messageText);
        }
    }

    // Método auxiliar isolado para salvar os dados cruamente no formato JSONB
    private void salvarComoPayload(String rawJsonText) {
        try {
            JsonNode jsonGenerico = objectMapper.readTree(rawJsonText);

            PayloadEntity payloadEntity = new PayloadEntity();
            payloadEntity.setDadosBrutos(jsonGenerico);

            payloadRepository.save(payloadEntity);
            log.info("Payload genérico salvo com sucesso na tabela payload_desconhecido.");
        } catch (Exception ex) {
            log.error("Falha CRÍTICA: A mensagem recebida no RabbitMQ não é sequer um JSON válido. Descartando mensagem: {}", rawJsonText);
        }
    }
}