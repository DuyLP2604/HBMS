package service;

import jakarta.mail.Authenticator;
import jakarta.mail.PasswordAuthentication;

/** Supplies the configured SMTP credentials when Jakarta Mail requests them. */
final class SmtpAuthenticator extends Authenticator {
    private final String senderEmail;
    private final String appPassword;

    SmtpAuthenticator(String senderEmail, String appPassword) {
        this.senderEmail = senderEmail;
        this.appPassword = appPassword;
    }

    @Override
    protected PasswordAuthentication getPasswordAuthentication() {
        return new PasswordAuthentication(senderEmail, appPassword);
    }
}
