package de.danielkoellgen.srscsprodtestservice.domain.domainprimitive;

import de.danielkoellgen.srscsprodtestservice.domain.core.AbstractStringValidation;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

@EqualsAndHashCode(callSuper = false)
public class DeckName extends AbstractStringValidation {

    @Getter
    private final String name;

    public DeckName(@NotNull String name) throws Exception {
        validateNameOrThrow(name);
        this.name = name;
    }

    public static @NotNull DeckName makeRandomDeckName() {
        int leftLimit = 97; // letter 'a'
        int rightLimit = 122; // letter 'z'
        int targetStringLength = 8;
        Random random = new Random();

        String generatedString = random.ints(leftLimit, rightLimit + 1)
                .limit(targetStringLength)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();

        try {
            return new DeckName(generatedString);
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize random Username.");
        }
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
