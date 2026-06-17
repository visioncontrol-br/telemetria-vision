package visioncontrol.mensageria.telemetria.business.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import visioncontrol.mensageria.telemetria.business.consumer.dto.RastreioPayloadDTO;
import visioncontrol.mensageria.telemetria.business.consumer.service.RastreioService;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class RastreioConsumer {

    private final RastreioService rastreioService;
    private final ObjectMapper objectMapper;

    @RabbitListener(
            queues = "${app.rabbitmq.queue}",
            ackMode = "MANUAL",
            containerFactory = "rabbitBatchContainerFactory"
    )
    public void receberLote(List<Message> messages, Channel channel) throws IOException {
        long lastDeliveryTag = messages.get(messages.size() - 1).getMessageProperties().getDeliveryTag();

        try {
            log.info("Processando lote de {} mensagens da Rastreamos...", messages.size());

            // Varre o lote de mensagens que o RabbitMQ entregou
            for (Message message : messages) {
                // 1. Pega o JSON cru em bytes e converte para texto
                String jsonString = new String(message.getBody(), StandardCharsets.UTF_8);

                // 2. Converte o texto JSON para o seu DTO
                RastreioPayloadDTO dto = objectMapper.readValue(jsonString, RastreioPayloadDTO.class);

                // 3. Manda para o Service achar o veículo e salvar no PostgreSQL
                rastreioService.processarPosicao(dto);
            }

            // 4. Se o loop terminar sem erros, confirma para o RabbitMQ que TUDO foi salvo!
            channel.basicAck(lastDeliveryTag, true);
            log.info("Lote salvo e confirmado com sucesso no PostgreSQL!");

        } catch (Exception e) {
            log.error("Erro fatal ao processar o lote de telemetria.", e);
            // Se der erro de banco de dados no meio do caminho, nenhuma mensagem é perdida.
            // O NACK devolve elas para a fila para tentar de novo.
            channel.basicNack(lastDeliveryTag, true, false);
        }
    }
}