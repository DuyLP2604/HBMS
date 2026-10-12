package controller;

import dao.WalletDAO;
import dto.WalletSummary;
import entity.Users;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

@WebServlet(name = "WalletNavbarServlet", urlPatterns = {"/wallet-navbar"})
public class WalletNavbarServlet extends HttpServlet
{
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        HttpSession session = request.getSession(false);
        Object value = session == null ? null : session.getAttribute("user");
        if (!(value instanceof Users))
        {
            return;
        }
        Users user = (Users) value;
        if (!"Customer".equals(user.getRole()) || user.getUserID() == null || user.getUserID() <= 0)
        {
            return;
        }
        response.setHeader("Cache-Control", "no-store");
        request.setAttribute("navbarWalletAvailable", false);
        request.removeAttribute("navbarWalletBalance");
        try
        {
            WalletSummary wallet = new WalletDAO().getWalletByUserID(user.getUserID());
            if (wallet != null)
            {
                request.setAttribute("navbarWalletBalance", wallet.getBalance());
                request.setAttribute("navbarWalletAvailable", true);
            }
        }
        catch (IllegalStateException exception)
        {
            getServletContext().log("Unable to load the navbar wallet balance.", exception);
        }
        request.getRequestDispatcher("/WEB-INF/components/wallet-navbar.jsp").include(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        doGet(request, response);
    }
}
