package msa.emaia.customer;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "partner-service-customer", url = "${partner.service.url:http://localhost:8084/v1}", fallback = CustomerServiceFallback.class)
public interface CustomerService {

    @GetMapping("/api/customers/{id}")
    Customer findById(@PathVariable("id") String id);
}
