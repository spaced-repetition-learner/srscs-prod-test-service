package de.danielkoellgen.srscsprodtestservice.web.deckservice.dto;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public record DeckResponseDto(

    @NotNull UUID deckId,

    @NotNull String deckName,

    @NotNull UUID userId,

    @Nullable UUID schedulerPresetId,

    @NotNull Boolean isActive

) {

}
