package msa.emaia.assessment;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import msa.emaia.client.catalog.dto.RefFormMinDto;
import msa.emaia.client.catalog.dto.RefQuestionMinDto;
import msa.emaia.client.catalog.dto.RefSectionMinDto;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AssessmentQuestion {
    private RefQuestionMinDto question;
    private RefSectionMinDto section;
    private RefFormMinDto form;
    private String assessmentId;
    private Long total;
    private String status;
    private String supplierComment;
    private Object value;

}
