package msa.emaia.common.chatBoot;

import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.timelimiter.TimeLimiterRegistry;
import org.apache.coyote.BadRequestException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.springframework.test.util.ReflectionTestUtils;

public class ChatBootApiServiceTest {

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
    private ChatBootApiService chatBootApiService;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        when(webClientBuilder.baseUrl(anyString())).thenReturn(webClientBuilder);
        when(webClientBuilder.build()).thenReturn(webClient);
        chatBootApiService = new ChatBootApiService(webClientBuilder, CircuitBreakerRegistry.ofDefaults(), TimeLimiterRegistry.ofDefaults());
    }

    @Test
    public void testCallChatBoot_success() throws BadRequestException {
        // Arrange
        String baseUrl = "http://localhost:8001";
        String endpoint = "/chat/message";

        ChatBootRequest request = new ChatBootRequest(); // You'll need to mock or create this class
        ChatBootResponse mockResponse = new ChatBootResponse(); // You'll need to mock or create this class

        ReflectionTestUtils.setField(chatBootApiService, "BASE_URL", baseUrl);
        ReflectionTestUtils.setField(chatBootApiService, "getMessageUrl", endpoint);

        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(endpoint)).thenReturn(requestBodySpec);
        when(requestBodySpec.body(any(Mono.class), eq(ChatBootRequest.class))).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(ChatBootResponse.class)).thenReturn(Mono.just(mockResponse));

        // Act
        Mono<ChatBootResponse> result = chatBootApiService.callChatBoot(request);

        // Assert
        assertNotNull(result);
        assertEquals(mockResponse, result.block()); // block() is okay here in a unit test
    }

    @Test
    public void testCallChatBoot_nullWebClient_throwsException() {
        // Arrange
        ReflectionTestUtils.setField(chatBootApiService, "BASE_URL", null); // triggers null webClient
        ReflectionTestUtils.setField(chatBootApiService, "getMessageUrl", "/chat/message");

        ChatBootRequest request = new ChatBootRequest();

        // Act + Assert
        assertThrows(BadRequestException.class, () -> {
            chatBootApiService.callChatBoot(request);
        });
    }

    @Test
    public void testCallChatBoot_nullEndpoint_throwsException() {
        // Arrange
        ReflectionTestUtils.setField(chatBootApiService, "BASE_URL", "http://localhost");
        ReflectionTestUtils.setField(chatBootApiService, "getMessageUrl", null); // this should trigger the error

        ChatBootRequest request = new ChatBootRequest();

        // Act + Assert
        assertThrows(BadRequestException.class, () -> {
            chatBootApiService.callChatBoot(request);
        });
    }
}
