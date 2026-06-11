package visioncontrol.mensageria.telemetria.business.consumer;

import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class RastreioConsumer {

    private final JdbcTemplate jdbcTemplate;

    @RabbitListener(
            queues = "${app.rabbitmq.queue}",
            ackMode = "MANUAL",
            containerFactory = "rabbitBatchContainerFactory"
    )
    public void receberLote(List<Message> messages, Channel channel) throws IOException {
        long lastDeliveryTag = messages.get(messages.size() - 1).getMessageProperties().getDeliveryTag();

        try {
            // Dispara o lote de Strings brutas para a função do Postgres
            jdbcTemplate.batchUpdate(
                    "SELECT public.rotear_payload_veiculo(?::jsonb)",
                    messages,
                    messages.size(),
                    (ps, message) -> {
                        String jsonBruto = new String(message.getBody(), StandardCharsets.UTF_8);
                        ps.setString(1, jsonBruto);
                    }
            );

            // Confirma o lote inteiro no RabbitMQ de uma vez só
            channel.basicAck(lastDeliveryTag, true);
            log.info("[SUCESSO] Lote de {} mensagens processado pelo PostgreSQL.", messages.size());

        } catch (Exception e) {
            log.error("Erro no lote de mensagens. Enviando bloco para a DLQ.", e);
            // Em caso de qualquer erro de banco, o lote inteiro vai para a DLQ para auditoria
            channel.basicNack(lastDeliveryTag, true, false);
        }
    }
}