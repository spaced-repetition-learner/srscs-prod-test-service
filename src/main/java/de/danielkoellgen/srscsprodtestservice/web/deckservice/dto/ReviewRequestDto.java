package de.danielkoellgen.srscsprodtestservice.web.deckservice.dto;

import org.jetbrains.annotations.NotNull;

public record ReviewRequestDto(

    @NotNull
    String reviewAction

) {
}
