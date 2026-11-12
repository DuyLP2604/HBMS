package controller;

import dao.PasswordResetDAO;
import dto.PasswordResetAccount;
import entity.Users;

import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import service.EmailService;

import util.flash.Flash;
import util.security.OtpGenerator;
import util.security.PasswordResetSession;

/** Starts password recovery by finding an eligible account and sending an OTP. */
@WebServlet(
        name = "ForgotPasswordServlet",
        urlPatterns = {"/forgot-password"}
)
public class ForgotPasswordServlet extends HttpServlet {

    /*
     * OTP is valid for 5 minutes
     */
    private static final long OTP_EXPIRY
            = 5 * 60 * 1000L;


    /*
     * Minimum time between two OTP emails
     */
    private static final long SEND_COOLDOWN
            = 60 * 1000L;


    /*
     * Maximum number of OTP emails
     * during one rate-limit window
     */
    private static final int MAX_OTP_REQUESTS
            = 5;


    /*
     * Rate-limit window: 15 minutes
     */
    private static final long RATE_LIMIT_WINDOW
            = 15 * 60 * 1000L;

    /** Show the password reset request form. */
    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws ServletException, IOException {

        String selected = request.getParameter("accountType");
        if (selected == null) {
            HttpSession session = request.getSession(false);
            Object user = session == null ? null : session.getAttribute("user");
            selected = user instanceof Users ? ((Users) user).getRole() : "Customer";
        }
        request.setAttribute("selectedAccountType", "Staff".equals(selected) ? "Staff" : "Customer");
        response.setHeader("Cache-Control", "no-store");
        request
                .getRequestDispatcher(
                        "/WEB-INF/views/forgot-password.jsp"
                )
                .forward(request, response);
    }

    /** Find the account and email an OTP when the send limits allow it. */
    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");


        // Validate the account details entered by the customer or staff member.
        String identifier
                = request.getParameter("identifier");

        if (identifier == null
                || identifier.trim().isEmpty()) {

            Flash.error(
                    request,
                    "Please enter your email or phone number."
            );

            response.sendRedirect(
                    request.getContextPath()
                    + "/forgot-password"
            );

            return;
        }

        identifier
                = identifier.trim();


        // Find an account that can receive an OTP by email.
        String accountType = request.getParameter("accountType");
        // Preserve the previous customer form contract when accountType is absent.
        if (accountType == null) {
            accountType = "Customer";
        }
        PasswordResetDAO resetDAO = createPasswordResetDAO();
        PasswordResetAccount account = resetDAO.findAccount(identifier, accountType);


        // Use the same response when no eligible account is found.
        if (account == null) {
            PasswordResetSession.clear(request.getSession(false));

            Flash.info(
                    request,
                    "Please check your account type and email or phone number, then try again."
            );

            response.sendRedirect(
                    request.getContextPath()
                    + "/forgot-password"
            );

            return;
        }

        String email = account.getEmail();


        /*
         * =========================
         * GET SESSION
         * =========================
         */
        HttpSession session
                = request.getSession();


