USE [master]
GO

/*******************************************************************************
   Drop database if it exists
********************************************************************************/
IF EXISTS (SELECT name FROM master.dbo.sysdatabases WHERE name = 'QLKS')
BEGIN 
	ALTER DATABASE QLKS SET OFFLINE WITH ROLLBACK IMMEDIATE;
	ALTER DATABASE QLKS SET ONLINE;
	DROP DATABASE QLKS;
END

GO

CREATE DATABASE QLKS
GO
-----------------------------------------------------
-----------------------------------------------------
USE QLKS
GO


--Hotel
create table HOTEL (
    [HotelID] char(3) primary key,
    [HotelName] nvarchar(50) not null,
    [HotelImage] nvarchar(100) not null,
    [Address] nvarchar(100) not null
);

CREATE TABLE USERS (
    UserID INT IDENTITY(1,1) PRIMARY KEY,

    Username VARCHAR(50) NOT NULL UNIQUE,
    Password VARCHAR(255) NOT NULL,
    Role VARCHAR(20) NOT NULL,
);

CREATE TABLE Nationality (
    NationalityID VARCHAR(50) PRIMARY KEY,
    NationalityName NVARCHAR(100) NOT NULL
);



INSERT INTO Nationality (NationalityID, NationalityName)
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

-- ADMIN
INSERT INTO USERS (Username, Password, Role)
VALUES
('admin', 'e10adc3949ba59abbe56e057f20f883e', 'Admin'); 

-- EMPLOYEE
INSERT INTO USERS (Username, Password, Role)
VALUES
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
('nv30', 'e13dd027be0f2152ce387ac0ea83d863', 'Staff');

-- CUSTOMER
INSERT INTO USERS (Username, Password, Role)
VALUES
('kh01', '4297f44b13955235245b2497399d7a93', 'Customer'),
('kh02', '4297f44b13955235245b2497399d7a93', 'Customer'),
('kh03', '4297f44b13955235245b2497399d7a93', 'Customer'),
('kh04', '4297f44b13955235245b2497399d7a93', 'Customer'),
('kh05', '4297f44b13955235245b2497399d7a93', 'Customer'),
('kh06', '4297f44b13955235245b2497399d7a93', 'Customer'),
('kh07', '4297f44b13955235245b2497399d7a93', 'Customer'),
('kh08', '4297f44b13955235245b2497399d7a93', 'Customer'),
('kh09', '4297f44b13955235245b2497399d7a93', 'Customer'),
('kh10', '4297f44b13955235245b2497399d7a93', 'Customer');

