/*
HBMS - DATABASE UPDATED 2026-10-07
IMPORTANT: This is a FULL RECREATE + SEED script. It DROPS the existing QLKS.
Back up existing data before executing. This file is not a live-data migration.

Requested changes:
1. CUSTOMER no longer stores citizen identity/passport numbers.
2. BOOKING.PaymentOption: DEPOSIT (exactly 30%) or FULL (100%).
   PAYMENT.PaymentType: DEPOSIT, FULL, BALANCE (all currently unpaid amount).
   FirstPaidAt and BookingAmountAtFirstPayment are immutable in the application API.
3. Refund 100% of all successful payments only when cancellation occurs strictly
   before FirstPaidAt + 24h. Exact boundary and later: zero refund. Check-in date
   does not determine this deadline. Later balance payments do not restart it.
4. Refunds credit CUSTOMER_WALLET through an auditable append-only ledger.
   PT09 is the optional internal-wallet payment method; PT06 remains external.
5. A PENDING_PAYMENT booking blocks all new bookings for the same customer,
   including bookings made on their behalf by staff. Paying the initial 30%/100%
   confirms it and releases that restriction; the remaining 70% does not block.
6. Anti-spam locks: 3 successive cancellations -> 5m; after unlock +2 -> 15m;
   after unlock +1 -> 60m, then +1 -> 60m again. Reset after a rolling 24h from
   the first cancellation. A completed stay breaks the current cancellation
   streak without lowering the penalty level until the daily reset. Payment
   alone does not reset it because paid bookings can still be cancelled.
   Customer/staff cancellation and payment-timeout cancellation all count.
   Cancellations during an active lock remain allowed (including eligible refunds)
   but do not trigger early escalation. Login, payment and wallet access remain available.

Run as database owner with SSMS/sqlcmd. Then integrate the stored procedures
with Java DAO and use HBMS_APP_ROLE with a dedicated application DB login.
Direct writes using sa/db_owner can bypass the procedure policy.
Schedule SP_EXPIRE_PENDING_BOOKINGS from Java/SQL Agent; it also resets old
anti-spam windows. No fixed new payment timeout is introduced.
*/

USE [master];
GO

/*******************************************************************************
   RECREATE DATABASE
   WARNING: This script deletes and recreates the QLKS database.
********************************************************************************/
IF DB_ID(N'QLKS') IS NOT NULL
BEGIN
    ALTER DATABASE QLKS SET SINGLE_USER WITH ROLLBACK IMMEDIATE;
    DROP DATABASE QLKS;
END;
GO

CREATE DATABASE QLKS;
GO

USE QLKS;
GO

SET ANSI_NULLS ON;
SET QUOTED_IDENTIFIER ON;
SET ANSI_PADDING ON;
SET ANSI_WARNINGS ON;
SET CONCAT_NULL_YIELDS_NULL ON;
SET ARITHABORT ON;
SET NUMERIC_ROUNDABORT OFF;
GO

/*******************************************************************************
   MASTER TABLES
********************************************************************************/

-- The project manages exactly one hotel. This table stores its public profile.
-- The hotel name is fixed, so it is the primary key and the CHECK keeps the
-- table at exactly one row.
--   HotelImage      : background image at the top of the home page
--   RoomImage       : "Luxury Rooms" image in the Overview section
--   PoolImage       : "Swimming Pool" image in the Overview section
--   RestaurantImage : "Restaurant" image in the Overview section
--   Address         : the hotel addresses, one address per line
CREATE TABLE HOTEL (
    HotelName NVARCHAR(50) NOT NULL,
    HotelImage NVARCHAR(100) NULL,
    RoomImage NVARCHAR(100) NULL,
    PoolImage NVARCHAR(100) NULL,
    RestaurantImage NVARCHAR(100) NULL,
    Address NVARCHAR(500) NOT NULL,

    CONSTRAINT PK_HOTEL PRIMARY KEY (HotelName),
    CONSTRAINT CK_HOTEL_SingleHotel CHECK (HotelName = N'Simple Bear Hotel')
);
GO

CREATE TABLE USERS (
    UserID INT IDENTITY(1,1) NOT NULL,
    Username VARCHAR(50) NOT NULL,
    Password VARCHAR(255) NOT NULL,
    Role VARCHAR(50) NOT NULL,

    CONSTRAINT PK_USERS PRIMARY KEY (UserID),
    CONSTRAINT UQ_USERS_Username UNIQUE (Username),
    CONSTRAINT CK_USERS_Role
    CHECK (
        Role IN (
            'Admin',
            'Staff',
            'Customer'
        )
    )
);
GO

CREATE TABLE NATIONALITY (
    NationalityID VARCHAR(50) NOT NULL,
    NationalityName NVARCHAR(100) NOT NULL,

    CONSTRAINT PK_NATIONALITY PRIMARY KEY (NationalityID),
    CONSTRAINT UQ_NATIONALITY_Name UNIQUE (NationalityName)
);
GO

CREATE TABLE CUSTOMER (
    CustomerID CHAR(6) NOT NULL,
    FullName NVARCHAR(100) NOT NULL,
    Phone VARCHAR(15) NULL,
    Email VARCHAR(100) NULL,
    Address NVARCHAR(200) NULL,
    NationalityID VARCHAR(50) NULL,
    UserID INT NOT NULL,

    CONSTRAINT PK_CUSTOMER PRIMARY KEY (CustomerID),
    CONSTRAINT UQ_CUSTOMER_User UNIQUE (UserID),
    CONSTRAINT UQ_CUSTOMER_Email UNIQUE (Email),
    CONSTRAINT FK_CUSTOMER_USERS
        FOREIGN KEY (UserID) REFERENCES USERS(UserID),
    CONSTRAINT FK_CUSTOMER_NATIONALITY
        FOREIGN KEY (NationalityID) REFERENCES NATIONALITY(NationalityID)
);
GO

-- Room types are global because the system has only one hotel.
CREATE TABLE ROOM_TYPE (
    RoomTypeID CHAR(4) NOT NULL,
    TypeName NVARCHAR(50) NOT NULL,
    Capacity INT NOT NULL,
    Price DECIMAL(12,2) NOT NULL,
    RoomTypeImage NVARCHAR(255) NULL,
    Description NVARCHAR(500) NULL,

    CONSTRAINT PK_ROOM_TYPE PRIMARY KEY (RoomTypeID),
    CONSTRAINT UQ_ROOM_TYPE_Name UNIQUE (TypeName),
    CONSTRAINT CK_ROOM_TYPE_Capacity CHECK (Capacity > 0),
    CONSTRAINT CK_ROOM_TYPE_Price CHECK (Price >= 0)
);
GO

-- Status describes operational state, not availability for a date range.
CREATE TABLE ROOM (
    RoomID CHAR(3) NOT NULL,
    RoomNumber NVARCHAR(20) NOT NULL,
    RoomImage NVARCHAR(100) NULL,
    Status VARCHAR(20) NOT NULL
        CONSTRAINT DF_ROOM_Status DEFAULT 'ACTIVE',
    RoomTypeID CHAR(4) NOT NULL,

    CONSTRAINT PK_ROOM PRIMARY KEY (RoomID),
    CONSTRAINT UQ_ROOM_Number UNIQUE (RoomNumber),
    CONSTRAINT FK_ROOM_ROOM_TYPE
        FOREIGN KEY (RoomTypeID) REFERENCES ROOM_TYPE(RoomTypeID),
    CONSTRAINT CK_ROOM_Status
        CHECK (Status IN ('ACTIVE', 'MAINTENANCE', 'INACTIVE'))
);
GO

CREATE TABLE EMPLOYEE (
    EmployeeID CHAR(6) NOT NULL,
    FullName NVARCHAR(100) NOT NULL,
    Position NVARCHAR(50) NOT NULL,
    Salary DECIMAL(12,2) NULL,
    Shift NVARCHAR(20) NULL,
    Address NVARCHAR(200) NULL,
    Phone VARCHAR(20) NULL,
    UserID INT NOT NULL,

    CONSTRAINT PK_EMPLOYEE PRIMARY KEY (EmployeeID),
    CONSTRAINT UQ_EMPLOYEE_User UNIQUE (UserID),
    CONSTRAINT UQ_EMPLOYEE_Phone UNIQUE (Phone),
    CONSTRAINT FK_EMPLOYEE_USERS
        FOREIGN KEY (UserID) REFERENCES USERS(UserID),
    CONSTRAINT CK_EMPLOYEE_Salary
        CHECK (Salary IS NULL OR Salary >= 0)
);
GO

/*******************************************************************************
   BOOKING TABLES
********************************************************************************/

-- Customer books room types. Concrete rooms are assigned later by staff.
CREATE TABLE BOOKING (
    BookingID CHAR(6) NOT NULL,
    PaymentOption VARCHAR(10) NOT NULL
        CONSTRAINT DF_BOOKING_PaymentOption DEFAULT 'FULL',
    FirstPaidAt DATETIME2(3) NULL,
    BookingAmountAtFirstPayment DECIMAL(12,2) NULL,
    RefundDeadline AS DATEADD(HOUR, 24, FirstPaidAt) PERSISTED,
    CancelledAt DATETIME2(3) NULL,
    CancellationReason VARCHAR(30) NULL,
    CancelledByUserID INT NULL,
    BookingDate DATETIME NOT NULL
        CONSTRAINT DF_BOOKING_BookingDate DEFAULT GETDATE(),
    PaymentDeadline DATETIME NULL,
    CheckInDate DATE NOT NULL,
    CheckOutDate DATE NOT NULL,
    BookingStatus VARCHAR(30) NOT NULL,
    CustomerID CHAR(6) NOT NULL,
    TotalAmount DECIMAL(12,2) NOT NULL
        CONSTRAINT DF_BOOKING_TotalAmount DEFAULT 0,

    CONSTRAINT PK_BOOKING PRIMARY KEY (BookingID),
    CONSTRAINT UQ_BOOKING_CustomerBooking UNIQUE (CustomerID, BookingID),
    CONSTRAINT FK_BOOKING_CancelledBy FOREIGN KEY (CancelledByUserID) REFERENCES USERS(UserID),
    CONSTRAINT CK_BOOKING_PaymentOption CHECK (PaymentOption IN ('DEPOSIT', 'FULL')),
    CONSTRAINT CK_BOOKING_FirstPayment CHECK (
        (FirstPaidAt IS NULL AND BookingAmountAtFirstPayment IS NULL)
        OR (FirstPaidAt IS NOT NULL AND BookingAmountAtFirstPayment IS NOT NULL AND BookingAmountAtFirstPayment > 0)
    ),
    CONSTRAINT CK_BOOKING_CancellationReason CHECK (
        CancellationReason IS NULL OR CancellationReason IN (
            'CUSTOMER_REQUEST', 'STAFF_REQUEST', 'PAYMENT_TIMEOUT'
        )
    ),
    CONSTRAINT FK_BOOKING_CUSTOMER
        FOREIGN KEY (CustomerID) REFERENCES CUSTOMER(CustomerID),
    CONSTRAINT CK_BOOKING_Date
        CHECK (CheckOutDate > CheckInDate),
    CONSTRAINT CK_BOOKING_Status
        CHECK (BookingStatus IN (
            'PENDING_PAYMENT',
            'CONFIRMED',
            'ASSIGNED',
            'CHECKED_IN',
            'CHECKED_OUT',
            'CANCELLED'
        )),
    CONSTRAINT CK_BOOKING_TotalAmount CHECK (TotalAmount >= 0),
    CONSTRAINT CK_BOOKING_PaymentDeadline CHECK (
        BookingStatus <> 'PENDING_PAYMENT'
        OR PaymentDeadline IS NOT NULL
    )
);
GO

CREATE TABLE BOOKING_DETAIL (
    BookingDetailID INT IDENTITY(1,1) NOT NULL,
    BookingID CHAR(6) NOT NULL,
    RoomTypeID CHAR(4) NOT NULL,
    Quantity INT NOT NULL,
    GuestCount INT NOT NULL,
    UnitPrice DECIMAL(12,2) NOT NULL,
    Subtotal DECIMAL(12,2) NOT NULL,

    CONSTRAINT PK_BOOKING_DETAIL PRIMARY KEY (BookingDetailID),
    CONSTRAINT UQ_BOOKING_DETAIL_Type
        UNIQUE (BookingID, RoomTypeID),
    CONSTRAINT FK_BOOKING_DETAIL_BOOKING
        FOREIGN KEY (BookingID) REFERENCES BOOKING(BookingID),
    CONSTRAINT FK_BOOKING_DETAIL_ROOM_TYPE
        FOREIGN KEY (RoomTypeID) REFERENCES ROOM_TYPE(RoomTypeID),
    CONSTRAINT CK_BOOKING_DETAIL_Quantity CHECK (Quantity > 0),
    CONSTRAINT CK_BOOKING_DETAIL_GuestCount CHECK (GuestCount > 0),
    CONSTRAINT CK_BOOKING_DETAIL_UnitPrice CHECK (UnitPrice >= 0),
    CONSTRAINT CK_BOOKING_DETAIL_Subtotal CHECK (Subtotal >= 0)
);
GO

CREATE TABLE ROOM_ASSIGNMENT (
    AssignmentID INT IDENTITY(1,1) NOT NULL,
    BookingDetailID INT NOT NULL,
    RoomID CHAR(3) NOT NULL,
    EmployeeID CHAR(6) NULL,
    AssignedAt DATETIME NOT NULL
        CONSTRAINT DF_ROOM_ASSIGNMENT_AssignedAt DEFAULT GETDATE(),

    CONSTRAINT PK_ROOM_ASSIGNMENT PRIMARY KEY (AssignmentID),
    CONSTRAINT UQ_ROOM_ASSIGNMENT_DetailRoom
        UNIQUE (BookingDetailID, RoomID),
    CONSTRAINT FK_ROOM_ASSIGNMENT_DETAIL
        FOREIGN KEY (BookingDetailID)
        REFERENCES BOOKING_DETAIL(BookingDetailID),
    CONSTRAINT FK_ROOM_ASSIGNMENT_ROOM
        FOREIGN KEY (RoomID) REFERENCES ROOM(RoomID),
    CONSTRAINT FK_ROOM_ASSIGNMENT_EMPLOYEE
        FOREIGN KEY (EmployeeID) REFERENCES EMPLOYEE(EmployeeID)
);
GO

/*******************************************************************************
   SERVICE, PAYMENT, INVOICE AND COMPLAINT
********************************************************************************/

-- A service is part of the single hotel's catalog, not a specific room.
CREATE TABLE SERVICE (
    ServiceID CHAR(4) NOT NULL,
    ServiceName NVARCHAR(60) NOT NULL,
    UnitPrice DECIMAL(12,2) NOT NULL,

    CONSTRAINT PK_SERVICE PRIMARY KEY (ServiceID),
    CONSTRAINT UQ_SERVICE_Name UNIQUE (ServiceName),
    CONSTRAINT CK_SERVICE_UnitPrice CHECK (UnitPrice >= 0)
);
GO

-- UnitPrice is a snapshot so later catalog price changes do not alter old bills.
CREATE TABLE BOOKING_SERVICE (
    BookingID CHAR(6) NOT NULL,
    ServiceID CHAR(4) NOT NULL,
    Quantity INT NOT NULL
        CONSTRAINT DF_BOOKING_SERVICE_Quantity DEFAULT 1,
    UnitPrice DECIMAL(12,2) NOT NULL,
    Subtotal DECIMAL(12,2) NOT NULL,

    CONSTRAINT PK_BOOKING_SERVICE
        PRIMARY KEY (BookingID, ServiceID),
    CONSTRAINT FK_BOOKING_SERVICE_BOOKING
        FOREIGN KEY (BookingID) REFERENCES BOOKING(BookingID),
    CONSTRAINT FK_BOOKING_SERVICE_SERVICE
        FOREIGN KEY (ServiceID) REFERENCES SERVICE(ServiceID),
    CONSTRAINT CK_BOOKING_SERVICE_Quantity CHECK (Quantity > 0),
    CONSTRAINT CK_BOOKING_SERVICE_UnitPrice CHECK (UnitPrice >= 0),
    CONSTRAINT CK_BOOKING_SERVICE_Subtotal CHECK (Subtotal >= 0),
    CONSTRAINT CK_BOOKING_SERVICE_Total
        CHECK (Subtotal = Quantity * UnitPrice)
);
GO

CREATE TABLE PAYMENTMETHOD (
    MethodID CHAR(4) NOT NULL,
    MethodName NVARCHAR(30) NOT NULL,

    CONSTRAINT PK_PAYMENTMETHOD PRIMARY KEY (MethodID),
    CONSTRAINT UQ_PAYMENTMETHOD_Name UNIQUE (MethodName)
);
GO

-- Multiple rows are allowed for retry attempts. Payment points to Booking.
CREATE TABLE PAYMENT (
    PaymentID CHAR(6) NOT NULL,
    BookingID CHAR(6) NOT NULL,
    PaymentType VARCHAR(10) NOT NULL
        CONSTRAINT DF_PAYMENT_Type DEFAULT 'FULL',
    PaymentTime DATETIME2(3) NULL,
    Amount DECIMAL(12,2) NOT NULL,
    Status VARCHAR(30) NOT NULL,
    MethodID CHAR(4) NULL,
    TransactionCode VARCHAR(100) NULL,

    CONSTRAINT PK_PAYMENT PRIMARY KEY (PaymentID),
    CONSTRAINT UQ_PAYMENT_BookingPayment UNIQUE (BookingID, PaymentID),
    CONSTRAINT CK_PAYMENT_Type CHECK (PaymentType IN ('DEPOSIT', 'FULL', 'BALANCE')),
    CONSTRAINT FK_PAYMENT_BOOKING
        FOREIGN KEY (BookingID) REFERENCES BOOKING(BookingID),
    CONSTRAINT FK_PAYMENT_METHOD
        FOREIGN KEY (MethodID) REFERENCES PAYMENTMETHOD(MethodID),
    CONSTRAINT CK_PAYMENT_Amount CHECK (Amount > 0),
    CONSTRAINT CK_PAYMENT_Status
        CHECK (Status IN ('PENDING', 'PAID', 'FAILED', 'REFUNDED')),
    CONSTRAINT CK_PAYMENT_CompletedData CHECK (
        Status IN ('PENDING', 'FAILED')
        OR (PaymentTime IS NOT NULL AND MethodID IS NOT NULL)
    )
);
GO

