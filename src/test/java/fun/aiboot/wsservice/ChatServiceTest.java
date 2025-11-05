//package fun.aiboot.wsservice;
//
//import fun.aiboot.communication.domain.ChatMessage;
//import fun.aiboot.communication.server.MessagePublisher;
//import fun.aiboot.dialogue.llm.LLMService;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.MockitoAnnotations;
//import reactor.core.publisher.Flux;
//
//import java.time.LocalDateTime;
//
//import static org.mockito.Mockito.*;
//
//class ChatServiceTest {
//
//    @Mock
//    private LLMService llmService;
//
//    @Mock
//    private MessagePublisher messagePublisher;
//
//    @InjectMocks
//    private ChatService chatService;
//
//    @BeforeEach
//    void setUp() {
//        MockitoAnnotations.openMocks(this);
//    }
//
//    @Test
//    void getType_ReturnsChat() {
//        // When
//        String type = chatService.getType();
//
//        // Then
////        assertEquals("chat", type);
//    }
//
//    @Test
//    void handleMessage_SuccessfulStream() {
//        // Given
//        String userId = "user1";
//        String content = "Hello, AI!";
//        ChatMessage message = new ChatMessage("user", content, LocalDateTime.now(), "text");
//
//        // Mock LLM service to return a stream
//        Flux<String> stream = Flux.just("Hello", " ", "user", "!");
//        when(llmService.stream(userId, content)).thenReturn(stream);
//
//        // When
//        chatService.handleMessage(userId, message);
//
//        // Then
//        // Verify that the LLM service was called
//        verify(llmService).stream(userId, content);
//
//        // Verify that messages were sent to the publisher
//        // Note: We can't easily verify the exact messages sent because they are created internally
//        // But we can verify that the publisher was called
//        verify(messagePublisher, atLeastOnce()).sendToUser(anyString(), any(ChatMessage.class));
//    }
//
//    @Test
//    void handleMessage_StreamError() {
//        // Given
//        String userId = "user1";
//        String content = "Hello, AI!";
//        ChatMessage message = new ChatMessage("user", content, LocalDateTime.now(), "text");
//
//        // Mock LLM service to return an error stream
//        Flux<String> stream = Flux.error(new RuntimeException("LLM service error"));
//        when(llmService.stream(userId, content)).thenReturn(stream);
//
//        // When
//        chatService.handleMessage(userId, message);
//
//        // Then
//        verify(llmService).stream(userId, content);
//        verify(messagePublisher, atLeastOnce()).sendToUser(anyString(), any(ChatMessage.class));
//    }
//
//    @Test
//    void handleMessage_StreamCompletes() {
//        // Given
//        String userId = "user1";
//        String content = "Hello, AI!";
//        ChatMessage message = new ChatMessage("user", content, LocalDateTime.now(), "text");
//
//        // Mock LLM service to return a stream
//        Flux<String> stream = Flux.just("Response", " ", "from", " ", "AI");
//        when(llmService.stream(userId, content)).thenReturn(stream);
//
//        // When & Then
////        StepVerifier.create(stream)
////                .expectNext("Response")
////                .expectNext(" ")
////                .expectNext("from")
////                .expectNext(" ")
////                .expectNext("AI")
////                .verifyComplete();
//    }
//}