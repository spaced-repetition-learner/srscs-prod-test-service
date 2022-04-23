package de.danielkoellgen.srscsprodtestservice.web.deckservice.dto;

import de.danielkoellgen.srscsprodtestservice.domain.domainprimitive.DeckName;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public record DeckRequestDto(

    @NotNull UUID userId,

    @NotNull String deckName

) {
    public @NotNull DeckName getMappedDeckName() {
        try {
            return new DeckName(deckName);
        } catch (Exception e) {
            throw new RuntimeException("Invalid deck-name.");
        }
    }
}
