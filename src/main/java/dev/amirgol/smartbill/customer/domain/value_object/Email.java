package dev.amirgol.smartbill.customer.domain.value_object;

import dev.amirgol.smartbill.customer.domain.exception.InvalidEmailException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.regex.Pattern;

public record Email(String value) {
    private static final Logger logger = LoggerFactory.getLogger(Email.class);
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");

    public Email {
        emailValidator(value);
    }

    private static void emailValidator(String email) {
        Objects.requireNonNull(email, "Email value cannot be null");

        if (!EMAIL_PATTERN.matcher(email).matches()) {
            logger.error("Invalid email format");
            throw new InvalidEmailException("Invalid email format");
        }
    }

}
