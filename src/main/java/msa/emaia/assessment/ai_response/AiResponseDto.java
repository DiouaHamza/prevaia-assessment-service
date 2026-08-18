package msa.emaia.assessment.ai_response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import msa.emaia.client.catalog.dto.RefAttachmentMinDto;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AiResponseDto {

    private List<RefAttachmentMinDto> missedAttachments;
    private AiResponse aiResponse;
    private String htmlResponse;
    private Integer stepDelay;

}