CREATE TABLE INVOICE (
    InvoiceID CHAR(6) NOT NULL,
    InvoiceDate DATE NOT NULL
        CONSTRAINT DF_INVOICE_Date DEFAULT CAST(GETDATE() AS DATE),
    TotalAmount DECIMAL(12,2) NOT NULL,
    Status NVARCHAR(30) NOT NULL
        CONSTRAINT DF_INVOICE_Status DEFAULT N'Có hiệu lực',
    ReplacedInvoiceID CHAR(6) NULL, 
    EmployeeID CHAR(6) NULL,
    BookingID CHAR(6) NOT NULL,

    CONSTRAINT PK_INVOICE PRIMARY KEY (InvoiceID),
    CONSTRAINT FK_INVOICE_EMPLOYEE
        FOREIGN KEY (EmployeeID) REFERENCES EMPLOYEE(EmployeeID),
    CONSTRAINT FK_INVOICE_BOOKING
        FOREIGN KEY (BookingID) REFERENCES BOOKING(BookingID),
    CONSTRAINT FK_INVOICE_REPLACED
        FOREIGN KEY (ReplacedInvoiceID) REFERENCES INVOICE(InvoiceID),
    CONSTRAINT CK_INVOICE_Status
        CHECK (Status IN (N'Có hiệu lực', N'Mất hiệu lực')),
    CONSTRAINT CK_INVOICE_TotalAmount
        CHECK (TotalAmount >= 0)
);
GO

CREATE UNIQUE INDEX UQ_INVOICE_ActiveBooking
ON INVOICE (BookingID)
WHERE Status = N'Có hiệu lực';
GO

CREATE TABLE COMPLAINT (
    ComplaintID INT IDENTITY(1,1) NOT NULL,
    Title NVARCHAR(200) NOT NULL,
    Content NVARCHAR(MAX) NOT NULL,
    CreatedAt DATETIME NOT NULL
        CONSTRAINT DF_COMPLAINT_CreatedAt DEFAULT GETDATE(),
    Status NVARCHAR(30) NOT NULL
        CONSTRAINT DF_COMPLAINT_Status DEFAULT N'Chưa xử lý',
    CustomerID CHAR(6) NULL,

    CONSTRAINT PK_COMPLAINT PRIMARY KEY (ComplaintID),
    CONSTRAINT FK_COMPLAINT_CUSTOMER
        FOREIGN KEY (CustomerID) REFERENCES CUSTOMER(CustomerID),
    CONSTRAINT CK_COMPLAINT_Status
        CHECK (Status IN (N'Chưa xử lý', N'Đang xử lý', N'Đã xử lý'))
);
GO

/*******************************************************************************
   INTERNAL WALLET, REFUNDS AND BOOKING ABUSE CONTROL
   All monetary amounts are VND; timestamps use the SQL Server clock consistently.
   Refund eligibility: CancelledAt < FirstPaidAt + 24 hours (exactly 24h: no refund).
   BALANCE payments never restart this clock.
********************************************************************************/
CREATE TABLE CUSTOMER_WALLET (
    WalletID BIGINT IDENTITY(1,1) NOT NULL,
    CustomerID CHAR(6) NOT NULL,
    CreatedAt DATETIME2(3) NOT NULL CONSTRAINT DF_WALLET_Created DEFAULT SYSDATETIME(),
    CONSTRAINT PK_CUSTOMER_WALLET PRIMARY KEY (WalletID),
    CONSTRAINT UQ_WALLET_Customer UNIQUE (CustomerID),
    CONSTRAINT UQ_WALLET_Owner UNIQUE (CustomerID, WalletID),
    CONSTRAINT FK_WALLET_Customer FOREIGN KEY (CustomerID) REFERENCES CUSTOMER(CustomerID)
);
GO

-- One completed, full refund per cancelled booking. Zero refunds are not recorded.
CREATE TABLE BOOKING_REFUND (
    RefundID BIGINT IDENTITY(1,1) NOT NULL,
    BookingID CHAR(6) NOT NULL,
    CustomerID CHAR(6) NOT NULL,
    WalletID BIGINT NOT NULL,
    Amount DECIMAL(12,2) NOT NULL,
    RefundedAt DATETIME2(3) NOT NULL,
    CONSTRAINT PK_BOOKING_REFUND PRIMARY KEY (RefundID),
    CONSTRAINT UQ_REFUND_Booking UNIQUE (BookingID),
    CONSTRAINT UQ_REFUND_BookingRefund UNIQUE (BookingID, RefundID),
    CONSTRAINT UQ_REFUND_WalletRefund UNIQUE (WalletID, RefundID),
    CONSTRAINT FK_REFUND_BookingOwner FOREIGN KEY (CustomerID, BookingID)
        REFERENCES BOOKING(CustomerID, BookingID),
    CONSTRAINT FK_REFUND_WalletOwner FOREIGN KEY (CustomerID, WalletID)
        REFERENCES CUSTOMER_WALLET(CustomerID, WalletID),
    CONSTRAINT CK_REFUND_Amount CHECK (Amount > 0)
);
GO

-- Original successful payment remains auditable; each can be refunded only once.
CREATE TABLE REFUND_PAYMENT (
    RefundID BIGINT NOT NULL,
    BookingID CHAR(6) NOT NULL,
    PaymentID CHAR(6) NOT NULL,
    Amount DECIMAL(12,2) NOT NULL,
    CONSTRAINT PK_REFUND_PAYMENT PRIMARY KEY (RefundID, PaymentID),
    CONSTRAINT UQ_REFUND_PAYMENT_Payment UNIQUE (PaymentID),
    CONSTRAINT FK_REFUND_PAYMENT_Refund FOREIGN KEY (BookingID, RefundID)
        REFERENCES BOOKING_REFUND(BookingID, RefundID),
    CONSTRAINT FK_REFUND_PAYMENT_Payment FOREIGN KEY (BookingID, PaymentID)
        REFERENCES PAYMENT(BookingID, PaymentID),
    CONSTRAINT CK_REFUND_PAYMENT_Amount CHECK (Amount > 0)
);
GO

-- Append-only ledger; balance is calculated, so no cached balance can drift.
CREATE TABLE WALLET_TRANSACTION (
    WalletTransactionID BIGINT IDENTITY(1,1) NOT NULL,
    WalletID BIGINT NOT NULL,
    TransactionType VARCHAR(20) NOT NULL,
    Amount DECIMAL(12,2) NOT NULL,
    TransactionTime DATETIME2(3) NOT NULL CONSTRAINT DF_WALLET_TX_Time DEFAULT SYSDATETIME(),
    RefundID BIGINT NULL,
    PaymentID CHAR(6) NULL,
    CONSTRAINT PK_WALLET_TRANSACTION PRIMARY KEY (WalletTransactionID),
    CONSTRAINT FK_WALLET_TX_Wallet FOREIGN KEY (WalletID) REFERENCES CUSTOMER_WALLET(WalletID),
    CONSTRAINT FK_WALLET_TX_Refund FOREIGN KEY (WalletID, RefundID)
        REFERENCES BOOKING_REFUND(WalletID, RefundID),
    CONSTRAINT FK_WALLET_TX_Payment FOREIGN KEY (PaymentID) REFERENCES PAYMENT(PaymentID),
    CONSTRAINT CK_WALLET_TX_Amount CHECK (Amount > 0),
    CONSTRAINT CK_WALLET_TX_Source CHECK (
        (TransactionType = 'REFUND_CREDIT' AND RefundID IS NOT NULL AND PaymentID IS NULL)
        OR (TransactionType = 'PAYMENT_DEBIT' AND PaymentID IS NOT NULL AND RefundID IS NULL)
    )
);
GO
CREATE UNIQUE INDEX UQ_WALLET_TX_Refund ON WALLET_TRANSACTION(RefundID) WHERE RefundID IS NOT NULL;
CREATE UNIQUE INDEX UQ_WALLET_TX_Payment ON WALLET_TRANSACTION(PaymentID) WHERE PaymentID IS NOT NULL;
CREATE INDEX IX_WALLET_TX_WalletTime ON WALLET_TRANSACTION(WalletID, TransactionTime);
GO

-- This lock restricts creating bookings; login, payments and wallet access stay available.
-- Rolling 24h window starts at the first cancellation, not at calendar midnight.
-- Cancellation thresholds: 3 -> 5m, +2 -> 15m, +1 -> 60m; later +1 -> 60m.
-- All cancellations (including payment timeout) count. A completed stay breaks
-- the cancellation streak but keeps the penalty level until the daily reset.
CREATE TABLE USER_BOOKING_CONTROL (
    UserID INT NOT NULL,
    WindowStartedAt DATETIME2(3) NULL,
    ConsecutiveCancellationCount INT NOT NULL CONSTRAINT DF_CONTROL_Streak DEFAULT 0,
    CancellationCountInWindow INT NOT NULL CONSTRAINT DF_CONTROL_Total DEFAULT 0,
    PenaltyLevel TINYINT NOT NULL CONSTRAINT DF_CONTROL_Level DEFAULT 0,
    BookingLockedUntil DATETIME2(3) NULL,
    UpdatedAt DATETIME2(3) NOT NULL CONSTRAINT DF_CONTROL_Updated DEFAULT SYSDATETIME(),
    CONSTRAINT PK_USER_BOOKING_CONTROL PRIMARY KEY (UserID),
    CONSTRAINT FK_CONTROL_User FOREIGN KEY (UserID) REFERENCES USERS(UserID),
    CONSTRAINT CK_CONTROL_Counts CHECK (
        ConsecutiveCancellationCount >= 0 AND CancellationCountInWindow >= ConsecutiveCancellationCount
    ),
    CONSTRAINT CK_CONTROL_Level CHECK (PenaltyLevel BETWEEN 0 AND 3)
);
GO

CREATE TABLE BOOKING_LOCK_HISTORY (
    LockHistoryID BIGINT IDENTITY(1,1) NOT NULL,
    UserID INT NOT NULL,
    TriggerBookingID CHAR(6) NOT NULL,
    PenaltyLevel TINYINT NOT NULL,
    LockedAt DATETIME2(3) NOT NULL,
    LockedUntil DATETIME2(3) NOT NULL,
    DurationMinutes INT NOT NULL,
    CONSTRAINT PK_BOOKING_LOCK_HISTORY PRIMARY KEY (LockHistoryID),
    CONSTRAINT UQ_LOCK_HISTORY_Booking UNIQUE (TriggerBookingID),
    CONSTRAINT FK_LOCK_HISTORY_User FOREIGN KEY (UserID) REFERENCES USERS(UserID),
    CONSTRAINT FK_LOCK_HISTORY_Booking FOREIGN KEY (TriggerBookingID) REFERENCES BOOKING(BookingID),
    CONSTRAINT CK_LOCK_HISTORY_Duration CHECK (
        (PenaltyLevel = 1 AND DurationMinutes = 5)
        OR (PenaltyLevel = 2 AND DurationMinutes = 15)
        OR (PenaltyLevel = 3 AND DurationMinutes = 60)
    ),
    CONSTRAINT CK_LOCK_HISTORY_Time CHECK (LockedUntil = DATEADD(MINUTE, DurationMinutes, LockedAt))
);
GO

-- One outstanding initial payment per account/customer, even with parallel requests.
CREATE UNIQUE INDEX UQ_BOOKING_OnePendingPerCustomer
ON BOOKING(CustomerID) WHERE BookingStatus = 'PENDING_PAYMENT';
CREATE UNIQUE INDEX UQ_PAYMENT_TransactionCode
ON PAYMENT(TransactionCode) WHERE TransactionCode IS NOT NULL;
GO

CREATE TRIGGER TR_CUSTOMER_CreateWalletAndControl ON CUSTOMER AFTER INSERT AS
BEGIN
    SET NOCOUNT ON;
    INSERT CUSTOMER_WALLET(CustomerID) SELECT CustomerID FROM inserted;
    INSERT USER_BOOKING_CONTROL(UserID) SELECT UserID FROM inserted;
END;
GO

/*******************************************************************************
   BUSINESS-RULE TRIGGERS
********************************************************************************/

-- Validate guest capacity and room subtotal for the selected stay.
CREATE TRIGGER TR_BOOKING_DETAIL_Validate
ON BOOKING_DETAIL
AFTER INSERT, UPDATE
AS
BEGIN
    SET NOCOUNT ON;

    IF EXISTS (
        SELECT 1
        FROM inserted AS i
        INNER JOIN BOOKING AS b
            ON b.BookingID = i.BookingID
        INNER JOIN ROOM_TYPE AS rt
            ON rt.RoomTypeID = i.RoomTypeID
        WHERE i.GuestCount > i.Quantity * rt.Capacity
           OR i.Subtotal <>
              i.Quantity * i.UnitPrice
              * DATEDIFF(DAY, b.CheckInDate, b.CheckOutDate)
    )
    BEGIN
        RAISERROR (
            'Invalid guest count or booking-detail subtotal.',
            16,
            1
        );
        IF @@TRANCOUNT > 0
            ROLLBACK TRANSACTION;
        RETURN;
    END;
END;
GO

-- Recalculate booking total whenever room details change.
CREATE TRIGGER TR_BOOKING_DETAIL_RecalculateTotal
ON BOOKING_DETAIL
AFTER INSERT, UPDATE, DELETE
AS
BEGIN
    SET NOCOUNT ON;

    ;WITH ChangedBooking AS (
        SELECT BookingID FROM inserted
        UNION
        SELECT BookingID FROM deleted
    )
    UPDATE b
    SET TotalAmount =
        COALESCE((
            SELECT SUM(bd.Subtotal)
            FROM BOOKING_DETAIL AS bd
            WHERE bd.BookingID = b.BookingID
        ), 0)
        +
        COALESCE((
            SELECT SUM(bs.Subtotal)
            FROM BOOKING_SERVICE AS bs
            WHERE bs.BookingID = b.BookingID
        ), 0)
    FROM BOOKING AS b
    INNER JOIN ChangedBooking AS cb
        ON cb.BookingID = b.BookingID;
END;
GO

-- Recalculate booking total whenever purchased services change.
CREATE TRIGGER TR_BOOKING_SERVICE_RecalculateTotal
ON BOOKING_SERVICE
AFTER INSERT, UPDATE, DELETE
AS
BEGIN
    SET NOCOUNT ON;

    ;WITH ChangedBooking AS (
        SELECT BookingID FROM inserted
        UNION
        SELECT BookingID FROM deleted
    )
    UPDATE b
    SET TotalAmount =
        COALESCE((
            SELECT SUM(bd.Subtotal)
            FROM BOOKING_DETAIL AS bd
            WHERE bd.BookingID = b.BookingID
        ), 0)
        +
        COALESCE((
            SELECT SUM(bs.Subtotal)
            FROM BOOKING_SERVICE AS bs
            WHERE bs.BookingID = b.BookingID
        ), 0)
    FROM BOOKING AS b
    INNER JOIN ChangedBooking AS cb
        ON cb.BookingID = b.BookingID;
END;
GO

-- Prevent assignment of a wrong, inactive, excessive or overlapping room.
CREATE TRIGGER TR_ROOM_ASSIGNMENT_Validate
ON ROOM_ASSIGNMENT
AFTER INSERT, UPDATE
AS
BEGIN
    SET NOCOUNT ON;

    IF EXISTS (
        SELECT 1
        FROM inserted AS i
        INNER JOIN BOOKING_DETAIL AS bd
            ON bd.BookingDetailID = i.BookingDetailID
        INNER JOIN BOOKING AS b
            ON b.BookingID = bd.BookingID
        INNER JOIN ROOM AS r
            ON r.RoomID = i.RoomID
        WHERE r.RoomTypeID <> bd.RoomTypeID
           OR r.Status <> 'ACTIVE'
           OR b.BookingStatus NOT IN (
               'CONFIRMED', 'ASSIGNED', 'CHECKED_IN', 'CHECKED_OUT'
           )
    )
    BEGIN
        RAISERROR (
            'Room must be active, match the requested type and belong to an assignable booking.',
            16,
            1
        );
        IF @@TRANCOUNT > 0
            ROLLBACK TRANSACTION;
        RETURN;
    END;

    IF EXISTS (
        SELECT 1
        FROM BOOKING_DETAIL AS bd
        INNER JOIN (
            SELECT DISTINCT BookingDetailID
            FROM inserted
        ) AS changed
            ON changed.BookingDetailID = bd.BookingDetailID
        CROSS APPLY (
            SELECT COUNT(*) AS AssignedCount
            FROM ROOM_ASSIGNMENT AS ra
            WHERE ra.BookingDetailID = bd.BookingDetailID
        ) AS counted
        WHERE counted.AssignedCount > bd.Quantity
    )
    BEGIN
        RAISERROR (
            'Assigned-room count cannot exceed requested quantity.',
            16,
            1
        );
        IF @@TRANCOUNT > 0
            ROLLBACK TRANSACTION;
        RETURN;
    END;

    IF EXISTS (
        SELECT 1
        FROM inserted AS i
        INNER JOIN BOOKING_DETAIL AS currentDetail
            ON currentDetail.BookingDetailID = i.BookingDetailID
        INNER JOIN BOOKING AS currentBooking
            ON currentBooking.BookingID = currentDetail.BookingID
        INNER JOIN ROOM_ASSIGNMENT AS otherAssignment
            ON otherAssignment.RoomID = i.RoomID
           AND otherAssignment.AssignmentID <> i.AssignmentID
        INNER JOIN BOOKING_DETAIL AS otherDetail
            ON otherDetail.BookingDetailID =
               otherAssignment.BookingDetailID
        INNER JOIN BOOKING AS otherBooking
            ON otherBooking.BookingID = otherDetail.BookingID
        WHERE currentBooking.BookingStatus <> 'CANCELLED'
          AND otherBooking.BookingStatus <> 'CANCELLED'
          AND currentBooking.CheckInDate < otherBooking.CheckOutDate
          AND currentBooking.CheckOutDate > otherBooking.CheckInDate
    )
    BEGIN
        RAISERROR (
            'This room is already assigned during an overlapping stay.',
            16,
            1
        );
        IF @@TRANCOUNT > 0
            ROLLBACK TRANSACTION;
        RETURN;
    END;
