package visioncontrol.mensageria.telemetria.infrastructure.config;

import jakarta.annotation.PostConstruct;
import org.springframework.amqp.rabbit.connection.Connection;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.rabbitmq.client.Channel;

@Configuration
public class RabbitMQConfig {

    @Value("${rabbitmq.queue.name}")
    private String queueName;

    @Value("${rabbitmq.exchange.name}")
    private String exchangeName;

    @Value("${rabbitmq.routingkey.name}")
    private String routingKey;

    private final ConnectionFactory connectionFactory;

    public RabbitMQConfig(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    @PostConstruct
    public void forcarCriacaoNativa() {
        // Abrimos uma conexão TPC física e direta com a VPS
        try (Connection conn = connectionFactory.createConnection();
             Channel channel = conn.createChannel(false)) {

            // 1. Declara a Exchange (durable = true)
            channel.exchangeDeclare(exchangeName, "direct", true);

            // 2. Declara a Fila (durable = true, exclusive = false, autoDelete = false)
            channel.queueDeclare(queueName, true, false, false, null);

            // 3. Faz o Bind (liga a fila na exchange usando a routing key)
            channel.queueBind(queueName, exchangeName, routingKey);

            System.out.println(">>>>>>>>>> RABBITMQ SUCESSO: Exchange e Fila criadas DIRETAMENTE na VPS! <<<<<<<<<<");

        } catch (Exception e) {
            System.err.println(">>>>>>>>>> RABBITMQ ERRO CRÍTICO: Falha ao tentar criar recursos na VPS. Verifique as permissões do usuário 'admin'! <<<<<<<<<<");
            e.printStackTrace();
        }
    }

    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}