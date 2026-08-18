package msa.emaia.client.catalog;

import msa.emaia.client.catalog.dto.RefFormMinDto;
import msa.emaia.client.catalog.dto.RefQuestionMinDto;
import msa.emaia.client.catalog.dto.RefRiskLevelMinDto;
import msa.emaia.client.catalog.dto.RefScoreRangeMinDto;
import msa.emaia.client.catalog.dto.RefAttachmentMinDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import msa.emaia.client.catalog.dto.RefQuestionNextDto;
import java.util.List;

@FeignClient(name = "catalog-service", url = "${feign.catalog-service.url:http://localhost:8082}/v1")
public interface CatalogFeignClient {
    
    @GetMapping("/api/questions/next")
    RefQuestionNextDto getNextQuestionInForm(@org.springframework.web.bind.annotation.RequestParam("formId") String formId,
                                              @org.springframework.web.bind.annotation.RequestParam("from") Integer from);

    @GetMapping("/api/forms/{id}")
    RefFormMinDto getFormById(@PathVariable("id") String id);

    @GetMapping("/api/forms/project")
    RefFormMinDto getFormForProject();

    @GetMapping("/api/forms/{id}/total-questions")
    Integer getTotalQuestions(@PathVariable("id") String id);

    @GetMapping("/api/questions/{id}")
    RefQuestionMinDto getQuestionById(@PathVariable("id") String id);

    @GetMapping("/api/risk-level/code/{code}")
    RefRiskLevelMinDto getRiskLevelByCode(@PathVariable("code") String code);

    @GetMapping("/api/score-range/score/{score}")
    RefScoreRangeMinDto getScoreRangeByScore(@PathVariable("score") Integer score);

    @GetMapping("/api/attachments/{id}")
    RefAttachmentMinDto getAttachmentById(@PathVariable("id") String id);

    @GetMapping("/api/attachments/form/{formId}")
    List<RefAttachmentMinDto> getAttachmentsByFormId(@PathVariable("formId") String formId);
    @GetMapping("/api/score-range/{id}")
    RefScoreRangeMinDto getScoreRangeById(@PathVariable("id") String id);

    @GetMapping("/api/risk-level/{id}")
    RefRiskLevelMinDto getRiskLevelById(@PathVariable("id") String id);

}

