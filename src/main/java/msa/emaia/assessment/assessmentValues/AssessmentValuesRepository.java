package msa.emaia.assessment.assessmentValues;

import msa.emaia.assessment.AssessmentQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AssessmentValuesRepository extends JpaRepository<AssessmentValues, String>  {
    Optional<AssessmentValues> findByAssessmentIdAndQuestionId(String assessmentId, String questionId);

    @Query(value = """
        select av.* from assessment a, assessment_values av
        where a.id = av.assessment_id
            and a.project_id = :projectId
    """, nativeQuery = true)
    List<AssessmentValues> findByProjectId(String projectId);

    void deleteByAssessmentId(String assessmentId);

    @Query(value = """
        select coalesce(sum(coalesce(score, 0)), 0) from ref_question_options rqo\s
        where question_id  = :questionId
            and code in :codes
    """, nativeQuery = true)
    int findOptionScoreByCodes(String questionId, List<String> codes);

}
