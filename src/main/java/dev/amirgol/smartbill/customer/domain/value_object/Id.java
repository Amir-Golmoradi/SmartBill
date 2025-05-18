package dev.amirgol.smartbill.customer.domain.value_object;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

/**
 * A value object representing a unique identifier in the system.
 * This class is implemented as a record, providing immutable storage of a Long value.
 */
public record Id(Long value) {
    /**
     * Thread-safe counter used for generating sequential IDs.
     * AtomicLong ensures that ID generation is thread-safe in concurrent environments.
     * Initialized to 0, each call to incrementAndGet() will return the next sequential number.
     */
    private static final AtomicLong SEQUENTIAL_ID = new AtomicLong(0);

    /**
     * Compact constructor for Id record.
     * Validates that the provided value is not null before object creation.
     *
     * @throws NullPointerException if value is null
     */
    public Id {
        Objects.requireNonNull(value, "Id value cannot be null");
    }

    /**
     * Generates a new unique Id using an atomic counter.
     * Thread-safe method that increments and returns a new sequential ID.
     *
     * @return a new Id instance with the next sequential value
     */
    public static Id generate() {
        return new Id(SEQUENTIAL_ID.incrementAndGet());
    }

    /**
     * Creates an Id instance from an existing Long value.
     *
     * @param value the Long value to create an Id from
     * @return a new Id instance containing the provided value
     * @throws NullPointerException if value is null
     */
    public static Id of(Long value) {
        return new Id(value);
    }

    /**
     * Creates an Id instance from a String representation of a number.
     *
     * @param value the String value to be converted to a Long
     * @return a new Id instance containing the parsed Long value
     * @throws NumberFormatException if the string cannot be converted to a Long
     * @throws NullPointerException  if value is null
     */
    public static Id of(String value) {
        return new Id(Long.valueOf(value));
    }

    /**
     * Returns the string representation of the Id's value.
     *
     * @return the Id's value as a String
     */
    @Override
    public String toString() {
        return String.valueOf(value);
    }
}