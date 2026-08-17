package msa.emaia.assessment.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import msa.emaia.client.catalog.dto.RefScoreRangeMinDto;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class StatisticBySectionDto {

    private String sectionCode;
    private String sectionTitle;
    private Integer avgScore;
    private Integer totalScore;

    private RefScoreRangeMinDto score;

    private String tasks;

}


