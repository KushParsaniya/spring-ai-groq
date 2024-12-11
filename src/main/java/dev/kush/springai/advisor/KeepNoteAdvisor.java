package dev.kush.springai.advisor;

import org.springframework.ai.chat.client.advisor.api.AdvisedRequest;
import org.springframework.ai.chat.client.advisor.api.AdvisedResponse;
import org.springframework.ai.chat.client.advisor.api.CallAroundAdvisor;
import org.springframework.ai.chat.client.advisor.api.CallAroundAdvisorChain;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Service;

@Service
public class KeepNoteAdvisor implements CallAroundAdvisor {
    private final ApplicationEventPublisher applicationEventPublisher;

    public record UserContainEvent(String text) {}
    public record AssistantContainEvent(String text) {}

    public KeepNoteAdvisor(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Override
    public AdvisedResponse aroundCall(AdvisedRequest advisedRequest, CallAroundAdvisorChain chain) {
        if (advisedRequest.userText().toLowerCase().contains("kush")) {
            applicationEventPublisher.publishEvent(new UserContainEvent(advisedRequest.userText()));
        }

        var response = chain.nextAroundCall(advisedRequest);

        String responseContent = response.response().getResult().getOutput().getContent();
        if (responseContent.toLowerCase().contains("kush")) {
            applicationEventPublisher.publishEvent(new AssistantContainEvent(responseContent));
        }
        return response;
    }

    @Override
    public String getName() {
        return "custom-advisor";
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
