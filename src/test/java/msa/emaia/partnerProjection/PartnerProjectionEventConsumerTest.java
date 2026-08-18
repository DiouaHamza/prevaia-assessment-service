package msa.emaia.partnerProjection;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PartnerProjectionEventConsumerTest {

    @Mock
    private SupplierProjectionRepository supplierProjectionRepository;

    @Mock
    private ProjectProjectionRepository projectProjectionRepository;

    @InjectMocks
    private PartnerProjectionEventConsumer consumer;

    private Map<String, Object> supplierEnvelope(String eventType, long occurredAtMillis, Map<String, Object> payload) {
        Map<String, Object> envelope = new HashMap<>();
        envelope.put("eventId", "evt-1");
        envelope.put("eventType", eventType);
        envelope.put("aggregateType", "Supplier");
        envelope.put("aggregateId", payload.get("supplierId"));
        envelope.put("occurredAt", occurredAtMillis);
        envelope.put("payload", payload);
        return envelope;
    }

    private Map<String, Object> projectEnvelope(String eventType, long occurredAtMillis, Map<String, Object> payload) {
        Map<String, Object> envelope = new HashMap<>();
        envelope.put("eventId", "evt-2");
        envelope.put("eventType", eventType);
        envelope.put("aggregateType", "Project");
        envelope.put("aggregateId", payload.get("projectId"));
        envelope.put("occurredAt", occurredAtMillis);
        envelope.put("payload", payload);
        return envelope;
    }

    @Test
    void onSupplierEvent_newSupplier_createsProjection() {
        Map<String, Object> payload = new HashMap<>();
        payload.put("supplierId", "sup-1");
        payload.put("name", "Acme");
        payload.put("status", "ACTIVE");
        payload.put("contactEmail", "acme@example.com");
        payload.put("customerId", "cust-1");
        payload.put("isAutoValidAssessment", true);

        when(supplierProjectionRepository.findById("sup-1")).thenReturn(Optional.empty());

        consumer.onSupplierEvent(supplierEnvelope("SUPPLIER_CREATED", 1000L, payload));

        ArgumentCaptor<SupplierProjection> captor = ArgumentCaptor.forClass(SupplierProjection.class);
        verify(supplierProjectionRepository).save(captor.capture());

        SupplierProjection saved = captor.getValue();
        assertEquals("sup-1", saved.getSupplierId());
        assertEquals("Acme", saved.getName());
        assertEquals("ACTIVE", saved.getStatus());
        assertEquals("acme@example.com", saved.getContactEmail());
        assertEquals("cust-1", saved.getCustomerId());
        assertTrue(saved.getIsAutoValidAssessment());
        assertEquals(new Date(1000L), saved.getLastEventAt());
    }

    @Test
    void onSupplierEvent_staleEvent_isIgnored() {
        SupplierProjection existing = new SupplierProjection();
        existing.setSupplierId("sup-1");
        existing.setLastEventAt(new Date(5000L));

        when(supplierProjectionRepository.findById("sup-1")).thenReturn(Optional.of(existing));

        Map<String, Object> payload = new HashMap<>();
        payload.put("supplierId", "sup-1");
        payload.put("status", "INACTIVE");

        // occurredAt (1000) is older than the projection's lastEventAt (5000) - out-of-order replay
        consumer.onSupplierEvent(supplierEnvelope("SUPPLIER_STATUS_CHANGED", 1000L, payload));

        verify(supplierProjectionRepository, never()).save(any());
    }

    @Test
    void onSupplierEvent_newerEvent_updatesExistingProjection() {
        SupplierProjection existing = new SupplierProjection();
        existing.setSupplierId("sup-1");
        existing.setName("Acme");
        existing.setStatus("ACTIVE");
        existing.setLastEventAt(new Date(1000L));

        when(supplierProjectionRepository.findById("sup-1")).thenReturn(Optional.of(existing));

        Map<String, Object> payload = new HashMap<>();
        payload.put("supplierId", "sup-1");
        payload.put("status", "INACTIVE");

        consumer.onSupplierEvent(supplierEnvelope("SUPPLIER_STATUS_CHANGED", 5000L, payload));

        ArgumentCaptor<SupplierProjection> captor = ArgumentCaptor.forClass(SupplierProjection.class);
        verify(supplierProjectionRepository).save(captor.capture());

        SupplierProjection saved = captor.getValue();
        assertEquals("INACTIVE", saved.getStatus());
        assertEquals("Acme", saved.getName()); // untouched field preserved
        assertEquals(new Date(5000L), saved.getLastEventAt());
    }

    @Test
    void onProjectEvent_newProject_createsProjection() {
        Map<String, Object> payload = new HashMap<>();
        payload.put("projectId", "proj-1");
        payload.put("supplierId", "sup-1");
        payload.put("title", "Onboarding");
        payload.put("status", "OPEN");

        when(projectProjectionRepository.findById("proj-1")).thenReturn(Optional.empty());

        consumer.onProjectEvent(projectEnvelope("PROJECT_CREATED", 1000L, payload));

        ArgumentCaptor<ProjectProjection> captor = ArgumentCaptor.forClass(ProjectProjection.class);
        verify(projectProjectionRepository).save(captor.capture());

        ProjectProjection saved = captor.getValue();
        assertEquals("proj-1", saved.getProjectId());
        assertEquals("sup-1", saved.getSupplierId());
        assertEquals("Onboarding", saved.getTitle());
        assertEquals("OPEN", saved.getStatus());
    }

    @Test
    void onProjectEvent_staleEvent_isIgnored() {
        ProjectProjection existing = new ProjectProjection();
        existing.setProjectId("proj-1");
        existing.setLastEventAt(new Date(5000L));

        when(projectProjectionRepository.findById("proj-1")).thenReturn(Optional.of(existing));

        Map<String, Object> payload = new HashMap<>();
        payload.put("projectId", "proj-1");
        payload.put("status", "CLOSED");

        consumer.onProjectEvent(projectEnvelope("PROJECT_CLOSED", 1000L, payload));

        verify(projectProjectionRepository, never()).save(any());
    }
}
