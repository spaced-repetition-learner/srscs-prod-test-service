package de.danielkoellgen.srscsprodtestservice.domain.domainprimitive;

import de.danielkoellgen.srscsprodtestservice.domain.core.AbstractStringValidation;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

import javax.persistence.Embeddable;
import java.util.Random;

@Embeddable
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
public class MailAddress extends AbstractStringValidation {

    @Getter
    private String mailAddress;

    private static final String pattern = "^[a-zA-Z0-9_!#$%&'*+/=?`{|}~^.-]+@[a-zA-Z0-9.-]+$";


    public MailAddress(@NotNull String mailAddress) throws Exception {
        validateMailAddressOrThrow(mailAddress);
        this.mailAddress = mailAddress;
    }

    public static @NotNull MailAddress makeRandomMailAddress() {
        int leftLimit = 97; // letter 'a'
        int rightLimit = 122; // letter 'z'
        int targetStringLength = 10;
        Random random = new Random();

        String generatedString = random.ints(leftLimit, rightLimit + 1)
                .limit(targetStringLength)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();

        try {
            return new MailAddress(generatedString+"@gmail.com");
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize random Username.");
        }
    }


    private void validateMailAddressOrThrow(@NotNull String mailAddress) throws Exception {
        validateRegexOrThrow(mailAddress, pattern, this::mapToException);
    }

    private Exception mapToException(String message) {
        return new MailAddressException(message);
    }

    @Override
    public String toString() {
        return "MailAddress{" +
                "mailAddress='" + mailAddress + '\'' +
                '}';
    }
}
