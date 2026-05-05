package visioncontrol.mensageria.telemetria.business.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import visioncontrol.mensageria.telemetria.infrastructure.entity.TelemetriaEntity;
import visioncontrol.mensageria.telemetria.infrastructure.repository.TelemetriaRepository;

import java.util.Map;

@Component
@Slf4j
@RequiredArgsConstructor
public class TelemetriaConsumer {

    private final TelemetriaRepository repository;

    // Escuta apenas a fila, assumindo que o RabbitMQConfig já a criou
    @RabbitListener(queues = "${rabbitmq.queue.name}")
    public void receberDados(Map<String, Object> payload) {
        log.info("Dados de telemetria recebidos: {}", payload);

        TelemetriaEntity entity = new TelemetriaEntity();
        entity.setPayload(payload.toString());

        repository.save(entity);
        log.info("Registro salvo no banco de dados com sucesso!");
    }
}