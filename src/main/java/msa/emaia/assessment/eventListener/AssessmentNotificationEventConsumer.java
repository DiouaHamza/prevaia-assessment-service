package msa.emaia.assessment.eventListener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import msa.emaia.appNotification.AppNotificationService;
import msa.emaia.assessment.Assessment;
import msa.emaia.assessment.AssessmentRepository;
import msa.emaia.client.iam.IamFeignClient;
import msa.emaia.client.iam.dto.ERole;
import msa.emaia.client.iam.dto.UserDto;
import msa.emaia.email.EmailService;
import msa.emaia.outbox.RabbitMQConfig;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Consumes the two notification events published (via the Outbox) from
 * AssessmentService#requestAssessmentToSupplier: one to alert an admin,
 * one to email the supplier. Not lazy, same reasoning as
 * PartnerProjectionEventConsumer: with spring.main.lazy-initialization=true,
 * a lazy @RabbitListener bean never gets its listener method registered.
 *
 * Any exception here is left to propagate: with
 * spring.rabbitmq.listener.simple.default-requeue-rejected=false, that is
 * what routes the message to its DLQ instead of retry-looping.
 *
 * Idempotency: each handler checks/sets a timestamp marker on the Assessment
 * itself (adminNotifiedAt / supplierEmailSentAt) before acting, so a
 * redelivered or replayed event is a silent no-op instead of a duplicate
 * notification/email.
 */
@Slf4j
@Component
@Lazy(false)
@RequiredArgsConstructor
public class AssessmentNotificationEventConsumer {

    private final AppNotificationService appNotificationService;
    private final EmailService emailService;
    private final AssessmentRepository assessmentRepository;
    private final IamFeignClient iamFeignClient;

    @Value("${app.portal.url:https://prevaia-v2-98153220814.us-central1.run.app}")
    private String portalUrl;

    @RabbitListener(queues = RabbitMQConfig.ADMIN_NOTIFICATION_REQUESTED_QUEUE)
    @Transactional
    @SuppressWarnings("unchecked")
    public void onAdminNotificationRequested(Map<String, Object> envelope) {
        Map<String, Object> payload = (Map<String, Object>) envelope.get("payload");
        String assessmentId = (String) payload.get("assessmentId");
        String supplierId = (String) payload.get("supplierId");
        String formTitle = (String) payload.get("formTitle");
        String supplierName = (String) payload.get("supplierName");

        Assessment assessment = assessmentRepository.findById(assessmentId).orElse(null);
        if (assessment == null) {
            log.warn("[AssessmentNotificationEventConsumer] Assessment {} not found, skipping admin notification", assessmentId);
            return;
        }
        if (assessment.getAdminNotifiedAt() != null) {
            log.info("[AssessmentNotificationEventConsumer] Assessment {} already notified to admin at {}, ignoring replayed event", assessmentId, assessment.getAdminNotifiedAt());
            return;
        }

        List<UserDto> admins = iamFeignClient.getUserByRole(ERole.ADMIN.name());
        if (admins.isEmpty()) {
            log.warn("[AssessmentNotificationEventConsumer] No admin user found, skipping notification for assessment {}", assessmentId);
            return;
        }

        appNotificationService.sendAppNotifications(
                admins.get(0).getId(),
                "Assessment requested",
                "Customer requested assessment : " + formTitle + " For supplier : " + supplierName
        );

        assessment.setAdminNotifiedAt(new Date());
        assessmentRepository.save(assessment);

        log.info("[AssessmentNotificationEventConsumer] Admin notified for assessment {} (supplier {})", assessmentId, supplierId);
    }

    @RabbitListener(queues = RabbitMQConfig.SUPPLIER_EMAIL_REQUESTED_QUEUE)
    @Transactional
    @SuppressWarnings("unchecked")
    public void onSupplierEmailRequested(Map<String, Object> envelope) {
        Map<String, Object> payload = (Map<String, Object>) envelope.get("payload");
        String assessmentId = (String) payload.get("assessmentId");
        String supplierName = (String) payload.get("supplierName");
        String supplierContactEmail = (String) payload.get("supplierContactEmail");
        String formTitle = (String) payload.get("formTitle");

        Assessment assessment = assessmentRepository.findById(assessmentId).orElse(null);
        if (assessment == null) {
            log.warn("[AssessmentNotificationEventConsumer] Assessment {} not found, skipping supplier email", assessmentId);
            return;
        }
        if (assessment.getSupplierEmailSentAt() != null) {
            log.info("[AssessmentNotificationEventConsumer] Assessment {} already emailed to supplier at {}, ignoring replayed event", assessmentId, assessment.getSupplierEmailSentAt());
            return;
        }
        if (supplierContactEmail == null) {
            log.warn("[AssessmentNotificationEventConsumer] No contact email in payload, skipping email for assessment {}", assessmentId);
            return;
        }

        try {
            String html = """
          <div style="font-family:Arial,Helvetica,sans-serif;max-width:560px;margin:auto;padding:24px;border:1px solid #eee;border-radius:12px">
            <div style="text-align:center;margin-bottom:16px">
              <img src="cid:logo" alt="Prevaia" style="max-width:160px;height:auto"/>
            </div>

            <h2 style="margin:0 0 12px 0;color:#111">New assessment assigned</h2>
            <p style="color:#333;margin:0 0 12px 0">
              Hello <b>%s</b>,
            </p>
            <p style="color:#333;margin:0 0 20px 0">
              A new assessment has been assigned to you: <b>%s</b>.<br/>
              Please log in to Prevaia to complete it the questionnaire at your earliest convenience.
            </p>

            <p style="text-align:center;margin:24px 0">
              <a href="%s"
                 style="display:inline-block;padding:12px 20px;border-radius:8px;text-decoration:none;
                        background:#B6E01D;color:#111;font-weight:600;border:1px solid #9AC80F">
                Start Assessment
              </a>
            </p>
          </div>
        """.formatted(supplierName, formTitle, portalUrl, portalUrl);

            Resource logo = new ClassPathResource("static/logo-dark-300x47.png");

            emailService.sendHtmlEmailWithInlineLogo(
                    supplierContactEmail,
                    "Assessment Request",
                    html,
                    logo,
                    "logo"
            );
        } catch (Exception e) {
            log.warn("[AssessmentNotificationEventConsumer] HTML email failed for assessment {}, falling back to plain text: {}", assessmentId, e.getMessage());
            emailService.sendEmail(
                    supplierContactEmail,
                    "Assessment Request",
                    "A new assessment has been assigned to you. Kindly access the Prevaia platform to complete it."
            );
        }

        assessment.setSupplierEmailSentAt(new Date());
        assessmentRepository.save(assessment);

        log.info("[AssessmentNotificationEventConsumer] Email sent to supplier for assessment {}", assessmentId);
    }
}