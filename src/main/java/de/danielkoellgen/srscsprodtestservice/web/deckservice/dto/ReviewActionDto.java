package de.danielkoellgen.srscsprodtestservice.web.deckservice.dto;

import de.danielkoellgen.srscsprodtestservice.domain.card.domain.ReviewAction;
import org.jetbrains.annotations.NotNull;

public class ReviewActionDto {

    public static @NotNull String fromReviewAction(@NotNull ReviewAction reviewAction) {
        return switch (reviewAction) {
            case EASY -> "easy";
            case NORMAL -> "normal";
            case HARD -> "hard";
            case LAPSE -> "lapse";
        };
    }
}
