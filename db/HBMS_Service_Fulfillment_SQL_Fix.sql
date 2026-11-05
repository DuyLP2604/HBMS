-- Select the HBMS database in SSMS, then run this entire script.
-- Change this value if the application uses a different database name.
DECLARE @TargetDatabase SYSNAME = N'QLKS';
IF DB_NAME() <> @TargetDatabase
    THROW 51400, 'Select the HBMS database before running this migration.', 1;

IF OBJECT_ID(N'dbo.BOOKING_SERVICE', N'U') IS NULL
    OR COL_LENGTH(N'dbo.BOOKING_SERVICE', N'ServiceStatus') IS NULL
    OR OBJECT_ID(N'dbo.SP_HBMS_ACQUIRE_WRITE_LOCK', N'P') IS NULL
    OR OBJECT_ID(N'dbo.V_BOOKING_PAYMENT_SUMMARY', N'V') IS NULL
    THROW 51401, 'This migration requires the HBMS v6 database.', 1;

SET XACT_ABORT ON;
BEGIN TRY
    BEGIN TRANSACTION;

    IF COL_LENGTH(N'dbo.BOOKING_SERVICE', N'ServedByUserID') IS NULL
        ALTER TABLE dbo.BOOKING_SERVICE ADD ServedByUserID INT NULL;

    IF OBJECT_ID(N'dbo.FK_BOOKING_SERVICE_ServedByUser', N'F') IS NULL
        EXEC(N'ALTER TABLE dbo.BOOKING_SERVICE WITH CHECK ADD CONSTRAINT FK_BOOKING_SERVICE_ServedByUser
            FOREIGN KEY (ServedByUserID) REFERENCES dbo.USERS(UserID);');

    -- Choose CREATE or ALTER using the procedures in this database.
    DECLARE @Definition NVARCHAR(MAX);

    SET @Definition = N'PROCEDURE dbo.SP_ADD_BOOKING_SERVICE
    @BookingID CHAR(6), @ServiceID CHAR(4), @Quantity INT, @ActorUserID INT,
    @UnitPrice DECIMAL(12,2) = NULL
AS
BEGIN
    SET NOCOUNT ON;
    SET XACT_ABORT ON;
    BEGIN TRY
        BEGIN TRANSACTION;
        EXEC SP_HBMS_ACQUIRE_WRITE_LOCK;
        IF @Quantity IS NULL OR @Quantity <= 0 THROW 51050, ''Service quantity must be positive.'', 1;
        IF NOT EXISTS (SELECT 1 FROM BOOKING AS b JOIN CUSTOMER AS c ON c.CustomerID = b.CustomerID
            WHERE b.BookingID = @BookingID AND b.BookingStatus IN (''PENDING_PAYMENT'', ''CONFIRMED'', ''ASSIGNED'', ''CHECKED_IN'')
            AND (c.UserID = @ActorUserID OR EXISTS (
                SELECT 1 FROM USERS WHERE UserID = @ActorUserID AND Role IN (''Admin'', ''Staff''))))
            THROW 51051, ''Booking not found, not authorized, or no longer accepts services.'', 1;
        IF EXISTS (SELECT 1 FROM BOOKING WHERE BookingID = @BookingID AND BookingStatus = ''PENDING_PAYMENT''
            AND PaymentDeadline <= SYSDATETIME()) THROW 51052, ''Pending booking has expired.'', 1;
        DECLARE @CatalogPrice DECIMAL(12,2);
        SELECT @CatalogPrice = UnitPrice FROM SERVICE WHERE ServiceID = @ServiceID;
        IF @CatalogPrice IS NULL THROW 51053, ''Service not found.'', 1;
        IF @UnitPrice IS NOT NULL AND @UnitPrice <> @CatalogPrice AND NOT EXISTS (
            SELECT 1 FROM USERS WHERE UserID = @ActorUserID AND Role IN (''Admin'', ''Staff''))
            THROW 51054, ''Only staff can set a service price different from the catalog.'', 1;
        IF @UnitPrice < 0 THROW 51055, ''Service price cannot be negative.'', 1;
        IF EXISTS (SELECT 1 FROM dbo.BOOKING_SERVICE WHERE BookingID = @BookingID AND ServiceID = @ServiceID)
        BEGIN
            -- One row represents one service per booking. Only unclaimed orders can grow.
            IF EXISTS (SELECT 1 FROM dbo.BOOKING_SERVICE WHERE BookingID = @BookingID AND ServiceID = @ServiceID
                AND (COALESCE(ServiceStatus, ''PENDING'') <> ''PENDING'' OR ServedByUserID IS NOT NULL))
                THROW 51300, N''Dịch vụ đã được tiếp nhận hoặc hoàn thành. Không thể tăng số lượng.'', 1;
            IF @UnitPrice IS NOT NULL AND EXISTS (SELECT 1 FROM dbo.BOOKING_SERVICE
                WHERE BookingID = @BookingID AND ServiceID = @ServiceID AND UnitPrice <> @UnitPrice)
                THROW 51056, ''Existing service has a different price snapshot.'', 1;
            UPDATE dbo.BOOKING_SERVICE SET Quantity = Quantity + @Quantity,
                Subtotal = (Quantity + @Quantity) * UnitPrice
            WHERE BookingID = @BookingID AND ServiceID = @ServiceID;
        END
        ELSE
        BEGIN
            SET @UnitPrice = COALESCE(@UnitPrice, @CatalogPrice);
            INSERT dbo.BOOKING_SERVICE(BookingID, ServiceID, Quantity, UnitPrice, Subtotal)
            VALUES (@BookingID, @ServiceID, @Quantity, @UnitPrice, @Quantity * @UnitPrice);
        END;
        COMMIT TRANSACTION;
        SELECT * FROM V_BOOKING_PAYMENT_SUMMARY WHERE BookingID = @BookingID;
    END TRY
    BEGIN CATCH
        IF @@TRANCOUNT > 0 ROLLBACK TRANSACTION;
        THROW;
    END CATCH;
END;';

    IF EXISTS (SELECT 1 FROM sys.procedures
               WHERE schema_id = SCHEMA_ID(N'dbo') AND name = N'SP_ADD_BOOKING_SERVICE')
        EXEC(N'ALTER ' + @Definition);
    ELSE
        EXEC(N'CREATE ' + @Definition);

    SET @Definition = N'PROCEDURE dbo.SP_UPDATE_BOOKING_SERVICE_STATUS
    @BookingID CHAR(6), @ServiceID CHAR(4), @ActorUserID INT, @NewStatus VARCHAR(20)
AS
BEGIN
    SET NOCOUNT ON;
    SET XACT_ABORT ON;
    BEGIN TRY
        BEGIN TRANSACTION;
        EXEC dbo.SP_HBMS_ACQUIRE_WRITE_LOCK;

        IF NOT EXISTS (
            SELECT 1 FROM dbo.EMPLOYEE AS e JOIN dbo.USERS AS u ON u.UserID = e.UserID
            WHERE u.UserID = @ActorUserID AND u.Role = ''Staff'' AND LOWER(e.Position) = N''service staff''
        )
            THROW 51301, ''Only service staff can fulfill service orders.'', 1;

        IF @NewStatus IS NULL OR @NewStatus NOT IN (''IN_PROGRESS'', ''COMPLETED'')
            THROW 51302, ''Invalid service status transition.'', 1;

        -- Match the old status and owner in the update to prevent competing claims.
        UPDATE bs
        SET ServiceStatus = @NewStatus, ServedByUserID = @ActorUserID
        FROM dbo.BOOKING_SERVICE AS bs
        JOIN dbo.BOOKING AS b ON b.BookingID = bs.BookingID
        WHERE bs.BookingID = @BookingID AND bs.ServiceID = @ServiceID
            AND b.BookingStatus IN (''PENDING_PAYMENT'', ''CONFIRMED'', ''ASSIGNED'', ''CHECKED_IN'')
            AND (b.BookingStatus <> ''PENDING_PAYMENT'' OR b.PaymentDeadline > SYSDATETIME())
            AND (
                (@NewStatus = ''IN_PROGRESS'' AND COALESCE(bs.ServiceStatus, ''PENDING'') = ''PENDING''
                    AND bs.ServedByUserID IS NULL)
                OR (@NewStatus = ''COMPLETED'' AND bs.ServiceStatus = ''IN_PROGRESS''
                    AND bs.ServedByUserID = @ActorUserID)
            );

        DECLARE @Changed BIT = CASE WHEN @@ROWCOUNT = 1 THEN 1 ELSE 0 END;
        COMMIT TRANSACTION;
        SELECT @Changed AS Changed;
    END TRY
    BEGIN CATCH
        IF @@TRANCOUNT > 0 ROLLBACK TRANSACTION;
        THROW;
    END CATCH;
END;';

    IF EXISTS (SELECT 1 FROM sys.procedures
               WHERE schema_id = SCHEMA_ID(N'dbo') AND name = N'SP_UPDATE_BOOKING_SERVICE_STATUS')
        EXEC(N'ALTER ' + @Definition);
    ELSE
        EXEC(N'CREATE ' + @Definition);

    IF DATABASE_PRINCIPAL_ID(N'HBMS_APP_ROLE') IS NOT NULL
    BEGIN
        EXEC(N'GRANT EXECUTE ON OBJECT::dbo.SP_ADD_BOOKING_SERVICE TO [HBMS_APP_ROLE];');
        EXEC(N'GRANT EXECUTE ON OBJECT::dbo.SP_UPDATE_BOOKING_SERVICE_STATUS TO [HBMS_APP_ROLE];');
    END;

    COMMIT TRANSACTION;
END TRY
BEGIN CATCH
    IF XACT_STATE() <> 0 ROLLBACK TRANSACTION;
    THROW;
END CATCH;

SELECT DB_NAME() AS DatabaseName, SCHEMA_NAME(schema_id) AS SchemaName, name AS ProcedureName
FROM sys.procedures
WHERE schema_id = SCHEMA_ID(N'dbo') AND name IN (N'SP_ADD_BOOKING_SERVICE', N'SP_UPDATE_BOOKING_SERVICE_STATUS')
ORDER BY name;
GO
