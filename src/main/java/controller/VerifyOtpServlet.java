package controller;

import dao.PasswordResetDAO;

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


/** Handles OTP verification and resend requests for password recovery. */
@WebServlet(
        name = "VerifyOtpServlet",
        urlPatterns = {"/verify-otp"}
)
public class VerifyOtpServlet extends HttpServlet {

    // Maximum number of incorrect OTP attempts
    private static final int MAX_ATTEMPTS = 5;

    // OTP lifetime
    private static final long OTP_EXPIRY =
            5 * 60 * 1000L;

    // Reset-password permission after successful OTP verification
    private static final long RESET_EXPIRY =
            10 * 60 * 1000L;

    // Minimum delay between OTP emails
    private static final long SEND_COOLDOWN =
            60 * 1000L;

    // Maximum OTP emails in one rate-limit window
    private static final int MAX_OTP_REQUESTS = 5;

    // Rate-limit window
    private static final long RATE_LIMIT_WINDOW =
            15 * 60 * 1000L;


    /** Show the OTP form and the wait time before another email can be sent. */
    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws ServletException, IOException {

        HttpSession session =
                request.getSession(false);


        if (!PasswordResetSession.isEligible(session, createPasswordResetDAO())) {
            PasswordResetSession.clear(session);

            response.sendRedirect(
                    request.getContextPath()
                    + "/forgot-password"
            );

            return;
        }


        /*
         * User already verified OTP.
         * They should continue to reset password,
         * not return to the verification page.
         */
        if (Boolean.TRUE.equals(
                session.getAttribute("resetOtpVerified")
        )) {

            response.sendRedirect(
                    request.getContextPath()
                    + "/reset-password"
            );

            return;
        }


        if (session.getAttribute("resetOtp") == null
                || session.getAttribute("resetUserId") == null
                || session.getAttribute("resetEmail") == null) {

            response.sendRedirect(
                    request.getContextPath()
                    + "/forgot-password"
            );

            return;
        }


        /*
         * Remaining resend cooldown
         * for UI countdown only.
         */
        long remainingSeconds = 0;


        Long lastSent =
                (Long) session.getAttribute(
                        "resetOtpLastSent"
                );


        if (lastSent != null) {

            long elapsed =
                    System.currentTimeMillis()
                    - lastSent;


            if (elapsed < SEND_COOLDOWN) {

                remainingSeconds =
                        (SEND_COOLDOWN
                        - elapsed
                        + 999) / 1000;
            }
        }


        request.setAttribute(
                "resendRemainingSeconds",
                remainingSeconds
        );


        request
                .getRequestDispatcher(
                        "/WEB-INF/views/verify-otp.jsp"
                )
                .forward(request, response);
    }


    /** Route the request to OTP verification or resend. */
    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws ServletException, IOException {

        String action =
                request.getParameter("action");


        if ("verify".equals(action)) {

            verifyOtp(
                    request,
                    response
            );

            return;
        }


        if ("resend".equals(action)) {

            resendOtp(
                    request,
                    response
            );

            return;
        }


        response.sendError(
                HttpServletResponse.SC_BAD_REQUEST
        );
    }


