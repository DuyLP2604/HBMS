package filter;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import java.io.IOException;
import service.BookingExpirationService;

@WebFilter(filterName = "BookingExpirationFilter", urlPatterns = {"/room-types", "/booking-wish-list", "/checkout", "/payment", "/my-bookings", "/my-bookings/*"})
public class BookingExpirationFilter implements Filter
{
    private final BookingExpirationService expirationService = new BookingExpirationService();

    @Override
    public void init(FilterConfig filterConfig) throws ServletException
    {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException
    {
        try
        {
            expirationService.expirePendingBookings();
        }
        catch (RuntimeException ex)
        {
            throw new ServletException("Unable to update expired bookings.", ex);
        }
        chain.doFilter(request, response);
    }

    @Override
    public void destroy()
    {
    }
}