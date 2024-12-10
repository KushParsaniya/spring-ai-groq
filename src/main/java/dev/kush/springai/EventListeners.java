package dev.kush.springai;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class EventListeners {

    private static final Logger log = LoggerFactory.getLogger(EventListeners.class);

    @EventListener(CustomAdvisor.UserContainEvent.class)
    public void handleContainEvent(CustomAdvisor.UserContainEvent event) {
        // TODO: analyze the user inputs
        log.info("User mentioned: {}", event.text());
    }

    @EventListener(CustomAdvisor.AssistantContainEvent.class)
    public void handleAssistantContain(CustomAdvisor.AssistantContainEvent event) {
        log.info("Assistant mentioned: {}", event.text());
    }

    @EventListener(HideSensitiveContentAdvisor.SensitiveContentFoundEvent.class)
    public void handleSensitiveContentFound(HideSensitiveContentAdvisor.SensitiveContentFoundEvent event) {
        log.info("Sensitive content found: {}, {}", event.message(),event.word());
    }
}
