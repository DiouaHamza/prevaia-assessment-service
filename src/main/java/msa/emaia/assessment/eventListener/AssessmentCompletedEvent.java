package msa.emaia.assessment.eventListener;

import lombok.Data;
import lombok.Getter;

@Getter
@Data
public class AssessmentCompletedEvent {

    private final String assessmentId;

    public AssessmentCompletedEvent(String assessmentId) {
        this.assessmentId = assessmentId;
    }

}
