package msa.emaia.partnerProjection;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import msa.emaia.outbox.RabbitMQConfig;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.Map;

/**
 * Keeps the local supplier_projection / project_projection tables in sync
 * with Partner's domain events, for display/reporting on the Assessment
 * side without a synchronous call. Not lazy for the same reason as
 * SupplierDeletedEventListener: with spring.main.lazy-initialization=true,
 * a lazy @RabbitListener bean never gets instantiated, so its listener
 * method never gets registered.
 *
 * Idempotency/ordering: rather than a separate dedup ledger, each envelope
 * carries "occurredAt" (the outbox event's createdAt). An update is only
 * applied if it is newer than the projection row's lastEventAt - a replayed
 * or out-of-order event is a silent no-op. Any exception here (malformed
 * envelope, missing field) is left to propagate: with
 * spring.rabbitmq.listener.simple.default-requeue-rejected=false, that is
 * what sends the message to the dead-letter queue instead of looping.
 */
@Slf4j
@Component
@Lazy(false)
@RequiredArgsConstructor
public class PartnerProjectionEventConsumer {

    private final SupplierProjectionRepository supplierProjectionRepository;
    private final ProjectProjectionRepository projectProjectionRepository;

    @RabbitListener(queues = RabbitMQConfig.PARTNER_SUPPLIER_PROJECTION_QUEUE)
    @Transactional
    @SuppressWarnings("unchecked")
    public void onSupplierEvent(Map<String, Object> envelope) {
        Date occurredAt = parseOccurredAt(envelope.get("occurredAt"));
        Map<String, Object> payload = (Map<String, Object>) envelope.get("payload");
        String supplierId = (String) payload.get("supplierId");

        SupplierProjection projection = supplierProjectionRepository.findById(supplierId).orElse(null);
        if (projection != null && !occurredAt.after(projection.getLastEventAt())) {
            log.info("[PartnerProjectionEventConsumer] Ignoring stale/replayed event {} for supplier {}", envelope.get("eventId"), supplierId);
            return;
        }

        if (projection == null) {
            projection = new SupplierProjection();
            projection.setSupplierId(supplierId);
        }

        if (payload.containsKey("name")) {
            projection.setName((String) payload.get("name"));
        }
        if (payload.containsKey("status")) {
            projection.setStatus((String) payload.get("status"));
        }
        if (payload.containsKey("contactEmail")) {
            projection.setContactEmail((String) payload.get("contactEmail"));
        }
        if (payload.containsKey("customerId")) {
            projection.setCustomerId((String) payload.get("customerId"));
        }
        if (payload.containsKey("customerFullName")) {
            projection.setCustomerFullName((String) payload.get("customerFullName"));
        }
        if (payload.containsKey("customerEmail")) {
            projection.setCustomerEmail((String) payload.get("customerEmail"));
        }
        if (payload.containsKey("isAutoValidAssessment")) {
            projection.setIsAutoValidAssessment((Boolean) payload.get("isAutoValidAssessment"));
        }
        projection.setLastEventAt(occurredAt);

        supplierProjectionRepository.save(projection);
        log.info("[PartnerProjectionEventConsumer] Applied {} to supplier_projection {}", envelope.get("eventType"), supplierId);
    }

    @RabbitListener(queues = RabbitMQConfig.PARTNER_PROJECT_PROJECTION_QUEUE)
    @Transactional
    @SuppressWarnings("unchecked")
    public void onProjectEvent(Map<String, Object> envelope) {
        Date occurredAt = parseOccurredAt(envelope.get("occurredAt"));
        Map<String, Object> payload = (Map<String, Object>) envelope.get("payload");
        String projectId = (String) payload.get("projectId");

        ProjectProjection projection = projectProjectionRepository.findById(projectId).orElse(null);
        if (projection != null && !occurredAt.after(projection.getLastEventAt())) {
            log.info("[PartnerProjectionEventConsumer] Ignoring stale/replayed event {} for project {}", envelope.get("eventId"), projectId);
            return;
        }

        if (projection == null) {
            projection = new ProjectProjection();
            projection.setProjectId(projectId);
        }

        if (payload.containsKey("supplierId")) {
            projection.setSupplierId((String) payload.get("supplierId"));
        }
        if (payload.containsKey("title")) {
            projection.setTitle((String) payload.get("title"));
        }
        if (payload.containsKey("status")) {
            projection.setStatus((String) payload.get("status"));
        }
        projection.setLastEventAt(occurredAt);

        projectProjectionRepository.save(projection);
        log.info("[PartnerProjectionEventConsumer] Applied {} to project_projection {}", envelope.get("eventType"), projectId);
    }

    private Date parseOccurredAt(Object rawOccurredAt) {
        if (rawOccurredAt instanceof Number number) {
            return new Date(number.longValue());
        }
        if (rawOccurredAt instanceof String text) {
            return Date.from(java.time.Instant.parse(text));
        }
        throw new IllegalArgumentException("Unrecognized occurredAt format: " + rawOccurredAt);
    }
}
