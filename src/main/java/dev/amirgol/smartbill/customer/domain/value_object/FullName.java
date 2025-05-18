package dev.amirgol.smartbill.customer.domain.value_object;

import dev.amirgol.smartbill.customer.domain.exception.InvalidFullNameException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.regex.Pattern;

public record FullName(String firstName, String lastName) {
    private static final Logger logger = LoggerFactory.getLogger(FullName.class);
    // This significantly improves performance as the pattern is compiled only once at class load time
    private static final Pattern NAME_PATTERN = Pattern.compile("[a-zA-Z][a-zA-Z\\s-]{1,49}");
    private static final int MINIMUM_LENGTH = 3;
    private static final int MAXIMUM_LENGTH = 50;

    public FullName {
        fullNameValidator(firstName, lastName);
    }

    private static void fullNameValidator(String firstName, String lastName) {
        Objects.requireNonNull(firstName, "First name cannot be null");
        Objects.requireNonNull(lastName, "Last name cannot be null");

        var trimmedFirstName = firstName.trim();
        var trimmedLastName = lastName.trim();

        if (trimmedFirstName.isEmpty() && trimmedLastName.isEmpty()) {
            logger.error("First name must contain at least {} characters", MINIMUM_LENGTH);
            throw new InvalidFullNameException("First name must contain at least " + MINIMUM_LENGTH + " characters");
        }

        if (!NAME_PATTERN.matcher(firstName).matches()) {
            logger.error("First name must contain only letters, spaces, and hyphens");
            throw new InvalidFullNameException("First name must contain only letters, spaces, and hyphens");
        }

        if (!NAME_PATTERN.matcher(lastName).matches()) {
            logger.error("Last name must contain only letters, spaces, and hyphens");
            throw new InvalidFullNameException("Last name must contain only letters, spaces, and hyphens");
        }
        if (firstName.length() < MINIMUM_LENGTH || firstName.length() > MAXIMUM_LENGTH) {
            logger.error("First name must be between {} and {} characters long", MINIMUM_LENGTH, MAXIMUM_LENGTH);
            throw new InvalidFullNameException("First name must be between " + MINIMUM_LENGTH + " and " + MAXIMUM_LENGTH + " characters long");
        }
    }
}