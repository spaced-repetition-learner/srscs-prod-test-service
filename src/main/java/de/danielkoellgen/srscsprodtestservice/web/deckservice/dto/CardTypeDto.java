package de.danielkoellgen.srscsprodtestservice.web.deckservice.dto;

import de.danielkoellgen.srscsprodtestservice.domain.card.domain.CardType;
import org.jetbrains.annotations.NotNull;

public class CardTypeDto {

    public static @NotNull String fromCardType(@NotNull CardType cardType) {
        return switch (cardType) {
            case DEFAULT -> "default";
            case TYPING -> "typing";
        };
    }

    public static @NotNull CardType fromString(@NotNull String cardType) {
        return switch (cardType) {
            case "default" -> CardType.DEFAULT;
            case "typing" -> CardType.TYPING;
            default -> throw new RuntimeException("Invalid mapping of type String to CardType.");
        };
    }
}
