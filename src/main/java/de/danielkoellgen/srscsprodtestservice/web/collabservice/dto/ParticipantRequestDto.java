package de.danielkoellgen.srscsprodtestservice.web.collabservice.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.danielkoellgen.srscsprodtestservice.domain.domainprimitive.Username;
import org.jetbrains.annotations.NotNull;

public record ParticipantRequestDto(

    @NotNull String username
) {

    @JsonIgnore
    public @NotNull Username getMappedUsername() {
        try {
            return new Username(username);
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize Username from DTO.");
        }
    }
}
