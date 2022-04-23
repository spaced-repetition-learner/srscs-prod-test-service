package de.danielkoellgen.srscsprodtestservice.web.collabservice.dto;

import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public record DeckDto(

    @NotNull UUID deckId

) {
}
