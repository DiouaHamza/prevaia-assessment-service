package msa.emaia.customer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import msa.emaia.partnerProjection.SupplierProjection;
import msa.emaia.partnerProjection.SupplierProjectionRepository;
import org.springframework.stereotype.Component;

/**
 * Circuit-breaker fallback for the Partner Service customer Feign client.
 * The projection table is keyed by supplier, not customer, so this is a
 * best-effort lookup via the first supplier_projection row carrying the
 * requested customerId - degraded, but enough for the notification flows
 * that need a customer's name/email/isAutoValidAssessment.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CustomerServiceFallback implements CustomerService {

    private final SupplierProjectionRepository supplierProjectionRepository;

    @Override
    public Customer findById(String id) {
        log.warn("[CustomerServiceFallback] Partner Service unavailable, falling back to projection for customer {}", id);

        SupplierProjection projection = supplierProjectionRepository.findFirstByCustomerId(id).orElse(null);
        if (projection == null) {
            return null;
        }

        Customer customer = new Customer();
        customer.setId(projection.getCustomerId());
        customer.setFullName(projection.getCustomerFullName());
        customer.setEmail(projection.getCustomerEmail());
        customer.setIsAutoValidAssessment(projection.getIsAutoValidAssessment());
        return customer;
    }
}
