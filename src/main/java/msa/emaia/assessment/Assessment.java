package msa.emaia.assessment;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import msa.emaia.assessment.assessmentSectionScore.AssessmentSectionScore;
import msa.emaia.assessment.assessmentStatus.AssessmentStatus;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.Date;
import java.util.List;

@Entity
@Table(name = "assessment")
@Data
@AllArgsConstructor
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@ToString(exclude = {"sectionsScore"})
public class Assessment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private Integer lastQuestion;

    @Column(name="form_id")
    private String formId;

    private Boolean isPreview;

    private Integer passedScore;
    private Integer maxScore;
    private Integer avgScore;
    private Integer requestedNumber;

    @Column(name="project_id")
    private String projectId;

    @ManyToOne
    @JoinColumn(name="status_id", referencedColumnName = "id")
    private AssessmentStatus status;

    @Column(name="supplier_id")
    private String supplierId;

    @Column(name="score_range_id")
    private String scoreRangeId;

    @Column(name="risk_level_id")
    private String riskLevelId;

    @OneToMany(mappedBy = "assessment", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<AssessmentSectionScore> sectionsScore;

    @Temporal(TemporalType.TIMESTAMP)
    private Date requestedOn;
    @Temporal(TemporalType.TIMESTAMP)
    private Date startedOn;
    @Temporal(TemporalType.TIMESTAMP)
    private Date acceptedOn;
    @Temporal(TemporalType.TIMESTAMP)
    private Date completedOn;
    @Temporal(TemporalType.TIMESTAMP)
    private Date assessedOn;

    /* Idempotency markers for the ADMIN_NOTIFICATION_REQUESTED / SUPPLIER_EMAIL_REQUESTED
       outbox events (Semaine 4, Jour 3 après-midi): set by AssessmentNotificationEventConsumer
       after a successful send, checked before acting on a redelivered/replayed event. */
    @Column(name = "admin_notified_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date adminNotifiedAt;

    @Column(name = "supplier_email_sent_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date supplierEmailSentAt;

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
