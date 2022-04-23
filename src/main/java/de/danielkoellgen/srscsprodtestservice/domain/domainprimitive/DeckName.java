package de.danielkoellgen.srscsprodtestservice.domain.domainprimitive;

import de.danielkoellgen.srscsprodtestservice.domain.core.AbstractStringValidation;
import jakarta.persistence.Column;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

@EqualsAndHashCode(callSuper = false)
public class DeckName extends AbstractStringValidation {

    @Getter
    @Column(name = "name")
    private final String name;

    public DeckName(@NotNull String name) throws Exception {
        validateNameOrThrow(name);
        this.name = name;
    }

    private void validateNameOrThrow(@NotNull String name) throws Exception {
        validateMinLengthOrThrow(name, 4, this::mapToDeckNameException);
        validateMaxLengthOrThrow(name, 16, this::mapToDeckNameException);
        validateRegexOrThrow(name, "^([A-Za-z0-9]){4,16}$", this::mapToDeckNameException);
    }

    private Exception mapToDeckNameException(String message) {
        return new DeckNameException(message);
    }
}
