package msa.emaia.assessment.dto;

import msa.emaia.client.catalog.dto.RefScoreRangeMinDto;

import java.util.List;


public interface IStatisticBySectionDto {

    Integer getSectionId();
    String getSectionCode();
    String getSectionTitle();
    Integer getAvgScore();
    RefScoreRangeMinDto getScore();
    Integer getTotalScore();

    List<String> getTasks();

}


