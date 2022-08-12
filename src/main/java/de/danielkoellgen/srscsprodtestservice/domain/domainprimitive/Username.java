package de.danielkoellgen.srscsprodtestservice.domain.domainprimitive;

import de.danielkoellgen.srscsprodtestservice.domain.core.AbstractStringValidation;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.util.Random;

@Embeddable
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
public class Username extends AbstractStringValidation {

    @Getter
    @Column(name = "username")
    private @NotNull String username;

    public Username(@NotNull String username) throws Exception {
        validateUsernameOrThrow(username);
        this.username = username;
    }

    public static @NotNull Username makeRandomUsername() {
        int leftLimit = 97; // letter 'a'
        int rightLimit = 122; // letter 'z'
        int targetStringLength = 12;
        Random random = new Random();

        String generatedString = random.ints(leftLimit, rightLimit + 1)
                .limit(targetStringLength)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();

        try {
            return new Username(generatedString);
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize random Username.");
        }
    }

    private void validateUsernameOrThrow(@NotNull String username) throws Exception {
        validateMinLengthOrThrow(username, 4, this::mapUserException);
        validateMaxLengthOrThrow(username, 16, this::mapUserException);
        validateRegexOrThrow(username, "^([A-Za-z0-9]){4,16}$", this::mapUserException);
    }

    private Exception mapUserException(String message) {
        return new UsernameException(message);
    }

    @Override
    public String toString() {
        return username;
    }
}
