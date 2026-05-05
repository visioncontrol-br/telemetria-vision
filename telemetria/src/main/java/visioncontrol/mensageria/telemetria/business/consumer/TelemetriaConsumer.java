package visioncontrol.mensageria.telemetria.business.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
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

    // Essa anotação vai FORÇAR a criação da Fila, da Exchange e do Binding no RabbitMQ!
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "${rabbitmq.queue.name}", durable = "true"),
            exchange = @Exchange(value = "${rabbitmq.exchange.name}", type = ExchangeTypes.DIRECT),
            key = "${rabbitmq.routingkey.name}"
    ))
    public void receberDados(Map<String, Object> payload) {
        log.info("Dados de telemetria recebidos: {}", payload);

        TelemetriaEntity entity = new TelemetriaEntity();
        // Como o banco espera um texto (TEXT), transformamos o Map em String
        entity.setPayload(payload.toString());

        repository.save(entity);
        log.info("Registro salvo no banco de dados com sucesso!");
    }
}