--CUSTOMER
create table CUSTOMER (
    [CustomerID] char(6) primary key,
    [FullName] nvarchar(100),
    [Phone] varchar(15),
    [Email] varchar(100),
    [Address] nvarchar(200),
	[CCCD] varchar(20),
	[PassportNumber] varchar(30),
    [NationalityID] varchar(50),
    [UserID] int,
    foreign key (UserID) references USERS(UserID),
    foreign key (NationalityID) references Nationality(NationalityID)
);
--ROOM
create table ROOM (
    [RoomID] char(3) primary key,
    [RoomNumber] NVARCHAR(20) not null,
    [RoomImage] NVARCHAR(100) not null,
    [Price] MONEY not null,
    [Status] NVARCHAR(50) not null,
    [HotelID] char(3),
    foreign key (HotelID) references HOTEL(HotelID)
);
--EMPLOYEE
create table EMPLOYEE (
    [EmployeeID] char(6) primary key,
    [FullName] nvarchar(100),
    [Position] nvarchar(50),
    [Salary] decimal(12,2),
    [Shift] nvarchar(20),
    [Address] nvarchar(200),
	[Phone] VARCHAR(20),
    [HotelID] char(3),
    [UserID] int,
    foreign key (HotelID) references HOTEL(HotelID),
    foreign key (UserID) references USERS(UserID)
);
--BOOKING
create table BOOKING (
    [BookingID] char(3) primary key not null,
    [BookingDate] DATE not null,
    [CheckInDate] DATE not null,
    [CheckOutDate] DATE not null,
    [BookingStatus] NVARCHAR(50) not null,
    [RoomID] char(3),
    [CustomerID] char(6),
    foreign key (RoomID) references ROOM(RoomID),
    foreign key (CustomerID) references CUSTOMER(CustomerID)
);
--Service
create table SERVICE (
    [ServiceID] char(4) primary key,
    [ServiceName] NVARCHAR(60) not null,
    [UnitPrice] DECIMAL(10, 2) not null,
    [HotelID] char(3),
    [RoomID] char(3),
    foreign key (HotelID) references HOTEL(HotelID),
    foreign key (RoomID) references ROOM(RoomID)
);
create table BOOKING_SERVICE(
    [BookingID] CHAR(3),
    [ServiceID] CHAR(4),

    primary key (BookingID, ServiceID),
    foreign key (BookingID) references BOOKING(BookingID),
    foreign key (ServiceID) references SERVICE(ServiceID)
)
--INVOICE
create table INVOICE (
    [InvoiceID] char(6) primary key,
    [InvoiceDate] date NOT NULL,
    [TotalAmount] decimal(10,2) NOT NULL,

    HotelID char(3) NOT NULL,
	foreign key (HotelID) references HOTEL(HotelID),
    CustomerID char(6) NOT NULL,
	foreign key (CustomerID) references CUSTOMER(CustomerID),
    EmployeeID char(6) NOT NULL,
	foreign key (EmployeeID) references EMPLOYEE(EmployeeID),
    BookingID char(3) NOT NULL UNIQUE,
	foreign key (BookingID) references BOOKING(BookingID)
);
--PAYMENTMETHOD
create table PAYMENTMETHOD(
	[MethodID] char(4) primary key,
	[MethodName] nvarchar(30) NOT NULL 
);
--PAYMENT
create table PAYMENT(
	[PaymentID] char(4) primary key,
	[Time] datetime NOT NULL,
	[Money] decimal(10,2) NOT NULL,
	[Status] nvarchar(50) NOT NULL,
	[MethodID] char(4),
	foreign key (MethodID) references [PAYMENTMETHOD](MethodID),
    [InvoiceID] char(6) UNIQUE,
	foreign key (InvoiceID) references [INVOICE](InvoiceID)
);
CREATE TABLE COMPLAINT (
    [ComplaintID] INT IDENTITY(1,1) PRIMARY KEY,
    [Title] NVARCHAR(200) NOT NULL,
    [Content] NVARCHAR(MAX) NOT NULL,
    [CreatedAt] DATETIME NOT NULL DEFAULT GETDATE(),
    [Status] NVARCHAR(50) NOT NULL DEFAULT N'Chưa xử lý',
    [CustomerID] CHAR(6) NULL,
    CONSTRAINT FK_Complaint_Customer FOREIGN KEY (CustomerID) REFERENCES CUSTOMER(CustomerID)
);
INSERT INTO HOTEL (HotelID, HotelName, HotelImage, Address)
VALUES 
('H01', N'Simple Bear Hà Nội','hotel1.jpg', N'Hoàng Mai, Hà Nội'),
('H02', N'Simple Bear Đà Nẵng','hotel2.jpg', N'Ngũ Hành Sơn, Đà Nẵng'),
('H03', N'Simple Bear Sài Gòn','hotel3.jpg', N'Phú Nhuận, TP.HCM');

INSERT INTO CUSTOMER (CustomerID, FullName, Phone, Email, Address, CCCD, PassportNumber, NationalityID, UserID)
VALUES 
('KH01', N'Phạm Minh Tuấn', '0903456789', 'tuan.pham@gmail.com', N'Quận 1, TP.HCM', '079085012345', NULL, 'N01',31),
('KH02', N'Nguyễn Tuyết Mai', '0912888999', 'maituyet92@yahoo.com', N'Quận Cầu Giấy, Hà Nội', '001192005678', NULL, 'N01',32),
('KH03', N'Lê Hoàng Nam', '0987111222', 'namlh@gmail.com', N'Quận 7, TP.HCM', '040090001234', 'VN889911', 'N01',33),
('KH04', N'Trần Thu Hà', '0356123456', 'hatran@fpt.com.vn', N'Quận Tây Hồ, Hà Nội', '038195009876', 'VN223344', 'N01',34),
('KH05', N'Robert Harrison', '0775123456', 'robert.h@outlook.com', N'London, United Kingdom', NULL, 'B12345678', 'N13',35),
('KH06', N'Chen Wei', '0933444555', 'chenwei88@qq.com', N'Beijing, China', NULL, 'G55667788', 'N03',36),
('KH07', N'Hans Müller', '0888777666', 'hans.m@gmail.de', N'Berlin, Germany', NULL, 'C88990011', 'N15',37),
('KH08', N'Kim Ji-won', '0944555666', 'jiwon.kim@naver.com', N'Seoul, South Korea', NULL, 'M11223344', 'N04',38),
('KH09', N'Jean Dupont', '0766112233', 'jean.dupont@france.fr', N'Paris, France', NULL, 'P11224455', 'N14',39),
('KH10', N'Đặng Phương Thảo', '0399123456', 'thaophuong@gmail.com', N'Quận Hải Châu, Đà Nẵng', '052199004455', NULL, 'N01',40)
GO
INSERT INTO ROOM (RoomID, RoomNumber, RoomImage, Price, Status, HotelID)
VALUES
('R01','101','room01.png', 600000, N'Occupied','H01'),  
('R02','102','room02.png', 800000, N'Available','H01'), 
('R03','201','room03.png', 600000, N'Occupied','H02'), 
('R04','202','room04.png', 800000, N'Occupied','H02'),  
('R05','301','room05.png', 600000, N'Occupied','H03'),  
('R06','302','room06.png', 800000, N'Available','H03'), 
('R07','303','room07.png', 600000, N'Available','H03'), 
('R08','304','room08.png', 800000, N'Occupied','H03'),  
('R09','305','room09.png', 600000, N'Available','H03'), 
('R10','306','room10.png', 800000, N'Available','H03');


