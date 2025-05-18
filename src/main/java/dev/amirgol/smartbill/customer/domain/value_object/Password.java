package dev.amirgol.smartbill.customer.domain.value_object;

import dev.amirgol.smartbill.customer.domain.exception.InvalidPasswordException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.regex.Pattern;

public record Password(String value) {
    private static final Logger logger = LoggerFactory.getLogger(Password.class);
    private static final Pattern PASSWORD_PATTERN = Pattern.compile("^(?=.*\\d)(?=.*[a-zA-Z])(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/]).{8,}$");
    private static final int MINIMUM_LENGTH = 8;

    public Password {
        passwordValidator(value);
    }

    private static void passwordValidator(String password) {
        Objects.requireNonNull(password, "Password value cannot be null");
        if (password.length() < MINIMUM_LENGTH) {
            logger.error("Password must be at least {} characters long", MINIMUM_LENGTH);
            throw new InvalidPasswordException("Password must be at least " + MINIMUM_LENGTH + " characters long");
        }

        if (!PASSWORD_PATTERN.matcher(password).matches()) {
            logger.error("Password must contain at least one digit, one letter, and one special character");
            throw new InvalidPasswordException("Password must contain at least one digit, one letter, and one special character");
        }
    }
}
