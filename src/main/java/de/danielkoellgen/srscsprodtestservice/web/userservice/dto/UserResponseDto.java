package de.danielkoellgen.srscsprodtestservice.web.userservice.dto;

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
}
