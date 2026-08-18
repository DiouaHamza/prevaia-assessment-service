package msa.emaia.partnerProjection;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SupplierProjectionRepository extends JpaRepository<SupplierProjection, String> {
    Optional<SupplierProjection> findFirstByCustomerId(String customerId);
}
