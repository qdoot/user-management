package it.intesigroup.usermanagement.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Componente responsabile della pubblicazione degli eventi utente su RabbitMQ.
 *
 * <p>La pubblicazione è fire-and-forget: in caso di errore viene loggato
 * ma non propagato al chiamante per non bloccare la creazione dell'utente.
 * In un sistema production-grade si valuterebbe l'uso di un outbox pattern
 * per garantire la consegna almeno una volta.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UserEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    @Value("${app.rabbitmq.exchange}")
    private String exchange;

    @Value("${app.rabbitmq.routing-key.user-created}")
    private String userCreatedRoutingKey;

    /**
     * Pubblica un evento {@link UserCreatedEvent} sull'exchange RabbitMQ.
     *
     * @param event l'evento da pubblicare, costruito dal service layer
     */
    public void publishUserCreated(UserCreatedEvent event) {
        event.setEventId(UUID.randomUUID());
        event.setEventType("USER_CREATED");
        event.setTimestamp(LocalDateTime.now());

        try {
            rabbitTemplate.convertAndSend(exchange, userCreatedRoutingKey, event);
            log.info("Evento USER_CREATED pubblicato per utente id={}, username={}",
                    event.getUserId(), event.getUsername());
        } catch (Exception ex) {
            log.error("Errore durante la pubblicazione dell'evento USER_CREATED " +
                    "per utente id={}: {}", event.getUserId(), ex.getMessage(), ex);
        }
    }
}
