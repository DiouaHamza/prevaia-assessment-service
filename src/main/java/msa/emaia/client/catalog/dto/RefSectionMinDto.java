package msa.emaia.client.catalog.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RefSectionMinDto {
    private String id;
    private String title;
    private Integer displayOrder;
}
