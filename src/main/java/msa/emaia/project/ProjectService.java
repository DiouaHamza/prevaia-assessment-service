package msa.emaia.project;

import msa.emaia.project.dto.ProjectDto;
import msa.emaia.project.dto.SaveProjectDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import java.util.List;

@FeignClient(name = "partner-service-project", url = "${partner.service.url:http://localhost:8084/v1}", fallback = ProjectServiceFallback.class)
public interface ProjectService {

    @GetMapping("/api/projects/{id}")
    Project findProjectsById(@PathVariable("id") String id);
    
    @GetMapping("/api/projects/supplier/{supplierId}")
    List<ProjectDto> getBySupplierId(@PathVariable("supplierId") String supplierId);
    
    @PutMapping("/api/projects/{id}")
    Project updateProjects(@PathVariable("id") String id, @RequestBody SaveProjectDto projectToEdit);
}
