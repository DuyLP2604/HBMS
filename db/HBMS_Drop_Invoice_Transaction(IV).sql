-- HBMS: loại bỏ bảng nối INVOICE_TRANSACTION trên database hiện có.
-- Bản sửa lỗi 51506: đọc đúng khai báo sau chú thích/khoảng trắng, hỗ trợ PROC và PROCEDURE.
-- Chọn database QLKS trong SSMS và chạy toàn bộ script, không chia nhỏ các đoạn.
-- Không chạy lại file khởi tạo DB_v6_merged_transaction.sql trên DB có dữ liệu cần giữ.
DECLARE @TargetDatabase SYSNAME = N'QLKS';
IF DB_NAME() <> @TargetDatabase
    THROW 51500, N'Chọn đúng database HBMS trước khi chạy migration.', 1;
IF @@TRANCOUNT <> 0
    THROW 51501, N'Chạy migration ngoài transaction đang mở.', 1;

SET ANSI_NULLS ON;
SET QUOTED_IDENTIFIER ON;
SET XACT_ABORT ON;
SET NOCOUNT ON;

IF OBJECT_ID(N'dbo.INVOICE', N'U') IS NULL
    OR OBJECT_ID(N'dbo.PAYMENT', N'U') IS NULL
    OR OBJECT_ID(N'dbo.TRANSACTION', N'U') IS NULL
    OR OBJECT_ID(N'dbo.SP_HBMS_ACQUIRE_WRITE_LOCK', N'P') IS NULL
    THROW 51502, N'Migration yêu cầu cấu trúc database HBMS v6.', 1;

