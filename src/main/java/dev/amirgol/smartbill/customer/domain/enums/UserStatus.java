package dev.amirgol.smartbill.customer.domain.enums;


/**
 * Represents the status of a user in the system.
 * <ul>
 *  <li><b>PENDING:</b> Likely for a new user who hasn't completed the registration process</li>
 *  <li><b>ACTIVE:</b> A normal user who can use the system</li>
 *  <li><b>SUSPENDED:</b> A user whose access has been temporarily restricted</li>
 *  <li><b>DELETED:</b> A user who has been removed from the system</li>
 * </ul>
 */
public enum UserStatus {
    PENDING,
    ACTIVE,
    SUSPENDED,
    DELETED
}
