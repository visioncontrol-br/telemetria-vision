package visioncontrol.mensageria.telemetria.business.consumer;

import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Component
@Slf4j
public class RastreioConsumer {

    @RabbitListener(
            queues = "${app.rabbitmq.queue}",
            ackMode = "MANUAL",
            containerFactory = "rabbitBatchContainerFactory"
    )
    public void receberLote(List<Message> messages, Channel channel) throws IOException {
        long lastDeliveryTag = messages.get(messages.size() - 1).getMessageProperties().getDeliveryTag();

        try {
            log.info("==================================================");
            log.info("LOTE RECEBIDO DO RABBITMQ! Tamanho do lote: {}", messages.size());

            // Imprime apenas a primeira mensagem do lote para nao poluir muito o log
            String primeiroJson = new String(messages.get(0).getBody(), StandardCharsets.UTF_8);
            log.info("Conteudo da 1a Mensagem: {}", primeiroJson);
            log.info("==================================================");

            // Confirma para o RabbitMQ que o lote inteiro foi "processado" (Aba 'Unacked' vai zerar)
            channel.basicAck(lastDeliveryTag, true);

        } catch (Exception e) {
            log.error("Erro ao simular o processamento do lote.", e);
            channel.basicNack(lastDeliveryTag, true, false);
        }
    }
}