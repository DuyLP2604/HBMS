package service;

import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import java.util.Properties;

public class EmailService {

    private final String senderEmail;
    private final String appPassword;

    public EmailService() {
        this.senderEmail = "simplebearhotel.hbms@gmail.com";

        this.appPassword = "ohyfitcfrzqcdlak";
                
    }

    public boolean sendPasswordResetOtp(
            String recipient,
            String otp
    ) {

        if (senderEmail == null
                || appPassword == null) {

            System.out.println(
                    "Email environment variables are not configured."
            );

            return false;
        }


        Properties properties =
                new Properties();

        properties.put(
                "mail.smtp.auth",
                "true"
        );

        properties.put(
                "mail.smtp.starttls.enable",
                "true"
        );

        properties.put(
                "mail.smtp.host",
                "smtp.gmail.com"
        );

        properties.put(
                "mail.smtp.port",
                "587"
        );


        Session mailSession =
                Session.getInstance(
                        properties,
                        new Authenticator() {

                            @Override
                            protected PasswordAuthentication
                                    getPasswordAuthentication() {

                                return new PasswordAuthentication(
                                        senderEmail,
                                        appPassword
                                );
                            }
                        }
                );


        try {

            Message message =
                    new MimeMessage(mailSession);

            message.setFrom(
                    new InternetAddress(
                            senderEmail,
                            "Simple Bear Hotel"
                    )
            );

            message.setRecipients(
                    Message.RecipientType.TO,
                    InternetAddress.parse(recipient)
            );

            message.setSubject(
                    "Simple Bear Hotel - Password Reset Code"
            );

            message.setText(
                    "Hello,\n\n"
                    + "Your password reset verification code is:\n\n"
                    + otp
                    + "\n\n"
                    + "This code will expire in 5 minutes.\n\n"
                    + "If you did not request a password reset, "
                    + "please ignore this email.\n\n"
                    + "Simple Bear Hotel"
            );


            Transport.send(message);

            return true;

        } catch (Exception e) {

            e.printStackTrace();

            return false;
        }
    }
}