package msa.emaia.customer;

import lombok.Data;

@Data
public class Customer {
    private String id;
    private String fullName;
    private String email;
    private Boolean isAutoValidAssessment;
}
