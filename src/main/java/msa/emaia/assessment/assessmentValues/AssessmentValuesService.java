package msa.emaia.assessment.assessmentValues;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AssessmentValuesService {

    @Autowired
    AssessmentValuesRepository assessmentValuesRepository;

    public AssessmentValues save(AssessmentValues assessmentValues) {
        return this.assessmentValuesRepository.save(assessmentValues);
    }


    public int getOptionsScore(String questionId, List<String> codeOptions) {
        return assessmentValuesRepository.findOptionScoreByCodes(questionId, codeOptions);
    }

}
