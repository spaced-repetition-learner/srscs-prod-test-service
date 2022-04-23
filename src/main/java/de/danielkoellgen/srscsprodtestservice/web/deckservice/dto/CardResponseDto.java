package de.danielkoellgen.srscsprodtestservice.web.deckservice.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public record CardResponseDto(

    @NotNull
    UUID cardId,

    @NotNull
    UUID deckId,

    @NotNull
    String cardType,

    @NotNull
    String cardStatus,

    @NotNull
    SchedulerDto scheduler,

    @Nullable
    HintDto hint,

    @Nullable
    ViewDto frontView,

    @Nullable
    ViewDto backView

) {
    @JsonIgnore
    public Boolean getIsActive() {
        return cardStatus.equals("active");
    }
}
