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
    CCCD VARCHAR(20) NULL,
    PassportNumber VARCHAR(30) NULL,
    NationalityID VARCHAR(50) NULL,
    UserID INT NOT NULL,

    CONSTRAINT PK_CUSTOMER PRIMARY KEY (CustomerID),
    CONSTRAINT UQ_CUSTOMER_User UNIQUE (UserID),
    CONSTRAINT UQ_CUSTOMER_Email UNIQUE (Email),
    CONSTRAINT FK_CUSTOMER_USERS
        FOREIGN KEY (UserID) REFERENCES USERS(UserID),
    CONSTRAINT FK_CUSTOMER_NATIONALITY
        FOREIGN KEY (NationalityID) REFERENCES NATIONALITY(NationalityID),
    CONSTRAINT CK_CUSTOMER_Identity
        CHECK (CCCD IS NOT NULL OR PassportNumber IS NOT NULL)
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
    PaymentTime DATETIME NULL,
    Amount DECIMAL(12,2) NOT NULL,
    Status VARCHAR(30) NOT NULL,
    MethodID CHAR(4) NULL,
    TransactionCode VARCHAR(100) NULL,

    CONSTRAINT PK_PAYMENT PRIMARY KEY (PaymentID),
    CONSTRAINT FK_PAYMENT_BOOKING
        FOREIGN KEY (BookingID) REFERENCES BOOKING(BookingID),
    CONSTRAINT FK_PAYMENT_METHOD
        FOREIGN KEY (MethodID) REFERENCES PAYMENTMETHOD(MethodID),
    CONSTRAINT CK_PAYMENT_Amount CHECK (Amount >= 0),
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
(CustomerID, FullName, Phone, Email, Address, CCCD, PassportNumber, NationalityID, UserID)
VALUES
('KH01', N'Phạm Minh Tuấn', '0903456789', 'tuan.pham@gmail.com', N'Quận 1, TP.HCM', '079085012345', NULL, 'N01', 32),
('KH02', N'Nguyễn Tuyết Mai', '0912888999', 'maituyet92@yahoo.com', N'Quận Cầu Giấy, Hà Nội', '001192005678', NULL, 'N01', 33),
('KH03', N'Lê Hoàng Nam', '0987111222', 'namlh@gmail.com', N'Quận 7, TP.HCM', '040090001234', 'VN889911', 'N01', 34),
('KH04', N'Trần Thu Hà', '0356123456', 'hatran@fpt.com.vn', N'Quận Tây Hồ, Hà Nội', '038195009876', 'VN223344', 'N01', 35),
('KH05', N'Robert Harrison', '0775123456', 'robert.h@outlook.com', N'London, United Kingdom', NULL, 'B12345678', 'N13', 36),
('KH06', N'Chen Wei', '0933444555', 'chenwei88@qq.com', N'Beijing, China', NULL, 'G55667788', 'N03', 37),
('KH07', N'Hans Müller', '0888777666', 'hans.m@gmail.de', N'Berlin, Germany', NULL, 'C88990011', 'N15', 38),
('KH08', N'Kim Ji-won', '0944555666', 'jiwon.kim@naver.com', N'Seoul, South Korea', NULL, 'M11223344', 'N04', 39),
('KH09', N'Jean Dupont', '0766112233', 'jean.dupont@france.fr', N'Paris, France', NULL, 'P11224455', 'N14', 40),
('KH10', N'Đặng Phương Thảo', '0399123456', 'thaophuong@gmail.com', N'Quận Hải Châu, Đà Nẵng', '052199004455', NULL, 'N01', 41),

