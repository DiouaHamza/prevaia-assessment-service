package msa.emaia.client.catalog.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RefQuestionNextDto {
    private RefQuestionMinDto question;
    private RefSectionMinDto section;
}