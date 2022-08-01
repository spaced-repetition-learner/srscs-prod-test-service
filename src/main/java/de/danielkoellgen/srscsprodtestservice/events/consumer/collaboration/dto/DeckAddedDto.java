package de.danielkoellgen.srscsprodtestservice.events.consumer.collaboration.dto;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public record DeckAddedDto(

        @NotNull UUID collaborationId,

        @NotNull UUID userId,

        @NotNull UUID deckId

) {
    public static @NotNull DeckAddedDto makeFromSerialization(@NotNull String serialized) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();
        return mapper.readValue(serialized, DeckAddedDto.class);
    }
}
