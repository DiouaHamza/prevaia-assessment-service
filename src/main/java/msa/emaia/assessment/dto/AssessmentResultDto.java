package msa.emaia.assessment.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import msa.emaia.assessment.assessmentSectionScore.AssessmentSectionScore;
import msa.emaia.assessment.assessmentStatus.AssessmentStatus;
import msa.emaia.client.catalog.dto.RefRiskLevelMinDto;
import msa.emaia.client.catalog.dto.RefScoreRangeMinDto;

import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AssessmentResultDto {

    private Integer avgScore;
    private Integer maxScore;
    private Integer passedScore;
    private Integer requestedNumber;

    private AssessmentStatus status;
    private RefScoreRangeMinDto score;
    private RefRiskLevelMinDto riskLevel;

    private Date assessedOn;
    private Date requestedOn;
    private Date startedOn;
    private Date acceptedOn;
    private Date completedOn;

    private List<AssessmentSectionScore> sectionsScore;
    private List<ImprovementsDto> improvements;
    private List<ImprovementsDto> engagements;
    private List<IAssessmentQuestionResultDto> questions;


}

