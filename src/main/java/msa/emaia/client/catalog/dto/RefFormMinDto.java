package msa.emaia.client.catalog.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RefFormMinDto {
    private String id;
    private String code;
    private String title;
    private String description;
    private Boolean isActive;
    private Integer passedScore;
}