    /** Check the submitted code and allow a password reset if it is valid. */
    private void verifyOtp(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException {

        HttpSession session =
                request.getSession(false);


        if (session == null) {

            response.sendRedirect(
                    request.getContextPath()
                    + "/forgot-password"
            );

            return;
        }


        // Lock the session so concurrent requests cannot skip the attempt count.
        synchronized (session) {

            if (!requireEligibleAccount(request, response, session)) {
                return;
            }

            /*
             * OTP has already been verified.
             */
            if (Boolean.TRUE.equals(
                    session.getAttribute(
                            "resetOtpVerified"
                    )
            )) {

                response.sendRedirect(
                        request.getContextPath()
                        + "/reset-password"
                );

                return;
            }


            String savedOtp =
                    (String) session.getAttribute(
                            "resetOtp"
                    );


            Long expiry =
                    (Long) session.getAttribute(
                            "resetOtpExpiry"
                    );


            Integer attempts =
                    (Integer) session.getAttribute(
                            "resetOtpAttempts"
                    );


            if (savedOtp == null
                    || expiry == null) {

                response.sendRedirect(
                        request.getContextPath()
                        + "/forgot-password"
                );

                return;
            }


            // Reject an expired OTP.
            if (System.currentTimeMillis() > expiry) {

                clearResetFlow(session);


                Flash.error(
                        request,
                        "The verification code has expired. "
                        + "Please request a new code."
                );


                response.sendRedirect(
                        request.getContextPath()
                        + "/forgot-password"
                );

                return;
            }


            if (attempts == null) {
                attempts = 0;
            }


            String enteredOtp =
                    request.getParameter("otp");


            boolean validFormat =
                    enteredOtp != null
                    && enteredOtp.trim()
                            .matches("\\d{6}");


            boolean correctOtp =
                    validFormat
                    && savedOtp.equals(
                            enteredOtp.trim()
                    );


            // Count failed attempts and end this reset flow when the limit is reached.
            if (!correctOtp) {

                attempts++;


                if (attempts >= MAX_ATTEMPTS) {

                    /*
                     * Clear reset flow,
                     * but KEEP spam protection data.
                     */
                    clearResetFlow(session);


                    Flash.error(
                            request,
                            "Too many incorrect attempts. "
                            + "Please request a new verification code."
                    );


                    response.sendRedirect(
                            request.getContextPath()
                            + "/forgot-password"
                    );

                    return;
                }


                session.setAttribute(
                        "resetOtpAttempts",
                        attempts
                );


                int remaining =
                        MAX_ATTEMPTS - attempts;


                Flash.error(
                        request,
                        "Invalid verification code. "
                        + remaining
                        + " attempt(s) remaining."
                );


                response.sendRedirect(
                        request.getContextPath()
                        + "/verify-otp"
                );

                return;
            }


            // A valid OTP allows a password reset for the next 10 minutes.

            session.setAttribute(
                    "resetOtpVerified",
                    true
            );


            session.setAttribute(
                    "resetVerifiedExpiry",
                    System.currentTimeMillis()
                    + RESET_EXPIRY
            );


            // Remove the OTP after successful verification so it cannot be reused.
            session.removeAttribute("resetOtp");
            session.removeAttribute("resetOtpExpiry");
            session.removeAttribute("resetOtpAttempts");
        }


        response.sendRedirect(
                request.getContextPath()
                + "/reset-password"
        );
    }


    /** Send a replacement OTP after checking the account and send limits. */
    private void resendOtp(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException {

        HttpSession session =
                request.getSession(false);


        if (session == null) {

            response.sendRedirect(
                    request.getContextPath()
                    + "/forgot-password"
            );

            return;
        }


        // Lock the session so resend requests run one at a time.
        synchronized (session) {

            if (!requireEligibleAccount(request, response, session)) {
                return;
            }

            /*
             * Do not allow resend after
             * successful OTP verification.
             */
            if (Boolean.TRUE.equals(
                    session.getAttribute(
                            "resetOtpVerified"
                    )
            )) {

                response.sendRedirect(
                        request.getContextPath()
                        + "/reset-password"
                );

                return;
            }


            String email =
                    (String) session.getAttribute(
                            "resetEmail"
                    );


            Integer userId =
                    (Integer) session.getAttribute(
                            "resetUserId"
                    );


            String currentOtp =
                    (String) session.getAttribute(
                            "resetOtp"
                    );


            if (email == null
                    || userId == null
                    || currentOtp == null) {

                clearResetFlow(session);


                response.sendRedirect(
                        request.getContextPath()
                        + "/forgot-password"
                );

                return;
            }


            long now =
                    System.currentTimeMillis();


            // Wait at least 60 seconds between OTP emails.

            Long lastSent =
                    (Long) session.getAttribute(
                            "resetOtpLastSent"
                    );


            if (lastSent != null) {

                long elapsed =
                        now - lastSent;


                if (elapsed < SEND_COOLDOWN) {

                    long remainingSeconds =
                            (SEND_COOLDOWN
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
                            + "/verify-otp"
                    );

                    return;
                }
            }


            // Allow up to five OTP emails in 15 minutes, including resends.

            Long windowStart =
                    (Long) session.getAttribute(
                            "resetOtpRateWindowStart"
                    );


            Integer requestCount =
                    (Integer) session.getAttribute(
                            "resetOtpRateCount"
                    );


            /*
             * New rate-limit window
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

                long remainingMilliseconds =
                        RATE_LIMIT_WINDOW
                        - (now - windowStart);


                long remainingMinutes =
                        Math.max(
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
                        + "/verify-otp"
                );

                return;
            }


            // Generate and send a replacement OTP.

            String newOtp =
                    OtpGenerator.generate();


            EmailService emailService =
                    createEmailService();


            boolean sent =
                    emailService
                            .sendPasswordResetOtp(
                                    email,
                                    newOtp
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
                        + "/verify-otp"
                );

                return;
            }


            /*
             * Use the actual successful send time.
             */
            long sentAt =
                    System.currentTimeMillis();


            /*
             * New OTP invalidates the previous OTP.
             */
            session.setAttribute(
                    "resetOtp",
                    newOtp
            );


            session.setAttribute(
                    "resetOtpExpiry",
                    sentAt + OTP_EXPIRY
            );


            session.setAttribute(
                    "resetOtpLastSent",
                    sentAt
            );


            /*
             * New OTP receives a fresh
             * verification-attempt counter.
             */
            session.setAttribute(
                    "resetOtpAttempts",
                    0
            );


            /*
             * Count successful OTP email.
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
             * Defensive cleanup.
             */
            session.removeAttribute(
                    "resetOtpVerified"
            );

            session.removeAttribute(
                    "resetVerifiedExpiry"
            );
        }


        Flash.success(
                request,
                "A new verification code has been sent to your email."
        );


        response.sendRedirect(
                request.getContextPath()
                + "/verify-otp"
        );
    }


    /** Clear the reset session data while keeping the email send limits. */
    private void clearResetFlow(
            HttpSession session
    ) {

        PasswordResetSession.clear(session);


        /*
         * DO NOT REMOVE:
         *
         * resetOtpLastSent
         * resetOtpRateCount
         * resetOtpRateWindowStart
         */
    }


    /** Check that the account still qualifies for password reset. */
    private boolean requireEligibleAccount(HttpServletRequest request,
            HttpServletResponse response, HttpSession session) throws IOException {
        if (PasswordResetSession.isEligible(session, createPasswordResetDAO())) {
            return true;
        }
        clearResetFlow(session);
        Flash.error(request, "This password reset request is no longer valid. Please start again.");
        response.sendRedirect(request.getContextPath() + "/forgot-password");
        return false;
    }

    protected PasswordResetDAO createPasswordResetDAO() {
        return new PasswordResetDAO();
    }

    protected EmailService createEmailService() {
        return new EmailService();
    }

    @Override
    public String getServletInfo() {

        return "Verify OTP Servlet";
    }
}
