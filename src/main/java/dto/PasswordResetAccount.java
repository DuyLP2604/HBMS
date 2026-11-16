package dto;

/** Account details used to verify an OTP password reset. */
public final class PasswordResetAccount {
    private final int userId;
    private final String role;
    private final String email;
    private final String passwordHash;
    private final int accessVersion;

    public PasswordResetAccount(int userId, String role, String email, String passwordHash, int accessVersion) {
        this.userId = userId;
        this.role = role;
        this.email = email;
        this.passwordHash = passwordHash;
        this.accessVersion = accessVersion;
    }

    public int getUserId() { return userId; }
    public String getRole() { return role; }
    public String getEmail() { return email; }
    public String getPasswordHash() { return passwordHash; }
    public int getAccessVersion() { return accessVersion; }
}
