package msa.emaia.assessment.assessmentValues;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import msa.emaia.assessment.Assessment;
import msa.emaia.tools.JsonMapConverter;
import org.hibernate.annotations.ColumnTransformer;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.Date;
import java.util.List;

@Entity
@Table(name = "assessment_values")
@Data
@AllArgsConstructor
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class AssessmentValues {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne
    @JoinColumn(name="assessment_id", referencedColumnName = "id")
    private Assessment assessment;

    @Size(max = 255)
    private String questionId;

    @Size(max = 255)
    private String question;

    @Size(max = 2000)
    private String description;
    private Integer score;
    private Integer totalScore;

    @Size(max = 2000)
    private String supplierComment;

    /* Distinct physical column from supplierComment - written by
       AssessmentRepository#addAssessmentAnswerComment via native SQL, never
       previously mapped on this entity (confirmed by introspecting the real
       table: both "comment" and "supplier_comment" exist as separate
       columns). Mapped here so ddl-auto=update creates it on a fresh DB. */
    @Size(max = 2000)
    private String comment;

    @Convert(converter = JsonMapConverter.class)
    @Column(columnDefinition = "jsonb")
    @ColumnTransformer(write = "?::jsonb")
    private List<String> response;

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
    private void onCreate(){
        if(createdAt == null){
            this.setCreatedAt(new Date());
        }
    }

    @PreUpdate
    private void onUpdate(){
        this.setEditedAt(new Date());
    }



}
