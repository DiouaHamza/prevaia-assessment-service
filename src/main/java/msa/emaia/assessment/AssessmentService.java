package msa.emaia.assessment;

import jakarta.persistence.EntityManager;
import msa.emaia.Constants;
import msa.emaia.assessment.ai_response.AiResponse;
import msa.emaia.assessment.ai_response.AiResponseDto;
import msa.emaia.assessment.ai_response.AiResponseRepository;
import msa.emaia.assessment.ai_summary.AiSummary;
import msa.emaia.assessment.ai_summary.AiSummaryDto;
import msa.emaia.assessment.ai_summary.AiSummaryRepository;
import msa.emaia.assessment.assessmentAttachment.AssessmentAttachment;
import msa.emaia.assessment.assessmentAttachment.AssessmentAttachmentRepository;
import msa.emaia.assessment.assessmentSectionScore.AssessmentSectionScore;
import msa.emaia.assessment.assessmentSectionScore.AssessmentSectionScoreRepository;
import msa.emaia.assessment.assessmentStatus.AssessmentStatus;
import msa.emaia.assessment.assessmentStatus.AssessmentStatusRepository;
import msa.emaia.assessment.assessmentValues.AssessmentValues;
import msa.emaia.assessment.assessmentValues.AssessmentValuesRepository;
import msa.emaia.assessment.assessmentValues.AssessmentValuesService;
import msa.emaia.assessment.dto.*;
import msa.emaia.assessment.eventListener.AssessmentCompletedEvent;
import msa.emaia.common.AI.AIApiService;
import msa.emaia.common.summary.SummaryAiApiService;
import msa.emaia.exceptions.RequiredException;
import msa.emaia.project.Project;
import msa.emaia.project.ProjectService;
import msa.emaia.client.catalog.dto.RefFormMinDto;
import msa.emaia.client.catalog.dto.RefQuestionMinDto;
import msa.emaia.client.catalog.dto.RefRiskLevelMinDto;
import msa.emaia.client.catalog.dto.RefScoreRangeMinDto;
import msa.emaia.client.catalog.dto.RefAttachmentMinDto;

import msa.emaia.client.catalog.CatalogFeignClient;


import msa.emaia.client.catalog.dto.RefQuestionMinDto;

import msa.emaia.client.catalog.dto.RefRiskLevelMinDto;

import msa.emaia.client.catalog.dto.RefScoreRangeMinDto;

