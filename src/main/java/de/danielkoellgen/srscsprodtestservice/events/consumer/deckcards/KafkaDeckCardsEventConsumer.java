package de.danielkoellgen.srscsprodtestservice.events.consumer.deckcards;

import com.fasterxml.jackson.core.JsonProcessingException;
import de.danielkoellgen.srscsprodtestservice.domain.deck.application.DeckService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Component
public class KafkaDeckCardsEventConsumer {

    private final DeckService deckService;

    private final Logger logger = LoggerFactory.getLogger(KafkaDeckCardsEventConsumer.class);


    @Autowired
    public KafkaDeckCardsEventConsumer(DeckService deckService) {
        this.deckService = deckService;
    }

    @KafkaListener(topics = {"${kafka.topic.deckscards}"}, id = "${kafka.groupId}")
    public void receive(@NotNull ConsumerRecord<String, String> event) throws JsonProcessingException {
        String eventName = getHeaderValue(event, "type");
        switch (eventName) {
            case "deck-created"     -> processDeckCreatedEvent(event);
            case "deck-disabled"    -> processDeckDisabledEvent(event);
            case "card-created"     -> processCardCreatedEvent(event);
            case "card-overridden"  -> processCardOverriddenEvent(event);
            case "card-disabled"    -> processCardDisabledEvent(event);
            default -> {
                logger.trace("Received event on 'cdc.decks-cards.0' of unknown type '{}'.", eventName);
                throw new RuntimeException("Received event on 'cdc.decks-cards.0' of unknown type '"+eventName+"'.");
            }
        }
    }

    private void processDeckCreatedEvent(@NotNull ConsumerRecord<String, String> event) throws JsonProcessingException {
        DeckCreated deckDisabled = new DeckCreated(deckService, event);
        logger.trace("Received 'DeckCreated' event. [tid={}, payload={}]",
                deckDisabled.getTransactionId(), deckDisabled);
        deckDisabled.execute();
    }

    private void processDeckDisabledEvent(@NotNull ConsumerRecord<String, String> event) throws JsonProcessingException {
        DeckDisabled deckDisabled = new DeckDisabled(deckService, event);
        logger.trace("Received 'DeckDisabled' event. [tid={}, payload={}]",
                deckDisabled.getTransactionId(), deckDisabled);
        deckDisabled.execute();
    }

    private void processCardCreatedEvent(@NotNull ConsumerRecord<String, String> event) throws JsonProcessingException {

    }

    private void processCardOverriddenEvent(@NotNull ConsumerRecord<String, String> event) throws JsonProcessingException {

    }

    private void processCardDisabledEvent(@NotNull ConsumerRecord<String, String> event) throws JsonProcessingException {

    }

    public static String getHeaderValue(ConsumerRecord<String, String> event, String key) {
        return new String(event.headers().lastHeader(key).value(), StandardCharsets.US_ASCII);
    }
}
