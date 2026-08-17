package msa.emaia.assessment.eventListener;

import msa.emaia.appNotification.AppNotificationService;
import msa.emaia.assessment.Assessment;
import msa.emaia.assessment.AssessmentRepository;
import msa.emaia.client.iam.IamFeignClient;
import msa.emaia.client.iam.dto.UserDto;
import msa.emaia.email.EmailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AssessmentNotificationEventConsumerTest {

    @Mock
    private AppNotificationService appNotificationService;

    @Mock
    private EmailService emailService;

    @Mock
    private AssessmentRepository assessmentRepository;

    @Mock
    private IamFeignClient iamFeignClient;

    @InjectMocks
    private AssessmentNotificationEventConsumer consumer;

    private Map<String, Object> envelope(Map<String, Object> payload) {
        Map<String, Object> envelope = new HashMap<>();
        envelope.put("eventId", "evt-1");
        envelope.put("aggregateType", "ASSESSMENT");
        envelope.put("aggregateId", payload.get("assessmentId"));
        envelope.put("occurredAt", new Date());
        envelope.put("payload", payload);
        return envelope;
    }

    private Map<String, Object> notificationPayload() {
        Map<String, Object> payload = new HashMap<>();
        payload.put("assessmentId", "assess-1");
        payload.put("supplierId", "sup-1");
        payload.put("formId", "form-1");
        payload.put("formTitle", "Security Questionnaire");
        payload.put("supplierName", "Acme Corp");
        payload.put("supplierContactEmail", "contact@acme.test");
        return payload;
    }

    private UserDto admin() {
        UserDto admin = new UserDto();
        admin.setId("admin-1");
        return admin;
    }

    @Test
    void onAdminNotificationRequested_nominal_sendsNotificationAndMarksAssessment() {
        Assessment assessment = new Assessment();
        assessment.setId("assess-1");

        when(assessmentRepository.findById("assess-1")).thenReturn(Optional.of(assessment));
        when(iamFeignClient.getUserByRole("ADMIN")).thenReturn(List.of(admin()));

        consumer.onAdminNotificationRequested(envelope(notificationPayload()));

        verify(appNotificationService).sendAppNotifications(eq("admin-1"), anyString(), anyString());
        assertNotNull(assessment.getAdminNotifiedAt());
        verify(assessmentRepository).save(assessment);
    }

    @Test
    void onAdminNotificationRequested_alreadyNotified_isNoOp() {
        Assessment assessment = new Assessment();
        assessment.setId("assess-1");
        assessment.setAdminNotifiedAt(new Date());

        when(assessmentRepository.findById("assess-1")).thenReturn(Optional.of(assessment));

        consumer.onAdminNotificationRequested(envelope(notificationPayload()));

        verifyNoInteractions(appNotificationService, iamFeignClient);
        verify(assessmentRepository, never()).save(any());
    }

    @Test
    void onAdminNotificationRequested_notificationFails_propagatesForDlq() {
        Assessment assessment = new Assessment();
        assessment.setId("assess-1");

        when(assessmentRepository.findById("assess-1")).thenReturn(Optional.of(assessment));
        when(iamFeignClient.getUserByRole("ADMIN")).thenReturn(List.of(admin()));
        doThrow(new RuntimeException("IAM notification service down"))
                .when(appNotificationService).sendAppNotifications(anyString(), anyString(), anyString());

        assertThrows(RuntimeException.class, () -> consumer.onAdminNotificationRequested(envelope(notificationPayload())));

        // Marker must not be set on failure, otherwise a legitimate retry after
        // the DLQ replay would be silently skipped as "already notified".
        verify(assessmentRepository, never()).save(any());
    }

    @Test
    void onSupplierEmailRequested_nominal_sendsHtmlEmailAndMarksAssessment() throws Exception {
        Assessment assessment = new Assessment();
        assessment.setId("assess-1");

        when(assessmentRepository.findById("assess-1")).thenReturn(Optional.of(assessment));

        consumer.onSupplierEmailRequested(envelope(notificationPayload()));

        verify(emailService).sendHtmlEmailWithInlineLogo(eq("contact@acme.test"), anyString(), anyString(), any(), anyString());
        verify(emailService, never()).sendEmail(anyString(), anyString(), anyString());
        assertNotNull(assessment.getSupplierEmailSentAt());
        verify(assessmentRepository).save(assessment);
    }

    @Test
    void onSupplierEmailRequested_alreadySent_isNoOp() {
        Assessment assessment = new Assessment();
        assessment.setId("assess-1");
        assessment.setSupplierEmailSentAt(new Date());

        when(assessmentRepository.findById("assess-1")).thenReturn(Optional.of(assessment));

        consumer.onSupplierEmailRequested(envelope(notificationPayload()));

        verifyNoInteractions(emailService);
        verify(assessmentRepository, never()).save(any());
    }

    @Test
    void onSupplierEmailRequested_htmlAndFallbackBothFail_propagatesForDlq() throws Exception {
        Assessment assessment = new Assessment();
        assessment.setId("assess-1");

        when(assessmentRepository.findById("assess-1")).thenReturn(Optional.of(assessment));
        doThrow(new RuntimeException("SMTP down"))
                .when(emailService).sendHtmlEmailWithInlineLogo(anyString(), anyString(), anyString(), any(), anyString());
        doThrow(new RuntimeException("SMTP down"))
                .when(emailService).sendEmail(anyString(), anyString(), anyString());

        assertThrows(RuntimeException.class, () -> consumer.onSupplierEmailRequested(envelope(notificationPayload())));

        verify(assessmentRepository, never()).save(any());
    }
}
