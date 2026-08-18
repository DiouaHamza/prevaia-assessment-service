package msa.emaia.outbox;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxPublisher {

    private final OutboxEventRepository outboxEventRepository;
    private final RabbitTemplate rabbitTemplate;

    @Scheduled(fixedDelay = 5000)
    public void publishPendingEvents() {
        List<OutboxEvent> pending = outboxEventRepository.findTop50ByStatusOrderByCreatedAtAsc(OutboxStatus.PENDING);

        for (OutboxEvent event : pending) {
            try {
                Map<String, Object> envelope = Map.of(
                        "eventId", event.getId(),
                        "eventType", event.getEventType(),
                        "aggregateType", event.getAggregateType(),
                        "aggregateId", event.getAggregateId(),
                        "occurredAt", event.getCreatedAt(),
                        "payload", event.getPayload()
                );

                rabbitTemplate.convertAndSend(RabbitMQConfig.EVENTS_EXCHANGE, routingKeyFor(event.getEventType()), envelope);

                event.setStatus(OutboxStatus.PUBLISHED);
                event.setPublishedAt(new Date());
                outboxEventRepository.save(event);
            } catch (Exception e) {
                log.error("[OutboxPublisher] Failed to publish outbox event {} ({}): {}", event.getId(), event.getEventType(), e.getMessage(), e);
            }
        }
    }

    private String routingKeyFor(String eventType) {
        return switch (eventType) {
            case "SUPPLIER_DELETE_REQUESTED" -> RabbitMQConfig.SUPPLIER_DELETED_ROUTING_KEY;
            case "SUPPLIER_CREATED" -> RabbitMQConfig.SUPPLIER_CREATED_ROUTING_KEY;
            case "SUPPLIER_UPDATED" -> RabbitMQConfig.SUPPLIER_UPDATED_ROUTING_KEY;
            case "SUPPLIER_STATUS_CHANGED" -> RabbitMQConfig.SUPPLIER_STATUS_CHANGED_ROUTING_KEY;
            case "PROJECT_CREATED" -> RabbitMQConfig.PROJECT_CREATED_ROUTING_KEY;
            case "PROJECT_CLOSED" -> RabbitMQConfig.PROJECT_CLOSED_ROUTING_KEY;
            case "ADMIN_NOTIFICATION_REQUESTED" -> RabbitMQConfig.ADMIN_NOTIFICATION_REQUESTED_ROUTING_KEY;
            case "SUPPLIER_EMAIL_REQUESTED" -> RabbitMQConfig.SUPPLIER_EMAIL_REQUESTED_ROUTING_KEY;
            default -> throw new IllegalArgumentException("No routing key mapped for event type: " + eventType);
        };
    }
}