        // Lock the session to prevent multiple OTP requests from sending at once.
        synchronized (session) {

            if (!resetDAO.isEligible(account)) {
                PasswordResetSession.clear(session);
                Flash.info(request, "Please check your account type and email or phone number, then try again.");
                response.sendRedirect(request.getContextPath() + "/forgot-password");
                return;
            }

            long now
                    = System.currentTimeMillis();


            // Wait at least 60 seconds between OTP emails.
            Long lastSent
                    = (Long) session.getAttribute(
                            "resetOtpLastSent"
                    );

            if (lastSent != null) {

                long elapsed
                        = now - lastSent;

                if (elapsed < SEND_COOLDOWN) {

                    long remainingSeconds
                            = (SEND_COOLDOWN
                            - elapsed
                            + 999) / 1000;

                    Flash.warning(
                            request,
                            "Please wait "
                            + remainingSeconds
                            + " seconds before requesting another code."
                    );

                    response.sendRedirect(
                            request.getContextPath()
                            + "/forgot-password"
                    );

                    return;
                }
            }


            // Allow up to five OTP emails in 15 minutes.
            Long windowStart
                    = (Long) session.getAttribute(
                            "resetOtpRateWindowStart"
                    );

            Integer requestCount
                    = (Integer) session.getAttribute(
                            "resetOtpRateCount"
                    );


            /*
             * Start a new rate-limit window
             */
            if (windowStart == null
                    || now - windowStart
                    >= RATE_LIMIT_WINDOW) {

                windowStart = now;
                requestCount = 0;

                session.setAttribute(
                        "resetOtpRateWindowStart",
                        windowStart
                );

                session.setAttribute(
                        "resetOtpRateCount",
                        requestCount
                );
            }

            if (requestCount == null) {
                requestCount = 0;
            }


            /*
             * Too many OTP emails
             */
            if (requestCount >= MAX_OTP_REQUESTS) {

                long remainingMilliseconds
                        = RATE_LIMIT_WINDOW
                        - (now - windowStart);

                long remainingMinutes
                        = Math.max(
                                1,
                                (remainingMilliseconds
                                + 59_999) / 60_000
                        );

                Flash.error(
                        request,
                        "Too many verification code requests. "
                        + "Please try again in about "
                        + remainingMinutes
                        + " minute(s)."
                );

                response.sendRedirect(
                        request.getContextPath()
                        + "/forgot-password"
                );

                return;
            }


            // Generate a new OTP.
            String otp
                    = OtpGenerator.generate();

            EmailService emailService
                    = createEmailService();


            // Send the OTP to the email on the account.
            boolean sent
                    = emailService
                            .sendPasswordResetOtp(
                                    email,
                                    otp
                            );


            // If email fails, keep the old OTP and the send count.
            if (!sent) {

                Flash.error(
                        request,
                        "Unable to send the verification code. "
                        + "Please try again later."
                );

                response.sendRedirect(
                        request.getContextPath()
                        + "/forgot-password"
                );

                return;
            }


            // Save the new OTP in the session only after the email is sent.
 /*
             * Account being reset
             */
            PasswordResetSession.bind(session, account);


            /*
             * New OTP replaces old OTP
             */
            session.setAttribute(
                    "resetOtp",
                    otp
            );


            /*
             * OTP expires after 5 minutes
             */
            session.setAttribute(
                    "resetOtpExpiry",
                    System.currentTimeMillis() + OTP_EXPIRY
            );


            /*
             * Used for 60-second cooldown
             */
            session.setAttribute(
                    "resetOtpLastSent",
                    now
            );


            /*
             * Reset incorrect OTP attempts
             */
            session.setAttribute(
                    "resetOtpAttempts",
                    0
            );


            /*
             * Increase global OTP email counter
             */
            session.setAttribute(
                    "resetOtpRateCount",
                    requestCount + 1
            );

            session.setAttribute(
                    "resetOtpRateWindowStart",
                    windowStart
            );


            /*
             * New password-reset flow must
             * not inherit previous verification.
             */
            session.removeAttribute(
                    "resetOtpVerified"
            );

            session.removeAttribute(
                    "resetVerifiedExpiry"
            );


            /*
             * Initial OTP is not a resend.
             */
            session.setAttribute(
                    "resetOtpResendCount",
                    0
            );

            session.setAttribute(
                    "resetOtpResendWindowStart",
                    now
            );
        }


        // Open the OTP verification page.
        response.sendRedirect(
                request.getContextPath()
                + "/verify-otp"
        );
    }

    protected PasswordResetDAO createPasswordResetDAO() {
        return new PasswordResetDAO();
    }

    protected EmailService createEmailService() {
        return new EmailService();
    }

    @Override
    public String getServletInfo() {

        return "Forgot Password Servlet";
    }
}
