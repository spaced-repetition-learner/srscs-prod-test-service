package de.danielkoellgen.srscsprodtestservice.web.collabservice.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.danielkoellgen.srscsprodtestservice.domain.domainprimitive.DeckName;
import de.danielkoellgen.srscsprodtestservice.domain.domainprimitive.Username;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public record CollaborationRequestDto(

    @NotNull List<String> invitedUsers,

    @NotNull String collaborationName

) {
    @JsonIgnore
    public @NotNull List<Username> getMappedInvitedUsers() {
        return invitedUsers.stream()
                .map(username -> {
                    try {
                        return new Username(username);
                    } catch (Exception e) {
                        throw new RuntimeException("Failed to initialize Username from DTO");
                    }
                }).toList();
    }

    @JsonIgnore
    public @NotNull DeckName getMappedCollaborationName() {
        try {
            return new DeckName(collaborationName);
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize DeckName from DTO.");
        }
    }
}
