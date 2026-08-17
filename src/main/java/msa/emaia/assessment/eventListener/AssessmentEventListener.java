package msa.emaia.assessment.eventListener;

import msa.emaia.assessment.AssessmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class AssessmentEventListener {

    @Autowired
    private AssessmentService assessmentService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleAssessmentCompleted(AssessmentCompletedEvent event) {
        assessmentService.setAIGenerateSummary(event.getAssessmentId());
    }
}
