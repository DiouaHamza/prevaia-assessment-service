package msa.emaia.assessment.dto;

public interface IAssessmentQuestionResultDto {

    String getSectionCode();
    String getSectionTitle();
    String getQuestionTitle();
    Integer getQuestionScore();
    Integer getScore();
    Integer getTotalScore();
    String getSupplierComment();
    String getResponse();

}
