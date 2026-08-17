package msa.emaia.supplier;


import msa.emaia.assessment.AssessmentService;
import msa.emaia.assessment.dto.AssessmentResultDto;
import msa.emaia.assessment.dto.IAttachmentsDto;
import msa.emaia.common.PaginatedResponse;
import msa.emaia.supplier.dto.SupplierAssessmentDto;
import msa.emaia.client.iam.dto.UserDto;
import msa.emaia.client.iam.IamFeignClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

/**
 * Composite Supplier+Assessment reads that legitimately span both domains.
 * Lives in root (not partner) since Partner must not import Assessment;
 * root is allowed to call into partner's SupplierService.
 */
@RestController
@RequestMapping("/api/suppliers")
public class SupplierAssessmentController {

    @Autowired
    SupplierService supplierService;

    @Autowired
    IamFeignClient iamFeignClient;

    @Autowired
    AssessmentService assessmentService;

    @GetMapping("/{supplierId}/documents")
    public List<IAttachmentsDto> getUploadedDocuments(@PathVariable("supplierId") String supplierId) {
        return this.assessmentService.getSupplierAttachments(supplierId);
    }

    @GetMapping("/statistic/{supplierId}")
    public AssessmentResultDto getStatisticAssessment(@PathVariable("supplierId") String supplierId) {
        return this.assessmentService.getStatisticSupplierId(supplierId);
    }

    @GetMapping()
    public PaginatedResponse<SupplierAssessmentDto> findAll(
            @RequestParam(value = "q", required = false) String q,
            @RequestParam(value = "tags", required = false) String tags,
            @RequestParam(value = "country", required = false) String countryId,
            @RequestParam(value = "status", required = false) String statusId,
            @RequestParam(value = "risk_level", required = false) String riskLevelId,
            @RequestParam(value = "score_range", required = false) String scoreRangeId,
            @RequestParam(defaultValue = "0", required = false) int page,
            @RequestParam(value = "sortKey", required = false) String sortKey,
            @RequestParam(value = "sortOrder", required = false) String sortOrder,
            Authentication authentication
    ) {
        UserDto user = ((UserDto) authentication.getPrincipal());

        Page<Supplier> suppliers = supplierService.findAll(
            user.getCustomerId(),
            countryId,
            statusId,
            riskLevelId,
            scoreRangeId,
            q,
            tags,
            page
        );

        List<SupplierAssessmentDto> result = new ArrayList<>();

        for(Supplier supplier : suppliers.getContent()) {
            SupplierAssessmentDto supplierAssessment = new SupplierAssessmentDto();
            supplierAssessment.setSupplier(supplier);
            supplierAssessment.setAssessmentResult(assessmentService.getStatisticSupplierId(supplier.getId()));
            result.add(supplierAssessment);
        }

        return new PaginatedResponse<>(
            result,
            suppliers.getNumber(),
            suppliers.getTotalPages(),
            suppliers.getTotalElements()
        );
    }

}

