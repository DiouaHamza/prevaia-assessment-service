package msa.emaia.assessment.assessmentSectionScore;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import msa.emaia.assessment.Assessment;
import msa.emaia.tools.JsonMapConverter;
import org.hibernate.annotations.ColumnTransformer;

import java.util.List;

@Entity
@Table(name = "assessment_section_score")
@Data
@ToString(exclude = {"assessment"})
@AllArgsConstructor
@NoArgsConstructor
public class AssessmentSectionScore {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne
    @JoinColumn(name="assessment_id", referencedColumnName = "id")
    @JsonIgnore
    private Assessment assessment;

    private String sectionCode;
    private String sectionTitle;
    private Integer totalScore;
    private Integer avgScore;

    @Convert(converter = JsonMapConverter.class)
    @Column(columnDefinition = "jsonb")
    @ColumnTransformer(write = "?::jsonb")
    private List<String> tasks;

    @Column(name="score_range_id")
    private String scoreRangeId;

}
