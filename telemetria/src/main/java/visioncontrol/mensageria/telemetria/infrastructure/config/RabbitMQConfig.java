package visioncontrol.mensageria.telemetria.infrastructure.config;

import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Value("${app.rabbitmq.queue}")
    private String queueName;

    // 1. Fila Principal (com regras apontando para a DLQ em caso de falha)
    @Bean
    public Queue principalQueue() {
        return QueueBuilder.durable(queueName)
                .withArgument("x-dead-letter-exchange", "telemetria.dlx")
                .withArgument("x-dead-letter-routing-key", "telemetria.dlq.key")
                .build();
    }

    // 2. Fila de Mensagens Mortas (DLQ)
    @Bean
    public Queue deadLetterQueue() {
        return QueueBuilder.durable(queueName + "-dlq").build();
    }

    // 3. Exchange da DLQ
    @Bean
    public DirectExchange deadLetterExchange() {
        return new DirectExchange("telemetria.dlx");
    }

    // 4. Conectando a DLQ ao Exchange
    @Bean
    public Binding deadLetterBinding(Queue deadLetterQueue, DirectExchange deadLetterExchange) {
        return BindingBuilder.bind(deadLetterQueue).to(deadLetterExchange).with("telemetria.dlq.key");
    }
}