END;
GO

/*******************************************************************************
   SEED DATA FOR ONE HOTEL
********************************************************************************/

INSERT INTO HOTEL (HotelName, HotelImage, RoomImage, PoolImage, RestaurantImage, Address)
VALUES (
    N'Simple Bear Hotel',
    'hotel2.jpg',
    'hotel3.jpg',
    'hotel.jpg',
    'hotel4.jpg',
    N'600 Nguyen Van Cu Street (Extended), An Binh Ward, Can Tho City'
    + CHAR(10)
    + N'Lot E2a-7, D1 Street, High-Tech Park, Tang Nhon Phu Ward, Ho Chi Minh City, Vietnam'
    + CHAR(10)
    + N'Area K, FPT University, FPT Technology Urban Area, Ngu Hanh Son Ward, Da Nang City, Vietnam'
);
GO

INSERT INTO NATIONALITY (NationalityID, NationalityName)
VALUES
('N01', N'Vietnam'),
('N02', N'Japan'),
('N03', N'China'),
('N04', N'South Korea'),
('N05', N'Thailand'),
('N06', N'Singapore'),
('N07', N'Malaysia'),
('N08', N'Indonesia'),
('N09', N'Philippines'),
('N10', N'India'),
('N11', N'United States'),
('N12', N'Canada'),
('N13', N'United Kingdom'),
('N14', N'France'),
('N15', N'Germany'),
('N16', N'Italy'),
('N17', N'Spain'),
('N18', N'Australia'),
('N19', N'New Zealand'),
('N20', N'Other');
GO

INSERT INTO USERS (Username, Password, Role)
VALUES
('admin', 'e10adc3949ba59abbe56e057f20f883e', 'Admin'),
('nv02', 'e13dd027be0f2152ce387ac0ea83d863', 'Staff'),
('nv03', 'e13dd027be0f2152ce387ac0ea83d863', 'Staff'),
('nv04', 'e13dd027be0f2152ce387ac0ea83d863', 'Staff'),
('nv05', 'e13dd027be0f2152ce387ac0ea83d863', 'Staff'),
('nv06', 'e13dd027be0f2152ce387ac0ea83d863', 'Staff'),
('nv07', 'e13dd027be0f2152ce387ac0ea83d863', 'Staff'),
('nv08', 'e13dd027be0f2152ce387ac0ea83d863', 'Staff'),
('nv09', 'e13dd027be0f2152ce387ac0ea83d863', 'Staff'),
('nv10', 'e13dd027be0f2152ce387ac0ea83d863', 'Staff'),
('nv11', 'e13dd027be0f2152ce387ac0ea83d863', 'Staff'),
('nv12', 'e13dd027be0f2152ce387ac0ea83d863', 'Staff'),
('nv13', 'e13dd027be0f2152ce387ac0ea83d863', 'Staff'),
('nv14', 'e13dd027be0f2152ce387ac0ea83d863', 'Staff'),
('nv15', 'e13dd027be0f2152ce387ac0ea83d863', 'Staff'),
('nv16', 'e13dd027be0f2152ce387ac0ea83d863', 'Staff'),
('nv17', 'e13dd027be0f2152ce387ac0ea83d863', 'Staff'),
('nv18', 'e13dd027be0f2152ce387ac0ea83d863', 'Staff'),
('nv19', 'e13dd027be0f2152ce387ac0ea83d863', 'Staff'),
('nv20', 'e13dd027be0f2152ce387ac0ea83d863', 'Staff'),
('nv21', 'e13dd027be0f2152ce387ac0ea83d863', 'Staff'),
('nv22', 'e13dd027be0f2152ce387ac0ea83d863', 'Staff'),
('nv23', 'e13dd027be0f2152ce387ac0ea83d863', 'Staff'),
('nv24', 'e13dd027be0f2152ce387ac0ea83d863', 'Staff'),
('nv25', 'e13dd027be0f2152ce387ac0ea83d863', 'Staff'),
('nv26', 'e13dd027be0f2152ce387ac0ea83d863', 'Staff'),
('nv27', 'e13dd027be0f2152ce387ac0ea83d863', 'Staff'),
('nv28', 'e13dd027be0f2152ce387ac0ea83d863', 'Staff'),
('nv29', 'e13dd027be0f2152ce387ac0ea83d863', 'Staff'),
('nv30', 'e13dd027be0f2152ce387ac0ea83d863', 'Staff'),
('nv31', 'e13dd027be0f2152ce387ac0ea83d863', 'Staff'),
('kh01', '4297f44b13955235245b2497399d7a93', 'Customer'),
('kh02', '4297f44b13955235245b2497399d7a93', 'Customer'),
('kh03', '4297f44b13955235245b2497399d7a93', 'Customer'),
('kh04', '4297f44b13955235245b2497399d7a93', 'Customer'),
('kh05', '4297f44b13955235245b2497399d7a93', 'Customer'),
('kh06', '4297f44b13955235245b2497399d7a93', 'Customer'),
('kh07', '4297f44b13955235245b2497399d7a93', 'Customer'),
('kh08', '4297f44b13955235245b2497399d7a93', 'Customer'),
('kh09', '4297f44b13955235245b2497399d7a93', 'Customer'),
('kh10', '4297f44b13955235245b2497399d7a93', 'Customer'),
('kh11', '4297f44b13955235245b2497399d7a93', 'Customer'),
('kh12', '4297f44b13955235245b2497399d7a93', 'Customer'),
('kh13', '4297f44b13955235245b2497399d7a93', 'Customer'),
('kh14', '4297f44b13955235245b2497399d7a93', 'Customer'),
('kh15', '4297f44b13955235245b2497399d7a93', 'Customer'),
('kh16', '4297f44b13955235245b2497399d7a93', 'Customer'),
('kh17', '4297f44b13955235245b2497399d7a93', 'Customer'),
('kh18', '4297f44b13955235245b2497399d7a93', 'Customer'),
('kh19', '4297f44b13955235245b2497399d7a93', 'Customer'),
('kh20', '4297f44b13955235245b2497399d7a93', 'Customer'),
('kh21', '4297f44b13955235245b2497399d7a93', 'Customer'),
('kh22', '4297f44b13955235245b2497399d7a93', 'Customer'),
('kh23', '4297f44b13955235245b2497399d7a93', 'Customer'),
('kh24', '4297f44b13955235245b2497399d7a93', 'Customer'),
('kh25', '4297f44b13955235245b2497399d7a93', 'Customer'),
('kh26', '4297f44b13955235245b2497399d7a93', 'Customer'),
('kh27', '4297f44b13955235245b2497399d7a93', 'Customer'),
('kh28', '4297f44b13955235245b2497399d7a93', 'Customer'),
('kh29', '4297f44b13955235245b2497399d7a93', 'Customer'),
('kh30', '4297f44b13955235245b2497399d7a93', 'Customer'),
('kh31', '4297f44b13955235245b2497399d7a93', 'Customer'),
('kh32', '4297f44b13955235245b2497399d7a93', 'Customer'),
('kh33', '4297f44b13955235245b2497399d7a93', 'Customer'),
('kh34', '4297f44b13955235245b2497399d7a93', 'Customer'),
('kh35', '4297f44b13955235245b2497399d7a93', 'Customer'),
('kh36', '4297f44b13955235245b2497399d7a93', 'Customer'),
('kh37', '4297f44b13955235245b2497399d7a93', 'Customer'),
('kh38', '4297f44b13955235245b2497399d7a93', 'Customer'),
('kh39', '4297f44b13955235245b2497399d7a93', 'Customer'),
('kh40', '4297f44b13955235245b2497399d7a93', 'Customer'),
('kh41', '4297f44b13955235245b2497399d7a93', 'Customer'),
('kh42', '4297f44b13955235245b2497399d7a93', 'Customer'),
('kh43', '4297f44b13955235245b2497399d7a93', 'Customer'),
('kh44', '4297f44b13955235245b2497399d7a93', 'Customer'),
('kh45', '4297f44b13955235245b2497399d7a93', 'Customer'),
('kh46', '4297f44b13955235245b2497399d7a93', 'Customer'),
('kh47', '4297f44b13955235245b2497399d7a93', 'Customer'),
('kh48', '4297f44b13955235245b2497399d7a93', 'Customer'),
('kh49', '4297f44b13955235245b2497399d7a93', 'Customer'),
('kh50', '4297f44b13955235245b2497399d7a93', 'Customer');
GO

INSERT INTO EMPLOYEE
(EmployeeID, FullName, Position, Salary, Shift, Address, Phone, UserID)
VALUES
('NV02', N'Nguyễn Minh Anh', N'Receptionist', 9500000, N'Morning', N'Cần Thơ', '0901000002', 2),
('NV03', N'Trần Quốc Bảo', N'Receptionist', 9500000, N'Afternoon', N'Cần Thơ', '0901000003', 3),
('NV04', N'Lê Hoàng Nam', N'Receptionist', 10500000, N'Night', N'Cần Thơ', '0901000004', 4),
('NV05', N'Phạm Thùy Linh', N'Receptionist', 9500000, N'Morning', N'Cần Thơ', '0901000005', 5),
('NV06', N'Võ Minh Khang', N'Receptionist', 9500000, N'Afternoon', N'Cần Thơ', '0901000006', 6),

('NV07', N'Nguyễn Ngọc Hân', N'Customer service staff', 8000000, N'Morning', N'Cần Thơ', '0901000007', 7),
('NV08', N'Trần Thanh Tùng', N'Customer service staff', 8000000, N'Afternoon', N'Cần Thơ', '0901000008', 8),
('NV09', N'Lê Khánh Vy', N'Customer service staff', 8000000, N'Night', N'Cần Thơ', '0901000009', 9),
('NV10', N'Phan Gia Huy', N'Customer service staff', 8000000, N'Morning', N'Cần Thơ', '0901000010', 10),
('NV11', N'Đặng Mỹ Duyên', N'Customer service staff', 8000000, N'Afternoon', N'Cần Thơ', '0901000011', 11),

-- Spa
('NV12', N'Bùi Thanh Trúc', N'Service staff', 8000000, N'Morning', N'Cần Thơ', '0901000012', 12),

-- Restaurant
('NV13', N'Nguyễn Thành Đạt', N'Service staff', 12000000, N'Morning', N'Cần Thơ', '0901000013', 13),
('NV14', N'Trần Minh Đức', N'Service staff', 10000000, N'Afternoon', N'Cần Thơ', '0901000014', 14),
('NV15', N'Lê Ngọc Mai', N'Service staff', 10000000, N'Night', N'Cần Thơ', '0901000015', 15),
('NV16', N'Phạm Quốc Khánh', N'Service staff', 10000000, N'Morning', N'Cần Thơ', '0901000016', 16),
('NV17', N'Võ Thu Hà', N'Service staff', 10000000, N'Afternoon', N'Cần Thơ', '0901000017', 17),

-- Car rental
('NV18', N'Nguyễn Văn Phúc', N'Service staff', 7000000, N'Morning', N'Cần Thơ', '0901000018', 18),
('NV19', N'Trần Gia Hưng', N'Service staff', 7000000, N'Afternoon', N'Cần Thơ', '0901000019', 19),
('NV20', N'Lê Minh Quân', N'Service staff', 7000000, N'Night', N'Cần Thơ', '0901000020', 20),

-- Laundry
('NV21', N'Phạm Thị Hương', N'Service staff', 7000000, N'Morning', N'Cần Thơ', '0901000021', 21),
('NV22', N'Nguyễn Thị Ngọc', N'Service staff', 7000000, N'Afternoon', N'Cần Thơ', '0901000022', 22),
('NV23', N'Trần Mỹ Linh', N'Service staff', 7000000, N'Night', N'Cần Thơ', '0901000023', 23),

-- Clean up
('NV24', N'Lê Thị Thanh', N'Service staff', 7000000, N'Morning', N'Cần Thơ', '0901000024', 24),
('NV25', N'Phạm Ngọc Lan', N'Service staff', 7000000, N'Afternoon', N'Cần Thơ', '0901000025', 25),
('NV26', N'Nguyễn Thị Mai', N'Service staff', 7000000, N'Night', N'Cần Thơ', '0901000026', 26),
('NV27', N'Trần Thị Thu', N'Service staff', 7000000, N'Morning', N'Cần Thơ', '0901000027', 27),

-- Spa
('NV28', N'Võ Ngọc Diệp', N'Service staff', 8000000, N'Afternoon', N'Cần Thơ', '0901000028', 28),
('NV29', N'Đặng Thảo Vy', N'Service staff', 8000000, N'Night', N'Cần Thơ', '0901000029', 29),
('NV30', N'Nguyễn Khánh An', N'Service staff', 8000000, N'Morning', N'Cần Thơ', '0901000030', 30),
('NV31', N'Trần Bảo Trâm', N'Service staff', 8000000, N'Afternoon', N'Cần Thơ', '0901000031', 31);
GO

INSERT INTO CUSTOMER
(CustomerID, FullName, Phone, Email, Address, NationalityID, UserID)
VALUES
('KH01', N'Phạm Minh Tuấn', '0903456789', 'tuan.pham@gmail.com', N'Quận 1, TP.HCM', 'N01', 32),
('KH02', N'Nguyễn Tuyết Mai', '0912888999', 'maituyet92@yahoo.com', N'Quận Cầu Giấy, Hà Nội', 'N01', 33),
('KH03', N'Lê Hoàng Nam', '0987111222', 'namlh@gmail.com', N'Quận 7, TP.HCM', 'N01', 34),
('KH04', N'Trần Thu Hà', '0356123456', 'hatran@fpt.com.vn', N'Quận Tây Hồ, Hà Nội', 'N01', 35),
('KH05', N'Robert Harrison', '0775123456', 'robert.h@outlook.com', N'London, United Kingdom', 'N13', 36),
('KH06', N'Chen Wei', '0933444555', 'chenwei88@qq.com', N'Beijing, China', 'N03', 37),
('KH07', N'Hans Müller', '0888777666', 'hans.m@gmail.de', N'Berlin, Germany', 'N15', 38),
('KH08', N'Kim Ji-won', '0944555666', 'jiwon.kim@naver.com', N'Seoul, South Korea', 'N04', 39),
('KH09', N'Jean Dupont', '0766112233', 'jean.dupont@france.fr', N'Paris, France', 'N14', 40),
('KH10', N'Đặng Phương Thảo', '0399123456', 'thaophuong@gmail.com', N'Quận Hải Châu, Đà Nẵng', 'N01', 41),

('KH11', N'Vũ Đình Tùng', '0988123123', 'tung.vu@gmail.com', N'Quận 3, TP.HCM', 'N01', 42),
('KH12', N'Lê Thị Thu Hương', '0977234234', 'huongle99@yahoo.com', N'Quận Đống Đa, Hà Nội', 'N01', 43),
('KH13', N'Nguyễn Quang Hải', '0912345345', 'hai.nq@fpt.edu.vn', N'Ninh Kiều, Cần Thơ', 'N01', 44),
('KH14', N'Phan Thanh Bình', '0933456456', 'binhpt@gmail.com', N'Quận 1, TP.HCM', 'N01', 45),
('KH15', N'Đỗ Mỹ Linh', '0944567567', 'mylinh.do@gmail.com', N'Quận Hoàn Kiếm, Hà Nội', 'N01', 46),
('KH16', N'Bùi Tiến Dũng', '0966678678', 'dung.bui@viettel.com.vn', N'Thạch Thất, Hà Nội', 'N01', 47),
('KH17', N'Trần Phương Anh', '0888789789', 'phuonganh.t@yahoo.com', N'Quận Sơn Trà, Đà Nẵng', 'N01', 48),
('KH18', N'Lý Nhã Kỳ', '0901890890', 'nhaky.ly@gmail.com', N'Quận 7, TP.HCM', 'N01', 49),
('KH19', N'Hoàng Lệ Thu', '0922901901', 'thuhl@outlook.com', N'Biên Hòa, Đồng Nai', 'N01', 50),
('KH20', N'Ngô Thanh Vân', '0933012012', 'van.ngo@gmail.com', N'Quận Phú Nhuận, TP.HCM', 'N01', 51),
('KH21', N'Võ Hoàng Yến', '0944123123', 'hoangyen.vo@gmail.com', N'Quận 4, TP.HCM', 'N01', 52),
('KH22', N'Đinh Ngọc Diệp', '0955234234', 'ngocdiep.dinh@yahoo.com', N'Quận Bình Thạnh, TP.HCM', 'N01', 53),
('KH23', N'Lương Xuân Trường', '0966345345', 'truong.lx@hagl.com.vn', N'Pleiku, Gia Lai', 'N01', 54),
('KH24', N'Cao Thái Sơn', '0977456456', 'soncao@gmail.com', N'Quận 10, TP.HCM', 'N01', 55),
('KH25', N'Hồ Ngọc Hà', '0988567567', 'hongocha@gmail.com', N'Quận 2, TP.HCM', 'N01', 56),
('KH26', N'Mai Phương Thúy', '0999678678', 'thuymp@gmail.com', N'Quận Cầu Giấy, Hà Nội', 'N01', 57),
('KH27', N'Dương Trương Thiên Lý', '0901789789', 'thienly.dt@gmail.com', N'Đồng Tháp', 'N01', 58),
('KH28', N'Tăng Thanh Hà', '0912890890', 'hathanh.tang@gmail.com', N'Quận 2, TP.HCM', 'N01', 59),
('KH29', N'Phạm Hương', '0923901901', 'huong.pham@gmail.com', N'Hải Phòng', 'N01', 60),
('KH30', N'Nguyễn Thúc Thùy Tiên', '0934012012', 'thuytien.nt@gmail.com', N'Quận Gò Vấp, TP.HCM', 'N01', 61),
('KH31', N'Lê Quốc Bảo', '0945123123', 'baolq@gmail.com', N'Quận Tân Bình, TP.HCM', 'N01', 62),
('KH32', N'Trần Kim Chi', '0956234234', 'kimchi.tran@yahoo.com', N'Quận Thanh Xuân, Hà Nội', 'N01', 63),
('KH33', N'Phan Đình Phùng', '0967345345', 'phungpd@gmail.com', N'Vinh, Nghệ An', 'N01', 64),
('KH34', N'Vũ Cát Tường', '0978456456', 'cattuong.vu@gmail.com', N'Long Xuyên, An Giang', 'N01', 65),
('KH35', N'Đỗ Trọng Hiếu', '0989567567', 'hieudo@gmail.com', N'Quận Hai Bà Trưng, Hà Nội', 'N01', 66),
('KH36', N'Bùi Anh Tuấn', '0990678678', 'anhtuan.bui@gmail.com', N'Quận 1, TP.HCM', 'N01', 67),
('KH37', N'Lê Minh Sơn', '0902789789', 'sonlm@gmail.com', N'Bắc Ninh', 'N01', 68),
('KH38', N'Hoàng Thùy Linh', '0913890890', 'thuylinh.hoang@gmail.com', N'Quận Ba Đình, Hà Nội', 'N01', 69),
('KH39', N'Nguyễn Trần Trung Quân', '0924901901', 'trungquan.nt@gmail.com', N'Quận Đống Đa, Hà Nội', 'N01', 70),
('KH40', N'Phạm Tiến Dũng', '0935012012', 'dungpt@gmail.com', N'Nha Trang, Khánh Hòa', 'N01', 71),

