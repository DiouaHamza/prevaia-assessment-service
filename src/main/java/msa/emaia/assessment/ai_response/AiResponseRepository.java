package msa.emaia.assessment.ai_response;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AiResponseRepository extends JpaRepository<AiResponse, String> {
    AiResponse findByAssessmentIdAndCodeQuestion(String assessmentId, String codeQuestion);

}