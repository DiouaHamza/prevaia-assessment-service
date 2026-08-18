package msa.emaia.assessment.ai_summary;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.Date;

@Entity
@Table(name = "ai_summary")
@Data
@AllArgsConstructor
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@ToString()
public class AiSummary {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "assessment_id")
    private String assessmentId;

    /* Physically TEXT on the real table (introspected read-only on the
       shared instance), not the varchar(255) Hibernate would otherwise
       default to for a plain String field. */
    @Column(name = "summary", columnDefinition = "TEXT")
    private String summary;

    /* Physically present on the real table but never mapped here - added
       so ddl-auto=update creates it on a fresh DB (needed for the Jour 4
       data migration, which copies this column from the shared instance). */
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;

    @PrePersist
    private void onCreate() {
        if (createdAt == null) {
            this.setCreatedAt(new Date());
        }
    }
}
