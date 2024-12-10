package dev.kush.springai;

import org.springframework.ai.chat.client.advisor.api.AdvisedRequest;
import org.springframework.ai.chat.client.advisor.api.AdvisedResponse;
import org.springframework.ai.chat.client.advisor.api.CallAroundAdvisor;
import org.springframework.ai.chat.client.advisor.api.CallAroundAdvisorChain;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class HideSensitiveContentAdvisor implements CallAroundAdvisor {

    private final ApplicationEventPublisher applicationEventPublisher;

    public HideSensitiveContentAdvisor(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    public record SensitiveContentFoundEvent(String message, String word) {}

    @Override
    public AdvisedResponse aroundCall(AdvisedRequest advisedRequest, CallAroundAdvisorChain chain) {
        var response = chain.nextAroundCall(advisedRequest);

        String responseContent = response.response().getResult().getOutput().getContent();
        if (responseContent.toLowerCase().contains("kush")) {
            applicationEventPublisher.publishEvent(new SensitiveContentFoundEvent(responseContent, "kush"));
            return new AdvisedResponse(ChatResponse.builder()
                    .withGenerations(List.of(new Generation(new AssistantMessage(responseContent.replaceAll("kush", "k***")))))
                    .build(),
                    advisedRequest.adviseContext());
        }
        return response;
    }

    @Override
    public String getName() {
        return "hide-sensitive-content-advisor";
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
