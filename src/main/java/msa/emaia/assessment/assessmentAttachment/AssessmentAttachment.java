package msa.emaia.assessment.assessmentAttachment;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import msa.emaia.assessment.Assessment;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.Date;

@Entity
@Table(name = "assessment_attachments")
@Data
@AllArgsConstructor
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class AssessmentAttachment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String filename;
    private String filepath;
    private String mimetype;
    private Long filesize;
    private String originalName;

    @Column(name = "file_id")
    private String fileId;

    @ManyToOne
    @JoinColumn(name = "assessment_id", referencedColumnName = "id")
    private Assessment assessment;

    @Column(name = "attachment_id")
    private String attachmentId;

    /* Start trace information */
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;

    @CreatedBy
    private String createdBy;

    @Temporal(TemporalType.TIMESTAMP)
    private Date editedAt;

    @LastModifiedBy
    private String editedBy;
    /* End trace information */

    @PrePersist
    private void onCreate() {
        if (createdAt == null) {
            this.setCreatedAt(new Date());
        }
    }

    @PreUpdate
    private void onUpdate() {
        this.setEditedAt(new Date());
    }
}
