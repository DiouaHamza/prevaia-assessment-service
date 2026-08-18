package msa.emaia.assessment.assessmentStatus;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "ref_assessment_status")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AssessmentStatus {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @NotBlank(message = "Code is required")
    @Size(max = 20)
    private String code;

    @NotBlank(message = "label is required")
    @Size(max = 255)
    private String label;

    @Size(max = 2000)
    private String color;

    private Integer step;

}