('KH11', N'Vũ Đình Tùng', '0988123123', 'tung.vu@gmail.com', N'Quận 3, TP.HCM', '079085112233', NULL, 'N01', 42),
('KH12', N'Lê Thị Thu Hương', '0977234234', 'huongle99@yahoo.com', N'Quận Đống Đa, Hà Nội', '001192112233', NULL, 'N01', 43),
('KH13', N'Nguyễn Quang Hải', '0912345345', 'hai.nq@fpt.edu.vn', N'Ninh Kiều, Cần Thơ', '092095001122', 'VN887766', 'N01', 44),
('KH14', N'Phan Thanh Bình', '0933456456', 'binhpt@gmail.com', N'Quận 1, TP.HCM', '079090004455', NULL, 'N01', 45),
('KH15', N'Đỗ Mỹ Linh', '0944567567', 'mylinh.do@gmail.com', N'Quận Hoàn Kiếm, Hà Nội', '001195007788', NULL, 'N01', 46),
('KH16', N'Bùi Tiến Dũng', '0966678678', 'dung.bui@viettel.com.vn', N'Thạch Thất, Hà Nội', '001097009900', 'VN112233', 'N01', 47),
('KH17', N'Trần Phương Anh', '0888789789', 'phuonganh.t@yahoo.com', N'Quận Sơn Trà, Đà Nẵng', '048199001122', NULL, 'N01', 48),
('KH18', N'Lý Nhã Kỳ', '0901890890', 'nhaky.ly@gmail.com', N'Quận 7, TP.HCM', '079185002233', 'VN556677', 'N01', 49),
('KH19', N'Hoàng Lệ Thu', '0922901901', 'thuhl@outlook.com', N'Biên Hòa, Đồng Nai', '075192003344', NULL, 'N01', 50),
('KH20', N'Ngô Thanh Vân', '0933012012', 'van.ngo@gmail.com', N'Quận Phú Nhuận, TP.HCM', '079080005566', NULL, 'N01', 51),
('KH21', N'Võ Hoàng Yến', '0944123123', 'hoangyen.vo@gmail.com', N'Quận 4, TP.HCM', '079189006677', 'VN990011', 'N01', 52),
('KH22', N'Đinh Ngọc Diệp', '0955234234', 'ngocdiep.dinh@yahoo.com', N'Quận Bình Thạnh, TP.HCM', '079088007788', NULL, 'N01', 53),
('KH23', N'Lương Xuân Trường', '0966345345', 'truong.lx@hagl.com.vn', N'Pleiku, Gia Lai', '064095008899', 'VN445566', 'N01', 54),
('KH24', N'Cao Thái Sơn', '0977456456', 'soncao@gmail.com', N'Quận 10, TP.HCM', '079085009900', NULL, 'N01', 55),
('KH25', N'Hồ Ngọc Hà', '0988567567', 'hongocha@gmail.com', N'Quận 2, TP.HCM', '079184001122', 'VN778899', 'N01', 56),
('KH26', N'Mai Phương Thúy', '0999678678', 'thuymp@gmail.com', N'Quận Cầu Giấy, Hà Nội', '001188002233', NULL, 'N01', 57),
('KH27', N'Dương Trương Thiên Lý', '0901789789', 'thienly.dt@gmail.com', N'Đồng Tháp', '087189003344', NULL, 'N01', 58),
('KH28', N'Tăng Thanh Hà', '0912890890', 'hathanh.tang@gmail.com', N'Quận 2, TP.HCM', '079186004455', NULL, 'N01', 59),
('KH29', N'Phạm Hương', '0923901901', 'huong.pham@gmail.com', N'Hải Phòng', '031191005566', 'VN223388', 'N01', 60),
('KH30', N'Nguyễn Thúc Thùy Tiên', '0934012012', 'thuytien.nt@gmail.com', N'Quận Gò Vấp, TP.HCM', '079198006677', NULL, 'N01', 61),
('KH31', N'Lê Quốc Bảo', '0945123123', 'baolq@gmail.com', N'Quận Tân Bình, TP.HCM', '079095007788', NULL, 'N01', 62),
('KH32', N'Trần Kim Chi', '0956234234', 'kimchi.tran@yahoo.com', N'Quận Thanh Xuân, Hà Nội', '001193008899', NULL, 'N01', 63),
('KH33', N'Phan Đình Phùng', '0967345345', 'phungpd@gmail.com', N'Vinh, Nghệ An', '040090009900', 'VN334455', 'N01', 64),
('KH34', N'Vũ Cát Tường', '0978456456', 'cattuong.vu@gmail.com', N'Long Xuyên, An Giang', '089192001122', NULL, 'N01', 65),
('KH35', N'Đỗ Trọng Hiếu', '0989567567', 'hieudo@gmail.com', N'Quận Hai Bà Trưng, Hà Nội', '001094002233', NULL, 'N01', 66),
('KH36', N'Bùi Anh Tuấn', '0990678678', 'anhtuan.bui@gmail.com', N'Quận 1, TP.HCM', '079091003344', 'VN998877', 'N01', 67),
('KH37', N'Lê Minh Sơn', '0902789789', 'sonlm@gmail.com', N'Bắc Ninh', '027085004455', NULL, 'N01', 68),
('KH38', N'Hoàng Thùy Linh', '0913890890', 'thuylinh.hoang@gmail.com', N'Quận Ba Đình, Hà Nội', '001188005566', NULL, 'N01', 69),
('KH39', N'Nguyễn Trần Trung Quân', '0924901901', 'trungquan.nt@gmail.com', N'Quận Đống Đa, Hà Nội', '001092006677', 'VN665544', 'N01', 70),
('KH40', N'Phạm Tiến Dũng', '0935012012', 'dungpt@gmail.com', N'Nha Trang, Khánh Hòa', '056096007788', NULL, 'N01', 71),

