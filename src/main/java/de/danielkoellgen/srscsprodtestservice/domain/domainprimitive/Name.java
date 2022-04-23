package de.danielkoellgen.srscsprodtestservice.domain.domainprimitive;

import de.danielkoellgen.srscsprodtestservice.domain.core.AbstractStringValidation;
import jakarta.persistence.Embeddable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

@Embeddable
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
public class Name extends AbstractStringValidation {

    @Getter
    private String name;

    public Name(@NotNull String name) throws Exception {
        validateNameOrThrow(name);
        this.name = name;
    }

    private void validateNameOrThrow(@NotNull String name) throws Exception {
        validateMinLengthOrThrow(name, 3, this::mapToException);
        validateMaxLengthOrThrow(name, 12, this::mapToException);
        validateRegexOrThrow(name, "^([A-Za-z0-9 ]){3,12}$", this::mapToException);
    }

    private Exception mapToException(String message) {
        return new NameException(message);
    }

    @Override
    public String toString() {
        return "Name{" +
                "name='" + name + '\'' +
                '}';
    }
}
