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
 * Consumes Partner's SUPPLIER_DELETE_REQUESTED (routing key supplier.deleted,
 * queue RabbitMQConfig.SUPPLIER_DELETED_QUEUE) to soft-delete the local
 * supplier_projection row. Without this, deleted suppliers never disappear
 * from the projection and native queries filtering on "supplier still
 * active" (previously suppliers.deleted_at IS NULL) would never see them as
 * deleted. Not lazy, same reasoning as PartnerProjectionEventConsumer: with
 * spring.main.lazy-initialization=true, a lazy @RabbitListener bean never
 * gets its listener method registered.
 *
 * Deliberately a soft marker (deletedAt set, row kept) rather than a hard
 * delete: Partner's own SupplierDeletedEventListener does a cascading hard
 * delete, but that pattern predates Partner having its own separate
 * database and doesn't apply here - Assessment still needs the historical
 * link for past assessments tied to a since-deleted supplier.
 */
@Slf4j
@Component
@Lazy(false)
@RequiredArgsConstructor
public class SupplierDeletedProjectionListener {

    private final SupplierProjectionRepository supplierProjectionRepository;

    @RabbitListener(queues = RabbitMQConfig.SUPPLIER_DELETED_QUEUE)
    @Transactional
    @SuppressWarnings("unchecked")
    public void onSupplierDeleted(Map<String, Object> envelope) {
        Map<String, Object> payload = (Map<String, Object>) envelope.get("payload");
        String supplierId = (String) payload.get("supplierId");

        SupplierProjection projection = supplierProjectionRepository.findById(supplierId).orElse(null);
        if (projection == null) {
            log.info("[SupplierDeletedProjectionListener] No local projection for supplier {}, nothing to mark", supplierId);
            return;
        }
        if (projection.getDeletedAt() != null) {
            log.info("[SupplierDeletedProjectionListener] Supplier {} already marked deleted, ignoring replayed event", supplierId);
            return;
        }

        projection.setDeletedAt(new Date());
        supplierProjectionRepository.save(projection);

        log.info("[SupplierDeletedProjectionListener] Marked supplier_projection {} as deleted", supplierId);
    }
}
