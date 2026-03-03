package it.intesigroup.usermanagement.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configurazione dell'infrastruttura RabbitMQ.
 *
 * <p>Dichiara l'exchange, la queue e il binding necessari
 * per la pubblicazione degli eventi utente. Spring AMQP crea
 * automaticamente queste risorse su RabbitMQ al primo avvio
 * se non esistono già — comportamento idempotente.
 *
 * <p>Exchange di tipo Topic: permette routing flessibile tramite
 * pattern sulla routing key, facilitando l'aggiunta di nuovi tipi
 * di evento senza modificare l'infrastruttura.
 */
@Configuration
public class RabbitMQConfig {

    @Value("${app.rabbitmq.exchange}")
    private String exchangeName;

    @Value("${app.rabbitmq.routing-key.user-created}")
    private String userCreatedRoutingKey;

    private static final String USER_CREATED_QUEUE = "user.created.queue";

    /**
     * Exchange di tipo Topic — durable: sopravvive al riavvio di RabbitMQ.
     */
    @Bean
    public TopicExchange userEventsExchange() {
        return new TopicExchange(exchangeName, true, false);
    }

    /**
     * Queue per gli eventi USER_CREATED — durable.
     */
    @Bean
    public Queue userCreatedQueue() {
        return QueueBuilder.durable(USER_CREATED_QUEUE).build();
    }

    /**
     * Binding tra l'exchange e la queue tramite routing key esatta.
     */
    @Bean
    public Binding userCreatedBinding() {
        return BindingBuilder
                .bind(userCreatedQueue())
                .to(userEventsExchange())
                .with(userCreatedRoutingKey);
    }

    /**
     * Converter JSON: gli oggetti Java vengono serializzati in JSON
     * prima dell'invio — leggibile da consumer in qualsiasi linguaggio.
     */
    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    /**
     * RabbitTemplate configurato con il converter JSON.
     */
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter());
        return template;
    }
}
