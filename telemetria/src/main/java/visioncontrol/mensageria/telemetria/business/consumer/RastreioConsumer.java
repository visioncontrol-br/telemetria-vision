package visioncontrol.mensageria.telemetria.business.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;
import visioncontrol.mensageria.telemetria.business.consumer.dto.RastreioPayloadDTO;

// 👇 Import atualizado para a Service nova
import visioncontrol.mensageria.telemetria.business.consumer.service.TelemetriaService;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class RastreioConsumer {

    // 👇 Injeção atualizada
    private final TelemetriaService telemetriaService;

    private final ObjectMapper objectMapper;
    private final RabbitTemplate rabbitTemplate;

    @Value("${app.rabbitmq.dlq-queue}")
    private String dlqQueue;

    @RabbitListener(
            queues = "${app.rabbitmq.queue}",
            ackMode = "MANUAL",
            containerFactory = "rabbitBatchContainerFactory"
    )
    public void receberLote(List<Message> messages, Channel channel) throws IOException {
        long lastDeliveryTag = messages.get(messages.size() - 1).getMessageProperties().getDeliveryTag();

        try {
            log.info("[CONSUMER] Processando lote de {} mensagens da Rastreamos...", messages.size());

            for (Message message : messages) {
                String jsonString = "";
                try {
                    // 1. Converte os bytes para String JSON
                    jsonString = new String(message.getBody(), StandardCharsets.UTF_8);

                    // 2. Parse do JSON para o DTO
                    RastreioPayloadDTO dto = objectMapper.readValue(jsonString, RastreioPayloadDTO.class);

                    //  3. Processamento apontando para a nova tabela de telemetria
                    telemetriaService.processarTelemetria(dto);

                } catch (Exception e) {
                    // Nós relançamos o erro para dar NACK no lote inteiro e tentar novamente mais tarde.
                    if (e instanceof DataAccessException || e.getCause() instanceof java.net.ConnectException) {
                        log.error("[INFRAESTRUTURA] Falha de banco de dados detectada. Abortando o lote para reonfileiramento.");
                        throw e;
                    }

                    // Capturamos o erro individualmente e desviamos apenas essa mensagem para a DLQ física na VPS
                    log.error("[POISON PILL] Mensagem inválida detectada no lote. Desviando para a DLQ. Erro: {}", e.getMessage());
                    rabbitTemplate.convertAndSend("", dlqQueue, jsonString);
                }
            }

            // Mensagens processadas foram salvas no Postgres. Mensagens com erro foram para a DLQ.
            channel.basicAck(lastDeliveryTag, true);
            log.info("[CONSUMER] Lote de telemetria finalizado e confirmado com sucesso!");

        } catch (Exception e) {
            log.error("[FALHA CRÍTICA] Lote abortado devido a erro de infraestrutura.", e);
            // Se caiu no bloco externo, significa que o banco caiu no meio do caminho.
            // O último parâmetro como 'true' faz o RabbitMQ reenfileirar (requeue) o lote inteiro na fila principal
            channel.basicNack(lastDeliveryTag, true, true);
        }
    }
}