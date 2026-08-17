package msa.emaia.client.catalog.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RefAttachmentMinDto {
    private String id;
    private String code;
    private String label;
    private String description;
    private String accept;
    private Boolean required;
    private Boolean isMultiple;
}
