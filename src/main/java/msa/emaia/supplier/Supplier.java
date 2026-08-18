package msa.emaia.supplier;

import lombok.Data;

@Data
public class Supplier {
    private String id;
    private String name;
    private String website;
    private msa.emaia.customer.Customer customer;
    private String contactEmail;
}