INSERT INTO EMPLOYEE (EmployeeID, FullName, Position, Salary, Shift, Address, Phone, HotelID,UserID)
VALUES
('NV01', N'Nguyễn Hoài Nam', N'Quản lý', 32000000.00, N'Sáng', N'Giải Phóng, Hoàng Mai, Hà Nội', '0912345671', 'H01',1),
('NV02', N'Lê Thảo Phương', N'Lễ tân', 12000000.00, N'Sáng', N'Trương Định, Hoàng Mai, Hà Nội', '0912345672', 'H01',2),
('NV03', N'Trần Minh Đức', N'Lễ tân', 12000000.00, N'Chiều', N'Bạch Mai, Hai Bà Trưng, Hà Nội', '0912345673', 'H01',3),
('NV04', N'Phạm Thanh Sơn', N'Bảo vệ', 9000000.00, N'Đêm', N'Lĩnh Nam, Hoàng Mai, Hà Nội', '0912345674', 'H01',4),
('NV05', N'Vũ Hồng Hạnh', N'Kế toán', 18000000.00, N'Sáng', N'Nguyễn Xiển, Thanh Xuân, Hà Nội', '0912345675', 'H01',5),
('NV06', N'Đặng Văn Hùng', N'Bếp trưởng', 22000000.00, N'Chiều', N'Tân Mai, Hoàng Mai, Hà Nội', '0912345676', 'H01',6),
('NV07', N'Bùi Mỹ Linh', N'Phục vụ phòng', 8500000.00, N'Sáng', N'Tam Trinh, Hoàng Mai, Hà Nội', '0912345677', 'H01',7),
('NV08', N'Ngô Anh Tuấn', N'Kỹ thuật', 11000000.00, N'Hành chính', N'Đền Lừ, Hoàng Mai, Hà Nội', '0912345678', 'H01',8),
('NV09', N'Hoàng Thu Trang', N'Nhân viên Bar', 10500000.00, N'Chiều', N'Đại La, Hai Bà Trưng, Hà Nội', '0912345679', 'H01',9),
('NV10', N'Lý Gia Bảo', N'Lái xe', 9500000.00, N'Sáng', N'Giáp Bát, Hoàng Mai, Hà Nội', '0912345680', 'H01',10),

('NV11', N'Lê Quốc Anh', N'Quản lý', 30000000.00, N'Sáng', N'Lê Văn Hiến, Ngũ Hành Sơn, Đà Nẵng', '0912345681', 'H02',11),
('NV12', N'Võ Thị Kim Oanh', N'Lễ tân', 11000000.00, N'Sáng', N'Trần Đại Nghĩa, Ngũ Hành Sơn, Đà Nẵng', '0912345682', 'H02',12),
('NV13', N'Đỗ Minh Quân', N'Lễ tân', 11000000.00, N'Chiều', N'Hồ Xuân Hương, Ngũ Hành Sơn, Đà Nẵng', '0912345683', 'H02',13),
('NV14', N'Trần Văn Toàn', N'Bảo vệ', 8500000.00, N'Đêm', N'Chương Dương, Ngũ Hành Sơn, Đà Nẵng', '0912345684', 'H02',14),
('NV15', N'Nguyễn Mai Anh', N'Kế toán', 17000000.00, N'Sáng', N'Võ Nguyên Giáp, Sơn Trà, Đà Nẵng', '0912345685', 'H02',15),
('NV16', N'Lâm Thế Vinh', N'Bếp trưởng', 20000000.00, N'Chiều', N'Nguyễn Văn Thoại, Sơn Trà, Đà Nẵng', '0912345686', 'H02',16),
('NV17', N'Hà Thị Bích', N'Phục vụ phòng', 8000000.00, N'Sáng', N'Mỹ Đa Tây, Ngũ Hành Sơn, Đà Nẵng', '0912345687', 'H02',17),
('NV18', N'Phan Thanh Hải', N'Kỹ thuật', 10000000.00, N'Hành chính', N'Đỗ Bá, Ngũ Hành Sơn, Đà Nẵng', '0912345688', 'H02',18),
('NV19', N'Trịnh Xuân Thu', N'Nhân viên Bar', 10000000.00, N'Chiều', N'Châu Thị Vĩnh Tế, Ngũ Hành Sơn, Đà Nẵng', '0912345689', 'H02',19),
('NV20', N'Đoàn Ngọc Dũng', N'Lái xe', 9000000.00, N'Sáng', N'Mai Đăng Chơn, Ngũ Hành Sơn, Đà Nẵng', '0912345690', 'H02',20),

