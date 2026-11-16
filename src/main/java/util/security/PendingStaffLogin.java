package util.security;

import entity.Users;
import java.io.Serializable;

/** Verified staff credentials awaiting first-login password setup. */
public final class PendingStaffLogin implements Serializable {
    private static final long serialVersionUID = 1L;
    private final int userId;
    private final int accessVersion;
    private final String username;
    private final String passwordHash;
    private final long expiresAt;

    public PendingStaffLogin(Users user, long expiresAt) {
        this.userId = user.getUserID();
        this.accessVersion = user.getAccessVersion();
        this.username = user.getUsername();
        this.passwordHash = user.getPassword();
        this.expiresAt = expiresAt;
    }

    public int getUserId() { return userId; }
    public int getAccessVersion() { return accessVersion; }
    public String getUsername() { return username; }
    public String getPasswordHash() { return passwordHash; }
    public long getExpiresAt() { return expiresAt; }
}
