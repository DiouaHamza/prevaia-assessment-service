package msa.emaia.assessment.assessmentAttachment;

import msa.emaia.assessment.dto.AssessmentAttachmentDto;
import msa.emaia.assessment.dto.IAttachmentsDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AssessmentAttachmentRepository extends JpaRepository<AssessmentAttachment, String> {

    @Query("""
        SELECT new msa.emaia.assessment.dto.AssessmentAttachmentDto(
            aa.id,
            aa.assessment.id,
            aa.attachmentId,
            aa.originalName,
            aa.filepath,
            aa.mimetype,
            aa.filesize
        )
        FROM AssessmentAttachment aa
        WHERE aa.assessment.id = :assessmentId
          AND aa.attachmentId = :attachmentId
    """)
    List<AssessmentAttachmentDto> findByAssessmentIdAndAttachmentId(
            @Param("assessmentId") String assessmentId,
            @Param("attachmentId") String attachmentId
    );

    @Query(value = """
        SELECT 
            aa.original_name AS originalName,
            aa.filename,
            aa.mimetype,
            aa.file_id AS fileId,
            aa.filesize
        FROM assessment_attachments aa
        JOIN assessment a ON aa.assessment_id = a.id
        JOIN supplier_projection s ON s.supplier_id = a.supplier_id
        WHERE a.supplier_id = :supplierId
          AND s.deleted_at IS NULL
        ORDER BY aa.filename DESC
    """, nativeQuery = true)
    List<IAttachmentsDto> listAttachmentsBySupplierId(@Param("supplierId") String supplierId);
}
