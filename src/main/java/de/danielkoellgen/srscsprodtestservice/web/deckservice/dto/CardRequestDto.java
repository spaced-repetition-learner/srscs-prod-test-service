package de.danielkoellgen.srscsprodtestservice.web.deckservice.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.danielkoellgen.srscsprodtestservice.domain.card.domain.CardType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public record CardRequestDto(

    @NotNull
    UUID deckId,

    @NotNull
    String cardType,

    @Nullable
    HintDto hint,

    @Nullable
    ViewDto frontView,

    @Nullable
    ViewDto backView

) {
    @JsonIgnore
    public @NotNull CardType getMappedCardType() {
        return switch(cardType) {
            case "default" -> CardType.DEFAULT;
            default -> throw new RuntimeException("Invalid card-type");
        };
    }
}
