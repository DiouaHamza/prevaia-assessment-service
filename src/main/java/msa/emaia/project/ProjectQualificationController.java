package msa.emaia.project;


import jakarta.validation.Valid;
import msa.emaia.assessment.AssessmentService;
import msa.emaia.assessment.assessmentValues.AssessmentValues;
import msa.emaia.assessment.assessmentValues.AssessmentValuesRepository;
import msa.emaia.project.dto.ProjectDto;
import msa.emaia.project.dto.SaveProjectDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;

/**
 * Composite Project+Assessment operations that legitimately span both domains.
 * Lives in root (not partner) since Partner must not import Assessment;
 * root is allowed to call into partner's ProjectService.
 */
@RestController
@RequestMapping("/api/projects")
public class ProjectQualificationController {

    @Autowired
    ProjectService projectService;

    @Autowired
    AssessmentService assessmentService;

    @Autowired
    AssessmentValuesRepository assessmentValuesRepository;

    @GetMapping("supplier/{supplierId}")
    public List<ProjectDto> getBySupplierId(@PathVariable("supplierId") String supplierId) {
        List<ProjectDto> projects = this.projectService.getBySupplierId(supplierId);

        for (ProjectDto dto : projects) {
            List<AssessmentValues> values = assessmentValuesRepository.findByProjectId(dto.getId());

            HashMap<String, Object> listValues = new HashMap<>();
            values.forEach(row -> listValues.put(row.getQuestionId(), row.getResponse()));

            dto.setQualificationValues(listValues);
        }

        return projects;
    }

    @Transactional
    @PutMapping("/{id}")
    public Project updateProjects(@PathVariable("id") String id, @Valid @RequestBody SaveProjectDto projectToEdit) {
        Project project = this.projectService.updateProjects(id, projectToEdit);

        if (project != null) {
            assessmentService.saveProjectAssessment(project, projectToEdit.getQuestionnaire());
        }

        return project;
    }

}
