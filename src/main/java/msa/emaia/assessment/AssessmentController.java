package msa.emaia.assessment;


import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import msa.emaia.Constants;
import msa.emaia.appNotification.AppNotificationService;
import msa.emaia.assessment.ai_response.AiResponseDto;
import msa.emaia.assessment.ai_summary.AiSummaryDto;
import msa.emaia.assessment.assessmentAttachment.AssessmentAttachment;
import msa.emaia.assessment.assessmentStatus.AssessmentStatus;
import msa.emaia.assessment.assessmentStatus.AssessmentStatusRepository;
import msa.emaia.assessment.dto.*;
import msa.emaia.customer.CustomerService;
import msa.emaia.email.EmailService;
import msa.emaia.exceptions.RequiredException;
import msa.emaia.project.Project;
import msa.emaia.project.ProjectService;
import msa.emaia.client.catalog.dto.RefFormMinDto;
import msa.emaia.client.catalog.CatalogFeignClient;
import msa.emaia.client.catalog.dto.RefAttachmentMinDto;


import msa.emaia.client.catalog.dto.RefQuestionMinDto;

import msa.emaia.storage.FilesStorageService;
import msa.emaia.supplier.Supplier;
import msa.emaia.supplier.SupplierService;
import msa.emaia.tools.Tools;
import msa.emaia.client.iam.dto.UserDto;
import msa.emaia.client.iam.IamFeignClient;
import msa.emaia.client.iam.dto.ERole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/assessments")
@Slf4j
public class AssessmentController {

    @Autowired
    AssessmentRepository assessmentRepository;
    @Autowired
    AssessmentStatusRepository statusRepository;


    @Autowired
    CustomerService customerService;

    @Autowired
    ProjectService projectService;

    @Autowired
    SupplierService supplierService;


    @Autowired
    AssessmentService assessmentService;

    @Autowired
    CatalogFeignClient catalogFeignClient;

    @Autowired
    IamFeignClient iamFeignClient;
    @Autowired
    AppNotificationService appNotificationService;
    @Autowired
    FilesStorageService storageService;

    @Autowired
    private EmailService emailService;

    @GetMapping("/ai_response/{assessmentId}/{codeQuestion}")
    public AiResponseDto getAiResponse(@PathVariable("assessmentId") String assessmentId, @PathVariable("codeQuestion") String codeQuestion) {
        return assessmentService.getAiResponse(assessmentId, codeQuestion);
    }

    @GetMapping("/ai_Summary/{supplierId}")
    public AiSummaryDto getAiSummary(@PathVariable("supplierId") String supplierId) {
        return assessmentService.getAiSummary(supplierId);
    }

    @GetMapping("/statistic/{assessmentId}")
    public AssessmentResultDto getStatisticAssessmentId(@PathVariable("assessmentId") String assessmentId) {
        AssessmentResultDto assessmentResult = assessmentService.getStatisticAssessmentId(assessmentId);
        return assessmentResult;

    }

    @GetMapping("/lov/status")
    public List<AssessmentStatus> getLovStatusAssessmentId() {
        return statusRepository.findAllOrderByStep();
    }

    @DeleteMapping("/upload/{attachmentId}")
    public void deleteFile(@PathVariable("attachmentId") String attachmentId) {
        AssessmentAttachment assessmentAttachment = assessmentService.findAttachmentById(attachmentId);
        if (assessmentAttachment != null) {
            assessmentService.deleteAttachment(attachmentId);
            storageService.deleteFile(assessmentAttachment.getFileId());
        }
    }

    @PostMapping("/upload")
    public ResponseEntity<String> uploadFile(@RequestParam("file") MultipartFile file, @RequestParam("assessmentId") String assessmentId, @RequestParam("attachmentId") String attachmentId) {
        try {
            if (attachmentId == null) {
                return ResponseEntity.status(HttpStatus.NO_CONTENT).body("Attachment not found");
            }

            if (assessmentId == null) {
                return ResponseEntity.status(HttpStatus.NO_CONTENT).body("Assessment not found");
            }

            Assessment assessment = assessmentService.findById(assessmentId);
            RefAttachmentMinDto attachment = catalogFeignClient.getAttachmentById(attachmentId);

            if (attachment == null) {
                return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body("Attachment not found");
            }

            if (assessment == null) {
                return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body("Assessment not found");
            }

            if (attachment.getRequired() && (file == null || file.isEmpty())) {
                return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body("File is required");
            }

            String filename = Tools.generateFileNameWithDate(file);
            String fileId = storageService.createFile(filename, assessment.getId(), file);

            AssessmentAttachment assessmentAttachment = new AssessmentAttachment();

            assessmentAttachment.setAssessment(assessment);
            assessmentAttachment.setAttachmentId(attachment.getId());
            assessmentAttachment.setFilename(filename);
            assessmentAttachment.setFileId(fileId);
            assessmentAttachment.setMimetype(file.getContentType());
            assessmentAttachment.setFilesize(file.getSize());
            assessmentAttachment.setOriginalName(file.getOriginalFilename());

            AssessmentAttachment aa = assessmentService.addAttachment(assessmentAttachment);

            List<RefAttachmentMinDto> missedAttachments = assessmentService.findMissedAttachments(assessmentAttachment.getAssessment().getId());
            if(missedAttachments.isEmpty()) {
                assessmentService.setAIGenerateResponse(assessmentAttachment.getAssessment().getId());
            }

            return ResponseEntity.status(HttpStatus.OK).body(aa.getId());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body("Could not upload the file. Error: " + e.getMessage());
        }
    }

