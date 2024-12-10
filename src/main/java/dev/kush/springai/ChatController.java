package dev.kush.springai;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SafeGuardAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class ChatController {

    private final ChatClient chatClient;

    @Value("classpath:prompts/first.txt")
    private Resource resource;

    public ChatController(ChatClient.Builder builder, CustomAdvisor customAdvisor, HideSensitiveContentAdvisor hideSensitiveContentAdvisor) {
        this.chatClient = builder
                .defaultFunctions("weatherByCity")
                .defaultAdvisors(new SimpleLoggerAdvisor(), customAdvisor, hideSensitiveContentAdvisor, new SafeGuardAdvisor(List.of("violence", "abuse", "hate", "racism")))
                .build();
    }

    @GetMapping("/chat")
    public String chat(@RequestBody String message) {
        return chatClient
                .prompt()
                .system(resource)
                .user(message)
                .call()
                .content();
    }
}
