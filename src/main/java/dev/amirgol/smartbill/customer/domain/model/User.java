package dev.amirgol.smartbill.customer.domain.model;

import dev.amirgol.smartbill.customer.domain.enums.Gender;
import dev.amirgol.smartbill.customer.domain.enums.Role;
import dev.amirgol.smartbill.customer.domain.enums.UserStatus;
import dev.amirgol.smartbill.customer.domain.exception.InvalidEmailException;
import dev.amirgol.smartbill.customer.domain.exception.InvalidFullNameException;
import dev.amirgol.smartbill.customer.domain.exception.InvalidPasswordException;
import dev.amirgol.smartbill.customer.domain.exception.InvalidStatusException;
import dev.amirgol.smartbill.customer.domain.value_object.Email;
import dev.amirgol.smartbill.customer.domain.value_object.FullName;
import dev.amirgol.smartbill.customer.domain.value_object.Id;
import dev.amirgol.smartbill.customer.domain.value_object.Password;
import lombok.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class User {
    private static final Logger logger = LoggerFactory.getLogger(User.class);
    private static final Map<UserStatus, Set<UserStatus>> VALID_STATUS_TRANSITIONS = Map.of(
            UserStatus.PENDING, EnumSet.of(UserStatus.ACTIVE, UserStatus.DELETED),
            UserStatus.ACTIVE, EnumSet.of(UserStatus.SUSPENDED, UserStatus.DELETED),
            UserStatus.SUSPENDED, EnumSet.of(UserStatus.ACTIVE, UserStatus.DELETED),
            UserStatus.DELETED, EnumSet.noneOf(UserStatus.class) // No valid transitions from DELETED
    );
    // Generated Value will be handled by postgres itself.
    private Id id;
    private Email email;
    private FullName fullName;
    private Password password;
    private Gender gender;
    private Role roles;
    private UserStatus status = UserStatus.PENDING;

    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt = LocalDateTime.now();

    private User(Id id, Email email, FullName fullName, Password password, Gender gender, Role roles) {
        this.id = id;
        this.email = email;
        this.fullName = fullName;
        this.password = password;
        this.gender = gender;
        this.roles = roles;
    }

    /**
     * Creates a User instance from pre-validated domain value objects.
     * Best used within the internal layers of the system where domain objects are already available.
     *
     * @param id       Validated Id object
     * @param email    Validated Email value object
     * @param fullName Validated FullName value object
     * @param password Validated Password value object
     * @param gender   User's gender enum
     * @param roles    User's role enum
     * @return A new User instance
     */
    public static User of(Id id, Email email, FullName fullName, Password password, Gender gender, Role roles) {
        return new User(id, email, fullName, password, gender, roles);
    }

    /**
     * Creates a User instance from primitive/basic types, handling validation internally.
     * Best used in presentation layer (controllers, UI, external APIs) where raw data is received.
     *
     * @param id        Raw ID value
     * @param email     Email address as string
     * @param firstName First name as string
     * @param lastName  Last name as string
     * @param password  Password as plain string
     * @param gender    User's gender enum
     * @param roles     User's role enum
     * @return A new User instance with validated value objects
     * @throws IllegalArgumentException If any input data fails validation
     */

    public static User of(Long id, String email, String firstName, String lastName, String password, Gender gender, Role roles) {
        return new User(new Id(id), new Email(email), new FullName(firstName, lastName), new Password(password), gender, roles);
    }

    public void changePassword(String password) {
        var currentPassword = this.password.value();
        if (Objects.equals(password, currentPassword)) {
            throw new InvalidPasswordException("The new password cannot be the same as the current password");
        }
        this.password = new Password(password);

        logger.info("User {} password changed from {} to {}", this.id, currentPassword, password);
    }

    /**
     * Changes the user's status with proper validation
     *
     * @param currentStatus The user's current status
     * @param newStatus     The requested new status
     * @throws InvalidStatusException if the status transition is not allowed
     */
    public void changeUserStatus(UserStatus currentStatus, UserStatus newStatus) {
        if (newStatus == null) {
            throw new InvalidStatusException("User status cannot be set to null");
        }

        if (currentStatus == newStatus) {
            // No change needed
            return;
        }
        // Check if the transition is valid
        var allowedTransitions = VALID_STATUS_TRANSITIONS.get(currentStatus);
        if (!allowedTransitions.contains(newStatus)) {
            throw new InvalidStatusException(String.format("Cannot change user status from '%s' to '%s'. Allowed transitions: %s", currentStatus, newStatus, allowedTransitions.isEmpty() ? "none" : allowedTransitions));
        }
        // Status transition is valid, proceed with the change
        this.status = newStatus;

        // Log the status change
        logger.info("User {} status changed from {} to {}", this.id, currentStatus, newStatus);
    }

    public void changeUserEmail(Email currentEmail, Email newEmail) {
        if (this.getStatus() == UserStatus.DELETED) {
            throw new InvalidEmailException("Deleted user cannot change email");
        }
        // Email Nullability check
        Objects.requireNonNull(newEmail, "New email cannot be null");
        if (Objects.equals(newEmail, currentEmail)) {
            throw new InvalidEmailException("New email must be different from the current email");
        }
        // User must be in ACTIVE status to change email
        if (this.getStatus() != UserStatus.ACTIVE) {
            throw new InvalidEmailException("User must be in ACTIVE status to change email");
        }
        // TODO: Implement rules for email changes based on user status
        // TODO: PENDING users might need to verify email before changing
        // TODO: SUSPENDED users might need additional verification
        // TODO: ACTIVE users can change normally
        this.email = newEmail;

        logger.info("User {} email changed from {} to {}", this.id, currentEmail, newEmail);
    }


    public void changeUserName(FullName currentName, FullName newName) {
        if (this.getStatus() == UserStatus.DELETED) {
            throw new InvalidFullNameException("Deleted user cannot change their names");
        }

        // FullName Nullability check
        Objects.requireNonNull(newName, "New name cannot be null");
        if (Objects.equals(newName, currentName)) {
            throw new InvalidFullNameException("New name must be different from the current name");
        }
        // User must be in ACTIVE status to change name
        if (this.getStatus() != UserStatus.ACTIVE) {
            throw new InvalidFullNameException("User must be in ACTIVE status to change name");
        }
        this.fullName = newName;

        logger.info("User {} name changed from {} to {}", this.id, currentName, newName);
    }
}
