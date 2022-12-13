package de.danielkoellgen.srscsprodtestservice.web.userservice.dto;

import de.danielkoellgen.srscsprodtestservice.domain.domainprimitive.Username;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public record UserResponseDto(

    @NotNull UUID userId,

    @NotNull String username,

    @NotNull String mailAddress,

    @NotNull String firstName,

    @NotNull String lastName,

    @NotNull Boolean isActive

) {

    public @NotNull Username getMappedUsername() {
        try {
            return new Username(username);
        } catch (Exception e) {
            throw new RuntimeException("String to username mapping failed for username "+ username +".");
        }
    }
}
