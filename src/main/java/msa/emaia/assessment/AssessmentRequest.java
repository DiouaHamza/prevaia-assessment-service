package msa.emaia.assessment;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import msa.emaia.assessment.assessmentValues.AssessmentValues;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AssessmentRequest {

    @Valid
    private String formId;

    @Valid
    private String assessmentId;

    @Valid
    private String questionId;

    @Valid
    private Object questionValue;

    @Valid
    private String supplierComment;
    
    @Valid
    private Integer nextQuestion;

    @Valid
    private String projectId;

    @Valid
    private String supplierId;

    @Valid
    private Boolean isPreview;

}
