package msa.emaia.common.AI;

import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.timelimiter.TimeLimiterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import org.springframework.test.util.ReflectionTestUtils;

import static org.mockito.Mockito.*;

public class AIApiServiceTest {

    @Mock
    private WebClient.Builder webClientBuilder;

    @Mock
    private WebClient webClient;

    @Mock
    private WebClient.RequestBodyUriSpec requestBodyUriSpec;

    @Mock
    private WebClient.RequestBodySpec requestBodySpec;

    @Mock
    private WebClient.ResponseSpec responseSpec;

    @InjectMocks
    private AIApiService aiApiService;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        when(webClientBuilder.baseUrl(anyString())).thenReturn(webClientBuilder);
        when(webClientBuilder.build()).thenReturn(webClient);
    }

    @Test
    public void testCallGenerateResponseApi_success() {
        // Arrange
        String baseUrl = "http://localhost:8000";
        String endpoint = "/api/trigger";
        String assessmentId = "123";

        // Set private fields
        aiApiService = new AIApiService(webClientBuilder, CircuitBreakerRegistry.ofDefaults(), TimeLimiterRegistry.ofDefaults());
        ReflectionTestUtils.setField(aiApiService, "BASE_URL", baseUrl);
        ReflectionTestUtils.setField(aiApiService, "GenerateResponseApi", endpoint);

        // Mock WebClient chain
        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(endpoint)).thenReturn(requestBodySpec);
        when(requestBodySpec.body(any(Mono.class), eq(String.class))).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(Void.class)).thenReturn(Mono.empty());

        // Act
        aiApiService.callGenerateResponseApi(assessmentId);

        // Assert
        verify(webClient).post();
        verify(requestBodyUriSpec).uri(endpoint);
        verify(requestBodySpec).body(any(Mono.class), eq(String.class));
        verify(requestBodySpec).retrieve();
        verify(responseSpec).bodyToMono(Void.class);
    }

    @Test
    public void testCallGenerateResponseApi_withNullAssessmentId() {
        // Arrange
        aiApiService = new AIApiService(webClientBuilder, CircuitBreakerRegistry.ofDefaults(), TimeLimiterRegistry.ofDefaults());
        ReflectionTestUtils.setField(aiApiService, "BASE_URL", "http://localhost:8000");
        ReflectionTestUtils.setField(aiApiService, "GenerateResponseApi", "/api/trigger");

        // Act
        aiApiService.callGenerateResponseApi(null);

        // Assert
        verify(webClient, never()).post();
    }
}
