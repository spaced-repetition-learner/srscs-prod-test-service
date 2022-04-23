package de.danielkoellgen.srscsprodtestservice.web.userservice.dto;

import org.jetbrains.annotations.NotNull;

public record UserRequestDto(

    @NotNull String username,

    @NotNull String mailAddress,

    @NotNull String firstName,

    @NotNull String lastName

) {
}
