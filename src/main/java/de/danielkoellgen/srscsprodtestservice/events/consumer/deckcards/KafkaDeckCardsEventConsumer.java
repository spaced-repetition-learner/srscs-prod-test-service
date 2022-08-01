package de.danielkoellgen.srscsprodtestservice.events.consumer.deckcards;

import com.fasterxml.jackson.core.JsonProcessingException;
import de.danielkoellgen.srscsprodtestservice.domain.card.application.CardService;
import de.danielkoellgen.srscsprodtestservice.domain.deck.application.DeckService;
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
public class KafkaDeckCardsEventConsumer {

    private final DeckService deckService;
    private final CardService cardService;

    @Autowired
    private Tracer tracer;

    private final Logger logger = LoggerFactory.getLogger(KafkaDeckCardsEventConsumer.class);


    @Autowired
    public KafkaDeckCardsEventConsumer(DeckService deckService, CardService cardService) {
        this.deckService = deckService;
        this.cardService = cardService;
    }

    @KafkaListener(topics = {"${kafka.topic.deckscards}"}, id = "${kafka.groupId.deckcards}")
    public void receive(@NotNull ConsumerRecord<String, String> event) throws JsonProcessingException {
        logger.trace("Receiving Deck-Cards-Event...");
        String eventName = getHeaderValue(event, "type");
        switch (eventName) {
            case "deck-created"     -> processDeckCreatedEvent(event);
            case "deck-disabled"    -> processDeckDisabledEvent(event);
            case "card-created"     -> processCardCreatedEvent(event);
            case "card-overridden"  -> processCardOverriddenEvent(event);
            case "card-disabled"    -> processCardDisabledEvent(event);
            default -> {
                logger.debug("Received event on 'cdc.decks-cards.0' of unknown type '{}'.", eventName);
                throw new RuntimeException("Received event on 'cdc.decks-cards.0' of unknown type '"+eventName+"'.");
            }
        }
    }

    private void processDeckCreatedEvent(@NotNull ConsumerRecord<String, String> event)
            throws JsonProcessingException {
        Span newSpan = tracer.nextSpan().name("event-deck-created");
        try (Tracer.SpanInScope ws = this.tracer.withSpan(newSpan.start())) {

            DeckCreated deckDisabled = new DeckCreated(deckService, event);
            logger.debug("Received 'DeckCreatedEvent'.");
            logger.debug("{}", deckDisabled);
            logger.info("Skipping 'DeckCreatedEvent'...");
//            deckDisabled.execute();
//            logger.info("Event processed.");

        } finally {
            newSpan.end();
        }
    }

    private void processDeckDisabledEvent(@NotNull ConsumerRecord<String, String> event)
            throws JsonProcessingException {
        Span newSpan = tracer.nextSpan().name("event-deck-disabled");
        try (Tracer.SpanInScope ws = this.tracer.withSpan(newSpan.start())) {

            DeckDisabled deckDisabled = new DeckDisabled(deckService, event);
            logger.debug("Received 'DeckDisabledEvent'.");
            logger.debug("{}", deckDisabled);
            logger.info("Skipped 'DeckDisabledEvent'...");
//            deckDisabled.execute();
//            logger.info("Event processed.");

        } finally {
            newSpan.end();
        }
    }

    private void processCardCreatedEvent(@NotNull ConsumerRecord<String, String> event)
            throws JsonProcessingException {
        Span newSpan = tracer.nextSpan().name("event-card-created");
        try (Tracer.SpanInScope ws = this.tracer.withSpan(newSpan.start())) {

            CardCreated cardCreated = new CardCreated(cardService, event);
            logger.debug("Received 'CardCreatedEvent'.");
            logger.debug("{}", cardCreated);
            logger.info("Skipped 'CardCreatedEvent'...");
//            cardCreated.execute();
//            logger.info("Event processed.");

        } finally {
            newSpan.end();
        }
    }

    private void processCardOverriddenEvent(@NotNull ConsumerRecord<String, String> event)
            throws JsonProcessingException {
        Span newSpan = tracer.nextSpan().name("event-card-overridden");
        try (Tracer.SpanInScope ws = this.tracer.withSpan(newSpan.start())) {

            CardOverridden cardOverridden = new CardOverridden(cardService, event);
            logger.debug("Received 'CardOverriddenEvent'.");
            logger.debug("{}", cardOverridden);
            logger.info("Skipped 'CardOverriddenEvent'...");
//            cardOverridden.execute();
//            logger.info("Event processed.");

        } finally {
            newSpan.end();
        }
    }

    private void processCardDisabledEvent(@NotNull ConsumerRecord<String, String> event)
            throws JsonProcessingException {
        Span newSpan = tracer.nextSpan().name("event-card-disabled");
        try (Tracer.SpanInScope ws = this.tracer.withSpan(newSpan.start())) {

            CardDisabled cardDisabled = new CardDisabled(event);
            logger.debug("Received 'CardDisabledEvent'.");
            logger.debug("{}", cardDisabled);
            logger.info("Skipped 'CardDisabledEvent'...");
//            cardDisabled.execute();
//            logger.info("Event processed.");

        } finally {
            newSpan.end();
        }
    }

    public static String getHeaderValue(ConsumerRecord<String, String> event, String key) {
        return new String(event.headers().lastHeader(key).value(), StandardCharsets.US_ASCII);
    }
}
