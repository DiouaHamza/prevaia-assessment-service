package msa.emaia.supplier;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "partner-service-supplier", url = "${partner.service.url:http://localhost:8084/v1}", fallback = SupplierServiceFallback.class)
public interface SupplierService {

    @GetMapping("/api/suppliers/{id}")
    Supplier findSupplierById(@PathVariable("id") String id);
    
    @GetMapping("/api/suppliers")
    Page<Supplier> findAll(
        @RequestParam(value = "search", required = false) String search,
        @RequestParam(value = "country", required = false) String country,
        @RequestParam(value = "activitySector", required = false) String activitySector,
        @RequestParam(value = "certifications", required = false) String certifications,
        @RequestParam(value = "products", required = false) String products,
        @RequestParam(value = "sortBy", required = false) String sortBy,
        @RequestParam(value = "sortDirection", required = false) String sortDirection,
        @RequestParam(value = "page", defaultValue = "0") int page
    );
}