('KH41', N'Emily Johnson', NULL, 'emily.j@yahoo.com', N'Los Angeles, USA', 'N11', 72),
('KH42', N'Michael Brown', NULL, 'michael.b@outlook.com', N'Chicago, USA', 'N11', 73),
('KH43', N'Li Na', NULL, 'lina.beijing@qq.com', N'Shanghai, China', 'N03', 74),
('KH44', N'Wang Lei', NULL, 'wanglei88@wechat.com', N'Guangzhou, China', 'N03', 75),
('KH45', N'Park Min-young', NULL, 'minyoung.park@naver.com', N'Busan, South Korea', 'N04', 76),
('KH46', N'Lee Jong-suk', NULL, 'jongsuk.lee@daum.net', N'Incheon, South Korea', 'N04', 77),
('KH47', N'Sato Kenji', NULL, 'kenji.sato@yahoo.co.jp', N'Tokyo, Japan', 'N02', 78),
('KH48', N'Tanaka Yuki', NULL, 'yuki.tanaka@gmail.com', N'Osaka, Japan', 'N02', 79),
('KH49', N'Anna Kowalski', NULL, 'anna.k@gmail.com', N'Warsaw, Poland', 'N20', 80),
('KH50', N'David Silva', NULL, 'david.silva@hotmail.com', N'Madrid, Spain', 'N17', 81);
GO

INSERT INTO ROOM_TYPE
(RoomTypeID, TypeName, Capacity, Price, RoomTypeImage, Description)
VALUES
('RT01', N'Phòng đơn', 2, 500000, N'room-type-single.jpg',
 N'1 giường đôi, tối đa 2 khách'),
('RT02', N'Phòng đôi', 4, 800000, N'room-type-double.jpg',
 N'2 giường đôi, tối đa 4 khách'),
('RT03', N'Phòng Luxury đơn', 2, 1200000, N'room-type-luxury-single.jpg',
 N'Phòng cao cấp 1 giường đôi, tối đa 2 khách'),
('RT04', N'Phòng Luxury đôi', 4, 1600000, N'room-type-luxury-double.jpg',
 N'Phòng cao cấp 2 giường đôi, tối đa 4 khách');
GO

INSERT INTO ROOM
(RoomID, RoomNumber, RoomImage, Status, RoomTypeID)
VALUES
('R01', N'101', 'room01.png', 'ACTIVE',      'RT01'),
('R02', N'102', 'room02.png', 'ACTIVE',      'RT01'),
('R03', N'103', 'room03.png', 'MAINTENANCE', 'RT01'),
('R04', N'201', 'room04.png', 'ACTIVE',      'RT02'),
('R05', N'202', 'room05.png', 'ACTIVE',      'RT02'),
('R06', N'203', 'room06.png', 'ACTIVE',      'RT02'),
('R07', N'204', 'room07.png', 'ACTIVE',      'RT02'),
('R08', N'301', 'room08.png', 'ACTIVE',      'RT03'),
('R09', N'302', 'room09.png', 'ACTIVE',      'RT03'),
('R10', N'401', 'room10.png', 'ACTIVE',      'RT04');
GO

INSERT INTO SERVICE (ServiceID, ServiceName, UnitPrice)
VALUES
('S01', N'Giặt ủi', 75000),
('S02', N'Suối nước nóng', 200000),
('S03', N'Thuê xe', 150000),
('S04', N'Spa', 450000),
('S05', N'Ăn uống tại phòng', 0),
('S06', N'Dịch vụ trông trẻ', 150000),
('S07', N'Đưa đón sân bay', 350000),
('S08', N'Dọn dẹp phòng', 0),
('S09', N'Ăn tối buffer', 350000),
('S10', N'Giữ hành lý', 50000);
GO

INSERT INTO BOOKING
(BookingID, BookingDate, PaymentDeadline, CheckInDate, CheckOutDate,
 BookingStatus, CustomerID)
VALUES
('BK0001', '2026-09-01 09:00:00', NULL, '2026-09-05', '2026-09-07',
 'CHECKED_OUT', 'KH01'),
('BK0002', '2026-09-03 10:15:00', NULL, '2026-09-08', '2026-09-10',
 'CHECKED_OUT', 'KH02'),
('BK0003', '2026-09-10 08:30:00', NULL, '2026-09-18', '2026-09-20',
 'ASSIGNED', 'KH03'),
('BK0004', '2026-09-11 14:00:00', NULL, '2026-09-20', '2026-09-22',
 'CONFIRMED', 'KH04'),
('BK0005', '2026-09-12 16:10:00', DATEADD(MINUTE, 15, GETDATE()),
 '2026-09-21', '2026-09-23',
 'PENDING_PAYMENT', 'KH05'),
('BK0006', '2026-09-13 11:45:00', NULL, '2026-09-24', '2026-09-26',
 'CONFIRMED', 'KH06'),
('BK0007', '2026-09-14 09:20:00', NULL, '2026-09-25', '2026-09-27',
 'CANCELLED', 'KH07'),
('BK0008', '2026-09-15 13:30:00', NULL, '2026-09-28', '2026-10-01',
 'ASSIGNED', 'KH08'),
('BK0009', '2026-09-16 15:00:00', NULL, '2026-10-02', '2026-10-04',
 'CONFIRMED', 'KH09'),
('BK0010', '2026-09-17 06:30:00', DATEADD(MINUTE, 15, GETDATE()),
 '2026-10-05', '2026-10-07',
 'PENDING_PAYMENT', 'KH10'),
('BK0011', '2026-07-27 09:00:00', NULL, '2026-08-03', '2026-08-05',
 'CHECKED_OUT', 'KH11'),
('BK0012', '2026-07-28 09:00:00', NULL, '2026-08-04', '2026-08-07',
 'CHECKED_OUT', 'KH12'),
('BK0013', '2026-08-02 09:00:00', NULL, '2026-08-09', '2026-08-11',
 'CHECKED_OUT', 'KH13'),
('BK0014', '2026-08-05 09:00:00', NULL, '2026-08-12', '2026-08-14',
 'CHECKED_OUT', 'KH14'),
('BK0015', '2026-08-10 09:00:00', NULL, '2026-08-17', '2026-08-20',
 'CHECKED_OUT', 'KH15'),
('BK0016', '2026-08-11 09:00:00', NULL, '2026-08-18', '2026-08-21',
 'CHECKED_OUT', 'KH16'),
('BK0017', '2026-08-16 09:00:00', NULL, '2026-08-23', '2026-08-25',
 'CHECKED_OUT', 'KH17'),
('BK0018', '2026-08-19 09:00:00', NULL, '2026-08-26', '2026-08-29',
 'CHECKED_OUT', 'KH18'),
('BK0019', '2026-08-23 09:00:00', NULL, '2026-08-30', '2026-09-01',
 'CHECKED_OUT', 'KH19'),
('BK0020', '2026-08-24 09:00:00', NULL, '2026-08-31', '2026-09-03',
 'CHECKED_OUT', 'KH20'),
('BK0021', '2026-08-28 09:00:00', NULL, '2026-09-04', '2026-09-06',
 'CHECKED_OUT', 'KH21'),
('BK0022', '2026-08-30 09:00:00', NULL, '2026-09-06', '2026-09-08',
 'CHECKED_OUT', 'KH22'),
('BK0023', '2026-09-02 09:00:00', NULL, '2026-09-09', '2026-09-11',
 'CHECKED_OUT', 'KH23'),
('BK0024', '2026-09-04 09:00:00', NULL, '2026-09-11', '2026-09-13',
 'CHECKED_OUT', 'KH24'),
('BK0025', '2026-09-06 09:00:00', NULL, '2026-09-13', '2026-09-16',
 'CHECKED_OUT', 'KH25'),
('BK0026', '2026-09-08 09:00:00', NULL, '2026-09-15', '2026-09-17',
 'CHECKED_OUT', 'KH26'),
('BK0027', '2026-09-10 09:00:00', NULL, '2026-09-17', '2026-09-19',
 'CHECKED_OUT', 'KH27'),
('BK0028', '2026-09-12 09:00:00', NULL, '2026-09-19', '2026-09-21',
 'CHECKED_OUT', 'KH28'),
('BK0029', '2026-09-13 09:00:00', NULL, '2026-09-20', '2026-09-22',
 'CHECKED_OUT', 'KH29'),
('BK0030', '2026-09-14 09:00:00', NULL, '2026-09-21', '2026-09-23',
 'CHECKED_OUT', 'KH30'),
('BK0031', '2026-09-23 09:00:00', NULL, '2026-09-24', '2026-09-26',
 'CONFIRMED', 'KH31'),
('BK0032', '2026-09-20 09:00:00', NULL, '2026-09-26', '2026-09-28',
 'ASSIGNED', 'KH32'),
('BK0033', '2026-09-21 09:00:00', NULL, '2026-09-27', '2026-09-29',
 'CONFIRMED', 'KH33'),
('BK0034', '2026-09-22 09:00:00', NULL, '2026-09-29', '2026-10-01',
 'ASSIGNED', 'KH34'),
('BK0035', '2026-09-23 09:00:00', NULL, '2026-10-02', '2026-10-04',
 'CONFIRMED', 'KH35'),
('BK0036', '2026-09-20 09:00:00', DATEADD(MINUTE, 15, GETDATE()), '2026-10-05', '2026-10-08',
 'PENDING_PAYMENT', 'KH36'),
('BK0037', '2026-09-21 09:00:00', DATEADD(MINUTE, 15, GETDATE()), '2026-10-08', '2026-10-10',
 'PENDING_PAYMENT', 'KH37'),
('BK0038', '2026-09-22 09:00:00', NULL, '2026-10-11', '2026-10-13',
 'CANCELLED', 'KH38'),
('BK0039', '2026-09-23 09:00:00', NULL, '2026-10-14', '2026-10-16',
 'CONFIRMED', 'KH39'),
('BK0040', '2026-09-20 09:00:00', NULL, '2026-10-18', '2026-10-21',
 'CONFIRMED', 'KH40');
GO

SET IDENTITY_INSERT BOOKING_DETAIL ON;

INSERT INTO BOOKING_DETAIL
(BookingDetailID, BookingID, RoomTypeID, Quantity,
 GuestCount, UnitPrice, Subtotal)
VALUES
(1,  'BK0001', 'RT01', 1, 1,  500000, 1000000),
(2,  'BK0002', 'RT02', 1, 2,  800000, 1600000),
(3,  'BK0003', 'RT03', 1, 2, 1200000, 2400000), -- FIX: RT03 capacity = 2
(4,  'BK0004', 'RT01', 1, 1,  500000, 1000000),
(5,  'BK0004', 'RT02', 1, 2,  800000, 1600000),
(6,  'BK0005', 'RT01', 1, 1,  500000, 1000000),
(7,  'BK0006', 'RT02', 2, 4,  800000, 3200000),
(8,  'BK0007', 'RT04', 1, 4, 1600000, 3200000),
(9,  'BK0008', 'RT02', 1, 2,  800000, 2400000),
(10, 'BK0009', 'RT01', 1, 1,  500000, 1000000),
(11, 'BK0010', 'RT03', 1, 2, 1200000, 2400000), -- RT03 capacity = 2
(12, 'BK0011', 'RT01', 1, 2, 500000, 1000000),
(13, 'BK0012', 'RT02', 1, 1, 800000, 2400000),
(14, 'BK0013', 'RT03', 1, 2, 1200000, 2400000),
(15, 'BK0014', 'RT04', 1, 2, 1600000, 3200000),
(16, 'BK0015', 'RT01', 1, 1, 500000, 1500000),
(17, 'BK0016', 'RT02', 1, 2, 800000, 2400000),
(18, 'BK0017', 'RT03', 1, 2, 1200000, 2400000),
(19, 'BK0018', 'RT04', 1, 1, 1600000, 4800000),
(20, 'BK0019', 'RT01', 1, 2, 500000, 1000000),
(21, 'BK0020', 'RT02', 1, 2, 800000, 2400000),
(22, 'BK0021', 'RT03', 1, 1, 1200000, 2400000),
(23, 'BK0022', 'RT04', 1, 2, 1600000, 3200000),
(24, 'BK0023', 'RT01', 1, 2, 500000, 1000000),
(25, 'BK0024', 'RT02', 1, 1, 800000, 1600000),
(26, 'BK0025', 'RT03', 1, 2, 1200000, 3600000),
(27, 'BK0026', 'RT04', 1, 2, 1600000, 3200000),
(28, 'BK0027', 'RT01', 1, 1, 500000, 1000000),
(29, 'BK0028', 'RT02', 1, 2, 800000, 1600000),
(30, 'BK0029', 'RT03', 1, 2, 1200000, 2400000),
(31, 'BK0030', 'RT04', 1, 1, 1600000, 3200000),
(32, 'BK0031', 'RT01', 1, 2, 500000, 1000000),
(33, 'BK0032', 'RT02', 1, 2, 800000, 1600000),
(34, 'BK0033', 'RT03', 1, 1, 1200000, 2400000),
(35, 'BK0034', 'RT04', 1, 2, 1600000, 3200000),
(36, 'BK0035', 'RT02', 1, 2, 800000, 1600000),
(37, 'BK0036', 'RT01', 1, 1, 500000, 1500000),
(38, 'BK0037', 'RT03', 1, 2, 1200000, 2400000),
(39, 'BK0038', 'RT04', 1, 2, 1600000, 3200000),
(40, 'BK0039', 'RT01', 1, 1, 500000, 1000000),
(41, 'BK0040', 'RT02', 1, 2, 800000, 2400000);

SET IDENTITY_INSERT BOOKING_DETAIL OFF;
GO

INSERT INTO BOOKING_SERVICE
(BookingID, ServiceID, Quantity, UnitPrice, Subtotal)
VALUES
('BK0001', 'S01', 1,  75000,  75000),
('BK0002', 'S02', 2, 250000, 500000),
('BK0003', 'S05', 1, 100000, 100000),
('BK0004', 'S07', 1, 350000, 350000),
('BK0006', 'S06', 1, 150000, 150000),
('BK0008', 'S02', 2, 250000, 500000),
('BK0009', 'S10', 1,  50000,  50000),
('BK0011', 'S01', 1, 75000, 75000),
('BK0013', 'S04', 1, 450000, 450000),
('BK0015', 'S03', 1, 150000, 150000),
('BK0017', 'S02', 2, 200000, 400000),
('BK0019', 'S10', 1, 50000, 50000),
('BK0021', 'S07', 1, 350000, 350000),
('BK0023', 'S09', 2, 350000, 700000),
('BK0025', 'S04', 1, 450000, 450000),
('BK0027', 'S01', 2, 75000, 150000),
('BK0029', 'S03', 1, 150000, 150000),
('BK0031', 'S07', 1, 350000, 350000),
('BK0032', 'S02', 1, 200000, 200000),
('BK0034', 'S09', 1, 350000, 350000),
('BK0035', 'S06', 1, 150000, 150000),
('BK0039', 'S10', 1, 50000, 50000),
('BK0040', 'S04', 1, 450000, 450000);
GO

-- Assign only active rooms that match the requested room type.
INSERT INTO ROOM_ASSIGNMENT
(BookingDetailID, RoomID, EmployeeID, AssignedAt)
VALUES
(1, 'R01', 'NV02', '2026-09-04 15:00:00'),
(2, 'R04', 'NV02', '2026-09-07 16:00:00'),
(3, 'R08', 'NV03', '2026-09-17 09:00:00'),
(9, 'R05', 'NV03', '2026-09-27 10:00:00'),
(12, 'R01', 'NV02', '2026-08-02 15:00:00'),
(13, 'R04', 'NV03', '2026-08-03 15:00:00'),
(14, 'R08', 'NV02', '2026-08-08 15:00:00'),
(15, 'R10', 'NV03', '2026-08-11 15:00:00'),
(16, 'R02', 'NV02', '2026-08-16 15:00:00'),
(17, 'R05', 'NV03', '2026-08-17 15:00:00'),
(18, 'R09', 'NV02', '2026-08-22 15:00:00'),
(19, 'R10', 'NV03', '2026-08-25 15:00:00'),
(20, 'R01', 'NV02', '2026-08-29 15:00:00'),
(21, 'R04', 'NV03', '2026-08-30 15:00:00'),
(22, 'R08', 'NV02', '2026-09-03 15:00:00'),
(23, 'R10', 'NV03', '2026-09-05 15:00:00'),
(24, 'R02', 'NV02', '2026-09-08 15:00:00'),
(25, 'R05', 'NV03', '2026-09-10 15:00:00'),
(26, 'R09', 'NV02', '2026-09-12 15:00:00'),
(27, 'R10', 'NV03', '2026-09-14 15:00:00'),
(28, 'R01', 'NV02', '2026-09-16 15:00:00'),
(29, 'R04', 'NV03', '2026-09-18 15:00:00'),
(30, 'R08', 'NV02', '2026-09-19 15:00:00'),
(31, 'R10', 'NV03', '2026-09-20 15:00:00'),
(33, 'R06', 'NV03', '2026-09-25 15:00:00'),
(35, 'R10', 'NV03', '2026-09-28 15:00:00');
GO

