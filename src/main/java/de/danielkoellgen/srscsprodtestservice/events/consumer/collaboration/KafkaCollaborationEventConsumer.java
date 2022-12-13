package de.danielkoellgen.srscsprodtestservice.events.consumer.collaboration;

import com.fasterxml.jackson.core.JsonProcessingException;
import de.danielkoellgen.srscsprodtestservice.domain.collaboration.application.CollaborationService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.sleuth.Span;
import org.springframework.cloud.sleuth.Tracer;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Component
public class KafkaCollaborationEventConsumer {

    private final CollaborationService collaborationService;

    @Autowired
    private Tracer tracer;

    private final Logger logger = LoggerFactory.getLogger(KafkaCollaborationEventConsumer.class);

    @Autowired
    public KafkaCollaborationEventConsumer(CollaborationService collaborationService) {
        this.collaborationService = collaborationService;
    }

//    @KafkaListener(topics = {"${kafka.topic.collaboration}"}, id = "${kafka.groupId.collaboration}")
    public void receive(@NotNull ConsumerRecord<String, String> event) throws JsonProcessingException {
        logger.trace("Receiving Collaboration-Event...");
        String eventName = getHeaderValue(event, "type");
        switch (eventName) {
            case "deck-added" -> processDeckAddedEvent(event);
            default -> {
                logger.debug("Received event on 'cdc.collaboration.0' of unknown type '{}'.", eventName);
                throw new RuntimeException("Received event on 'cdc.collaboration.0' of unknown type '"+eventName+"'.");
            }
        }
    }

    private void processDeckAddedEvent(@NotNull ConsumerRecord<String, String> event)
            throws JsonProcessingException {
        Span newSpan = tracer.nextSpan().name("event-deck-added");
        try (Tracer.SpanInScope ws = this.tracer.withSpan(newSpan.start())) {

            DeckAdded deckAdded = new DeckAdded(collaborationService, event);
            logger.debug("Received 'DeckAddedEvent'.");
            logger.debug("{}", deckAdded);
            logger.info("Processing 'DeckAddedEvent'...");
            deckAdded.execute();
            logger.info("Event processed.");

        } finally {
            newSpan.end();
        }
    }

    public static String getHeaderValue(ConsumerRecord<String, String> event, String key) {
        return new String(event.headers().lastHeader(key).value(), StandardCharsets.US_ASCII);
    }
}
