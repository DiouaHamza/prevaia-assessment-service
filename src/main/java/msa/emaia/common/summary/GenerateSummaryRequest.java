package msa.emaia.common.summary;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GenerateSummaryRequest {

    @JsonProperty("assessment_id")
    private String assessmentId;
}
