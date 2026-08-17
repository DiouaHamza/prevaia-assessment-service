package msa.emaia.assessment.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ImprovementsDto {
    private String title;
    private String description;
    private List<AssessmentRecommendationDto> improvementActions;
    private List<AssessmentRecommendationDto> stregths;

    /*public ImprovementsDto setImprovementActionsFromStringList(List<AssessmentRecommendationDto> lists){
        this.improvementActions = new ArrayList<>();
        for(AssessmentRecommendationDto item : lists){
            this.improvementActions.add(new ImprovementsActionsDto(null, item.getRecommendation()));
        }

        return this;
    }*/
}

@Data
@NoArgsConstructor
@AllArgsConstructor
class ImprovementsActionsDto {
    private String impact;
    private String recommendation;
    private String sectionCode;
}
