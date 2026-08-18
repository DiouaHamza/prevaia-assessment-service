package msa.emaia.assessment.assessmentSectionScore;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AssessmentSectionScoreRepository extends JpaRepository<AssessmentSectionScore, String> {
    Optional<AssessmentSectionScore> findByAssessmentIdAndSectionCode(String assessmentId, String sectionCode);
}
