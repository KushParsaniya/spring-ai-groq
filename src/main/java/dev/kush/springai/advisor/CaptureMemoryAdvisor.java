package dev.kush.springai.advisor;

import dev.kush.springai.service.MemoryBasicExtractor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.api.AdvisedRequest;
import org.springframework.ai.chat.client.advisor.api.AdvisedResponse;
import org.springframework.ai.chat.client.advisor.api.CallAroundAdvisor;
import org.springframework.ai.chat.client.advisor.api.CallAroundAdvisorChain;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY;

@Component
public class CaptureMemoryAdvisor implements CallAroundAdvisor {

    private static final Logger log = LoggerFactory.getLogger(CaptureMemoryAdvisor.class);

    public record MemoryLLMResponse(String content, boolean useful) {
    }

    private final VectorStore vectorStore;
    private final ExecutorService executorService;
    private final MemoryBasicExtractor memoryBasicExtractor;
    private final RetryTemplate retryTemplate;
    // TODO: cheap model like local ollama
    private final ChatClient chatClient;

    @Value("classpath:prompts/capture_memory.sh")
    private String captureMemoryScript;

    public CaptureMemoryAdvisor(VectorStore vectorStore, MemoryBasicExtractor memoryBasicExtractor, RetryTemplate retryTemplate, ChatClient.Builder builder) {
        this.vectorStore = vectorStore;
        this.chatClient = builder.build();
        this.executorService = Executors.newFixedThreadPool(2);
        this.memoryBasicExtractor = memoryBasicExtractor;
        this.retryTemplate = retryTemplate;
    }

    @Override
    public AdvisedResponse aroundCall(AdvisedRequest advisedRequest, CallAroundAdvisorChain chain) {
        var response = chain.nextAroundCall(advisedRequest);
        executorService.submit(backgroundTask(advisedRequest, response));
        return response;
    }

    private Runnable backgroundTask(AdvisedRequest advisedRequest, AdvisedResponse response) {
        return () -> {
            try {
                retryTemplate.execute((RetryCallback<Boolean, Throwable>) retryContext -> captureMemoryTask(advisedRequest, response));
            } catch (Throwable e) {
                e.printStackTrace();
            }
        };
    }

    private boolean captureMemoryTask(AdvisedRequest advisedRequest, AdvisedResponse response) {
        // TODO: extract useful info form response.
        var content = memoryBasicExtractor.extractRequestContent(advisedRequest);
        var capturedMemory = chatClient.prompt()
                .system(captureMemoryScript)
                .user(content)
                .call()
                .entity(MemoryLLMResponse.class);

        final boolean isUseful = capturedMemory != null && capturedMemory.useful();
        if (isUseful) {
            String chatId = (String) advisedRequest.adviseContext().getOrDefault(CHAT_MEMORY_CONVERSATION_ID_KEY, "");
            log.info("Captured memory: {}, chatId: {}", capturedMemory.content(), chatId);
//                    vectorStore.accept(
//                            List.of(new Document("""
//                                            Remember this about user:
//                                            %s
//                                            """.formatted(capturedMemory.content()),
//                                            Map.of("chatId", chatId)
//                                    )
//                            ));
        }
        return isUseful;
    }

    @Override
    public String getName() {
        return "captureMemoryAdvisor";
    }

    @Override
    public int getOrder() {
        return HIGHEST_PRECEDENCE;
    }
}