    @Transactional
    @PostMapping("/validation/{assessmentId}")
    public void validation(@PathVariable("assessmentId") String assessmentId, @Valid @RequestBody List<AssessmentCommentDto> comments, Authentication authentication) {
        UserDto user = ((UserDto) authentication.getPrincipal());
        Assessment assessment = assessmentService.findById(assessmentId);
        if (assessment == null) {
            throw new RequiredException("Assessment not found");
        }

        if (ERole.ADMIN.name().equals(user.getRole().getCode())) {
            assessmentService.validateAssessment(assessment, comments);
            assessmentService.calculateAssessmentStatistic(assessment);

            Supplier supplier = supplierService.findSupplierById(assessment.getSupplierId());
            UserDto customerUser = iamFeignClient.getUserByEmail(supplier.getCustomer().getEmail());
            if(customerUser != null) {
                appNotificationService.sendAppNotifications(
                    customerUser.getId(),
                "Assessment validation",
            "Admin validate assessment : "+catalogFeignClient.getFormById(assessment.getFormId()).getTitle()+" For the supplier : "+supplier.getName()
                );
            }
        }
    }

//    @PostMapping("/ai_summary/{assessmentId}")
//    public String generatedAiSummary(@PathVariable("assessmentId") String assessmentId) {
//        assessmentService.setAIGenerateSummary(assessmentId);
//        return "success";
//    }

    @GetMapping()
    public List<Assessment> findAll(
            @RequestParam(value = "customerId", required = false) String customerId,
            @RequestParam(value = "supplierId", required = false) String supplierId,
            Authentication authentication
    ) {
        UserDto user = ((UserDto) authentication.getPrincipal());

        if (user.getRole().getCode().equals(ERole.SUPPLIER)) {
            return assessmentService.findSupplierAssessments(user.getSupplierId());
        }

        if (user.getRole().getCode().equals(ERole.CUSTOMER)) {
            return assessmentService.findCustomerAssessments(user.getCustomerId(), supplierId);
        }

        return assessmentService.findAdminAssessments(customerId, supplierId);
    }


    @GetMapping("/{id}")
    public Assessment findById(@PathVariable("id") String id) {
        return this.assessmentRepository.findById(id).orElse(null);
    }


    @GetMapping("/{formId}/question")
    public AssessmentQuestion getNextQuestion(@PathVariable("formId") String formId) {
        return this.assessmentService.getNextQuestion(formId, null, 1);
    }

    @GetMapping("/{formId}/question/{assessmentId}")
    public AssessmentQuestion getNextQuestion(@PathVariable("formId") String formId, @PathVariable("assessmentId") String assessmentId) {
        return this.assessmentService.getNextQuestion(formId, assessmentId, 1);
    }

    @GetMapping("/{formId}/attachments/{assessmentId}")
    public List<AssessmentAttachmentResponseDto> findAssessmentAttachments(@PathVariable("formId") String formId, @PathVariable("assessmentId") String assessmentId) {
        return assessmentService.getFormAttachments(formId, assessmentId);
    }

    @GetMapping("/answers/{assessmentId}")
    public List<AssessmentValuesDto> findAssessmentValuesById(@PathVariable("assessmentId") String assessmentId) {
        return this.assessmentService.findAssessmentValuesById(assessmentId);
    }

    @GetMapping("/supplier/{supplierId}")
    public List<Assessment> getSupplierAssessments(@PathVariable("supplierId") String supplierId) {
        return this.assessmentService.findBySupplierId(supplierId);
    }

