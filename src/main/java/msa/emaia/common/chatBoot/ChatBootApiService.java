package msa.emaia.common.chatBoot;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.reactor.circuitbreaker.operator.CircuitBreakerOperator;
import io.github.resilience4j.reactor.timelimiter.TimeLimiterOperator;
import io.github.resilience4j.timelimiter.TimeLimiter;
import io.github.resilience4j.timelimiter.TimeLimiterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
@Slf4j
public class ChatBootApiService {

    @Value("${chat.base.url}")
    private String BASE_URL;

    @Value("${chat.get_message.url}")
    private String getMessageUrl;

    private WebClient webClient = null;
    private final WebClient.Builder webClientBuilder;
    private final CircuitBreaker circuitBreaker;
    private final TimeLimiter timeLimiter;

    public ChatBootApiService(WebClient.Builder webClientBuilder, CircuitBreakerRegistry circuitBreakerRegistry, TimeLimiterRegistry timeLimiterRegistry) {
        this.webClientBuilder = webClientBuilder;
        this.circuitBreaker = circuitBreakerRegistry.circuitBreaker("fastapiAi");
        this.timeLimiter = timeLimiterRegistry.timeLimiter("fastapiAi");
    }

    public void initWebClient(){
        if(this.webClient == null && BASE_URL != null){
            this.webClient = webClientBuilder.baseUrl(BASE_URL).build();
        }
    }

    public Mono<ChatBootResponse> callChatBoot(ChatBootRequest chatBootRequest) throws BadRequestException {

        initWebClient();

        if(webClient == null){
            log.error("Base url of chatBoot server is null or incorrect");
            throw new BadRequestException("Base url of chatBoot server is null or incorrect");
        }

        if(getMessageUrl == null){
            log.error("EndPoint to get message is null ");
            throw new BadRequestException("EndPoint to get message is null");
        }

        log.info("Start call {}", getMessageUrl);

        return this.webClient.post()
            .uri(getMessageUrl)
            .body(Mono.just(chatBootRequest), ChatBootRequest.class)
            .retrieve()
            .bodyToMono(ChatBootResponse.class)
            .transformDeferred(CircuitBreakerOperator.of(circuitBreaker))
            .transformDeferred(TimeLimiterOperator.of(timeLimiter))
            ;

    }

}
