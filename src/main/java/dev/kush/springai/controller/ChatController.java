package dev.kush.springai.controller;

import dev.kush.springai.advisor.HideSensitiveContentAdvisor;
import dev.kush.springai.advisor.KeepNoteAdvisor;
import dev.kush.springai.dto.NotionPageRequest;
import org.apache.commons.lang3.StringUtils;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.QuestionAnswerAdvisor;
import org.springframework.ai.chat.client.advisor.SafeGuardAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY;

@RestController
public class ChatController {

    private final ChatClient chatClient;
    private final VectorStore vectorStore;
    private final ChatMemory chatMemory;

    @Value("classpath:prompts/first.sh")
    private Resource resource;

    @Value("classpath:prompts/notion.sh")
    private Resource notionResource;

    @Value("classpath:prompts/format.sh")
    private Resource formateResource;

    public ChatController(ChatClient.Builder builder, KeepNoteAdvisor keepNoteAdvisor, HideSensitiveContentAdvisor hideSensitiveContentAdvisor, VectorStore vectorStore, ChatMemory chatMemory) {
        this.vectorStore = vectorStore;
        this.chatMemory = chatMemory;
        this.chatClient = builder
                .defaultFunctions("weatherByCity")
                .defaultAdvisors(new SimpleLoggerAdvisor(),
                        keepNoteAdvisor,
                        hideSensitiveContentAdvisor,
                        new MessageChatMemoryAdvisor(chatMemory),
                        new SafeGuardAdvisor(List.of("violence", "abuse", "hate", "racism")))
                .build();
    }

    @GetMapping("/chat")
    public String chat(@RequestBody String message, @RequestParam String chatId) {
        return chatClient
                .prompt()
                .advisors(advisorSpec -> {
                    if (StringUtils.isNotBlank(chatId)) {
                        advisorSpec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId);
                    }
                })
                .system(resource)
                .user(message)
                .call()
                .content();
    }


    @GetMapping("/vs/chat")
    public String chatWithVectorStore(@RequestBody String message) {
        var result = vectorStore.similaritySearch(SearchRequest
                .defaults().withFilterExpression("page == 10").getQuery());
        System.out.println(result);
        return chatClient
                .prompt()
                .advisors(new QuestionAnswerAdvisor(vectorStore))
                .system(resource)
                .user(message)
                .call()
                .content();
    }

    @GetMapping("/notion")
    public NotionPageRequest.NotionPagePayload notionPage(@RequestBody String message) {
        return chatClient
                .prompt()
                .system(notionResource)
                .user(message)
                .call()
                .entity(NotionPageRequest.NotionPagePayload.class);
    }

    @GetMapping("/md")
    public String markdown(@RequestBody String message) {
        return chatClient
                .prompt()
                .system(promptSystemSpec -> promptSystemSpec.param("format", "markdown"))
                .user(message)
                .call()
                .content();
    }
}
