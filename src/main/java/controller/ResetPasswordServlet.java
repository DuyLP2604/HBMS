package controller;

import dao.UserDAO;
import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import util.flash.Flash;

@WebServlet(
        name = "ResetPasswordServlet",
        urlPatterns = {"/reset-password"}
)
public class ResetPasswordServlet extends HttpServlet {


    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws ServletException, IOException {

        HttpSession session =
                request.getSession(false);

        if (!isResetAllowed(session)) {

            response.sendRedirect(
                    request.getContextPath()
                    + "/forgot-password"
            );

            return;
        }


        request
                .getRequestDispatcher(
                        "/WEB-INF/views/reset-password.jsp"
                )
                .forward(request, response);
    }


    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        HttpSession session =
                request.getSession(false);


        if (!isResetAllowed(session)) {

            response.sendRedirect(
                    request.getContextPath()
                    + "/forgot-password"
            );

            return;
        }


        String password =
                request.getParameter("password");

        String confirmPassword =
                request.getParameter(
                        "confirmPassword"
                );


        if (password == null
                || password.length() < 6) {

            Flash.error(
                    request,
                    "Password must contain at least 6 characters."
            );

            response.sendRedirect(
                    request.getContextPath()
                    + "/reset-password"
            );

            return;
        }


        if (!password.equals(confirmPassword)) {

            Flash.error(
                    request,
                    "Passwords do not match."
            );

            response.sendRedirect(
                    request.getContextPath()
                    + "/reset-password"
            );

            return;
        }


        Integer userId =
                (Integer) session.getAttribute(
                        "resetUserId"
                );


        UserDAO userDAO =
                new UserDAO();


        boolean updated =
                userDAO.updatePassword(
                        userId,
                        password
                );


        if (!updated) {

            Flash.error(
                    request,
                    "Unable to reset the password."
            );

            response.sendRedirect(
                    request.getContextPath()
                    + "/reset-password"
            );

            return;
        }


        clearResetSession(session);


        Flash.success(
                request,
                "Password reset successfully. Please log in."
        );


        response.sendRedirect(
                request.getContextPath()
                + "/login"
        );
    }


    private boolean isResetAllowed(
            HttpSession session
    ) {

        if (session == null) {
            return false;
        }


        Boolean verified =
                (Boolean) session.getAttribute(
                        "resetOtpVerified"
                );

        Integer userId =
                (Integer) session.getAttribute(
                        "resetUserId"
                );

        Long expiry =
                (Long) session.getAttribute(
                        "resetVerifiedExpiry"
                );


        return Boolean.TRUE.equals(verified)
                && userId != null
                && expiry != null
                && System.currentTimeMillis() <= expiry;
    }


    private void clearResetSession(
            HttpSession session
    ) {

        session.removeAttribute("resetOtp");
        session.removeAttribute("resetOtpExpiry");
        session.removeAttribute("resetOtpAttempts");
        session.removeAttribute("resetOtpLastSent");

        session.removeAttribute("resetOtpVerified");
        session.removeAttribute("resetVerifiedExpiry");

        session.removeAttribute("resetUserId");
        session.removeAttribute("resetEmail");
    }
    
    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>
}