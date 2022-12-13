package de.danielkoellgen.srscsprodtestservice.events.consumer.deckcards;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.danielkoellgen.srscsprodtestservice.domain.card.application.CardService;
import de.danielkoellgen.srscsprodtestservice.events.consumer.AbstractConsumerEvent;
import de.danielkoellgen.srscsprodtestservice.events.consumer.deckcards.dto.CardOverriddenDto;
import lombok.Getter;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.jetbrains.annotations.NotNull;

public class CardOverridden extends AbstractConsumerEvent {

    private final CardService cardService;

    @Getter
    private final @NotNull CardOverriddenDto payload;

    public CardOverridden(@NotNull CardService cardService, @NotNull ConsumerRecord<String, String> event)
            throws JsonProcessingException {
        super(event);
        this.cardService = cardService;
        this.payload = CardOverriddenDto.makeFromSerialization(event.value());
    }

    @Override
    public void execute() {
        //TODO
    }

    @Override
    public @NotNull String getSerializedContent() {
        ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (Exception e) {
            throw new RuntimeException("ObjectMapper conversion failed.");
        }
    }

    @Override
    public String toString() {
        return "CardOverridden{" +
                "payload=" + payload +
                ", " + super.toString() +
                '}';
    }
}
