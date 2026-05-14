package visioncontrol.mensageria.telemetria.business.consumer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import visioncontrol.mensageria.telemetria.infrastructure.entity.TelemetriaEntity;
import visioncontrol.mensageria.telemetria.infrastructure.repository.TelemetriaRepository;

@Component
@Slf4j
public class TelemetriaConsumer {

    private final TelemetriaRepository repository;

    public TelemetriaConsumer(TelemetriaRepository repository) {
        this.repository = repository;
    }

    @RabbitListener(queues = "rastreamos.entrada")
    public void receive(String payload) {
        log.info("Mensagem recebida da fila rastreamos.entrada: {}", payload);
        TelemetriaEntity entity = new TelemetriaEntity();
        entity.setPayload(payload);
        repository.save(entity);
        log.info("Salvo no Supabase com id: {}", entity.getId());
    }
}