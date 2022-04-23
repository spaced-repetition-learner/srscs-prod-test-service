package de.danielkoellgen.srscsprodtestservice.web.deckservice.dto;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public record ViewDto(

    @NotNull List<ContentElementDto> content

) {

}
