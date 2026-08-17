package msa.emaia.project.dto;

import lombok.Data;

import java.util.HashMap;

@Data
public class SaveProjectDto {
    private String name;
    private String description;
    private HashMap<String, Object> questionnaire;
}
