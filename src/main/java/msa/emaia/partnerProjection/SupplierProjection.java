package msa.emaia.partnerProjection;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * Read-only local projection of Partner's Supplier, kept up to date by
 * PartnerProjectionEventConsumer. Not a source of truth - Partner owns the
 * real data, this exists only so Assessment can display supplier info
 * without a synchronous call.
 */
@Entity
@Table(name = "supplier_projection")
@Data
@NoArgsConstructor
public class SupplierProjection {

    @Id
    private String supplierId;

    private String name;

    private String status;

    private String contactEmail;

    private String customerId;

    private String customerFullName;

    private String customerEmail;

    private Boolean isAutoValidAssessment;

    private Date lastEventAt;

    /* Set by SupplierDeletedProjectionListener when Partner publishes
       SUPPLIER_DELETE_REQUESTED (routing key supplier.deleted). Soft marker -
       the row is kept, not removed, so native queries can filter on it the
       same way they used to filter on suppliers.deleted_at. */
    private Date deletedAt;
}