INSERT INTO PAYMENTMETHOD (MethodID, MethodName)
VALUES
('PT01', N'Cash'),
('PT02', N'Credit Card'),
('PT03', N'Debit Card'),
('PT04', N'Bank Transfer'),
('PT05', N'Internet Banking'),
('PT06', N'E-Wallet'),
('PT07', N'PayPal'),
('PT08', N'Others'),
('PT09', N'Internal Wallet');
GO

-- Amounts include both room and service charges.
INSERT INTO PAYMENT
(PaymentID, BookingID, PaymentTime, Amount,
 Status, MethodID, TransactionCode, PaymentType)
VALUES
('PM0001', 'BK0001', '2026-09-01 09:05:00', 1075000,
 'PAID', 'PT02', 'TXN-BK0001', 'FULL'),
('PM0002', 'BK0002', '2026-09-03 10:20:00', 2100000,
 'PAID', 'PT04', 'TXN-BK0002', 'FULL'),
('PM0003', 'BK0003', '2026-09-10 08:35:00', 750000,
 'PAID', 'PT06', 'TXN-BK0003', 'DEPOSIT'),
('PM0004', 'BK0004', '2026-09-11 14:05:00', 885000,
 'PAID', 'PT02', 'TXN-BK0004-DEPOSIT', 'DEPOSIT'),
('PM0005', 'BK0005', '2026-09-12 16:15:00', 1000000,
 'PENDING', 'PT07', NULL, 'FULL'),
('PM0006', 'BK0006', '2026-09-13 11:50:00', 3350000,
 'PAID', 'PT04', 'TXN-BK0006', 'FULL'),
('PM0007', 'BK0007', '2026-09-14 09:25:00', 3200000,
 'REFUNDED', 'PT02', 'REF-BK0007', 'FULL'),
('PM0008', 'BK0008', '2026-09-15 13:35:00', 2900000,
 'PAID', 'PT03', 'TXN-BK0008', 'FULL'),
('PM0009', 'BK0009', '2026-09-16 15:05:00', 1050000,
 'PAID', 'PT06', 'TXN-BK0009', 'FULL'),
('PM0010', 'BK0010', '2026-09-17 06:35:00', 2400000,
 'FAILED', 'PT05', NULL, 'FULL'),
('PM0011', 'BK0011', '2026-07-27 09:05:00', 1075000,
 'PAID', 'PT02', 'TXN-BK0011', 'FULL'),
('PM0012', 'BK0012', '2026-07-28 09:05:00', 2400000,
 'PAID', 'PT04', 'TXN-BK0012', 'FULL'),
('PM0013', 'BK0013', '2026-08-02 09:05:00', 2850000,
 'PAID', 'PT02', 'TXN-BK0013', 'FULL'),
('PM0014', 'BK0014', '2026-08-05 09:05:00', 3200000,
 'PAID', 'PT04', 'TXN-BK0014', 'FULL'),
('PM0015', 'BK0015', '2026-08-10 09:05:00', 1650000,
 'PAID', 'PT02', 'TXN-BK0015', 'FULL'),
('PM0016', 'BK0016', '2026-08-11 09:05:00', 2400000,
 'PAID', 'PT04', 'TXN-BK0016', 'FULL'),
('PM0017', 'BK0017', '2026-08-16 09:05:00', 2800000,
 'PAID', 'PT02', 'TXN-BK0017', 'FULL'),
('PM0018', 'BK0018', '2026-08-19 09:05:00', 4800000,
 'PAID', 'PT04', 'TXN-BK0018', 'FULL'),
('PM0019', 'BK0019', '2026-08-23 09:05:00', 1050000,
 'PAID', 'PT02', 'TXN-BK0019', 'FULL'),
('PM0020', 'BK0020', '2026-08-24 09:05:00', 2400000,
 'PAID', 'PT04', 'TXN-BK0020', 'FULL'),
('PM0021', 'BK0021', '2026-08-28 09:05:00', 2750000,
 'PAID', 'PT02', 'TXN-BK0021', 'FULL'),
('PM0022', 'BK0022', '2026-08-30 09:05:00', 3200000,
 'PAID', 'PT04', 'TXN-BK0022', 'FULL'),
('PM0023', 'BK0023', '2026-09-02 09:05:00', 1700000,
 'PAID', 'PT02', 'TXN-BK0023', 'FULL'),
('PM0024', 'BK0024', '2026-09-04 09:05:00', 1600000,
 'PAID', 'PT04', 'TXN-BK0024', 'FULL'),
('PM0025', 'BK0025', '2026-09-06 09:05:00', 4050000,
 'PAID', 'PT02', 'TXN-BK0025', 'FULL'),
('PM0026', 'BK0026', '2026-09-08 09:05:00', 3200000,
 'PAID', 'PT04', 'TXN-BK0026', 'FULL'),
('PM0027', 'BK0027', '2026-09-10 09:05:00', 1150000,
 'PAID', 'PT02', 'TXN-BK0027', 'FULL'),
('PM0028', 'BK0028', '2026-09-12 09:05:00', 1600000,
 'PAID', 'PT04', 'TXN-BK0028', 'FULL'),
('PM0029', 'BK0029', '2026-09-13 09:05:00', 2550000,
 'PAID', 'PT02', 'TXN-BK0029', 'FULL'),
('PM0030', 'BK0030', '2026-09-14 09:05:00', 3200000,
 'PAID', 'PT04', 'TXN-BK0030', 'FULL'),
('PM0031', 'BK0031', '2026-09-23 09:05:00', 405000,
 'PAID', 'PT02', 'TXN-BK0031', 'DEPOSIT'),
('PM0032', 'BK0032', '2026-09-20 09:05:00', 1800000,
 'PAID', 'PT04', 'TXN-BK0032', 'FULL'),
('PM0033', 'BK0033', '2026-09-21 09:05:00', 2400000,
 'PAID', 'PT02', 'TXN-BK0033', 'FULL'),
('PM0034', 'BK0034', '2026-09-22 09:05:00', 3550000,
 'PAID', 'PT04', 'TXN-BK0034', 'FULL'),
('PM0035', 'BK0035', '2026-09-23 09:05:00', 1750000,
 'PAID', 'PT02', 'TXN-BK0035', 'FULL'),
('PM0036', 'BK0036', '2026-09-20 09:05:00', 1500000,
 'PENDING', 'PT04', NULL, 'FULL'),
('PM0037', 'BK0037', '2026-09-21 09:05:00', 2400000,
 'FAILED', 'PT02', NULL, 'FULL'),
('PM0038', 'BK0038', '2026-09-22 09:05:00', 3200000,
 'REFUNDED', 'PT04', 'REF-BK0038', 'FULL'),
('PM0039', 'BK0039', '2026-09-23 09:05:00', 1050000,
 'PAID', 'PT02', 'TXN-BK0039', 'FULL'),
('PM0040', 'BK0040', '2026-09-20 09:05:00', 2850000,
 'PAID', 'PT04', 'TXN-BK0040', 'FULL'),
('PM0041', 'BK0004', '2026-09-12 10:00:00', 2065000,
 'PAID', 'PT04', 'TXN-BK0004-BALANCE', 'BALANCE');
GO

INSERT INTO INVOICE
(InvoiceID, InvoiceDate, TotalAmount, Status, ReplacedInvoiceID, EmployeeID, BookingID)
VALUES
('HD0005', '2026-09-01',  900000, N'Mất hiệu lực', NULL,     'NV02', 'BK0001'),
('HD0007', '2026-09-01', 1075000, N'Có hiệu lực',  'HD0005', 'NV02', 'BK0001'),
('HD0002', '2026-09-03', 2100000, N'Có hiệu lực',  NULL, NULL, 'BK0002'),
('HD0003', '2026-09-10', 2500000, N'Có hiệu lực',  NULL, NULL, 'BK0003'),
('HD0004', '2026-09-11', 2950000, N'Có hiệu lực',  NULL, NULL, 'BK0004'),
('HD0006', '2026-09-13', 3350000, N'Có hiệu lực',  NULL, NULL, 'BK0006'),
('HD0008', '2026-09-15', 2900000, N'Có hiệu lực',  NULL, NULL, 'BK0008'),
('HD0009', '2026-09-16', 1050000, N'Có hiệu lực',  NULL, NULL, 'BK0009'),
('HD0011', '2026-07-27', 1075000, N'Có hiệu lực',  NULL, NULL, 'BK0011'),
('HD0012', '2026-07-28', 2400000, N'Có hiệu lực',  NULL, NULL, 'BK0012'),
('HD0013', '2026-08-02', 2850000, N'Có hiệu lực',  NULL, NULL, 'BK0013'),
('HD0014', '2026-08-05', 3200000, N'Có hiệu lực',  NULL, NULL, 'BK0014'),
('HD0015', '2026-08-10', 1650000, N'Có hiệu lực',  NULL, NULL, 'BK0015'),
('HD0016', '2026-08-11', 2400000, N'Có hiệu lực',  NULL, NULL, 'BK0016'),
('HD0017', '2026-08-16', 2800000, N'Có hiệu lực',  NULL, NULL, 'BK0017'),
('HD0018', '2026-08-19', 4800000, N'Có hiệu lực',  NULL, NULL, 'BK0018'),
('HD0019', '2026-08-23', 1050000, N'Có hiệu lực',  NULL, NULL, 'BK0019'),
('HD0020', '2026-08-24', 2400000, N'Có hiệu lực',  NULL, NULL, 'BK0020'),
('HD0021', '2026-08-28', 2750000, N'Có hiệu lực',  NULL, NULL, 'BK0021'),
('HD0022', '2026-08-30', 3200000, N'Có hiệu lực',  NULL, NULL, 'BK0022'),
('HD0023', '2026-09-02', 1700000, N'Có hiệu lực',  NULL, NULL, 'BK0023'),
('HD0024', '2026-09-04', 1600000, N'Có hiệu lực',  NULL, NULL, 'BK0024'),
('HD0025', '2026-09-06', 4050000, N'Có hiệu lực',  NULL, NULL, 'BK0025'),
('HD0026', '2026-09-08', 3200000, N'Có hiệu lực',  NULL, NULL, 'BK0026'),
('HD0027', '2026-09-10', 1150000, N'Có hiệu lực',  NULL, NULL, 'BK0027'),
('HD0028', '2026-09-12', 1600000, N'Có hiệu lực',  NULL, NULL, 'BK0028'),
('HD0029', '2026-09-13', 2550000, N'Có hiệu lực',  NULL, NULL, 'BK0029'),
('HD0030', '2026-09-14', 3200000, N'Có hiệu lực',  NULL, NULL, 'BK0030'),
('HD0031', '2026-09-23', 1350000, N'Có hiệu lực',  NULL, NULL, 'BK0031'),
('HD0032', '2026-09-20', 1800000, N'Có hiệu lực',  NULL, NULL, 'BK0032'),
('HD0033', '2026-09-21', 2400000, N'Có hiệu lực',  NULL, NULL, 'BK0033'),
('HD0034', '2026-09-22', 3550000, N'Có hiệu lực',  NULL, NULL, 'BK0034'),
('HD0035', '2026-09-23', 1750000, N'Có hiệu lực',  NULL, NULL, 'BK0035'),
('HD0039', '2026-09-23', 1050000, N'Có hiệu lực',  NULL, NULL, 'BK0039'),
('HD0040', '2026-09-20', 2850000, N'Có hiệu lực',  NULL, NULL, 'BK0040');
GO

INSERT INTO COMPLAINT (Title, Content, Status, CustomerID)
VALUES (
    N'Yêu cầu hỗ trợ đặt phòng',
    N'Tôi muốn kiểm tra lại thông tin phòng đã được sắp xếp cho booking của mình.',
    N'Chưa xử lý',
    'KH03'
);
GO

/*******************************************************************************
   AVAILABILITY PROCEDURE AND INDEXES
********************************************************************************/

-- Availability is based on requested room-type quantities, even before staff
-- assigns concrete rooms. Only unexpired pending payments count as holds.
CREATE PROCEDURE SP_GET_ROOM_TYPE_AVAILABILITY
    @CheckInDate DATE,
    @CheckOutDate DATE
AS
BEGIN
    SET NOCOUNT ON;

    IF @CheckOutDate <= @CheckInDate
    BEGIN
        RAISERROR (
            'Check-out date must be later than check-in date.',
            16,
            1
        );
        RETURN;
    END;

    SELECT
        rt.RoomTypeID,
        rt.TypeName,
        rt.Capacity,
        rt.Price,
        rt.RoomTypeImage,
        COUNT(CASE WHEN r.Status = 'ACTIVE' THEN 1 END) AS TotalActiveRooms,
        COALESCE(reserved.ReservedQuantity, 0) AS ReservedRooms,
        COUNT(CASE WHEN r.Status = 'ACTIVE' THEN 1 END)
            - COALESCE(reserved.ReservedQuantity, 0) AS AvailableRooms
    FROM ROOM_TYPE AS rt
    LEFT JOIN ROOM AS r
        ON r.RoomTypeID = rt.RoomTypeID
    OUTER APPLY (
        SELECT SUM(bd.Quantity) AS ReservedQuantity
        FROM BOOKING_DETAIL AS bd
        INNER JOIN BOOKING AS b
            ON b.BookingID = bd.BookingID
        WHERE bd.RoomTypeID = rt.RoomTypeID
          AND (
              (
                  b.BookingStatus = 'PENDING_PAYMENT'
                  AND b.PaymentDeadline > GETDATE()
              )
              OR b.BookingStatus IN (
                  'CONFIRMED',
                  'ASSIGNED',
                  'CHECKED_IN'
              )
          )
          AND b.CheckInDate < @CheckOutDate
          AND b.CheckOutDate > @CheckInDate
    ) AS reserved
    GROUP BY
        rt.RoomTypeID,
        rt.TypeName,
        rt.Capacity,
        rt.Price,
        rt.RoomTypeImage,
        reserved.ReservedQuantity
    ORDER BY rt.Price;
END;
GO

CREATE INDEX IX_BOOKING_Date_Status
ON BOOKING (CheckInDate, CheckOutDate, BookingStatus);
GO

CREATE INDEX IX_BOOKING_Customer
ON BOOKING (CustomerID, BookingDate);
GO

CREATE INDEX IX_BOOKING_PaymentTimeout
ON BOOKING (BookingStatus, PaymentDeadline);
GO

CREATE INDEX IX_BOOKING_DETAIL_RoomType
ON BOOKING_DETAIL (RoomTypeID, BookingID);
GO

CREATE INDEX IX_ROOM_RoomType_Status
ON ROOM (RoomTypeID, Status);
GO

CREATE INDEX IX_ROOM_ASSIGNMENT_Room
ON ROOM_ASSIGNMENT (RoomID, BookingDetailID);
GO

CREATE INDEX IX_PAYMENT_Booking_Status
ON PAYMENT (BookingID, Status);
GO

USE QLKS;
GO

UPDATE EMPLOYEE 
SET Position = N'Customer service' 
WHERE EmployeeID IN ('NV05', 'NV06', 'NV07');

ALTER TABLE COMPLAINT 
ADD ReplyMessage NVARCHAR(MAX) NULL;

ALTER TABLE COMPLAINT 
ADD EmployeeID CHAR(6) NULL;
GO

/*******************************************************************************
   INITIALIZE NEW FIELDS AND KEEP THE 40 EXISTING SEED BOOKINGS
********************************************************************************/
UPDATE BOOKING SET PaymentOption = 'DEPOSIT'
WHERE BookingID IN ('BK0003', 'BK0004', 'BK0031');
GO

UPDATE b SET FirstPaidAt = p.FirstPaidAt, BookingAmountAtFirstPayment = b.TotalAmount
FROM BOOKING AS b
CROSS APPLY (
    SELECT MIN(PaymentTime) AS FirstPaidAt FROM PAYMENT
    WHERE BookingID = b.BookingID AND Status IN ('PAID', 'REFUNDED')
) AS p
WHERE p.FirstPaidAt IS NOT NULL;
GO

-- Historical seed refunds happened one hour after their first successful payment.
UPDATE BOOKING SET CancelledAt = DATEADD(HOUR, 1, FirstPaidAt),
    CancellationReason = 'CUSTOMER_REQUEST',
    CancelledByUserID = (SELECT UserID FROM CUSTOMER WHERE CUSTOMER.CustomerID = BOOKING.CustomerID)
WHERE BookingStatus = 'CANCELLED';
GO

INSERT BOOKING_REFUND(BookingID, CustomerID, WalletID, Amount, RefundedAt)
SELECT b.BookingID, b.CustomerID, w.WalletID, SUM(p.Amount), b.CancelledAt
FROM BOOKING AS b JOIN CUSTOMER_WALLET AS w ON w.CustomerID = b.CustomerID
JOIN PAYMENT AS p ON p.BookingID = b.BookingID AND p.Status = 'REFUNDED'
GROUP BY b.BookingID, b.CustomerID, w.WalletID, b.CancelledAt;

INSERT REFUND_PAYMENT(RefundID, BookingID, PaymentID, Amount)
SELECT r.RefundID, p.BookingID, p.PaymentID, p.Amount
FROM BOOKING_REFUND AS r JOIN PAYMENT AS p ON p.BookingID = r.BookingID
WHERE p.Status = 'REFUNDED';

INSERT WALLET_TRANSACTION(WalletID, TransactionType, Amount, TransactionTime, RefundID)
SELECT WalletID, 'REFUND_CREDIT', Amount, RefundedAt, RefundID FROM BOOKING_REFUND;
GO

-- Initialize historic cancellation windows; expired windows are reset on access.
UPDATE c SET WindowStartedAt = b.CancelledAt, ConsecutiveCancellationCount = 1,
    CancellationCountInWindow = 1, UpdatedAt = b.CancelledAt
FROM USER_BOOKING_CONTROL AS c
JOIN CUSTOMER AS cu ON cu.UserID = c.UserID
JOIN BOOKING AS b ON b.CustomerID = cu.CustomerID AND b.BookingStatus = 'CANCELLED';
GO

