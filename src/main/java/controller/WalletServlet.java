package controller;

import dao.WalletDAO;
import dto.WalletSummary;
import dto.WalletTransactionItem;
import entity.Users;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@WebServlet(name = "WalletServlet", urlPatterns = {"/wallet"})
public class WalletServlet extends HttpServlet
{
    private static final int PAGE_SIZE = 20;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        response.setHeader("Cache-Control", "no-store");
        HttpSession session = request.getSession(false);
        Object value = session == null ? null : session.getAttribute("user");
        if (!(value instanceof Users))
        {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }
        Users user = (Users) value;
        if (!"Customer".equalsIgnoreCase(user.getRole()))
        {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Only customers can access their wallet.");
            return;
        }
        if (user.getUserID() == null || user.getUserID() <= 0)
        {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "The login session is invalid.");
            return;
        }
        int page;
        try
        {
            String raw = request.getParameter("page");
            page = raw == null || raw.isBlank() ? 1 : Integer.parseInt(raw);
            if (page < 1 || page > 100000)
            {
                throw new NumberFormatException();
            }
        }
        catch (NumberFormatException exception)
        {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid wallet history page.");
            return;
        }
        try
        {
            WalletDAO dao = new WalletDAO();
            WalletSummary wallet = dao.getWalletByUserID(user.getUserID());
            List<WalletTransactionItem> items = wallet == null ? new ArrayList<>() : dao.getTransactionsByUserID(user.getUserID(), page, PAGE_SIZE);
            boolean hasNext = page < 100000 && items.size() > PAGE_SIZE;
            if (items.size() > PAGE_SIZE)
            {
                items = new ArrayList<>(items.subList(0, PAGE_SIZE));
            }
            request.setAttribute("wallet", wallet);
            request.setAttribute("walletAvailable", wallet != null);
            request.setAttribute("walletTransactions", items);
            request.setAttribute("walletPage", page);
            request.setAttribute("walletHasNext", hasNext);
            request.getRequestDispatcher("/WEB-INF/views/wallet.jsp").forward(request, response);
        }
        catch (IllegalStateException exception)
        {
            getServletContext().log("Unable to load the customer wallet.", exception);
            response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE, "Your wallet is temporarily unavailable. Please try again later.");
        }
    }

}