('NV21', N'Huỳnh Minh Triết', N'Quản lý', 35000000.00, N'Sáng', N'Phan Xích Long, Phú Nhuận, TP.HCM', '0912345691', 'H03',21),
('NV22', N'Nguyễn Ngọc Diệp', N'Lễ tân', 13000000.00, N'Sáng', N'Nguyễn Kiệm, Phú Nhuận, TP.HCM', '0912345692', 'H03',22),
('NV23', N'Trương Công Định', N'Lễ tân', 13000000.00, N'Chiều', N'Phan Đăng Lưu, Phú Nhuận, TP.HCM', '0912345693', 'H03',23),
('NV24', N'Lương Văn Tám', N'Bảo vệ', 10000000.00, N'Đêm', N'Lê Văn Sỹ, Phú Nhuận, TP.HCM', '0912345694', 'H03',24),
('NV25', N'Phạm Thùy Dương', N'Kế toán', 19000000.00, N'Sáng', N'Thích Quảng Đức, Phú Nhuận, TP.HCM', '0912345695', 'H03',25),
('NV26', N'Ngô Thanh Tùng', N'Bếp trưởng', 23000000.00, N'Chiều', N'Hoàng Văn Thụ, Phú Nhuận, TP.HCM', '0912345696', 'H03',26),
('NV27', N'Đỗ Thị Thắm', N'Phục vụ phòng', 9000000.00, N'Sáng', N'Huỳnh Văn Bánh, Phú Nhuận, TP.HCM', '0912345697', 'H03',27),
('NV28', N'Vương Quốc Bình', N'Kỹ thuật', 12000000.00, N'Hành chính', N'Phan Đình Phùng, Phú Nhuận, TP.HCM', '0912345698', 'H03',28),
('NV29', N'Tô Minh Nguyệt', N'Nhân viên Bar', 11500000.00, N'Chiều', N'Bạch Đằng, Bình Thạnh, TP.HCM', '0912345699', 'H03',29),
('NV30', N'Phan Anh Tú', N'Lái xe', 10000000.00, N'Sáng', N'Nguyễn Oanh, Gò Vấp, TP.HCM', '0912345700', 'H03',30);
GO


INSERT INTO BOOKING
(BookingID, BookingDate, CheckInDate, CheckOutDate, BookingStatus, CustomerID, RoomID)
VALUES
('B01', '2025-10-15', '2025-10-20', '2025-10-21', N'Completed','KH01','R01'),
('B02', '2025-10-25', '2025-10-31', '2025-11-01', N'Completed','KH02','R03'),
('B03', '2025-11-05', '2025-11-11', '2025-11-12', N'Completed','KH03','R05'),
('B04', '2025-11-15', '2025-11-20', '2025-11-21', N'Completed','KH04','R06'),
('B05', '2025-11-25', '2025-11-30', '2025-12-01', N'Completed','KH05','R07'),
('B06', '2025-12-01', '2025-12-04', '2025-12-05', N'Completed','KH06','R08'),
('B07', '2025-12-10', '2025-12-12', '2025-12-13', N'Completed','KH07','R09'),
('B08', '2025-12-20', '2025-12-25', '2025-12-26', N'Completed','KH08','R10'),
('B09', '2025-12-22', '2025-12-25', '2025-12-26', N'Completed','KH09','R02'),
('B10','2025-12-23', '2025-12-25', '2025-12-28', N'Incompleted','KH10','R04');

