-- Run on the existing HBMS v6 database.
SET ANSI_NULLS ON;
SET QUOTED_IDENTIFIER ON;
SET ANSI_PADDING ON;
SET ANSI_WARNINGS ON;
SET CONCAT_NULL_YIELDS_NULL ON;
SET ARITHABORT ON;
SET NUMERIC_ROUNDABORT OFF;
GO

IF OBJECT_ID(N'dbo.SP_RECORD_PAYMENT', N'P') IS NULL
    OR COL_LENGTH(N'dbo.PAYMENT', N'CollectedByUserID') IS NULL
    THROW 51310, 'Select the HBMS v6 database before running this migration.', 1;
GO

IF OBJECT_ID(N'dbo.PAYOS_ORDER', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.PAYOS_ORDER (
        OrderCode BIGINT NOT NULL PRIMARY KEY,
        BookingID CHAR(6) NOT NULL REFERENCES dbo.BOOKING(BookingID),
        UserID INT NOT NULL REFERENCES dbo.USERS(UserID),
        PaymentType VARCHAR(10) NOT NULL CHECK (PaymentType IN ('DEPOSIT', 'FULL', 'BALANCE')),
        Amount DECIMAL(12,2) NOT NULL,
        WalletAmount DECIMAL(12,2) NOT NULL DEFAULT (0),
        ExternalAmount DECIMAL(12,2) NOT NULL,
        PaymentLinkId VARCHAR(100) NULL,
        Status VARCHAR(20) NOT NULL DEFAULT ('PENDING') CHECK (Status IN ('PENDING','PROCESSING','PAID','FAILED')),
        Reference VARCHAR(100) NULL,
        ErrorMessage VARCHAR(300) NULL,
        CreatedAt DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
        PaidAt DATETIME2 NULL,
        PaymentID CHAR(6) NULL REFERENCES dbo.PAYMENT(PaymentID),
        CONSTRAINT CK_PAYOS_ORDER_Amount CHECK (WalletAmount >= 0 AND ExternalAmount > 0
            AND Amount = WalletAmount + ExternalAmount)
    );
    CREATE UNIQUE INDEX UX_PAYOS_ORDER_PaymentLinkId ON dbo.PAYOS_ORDER(PaymentLinkId)
        WHERE PaymentLinkId IS NOT NULL;
    CREATE INDEX IX_PAYOS_ORDER_Booking ON dbo.PAYOS_ORDER(BookingID, UserID, CreatedAt DESC);
END
ELSE IF COL_LENGTH(N'dbo.PAYOS_ORDER', N'PaymentID') IS NULL
    ALTER TABLE dbo.PAYOS_ORDER ADD PaymentID CHAR(6) NULL REFERENCES dbo.PAYMENT(PaymentID);
GO

-- Link existing paid orders to their receipts.
UPDATE o SET PaymentID = p.PaymentID
FROM dbo.PAYOS_ORDER o JOIN dbo.PAYMENT p
    ON p.TransactionID = o.Reference AND p.BookingID = o.BookingID
    AND p.Amount = o.Amount AND p.WalletAmount = o.WalletAmount
    AND p.PaymentType = o.PaymentType AND p.MethodID = 'PT01'
WHERE o.Status = 'PAID' AND o.PaymentID IS NULL AND p.Status IN ('PAID', 'REFUNDED');
GO

CREATE OR ALTER PROCEDURE dbo.SP_CONFIRM_PAYOS_ORDER
    @OrderCode BIGINT, @PaymentLinkId VARCHAR(100), @ExternalAmount DECIMAL(12,2), @Reference VARCHAR(100)
AS
BEGIN
    SET NOCOUNT ON;
    SET XACT_ABORT ON;
    DECLARE @BookingID CHAR(6), @UserID INT, @Status VARCHAR(20), @PaymentType VARCHAR(10),
        @Amount DECIMAL(12,2), @WalletAmount DECIMAL(12,2), @ExpectedExternal DECIMAL(12,2),
        @PaymentID CHAR(6), @TransactionID VARCHAR(100) = CONCAT('PAYOS-', @OrderCode),
        @BookingStatus VARCHAR(30), @Deadline DATETIME, @Required DECIMAL(12,2),
        @WalletBalance DECIMAL(12,2), @Reason VARCHAR(300), @NextID INT;

    BEGIN TRY
        BEGIN TRANSACTION;
        EXEC dbo.SP_HBMS_ACQUIRE_WRITE_LOCK;
        SELECT @BookingID = o.BookingID, @UserID = o.UserID, @Status = o.Status,
            @PaymentType = o.PaymentType, @Amount = o.Amount, @WalletAmount = o.WalletAmount,
            @ExpectedExternal = o.ExternalAmount
        FROM dbo.PAYOS_ORDER o WITH (UPDLOCK, HOLDLOCK)
        WHERE o.OrderCode = @OrderCode
          AND o.PaymentLinkId COLLATE Latin1_General_100_BIN2 = @PaymentLinkId;

        -- Ignore test events and orders already paid.
        IF @BookingID IS NULL OR @Status = 'PAID'
        BEGIN
            COMMIT TRANSACTION;
            RETURN;
        END;

        IF @ExpectedExternal <> @ExternalAmount
            SET @Reason = N'Transfer amount does not match the PayOS order.';
        ELSE
        BEGIN
            SELECT @PaymentID = p.PaymentID
            FROM dbo.PAYMENT p
            JOIN dbo.[TRANSACTION] t ON t.TransactionID = p.TransactionID
            WHERE p.TransactionID IN (@TransactionID, @Reference) AND t.TransactionCode = @Reference
              AND p.BookingID = @BookingID AND p.PaymentType = @PaymentType AND p.MethodID = 'PT01'
              AND p.Amount = @Amount AND p.WalletAmount = @WalletAmount AND p.Status IN ('PAID','REFUNDED');

            IF @PaymentID IS NULL
            BEGIN
                SELECT @BookingStatus = b.BookingStatus, @Deadline = b.PaymentDeadline,
                    @Required = CASE WHEN b.FirstPaidAt IS NULL AND b.PaymentOption = @PaymentType
                            AND b.BookingStatus = 'PENDING_PAYMENT' THEN s.InitialRequiredAmount
                        WHEN b.FirstPaidAt IS NOT NULL AND @PaymentType = 'BALANCE'
                            AND b.BookingStatus IN ('CONFIRMED','ASSIGNED') THEN r.RoomRemainingAmount
                        WHEN b.FirstPaidAt IS NOT NULL AND @PaymentType = 'BALANCE'
                            AND b.BookingStatus = 'CHECKED_IN' THEN s.RemainingAmount END,
                    @WalletBalance = w.Balance
                FROM dbo.BOOKING b
                JOIN dbo.CUSTOMER c ON c.CustomerID = b.CustomerID
                JOIN dbo.V_BOOKING_PAYMENT_SUMMARY s ON s.BookingID = b.BookingID
                LEFT JOIN dbo.V_BOOKING_ROOM_BALANCE r ON r.BookingID = b.BookingID
                LEFT JOIN dbo.V_WALLET_BALANCE w ON w.CustomerID = b.CustomerID
                WHERE b.BookingID = @BookingID AND c.UserID = @UserID;

                IF @Required IS NULL OR @Required <> @Amount
                    OR (@BookingStatus = 'PENDING_PAYMENT' AND (@Deadline IS NULL OR @Deadline <= SYSDATETIME()))
                    SET @Reason = N'Booking expired or the amount due changed.';
                ELSE IF @WalletAmount > 0 AND ISNULL(@WalletBalance, 0) < @WalletAmount
                    SET @Reason = N'Wallet balance changed after the PayOS order was created.';
                ELSE IF EXISTS (SELECT 1 FROM dbo.[TRANSACTION] WHERE TransactionCode = @Reference
                    OR TransactionID = @TransactionID)
                    SET @Reason = N'This bank transaction was already recorded elsewhere.';
                ELSE
                BEGIN
                    SELECT @NextID = COALESCE(MAX(TRY_CONVERT(INT, SUBSTRING(PaymentID, 3, 4))), 0) + 1
                    FROM dbo.PAYMENT WHERE PaymentID LIKE 'PM[0-9][0-9][0-9][0-9]';
                    IF @NextID > 9999 THROW 51311, 'Payment ID limit reached.', 1;
                    SET @PaymentID = 'PM' + RIGHT('0000' + CONVERT(VARCHAR(4), @NextID), 4);

                    EXEC dbo.SP_RECORD_PAYMENT @PaymentID = @PaymentID, @BookingID = @BookingID,
                        @ActorUserID = @UserID, @PaymentType = @PaymentType, @Amount = @Amount,
                        @MethodID = 'PT01', @TransactionCode = @Reference, @WalletAmount = @WalletAmount,
                        @WalletAuthorizedBy = @UserID, @TransactionID = @TransactionID;
                    -- This payment was confirmed by PayOS.
                    UPDATE dbo.PAYMENT SET CollectedByUserID = NULL WHERE PaymentID = @PaymentID;
                END;
            END;
        END;

        IF @Reason IS NULL
            UPDATE dbo.PAYOS_ORDER SET Status = 'PAID', PaidAt = SYSDATETIME(),
                Reference = @Reference, PaymentID = @PaymentID, ErrorMessage = NULL WHERE OrderCode = @OrderCode;
        ELSE
            UPDATE dbo.PAYOS_ORDER SET Status = 'FAILED', Reference = @Reference,
                ErrorMessage = @Reason WHERE OrderCode = @OrderCode;

        COMMIT TRANSACTION;
    END TRY
    BEGIN CATCH
        IF XACT_STATE() <> 0 ROLLBACK TRANSACTION;
        THROW;
    END CATCH;
END;
GO

IF DATABASE_PRINCIPAL_ID(N'HBMS_APP_ROLE') IS NOT NULL
BEGIN
    GRANT INSERT ON dbo.PAYOS_ORDER TO HBMS_APP_ROLE;
    GRANT EXECUTE ON dbo.SP_CONFIRM_PAYOS_ORDER TO HBMS_APP_ROLE;
END;
GO
