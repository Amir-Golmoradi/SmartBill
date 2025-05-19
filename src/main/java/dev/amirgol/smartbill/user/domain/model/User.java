package dev.amirgol.smartbill.user.domain.model;

import dev.amirgol.smartbill.user.domain.enums.Gender;
import dev.amirgol.smartbill.user.domain.enums.Role;
import dev.amirgol.smartbill.user.domain.enums.UserStatus;
import dev.amirgol.smartbill.user.domain.exception.*;
import dev.amirgol.smartbill.user.domain.value_object.Email;
import dev.amirgol.smartbill.user.domain.value_object.FullName;
import dev.amirgol.smartbill.user.domain.value_object.Id;
import dev.amirgol.smartbill.user.domain.value_object.Password;
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
    private static final Map<Gender, Set<Gender>> VALID_GENDER_TRANSITIONS = Map.of(
            Gender.MALE, EnumSet.of(Gender.FEMALE),
            Gender.FEMALE, EnumSet.of(Gender.MALE)
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

    /**
     * Changes the user's password.
     * Throws InvalidPasswordException if the new password is the same as the current one.
     *
     * @param password the new password to set
     */
    public void changePassword(String password) {
        // Retrieve the current password value for comparison
        var currentPassword = this.password.value();

        // Prevent reuse of the same password
        if (Objects.equals(password, currentPassword)) {
            throw new InvalidPasswordException("The new password cannot be the same as the current password");
        }

        // Set the new password (assumes Password value object validates itself)
        this.password = new Password(password);

        // Log the password change event for auditing
        logger.info("User {} password changed from {} to {}", this.fullName, currentPassword, password);
    }

    /**
     * Changes the user's email address.
     * Ensures only ACTIVE users (not DELETED, PENDING, or SUSPENDED) can change email.
     * Throws InvalidEmailException for invalid state or if new email is the same as the current one.
     *
     * @param currentEmail the user's current email (for validation)
     * @param newEmail the new email to set
     */
    public void changeUserEmail(Email currentEmail, Email newEmail) {
        // Prevent email change for deleted users
        if (this.getStatus() == UserStatus.DELETED) {
            throw new InvalidEmailException("Deleted user cannot change email");
        }

        // Ensure the new email is not null
        Objects.requireNonNull(newEmail, "New email cannot be null");

        // Prevent setting the same email
        if (Objects.equals(newEmail, currentEmail)) {
            throw new InvalidEmailException("New email must be different from the current email");
        }

        // Only ACTIVE users can change email
        if (this.getStatus() != UserStatus.ACTIVE) {
            throw new InvalidEmailException("User must be in ACTIVE status to change email");
        }

        // TODO: Implement more nuanced rules for PENDING and SUSPENDED users if needed

        // Set the new email
        this.email = newEmail;

        // Log the email change event for auditing
        logger.info("User {} email changed from {} to {}", this.fullName, currentEmail, newEmail);
    }

    /**
     * Changes the user's full name.
     * Only ACTIVE users can change their name; DELETED users are forbidden.
     * Throws InvalidFullNameException for invalid state or if new name is the same as the current one.
     *
     * @param currentName the user's current full name (for validation)
     * @param newName     the new full name to set
     */
    public void changeUserName(FullName currentName, FullName newName) {
        // Prevent name change for deleted users
        if (this.getStatus() == UserStatus.DELETED) {
            throw new InvalidFullNameException("Deleted user cannot change their names");
        }

        // Ensure the new name is not null
        Objects.requireNonNull(newName, "New name cannot be null");

        // Prevent setting the same name
        if (Objects.equals(newName, currentName)) {
            throw new InvalidFullNameException("New name must be different from the current name");
        }

        // Only ACTIVE users can change their name
        if (this.getStatus() != UserStatus.ACTIVE) {
            throw new InvalidFullNameException("User must be in ACTIVE status to change name");
        }

        // Set the new full name
        this.fullName = newName;

        // Log the name change event for auditing
        logger.info("User {} name changed from {} to {}", this.fullName, currentName, newName);
    }

    /**
     * Marks the user as deleted.
     * Throws InvalidStatusException if the user is already deleted.
     */
    public void markAsDeleted() {
        // Prevent redundant deletion
        if (this.getStatus() == UserStatus.DELETED) {
            throw new InvalidStatusException("User is already deleted");
        }

        // Change the user's status to DELETED using the status transition method
        this.changeUserStatus(this.getStatus(), UserStatus.DELETED);
    }

    /**
     * Changes the user's gender.
     * Only allows transitions defined in VALID_GENDER_TRANSITIONS.
     * Throws InvalidGenderException for invalid transitions or null gender.
     *
     * @param currentGender the user's current gender
     * @param newGender     the new gender to set
     */
    public void changeUserGender(Gender currentGender, Gender newGender) {
        // Ensure the new gender is not null
        if (newGender == null) {
            throw new InvalidGenderException("User must have valid Gender type.");
        }

        // If the gender hasn't changed, do nothing
        if (newGender == currentGender) {
            return;
        }

        // Retrieve allowed gender transitions for the current gender
        var allowedGender = VALID_GENDER_TRANSITIONS.get(currentGender);

        // If the new gender is not allowed, throw exception
        if (!allowedGender.contains(newGender)) {
            throw new InvalidGenderException(
                    String.format(
                            "Cannot change user gender from '%s' to '%s'. Allowed transitions: %s",
                            currentGender,
                            newGender,
                            allowedGender.isEmpty() ? "none" : allowedGender)
            );
        }

        // Set the new gender
        this.gender = newGender;

        // Log the gender change event for auditing
        logger.info("User {} gender changed from {} to {}", this.fullName, currentGender, newGender);
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
        logger.info("User {} status changed from {} to {}", this.fullName, currentStatus, newStatus);
    }
}
