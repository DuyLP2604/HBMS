package service;

import java.io.OutputStream;
import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import util.PersistenceManager;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFFormulaEvaluator;

/**
 * Excel backup of physical v6 tables, independent of JPA relationships.
 */
public class DataService {

    private static final List<String> TABLES = List.of("HOTEL", "NATIONALITY", "ROOM_TYPE", "SERVICE",
            "PAYMENTMETHOD", "USERS", "EMPLOYEE", "CUSTOMER", "USER_BOOKING_CONTROL", "CUSTOMER_WALLET",
            "ROOM", "COMPLAINT", "BOOKING", "BOOKING_DETAIL", "BOOKING_SERVICE", "ROOM_ASSIGNMENT",
            "TRANSACTION", "PAYMENT", "BOOKING_REFUND", "REFUND_PAYMENT", "WALLET_TRANSACTION",
            "BOOKING_LOCK_HISTORY", "INVOICE", "PAYOS_ORDER");
    private static final int MAX_REPORT_DAYS = 60;
    private static final int FIRST_COL = 2;

    protected Connection openConnection() throws SQLException {
        return PersistenceManager.openConnection();
    }

    /**
     *
     * @param output
     * @param beginDate
     * @param endDate
     * @throws Exception
     */
    public void exportReport(OutputStream output, Date beginDate, Date endDate) throws Exception {

        if (beginDate == null || endDate == null) {
            throw new IllegalArgumentException("Begin date and end date are required.");
        }
        LocalDate begin = beginDate.toLocalDate();
        LocalDate end = endDate.toLocalDate();
        if (end.isBefore(begin)) {
            throw new IllegalArgumentException("End date must not be before begin date.");
        }
        if (ChronoUnit.DAYS.between(begin, end) + 1 > MAX_REPORT_DAYS) {
            throw new IllegalArgumentException(
                    "The report range must not exceed " + MAX_REPORT_DAYS + " days.");
        }
        // -----------------------------------------------------------------
        // 2️⃣ Thu thập dữ liệu từ DB
        // -----------------------------------------------------------------
        Map<LocalDate, Integer> bookings = new HashMap<>();
        Map<LocalDate, BigDecimal> collected = new HashMap<>();
        Map<LocalDate, BigDecimal> refunded = new HashMap<>();
        Timestamp from = Timestamp.valueOf(begin.atStartOfDay());
        Timestamp to = Timestamp.valueOf(end.plusDays(1).atStartOfDay()); // exclusive
        String hotelName = "Simple Bear Hotel";
        try (Connection conn = openConnection()) {
            // Tên khách sạn (nếu có)
            try (PreparedStatement st = conn.prepareStatement(
                    "SELECT TOP (1) HotelName FROM dbo.HOTEL"); ResultSet rs = st.executeQuery()) {
                if (rs.next() && rs.getString(1) != null && !rs.getString(1).isBlank()) {
                    hotelName = rs.getString(1).trim();
                }
            }
            // ---- DOANH THU ĐÃ THANH TOÁN (PAID + REFUNDED) ----
            try (PreparedStatement st = conn.prepareStatement(
                    "SELECT CAST(PaymentTime AS DATE) AS d,\n"
                    + "       COUNT(DISTINCT BookingID) AS bookings,\n"
                    + "       SUM(Amount) AS total\n"
                    + "FROM PAYMENT\n"
                    + "WHERE Status IN ('PAID', 'REFUNDED')\n"
                    + "  AND PaymentTime >= ?\n"
                    + "  AND PaymentTime <  ?\n"
                    + "GROUP BY CAST(PaymentTime AS DATE);")) {
                st.setTimestamp(1, from);
                st.setTimestamp(2, to);
                try (ResultSet rs = st.executeQuery()) {
                    while (rs.next()) {
                        LocalDate day = rs.getDate(1).toLocalDate();
                        bookings.put(day, rs.getInt(2));
                        collected.put(day, rs.getBigDecimal(3));
                    }
                }
            }
            // ---- SỐ TIỀN ĐÃ HOÀN (REFUNDED) ----
            try (PreparedStatement st = conn.prepareStatement(
                    "SELECT CAST(RefundedAt AS DATE) AS d,\n"
                    + "       SUM(Amount) AS total\n"
                    + "FROM dbo.BOOKING_REFUND\n"
                    + "WHERE RefundedAt >= ? \n"
                    + "  AND RefundedAt <  ? \n"
                    + "GROUP BY CAST(RefundedAt AS DATE);")) {
                st.setTimestamp(1, from);
                st.setTimestamp(2, to);
                try (ResultSet rs = st.executeQuery()) {
                    while (rs.next()) {
                        LocalDate day = rs.getDate(1).toLocalDate();
                        refunded.put(day, rs.getBigDecimal(2));
                    }
                }
            }
        }
        // -----------------------------------------------------------------
        // 3️⃣ Tạo workbook và viết dữ liệu
        // -----------------------------------------------------------------
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Revenue Report");
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            // Styles -------------------------------------------------------
            CellStyle titleStyle = reportStyle(workbook, true, 14, false, null);
            CellStyle headerStyle = reportStyle(workbook, true, 11, true, null);
            CellStyle textStyle = reportStyle(workbook, false, 11, true, null);
            CellStyle textDate = reportStyle(workbook, false, 11, false, null);
            CellStyle intStyle = reportStyle(workbook, false, 11, true, "0");
            CellStyle moneyStyle = reportStyle(workbook, false, 11, true, "#,##0");
            CellStyle totalLabel = reportStyle(workbook, true, 11, true, null);
            CellStyle totalValue = reportStyle(workbook, true, 11, true, "#,##0");
            // -------------------------------------------------------------
            // Tiêu đề
            putMerged(sheet, 1, titleStyle, hotelName);
            putMerged(sheet, 3, titleStyle, "Revenue Report By Date");
            putMerged(sheet, 5, textDate,
                    "From: " + begin.format(fmt) + "   To: " + end.format(fmt));
            // Header bảng
            int headerRow = 7;
            String[] titles = {"Date", "Bookings", "Total Collected", "Refunded", "Net Income"};
            Row header = sheet.createRow(headerRow);
            for (int i = 0; i < titles.length; i++) {
                Cell c = header.createCell(FIRST_COL + i);
                c.setCellValue(titles[i]);
                c.setCellStyle(headerStyle);
            }
            // Dữ liệu theo ngày
            int rowIdx = headerRow + 1;               // bắt đầu dòng dữ liệu
            int firstDataRow = rowIdx;                // lưu vị trí dòng dữ liệu đầu tiên
            for (LocalDate day = begin; !day.isAfter(end); day = day.plusDays(1)) {
                Row r = sheet.createRow(rowIdx);
                int excelRow = rowIdx + 1;            // Excel dùng chỉ số 1‑based
                BigDecimal colCollected = collected.getOrDefault(day, BigDecimal.ZERO);
                BigDecimal colRefunded = refunded.getOrDefault(day, BigDecimal.ZERO);
                // Cột A: ngày
                Cell c0 = r.createCell(FIRST_COL);
                c0.setCellValue(day.format(fmt));
                c0.setCellStyle(textStyle);
                // Cột B: bookings
                Cell c1 = r.createCell(FIRST_COL + 1);
                c1.setCellValue(bookings.getOrDefault(day, 0));
                c1.setCellStyle(intStyle);
                // Cột C: total collected
                Cell c2 = r.createCell(FIRST_COL + 2);
                c2.setCellValue(colCollected.doubleValue());
                c2.setCellStyle(moneyStyle);
                // Cột D: refunded
                Cell c3 = r.createCell(FIRST_COL + 3);
                c3.setCellValue(colRefunded.doubleValue());
                c3.setCellStyle(moneyStyle);
                // Cột E: Net Income = Collected - Refunded
                Cell c4 = r.createCell(FIRST_COL + 4);
                c4.setCellFormula("E" + excelRow + "-F" + excelRow);
                c4.setCellStyle(moneyStyle);
                rowIdx++;
            }
            int lastDataRow = rowIdx - 1;   // dòng cuối cùng chứa dữ liệu thực
            // -----------------------------------------------------------------
            // 4️⃣ Hàng Total (đặt **sau** dữ liệu)
            // -----------------------------------------------------------------
            Row totalRow = sheet.createRow(rowIdx);   // ngay dưới cùng dữ liệu
            // Nhãn "Total:" ở cột Date
            Cell totalLabelCell = totalRow.createCell(FIRST_COL);
            totalLabelCell.setCellValue("Total:");
            totalLabelCell.setCellStyle(totalLabel);
            // Tổng Bookings
            Cell totalBooking = totalRow.createCell(FIRST_COL + 1);
            totalBooking.setCellFormula("SUM(D" + (firstDataRow + 1) + ":D" + (lastDataRow + 1) + ")");
            totalBooking.setCellStyle(intStyle);
            // Tổng Collected
            Cell totalCollected = totalRow.createCell(FIRST_COL + 2);
            totalCollected.setCellFormula("SUM(E" + (firstDataRow + 1) + ":E" + (lastDataRow + 1) + ")");
            totalCollected.setCellStyle(moneyStyle);
            // Tổng Refunded
            Cell totalRefunded = totalRow.createCell(FIRST_COL + 3);
            totalRefunded.setCellFormula("SUM(F" + (firstDataRow + 1) + ":F" + (lastDataRow + 1) + ")");
            totalRefunded.setCellStyle(moneyStyle);
            // Tổng Net Income
            Cell totalNet = totalRow.createCell(FIRST_COL + 4);
            totalNet.setCellFormula("SUM(G" + (firstDataRow + 1) + ":G" + (lastDataRow + 1) + ")");
            totalNet.setCellStyle(moneyStyle);
            // -----------------------------------------------------------------
            // 5️⃣ Định dạng cột & tính lại công thức
            // -----------------------------------------------------------------
            sheet.setColumnWidth(0, 3 * 256);
            sheet.setColumnWidth(1, 3 * 256);
            for (int col = FIRST_COL; col <= FIRST_COL + 4; col++) {
                sheet.setColumnWidth(col, 20 * 256);
            }
            PrintSetup printSetup = sheet.getPrintSetup();
            printSetup.setLandscape(true);
            printSetup.setPaperSize(PrintSetup.A4_PAPERSIZE);

            sheet.setFitToPage(true);
            printSetup.setFitWidth((short) 1);
            printSetup.setFitHeight((short) 0);

            // Bảo đảm công thức được tính ngay (đặc biệt với LibreOffice, Google Sheets…)
            workbook.setForceFormulaRecalculation(true);
            XSSFFormulaEvaluator evaluator = new XSSFWorkbook().getCreationHelper()
                    .createFormulaEvaluator();   // or cast workbook to XSSFWorkbook
            XSSFFormulaEvaluator.evaluateAllFormulaCells(workbook);
            // -------------------------------------------------------------
            // Ghi ra output stream
            // -------------------------------------------------------------
            workbook.write(output);
        }
    }

    /**
     * Write a centered text into a row, merged across the 5 table columns.
     */
    private static void putMerged(Sheet sheet, int rowIndex, CellStyle style, String text) {
        Row row = sheet.createRow(rowIndex);
        for (int col = FIRST_COL; col <= FIRST_COL + 4; col++) {
            row.createCell(col).setCellStyle(style);
        }
        Cell cell = row.getCell(FIRST_COL);
        cell.setCellValue(text);
        sheet.addMergedRegion(new CellRangeAddress(rowIndex, rowIndex, FIRST_COL, FIRST_COL + 4));
    }

    /**
     * Centered cell style with optional bold, font size, thin border and number
     * format.
     */
    private static CellStyle reportStyle(Workbook workbook, boolean bold, int size, boolean border, String format) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(bold);
        font.setFontHeightInPoints((short) size);
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        if (border) {
            style.setBorderTop(BorderStyle.THIN);
            style.setBorderBottom(BorderStyle.THIN);
            style.setBorderLeft(BorderStyle.THIN);
            style.setBorderRight(BorderStyle.THIN);
        }
        if (format != null) {
            style.setDataFormat(workbook.createDataFormat().getFormat(format));
        }
        return style;
    }

    /**
     * Write the configured database tables to an Excel workbook.
     */
    public void exportDB(OutputStream output) throws Exception {
        try (Connection connection = openConnection(); Workbook workbook = new XSSFWorkbook()) {
            connection.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
            connection.setAutoCommit(false);
            try {
                lock(connection);
                CellStyle dateStyle = workbook.createCellStyle();
                dateStyle.setDataFormat(workbook.createDataFormat().getFormat("yyyy-mm-dd hh:mm:ss.000"));
                for (String table : TABLES) {
                    List<BackupColumn> columns = columns(connection, table);
                    Sheet sheet = workbook.createSheet(table);
                    Row header = sheet.createRow(0);
                    for (int i = 0; i < columns.size(); i++) {
                        header.createCell(i).setCellValue(columns.get(i).name);
                    }
                    String sql = "SELECT " + names(columns) + " FROM " + table(table);
                    List<BackupColumn> keys = keys(columns);
                    if (!keys.isEmpty()) {
                        sql += " ORDER BY " + names(keys);
                    }
                    try (PreparedStatement statement = connection.prepareStatement(sql); ResultSet rows = statement.executeQuery()) {
                        int rowIndex = 1;
                        while (rows.next()) {
                            Row row = sheet.createRow(rowIndex++);
                            for (int i = 0; i < columns.size(); i++) {
                                Object value = rows.getObject(i + 1);
                                Cell cell = row.createCell(i);
                                if (value == null) {
                                    continue;
                                }
                                if (value instanceof java.util.Date) {
                                    cell.setCellValue((java.util.Date) value);
                                    cell.setCellStyle(dateStyle);
                                } else if (value instanceof Boolean) {
                                    cell.setCellValue((Boolean) value);
                                } else if (value instanceof BigDecimal) {
                                    cell.setCellValue(((BigDecimal) value).toPlainString());
                                } // Keep identity values and codes exact, even beyond Excel's numeric precision.
                                else {
                                    cell.setCellValue(value.toString());
                                }
                            }
                        }
                    }
                }
                connection.commit();
                workbook.write(output);
            } catch (Exception exception) {
                rollback(connection, exception);
                throw exception;
            }
        }
    }

    protected void clearCache() {
        PersistenceManager.getEMF().getCache().evictAll();
    }

    private static Object value(Cell cell, int type) {
        if (cell == null || cell.getCellType() == CellType.BLANK) {
            return null;
        }
        if (cell.getCellType() == CellType.FORMULA) {
            throw new IllegalArgumentException("Formula cells are not supported in backups.");
        }
        String text = new DataFormatter(Locale.ROOT).formatCellValue(cell).trim();
        if (text.isEmpty()) {
            return cell.getCellType() == CellType.STRING && Set.of(Types.CHAR, Types.VARCHAR, Types.NCHAR,
                    Types.NVARCHAR, Types.LONGVARCHAR, Types.LONGNVARCHAR).contains(type) ? "" : null;
        }
        switch (type) {
            case Types.BIT:
            case Types.BOOLEAN:
                if ("true".equalsIgnoreCase(text) || "1".equals(text)) {
                    return true;
                }
                if ("false".equalsIgnoreCase(text) || "0".equals(text)) {
                    return false;
                }
                throw new IllegalArgumentException("Invalid boolean value: " + text);
            case Types.TINYINT:
            case Types.SMALLINT:
            case Types.INTEGER:
                return new BigDecimal(text).intValueExact();
            case Types.BIGINT:
                return new BigDecimal(text).longValueExact();
            case Types.NUMERIC:
            case Types.DECIMAL:
            case Types.FLOAT:
            case Types.REAL:
            case Types.DOUBLE:
                return cell.getCellType() == CellType.NUMERIC ? BigDecimal.valueOf(cell.getNumericCellValue()) : new BigDecimal(text);
            case Types.DATE:
                if (cell.getCellType() == CellType.NUMERIC) {
                    return new java.sql.Date(cell.getDateCellValue().getTime());
                }
                return java.sql.Date.valueOf(text.substring(0, 10));
            case Types.TIMESTAMP:
            case Types.TIMESTAMP_WITH_TIMEZONE:
                if (cell.getCellType() == CellType.NUMERIC) {
                    return new Timestamp(cell.getDateCellValue().getTime());
                }
                if (text.length() == 10) {
                    text += " 00:00:00";
                }
                return Timestamp.valueOf(text.replace('T', ' '));
            default:
                if (cell.getCellType() == CellType.NUMERIC) {
                    return BigDecimal.valueOf(cell.getNumericCellValue()).stripTrailingZeros().toPlainString();
                }
                return text;
        }
    }

    private static void bind(PreparedStatement statement, int index, BackupColumn column, Object value) throws SQLException {
        if (value == null) {
            statement.setNull(index, column.type);
        } else {
            statement.setObject(index, value, column.type);
        }
    }

    private static List<BackupColumn> columns(Connection connection, String table) throws SQLException {
        Set<String> keys = new HashSet<>();
        try (ResultSet rows = connection.getMetaData().getPrimaryKeys(null, "dbo", table)) {
            while (rows.next()) {
                keys.add(rows.getString("COLUMN_NAME"));
            }
        }
        Set<String> excluded = new HashSet<>();
        Set<String> identities = new HashSet<>();
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT name, is_identity FROM sys.columns WHERE object_id = OBJECT_ID(?) "
                + "AND (is_computed = 1 OR system_type_id = 189 OR is_identity = 1)")) {
            statement.setString(1, "dbo." + quote(table));
            try (ResultSet rows = statement.executeQuery()) {
                while (rows.next()) {
                    if (rows.getBoolean(2)) {
                        identities.add(rows.getString(1));
                    } else {
                        excluded.add(rows.getString(1));
                    }
                }
            }
        }
        List<BackupColumn> columns = new ArrayList<>();
        try (ResultSet rows = connection.getMetaData().getColumns(null, "dbo", table, null)) {
            while (rows.next()) {
                if (!table.equalsIgnoreCase(rows.getString("TABLE_NAME"))) {
                    continue;
                }
                String name = rows.getString("COLUMN_NAME");
                if (!excluded.contains(name)) {
                    columns.add(new BackupColumn(name, rows.getInt("DATA_TYPE"),
                            keys.contains(name), identities.contains(name)));
                }
            }
        }
        if (columns.isEmpty()) {
            throw new SQLException("Missing v6 table: " + table);
        }
        return columns;
    }

    private static List<BackupColumn> keys(List<BackupColumn> columns) {
        return columns.stream().filter(c -> c.key).collect(Collectors.toList());
    }

    private static String names(List<BackupColumn> columns) {
        return columns.stream().map(c -> quote(c.name)).collect(Collectors.joining(", "));
    }

    private static String quote(String identifier) {
        return "[" + identifier.replace("]", "]]") + "]";
    }

    private static String table(String name) {
        return "dbo." + quote(name);
    }

    private static boolean empty(Row row) {
        if (row == null) {
            return true;
        }
        for (Cell cell : row) {
            if (cell.getCellType() != CellType.BLANK && !cell.toString().trim().isEmpty()) {
                return false;
            }
        }
        return true;
    }

    private static List<String> enabledTriggers(Connection connection) throws SQLException {
        List<String> result = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT tr.name, t.name FROM sys.triggers tr JOIN sys.tables t ON t.object_id = tr.parent_id "
                + "JOIN sys.schemas s ON s.schema_id = t.schema_id WHERE s.name = 'dbo' AND tr.is_disabled = 0"); ResultSet rows = statement.executeQuery()) {
            while (rows.next()) {
                if (TABLES.contains(rows.getString(2))) {
                    result.add(quote(rows.getString(1)) + " ON " + table(rows.getString(2)));
                }
            }
        }
        return result;
    }

    private static void lock(Connection connection) throws SQLException {
        // Use SP_HBMS_ACQUIRE_WRITE_LOCK before restoring booking and wallet data.
        execute(connection, "SELECT TOP (1) UserID FROM dbo.USERS; EXEC dbo.SP_HBMS_ACQUIRE_WRITE_LOCK;");
    }

    private static void execute(Connection connection, String sql) throws SQLException {
        // Run session settings outside the driver's prepared SQL batch.
        try (Statement statement = connection.createStatement()) {
            drain(statement, statement.execute(sql));
        }
    }

    private static void drain(Statement statement, boolean result) throws SQLException {
        while (true) {
            if (result) {
                try (ResultSet rows = statement.getResultSet()) {
                    while (rows.next()) {
                        /* consume */ }
                }
            } else if (statement.getUpdateCount() == -1) {
                break;
            }
            result = statement.getMoreResults(Statement.CLOSE_CURRENT_RESULT);
        }
    }

    private static void rollback(Connection connection, Exception exception) {
        try {
            connection.rollback();
        } catch (SQLException rollback) {
            exception.addSuppressed(rollback);
        }
    }

    /**
     * Check that restored payments, refunds, and wallet entries match their
     * transactions.
     */
    private static void validateLedger(Connection connection) throws SQLException {
        execute(connection,
                "IF EXISTS (SELECT 1 FROM dbo.PAYMENT p LEFT JOIN dbo.[TRANSACTION] t ON t.TransactionID = p.TransactionID "
                + "WHERE p.Status IN ('PAID','REFUNDED') AND (t.TransactionID IS NULL OR t.TransactionType <> 'PAYMENT' OR t.TransactionValue <> p.Amount)) "
                + "THROW 51221, 'Restored payments do not match their central transactions.', 1; "
                + "IF EXISTS (SELECT 1 FROM dbo.BOOKING_REFUND r JOIN dbo.[TRANSACTION] t ON t.TransactionID = r.TransactionID "
                + "WHERE t.TransactionType <> 'REFUND' OR t.TransactionValue <> r.Amount) "
                + "THROW 51222, 'Restored refunds do not match their central transactions.', 1; "
                + "IF EXISTS (SELECT 1 FROM dbo.V_WALLET_BALANCE WHERE Balance < 0) THROW 51124, 'Restored wallet balance is negative.', 1; "
                + "IF EXISTS (SELECT 1 FROM dbo.CUSTOMER c LEFT JOIN dbo.CUSTOMER_WALLET w ON w.CustomerID = c.CustomerID "
                + "LEFT JOIN dbo.USER_BOOKING_CONTROL b ON b.UserID = c.UserID WHERE w.WalletID IS NULL OR b.UserID IS NULL) "
                + "THROW 51107, 'Restored customers require wallet and booking-control rows.', 1; "
                + "IF EXISTS (SELECT 1 FROM dbo.PAYMENT p OUTER APPLY (SELECT COALESCE(SUM(w.Amount),0) amount FROM dbo.WALLET_TRANSACTION w "
                + "WHERE w.PaymentID = p.PaymentID AND w.TransactionType = 'PAYMENT_DEBIT') d "
                + "WHERE d.amount <> CASE WHEN p.Status IN ('PAID','REFUNDED') THEN p.WalletAmount ELSE 0 END) "
                + "THROW 51122, 'Restored wallet debits differ from payment contributions.', 1; "
                + "IF EXISTS (SELECT 1 FROM dbo.BOOKING_REFUND r OUTER APPLY (SELECT COALESCE(SUM(w.Amount),0) amount "
                + "FROM dbo.WALLET_TRANSACTION w WHERE w.RefundID = r.RefundID AND w.TransactionType = 'REFUND_CREDIT') c "
                + "OUTER APPLY (SELECT COALESCE(SUM(p.Amount),0) amount FROM dbo.REFUND_PAYMENT p WHERE p.RefundID = r.RefundID) a "
                + "WHERE c.amount <> r.Amount OR a.amount <> r.Amount) THROW 51222, 'Restored refund allocations or wallet credits differ.', 1; "
                + "IF EXISTS (SELECT 1 FROM dbo.BOOKING b WHERE b.TotalAmount <> "
                + "COALESCE((SELECT SUM(d.Subtotal) FROM dbo.BOOKING_DETAIL d WHERE d.BookingID = b.BookingID),0) + "
                + "COALESCE((SELECT SUM(s.Subtotal) FROM dbo.BOOKING_SERVICE s WHERE s.BookingID = b.BookingID),0)) "
                + "THROW 51225, 'Restored booking totals differ from room and service lines.', 1; "
                + "IF EXISTS (SELECT 1 FROM dbo.BOOKING b JOIN dbo.V_BOOKING_ROOM_BALANCE r ON r.BookingID=b.BookingID "
                + "WHERE (b.BookingStatus IN ('CHECKED_IN','CHECKED_OUT') AND r.RoomRemainingAmount > 0) "
                + "OR (b.BookingStatus='CHECKED_OUT' AND r.ServiceRemainingAmount > 0)) "
                + "THROW 51226, 'Restored stay status has unpaid room or service charges.', 1; "
                + "IF EXISTS (SELECT 1 FROM dbo.ROOM_ASSIGNMENT a JOIN dbo.BOOKING_DETAIL da ON da.BookingDetailID=a.BookingDetailID "
                + "JOIN dbo.BOOKING ba ON ba.BookingID=da.BookingID JOIN dbo.ROOM_ASSIGNMENT z ON z.RoomID=a.RoomID AND z.AssignmentID>a.AssignmentID "
                + "JOIN dbo.BOOKING_DETAIL dz ON dz.BookingDetailID=z.BookingDetailID JOIN dbo.BOOKING bz ON bz.BookingID=dz.BookingID "
                + "WHERE ba.BookingStatus NOT IN ('CANCELLED','CHECKED_OUT') AND bz.BookingStatus NOT IN ('CANCELLED','CHECKED_OUT') "
                + "AND ba.CheckInDate < bz.CheckOutDate AND bz.CheckInDate < ba.CheckOutDate) "
                + "THROW 51227, 'Restored room assignments overlap.', 1;");
    }
}