BEGIN TRY
    BEGIN TRANSACTION;
    EXEC dbo.SP_HBMS_ACQUIRE_WRITE_LOCK;

    -- Kiểm tra có thể suy ra các liên kết cũ từ dữ liệu gốc trước khi xóa bảng nối.
    -- SQL động giúp script chạy lại được cả khi bảng đã bị xóa.
    IF OBJECT_ID(N'dbo.INVOICE_TRANSACTION', N'U') IS NOT NULL
        EXEC sys.sp_executesql N'
            IF EXISTS (
                SELECT 1 FROM dbo.INVOICE_TRANSACTION AS link
                LEFT JOIN dbo.INVOICE AS i ON i.InvoiceID = link.InvoiceID AND i.BookingID = link.BookingID
                LEFT JOIN dbo.PAYMENT AS p ON p.PaymentID = link.PaymentID AND p.BookingID = link.BookingID
                    AND p.TransactionID = link.TransactionID
                LEFT JOIN dbo.[TRANSACTION] AS t ON t.TransactionID = p.TransactionID
                LEFT JOIN dbo.PAYMENTMETHOD AS m ON m.MethodID = p.MethodID
                WHERE i.InvoiceID IS NULL OR p.PaymentID IS NULL OR t.TransactionID IS NULL
                    OR m.MethodID IS NULL OR p.Status NOT IN (''PAID'', ''REFUNDED'')
            ) THROW 51503, N''Liên kết hóa đơn cũ không khớp dữ liệu thanh toán. Migration đã dừng.'', 1;';

    -- Chỉ xóa đúng INSERT bảng nối; không ghi đè toàn bộ thủ tục từ một bản DB cũ.
    -- Đoạn cũ lấy từ HBMS.zip. CRLF/LF được chuẩn hóa trước khi so khớp.
    DECLARE @Modules TABLE (ID INT PRIMARY KEY, ModuleName SYSNAME, LegacyBlock NVARCHAR(MAX));
    INSERT @Modules (ID, ModuleName, LegacyBlock) VALUES
        (1, N'dbo.SP_RECORD_PAYMENT', N'        INSERT INVOICE_TRANSACTION(InvoiceID, BookingID, PaymentID, TransactionID)
        SELECT i.InvoiceID, @BookingID, @PaymentID, @TransactionID
        FROM INVOICE AS i
        WHERE i.BookingID = @BookingID
          AND NOT EXISTS (SELECT 1 FROM INVOICE_TRANSACTION AS link
              WHERE link.InvoiceID = i.InvoiceID AND link.TransactionID = @TransactionID);
'),
        (2, N'dbo.SP_CHECKOUT_WITH_INVOICE', N'        INSERT INVOICE_TRANSACTION(InvoiceID, BookingID, PaymentID, TransactionID)
        SELECT @InvoiceID, @BookingID, p.PaymentID, p.TransactionID
        FROM PAYMENT AS p
        WHERE p.BookingID = @BookingID AND p.Status = ''PAID''
          AND NOT EXISTS (SELECT 1 FROM INVOICE_TRANSACTION AS link
              WHERE link.InvoiceID = @InvoiceID AND link.TransactionID = p.TransactionID);
');

    DECLARE @Index INT = 1, @ModuleName SYSNAME, @LegacyBlock NVARCHAR(MAX),
        @Definition NVARCHAR(MAX), @Updated NVARCHAR(MAX), @Message NVARCHAR(2048);
    DECLARE @HeaderPosition INT, @DefinitionLength INT, @HeaderStep INT, @HeaderValid BIT,
        @TokenStart INT, @TokenLength INT, @Token NVARCHAR(128), @CharacterCode INT,
        @Verb NVARCHAR(10), @VerbStart INT, @VerbLength INT, @CommentDepth INT, @LineEnd INT;
    WHILE @Index <= 2
    BEGIN
        SELECT @ModuleName = ModuleName, @LegacyBlock = LegacyBlock FROM @Modules WHERE ID = @Index;
        SET @Definition = OBJECT_DEFINITION(OBJECT_ID(@ModuleName, N'P'));
        IF @Definition IS NULL
        BEGIN
            SET @Message = CONCAT(N'Không đọc được thủ tục ', @ModuleName, N'. Kiểm tra phiên bản DB và quyền VIEW DEFINITION.');
            THROW 51504, @Message, 1;
        END;
        SET @Definition = REPLACE(@Definition, CHAR(13) + CHAR(10), CHAR(10));
        SET @LegacyBlock = REPLACE(@LegacyBlock, CHAR(13) + CHAR(10), CHAR(10));
        SET @Updated = REPLACE(@Definition, @LegacyBlock, N'');
        IF PATINDEX(N'%[^A-Z0-9_]INVOICE[_]TRANSACTION[^A-Z0-9_]%',
            (N' ' + @Updated + N' ') COLLATE Latin1_General_100_CI_AS) > 0
        BEGIN
            SET @Message = CONCAT(N'Thủ tục ', @ModuleName,
                N' có tham chiếu INVOICE_TRANSACTION khác mẫu HBMS.zip. Migration hoàn tác để giữ logic hiện tại.');
            THROW 51505, @Message, 1;
        END;
        IF @Updated <> @Definition
        BEGIN
            -- ALTER giữ nguyên object và các quyền EXECUTE đã cấp.
            -- OBJECT_DEFINITION có thể giữ chú thích dài trước khai báo. Chỉ đọc
            -- các token đầu, bỏ qua khoảng trắng và chú thích; không tìm từ khóa
            -- trong chú thích/chuỗi của thân thủ tục và không giới hạn 40 ký tự.
            -- HBMS_HEADER_PARSER_BEGIN
            SELECT @HeaderPosition = 1, @DefinitionLength = LEN(@Updated),
                @HeaderStep = 0, @HeaderValid = 0, @Verb = NULL;
            WHILE @HeaderPosition <= @DefinitionLength AND @HeaderValid = 0
            BEGIN
                WHILE @HeaderPosition <= @DefinitionLength
                BEGIN
                    SET @CharacterCode = UNICODE(SUBSTRING(@Updated, @HeaderPosition, 1));
                    IF @CharacterCode IN (9, 10, 11, 12, 13, 32, 65279)
                    BEGIN
                        SET @HeaderPosition += 1;
                        CONTINUE;
                    END;
                    IF SUBSTRING(@Updated, @HeaderPosition, 2) = N'--'
                    BEGIN
                        SET @LineEnd = CHARINDEX(CHAR(10), @Updated, @HeaderPosition + 2);
                        SET @HeaderPosition = CASE WHEN @LineEnd = 0 THEN @DefinitionLength + 1 ELSE @LineEnd + 1 END;
                        CONTINUE;
                    END;
                    IF SUBSTRING(@Updated, @HeaderPosition, 2) = N'/*'
                    BEGIN
                        SELECT @CommentDepth = 1, @HeaderPosition = @HeaderPosition + 2;
                        WHILE @HeaderPosition <= @DefinitionLength AND @CommentDepth > 0
                        BEGIN
                            IF SUBSTRING(@Updated, @HeaderPosition, 2) = N'/*'
                                SELECT @CommentDepth = @CommentDepth + 1, @HeaderPosition = @HeaderPosition + 2;
                            ELSE IF SUBSTRING(@Updated, @HeaderPosition, 2) = N'*/'
                                SELECT @CommentDepth = @CommentDepth - 1, @HeaderPosition = @HeaderPosition + 2;
                            ELSE
                                SET @HeaderPosition += 1;
                        END;
                        IF @CommentDepth <> 0 BREAK;
                        CONTINUE;
                    END;
                    BREAK;
                END;
                IF @HeaderPosition > @DefinitionLength BREAK;
                SET @TokenStart = @HeaderPosition;
                WHILE @HeaderPosition <= @DefinitionLength
                BEGIN
                    SET @CharacterCode = UNICODE(SUBSTRING(@Updated, @HeaderPosition, 1));
                    IF NOT (@CharacterCode BETWEEN 65 AND 90 OR @CharacterCode BETWEEN 97 AND 122) BREAK;
                    SET @HeaderPosition += 1;
                END;
                SET @TokenLength = @HeaderPosition - @TokenStart;
                IF @TokenLength = 0 OR @TokenLength > 128 BREAK;
                SET @Token = UPPER(SUBSTRING(@Updated, @TokenStart, @TokenLength));
                IF @HeaderStep = 0 AND @Token IN (N'CREATE', N'ALTER')
                    SELECT @Verb = @Token, @VerbStart = @TokenStart, @VerbLength = @TokenLength, @HeaderStep = 1;
                ELSE IF @HeaderStep IN (1, 3) AND @Token IN (N'PROC', N'PROCEDURE')
                    SET @HeaderValid = 1;
                ELSE IF @HeaderStep = 1 AND @Verb = N'CREATE' AND @Token = N'OR'
                    SET @HeaderStep = 2;
                ELSE IF @HeaderStep = 2 AND @Token = N'ALTER'
                    SET @HeaderStep = 3;
                ELSE
                    BREAK;
            END;
            IF @HeaderValid = 0
            BEGIN
                SET @Message = CONCAT(N'Không nhận diện được khai báo CREATE/ALTER PROC[EDURE] của ',
                    @ModuleName, N'. Migration đã hoàn tác.');
                THROW 51506, @Message, 1;
            END;
            -- CREATE PROC -> ALTER PROC. ALTER và CREATE OR ALTER dùng được ngay.
            -- Giữ nguyên chú thích, tên, tham số và toàn bộ thân thủ tục.
            IF @Verb = N'CREATE' AND @HeaderStep = 1
                SET @Updated = STUFF(@Updated, @VerbStart, @VerbLength, N'ALTER');
            -- HBMS_HEADER_PARSER_END
            EXEC sys.sp_executesql @Updated;
        END;
        SET @Index += 1;
    END;

    -- Giữ tên view và các cột cũ; truy vấn trực tiếp theo BookingID và TransactionID.
    EXEC sys.sp_executesql N'CREATE OR ALTER VIEW dbo.V_INVOICE_TRANSACTION_DETAILS AS
SELECT i.InvoiceID, i.InvoiceDate, i.BookingID,
    i.TotalAmount AS InvoiceAmount, t.id AS TransactionRowID, t.TransactionID,
    t.TransactionType, t.TransactionValue, p.PaymentID, p.PaymentType,
    p.PaymentTime, p.Status AS PaymentStatus, p.MethodID, m.MethodName,
    t.TransactionCode, p.WalletAmount, p.ExternalAmount
FROM INVOICE AS i
JOIN PAYMENT AS p ON p.BookingID = i.BookingID AND p.Status IN (''PAID'', ''REFUNDED'')
JOIN dbo.[TRANSACTION] AS t ON t.TransactionID = p.TransactionID
JOIN PAYMENTMETHOD AS m ON m.MethodID = p.MethodID;';

    IF OBJECT_ID(N'dbo.TR_INVOICE_TRANSACTION_Validate', N'TR') IS NOT NULL
        DROP TRIGGER dbo.TR_INVOICE_TRANSACTION_Validate;

    -- Không bỏ sót module khác có phụ thuộc tĩnh hoặc câu SQL động còn nhắc tên bảng.
    IF EXISTS (
        SELECT 1 FROM sys.sql_expression_dependencies
        WHERE (referenced_schema_name IN (N'dbo', N'') OR referenced_schema_name IS NULL)
            AND referenced_entity_name = N'INVOICE_TRANSACTION'
    ) OR EXISTS (
        SELECT 1 FROM sys.sql_modules
        WHERE PATINDEX(N'%[^A-Z0-9_]INVOICE[_]TRANSACTION[^A-Z0-9_]%',
            (N' ' + definition + N' ') COLLATE Latin1_General_100_CI_AS) > 0
    )
        THROW 51507, N'Có module khác còn dùng INVOICE_TRANSACTION. Toàn bộ migration được hoàn tác.', 1;

    -- Các PK/FK/index/quyền gắn riêng với bảng nối được SQL Server xóa cùng bảng.
    IF OBJECT_ID(N'dbo.INVOICE_TRANSACTION', N'U') IS NOT NULL
        EXEC sys.sp_executesql N'DROP TABLE dbo.INVOICE_TRANSACTION;';

    -- Phép đọc thực tế xác nhận view vẫn hoạt động sau khi bảng đã biến mất.
    EXEC sys.sp_executesql N'SELECT TOP (0) * FROM dbo.V_INVOICE_TRANSACTION_DETAILS;';

    COMMIT TRANSACTION;
    PRINT N'Đã xóa bảng INVOICE_TRANSACTION và cập nhật các đối tượng liên quan.';
END TRY
BEGIN CATCH
    IF @@TRANCOUNT > 0 ROLLBACK TRANSACTION;
    THROW;
END CATCH;
