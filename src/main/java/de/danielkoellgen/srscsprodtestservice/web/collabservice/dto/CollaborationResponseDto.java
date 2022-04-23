package de.danielkoellgen.srscsprodtestservice.web.collabservice.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.danielkoellgen.srscsprodtestservice.domain.domainprimitive.DeckName;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;

public record CollaborationResponseDto(

    @NotNull UUID collaborationId,

    @NotNull String collaborationName,

    @NotNull List<ParticipantResponseDto> participants

) {
    @JsonIgnore
    public @NotNull DeckName getMappedCollaborationName() {
        try {
            return new DeckName(collaborationName);
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize DeckName from DTO.");
        }
    }
}
