package msa.emaia.project;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import msa.emaia.partnerProjection.ProjectProjection;
import msa.emaia.partnerProjection.ProjectProjectionRepository;
import msa.emaia.project.dto.ProjectDto;
import msa.emaia.project.dto.SaveProjectDto;
import msa.emaia.supplier.Supplier;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;

/**
 * Circuit-breaker fallback for the Partner Service project Feign client.
 * Reads are served (best-effort) from the local read-only projection.
 * updateProjects is a write against Partner's own data - there is nothing
 * safe to fall back to, so it fails loudly instead of pretending the write
 * succeeded.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ProjectServiceFallback implements ProjectService {

    private final ProjectProjectionRepository projectProjectionRepository;

    @Override
    public Project findProjectsById(String id) {
        log.warn("[ProjectServiceFallback] Partner Service unavailable, falling back to projection for project {}", id);

        ProjectProjection projection = projectProjectionRepository.findById(id).orElse(null);
        if (projection == null) {
            return null;
        }

        Project project = new Project();
        project.setId(projection.getProjectId());
        project.setName(projection.getTitle());

        if (projection.getSupplierId() != null) {
            Supplier supplier = new Supplier();
            supplier.setId(projection.getSupplierId());
            project.setSupplier(supplier);
        }

        return project;
    }

    @Override
    public List<ProjectDto> getBySupplierId(String supplierId) {
        log.warn("[ProjectServiceFallback] Partner Service unavailable, falling back to projection for supplier {}'s projects", supplierId);

        return projectProjectionRepository.findBySupplierId(supplierId).stream()
                .map(p -> {
                    ProjectDto dto = new ProjectDto();
                    dto.setId(p.getProjectId());
                    dto.setName(p.getTitle());
                    dto.setQualificationValues(new HashMap<>());
                    return dto;
                })
                .toList();
    }

    @Override
    public Project updateProjects(String id, SaveProjectDto projectToEdit) {
        throw new IllegalStateException("Partner Service is unavailable - cannot update project " + id + ", no fallback exists for writes");
    }
}