('KH41', N'Emily Johnson', NULL, 'emily.j@yahoo.com', N'Los Angeles, USA', NULL, '987654321', 'N11', 72),
('KH42', N'Michael Brown', NULL, 'michael.b@outlook.com', N'Chicago, USA', NULL, '456789123', 'N11', 73),
('KH43', N'Li Na', NULL, 'lina.beijing@qq.com', N'Shanghai, China', NULL, 'E11223344', 'N03', 74),
('KH44', N'Wang Lei', NULL, 'wanglei88@wechat.com', N'Guangzhou, China', NULL, 'E99887766', 'N03', 75),
('KH45', N'Park Min-young', NULL, 'minyoung.park@naver.com', N'Busan, South Korea', NULL, 'M55667788', 'N04', 76),
('KH46', N'Lee Jong-suk', NULL, 'jongsuk.lee@daum.net', N'Incheon, South Korea', NULL, 'M99887766', 'N04', 77),
('KH47', N'Sato Kenji', NULL, 'kenji.sato@yahoo.co.jp', N'Tokyo, Japan', NULL, 'TR1234567', 'N02', 78),
('KH48', N'Tanaka Yuki', NULL, 'yuki.tanaka@gmail.com', N'Osaka, Japan', NULL, 'TR7654321', 'N02', 79),
('KH49', N'Anna Kowalski', NULL, 'anna.k@gmail.com', N'Warsaw, Poland', NULL, 'AB1122334', 'N20', 80),
('KH50', N'David Silva', NULL, 'david.silva@hotmail.com', N'Madrid, Spain', NULL, 'ES1234567', 'N17', 81);
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
('PT08', N'Others');
GO

-- Amounts include both room and service charges.
INSERT INTO PAYMENT
(PaymentID, BookingID, PaymentTime, Amount,
 Status, MethodID, TransactionCode)
VALUES
('PM0001', 'BK0001', '2026-09-01 09:05:00', 1075000,
 'PAID', 'PT02', 'TXN-BK0001'),
('PM0002', 'BK0002', '2026-09-03 10:20:00', 2100000,
 'PAID', 'PT04', 'TXN-BK0002'),
('PM0003', 'BK0003', '2026-09-10 08:35:00', 2500000,
 'PAID', 'PT06', 'TXN-BK0003'),
('PM0004', 'BK0004', '2026-09-11 14:05:00', 2950000,
 'PAID', 'PT02', 'TXN-BK0004'),
('PM0005', 'BK0005', '2026-09-12 16:15:00', 1000000,
 'PENDING', 'PT07', NULL),
('PM0006', 'BK0006', '2026-09-13 11:50:00', 3350000,
 'PAID', 'PT04', 'TXN-BK0006'),
('PM0007', 'BK0007', '2026-09-14 09:25:00', 3200000,
 'REFUNDED', 'PT02', 'REF-BK0007'),
('PM0008', 'BK0008', '2026-09-15 13:35:00', 2900000,
 'PAID', 'PT03', 'TXN-BK0008'),
('PM0009', 'BK0009', '2026-09-16 15:05:00', 1050000,
 'PAID', 'PT06', 'TXN-BK0009'),
('PM0010', 'BK0010', '2026-09-17 06:35:00', 2400000,
 'FAILED', 'PT05', NULL),
('PM0011', 'BK0011', '2026-07-27 09:05:00', 1075000,
 'PAID', 'PT02', 'TXN-BK0011'),
('PM0012', 'BK0012', '2026-07-28 09:05:00', 2400000,
 'PAID', 'PT04', 'TXN-BK0012'),
('PM0013', 'BK0013', '2026-08-02 09:05:00', 2850000,
 'PAID', 'PT02', 'TXN-BK0013'),
('PM0014', 'BK0014', '2026-08-05 09:05:00', 3200000,
 'PAID', 'PT04', 'TXN-BK0014'),
('PM0015', 'BK0015', '2026-08-10 09:05:00', 1650000,
 'PAID', 'PT02', 'TXN-BK0015'),
('PM0016', 'BK0016', '2026-08-11 09:05:00', 2400000,
 'PAID', 'PT04', 'TXN-BK0016'),
('PM0017', 'BK0017', '2026-08-16 09:05:00', 2800000,
 'PAID', 'PT02', 'TXN-BK0017'),
('PM0018', 'BK0018', '2026-08-19 09:05:00', 4800000,
 'PAID', 'PT04', 'TXN-BK0018'),
('PM0019', 'BK0019', '2026-08-23 09:05:00', 1050000,
 'PAID', 'PT02', 'TXN-BK0019'),
