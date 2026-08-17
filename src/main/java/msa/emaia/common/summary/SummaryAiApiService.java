package msa.emaia.common.summary;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.reactor.circuitbreaker.operator.CircuitBreakerOperator;
import io.github.resilience4j.reactor.timelimiter.TimeLimiterOperator;
import io.github.resilience4j.timelimiter.TimeLimiter;
import io.github.resilience4j.timelimiter.TimeLimiterRegistry;
import lombok.extern.slf4j.Slf4j;
import msa.emaia.assessment.ai_summary.AiSummary;
import msa.emaia.assessment.ai_summary.AiSummaryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
@Slf4j
public class SummaryAiApiService {

    @Value("${ai_summary.base.url}")
    private String BASE_URL;

    @Value("${ai.generate_ai_summary.url}")
    private String GenerateAISummaryApi;

    private WebClient webClient = null;
    private final WebClient.Builder webClientBuilder;
    private final CircuitBreaker circuitBreaker;
    private final TimeLimiter timeLimiter;

    @Autowired
    private AiSummaryRepository aiSummaryRepository;

    public SummaryAiApiService(WebClient.Builder webClientBuilder, CircuitBreakerRegistry circuitBreakerRegistry, TimeLimiterRegistry timeLimiterRegistry) {
        this.webClientBuilder = webClientBuilder;
        this.circuitBreaker = circuitBreakerRegistry.circuitBreaker("fastapiAi");
        this.timeLimiter = timeLimiterRegistry.timeLimiter("fastapiAi");
    }

    public void initWebClient(){
        if(this.webClient == null && BASE_URL != null){
            this.webClient = webClientBuilder.baseUrl(BASE_URL).build();
        }
    }

    public void callGenerateSummaryApi(String assessmentId) {
        initWebClient();

        if (assessmentId == null) {
            log.info("Assessment ID is null");
            return;
        }

        GenerateSummaryRequest request = new GenerateSummaryRequest();
        request.setAssessmentId(assessmentId);

        log.info("Start call {}", GenerateAISummaryApi);

        this.webClient.post()
                .uri(GenerateAISummaryApi)
                .body(Mono.just(request), GenerateSummaryRequest.class)
                .retrieve()
                .onStatus(HttpStatusCode::isError, response ->
                        response.bodyToMono(String.class).flatMap(body -> {
                            log.error("AI API returned error: {}", body);
                            return Mono.error(new RuntimeException("API error: " + body));
                        })
                )
                .bodyToMono(String.class)
                .transformDeferred(CircuitBreakerOperator.of(circuitBreaker))
                .transformDeferred(TimeLimiterOperator.of(timeLimiter))
                .publishOn(Schedulers.boundedElastic())
                .doOnError(error -> {
                    log.error("Error calling AI summary API", error);
                })
                .onErrorResume(e -> Mono.empty())
                .subscribe();

        log.info("End call {}", GenerateAISummaryApi);
    }
}