ALTER TABLE BOOKING ADD CONSTRAINT CK_BOOKING_CancellationData CHECK (
    (BookingStatus = 'CANCELLED' AND CancelledAt IS NOT NULL AND CancellationReason IS NOT NULL)
    OR (BookingStatus <> 'CANCELLED' AND CancelledAt IS NULL
        AND CancellationReason IS NULL AND CancelledByUserID IS NULL)
);
GO

/*******************************************************************************
   VIEWS AND TRANSACTIONAL APPLICATION API
   Java must pass the authenticated session's UserID; never take it from a form.
   SP_RECORD_PAYMENT is called only after verifying external provider callbacks,
   or after staff has collected cash. A pending attempt is not a successful payment.
   SQL Server 2016+; execute with SSMS/sqlcmd because GO is a batch separator.
********************************************************************************/
CREATE VIEW V_WALLET_BALANCE AS
SELECT w.WalletID, w.CustomerID, w.CreatedAt,
    CAST(COALESCE(SUM(CASE WHEN t.TransactionType = 'REFUND_CREDIT'
        THEN t.Amount ELSE -t.Amount END), 0) AS DECIMAL(12,2)) AS Balance
FROM CUSTOMER_WALLET AS w LEFT JOIN WALLET_TRANSACTION AS t ON t.WalletID = w.WalletID
GROUP BY w.WalletID, w.CustomerID, w.CreatedAt;
GO

CREATE VIEW V_BOOKING_PAYMENT_SUMMARY AS
SELECT b.BookingID, b.CustomerID, b.BookingStatus, b.PaymentOption, b.TotalAmount,
    b.BookingAmountAtFirstPayment, b.FirstPaidAt, b.RefundDeadline,
    b.CancelledAt, b.CancellationReason,
    CAST(CASE WHEN b.PaymentOption = 'DEPOSIT'
        THEN ROUND(COALESCE(b.BookingAmountAtFirstPayment, b.TotalAmount) * 0.30, 2)
        ELSE COALESCE(b.BookingAmountAtFirstPayment, b.TotalAmount) END AS DECIMAL(12,2)) AS InitialRequiredAmount,
    p.TotalPaidAmount, COALESCE(r.Amount, 0) AS RefundedAmount,
    p.TotalPaidAmount - COALESCE(r.Amount, 0) AS NetPaidAmount,
    CAST(CASE WHEN b.BookingStatus = 'CANCELLED' THEN 0
        WHEN b.TotalAmount > p.TotalPaidAmount THEN b.TotalAmount - p.TotalPaidAmount
        ELSE 0 END AS DECIMAL(12,2)) AS RemainingAmount,
    CASE WHEN r.RefundID IS NOT NULL THEN 'REFUNDED'
        WHEN b.BookingStatus = 'CANCELLED' AND p.TotalPaidAmount > 0 THEN 'NO_REFUND'
        WHEN p.TotalPaidAmount = 0 THEN 'UNPAID'
        WHEN p.TotalPaidAmount >= b.TotalAmount THEN 'FULLY_PAID'
        ELSE 'DEPOSIT_PAID' END AS PaymentStatus
FROM BOOKING AS b
OUTER APPLY (
    SELECT CAST(COALESCE(SUM(Amount), 0) AS DECIMAL(12,2)) AS TotalPaidAmount
    FROM PAYMENT WHERE BookingID = b.BookingID AND Status IN ('PAID', 'REFUNDED')
) AS p LEFT JOIN BOOKING_REFUND AS r ON r.BookingID = b.BookingID;
GO

CREATE VIEW V_USER_BOOKING_ACCESS AS
SELECT c.UserID, cu.CustomerID,
    CASE WHEN c.WindowStartedAt IS NULL OR DATEADD(DAY, 1, c.WindowStartedAt) <= SYSDATETIME()
        THEN 0 ELSE c.ConsecutiveCancellationCount END AS ConsecutiveCancellationCount,
    CASE WHEN c.WindowStartedAt IS NULL OR DATEADD(DAY, 1, c.WindowStartedAt) <= SYSDATETIME()
        THEN 0 ELSE c.CancellationCountInWindow END AS CancellationCountInWindow,
    CASE WHEN c.WindowStartedAt IS NULL OR DATEADD(DAY, 1, c.WindowStartedAt) <= SYSDATETIME()
        THEN 0 ELSE c.PenaltyLevel END AS PenaltyLevel,
    CASE WHEN DATEADD(DAY, 1, c.WindowStartedAt) > SYSDATETIME()
        AND c.BookingLockedUntil > SYSDATETIME() THEN c.BookingLockedUntil END AS BookingLockedUntil,
    pending.BookingID AS PendingBookingID,
    CAST(CASE WHEN pending.BookingID IS NOT NULL OR (
        DATEADD(DAY, 1, c.WindowStartedAt) > SYSDATETIME()
        AND c.BookingLockedUntil > SYSDATETIME()) THEN 0 ELSE 1 END AS BIT) AS CanCreateBooking
FROM USER_BOOKING_CONTROL AS c JOIN CUSTOMER AS cu ON cu.UserID = c.UserID
LEFT JOIN BOOKING AS pending ON pending.CustomerID = cu.CustomerID AND pending.BookingStatus = 'PENDING_PAYMENT';
GO

-- Serialize mutations for this small, single-hotel project. The lock is owned
-- by the transaction and is released at commit/rollback. This also protects
-- room availability and wallet debits across different customer accounts.
CREATE PROCEDURE SP_HBMS_ACQUIRE_WRITE_LOCK AS
BEGIN
    SET NOCOUNT ON;
    DECLARE @Result INT;
    EXEC @Result = sys.sp_getapplock @Resource = N'HBMS:booking-wallet-write',
        @LockMode = 'Exclusive', @LockOwner = 'Transaction', @LockTimeout = 10000;
    IF @Result < 0 THROW 51000, 'System busy. Please retry the request.', 1;
END;
GO

CREATE PROCEDURE SP_RESET_BOOKING_CONTROL @UserID INT = NULL AS
BEGIN
    SET NOCOUNT ON;
    UPDATE USER_BOOKING_CONTROL
    SET WindowStartedAt = NULL, ConsecutiveCancellationCount = 0,
        CancellationCountInWindow = 0, PenaltyLevel = 0,
        BookingLockedUntil = NULL, UpdatedAt = SYSDATETIME()
    WHERE (@UserID IS NULL OR UserID = @UserID)
      AND DATEADD(DAY, 1, WindowStartedAt) <= SYSDATETIME();
END;
GO

CREATE PROCEDURE SP_CANCEL_BOOKING
    @BookingID CHAR(6),
    @ActorUserID INT = NULL,
    @Reason VARCHAR(30) = 'CUSTOMER_REQUEST',
    @ReturnResult BIT = 1
AS
BEGIN
    SET NOCOUNT ON;
    SET XACT_ABORT ON;
    BEGIN TRY
        BEGIN TRANSACTION;
        EXEC SP_HBMS_ACQUIRE_WRITE_LOCK;
        DECLARE @Now DATETIME2(3) = SYSDATETIME(), @CustomerID CHAR(6), @OwnerUserID INT,
            @Status VARCHAR(30), @FirstPaidAt DATETIME2(3), @Deadline DATETIME,
            @Paid DECIMAL(12,2), @WalletID BIGINT, @RefundID BIGINT,
            @Streak INT, @Level TINYINT, @Minutes INT, @PreviouslyLockedUntil DATETIME2(3);
        SELECT @CustomerID = b.CustomerID, @OwnerUserID = c.UserID,
            @Status = b.BookingStatus, @FirstPaidAt = b.FirstPaidAt, @Deadline = b.PaymentDeadline
        FROM BOOKING AS b WITH (UPDLOCK, HOLDLOCK)
        JOIN CUSTOMER AS c ON c.CustomerID = b.CustomerID WHERE b.BookingID = @BookingID;
        IF @CustomerID IS NULL THROW 51001, 'Booking not found.', 1;
        IF @Reason IS NULL OR @Reason NOT IN ('CUSTOMER_REQUEST', 'STAFF_REQUEST', 'PAYMENT_TIMEOUT')
            THROW 51002, 'Invalid cancellation reason.', 1;
        IF @Reason = 'CUSTOMER_REQUEST' AND (@ActorUserID IS NULL OR @ActorUserID <> @OwnerUserID)
            THROW 51003, 'You cannot cancel another customer booking.', 1;
        IF @Reason = 'STAFF_REQUEST' AND NOT EXISTS (
            SELECT 1 FROM USERS WHERE UserID = @ActorUserID AND Role IN ('Admin', 'Staff'))
            THROW 51004, 'Only staff or admin can cancel on behalf of customers.', 1;
        IF @Reason = 'PAYMENT_TIMEOUT' AND (@ActorUserID IS NOT NULL OR @FirstPaidAt IS NOT NULL
            OR @Deadline IS NULL OR @Deadline > @Now)
            THROW 51005, 'Payment timeout cancellation is not applicable.', 1;

        -- Retrying a cancellation never credits the wallet or increments the streak twice.
        IF @Status = 'CANCELLED'
        BEGIN
            COMMIT TRANSACTION;
            IF @ReturnResult = 1 SELECT * FROM V_BOOKING_PAYMENT_SUMMARY WHERE BookingID = @BookingID;
            RETURN;
        END;
        IF @Status NOT IN ('PENDING_PAYMENT', 'CONFIRMED', 'ASSIGNED')
            THROW 51006, 'Only bookings before check-in can be cancelled.', 1;

        UPDATE BOOKING SET BookingStatus = 'CANCELLED', CancelledAt = @Now,
            CancellationReason = @Reason, CancelledByUserID = @ActorUserID
        WHERE BookingID = @BookingID;
        UPDATE PAYMENT SET Status = 'FAILED' WHERE BookingID = @BookingID AND Status = 'PENDING';
        UPDATE INVOICE SET Status = N'Mất hiệu lực' WHERE BookingID = @BookingID AND Status = N'Có hiệu lực';

        SELECT @Paid = COALESCE(SUM(Amount), 0) FROM PAYMENT
        WHERE BookingID = @BookingID AND Status = 'PAID';
        IF @Paid > 0 AND @FirstPaidAt IS NOT NULL AND @Now < DATEADD(HOUR, 24, @FirstPaidAt)
        BEGIN
            SELECT @WalletID = WalletID FROM CUSTOMER_WALLET WHERE CustomerID = @CustomerID;
            IF @WalletID IS NULL THROW 51007, 'Customer wallet not found.', 1;
            INSERT BOOKING_REFUND(BookingID, CustomerID, WalletID, Amount, RefundedAt)
            VALUES (@BookingID, @CustomerID, @WalletID, @Paid, @Now);
            SET @RefundID = CONVERT(BIGINT, SCOPE_IDENTITY());
            INSERT REFUND_PAYMENT(RefundID, BookingID, PaymentID, Amount)
            SELECT @RefundID, BookingID, PaymentID, Amount FROM PAYMENT
            WHERE BookingID = @BookingID AND Status = 'PAID';
            INSERT WALLET_TRANSACTION(WalletID, TransactionType, Amount, TransactionTime, RefundID)
            VALUES (@WalletID, 'REFUND_CREDIT', @Paid, @Now, @RefundID);
            UPDATE PAYMENT SET Status = 'REFUNDED' WHERE BookingID = @BookingID AND Status = 'PAID';
        END;

        EXEC SP_RESET_BOOKING_CONTROL @UserID = @OwnerUserID;
        -- Customers may cancel previously paid bookings during a lock; this
        -- still refunds eligible payments but does not escalate the lock early.
        SELECT @PreviouslyLockedUntil = BookingLockedUntil FROM USER_BOOKING_CONTROL WHERE UserID = @OwnerUserID;
        UPDATE USER_BOOKING_CONTROL SET WindowStartedAt = COALESCE(WindowStartedAt, @Now),
            ConsecutiveCancellationCount = ConsecutiveCancellationCount
                + CASE WHEN BookingLockedUntil > @Now THEN 0 ELSE 1 END,
            CancellationCountInWindow = CancellationCountInWindow + 1, UpdatedAt = @Now
        WHERE UserID = @OwnerUserID;
        SELECT @Streak = ConsecutiveCancellationCount, @Level = PenaltyLevel
        FROM USER_BOOKING_CONTROL WHERE UserID = @OwnerUserID;
        IF (@PreviouslyLockedUntil IS NULL OR @PreviouslyLockedUntil <= @Now)
            AND @Streak >= CASE WHEN @Level = 0 THEN 3 WHEN @Level = 1 THEN 2 ELSE 1 END
        BEGIN
            SET @Level = CASE WHEN @Level < 3 THEN @Level + 1 ELSE 3 END;
            SET @Minutes = CASE WHEN @Level = 1 THEN 5 WHEN @Level = 2 THEN 15 ELSE 60 END;
            UPDATE USER_BOOKING_CONTROL SET PenaltyLevel = @Level,
                ConsecutiveCancellationCount = 0, BookingLockedUntil = DATEADD(MINUTE, @Minutes, @Now)
            WHERE UserID = @OwnerUserID;
            INSERT BOOKING_LOCK_HISTORY(UserID, TriggerBookingID, PenaltyLevel, LockedAt, LockedUntil, DurationMinutes)
            VALUES (@OwnerUserID, @BookingID, @Level, @Now, DATEADD(MINUTE, @Minutes, @Now), @Minutes);
        END;
        COMMIT TRANSACTION;
        IF @ReturnResult = 1 SELECT * FROM V_BOOKING_PAYMENT_SUMMARY WHERE BookingID = @BookingID;
    END TRY
    BEGIN CATCH
        IF @@TRANCOUNT > 0 ROLLBACK TRANSACTION;
        THROW;
    END CATCH;
END;
GO

CREATE PROCEDURE SP_EXPIRE_PENDING_BOOKINGS
    @CustomerID CHAR(6) = NULL, @ReturnResult BIT = 1
AS
BEGIN
    SET NOCOUNT ON;
    SET XACT_ABORT ON;
    DECLARE @BookingID CHAR(6), @Count INT = 0;
    BEGIN TRY
        BEGIN TRANSACTION;
        EXEC SP_HBMS_ACQUIRE_WRITE_LOCK;
        -- No result sets from each cancellation. Process oldest bookings first.
        DECLARE expired CURSOR LOCAL FAST_FORWARD FOR
            SELECT BookingID FROM BOOKING WHERE BookingStatus = 'PENDING_PAYMENT'
                AND PaymentDeadline <= SYSDATETIME() AND (@CustomerID IS NULL OR CustomerID = @CustomerID)
            ORDER BY BookingDate, BookingID;
        OPEN expired;
        FETCH NEXT FROM expired INTO @BookingID;
        WHILE @@FETCH_STATUS = 0
        BEGIN
            EXEC SP_CANCEL_BOOKING @BookingID = @BookingID, @ActorUserID = NULL,
                @Reason = 'PAYMENT_TIMEOUT', @ReturnResult = 0;
            SET @Count += 1;
            FETCH NEXT FROM expired INTO @BookingID;
        END;
        CLOSE expired;
        DEALLOCATE expired;
        EXEC SP_RESET_BOOKING_CONTROL;
        COMMIT TRANSACTION;
        IF @ReturnResult = 1 SELECT @Count AS ExpiredBookingCount;
        RETURN @Count;
    END TRY
    BEGIN CATCH
        IF CURSOR_STATUS('local', 'expired') >= 0 CLOSE expired;
        IF CURSOR_STATUS('local', 'expired') >= -1 DEALLOCATE expired;
        IF @@TRANCOUNT > 0 ROLLBACK TRANSACTION;
        THROW;
    END CATCH;
END;
GO

CREATE TYPE BOOKING_ROOM_INPUT AS TABLE (
    RoomTypeID CHAR(4) NOT NULL PRIMARY KEY,
    Quantity INT NOT NULL CHECK (Quantity > 0),
    GuestCount INT NOT NULL CHECK (GuestCount > 0)
);
GO

CREATE PROCEDURE SP_CREATE_BOOKING
    @BookingID CHAR(6), @CustomerID CHAR(6), @ActorUserID INT,
    @CheckInDate DATE, @CheckOutDate DATE, @PaymentOption VARCHAR(10),
    @Rooms BOOKING_ROOM_INPUT READONLY,
    @PaymentDeadline DATETIME -- Supply your existing timeout policy; no new timeout is imposed.