import msa.emaia.supplier.Supplier;
import msa.emaia.supplier.SupplierService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class AssessmentService {

    @Autowired
    AssessmentRepository assessmentRepository;

    
    @Autowired
    AssessmentAttachmentRepository assessmentAttachmentRepository;

    @Autowired
    CatalogFeignClient catalogFeignClient;

    
    @Autowired
    SupplierService supplierService;

    @Autowired
    AssessmentValuesService assessmentValuesService;

    @Autowired
    private ProjectService projectService;

    @Autowired
    AssessmentValuesRepository assessmentValuesRepository;

    @Autowired
    private EntityManager entityManager;


    @Autowired
    private AssessmentStatusRepository assessmentStatusRepository;


    @Autowired
    private AiResponseRepository aiResponseRepository;

    @Autowired
    private AssessmentSectionScoreRepository assessmentSectionScoreRepository;

    @Autowired
    private AIApiService aIApiService;

    @Autowired
    private AiSummaryRepository aiSummaryRepository;

    @Autowired
    private SummaryAiApiService summaryAiApiService;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Autowired
    private msa.emaia.outbox.OutboxEventService outboxEventService;

    
    public void setAIGenerateResponse(String assessmentId){
        aIApiService.callGenerateResponseApi(assessmentId);
    }

    public AiResponseDto getAiResponse(String assessmentId, String codeQuestion) {
        AiResponseDto result = new AiResponseDto();
        AiResponse res = aiResponseRepository.findByAssessmentIdAndCodeQuestion(assessmentId, codeQuestion);
        List<RefAttachmentMinDto> missedAttachments = findMissedAttachments(assessmentId);
        result.setMissedAttachments(missedAttachments);
        result.setAiResponse(res);
        result.setStepDelay(5);
        if(res!=null){
            StringBuilder htmlResponse = getStringBuilder(res);

            result.setHtmlResponse(htmlResponse.toString());
        }
        return result;
    }

    public void setAIGenerateSummary(String assessmentId){
        summaryAiApiService.callGenerateSummaryApi(assessmentId);
    }

    public AiSummaryDto getAiSummary(String supplierId) {
        AiSummaryDto summary = new AiSummaryDto();
        AiSummary res = aiSummaryRepository.getSummaryAi(supplierId);
//        summary.setAssessmentId(res.getAssessmentId());
        summary.setAiSummary(res);
        return summary;
    }

    private static StringBuilder getStringBuilder(AiResponse res) {
        StringBuilder htmlResponse = new StringBuilder();
        htmlResponse.append(String.format("<strong>From the passage : </strong> <p> %s </p>", res.getExtractedPassage1()));

        /*if(res.getExtractedPassage2() != null){
            htmlResponse.append(String.format("<br/><strong>And from the passage : </strong> <p> %s </p>", res.getExtractedPassage1()));
        }*/

        if(res.getResponse() != null){
            htmlResponse.append(String.format("<br/><strong>The response is : %s", res.getResponse().trim()));
            if("Yes".equalsIgnoreCase(res.getResponse().trim())){
                htmlResponse.append(String.format("<p style=\"text-align: center;\"><button type=\"button\" class=\"ant-btn root ant-btn-primary ant-btn-color-primary ant-btn-variant-solid\"><span>%s</span></button></p>", res.getResponse().trim()));
            }else if("No".equalsIgnoreCase(res.getResponse().trim())){
                htmlResponse.append(String.format("<p style=\"text-align: center;\"><button type=\"button\" class=\"ant-btn root ant-btn-primary ant-btn-color-dangerous ant-btn-variant-solid\"><span>%s</span></button></p>", res.getResponse().trim()));
            }else{
                htmlResponse.append(String.format("<p style=\"text-align: center;\"><button type=\"button\" class=\"ant-btn root ant-btn-primary\"><span>%s</span></button></p>", res.getResponse().trim()));
            }
        }else{
            htmlResponse.append("<br/>No response provided.");
        }

        return htmlResponse;
    }

    public List<RefAttachmentMinDto> findMissedAttachments(String assessmentId) {
        return assessmentRepository.findMissedAttachments(assessmentId);
    }

    public AssessmentResultDto getStatisticSupplierId(String supplierId) {
        Assessment assessment = assessmentRepository.lastSupplierAssessment(supplierId).orElse(null);
        return getStatisticAssessment(assessment);
    }

    public AssessmentResultDto getStatisticAssessmentId(String assessmentId) {
        Assessment assessment = assessmentRepository.findById(assessmentId).orElse(null);

        return getStatisticAssessment(assessment);
    }

    @Transactional
    public void increaseNumberOfRequest(Assessment assessment) {
        assessment.setRequestedNumber(assessment.getRequestedNumber()+1);
        this.assessmentRepository.saveAndFlush(assessment);
    }

    @Transactional
    public void calculateAssessmentStatistic(Assessment assessment) {

        List<IStatisticBySectionDto> istatisticBySection = assessmentRepository.statisticBySection(assessment.getId());

        List<AssessmentSectionScore> statisticBySection = new ArrayList<AssessmentSectionScore>();

        for (IStatisticBySectionDto statisticSection : istatisticBySection) {
            AssessmentSectionScore section = assessmentSectionScoreRepository.findByAssessmentIdAndSectionCode(assessment.getId(), statisticSection.getSectionCode()).orElse(new AssessmentSectionScore());
            section.setAssessment(assessment);
            section.setSectionCode(statisticSection.getSectionCode());
            section.setSectionTitle(statisticSection.getSectionTitle());
            section.setAvgScore(statisticSection.getAvgScore());
            section.setTotalScore(statisticSection.getTotalScore());
            section.setTasks(statisticSection.getTasks());
            RefScoreRangeMinDto scoreRange = catalogFeignClient.getScoreRangeByScore(statisticSection.getAvgScore());
            section.setScoreRangeId(scoreRange != null ? scoreRange.getId() : null);
            statisticBySection.add(section);
        }

        assessment.setSectionsScore(statisticBySection);

        double AvgScore = statisticBySection.stream()
            .mapToInt(AssessmentSectionScore::getAvgScore)
            //.average()
            .sum();

        assessment.setPassedScore(catalogFeignClient.getFormById(assessment.getFormId()).getPassedScore());
        assessment.setMaxScore(assessmentRepository.getTotalScoreForm(assessment.getFormId()));
        assessment.setAvgScore((int) Math.floor(AvgScore));

        RefRiskLevelMinDto riskLevel = null;
        if(assessment.getAvgScore() >= assessment.getPassedScore()){
            riskLevel = catalogFeignClient.getRiskLevelByCode("PS");
        }else{
            riskLevel = catalogFeignClient.getRiskLevelByCode("AR");
        }

        RefScoreRangeMinDto scoreRange = catalogFeignClient.getScoreRangeByScore(assessment.getAvgScore());
        //RefRiskLevelMinDto riskLevel = refRiskLevelRepository.getScore(assessment.getAvgScore());
        assessment.setRiskLevelId(riskLevel != null ? riskLevel.getId() : null);
        assessment.setScoreRangeId(scoreRange != null ? scoreRange.getId() : null);

        AssessmentStatus assessmentStatus = assessmentStatusRepository.findByCode("AC");
        assessment.setStatus(assessmentStatus);
        assessment.setAssessedOn(new Date());

        this.assessmentRepository.saveAndFlush(assessment);
        eventPublisher.publishEvent(new AssessmentCompletedEvent(assessment.getId()));
    }

    public AssessmentResultDto getStatisticAssessment(Assessment assessment) {
        AssessmentResultDto result = new AssessmentResultDto();

        if (assessment == null) return null;

        result.setPassedScore(assessment.getPassedScore());
        result.setMaxScore(assessment.getMaxScore());
        result.setSectionsScore(assessment.getSectionsScore());
        result.setStatus(assessment.getStatus());
        result.setAvgScore(assessment.getAvgScore());
        result.setScore(catalogFeignClient.getScoreRangeById(assessment.getScoreRangeId()));
        result.setRiskLevel(catalogFeignClient.getRiskLevelById(assessment.getRiskLevelId()));
        result.setRequestedOn(assessment.getRequestedOn());
        result.setAssessedOn(assessment.getAssessedOn());
        result.setStartedOn(assessment.getStartedOn());
        result.setAcceptedOn(assessment.getAcceptedOn());
        result.setCompletedOn(assessment.getCompletedOn());
        result.setRequestedNumber(assessment.getRequestedNumber());

        ImprovementsDto improvements = new ImprovementsDto();
        improvements.setImprovementActions(assessmentRepository.findAssessmentRecommendation(assessment.getId()));
        improvements.setTitle("Improvements");

        ImprovementsDto engagements = new ImprovementsDto();
        engagements.setStregths(assessmentRepository.findAssessmentEngagement(assessment.getId()));
        engagements.setTitle("Engagements");

        List<ImprovementsDto> improvementsList = new ArrayList<>();
        improvementsList.add(improvements);
        improvementsList.add(engagements);
        result.setImprovements(improvementsList);

        /*

        List<ImprovementsDto> engagementsList = new ArrayList<>();
        engagementsList.add(engagements);
        result.setEngagements(engagementsList);*/

        result.setQuestions(assessmentRepository.assessmentQuestion(assessment.getId()));

        return result;
    }


    public void deleteAttachment(String attachmentId) {
        assessmentAttachmentRepository.deleteById(attachmentId);
    }

    public AssessmentAttachment addAttachment(AssessmentAttachment assessmentAttachment) {
        return assessmentAttachmentRepository.save(assessmentAttachment);
    }

    public Assessment findById(String id) {
        return assessmentRepository.findById(id).orElse(null);
    }

    public List<Assessment> findAdminAssessments(String customerId, String supplierId) {
        return assessmentRepository.findAssessments(customerId, supplierId);
    }

    public List<Assessment> findSupplierAssessments(String supplierId) {
        if (supplierId == null) return new ArrayList<>();

        return assessmentRepository.findSupplierAssessments(supplierId);
    }

    public List<Assessment> findCustomerAssessments(String customerId, String supplierId) {
        if (customerId == null) return new ArrayList<>();

        return assessmentRepository.findCustomerAssessments(customerId, supplierId);
    }

    public Assessment findByProjectId(String projectId) {
        return this.assessmentRepository.findByProjectId(projectId);
    }

    public List<Assessment> findBySupplierId(String supplierId) {
        return this.assessmentRepository.findBySupplierId(supplierId);
    }

    public List<Assessment> findNotStartedBySupplierId(String supplierId) {
        return this.assessmentRepository.findNotStartedBySupplierId(supplierId);
    }

    public void setInvitedAssessmentToAccepted(String supplierId) {
        if(supplierId != null){
            this.assessmentRepository.setInvitedAssessmentToAccepted(supplierId);
        }
    }


//    public Assessment addSubmitForm(Assessment assessment) {
//        return this.assessmentRepository.save(assessment);
//    }

    public Assessment saveAssessment(String id, String formId, String projectId, String supplierId, Boolean isPreview) {
        Assessment newAssessment;

        if (id == null || id.isBlank()) {
            newAssessment = new Assessment();

            RefFormMinDto form = catalogFeignClient.getFormById(formId);
            if (form == null) {
                throw new RequiredException("Form id is required");
            }

            if (projectId != null) {
                msa.emaia.project.Project project = projectService.findProjectsById(projectId);
                if (project == null) {
                    throw new RequiredException("Invalid project");
                }
                newAssessment.setProjectId(projectId);
            }

            if (supplierId != null) {
                Supplier supplier = supplierService.findSupplierById(supplierId);

                if (supplier == null) {
                    throw new RequiredException("Invalid supplier");
                }
                newAssessment.setSupplierId(supplier.getId());
            }

            AssessmentStatus status = assessmentStatusRepository.findByCode(Constants.QUESTIONNAIRE_STARTED);

            newAssessment.setFormId(form.getId());
            newAssessment.setStatus(status);
            newAssessment.setLastQuestion(1);
            newAssessment.setRequestedNumber(1);
            newAssessment.setIsPreview(isPreview);

            this.assessmentRepository.saveAndFlush(newAssessment);
        } else {
            newAssessment = this.assessmentRepository.findById(id).orElseThrow();
        }

        entityManager.flush();
        entityManager.clear();

        return newAssessment;
    }

    public void saveProjectAssessment(Project project, HashMap<String, Object> questionnaire) {
        Assessment assessment = findByProjectId(project.getId());
        RefFormMinDto form = catalogFeignClient.getFormForProject();
        if (form == null) {
            throw new RequiredException("Form id is required");
        }

        // Save the assessment of project if not exists
        assessment = saveAssessment(assessment==null?null:assessment.getId(), form.getId(), project.getId(), project.getSupplier().getId(), false);

        assessmentValuesRepository.deleteByAssessmentId(assessment.getId());

        // Save the assessment questions
        for (Map.Entry<String, Object> entry : questionnaire.entrySet()){
            String questionId = entry.getKey();
            Object questionValue = entry.getValue();

            if (questionValue == null) continue;

            saveAssessmentQuestion(assessment, questionId, questionValue, null, -1);
        }
    }

    public Assessment saveAssessmentQuestion(Assessment assessment, String questionId, Object questionValue, String supplierComment, int order) {

        RefQuestionMinDto question = catalogFeignClient.getQuestionById(questionId);

        if (question == null) {
            throw new RequiredException("Invalid Question!");
        }

        assessment.setLastQuestion((order == -1) ? question.getDisplayOrder() : order);

        Integer totalQuestions = catalogFeignClient.getTotalQuestions(assessment.getFormId());

        AssessmentStatus status = assessmentStatusRepository.findByCode(Constants.QUESTIONNAIRE_STARTED);

        if (assessment.getStartedOn() == null) {
            assessment.setStartedOn(new Date());
        }

        if (Objects.equals(totalQuestions, assessment.getLastQuestion() - 1)) {
            status = assessmentStatusRepository.findByCode(Constants.QUESTIONNAIRE_COMPLETED);
            assessment.setCompletedOn(new Date());
        }

        assessment.setStatus(status);

        this.assessmentRepository.saveAndFlush(assessment);

        AssessmentValues newAssessmentValues = this.assessmentValuesRepository.findByAssessmentIdAndQuestionId(
                assessment.getId(),
                question.getId()
        ).orElse(new AssessmentValues());

        newAssessmentValues.setSupplierComment(supplierComment);

        newAssessmentValues.setQuestionId(question.getId());
        newAssessmentValues.setAssessment(assessment);
        newAssessmentValues.setQuestion(question.getTitle());
        newAssessmentValues.setDescription(question.getDescription());

        if (question.getIsRequired() && questionValue == null) {
            throw new RequiredException("Value is required");
        }


        if (questionValue != null) {
            int score = 0;
            List<String> codes = new ArrayList<String>();
            if ("CHECKBOX".equalsIgnoreCase(question.getType().getCode())) {
                codes = Arrays.stream(questionValue.toString().split(","))
                    .map(String::trim)
                    .collect(Collectors.toList());
            }else{
                codes.add(questionValue.toString());
            }

            score = assessmentValuesService.getOptionsScore(question.getId(), codes);

            newAssessmentValues.setScore(score);
            newAssessmentValues.setTotalScore(question.getScore());

            newAssessmentValues.setResponse(codes);
        }

        assessmentValuesService.save(newAssessmentValues);

        entityManager.flush();
        entityManager.clear();

        return assessment;
    }

    public List<AssessmentAttachmentResponseDto> getFormAttachments(String formId, String idAssessment) {

        List<RefAttachmentMinDto> refAttachments = catalogFeignClient.getAttachmentsByFormId(formId);

        List<AssessmentAttachmentResponseDto> result = new ArrayList<>();
        for (RefAttachmentMinDto refAttachment : refAttachments) {

            AssessmentAttachmentResponseDto assessmentAttachment = new AssessmentAttachmentResponseDto();

            assessmentAttachment.setId(refAttachment.getId());
            assessmentAttachment.setCode(refAttachment.getCode());
            assessmentAttachment.setLabel(refAttachment.getLabel());
            assessmentAttachment.setDescription(refAttachment.getDescription());
            assessmentAttachment.setAccept(refAttachment.getAccept());
            assessmentAttachment.setRequired(refAttachment.getRequired());
            assessmentAttachment.setIsMultiple(refAttachment.getIsMultiple());
            assessmentAttachment.setAttachments(assessmentAttachmentRepository.findByAssessmentIdAndAttachmentId(idAssessment, refAttachment.getId()));

            result.add(assessmentAttachment);
        }

        return result;
    }

    public AssessmentQuestion getNextQuestion(String formId, String idAssessment, Integer nextQuestion) {
        AssessmentQuestion aq = new AssessmentQuestion();
        aq.setForm(catalogFeignClient.getFormById(formId));

        msa.emaia.client.catalog.dto.RefQuestionNextDto next = catalogFeignClient.getNextQuestionInForm(formId, nextQuestion);
        if (next != null) {
            aq.setQuestion(next.getQuestion());
            aq.setSection(next.getSection());
        }

        aq.setAssessmentId(idAssessment);

        // if the assessment is validate by supplier set question and section to null
        if (Constants.QUESTIONNAIRE_COMPLETED.equalsIgnoreCase(aq.getStatus()) || Constants.ASSESSMENT_COMPLETED.equalsIgnoreCase(aq.getStatus())) {
            aq.setQuestion(null);
            aq.setSection(null);
        }

        return aq;
    }

    public Assessment requestAssessmentToSupplier(String supplierId, String formId) {

        Supplier supplier = supplierService.findSupplierById(supplierId);
        RefFormMinDto form = catalogFeignClient.getFormById(formId);

        if (supplier == null) {
            throw new RequiredException("Supplier not found");
        }

        if (form == null) {
            throw new RequiredException("Evaluation Supplier Form not found");
        }

        List<Assessment> supplierAssessments = assessmentRepository.findBySupplierId(supplier.getId());
        Assessment newAssessment = null;

        if (supplierAssessments.isEmpty()) {
            newAssessment = new Assessment();
            newAssessment.setSupplierId(supplier.getId());
            newAssessment.setFormId(form.getId());
            newAssessment.setMaxScore(0);
            newAssessment.setRequestedNumber(1);
            newAssessment.setPassedScore(form.getPassedScore());
            newAssessment.setLastQuestion(1);
            newAssessment.setIsPreview(false);
            newAssessment.setStatus(assessmentStatusRepository.findByCode(Constants.ASSESSMENT_REQUESTED));
            newAssessment.setRequestedOn(new Date());

            this.assessmentRepository.saveAndFlush(newAssessment);

            Map<String, Object> notificationPayload = new HashMap<>();
            notificationPayload.put("assessmentId", newAssessment.getId());
            notificationPayload.put("supplierId", supplier.getId());
            notificationPayload.put("formId", form.getId());
            notificationPayload.put("formTitle", form.getTitle());
            notificationPayload.put("supplierName", supplier.getName());
            notificationPayload.put("supplierContactEmail", supplier.getContactEmail());

            outboxEventService.publish(
                    "ASSESSMENT",
                    newAssessment.getId(),
                    "ADMIN_NOTIFICATION_REQUESTED",
                    notificationPayload
            );

            outboxEventService.publish(
                    "ASSESSMENT",
                    newAssessment.getId(),
                    "SUPPLIER_EMAIL_REQUESTED",
                    notificationPayload
            );
        } else {
            newAssessment = supplierAssessments.get(0);
        }

        return newAssessment;
    }

    public List<AssessmentValuesDto> findAssessmentValuesById(String idAssessment) {
        return assessmentRepository.findAssessmentAnswersById(idAssessment);
    }

    @Transactional
    public void validateAssessment(Assessment assessment, List<AssessmentCommentDto> comments) {
        if(comments != null){
            for (AssessmentCommentDto comment : comments) {
                if (comment.getComment() != null) {
                    assessmentRepository.addAssessmentAnswerComment(comment.getAnswerId(), comment.getComment());
                }
            }
        }

        //assessmentRepository.validateAssessment(assessment.getId());
        entityManager.flush();
        entityManager.clear();

    }

    public AssessmentAttachment findAttachmentById(String attachmentId) {
        return assessmentAttachmentRepository.findById(attachmentId).orElse(null);

    }

    public List<IAttachmentsDto> getSupplierAttachments(String supplierId) {
        return assessmentAttachmentRepository.listAttachmentsBySupplierId(supplierId);
    }
}