package msa.emaia.supplier;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import msa.emaia.customer.Customer;
import msa.emaia.partnerProjection.SupplierProjection;
import msa.emaia.partnerProjection.SupplierProjectionRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

/**
 * Circuit-breaker fallback for the Partner Service supplier Feign client.
 * Reconstructs a best-effort Supplier from the local read-only projection
 * (kept up to date by PartnerProjectionEventConsumer) when Partner Service
 * is unavailable. Not a full replica - only the fields the projection
 * captures are populated, everything else is left null.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SupplierServiceFallback implements SupplierService {

    private final SupplierProjectionRepository supplierProjectionRepository;

    @Override
    public Supplier findSupplierById(String id) {
        log.warn("[SupplierServiceFallback] Partner Service unavailable, falling back to projection for supplier {}", id);

        SupplierProjection projection = supplierProjectionRepository.findById(id).orElse(null);
        if (projection == null) {
            return null;
        }

        return toSupplier(projection);
    }

    @Override
    public Page<Supplier> findAll(
            String search, String country, String activitySector, String certifications,
            String products, String sortBy, String sortDirection, int page
    ) {
        // The projection carries none of these filter fields (country/activitySector/
        // certifications/products) - faking a filtered result would be misleading, so
        // this returns empty rather than a wrong/partial list.
        log.warn("[SupplierServiceFallback] Partner Service unavailable, findAll has no fallback data - returning empty page");
        return Page.empty(PageRequest.of(page, 10));
    }

    private Supplier toSupplier(SupplierProjection projection) {
        Supplier supplier = new Supplier();
        supplier.setId(projection.getSupplierId());
        supplier.setName(projection.getName());
        supplier.setContactEmail(projection.getContactEmail());

        if (projection.getCustomerId() != null) {
            Customer customer = new Customer();
            customer.setId(projection.getCustomerId());
            customer.setFullName(projection.getCustomerFullName());
            customer.setEmail(projection.getCustomerEmail());
            customer.setIsAutoValidAssessment(projection.getIsAutoValidAssessment());
            supplier.setCustomer(customer);
        }

        return supplier;
    }
}
