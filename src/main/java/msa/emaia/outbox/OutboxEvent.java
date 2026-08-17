package msa.emaia.outbox;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;
import msa.emaia.tools.JsonMapConverter;

import java.util.Date;
import java.util.Map;

@Entity
@Table(name = "outbox_events")
@Data
@NoArgsConstructor
public class OutboxEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String aggregateType;

    private String aggregateId;

    private String eventType;

    @Convert(converter = JsonMapConverter.class)
    @Column(columnDefinition = "text")
    private Map<String, Object> payload;

    @Enumerated(EnumType.STRING)
    private OutboxStatus status;

    private Date createdAt;

    private Date publishedAt;

    public OutboxEvent(String aggregateType, String aggregateId, String eventType, Map<String, Object> payload) {
        this.aggregateType = aggregateType;
        this.aggregateId = aggregateId;
        this.eventType = eventType;
        this.payload = payload;
        this.status = OutboxStatus.PENDING;
        this.createdAt = new Date();
    }
}
