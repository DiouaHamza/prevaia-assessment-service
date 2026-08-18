package msa.emaia.partnerProjection;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * Read-only local projection of Partner's Project, kept up to date by
 * PartnerProjectionEventConsumer. Not a source of truth - Partner owns the
 * real data, this exists only so Assessment can display project info
 * without a synchronous call.
 */
@Entity
@Table(name = "project_projection")
@Data
@NoArgsConstructor
public class ProjectProjection {

    @Id
    private String projectId;

    private String supplierId;

    private String title;

    private String status;

    private Date lastEventAt;
}
