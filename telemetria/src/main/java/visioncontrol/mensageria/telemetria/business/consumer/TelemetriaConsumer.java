package visioncontrol.mensageria.telemetria.business.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import visioncontrol.mensageria.telemetria.business.dto.PayloadRastreamentoDTO;
import visioncontrol.mensageria.telemetria.infrastructure.entity.TelemetriaEntity;
import visioncontrol.mensageria.telemetria.infrastructure.repository.TelemetriaRepository;

@Component
@Slf4j
@RequiredArgsConstructor
public class TelemetriaConsumer {

    private final TelemetriaRepository repository;

    // Injetamos o ObjectMapper do Spring para converter JSON em Objeto
    private final ObjectMapper objectMapper;

    @RabbitListener(queues = "dados-telemetria")
    public void receber(String messageText) {
        try {
            // 1. Converte a string bruta para o nosso DTO estruturado
            PayloadRastreamentoDTO dto = objectMapper.readValue(messageText, PayloadRastreamentoDTO.class);

            // 2. Transfere os dados do DTO para a Entidade JPA que vai pro Supabase
            TelemetriaEntity entity = new TelemetriaEntity();

            // Dados da Raiz do JSON
            entity.setPlate(dto.getPlate());
            entity.setEvent(dto.getEvent());
            entity.setIdTracking(dto.getIdTracking());
            entity.setGpsValid(dto.getGpsValid());
            entity.setIgnition(dto.getIgnition());
            entity.setOdometer(dto.getOdometer());
            entity.setBatteryVoltage(dto.getBatteryVoltage());

            if (dto.getLatLong() != null) {
                entity.setLatitude(dto.getLatLong().getLatitude());
                entity.setLongitude(dto.getLatLong().getLongitude());
            }

            // Dados do Bloco Aninhado "telemetria"
            // Dados do Bloco Aninhado "telemetria"
            if (dto.getTelemetria() != null) {
                // CORREÇÃO: Acessamos a classe estática usando o nome do DTO principal, sem o "get" no tipo
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

                // Se você implementou o método para capturar os campos extras
                entity.setExtra(telDto.getAny());
            }

            // Se a mensagem vier com o ID do webhook associado (vindo do outro módulo)
            // entity.setWebhookId(idDoWebhook);

            // 3. Salva no Supabase de forma estruturada!
            repository.save(entity);
            log.info("Telemetria estruturada da placa {} salva no Supabase!", entity.getPlate());

        } catch (Exception e) {
            log.error("Falha ao processar telemetria: ", e);
        }
    }
}