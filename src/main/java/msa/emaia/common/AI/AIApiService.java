package msa.emaia.common.AI;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.reactor.circuitbreaker.operator.CircuitBreakerOperator;
import io.github.resilience4j.reactor.timelimiter.TimeLimiterOperator;
import io.github.resilience4j.timelimiter.TimeLimiter;
import io.github.resilience4j.timelimiter.TimeLimiterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
@Slf4j
public class AIApiService {

    @Value("${ai.base.url}")
    private String BASE_URL;

    @Value("${ai.generate_response.url}")
    private String GenerateResponseApi;

    private WebClient webClient = null;
    private final WebClient.Builder webClientBuilder;
    private final CircuitBreaker circuitBreaker;
    private final TimeLimiter timeLimiter;

    public AIApiService(WebClient.Builder webClientBuilder, CircuitBreakerRegistry circuitBreakerRegistry, TimeLimiterRegistry timeLimiterRegistry) {
        this.webClientBuilder = webClientBuilder;
        this.circuitBreaker = circuitBreakerRegistry.circuitBreaker("fastapiAi");
        this.timeLimiter = timeLimiterRegistry.timeLimiter("fastapiAi");
    }

    public void initWebClient(){
        if(this.webClient == null && BASE_URL != null){
            this.webClient = webClientBuilder.baseUrl(BASE_URL).build();
        }
    }

    public void callGenerateResponseApi(String assessmentId) {

        initWebClient();

        if(assessmentId == null){
            log.info("Assessment ID is null");
            return;
        }

        if(webClient == null){
            log.info("Base url of AI server is null or incorrect");
            return;
        }

        if(GenerateResponseApi == null){
            log.info("EndPoint to generate response is null ");
            return;
        }

        GenerateResponseRequest request = new GenerateResponseRequest();
        request.setAssessment_id(assessmentId);

        log.info("Start call {}", GenerateResponseApi);

        this.webClient.post()
            .uri(GenerateResponseApi)
            .body(Mono.just(assessmentId), String.class)
            .retrieve()
            .bodyToMono(Void.class)
            .transformDeferred(CircuitBreakerOperator.of(circuitBreaker))
            .transformDeferred(TimeLimiterOperator.of(timeLimiter))
            .onErrorResume(e -> {
                log.warn("[AIApiService] call skipped/failed for assessment {}: {}", assessmentId, e.getMessage());
                return Mono.empty();
            })
            .subscribe();

        log.info("End call {}", GenerateResponseApi);


    }

}
