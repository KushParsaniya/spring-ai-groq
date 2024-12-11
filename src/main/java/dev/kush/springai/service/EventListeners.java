package dev.kush.springai.service;

import dev.kush.springai.advisor.HideSensitiveContentAdvisor;
import dev.kush.springai.advisor.KeepNoteAdvisor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class EventListeners {

    private static final Logger log = LoggerFactory.getLogger(EventListeners.class);

    @EventListener(KeepNoteAdvisor.UserContainEvent.class)
    public void handleContainEvent(KeepNoteAdvisor.UserContainEvent event) {
        // TODO: analyze the user inputs
        log.info("User mentioned: {}", event.text());
    }

    @EventListener(KeepNoteAdvisor.AssistantContainEvent.class)
    public void handleAssistantContain(KeepNoteAdvisor.AssistantContainEvent event) {
        log.info("Assistant mentioned: {}", event.text());
    }

    @EventListener(HideSensitiveContentAdvisor.SensitiveContentFoundEvent.class)
    public void handleSensitiveContentFound(HideSensitiveContentAdvisor.SensitiveContentFoundEvent event) {
        log.info("Sensitive content found: {}, {}", event.message(),event.word());
    }
}
