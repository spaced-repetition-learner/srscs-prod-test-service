package de.danielkoellgen.srscsprodtestservice.events.consumer.collaboration;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.danielkoellgen.srscsprodtestservice.domain.collaboration.application.CollaborationService;
import de.danielkoellgen.srscsprodtestservice.events.consumer.AbstractConsumerEvent;
import de.danielkoellgen.srscsprodtestservice.events.consumer.collaboration.dto.DeckAddedDto;
import lombok.Getter;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.jetbrains.annotations.NotNull;

public class DeckAdded extends AbstractConsumerEvent {

    private final CollaborationService collaborationService;

    @Getter
    private final @NotNull DeckAddedDto payload;

    public DeckAdded(@NotNull CollaborationService collaborationService, @NotNull ConsumerRecord<String, String> event)
            throws JsonProcessingException {
        super(event);
        this.collaborationService = collaborationService;
        this.payload = DeckAddedDto.makeFromSerialization(event.value());
    }

    @Override
    public void execute() {
        collaborationService.addDeckToParticipant(
                payload.collaborationId(), payload.userId(), payload.deckId()
        );
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
        return "DeckAdded{" +
                "eventId=" + eventId +
                ", transactionId=" + transactionId +
                ", eventName='" + eventName + '\'' +
                ", occurredAt=" + occurredAt +
                ", receivedAt=" + receivedAt +
                ", topic='" + topic + '\'' +
                ", payload=" + payload +
                '}';
    }
}
