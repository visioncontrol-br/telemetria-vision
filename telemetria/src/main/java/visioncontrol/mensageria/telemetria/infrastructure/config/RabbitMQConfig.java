    package visioncontrol.mensageria.telemetria.infrastructure.config;

    import org.springframework.amqp.core.Queue;
    import org.springframework.amqp.core.QueueBuilder;
    import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
    import org.springframework.amqp.support.converter.MessageConverter;
    import org.springframework.beans.factory.annotation.Value;
    import org.springframework.context.annotation.Bean;
    import org.springframework.context.annotation.Configuration;

    @Configuration
    public class RabbitMQConfig {

        @Value("${app.rabbitmq.queue}")
        private String queueName;

        @Bean
        public Queue filaPrincipal() {
            return QueueBuilder.durable(queueName)
                    .withArgument("x-dead-letter-exchange", "")
                    .withArgument("x-dead-letter-routing-key", queueName + ".dlq")
                    .build();
        }

        @Bean
        public Queue filaDLQ() {
            return QueueBuilder.durable(queueName + ".dlq").build();
        }
    }