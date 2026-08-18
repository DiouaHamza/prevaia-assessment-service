package msa.emaia.assessment.dto;

import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import msa.emaia.assessment.assessmentAttachment.AssessmentAttachment;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AssessmentAttachmentResponseDto {

    @Id
    private String id;
    private String code;
    private String label;
    private String description;
    private String accept;
    private Boolean isMultiple;
    private Boolean required;

    List<AssessmentAttachmentDto> attachments;
}