INSERT INTO SERVICE (ServiceID, ServiceName, UnitPrice, HotelID, RoomID) VALUES 
('S01',N'Giặt ủi',75000,'H01','R01'),
('S02',N'Ăn sáng Buffet',250000,'H01','R03'),
('S03',N'Thuê xe',150000,'H02','R04'),
('S04',N'Spa, massage',450000,'H02','R04'),
('S05',N'Ăn uống tại phòng',100000,'H03','R02'),
('S06',N'Dịch vụ trông trẻ',150000,'H03','R05'),
('S07',N'Đưa đón sân bay',350000,'H01','R05'),
('S08',N'Wake-up call',50000,'H02','R08'),
('S09',N'Đặt taxi',300000,'H03','R06'),
('S10',N'Giữ hành lý',50000,'H03','R06');

INSERT INTO BOOKING_SERVICE (BookingID, ServiceID) VALUES
('B01', 'S01'),
('B02', 'S02'),
('B03', 'S03'),
('B04', 'S04'),
('B05', 'S05'),
('B06', 'S06'),
('B07', 'S07'),
('B08', 'S08'),
('B09', 'S09'),
('B10','S10');

INSERT INTO INVOICE
(InvoiceID, InvoiceDate, TotalAmount, HotelID, CustomerID, EmployeeID, BookingID)
VALUES
('HD01', '2025-10-20', 729000.00,  'H01', 'KH01', 'NV01', 'B01'), -- R01(600k) + S01(75k) + 8% VAT
('HD02', '2025-10-31', 918000.00,  'H01', 'KH02', 'NV01', 'B02'), -- R03(600k) + S02(250k) + 8% VAT
('HD03', '2025-11-11', 756000.00,  'H01', 'KH05', 'NV01', 'B05'), -- R07(600k) + S05(100k) + 8% VAT
('HD04', '2025-11-20', 810000.00,  'H02', 'KH03', 'NV02', 'B03'), -- R05(600k) + S03(150k) + 8% VAT
('HD05', '2025-11-30', 1026000.00, 'H02', 'KH06', 'NV02', 'B06'), -- R08(800k) + S06(150k) + 8% VAT
('HD06', '2025-12-04', 1350000.00, 'H03', 'KH04', 'NV03', 'B04'), -- R06(800k) + S04(450k) + 8% VAT
('HD07', '2025-12-12', 918000.00,  'H03', 'KH08', 'NV03', 'B08'), -- R10(800k) + S08(50k)  + 8% VAT
('HD08', '2025-12-25', 1188000.00, 'H03', 'KH09', 'NV03', 'B09'), -- R02(800k) + S09(300k) + 8% VAT
('HD09', '2025-12-25', 1026000.00, 'H02', 'KH09', 'NV03', 'B07'), -- R09(600k) + S07(350k) + 8% VAT
('HD10', '2025-12-25', 2646000.00, 'H03', 'KH10', 'NV03', 'B10');



INSERT INTO PAYMENTMETHOD (MethodID, MethodName)
VALUES
('PT01',  N'Cash'),
('PT02',  N'Credit Card'),
('PT03',  N'Debit Card'),
('PT04',  N'Bank Transfer'),
('PT05',  N'Internet Banking'),
('PT06',  N'E-Wallet'),
('PT07',  N'PayPal'),
('PT08', N'Others');

INSERT INTO PAYMENT
(PaymentID, [Time], Money, Status, MethodID, InvoiceID)
VALUES
('TT01', '2025-10-20 09:00:00', 729000.00,  N'Paid',    'PT01', 'HD01'),
('TT02', '2025-10-31 14:30:50', 918000.00,  N'Paid',    'PT02', 'HD02'),
('TT03', '2025-11-11 19:30:30', 756000.00,  N'Pending', 'PT07', 'HD03'),
('TT04', '2025-11-20 15:20:45', 810000.00,  N'Failed',  'PT05', 'HD04'),
('TT05', '2025-11-30 16:12:40', 1026000.00, N'Paid',    'PT01', 'HD05'),
('TT06', '2025-12-04 18:24:30', 1350000.00, N'Paid',    'PT04', 'HD06'),
('TT07', '2025-12-12 12:30:15', 918000.00,  N'Pending', 'PT03', 'HD07'),
('TT08', '2025-12-25 18:30:25', 1188000.00, N'Paid',    'PT01', 'HD08'),
('TT09', '2025-12-25 20:15:50', 1026000.00, N'Pending', 'PT06', 'HD09'),
('TT10', '2025-12-25 21:45:30', 2646000.00, N'Paid',    'PT02', 'HD10');