    @PostMapping()
    @Transactional
    public AssessmentQuestion addAssessment(@Valid @RequestBody AssessmentRequest assessmentRequest, Authentication authentication) {

        RefQuestionMinDto question = catalogFeignClient.getQuestionById(assessmentRequest.getQuestionId());

        if (question == null) {
            throw new RequiredException("Question id not found");
        }

        int nextQuestion = question.getDisplayOrder();
        boolean shouldNavigate = true;

        if (assessmentRequest.getNextQuestion() == 0) {
            shouldNavigate = false;
        } else if (assessmentRequest.getNextQuestion() > 0) {
            nextQuestion = nextQuestion + 1;
        } else {
            nextQuestion = nextQuestion - 1;
        }

        if (nextQuestion < 1) nextQuestion = 1;

        UserDto user = (UserDto) authentication.getPrincipal();

        Assessment assessment = assessmentService.saveAssessment(
                assessmentRequest.getAssessmentId(),
                assessmentRequest.getFormId(),
                assessmentRequest.getProjectId(),
                user.getSupplierId(),
                assessmentRequest.getIsPreview()
        );

        assessment = assessmentService.saveAssessmentQuestion(
                assessment,
                assessmentRequest.getQuestionId(),
                assessmentRequest.getQuestionValue(),
                assessmentRequest.getSupplierComment(),
                nextQuestion
        );

        AssessmentQuestion assessmentQuestion;
        if (!shouldNavigate) {
            assessmentQuestion = this.assessmentService.getNextQuestion(
                    assessment.getFormId(),
                    assessment.getId(),
                    question.getDisplayOrder()
            );
        } else {
            assessmentQuestion = this.assessmentService.getNextQuestion(
                    assessment.getFormId(),
                    assessment.getId(),
                    nextQuestion
            );
        }

        if (!Constants.QUESTIONNAIRE_STARTED.equalsIgnoreCase(assessment.getStatus().getCode())) {
            assessmentQuestion.setStatus(assessment.getStatus().getCode());
            assessmentQuestion.setForm(catalogFeignClient.getFormById(assessment.getFormId()));
            assessmentQuestion.setQuestion(null);
            assessmentQuestion.setSection(null);
        }

        if (Constants.QUESTIONNAIRE_COMPLETED.equalsIgnoreCase(assessment.getStatus().getCode()) && !assessmentRequest.getIsPreview()) {
            Supplier supplier = supplierService.findSupplierById(assessment.getSupplierId());

            for (UserDto admin : iamFeignClient.getUserByRole(ERole.ADMIN.name())) {
                appNotificationService.sendAppNotifications(
                        admin.getId(), "Questionnaire is completed",
                        "Questionnaire is completed by " + supplier.getName() + " of customer " + supplier.getCustomer().getFullName());
            }

            if(supplier.getCustomer().getIsAutoValidAssessment()){
                assessmentService.calculateAssessmentStatistic(assessment);

                UserDto customerUser = iamFeignClient.getUserByEmail(supplier.getCustomer().getEmail());
                if(customerUser != null) {
                    appNotificationService.sendAppNotifications(
                            customerUser.getId(),
                            "Assessment validation",
                            "Assessment : "+catalogFeignClient.getFormById(assessment.getFormId()).getTitle()+" For the supplier : "+supplier.getName() + " is now completed"
                    );
                }
            }
        }

        return assessmentQuestion;
    }

    @PostMapping("/supplier/request")
    public Assessment requestAssessment(@Valid @RequestBody RequestAssessmentDto requestAssessmentDto) {
        // Admin notification + supplier email are handled asynchronously by
        // AssessmentNotificationEventConsumer, via the Outbox events published
        // from AssessmentService#requestAssessmentToSupplier (retry-on-failure
        // instead of the previous unprotected synchronous calls here).
        return assessmentService.requestAssessmentToSupplier(
                requestAssessmentDto.getSupplierId(),
                requestAssessmentDto.getFormId()
        );
    }


    @PostMapping("/supplier/re-request")
    public void relanceRequestAssessment(@Valid @RequestBody RequestAssessmentDto requestAssessmentDto) {

        UserDto admin = iamFeignClient.getUserByRole(ERole.ADMIN.name()).get(0);
        List<Assessment> assessments = assessmentService.findNotStartedBySupplierId(requestAssessmentDto.getSupplierId());
        assessments
            .forEach(assessment -> {
                Supplier supplier = supplierService.findSupplierById(assessment.getSupplierId());
                assessmentService.increaseNumberOfRequest(assessment);
                appNotificationService.sendAppNotifications(
                        admin.getId(),
                        "Assessment new requested",
                        "Customer requested assessment : "+catalogFeignClient.getFormById(assessment.getFormId()).getTitle()+" For supplier : "+supplier.getName()
                );

                emailService.sendEmail(supplier.getContactEmail(), "Assessment Request", "A new assessment has been assigned to you. Kindly access the Previai platform to complete it.");

            });

    }

    @GetMapping("/{formId}/question-by-id/{questionId}")
    public AssessmentQuestion getQuestionById(
            @PathVariable("formId") String formId,
            @PathVariable("questionId") String questionId,
            @RequestParam(value = "assessmentId", required = false) String assessmentId) {

        RefQuestionMinDto question = catalogFeignClient.getQuestionById(questionId);

        if (question == null) {
            throw new RequiredException("Question not found");
        }

        return this.assessmentService.getNextQuestion(formId, assessmentId, question.getDisplayOrder());
    }

}









