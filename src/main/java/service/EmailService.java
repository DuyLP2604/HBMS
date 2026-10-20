package service;

import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import java.util.Properties;
import java.util.logging.Logger;

public class EmailService {
    private static final Logger LOGGER = Logger.getLogger(EmailService.class.getName());
    private final String senderEmail;
    private final String appPassword;
    
    //simplebearhotelhcm@gmail.com
    //kogw whjq imqx bzuw
    public EmailService() {
//        this.senderEmail = setting("hbms.mail.username", "HBMS_MAIL_USERNAME");
//        this.appPassword = setting("hbms.mail.appPassword", "HBMS_MAIL_APP_PASSWORD");
          this.senderEmail = "simplebearhotelhcm@gmail.com";
          this.appPassword = "kogwwhjqimqxbzuw";
    }

    private static String setting(String property, String environment) {
        String value = System.getProperty(property);
        if (value == null || value.isBlank()) {
            value = System.getenv(environment);
        }
        return value == null ? "" : value.trim();
    }

    public boolean isConfigured() {
        if (senderEmail.isBlank() || appPassword.isBlank()) {
            return false;
        }
        try {
            InternetAddress address = new InternetAddress(senderEmail, true);
            address.validate();
            return address.getAddress().contains("@");
        } catch (Exception exception) {
            return false;
        }
    }

    public boolean sendPasswordResetOtp(String recipient, String otp) {
        return sendText(recipient, "Simple Bear Hotel - Password Reset Code", "Hello,\n\nYour password reset verification code is:\n\n" + otp + "\n\nThis code will expire in 5 minutes.\n\nIf you did not request a password reset, please ignore this email.\n\nSimple Bear Hotel");
    }

    public boolean sendStaffCredentials(String recipient, String fullName, String username, String temporaryPassword) {
        String body = "Hello " + fullName + ",\n\nYour Simple Bear Hotel staff account is ready.\n\nUsername: " + username + "\nTemporary password: " + temporaryPassword + "\n\nLog in using these credentials. You must set a new password before accessing the staff dashboard.\n\nIf you receive another login email, use the credentials in the latest email.\nKeep these credentials private.\n\nSimple Bear Hotel";
        return sendText(recipient, "Simple Bear Hotel - Your Staff Account", body);
    }

    protected boolean sendText(String recipient, String subject, String body) {
        if (!isConfigured()) {
            LOGGER.warning("Email is not configured. Set HBMS_MAIL_USERNAME and HBMS_MAIL_APP_PASSWORD.");
            return false;
        }
        Properties properties = new Properties();
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.smtp.starttls.required", "true");
        properties.put("mail.smtp.ssl.checkserveridentity", "true");
        properties.put("mail.smtp.host", "smtp.gmail.com");
        properties.put("mail.smtp.port", "587");
        properties.put("mail.smtp.connectiontimeout", "10000");
        properties.put("mail.smtp.timeout", "10000");
        properties.put("mail.smtp.writetimeout", "10000");
        Session mailSession = Session.getInstance(properties, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(senderEmail, appPassword);
            }
        });
        try {
            InternetAddress address = new InternetAddress(recipient, true);
            address.validate();
            MimeMessage message = new MimeMessage(mailSession);
            message.setFrom(new InternetAddress(senderEmail, "Simple Bear Hotel", "UTF-8"));
            message.setRecipient(Message.RecipientType.TO, address);
            message.setSubject(subject, "UTF-8");
            message.setText(body, "UTF-8");
            Transport.send(message);
            return true;
        } catch (Exception exception) {
            // Do not log SMTP protocol content or credentials.
            LOGGER.warning("Unable to send email. Check sender settings and SMTP connectivity.");
            return false;
        }
    }
}
