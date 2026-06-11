package visioncontrol.mensageria.telemetria.infrastructure.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Value("${app.rabbitmq.queue}")
    private String queueName;

    @Value("${app.rabbitmq.exchange}")
    private String exchangeName;

    @Value("${app.rabbitmq.routing-key}")
    private String routingKey;

    // =========================================================
    // 1. INFRAESTRUTURA DO RABBITMQ (CRIAÇÃO AUTOMÁTICA)
    // =========================================================

    @Bean
    public Queue filaTelemetria() {
        // O "true" indica que a fila é durável (sobrevive a reinicializações da VPS)
        return new Queue(queueName, true);
    }

    @Bean
    public DirectExchange exchangeTelemetria() {
        return new DirectExchange(exchangeName);
    }

    @Bean
    public Binding bindingTelemetria(Queue filaTelemetria, DirectExchange exchangeTelemetria) {
        // Liga a Exchange à Fila usando a chave de roteamento
        return BindingBuilder.bind(filaTelemetria).to(exchangeTelemetria).with(routingKey);
    }

    // =========================================================
    // 2. CONFIGURAÇÃO DE CONSUMO EM LOTE (BATCHING)
    // =========================================================

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitBatchContainerFactory(ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);

        // Ativa o modo de escuta em lotes (necessário para receber List<Message> no Consumer)
        factory.setBatchListener(true);

        // Ativa a quebra em lotes no nível do consumidor
        factory.setConsumerBatchEnabled(true);

        // Define o tamanho do lote. Ele vai pegar 100 mensagens de uma vez ou o que tiver na fila.
        // Isso é fundamental para passar no Teste de Pico (Spike Testing) dos 1000 veículos.
        factory.setBatchSize(100);

        return factory;
    }
}