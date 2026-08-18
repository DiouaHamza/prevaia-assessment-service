package msa.emaia.client.catalog.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RefScoreRangeMinDto {
    private String id;
    private Integer minScore;
    private Integer maxScore;
}