AS
BEGIN
    SET NOCOUNT ON;
    SET XACT_ABORT ON;
    DECLARE @OwnerUserID INT;
    SELECT @OwnerUserID = UserID FROM CUSTOMER WHERE CustomerID = @CustomerID;
    IF @OwnerUserID IS NULL THROW 51010, 'Customer not found.', 1;
    IF @ActorUserID IS NULL OR (@ActorUserID <> @OwnerUserID AND NOT EXISTS (
        SELECT 1 FROM USERS WHERE UserID = @ActorUserID AND Role IN ('Admin', 'Staff')))
        THROW 51011, 'Not authorized to create this booking.', 1;
    IF @PaymentOption IS NULL OR @PaymentOption NOT IN ('DEPOSIT', 'FULL')
        THROW 51012, 'Choose DEPOSIT (30 percent) or FULL.', 1;
    IF @CheckInDate IS NULL OR @CheckOutDate IS NULL OR @CheckOutDate <= @CheckInDate
        THROW 51013, 'Invalid stay dates.', 1;
    IF @PaymentDeadline IS NULL OR @PaymentDeadline <= SYSDATETIME()
        THROW 51014, 'Payment deadline must be in the future.', 1;
    IF NOT EXISTS (SELECT 1 FROM @Rooms) THROW 51015, 'At least one room type is required.', 1;

    IF @@TRANCOUNT <> 0 THROW 51021, 'Call SP_CREATE_BOOKING without an outer transaction.', 1;

    -- Commit expired holds before creating; a rejected new booking must not undo
    -- a timeout cancellation or its anti-spam penalty.
    EXEC SP_EXPIRE_PENDING_BOOKINGS @CustomerID = @CustomerID, @ReturnResult = 0;
    BEGIN TRY
        BEGIN TRANSACTION;
        EXEC SP_HBMS_ACQUIRE_WRITE_LOCK;
        EXEC SP_RESET_BOOKING_CONTROL @UserID = @OwnerUserID;
        IF @PaymentDeadline <= SYSDATETIME() THROW 51014, 'Payment deadline must be in the future.', 1;
        IF EXISTS (SELECT 1 FROM USER_BOOKING_CONTROL WHERE UserID = @OwnerUserID
            AND BookingLockedUntil > SYSDATETIME())
            THROW 51016, 'Booking temporarily locked. Check V_USER_BOOKING_ACCESS for unlock time.', 1;
        IF EXISTS (SELECT 1 FROM BOOKING WHERE CustomerID = @CustomerID AND BookingStatus = 'PENDING_PAYMENT')
            THROW 51017, 'Pay the existing booking deposit/full amount or cancel it before creating another.', 1;
        IF EXISTS (SELECT 1 FROM @Rooms AS i LEFT JOIN ROOM_TYPE AS rt ON rt.RoomTypeID = i.RoomTypeID
            WHERE rt.RoomTypeID IS NULL OR i.GuestCount > i.Quantity * rt.Capacity)
            THROW 51018, 'Invalid room type or guest capacity.', 1;
        IF EXISTS (
            SELECT 1 FROM @Rooms AS i
            CROSS APPLY (SELECT COUNT(*) AS ActiveRooms FROM ROOM
                WHERE RoomTypeID = i.RoomTypeID AND Status = 'ACTIVE') AS available
            OUTER APPLY (
                SELECT COALESCE(SUM(d.Quantity), 0) AS ReservedRooms
                FROM BOOKING_DETAIL AS d JOIN BOOKING AS b ON b.BookingID = d.BookingID
                WHERE d.RoomTypeID = i.RoomTypeID
                  AND (b.BookingStatus IN ('CONFIRMED', 'ASSIGNED', 'CHECKED_IN')
                    OR (b.BookingStatus = 'PENDING_PAYMENT' AND b.PaymentDeadline > SYSDATETIME()))
                  AND b.CheckInDate < @CheckOutDate AND b.CheckOutDate > @CheckInDate
            ) AS reserved
            WHERE i.Quantity > available.ActiveRooms - reserved.ReservedRooms
        ) THROW 51019, 'Not enough available rooms for the requested dates.', 1;
        INSERT BOOKING(BookingID, BookingDate, PaymentDeadline, CheckInDate, CheckOutDate,
            BookingStatus, CustomerID, PaymentOption)
        VALUES (@BookingID, SYSDATETIME(), @PaymentDeadline, @CheckInDate, @CheckOutDate,
            'PENDING_PAYMENT', @CustomerID, @PaymentOption);
        INSERT BOOKING_DETAIL(BookingID, RoomTypeID, Quantity, GuestCount, UnitPrice, Subtotal)
        SELECT @BookingID, i.RoomTypeID, i.Quantity, i.GuestCount, rt.Price,
            i.Quantity * rt.Price * DATEDIFF(DAY, @CheckInDate, @CheckOutDate)
        FROM @Rooms AS i JOIN ROOM_TYPE AS rt ON rt.RoomTypeID = i.RoomTypeID;
        IF EXISTS (SELECT 1 FROM BOOKING WHERE BookingID = @BookingID AND TotalAmount <= 0)
            THROW 51020, 'Booking amount must be positive.', 1;
        COMMIT TRANSACTION;
        SELECT * FROM V_BOOKING_PAYMENT_SUMMARY WHERE BookingID = @BookingID;
    END TRY
    BEGIN CATCH
        IF @@TRANCOUNT > 0 ROLLBACK TRANSACTION;
        THROW;
    END CATCH;
END;
GO

CREATE PROCEDURE SP_RECORD_PAYMENT
    @PaymentID CHAR(6), @BookingID CHAR(6), @ActorUserID INT,
    @PaymentType VARCHAR(10), @Amount DECIMAL(12,2), @MethodID CHAR(4),
    @TransactionCode VARCHAR(100) = NULL
AS
BEGIN
    SET NOCOUNT ON;
    SET XACT_ABORT ON;
    BEGIN TRY
        BEGIN TRANSACTION;
        EXEC SP_HBMS_ACQUIRE_WRITE_LOCK;
        DECLARE @Now DATETIME2(3) = SYSDATETIME(), @CustomerID CHAR(6), @OwnerUserID INT,
            @Status VARCHAR(30), @Option VARCHAR(10), @FirstPaidAt DATETIME2(3),
            @Total DECIMAL(12,2), @Paid DECIMAL(12,2), @Required DECIMAL(12,2),
            @Deadline DATETIME, @WalletID BIGINT, @Balance DECIMAL(12,2);
        SELECT @CustomerID = b.CustomerID, @OwnerUserID = c.UserID, @Status = b.BookingStatus,
            @Option = b.PaymentOption, @FirstPaidAt = b.FirstPaidAt, @Total = b.TotalAmount,
            @Deadline = b.PaymentDeadline
        FROM BOOKING AS b WITH (UPDLOCK, HOLDLOCK)
        JOIN CUSTOMER AS c ON c.CustomerID = b.CustomerID WHERE b.BookingID = @BookingID;
        IF @CustomerID IS NULL THROW 51030, 'Booking not found.', 1;
        IF @ActorUserID IS NULL OR (@ActorUserID <> @OwnerUserID AND NOT EXISTS (
            SELECT 1 FROM USERS WHERE UserID = @ActorUserID AND Role IN ('Admin', 'Staff')))
            THROW 51031, 'Not authorized to pay for this booking.', 1;
        IF @Amount IS NULL OR @Amount <= 0 THROW 51032, 'Payment amount must be positive.', 1;
        IF @PaymentType IS NULL OR @PaymentType NOT IN ('DEPOSIT', 'FULL', 'BALANCE')
            THROW 51033, 'Invalid payment type.', 1;
        IF NOT EXISTS (SELECT 1 FROM PAYMENTMETHOD WHERE MethodID = @MethodID)
            THROW 51034, 'Payment method not found.', 1;
        IF @MethodID = 'PT09' SET @TransactionCode = CONCAT('WALLET-', RTRIM(@PaymentID));
        IF @MethodID = 'PT01'
        BEGIN
            IF NOT EXISTS (SELECT 1 FROM USERS WHERE UserID = @ActorUserID AND Role IN ('Admin', 'Staff'))
                THROW 51035, 'Cash collection must be recorded by staff.', 1;
            IF @TransactionCode IS NULL SET @TransactionCode = CONCAT('CASH-', RTRIM(@PaymentID));
        END;
        IF @TransactionCode IS NULL OR LEN(LTRIM(RTRIM(@TransactionCode))) = 0
            THROW 51036, 'Successful payment requires a unique transaction code.', 1;

        -- Provider retries after payment or cancellation are idempotent.
        IF EXISTS (SELECT 1 FROM PAYMENT WHERE PaymentID = @PaymentID AND Status IN ('PAID', 'REFUNDED'))
        BEGIN
            IF NOT EXISTS (SELECT 1 FROM PAYMENT WHERE PaymentID = @PaymentID AND BookingID = @BookingID
                AND PaymentType = @PaymentType AND Amount = @Amount AND MethodID = @MethodID
                AND TransactionCode = @TransactionCode)
                THROW 51037, 'Payment ID already used with different payment data.', 1;
            COMMIT TRANSACTION;
            SELECT * FROM V_BOOKING_PAYMENT_SUMMARY WHERE BookingID = @BookingID;
            RETURN;
        END;
        IF EXISTS (SELECT 1 FROM PAYMENT WHERE TransactionCode = @TransactionCode AND PaymentID <> @PaymentID)
            THROW 51038, 'Transaction code already recorded under another payment ID.', 1;
        IF @Status IN ('CANCELLED', 'CHECKED_OUT')
            THROW 51039, 'Cannot record a new payment on a cancelled or completed booking.', 1;
        IF @Status = 'PENDING_PAYMENT' AND @Deadline <= @Now
            THROW 51040, 'Payment deadline has expired. Do not collect money for this booking.', 1;
        SELECT @Paid = COALESCE(SUM(Amount), 0) FROM PAYMENT WHERE BookingID = @BookingID AND Status = 'PAID';
        IF @FirstPaidAt IS NULL
        BEGIN
            IF @Status <> 'PENDING_PAYMENT' OR @Paid <> 0
                THROW 51041, 'Booking has an inconsistent initial payment state.', 1;
            IF (@Option = 'DEPOSIT' AND @PaymentType <> 'DEPOSIT')
                OR (@Option = 'FULL' AND @PaymentType <> 'FULL')
                THROW 51042, 'Payment type must match the selected booking option.', 1;
            SET @Required = CASE WHEN @Option = 'DEPOSIT' THEN ROUND(@Total * 0.30, 2) ELSE @Total END;
        END
        ELSE
        BEGIN
            IF @PaymentType <> 'BALANCE' THROW 51043, 'Initial payment already made; use BALANCE.', 1;
            SET @Required = @Total - @Paid;
        END;
        IF @Required <= 0 OR @Amount <> @Required
            THROW 51044, 'Pay exactly the initial required amount or the current remaining balance.', 1;
        IF EXISTS (SELECT 1 FROM PAYMENT WHERE PaymentID = @PaymentID AND (
            BookingID <> @BookingID OR PaymentType <> @PaymentType OR Amount <> @Amount))
            THROW 51045, 'Payment attempt does not match this booking and amount.', 1;
        IF @MethodID = 'PT09'
        BEGIN
            SELECT @WalletID = WalletID, @Balance = Balance FROM V_WALLET_BALANCE WHERE CustomerID = @CustomerID;
            IF @WalletID IS NULL OR @Balance < @Amount
                THROW 51046, 'Insufficient internal wallet balance.', 1;
        END;
        IF EXISTS (SELECT 1 FROM PAYMENT WHERE PaymentID = @PaymentID)
            UPDATE PAYMENT SET Status = 'PAID', PaymentTime = @Now,
                MethodID = @MethodID, TransactionCode = @TransactionCode WHERE PaymentID = @PaymentID;
        ELSE
            INSERT PAYMENT(PaymentID, BookingID, PaymentType, PaymentTime, Amount, Status, MethodID, TransactionCode)
            VALUES (@PaymentID, @BookingID, @PaymentType, @Now, @Amount, 'PAID', @MethodID, @TransactionCode);
        IF @MethodID = 'PT09'
            INSERT WALLET_TRANSACTION(WalletID, TransactionType, Amount, TransactionTime, PaymentID)
            VALUES (@WalletID, 'PAYMENT_DEBIT', @Amount, @Now, @PaymentID);
        IF @FirstPaidAt IS NULL
            UPDATE BOOKING SET FirstPaidAt = @Now, BookingAmountAtFirstPayment = @Total,
                BookingStatus = 'CONFIRMED', PaymentDeadline = NULL WHERE BookingID = @BookingID;
        COMMIT TRANSACTION;
        SELECT * FROM V_BOOKING_PAYMENT_SUMMARY WHERE BookingID = @BookingID;
    END TRY
    BEGIN CATCH
        IF @@TRANCOUNT > 0 ROLLBACK TRANSACTION;
        THROW;
    END CATCH;
END;
GO

-- Existing room/service total triggers still calculate TotalAmount. Room details
-- are fixed after creation; additional service charges increase the balance due.
CREATE PROCEDURE SP_ADD_BOOKING_SERVICE
    @BookingID CHAR(6), @ServiceID CHAR(4), @Quantity INT, @ActorUserID INT,
    @UnitPrice DECIMAL(12,2) = NULL
AS
BEGIN
    SET NOCOUNT ON;
    SET XACT_ABORT ON;
    BEGIN TRY
        BEGIN TRANSACTION;
        EXEC SP_HBMS_ACQUIRE_WRITE_LOCK;
        IF @Quantity IS NULL OR @Quantity <= 0 THROW 51050, 'Service quantity must be positive.', 1;
        IF NOT EXISTS (SELECT 1 FROM BOOKING AS b JOIN CUSTOMER AS c ON c.CustomerID = b.CustomerID
            WHERE b.BookingID = @BookingID AND b.BookingStatus IN ('PENDING_PAYMENT', 'CONFIRMED', 'ASSIGNED', 'CHECKED_IN')
            AND (c.UserID = @ActorUserID OR EXISTS (
                SELECT 1 FROM USERS WHERE UserID = @ActorUserID AND Role IN ('Admin', 'Staff'))))
            THROW 51051, 'Booking not found, not authorized, or no longer accepts services.', 1;
        IF EXISTS (SELECT 1 FROM BOOKING WHERE BookingID = @BookingID AND BookingStatus = 'PENDING_PAYMENT'
            AND PaymentDeadline <= SYSDATETIME()) THROW 51052, 'Pending booking has expired.', 1;
        DECLARE @CatalogPrice DECIMAL(12,2);
        SELECT @CatalogPrice = UnitPrice FROM SERVICE WHERE ServiceID = @ServiceID;
        IF @CatalogPrice IS NULL THROW 51053, 'Service not found.', 1;
        IF @UnitPrice IS NOT NULL AND @UnitPrice <> @CatalogPrice AND NOT EXISTS (
            SELECT 1 FROM USERS WHERE UserID = @ActorUserID AND Role IN ('Admin', 'Staff'))
            THROW 51054, 'Only staff can set a service price different from the catalog.', 1;
        SET @UnitPrice = COALESCE(@UnitPrice, @CatalogPrice);
        IF @UnitPrice < 0 THROW 51055, 'Service price cannot be negative.', 1;
        IF EXISTS (SELECT 1 FROM BOOKING_SERVICE WHERE BookingID = @BookingID AND ServiceID = @ServiceID)
        BEGIN
            IF EXISTS (SELECT 1 FROM BOOKING_SERVICE WHERE BookingID = @BookingID AND ServiceID = @ServiceID
                AND UnitPrice <> @UnitPrice) THROW 51056, 'Existing service has a different price snapshot.', 1;
            UPDATE BOOKING_SERVICE SET Quantity = Quantity + @Quantity,
                Subtotal = (Quantity + @Quantity) * UnitPrice
            WHERE BookingID = @BookingID AND ServiceID = @ServiceID;
        END
        ELSE
            INSERT BOOKING_SERVICE(BookingID, ServiceID, Quantity, UnitPrice, Subtotal)
            VALUES (@BookingID, @ServiceID, @Quantity, @UnitPrice, @Quantity * @UnitPrice);
        COMMIT TRANSACTION;
        SELECT * FROM V_BOOKING_PAYMENT_SUMMARY WHERE BookingID = @BookingID;
    END TRY
    BEGIN CATCH
        IF @@TRANCOUNT > 0 ROLLBACK TRANSACTION;
        THROW;
    END CATCH;
END;
GO

CREATE PROCEDURE SP_SET_BOOKING_STATUS
    @BookingID CHAR(6), @NewStatus VARCHAR(30), @ActorUserID INT
AS
BEGIN
    SET NOCOUNT ON;
    SET XACT_ABORT ON;
    BEGIN TRY
        BEGIN TRANSACTION;
        EXEC SP_HBMS_ACQUIRE_WRITE_LOCK;
        IF NOT EXISTS (SELECT 1 FROM USERS WHERE UserID = @ActorUserID AND Role IN ('Admin', 'Staff'))
            THROW 51060, 'Only staff/admin can manage stay status.', 1;
        DECLARE @OldStatus VARCHAR(30), @OwnerUserID INT;
        SELECT @OldStatus = b.BookingStatus, @OwnerUserID = c.UserID
        FROM BOOKING AS b JOIN CUSTOMER AS c ON c.CustomerID = b.CustomerID WHERE BookingID = @BookingID;
        IF @OldStatus IS NULL THROW 51061, 'Booking not found.', 1;
        IF @NewStatus IS NULL OR @NewStatus NOT IN ('ASSIGNED', 'CHECKED_IN', 'CHECKED_OUT')
            THROW 51062, 'Use payment/cancellation procedures for other status changes.', 1;
        IF @NewStatus = @OldStatus
        BEGIN
            COMMIT TRANSACTION;
            RETURN;
        END;
        IF NOT ((@OldStatus = 'CONFIRMED' AND @NewStatus = 'ASSIGNED')
            OR (@OldStatus = 'ASSIGNED' AND @NewStatus = 'CHECKED_IN')
            OR (@OldStatus = 'CHECKED_IN' AND @NewStatus = 'CHECKED_OUT'))
            THROW 51063, 'Invalid stay status transition.', 1;
        IF @NewStatus IN ('ASSIGNED', 'CHECKED_IN') AND EXISTS (
            SELECT 1 FROM BOOKING_DETAIL AS d WHERE d.BookingID = @BookingID
            AND d.Quantity <> (SELECT COUNT(*) FROM ROOM_ASSIGNMENT AS a WHERE a.BookingDetailID = d.BookingDetailID))
            THROW 51064, 'Assign all requested rooms first.', 1;
        IF @NewStatus = 'CHECKED_OUT' AND EXISTS (
            SELECT 1 FROM V_BOOKING_PAYMENT_SUMMARY WHERE BookingID = @BookingID AND RemainingAmount > 0)
            THROW 51065, 'Pay the remaining balance before check-out.', 1;
        UPDATE BOOKING SET BookingStatus = @NewStatus WHERE BookingID = @BookingID;
        IF @NewStatus = 'CHECKED_OUT'
        BEGIN
            EXEC SP_RESET_BOOKING_CONTROL @UserID = @OwnerUserID;
            UPDATE USER_BOOKING_CONTROL SET ConsecutiveCancellationCount = 0, UpdatedAt = SYSDATETIME()
            WHERE UserID = @OwnerUserID;
        END;
        COMMIT TRANSACTION;
    END TRY
    BEGIN CATCH
        IF @@TRANCOUNT > 0 ROLLBACK TRANSACTION;
        THROW;
    END CATCH;
END;
GO

CREATE PROCEDURE SP_ASSIGN_ROOM
    @BookingDetailID INT, @RoomID CHAR(3), @ActorUserID INT
