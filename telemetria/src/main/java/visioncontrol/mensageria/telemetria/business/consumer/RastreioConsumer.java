package visioncontrol.mensageria.telemetria.business.consumer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import visioncontrol.mensageria.telemetria.business.consumer.processor.TelemetryProcessor;

import java.io.IOException;

@Component
@Slf4j
@RequiredArgsConstructor
public class RastreioConsumer {

    private final ObjectMapper objectMapper;
    private final TelemetryProcessor processor; // Classe que vai conter a lógica de negócio

    // Lendo o nome da fila do application.yml dinamicamente
    @RabbitListener(queues = "${app.rabbitmq.queue}", ackMode = "MANUAL")
    public void receber(Message message, Channel channel) throws IOException {
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        try {
            String payload = new String(message.getBody());
            JsonNode rootNode = objectMapper.readTree(payload);

            // Roteamento O(1) - Rápido e sem Reflection
            if (rootNode.has("telemetria")) {
                log.info("[ROTEAMENTO] Chave 'telemetria' identificada. Direcionando para tabela: telemetria_avancada.");
                processor.processTelemetria(rootNode, payload);

            } else if (rootNode.has("latLong")) {
                log.info("[ROTEAMENTO] Apenas chave 'latLong' identificada. Direcionando para tabela: posicoes.");
                processor.processPosicao(rootNode, payload);

            } else {
                log.warn("[ROTEAMENTO] Payload com estrutura desconhecida. Desviando para a tabela genérica de falhas.");
                processor.processUnknown(payload);
            }

            // Confirma o sucesso para o RabbitMQ deletar a fila
            channel.basicAck(deliveryTag, false);

        } catch (Exception e) {
            log.error("Erro ao processar mensagem. Enviando para DLQ.", e);
            // Rejeita a mensagem (false para não voltar pra mesma fila, vai para a DLQ)
            channel.basicNack(deliveryTag, false, false);
        }
    }
}