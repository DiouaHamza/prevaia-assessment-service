package msa.emaia.assessment;

import msa.emaia.assessment.dto.*;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AssessmentRepository extends JpaRepository<Assessment, String> {

    /* All native queries below joined directly against Partner's "suppliers"
       table, which only ever worked because back's original DB still had a
       leftover, no-longer-updated copy of it. Rewritten against
       supplier_projection (kept live by PartnerProjectionEventConsumer /
       SupplierDeletedProjectionListener) so this repository works against
       Assessment's own isolated database (Semaine 4, Jour 4 matin).
       ref_sections/ref_questions/ref_question_options/ref_attachments stay
       as-is: they're mirrored locally (read-only copy from Catalog, see
       back/assessment-service-extraction-migration.sql), same table/column
       names, so no query change needed for those. */

    @Query(value = """
        select rqo.recommendation, rs.code as sectionCode, rs.title as sectionTitle, rq.score as impact, rq.title as question
        from assessment_values av
        join assessment a ON av.assessment_id = a.id
        join supplier_projection s ON a.supplier_id = s.supplier_id
        join ref_question_options rqo ON av.question_id = rqo.question_id
        join ref_questions rq ON rqo.question_id = rq.id
        join ref_sections rs ON rq.section_id = rs.id
        where av.assessment_id = :assessment_id
            and rqo.code in(
                 SELECT jsonb_array_elements_text(av.response)
            )
            and rqo.recommendation is not null
            and coalesce(rqo.score, 0) <= 0
            and s.deleted_at is null
    """, nativeQuery = true )
    List<AssessmentRecommendationDto> findAssessmentRecommendation(String assessment_id);

    @Query(value = """
        select rqo.recommendation, rs.code as sectionCode, rs.title as sectionTitle, rq.score as impact, rq.title as question
        from assessment_values av
        join assessment a ON av.assessment_id = a.id
        join supplier_projection s ON a.supplier_id = s.supplier_id
        join ref_question_options rqo ON av.question_id = rqo.question_id
        join ref_questions rq ON rqo.question_id = rq.id
        join ref_sections rs ON rq.section_id = rs.id
        where av.assessment_id = :assessment_id
            and rqo.code in(
                 SELECT jsonb_array_elements_text(av.response)
            )
            and rqo.recommendation is not null
            and coalesce(rqo.score, 0) > 0
            and s.deleted_at is null
    """, nativeQuery = true )
    List<AssessmentRecommendationDto> findAssessmentEngagement(String assessment_id);

    @Query(value = """
        select ra.id, ra.code, ra.label, ra.description, ra.accept, ra.is_multiple, ra.required
        from ref_attachments ra
        join assessment a ON a.form_id = ra.form_id
        join supplier_projection s ON a.supplier_id = s.supplier_id
        where not exists(
                select 1 from assessment_attachments aa
                where aa.attachment_id = ra.id
                    and aa.assessment_id = :assessmentId
            )
            and a.id = :assessmentId
            and ra.required = true
            and s.deleted_at is null
    """, nativeQuery = true)
    List<msa.emaia.client.catalog.dto.RefAttachmentMinDto> findMissedAttachments(String assessmentId);

    @Query(value = """
        SELECT a.*
        FROM assessment a
        JOIN supplier_projection s ON s.supplier_id = a.supplier_id
        WHERE a.supplier_id = :supplierId
          AND s.deleted_at IS NULL
          AND a.created_at = (
              SELECT MAX(aa.created_at)
              FROM assessment aa
              JOIN supplier_projection ss ON aa.supplier_id = ss.supplier_id
              WHERE aa.supplier_id = a.supplier_id
                AND ss.deleted_at IS NULL
          )
    """, nativeQuery = true)
    Optional<Assessment> lastSupplierAssessment(@Param("supplierId") String supplierId);

    @Query(value = """
       select
           rs.code              as "sectionCode",
           rs.title             as "sectionTitle",
           rq.title             as "questionTitle",
           rq.score             as "questionScore",
           av.score             as "score",
           av.supplier_comment   as "supplierComment",
           av.response          as "response"
       from assessment a
            join supplier_projection s ON a.supplier_id = s.supplier_id
            join ref_sections rs on rs.form_id = a.form_id
            left join ref_questions rq on rq.section_id = rs.id
            left join assessment_values av on rq.id = av.question_id and a.id = av.assessment_id
       where a.id = :assessmentId
            and s.deleted_at is null
       order by rs.display_order, rq.display_order
    """, nativeQuery = true)
    List<IAssessmentQuestionResultDto> assessmentQuestion(String assessmentId);

    @Query(value = """
           select
               rs.id,
               max(rs.code) "sectionCode",
               max(rs.title) as "sectionTitle",
               (select rss.tasks from ref_sections rss where rss.id = rs.id) as tasks,
               coalesce(sum(av.score), 0) as "avgScore",
               coalesce (sum(av.total_score), 0) as "totalScore"
           from assessment a
                join supplier_projection s ON a.supplier_id = s.supplier_id
                join ref_sections rs on rs.form_id = a.form_id
                left join ref_questions rq on rq.section_id = rs.id
                left join assessment_values av on rq.id = av.question_id and a.id = av.assessment_id
           where a.id = :assessmentId
                and s.deleted_at is null
           group by rs.id
    """, nativeQuery = true)
    List<IStatisticBySectionDto> statisticBySection(String assessmentId);

    @Query(value = """
       select coalesce(sum(score),0) from ref_questions rq, ref_sections rs
       where rq.section_id = rs.id
            and rs.form_id = :formId
    """, nativeQuery = true)
    int getTotalScoreForm(String formId);

    @Query(value = """
        update assessment a
            set status_id = (select id from ref_assessment_status rs where rs.code = 'AC'),
                assessed_on = now()
        from supplier_projection s
        where a.id = :assessmentId
            and a.supplier_id = s.supplier_id
            and s.deleted_at is null
        returning a.id
    """, nativeQuery = true)
    void validateAssessment(String assessmentId);

    @Query(value = """
        update assessment a
            set status_id = (select id from ref_assessment_status rs where rs.code = 'RA'),
                accepted_on = now()
        from supplier_projection s
        where a.supplier_id = :supplierId
            and a.supplier_id = s.supplier_id
            and a.status_id = (select id from ref_assessment_status rs where rs.code = 'RQ')
            and s.deleted_at is null
        returning a.id
    """, nativeQuery = true)
    void setInvitedAssessmentToAccepted(String supplierId);

    @Query(value = """
        update assessment_values av
        set comment = :comment
        from assessment a
        join supplier_projection s ON a.supplier_id = s.supplier_id
        where av.id = :id
            and av.assessment_id = a.id
            and s.deleted_at is null
        returning av.id
    """, nativeQuery = true)
    void addAssessmentAnswerComment(String id, String comment);

    @Query(value = """
        select
            ra.id, ra.code, ra.label, ra.description, ra.accept, ra.is_multiple, ra.required,
            jsonb_agg(aa.*) attachments
        from ref_attachments ra
        join assessment a ON ra.form_id = a.form_id
        join supplier_projection s ON a.supplier_id = s.supplier_id
        left join assessment_attachments aa on aa.attachment_id = ra.id and aa.assessment_id = :assessmentId
        where ra.form_id = :formId
            and a.id = :assessmentId
            and s.deleted_at is null
        group by ra.id
    """, nativeQuery = true)
    List<AssessmentAttachmentResponseDto> findAssessmentAttachments(@Param("formId") String formId, @Param("assessmentId") String assessmentId);

    @Query(value = """
         SELECT a.* FROM assessment a
         JOIN supplier_projection s ON a.supplier_id = s.supplier_id
         where a.project_id = :projectId
             and s.deleted_at is null
    """, nativeQuery = true)
    Assessment findByProjectId(@Param("projectId") String projectId);

    @Query(value = """
         select
             s.code sectionCode, s.title sectionTitle, s.description sectionDescription,
             rq.code questionCode, rq.title questionTitle, rq.description questionDescription,
             av.id responseId, av.response, av.comment
         from assessment a
         join supplier_projection sup ON a.supplier_id = sup.supplier_id
         join ref_sections s ON s.form_id = a.form_id
         join assessment_values av ON av.assessment_id = a.id
         join ref_questions rq ON av.question_id = rq.id and rq.section_id = s.id
         where a.id = :assessmentId
             and sup.deleted_at is null
         order by a.created_at desc
    """, nativeQuery = true)
    List<AssessmentValuesDto> findAssessmentAnswersById(@Param("assessmentId") String assessmentId);

    @Query(value = """
         SELECT a.* FROM assessment a
         JOIN supplier_projection s ON a.supplier_id = s.supplier_id
         where a.supplier_id = :supplierId
            and a.started_on is null
            and s.deleted_at is null
         order by a.created_at desc
    """, nativeQuery = true)
    List<Assessment> findNotStartedBySupplierId(@Param("supplierId") String supplierId);

    @Query(value = """
         SELECT a.* FROM assessment a
         JOIN supplier_projection s ON a.supplier_id = s.supplier_id
         where a.supplier_id = :supplierId
             and s.deleted_at is null
         order by a.created_at desc
    """, nativeQuery = true)
    List<Assessment> findBySupplierId(@Param("supplierId") String supplierId);

    @Query(value = """
        SELECT a.* FROM assessment a
        JOIN supplier_projection s ON a.supplier_id = s.supplier_id
        where (:customerId is null or s.customer_id = :customerId)
            and (:supplierId is null or a.supplier_id = :supplierId)
            and s.deleted_at is null
        order by a.created_at desc
    """, nativeQuery = true)
    List<Assessment> findAssessments(@Param("customerId") String customerId, @Param("supplierId") String supplierId);

    @Query(value = """
        SELECT a.* FROM assessment a
        JOIN supplier_projection s ON a.supplier_id = s.supplier_id
        where a.supplier_id = :supplierId
            and s.deleted_at is null
        order by a.created_at desc
    """, nativeQuery = true)
    List<Assessment> findSupplierAssessments(@Param("supplierId") String supplierId);

    @Query(value = """
        SELECT a.* FROM assessment a
        JOIN supplier_projection s ON a.supplier_id = s.supplier_id
        where s.customer_id = :customerId
            and (:supplierId is null or a.supplier_id = :supplierId)
            and s.deleted_at is null
        order by a.created_at desc
    """, nativeQuery = true)
    List<Assessment> findCustomerAssessments(@Param("customerId") String customerId, @Param("supplierId") String supplierId);
}
