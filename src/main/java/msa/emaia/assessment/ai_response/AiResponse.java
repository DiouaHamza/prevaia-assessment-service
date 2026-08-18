package msa.emaia.assessment.ai_response;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "ai_response")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AiResponse {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "assessment_id")
    private String assessmentId;

    @Column(name = "code_question")
    private String codeQuestion;

    /* Physically TEXT on the real table (introspected read-only on the
       shared instance), not the varchar(255) Hibernate would otherwise
       default to for a plain String field - AI-extracted passages routinely
       exceed 255 chars. */
    @Column(name = "extracted_passage_1", columnDefinition = "TEXT")
    private String extractedPassage1;

    @Column(name = "extracted_passage_2", columnDefinition = "TEXT")
    private String extractedPassage2;

    @Column(name = "score")
    private Integer score;

    @Column(name = "score_1")
    private Integer score1;

    @Column(name = "category")
    private String category;

    @Column(name = "is_existant")
    private Boolean isExistant;

    @Column(name = "response")
    private String response;

}
