package msa.emaia.assessment.ai_summary;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AiSummaryDto {
    String assessmentId;
    AiSummary aiSummary;
}