AS
BEGIN
    SET NOCOUNT ON;
    SET XACT_ABORT ON;
    BEGIN TRY
        BEGIN TRANSACTION;
        EXEC SP_HBMS_ACQUIRE_WRITE_LOCK;
        DECLARE @EmployeeID CHAR(6);
        SELECT @EmployeeID = EmployeeID FROM EMPLOYEE
        WHERE UserID = @ActorUserID AND Position = N'Receptionist';
        IF @EmployeeID IS NULL THROW 51066, 'Only receptionists can assign rooms.', 1;
        IF NOT EXISTS (SELECT 1 FROM BOOKING_DETAIL AS d JOIN BOOKING AS b ON b.BookingID = d.BookingID
            WHERE d.BookingDetailID = @BookingDetailID AND b.BookingStatus IN ('CONFIRMED', 'ASSIGNED'))
            THROW 51067, 'Booking must be confirmed and not checked in to assign rooms.', 1;
        INSERT ROOM_ASSIGNMENT(BookingDetailID, RoomID, EmployeeID)
        VALUES (@BookingDetailID, @RoomID, @EmployeeID);
        COMMIT TRANSACTION;
    END TRY
    BEGIN CATCH
        IF @@TRANCOUNT > 0 ROLLBACK TRANSACTION;
        THROW;
    END CATCH;
END;
GO

-- Read this before enabling the application login:
-- * Use a dedicated login mapped to HBMS_APP_ROLE, never sa/db_owner.
-- * The login must not belong to another role granting bypass permissions.
-- * Old DAO code using INSERT/UPDATE on the protected tables must call these
--   procedures instead. Application authentication still checks actors by role.
-- * Schedule SP_EXPIRE_PENDING_BOOKINGS periodically in Java/SQL Agent and call
--   it before loading availability/booking access. SQL does not run on a timer.
-- * JDBC writes should use autocommit for each complete procedure call; do not
--   wrap SP_CREATE_BOOKING in an outer transaction (expiry must remain committed).
CREATE ROLE HBMS_APP_ROLE AUTHORIZATION dbo;
GO
DENY INSERT, UPDATE, DELETE ON BOOKING TO HBMS_APP_ROLE;
DENY INSERT, UPDATE, DELETE ON BOOKING_DETAIL TO HBMS_APP_ROLE;
DENY INSERT, UPDATE, DELETE ON BOOKING_SERVICE TO HBMS_APP_ROLE;
DENY INSERT, UPDATE, DELETE ON ROOM_ASSIGNMENT TO HBMS_APP_ROLE;
DENY INSERT, UPDATE, DELETE ON PAYMENT TO HBMS_APP_ROLE;
DENY INSERT, UPDATE, DELETE ON CUSTOMER_WALLET TO HBMS_APP_ROLE;
DENY INSERT, UPDATE, DELETE ON WALLET_TRANSACTION TO HBMS_APP_ROLE;
DENY INSERT, UPDATE, DELETE ON BOOKING_REFUND TO HBMS_APP_ROLE;
DENY INSERT, UPDATE, DELETE ON REFUND_PAYMENT TO HBMS_APP_ROLE;
DENY INSERT, UPDATE, DELETE ON USER_BOOKING_CONTROL TO HBMS_APP_ROLE;
DENY INSERT, UPDATE, DELETE ON BOOKING_LOCK_HISTORY TO HBMS_APP_ROLE;
GRANT EXECUTE ON SP_CREATE_BOOKING TO HBMS_APP_ROLE;
GRANT EXECUTE ON SP_RECORD_PAYMENT TO HBMS_APP_ROLE;
GRANT EXECUTE ON SP_CANCEL_BOOKING TO HBMS_APP_ROLE;
GRANT EXECUTE ON SP_EXPIRE_PENDING_BOOKINGS TO HBMS_APP_ROLE;
GRANT EXECUTE ON SP_ADD_BOOKING_SERVICE TO HBMS_APP_ROLE;
GRANT EXECUTE ON SP_SET_BOOKING_STATUS TO HBMS_APP_ROLE;
GRANT EXECUTE ON SP_ASSIGN_ROOM TO HBMS_APP_ROLE;
GRANT EXECUTE ON SP_GET_ROOM_TYPE_AVAILABILITY TO HBMS_APP_ROLE;
GRANT EXECUTE, REFERENCES ON TYPE::BOOKING_ROOM_INPUT TO HBMS_APP_ROLE;
GRANT SELECT ON SCHEMA::dbo TO HBMS_APP_ROLE;
GO

/*******************************************************************************
   FINAL CONSISTENCY CHECKS
********************************************************************************/

IF EXISTS (
    SELECT 1
    FROM BOOKING AS b
    CROSS APPLY (
        SELECT
            COALESCE((
                SELECT SUM(bd.Subtotal)
                FROM BOOKING_DETAIL AS bd
                WHERE bd.BookingID = b.BookingID
            ), 0)
            +
            COALESCE((
                SELECT SUM(bs.Subtotal)
                FROM BOOKING_SERVICE AS bs
                WHERE bs.BookingID = b.BookingID
            ), 0) AS ExpectedTotal
    ) AS totals
    WHERE b.TotalAmount <> totals.ExpectedTotal
)
BEGIN
    RAISERROR (
        'Seed booking totals are inconsistent.',
        16,
        1
    );
    RETURN;
END;
GO

IF EXISTS (
    SELECT 1
    FROM INVOICE AS i
    INNER JOIN BOOKING AS b
        ON b.BookingID = i.BookingID
    WHERE i.Status = N'Có hiệu lực'
      AND i.TotalAmount <> b.TotalAmount
)
BEGIN
    RAISERROR (
        'Seed invoice totals are inconsistent.',
        16,
        1
    );
    RETURN;
END;
GO

IF (SELECT COUNT(*) FROM BOOKING) <> 40
   OR EXISTS (
       SELECT 1
       FROM BOOKING AS b
       WHERE NOT EXISTS (
           SELECT 1 FROM BOOKING_DETAIL AS bd
           WHERE bd.BookingID = b.BookingID
       )
   )
BEGIN
    RAISERROR ('Expected 40 bookings, each with a room detail.', 16, 1);
    RETURN;
END;
GO


/*******************************************************************************
   VALIDATE SPLIT PAYMENTS, REFUNDS AND INTERNAL WALLET SEED
********************************************************************************/
IF EXISTS (
    SELECT 1 FROM V_BOOKING_PAYMENT_SUMMARY
    WHERE BookingStatus IN ('CONFIRMED', 'ASSIGNED', 'CHECKED_IN', 'CHECKED_OUT')
      AND (FirstPaidAt IS NULL OR TotalPaidAmount < InitialRequiredAmount OR TotalPaidAmount > TotalAmount)
) THROW 51100, 'Seed confirmed bookings must have at least their required initial payment.', 1;
GO
IF EXISTS (
    SELECT 1 FROM V_BOOKING_PAYMENT_SUMMARY
    WHERE BookingStatus = 'CHECKED_OUT' AND RemainingAmount <> 0
) THROW 51101, 'Seed checked-out bookings must be fully paid.', 1;
GO
IF EXISTS (
    SELECT 1 FROM PAYMENT AS p JOIN BOOKING AS b ON b.BookingID = p.BookingID
    WHERE p.Status IN ('PAID', 'REFUNDED') AND (
        (p.PaymentType = 'DEPOSIT' AND (b.PaymentOption <> 'DEPOSIT'
            OR p.Amount <> ROUND(b.BookingAmountAtFirstPayment * 0.30, 2)))
        OR (p.PaymentType = 'FULL' AND (b.PaymentOption <> 'FULL'
            OR p.Amount <> b.BookingAmountAtFirstPayment))
    )
) THROW 51102, 'Seed initial payment types or amounts are inconsistent.', 1;
GO
IF EXISTS (
    SELECT 1 FROM BOOKING AS b OUTER APPLY (
        SELECT MIN(PaymentTime) AS FirstPaidAt FROM PAYMENT
        WHERE BookingID = b.BookingID AND Status IN ('PAID', 'REFUNDED')
    ) AS p
    WHERE (b.FirstPaidAt IS NULL AND p.FirstPaidAt IS NOT NULL)
       OR (b.FirstPaidAt IS NOT NULL AND p.FirstPaidAt IS NULL)
       OR b.FirstPaidAt <> p.FirstPaidAt
) THROW 51103, 'Seed first-payment timestamp is inconsistent.', 1;
GO
IF EXISTS (
    SELECT 1 FROM BOOKING_REFUND AS r JOIN BOOKING AS b ON b.BookingID = r.BookingID
    WHERE b.BookingStatus <> 'CANCELLED' OR b.FirstPaidAt IS NULL
       OR b.CancelledAt < b.FirstPaidAt OR b.CancelledAt >= b.RefundDeadline
       OR r.RefundedAt <> b.CancelledAt
       OR r.Amount <> (SELECT COALESCE(SUM(Amount), 0) FROM REFUND_PAYMENT WHERE RefundID = r.RefundID)
       OR r.Amount <> (SELECT COALESCE(SUM(Amount), 0) FROM PAYMENT
           WHERE BookingID = b.BookingID AND Status IN ('PAID', 'REFUNDED'))
       OR NOT EXISTS (SELECT 1 FROM WALLET_TRANSACTION AS t WHERE t.RefundID = r.RefundID
           AND t.WalletID = r.WalletID AND t.Amount = r.Amount AND t.TransactionType = 'REFUND_CREDIT')
) THROW 51104, 'Seed refunds must equal all captured payments and credit the correct wallet within 24h.', 1;
GO
IF EXISTS (
    SELECT 1 FROM REFUND_PAYMENT AS r JOIN PAYMENT AS p ON p.PaymentID = r.PaymentID
    WHERE p.Status <> 'REFUNDED' OR p.Amount <> r.Amount
) OR EXISTS (
    SELECT 1 FROM PAYMENT AS p WHERE p.Status = 'REFUNDED'
    AND NOT EXISTS (SELECT 1 FROM REFUND_PAYMENT AS r WHERE r.PaymentID = p.PaymentID)
) THROW 51105, 'Seed refunded payments require matching refund details.', 1;
GO
IF EXISTS (SELECT 1 FROM V_WALLET_BALANCE WHERE Balance < 0)
    THROW 51106, 'Internal wallet cannot have a negative balance.', 1;
IF (SELECT COUNT(*) FROM CUSTOMER_WALLET) <> (SELECT COUNT(*) FROM CUSTOMER)
    THROW 51107, 'Every customer must have exactly one internal wallet.', 1;
IF (SELECT COUNT(*) FROM USER_BOOKING_CONTROL) <> (SELECT COUNT(*) FROM CUSTOMER)
    THROW 51108, 'Every customer account must have booking-control state.', 1;
GO

PRINT N'QLKS created: 40 bookings, deposit/full payment, 24h wallet refunds and anti-spam booking locks.';
GO

/*******************************************************************************
   APPLICATION INTEGRATION / EXAMPLES (COMMENTED OUT; NO EXTRA SEED WRITES)
   Use authenticated UserID from session. Amount and payment options must come
   from the server. SQL procedures do not authenticate payment-provider receipts.
********************************************************************************/
-- Read available actions / balance / deposit and final-payment amount:
-- EXEC SP_EXPIRE_PENDING_BOOKINGS;
-- SELECT * FROM V_USER_BOOKING_ACCESS WHERE UserID = 32;
-- SELECT * FROM V_WALLET_BALANCE WHERE CustomerID = 'KH01';
-- SELECT * FROM V_BOOKING_PAYMENT_SUMMARY WHERE BookingID IN ('BK0003','BK0004','BK0031');
-- BK0003: 750,000 deposit; 1,750,000 remaining.
-- BK0004: 885,000 deposit + 2,065,000 balance; first-payment time stays 2026-09-11 14:05.
-- BK0031: 405,000 deposit; 945,000 remaining.
-- KH07 and KH38: 3,200,000 credited to each internal wallet from historical refunds.

-- DECLARE @Rooms BOOKING_ROOM_INPUT;
-- INSERT @Rooms(RoomTypeID, Quantity, GuestCount) VALUES ('RT01', 1, 2);
-- DECLARE @Deadline DATETIME = DATEADD(MINUTE, 15, GETDATE()); -- existing example timeout only
-- EXEC SP_CREATE_BOOKING @BookingID='BK0041', @CustomerID='KH01', @ActorUserID=32,
--     @CheckInDate='2026-12-01', @CheckOutDate='2026-12-03', @PaymentOption='DEPOSIT',
--     @Rooms=@Rooms, @PaymentDeadline=@Deadline;
-- EXEC SP_RECORD_PAYMENT @PaymentID='PM0042', @BookingID='BK0041', @ActorUserID=32,
--     @PaymentType='DEPOSIT', @Amount=300000, @MethodID='PT04', @TransactionCode='DEMO-BK0041-DEPOSIT';
-- EXEC SP_RECORD_PAYMENT @PaymentID='PM0043', @BookingID='BK0041', @ActorUserID=32,
--     @PaymentType='BALANCE', @Amount=700000, @MethodID='PT04', @TransactionCode='DEMO-BK0041-BALANCE';
-- EXEC SP_CANCEL_BOOKING @BookingID='BK0041', @ActorUserID=32;
-- EXEC SP_CANCEL_BOOKING @BookingID='BK0041', @ActorUserID=32; -- retry: no additional refund/streak
-- SELECT * FROM V_WALLET_BALANCE WHERE CustomerID='KH01'; -- exactly 1,000,000 refund credit

-- To connect the Java login after updating DAO code:
-- CREATE USER hbms_app FOR LOGIN hbms_app;
-- ALTER ROLE HBMS_APP_ROLE ADD MEMBER hbms_app;
-- Grant account/profile and other unrelated module permissions separately.
-- Do not add hbms_app to db_owner or db_datawriter to bypass the intended API.

USE QLKS;
GO
SET ANSI_NULLS ON;
SET QUOTED_IDENTIFIER ON;
GO
CREATE OR ALTER PROCEDURE dbo.SP_CHECKOUT_WITH_INVOICE
    @BookingID CHAR(6), @ActorUserID INT
AS
BEGIN
    SET NOCOUNT ON;
    SET XACT_ABORT ON;
    IF @@TRANCOUNT <> 0 THROW 51109, 'Call checkout without an outer transaction.', 1;
    BEGIN TRY
        BEGIN TRANSACTION;
        EXEC dbo.SP_HBMS_ACQUIRE_WRITE_LOCK;
        IF NOT EXISTS (SELECT 1 FROM dbo.USERS WHERE UserID = @ActorUserID AND Role IN ('Staff', 'Admin'))
            THROW 51100, 'Only staff/admin can issue invoices.', 1;
        DECLARE @EmployeeID CHAR(6);
        SELECT @EmployeeID = EmployeeID FROM dbo.EMPLOYEE WHERE UserID = @ActorUserID;
        IF @EmployeeID IS NULL THROW 51101, 'The account is not linked to an employee profile.', 1;
        DECLARE @Status VARCHAR(30), @Total DECIMAL(12,2), @FirstPaidAt DATETIME2(3), @Paid DECIMAL(38,2);
        SELECT @Status = BookingStatus, @Total = TotalAmount, @FirstPaidAt = FirstPaidAt
        FROM dbo.BOOKING WITH (UPDLOCK, HOLDLOCK) WHERE BookingID = @BookingID;
        IF @Status IS NULL THROW 51102, 'Booking not found.', 1;
        IF @Status NOT IN ('CHECKED_IN', 'CHECKED_OUT') THROW 51103, 'Only checked-in/out bookings can issue an invoice.', 1;
        SELECT @Paid = COALESCE(SUM(Amount), 0) FROM dbo.PAYMENT WHERE BookingID = @BookingID AND Status = 'PAID';
        IF @FirstPaidAt IS NULL OR @Paid <= 0 THROW 51105, 'The booking has no successful payment.', 1;
        IF @Paid < @Total THROW 51104, 'Complete the remaining payment before checkout.', 1;
        DECLARE @InvoiceID CHAR(6), @InvoiceTotal DECIMAL(12,2), @Created BIT = 0;
        SELECT @InvoiceID = InvoiceID, @InvoiceTotal = TotalAmount
        FROM dbo.INVOICE WITH (UPDLOCK, HOLDLOCK) WHERE BookingID = @BookingID AND Status = N'Có hiệu lực';
        IF @InvoiceID IS NOT NULL AND @InvoiceTotal <> @Total
            THROW 51106, 'The active invoice total does not match the booking total.', 1;
        IF @InvoiceID IS NULL
        BEGIN
            DECLARE @NextNumber INT;
            SELECT @NextNumber = COALESCE(MAX(TRY_CONVERT(INT, SUBSTRING(RTRIM(InvoiceID), 3, 4))), 0) + 1
            FROM dbo.INVOICE WITH (UPDLOCK, HOLDLOCK) WHERE RTRIM(InvoiceID) LIKE 'HD[0-9][0-9][0-9][0-9]';
            IF @NextNumber > 9999 THROW 51107, 'The invoice ID range is exhausted.', 1;
            SET @InvoiceID = 'HD' + RIGHT('0000' + CONVERT(VARCHAR(4), @NextNumber), 4);
            INSERT dbo.INVOICE (InvoiceID, InvoiceDate, TotalAmount, Status, EmployeeID, BookingID)
            VALUES (@InvoiceID, CAST(SYSDATETIME() AS DATE), @Total, N'Có hiệu lực', @EmployeeID, @BookingID);
            SET @Created = 1;
        END;
        IF @Status = 'CHECKED_IN'
            EXEC dbo.SP_SET_BOOKING_STATUS @BookingID = @BookingID, @NewStatus = 'CHECKED_OUT', @ActorUserID = @ActorUserID;
        COMMIT TRANSACTION;
        SELECT @InvoiceID AS InvoiceID, @BookingID AS BookingID, @Total AS TotalAmount, @Created AS InvoiceCreated;
    END TRY
    BEGIN CATCH
        IF @@TRANCOUNT > 0 ROLLBACK TRANSACTION;
        THROW;
    END CATCH;
END;
GO
DENY INSERT, UPDATE, DELETE ON dbo.INVOICE TO HBMS_APP_ROLE;
GRANT EXECUTE ON dbo.SP_CHECKOUT_WITH_INVOICE TO HBMS_APP_ROLE;
GO