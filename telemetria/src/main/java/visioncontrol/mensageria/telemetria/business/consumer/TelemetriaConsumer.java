package visioncontrol.mensageria.telemetria.business.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import visioncontrol.mensageria.telemetria.infrastructure.entity.TelemetriaEntity;
import visioncontrol.mensageria.telemetria.infrastructure.repository.TelemetriaRepository;

@Component
@Slf4j
@RequiredArgsConstructor
public class TelemetriaConsumer {

    private final TelemetriaRepository repository;

    @RabbitListener(queues = "dados-telemetria")
    public void receber(String payload) {
        log.info("Mensagem recebida: {}", payload);
        TelemetriaEntity entity = new TelemetriaEntity();
        entity.setPayload(payload);
        //repository.save(entity);
    }
}