package service;

import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import util.PersistenceManager;

/** Excel backup of physical v6 tables, independent of JPA relationships. */
public class DatabaseBackupService {
    private static final List<String> TABLES = List.of("HOTEL", "NATIONALITY", "ROOM_TYPE", "SERVICE",
            "PAYMENTMETHOD", "USERS", "EMPLOYEE", "CUSTOMER", "USER_BOOKING_CONTROL", "CUSTOMER_WALLET",
            "ROOM", "COMPLAINT", "BOOKING", "BOOKING_DETAIL", "BOOKING_SERVICE", "ROOM_ASSIGNMENT",
            "TRANSACTION", "PAYMENT", "BOOKING_REFUND", "REFUND_PAYMENT", "WALLET_TRANSACTION",
            "BOOKING_LOCK_HISTORY", "INVOICE", "INVOICE_TRANSACTION");

    protected Connection openConnection() throws SQLException { return PersistenceManager.openConnection(); }

    public void exportDB(OutputStream output) throws Exception {
        try (Connection connection = openConnection(); Workbook workbook = new XSSFWorkbook()) {
            connection.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
            connection.setAutoCommit(false);
            try {
                lock(connection);
                CellStyle dateStyle = workbook.createCellStyle();
                dateStyle.setDataFormat(workbook.createDataFormat().getFormat("yyyy-mm-dd hh:mm:ss.000"));
                for (String table : TABLES) {
                    List<Column> columns = columns(connection, table);
                    Sheet sheet = workbook.createSheet(table);
                    Row header = sheet.createRow(0);
                    for (int i = 0; i < columns.size(); i++) header.createCell(i).setCellValue(columns.get(i).name);
                    String sql = "SELECT " + names(columns) + " FROM " + table(table);
                    List<Column> keys = keys(columns);
                    if (!keys.isEmpty()) sql += " ORDER BY " + names(keys);
                    try (PreparedStatement statement = connection.prepareStatement(sql); ResultSet rows = statement.executeQuery()) {
                        int rowIndex = 1;
                        while (rows.next()) {
                            Row row = sheet.createRow(rowIndex++);
                            for (int i = 0; i < columns.size(); i++) {
                                Object value = rows.getObject(i + 1);
                                Cell cell = row.createCell(i);
                                if (value == null) continue;
                                if (value instanceof java.util.Date) {
                                    cell.setCellValue((java.util.Date) value);
                                    cell.setCellStyle(dateStyle);
                                } else if (value instanceof Boolean) cell.setCellValue((Boolean) value);
                                else if (value instanceof BigDecimal) cell.setCellValue(((BigDecimal) value).toPlainString());
                                // Keep identity values and codes exact, even beyond Excel's numeric precision.
                                else cell.setCellValue(value.toString());
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

    public void importDB(InputStream input) throws Exception {
        try (Workbook workbook = WorkbookFactory.create(input); Connection connection = openConnection()) {
            if (TABLES.stream().noneMatch(name -> workbook.getSheet(name) != null)) {
                throw new IllegalArgumentException("The file contains no HBMS backup sheets.");
            }
            connection.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
            connection.setAutoCommit(false);
            try {
                lock(connection);
                List<String> triggers = enabledTriggers(connection);
                for (String trigger : triggers) execute(connection, "DISABLE TRIGGER " + trigger);
                for (String table : TABLES) {
                    Sheet sheet = workbook.getSheet(table);
                    if (sheet != null) importSheet(connection, table, sheet);
                }
                validateLedger(connection);
                for (String trigger : triggers) execute(connection, "ENABLE TRIGGER " + trigger);
                connection.commit();
            } catch (Exception exception) {
                // Trigger changes, identities and all restored rows roll back together.
                rollback(connection, exception);
                throw exception;
            }
        }
        try { clearCache(); }
        catch (RuntimeException ignored) { /* A committed restore must not be reported as failed. */ }
    }

    protected void clearCache() { PersistenceManager.getEMF().getCache().evictAll(); }

    private void importSheet(Connection connection, String table, Sheet sheet) throws Exception {
        Row header = sheet.getRow(0);
        if (header == null) return;
        List<Column> available = columns(connection, table);
        Map<String, Integer> headers = new HashMap<>();
        DataFormatter formatter = new DataFormatter(Locale.ROOT);
        for (Cell cell : header) {
            String name = formatter.formatCellValue(cell).trim().toUpperCase(Locale.ROOT);
            if (!name.isEmpty() && headers.put(name, cell.getColumnIndex()) != null) {
                throw new IllegalArgumentException(table + ": duplicate column " + name);
            }
        }
        List<Column> selected = available.stream()
                .filter(c -> headers.containsKey(c.name.toUpperCase(Locale.ROOT))).collect(Collectors.toList());
        List<Column> primary = keys(available);
        if (primary.isEmpty() || !selected.containsAll(primary)) {
            throw new IllegalArgumentException(table + ": the backup is missing primary key columns.");
        }
        List<Column> updates = selected.stream().filter(c -> !c.key && !c.identity).collect(Collectors.toList());
        String where = primary.stream().map(c -> quote(c.name) + " = ?").collect(Collectors.joining(" AND "));
        String insert = "INSERT INTO " + table(table) + " (" + names(selected) + ") VALUES ("
                + selected.stream().map(c -> "?").collect(Collectors.joining(", ")) + ")";
        String sql;
        if (updates.isEmpty()) {
            sql = "IF NOT EXISTS (SELECT 1 FROM " + table(table) + " WHERE " + where + ") " + insert;
        } else {
            sql = "UPDATE " + table(table) + " SET "
                    + updates.stream().map(c -> quote(c.name) + " = ?").collect(Collectors.joining(", "))
                    + " WHERE " + where + "; IF @@ROWCOUNT = 0 " + insert;
        }
        boolean identity = selected.stream().anyMatch(c -> c.identity);
        if (identity) execute(connection, "SET IDENTITY_INSERT " + table(table) + " ON");
        Exception failure = null;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            for (int index = 1; index <= sheet.getLastRowNum(); index++) {
                Row row = sheet.getRow(index);
                if (empty(row)) continue;
                try {
                    Map<Column, Object> values = new HashMap<>();
                    for (Column column : selected) {
                        Object value = value(row.getCell(headers.get(column.name.toUpperCase(Locale.ROOT))), column.type);
                        if (column.key && value == null) throw new IllegalArgumentException("Missing key " + column.name);
                        values.put(column, value);
                    }
                    int parameter = 1;
                    for (Column column : updates) bind(statement, parameter++, column, values.get(column));
                    for (Column column : primary) bind(statement, parameter++, column, values.get(column));
                    for (Column column : selected) bind(statement, parameter++, column, values.get(column));
                    drain(statement);
                } catch (Exception exception) {
                    throw new IllegalArgumentException(table + " row " + (index + 1) + ": " + exception.getMessage(), exception);
                }
            }
        } catch (Exception exception) {
            failure = exception;
            throw exception;
        } finally {
            if (identity) {
                try { execute(connection, "SET IDENTITY_INSERT " + table(table) + " OFF"); }
                catch (SQLException exception) {
                    if (failure != null) failure.addSuppressed(exception);
                    else throw exception;
                }
            }
        }
    }

    private static Object value(Cell cell, int type) {
        if (cell == null || cell.getCellType() == CellType.BLANK) return null;
        if (cell.getCellType() == CellType.FORMULA) throw new IllegalArgumentException("Formula cells are not supported in backups.");
        String text = new DataFormatter(Locale.ROOT).formatCellValue(cell).trim();
        if (text.isEmpty()) {
            return cell.getCellType() == CellType.STRING && Set.of(Types.CHAR, Types.VARCHAR, Types.NCHAR,
                    Types.NVARCHAR, Types.LONGVARCHAR, Types.LONGNVARCHAR).contains(type) ? "" : null;
        }
        switch (type) {
            case Types.BIT: case Types.BOOLEAN:
                if ("true".equalsIgnoreCase(text) || "1".equals(text)) return true;
                if ("false".equalsIgnoreCase(text) || "0".equals(text)) return false;
                throw new IllegalArgumentException("Invalid boolean value: " + text);
            case Types.TINYINT: case Types.SMALLINT: case Types.INTEGER:
                return new BigDecimal(text).intValueExact();
            case Types.BIGINT: return new BigDecimal(text).longValueExact();
            case Types.NUMERIC: case Types.DECIMAL: case Types.FLOAT: case Types.REAL: case Types.DOUBLE:
                return cell.getCellType() == CellType.NUMERIC ? BigDecimal.valueOf(cell.getNumericCellValue()) : new BigDecimal(text);
            case Types.DATE:
                if (cell.getCellType() == CellType.NUMERIC) return new java.sql.Date(cell.getDateCellValue().getTime());
                return java.sql.Date.valueOf(text.substring(0, 10));
            case Types.TIMESTAMP: case Types.TIMESTAMP_WITH_TIMEZONE:
                if (cell.getCellType() == CellType.NUMERIC) return new Timestamp(cell.getDateCellValue().getTime());
                if (text.length() == 10) text += " 00:00:00";
                return Timestamp.valueOf(text.replace('T', ' '));
            default:
                if (cell.getCellType() == CellType.NUMERIC) return BigDecimal.valueOf(cell.getNumericCellValue()).stripTrailingZeros().toPlainString();
                return text;
        }
    }

    private static void bind(PreparedStatement statement, int index, Column column, Object value) throws SQLException {
        if (value == null) statement.setNull(index, column.type);
        else statement.setObject(index, value, column.type);
    }

    private static List<Column> columns(Connection connection, String table) throws SQLException {
        Set<String> keys = new HashSet<>();
        try (ResultSet rows = connection.getMetaData().getPrimaryKeys(null, "dbo", table)) {
            while (rows.next()) keys.add(rows.getString("COLUMN_NAME"));
        }
        Set<String> excluded = new HashSet<>();
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT name FROM sys.columns WHERE object_id = OBJECT_ID(?) AND (is_computed = 1 OR system_type_id = 189)")) {
            statement.setString(1, "dbo." + quote(table));
            try (ResultSet rows = statement.executeQuery()) { while (rows.next()) excluded.add(rows.getString(1)); }
        }
        List<Column> columns = new ArrayList<>();
        try (ResultSet rows = connection.getMetaData().getColumns(null, "dbo", table, null)) {
            while (rows.next()) {
                if (!table.equalsIgnoreCase(rows.getString("TABLE_NAME"))) continue;
                String name = rows.getString("COLUMN_NAME");
                if (!excluded.contains(name)) columns.add(new Column(name, rows.getInt("DATA_TYPE"),
                        keys.contains(name), "YES".equals(rows.getString("IS_AUTOINCREMENT"))));
            }
        }
        if (columns.isEmpty()) throw new SQLException("Missing v6 table: " + table);
        return columns;
    }

    private static List<Column> keys(List<Column> columns) {
        return columns.stream().filter(c -> c.key).collect(Collectors.toList());
    }
    private static String names(List<Column> columns) {
        return columns.stream().map(c -> quote(c.name)).collect(Collectors.joining(", "));
    }
    private static String quote(String identifier) { return "[" + identifier.replace("]", "]]") + "]"; }
    private static String table(String name) { return "dbo." + quote(name); }
    private static boolean empty(Row row) {
        if (row == null) return true;
        for (Cell cell : row) if (cell.getCellType() != CellType.BLANK && !cell.toString().trim().isEmpty()) return false;
        return true;
    }
    private static List<String> enabledTriggers(Connection connection) throws SQLException {
        List<String> result = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT tr.name, t.name FROM sys.triggers tr JOIN sys.tables t ON t.object_id = tr.parent_id "
                + "JOIN sys.schemas s ON s.schema_id = t.schema_id WHERE s.name = 'dbo' AND tr.is_disabled = 0");
                ResultSet rows = statement.executeQuery()) {
            while (rows.next()) if (TABLES.contains(rows.getString(2))) result.add(quote(rows.getString(1)) + " ON " + table(rows.getString(2)));
        }
        return result;
    }
    private static void lock(Connection connection) throws SQLException {
        execute(connection, "SELECT TOP (1) UserID FROM dbo.USERS; EXEC dbo.SP_HBMS_ACQUIRE_WRITE_LOCK;");
    }
    private static void execute(Connection connection, String sql) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(sql)) { drain(statement); }
    }
    private static void drain(PreparedStatement statement) throws SQLException {
        boolean result = statement.execute();
        while (true) {
            if (result) {
                try (ResultSet rows = statement.getResultSet()) { while (rows.next()) { /* consume */ } }
            } else if (statement.getUpdateCount() == -1) break;
            result = statement.getMoreResults(Statement.CLOSE_CURRENT_RESULT);
        }
    }
    private static void rollback(Connection connection, Exception exception) {
        try { connection.rollback(); } catch (SQLException rollback) { exception.addSuppressed(rollback); }
    }
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
    private static final class Column {
        final String name;
        final int type;
        final boolean key;
        final boolean identity;
        Column(String name, int type, boolean key, boolean identity) {
            this.name = name; this.type = type; this.key = key; this.identity = identity;
        }
    }
}
