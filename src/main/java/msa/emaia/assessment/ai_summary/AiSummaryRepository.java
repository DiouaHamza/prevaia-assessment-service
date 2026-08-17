package msa.emaia.assessment.ai_summary;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AiSummaryRepository extends JpaRepository<AiSummary, String> {
//    AiSummary findByAssessmentId(String assessmentId);
      @Query(value = """
            SELECT c.* FROM ai_summary c
            LEFT JOIN assessment a ON c.assessment_id = a.id
            LEFT JOIN supplier_projection s ON a.supplier_id = s.supplier_id
            WHERE c.assessment_id = :id OR s.supplier_id = :id
            and s.deleted_at is null
            """, nativeQuery = true)
      AiSummary getSummaryAi(@Param("id") String id);
//      @Modifying
//      @Transactional
//      @Query("DELETE FROM AiSummary a WHERE a.assessmentId = :assessmentId")
//      void deleteAllByAssessmentId(@Param("assessmentId") String assessmentId);
}
