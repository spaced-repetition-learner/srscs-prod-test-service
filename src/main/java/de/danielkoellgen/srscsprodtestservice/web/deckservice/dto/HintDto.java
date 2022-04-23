package de.danielkoellgen.srscsprodtestservice.web.deckservice.dto;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public record HintDto(

    @NotNull
    List<ContentElementDto> content

) {
}
