package util.exception;

import java.sql.SQLException;

/** Carries the SQL Server error code for a failed booking operation. */
public class BookingOperationException extends IllegalStateException
{
    private static final long serialVersionUID = 1L;

    private final int sqlErrorCode;

    public BookingOperationException(SQLException cause)
    {
        super("Booking operation failed (SQL " + cause.getErrorCode() + ").", cause);
        this.sqlErrorCode = cause.getErrorCode();
    }

    public int getSqlErrorCode()
    {
        return sqlErrorCode;
    }
}
