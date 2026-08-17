package msa.emaia.client.catalog.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RefQuestionMinDto {
    private String id;
    private String title;
    private String description;
    private RefQuestionTypeDto type;
    private Integer displayOrder;
    private Boolean isRequired;
    private Integer score;
    private List<String> options;
}
