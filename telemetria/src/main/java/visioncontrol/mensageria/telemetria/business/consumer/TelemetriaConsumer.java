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

    // Aponta dinamicamente para o properties/yml
    @RabbitListener(queues = "${rabbitmq.queue.name}")
    public void receberDados(String payload) {
        // O RabbitMQ + Jackson já entregam o payload convertido se configurado
        log.info("Dados de telemetria recebidos: {}", payload);

        TelemetriaEntity entity = new TelemetriaEntity();
        entity.setPayload(payload);

        repository.save(entity);
        log.info("Registro salvo no banco de dados com sucesso!");
    }
}