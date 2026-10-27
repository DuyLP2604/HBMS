package controller;

import dao.PasswordResetDAO;
import dao.PasswordResetDAO.Account;
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

    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");


        /*
         * =========================
         * VALIDATE INPUT
         * =========================
         */
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


        /*
         * =========================
         * FIND AN ELIGIBLE ACCOUNT
         * =========================
         */
        String accountType = request.getParameter("accountType");
        // Preserve the previous customer form contract when accountType is absent.
        if (accountType == null) {
            accountType = "Customer";
        }
        PasswordResetDAO resetDAO = createPasswordResetDAO();
        Account account = resetDAO.findAccount(identifier, accountType);


        /*
         * Do not reveal whether
         * an account exists.
         */
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


        /*
         * Synchronizing on this user's session prevents
         * multiple simultaneous POST requests from
         * sending several OTP emails at once.
         */
        synchronized (session) {

            if (!resetDAO.isEligible(account)) {
                PasswordResetSession.clear(session);
                Flash.info(request, "Please check your account type and email or phone number, then try again.");
                response.sendRedirect(request.getContextPath() + "/forgot-password");
                return;
            }

            long now
                    = System.currentTimeMillis();


            /*
             * =========================
             * 60 SECOND COOLDOWN
             * =========================
             */
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


            /*
             * =========================
             * RATE LIMIT
             * 5 OTP EMAILS / 15 MINUTES
             * =========================
             */
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


            /*
             * =========================
             * GENERATE OTP
             * =========================
             */
            String otp
                    = OtpGenerator.generate();

            EmailService emailService
                    = createEmailService();


            /*
             * =========================
             * SEND EMAIL
             * =========================
             */
            boolean sent
                    = emailService
                            .sendPasswordResetOtp(
                                    email,
                                    otp
                            );


            /*
             * Email failed:
             * do not consume rate limit
             * and do not replace old OTP.
             */
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


            /*
             * =========================
             * EMAIL SENT SUCCESSFULLY
             * =========================
             */
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


        /*
         * =========================
         * GO TO OTP PAGE
         * =========================
         */
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
