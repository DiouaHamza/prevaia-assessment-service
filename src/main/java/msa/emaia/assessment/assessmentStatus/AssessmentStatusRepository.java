package msa.emaia.assessment.assessmentStatus;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AssessmentStatusRepository extends JpaRepository<AssessmentStatus, String> {
    AssessmentStatus findByCode(String code);

    @Query(value = "SELECT e.* FROM ref_assessment_status e ORDER BY e.step", nativeQuery = true )
    List<AssessmentStatus> findAllOrderByStep();
}