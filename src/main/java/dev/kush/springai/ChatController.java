package dev.kush.springai;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.QuestionAnswerAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ChatController {

    private final ChatClient chatClient;
    private final CustomAdvisor customAdvisor;
    private final EmbeddingConfig embeddingConfig;

    @Value("classpath:prompts/first.txt")
    private Resource resource;

    public ChatController(ChatClient.Builder builder, CustomAdvisor customAdvisor, EmbeddingConfig embeddingConfig) {
        this.embeddingConfig = embeddingConfig;
        this.chatClient = builder
                .defaultFunctions("weatherByCity")
                .defaultAdvisors(new SimpleLoggerAdvisor(), customAdvisor)
                .build();
        this.customAdvisor = customAdvisor;
    }

    @GetMapping("/chat")
    public String chat(@RequestBody String message) {
        embeddingConfig.list.forEach(d -> System.out.println(d.getFormattedContent()));
        return chatClient
                .prompt()
                .system(resource)
                .user(message)
                .call()
                .content();
    }
}
