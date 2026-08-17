package msa.emaia.project;

import lombok.Data;
import msa.emaia.supplier.Supplier;

@Data
public class Project {
    private String id;
    private String name;
    private String description;
    private Supplier supplier;
}