('PM0020', 'BK0020', '2026-08-24 09:05:00', 2400000,
 'PAID', 'PT04', 'TXN-BK0020'),
('PM0021', 'BK0021', '2026-08-28 09:05:00', 2750000,
 'PAID', 'PT02', 'TXN-BK0021'),
('PM0022', 'BK0022', '2026-08-30 09:05:00', 3200000,
 'PAID', 'PT04', 'TXN-BK0022'),
('PM0023', 'BK0023', '2026-09-02 09:05:00', 1700000,
 'PAID', 'PT02', 'TXN-BK0023'),
('PM0024', 'BK0024', '2026-09-04 09:05:00', 1600000,
 'PAID', 'PT04', 'TXN-BK0024'),
('PM0025', 'BK0025', '2026-09-06 09:05:00', 4050000,
 'PAID', 'PT02', 'TXN-BK0025'),
('PM0026', 'BK0026', '2026-09-08 09:05:00', 3200000,
 'PAID', 'PT04', 'TXN-BK0026'),
('PM0027', 'BK0027', '2026-09-10 09:05:00', 1150000,
 'PAID', 'PT02', 'TXN-BK0027'),
('PM0028', 'BK0028', '2026-09-12 09:05:00', 1600000,
 'PAID', 'PT04', 'TXN-BK0028'),
('PM0029', 'BK0029', '2026-09-13 09:05:00', 2550000,
 'PAID', 'PT02', 'TXN-BK0029'),
('PM0030', 'BK0030', '2026-09-14 09:05:00', 3200000,
 'PAID', 'PT04', 'TXN-BK0030'),
('PM0031', 'BK0031', '2026-09-23 09:05:00', 1350000,
 'PAID', 'PT02', 'TXN-BK0031'),
('PM0032', 'BK0032', '2026-09-20 09:05:00', 1800000,
 'PAID', 'PT04', 'TXN-BK0032'),
('PM0033', 'BK0033', '2026-09-21 09:05:00', 2400000,
 'PAID', 'PT02', 'TXN-BK0033'),
('PM0034', 'BK0034', '2026-09-22 09:05:00', 3550000,
 'PAID', 'PT04', 'TXN-BK0034'),
('PM0035', 'BK0035', '2026-09-23 09:05:00', 1750000,
 'PAID', 'PT02', 'TXN-BK0035'),
('PM0036', 'BK0036', '2026-09-20 09:05:00', 1500000,
 'PENDING', 'PT04', NULL),
('PM0037', 'BK0037', '2026-09-21 09:05:00', 2400000,
 'FAILED', 'PT02', NULL),
('PM0038', 'BK0038', '2026-09-22 09:05:00', 3200000,
 'REFUNDED', 'PT04', 'REF-BK0038'),
('PM0039', 'BK0039', '2026-09-23 09:05:00', 1050000,
 'PAID', 'PT02', 'TXN-BK0039'),
('PM0040', 'BK0040', '2026-09-20 09:05:00', 2850000,
 'PAID', 'PT04', 'TXN-BK0040');
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

-- Changes expired unpaid bookings to CANCELLED. The application may execute
-- the same update before loading booking/payment pages.
CREATE PROCEDURE SP_EXPIRE_PENDING_BOOKINGS
AS
BEGIN
    SET NOCOUNT ON;

    UPDATE BOOKING
    SET BookingStatus = 'CANCELLED'
    WHERE BookingStatus = 'PENDING_PAYMENT'
      AND PaymentDeadline <= GETDATE();

    SELECT @@ROWCOUNT AS ExpiredBookingCount;
END;
GO

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
    FROM PAYMENT AS p
    INNER JOIN BOOKING AS b
        ON b.BookingID = p.BookingID
    WHERE p.Status = 'PAID'
      AND p.Amount <> b.TotalAmount
)
BEGIN
    RAISERROR (
        'Seed paid-payment amounts are inconsistent.',
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

PRINT N'QLKS single-hotel database created successfully.';
GO

-- Timeout test:
-- UPDATE BOOKING
-- SET PaymentDeadline = DATEADD(SECOND, 10, GETDATE())
-- WHERE BookingID = 'BK0005';
-- WAITFOR DELAY '00:00:11';
-- EXEC SP_EXPIRE_PENDING_BOOKINGS;
-- SELECT BookingID, BookingStatus, PaymentDeadline
-- FROM BOOKING
-- WHERE BookingID = 'BK0005';

-- Example:
-- EXEC SP_GET_ROOM_TYPE_AVAILABILITY
--     @CheckInDate = '2026-09-20',
--     @CheckOutDate = '2026-09-22';
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
