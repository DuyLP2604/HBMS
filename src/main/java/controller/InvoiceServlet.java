import dao.BookingDAO;
import dao.InvoiceDAO;
import dto.InvoiceDTO;
import entity.Users;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;




@WebServlet(name = "InvoiceServlet", urlPatterns = {"/invoice"})
public class InvoiceServlet extends HttpServlet
{
    private final InvoiceDAO invoiceDAO = new InvoiceDAO();
    private final BookingDAO bookingDAO = new BookingDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        if (requireStaff(request, response) == null)
        {
            return;
        }

        String action = request.getParameter("action");
        boolean print = "print".equals(action);

        if (action != null && !action.isBlank() && !"list".equals(action) && !print)
        {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Unsupported invoice action.");
            return;
        }

        String bookingID = request.getParameter("bookingId");
        if (bookingID == null || bookingID.isBlank())
        {
            bookingID = request.getParameter("bookingID");
        }
        if (bookingID != null)
        {
            bookingID = bookingID.trim();
        }

        if (bookingID != null && bookingID.length() > 6)
        {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "A valid booking ID is required.");
            return;
        }

        // Nếu bấm "Select" (có bookingID) -> Tính toán và ném vào InvoiceDTO
        if (bookingID != null && !bookingID.isEmpty())
        {
            try
            {
                Map<String, Object> summary = bookingDAO.getPaymentSummary(bookingID);
                if (summary != null && !summary.isEmpty())
                {
                    Map<String, String> roomDetail = invoiceDAO.getRoomDetailByBooking(bookingID);
                    List<Map<String, String>> services = invoiceDAO.getServicesByBooking(bookingID);
                    
                    if (roomDetail != null && !roomDetail.isEmpty()) {
                        InvoiceDTO invoiceDTO = new InvoiceDTO(roomDetail, services, summary);
                        request.setAttribute("invoiceDTO", invoiceDTO);
                    }
                }
            }
            catch (Exception ex)
            {
                throw new ServletException("Unable to load the booking details.", ex);
            }
        }

        // Tải danh sách 2 bảng
        try
        {
            request.setAttribute("occupiedRooms", invoiceDAO.getOccupiedRooms());
            request.setAttribute("paidInvoices", invoiceDAO.getPaidInvoices());
        }
        catch (Exception ex)
        {
            throw new ServletException("Unable to load the invoice list.", ex);
        }
        
        // Điều hướng: Nếu không phải action in ấn, luôn hiển thị checkout.jsp (Dashboard)
        request.getRequestDispatcher(print ? "/WEB-INF/views/invoice.jsp" : "/WEB-INF/views/checkout.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        Users user = requireStaff(request, response);
        if (user == null)
        {
            return;
        }

        String action = request.getParameter("action");
        if ("print".equals(action))
        {
            String bookingID = request.getParameter("bookingID");
            try 
            {
                // Thực thi lưu CSDL khi bấm "Print Invoice"
                invoiceDAO.processCheckout(bookingID, user.getUserID());
            } 
            catch (Exception e) 
            {
                log("Error saving invoice for booking " + bookingID, e);
            }
            
            // Xong xuôi thì tải lại trang Dashboard
            response.sendRedirect(request.getContextPath() + "/invoice");
        }
        else 
        {
            response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED, "Invalid action.");
        }
    }

    private Users requireStaff(HttpServletRequest request, HttpServletResponse response) throws IOException
    {
        HttpSession session = request.getSession(false);
        Object sessionUser = session == null ? null : session.getAttribute("user");
        if (!(sessionUser instanceof Users))
        {
            response.sendRedirect(request.getContextPath() + "/login");
            return null;
        }
        Users user = (Users) sessionUser;
        if (!"Staff".equalsIgnoreCase(user.getRole()) && !"Admin".equalsIgnoreCase(user.getRole()))
        {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Only staff or admin can access invoices.");
            return null;
        }
        if (user.getUserID() == null || user.getUserID() <= 0)
        {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Your login session is invalid. Please log in again.");
            return null;
        }
        return user;
    }

    @Override
    public String getServletInfo()
    {
        return "Invoice viewing and printing servlet";
    }
}