package msa.emaia.partnerProjection;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProjectProjectionRepository extends JpaRepository<ProjectProjection, String> {
    List<ProjectProjection> findBySupplierId(String supplierId);
}
