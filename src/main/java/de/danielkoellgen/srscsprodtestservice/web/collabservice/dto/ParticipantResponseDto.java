package de.danielkoellgen.srscsprodtestservice.web.collabservice.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.danielkoellgen.srscsprodtestservice.domain.participant.domain.ParticipantStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public record ParticipantResponseDto(

    @NotNull UUID userId,

    @NotNull String participantStatus,

    @Nullable DeckDto deck

) {

    @JsonIgnore
    public @NotNull ParticipantStatus getMappedParticipantStatus() {
        return ParticipantStatus.toEnumFromString(participantStatus);
    }
}
