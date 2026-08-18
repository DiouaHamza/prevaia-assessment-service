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
public class AssessmentAttachmentDto {

    @Id
    private String id;
    private String assessmentId;
    private String attachmentId;
    private String filename;
    private String filepath;
    private String mimetype;
    private Long filesize;
}
