package de.danielkoellgen.srscsprodtestservice.web.deckservice.dto;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public record SchedulerDto(

    @NotNull
    UUID presetId,

    @NotNull
    String presetName,

    @NotNull
    String reviewState,

    @NotNull
    Integer reviewCount,

    @NotNull
    String lastReview,

    @NotNull
    String nextReview,

    @NotNull
    Double easeFactor,

    @NotNull
    Long currentInterval,

    @Nullable
    Integer learningStep,

    @Nullable
    Integer lapseStep

) {
}
