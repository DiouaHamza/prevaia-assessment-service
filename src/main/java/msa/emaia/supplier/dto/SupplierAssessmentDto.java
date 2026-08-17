package msa.emaia.supplier.dto;

import lombok.Data;
import msa.emaia.assessment.dto.AssessmentResultDto;
import msa.emaia.supplier.Supplier;

import java.util.Date;

@Data
public class SupplierAssessmentDto {

    private Supplier supplier;

    private AssessmentResultDto assessmentResult;

//    private Integer avgScore;
//    private String riskLevel;
//    private String status;
//    private Date assessedOn;
//    private Date requestedOn;


}
