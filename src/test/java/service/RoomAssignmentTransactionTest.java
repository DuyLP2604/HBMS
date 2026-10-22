package service;

import entity.BookingDetail;
import java.lang.reflect.Proxy;
import java.sql.*;
import java.util.*;

public class RoomAssignmentTransactionTest {
    private static class FakeDatabase {
        boolean transaction;
        boolean locked;
        int pendingRooms;
        int committedRooms;
        int commits;
        int rollbacks;
        boolean pendingStatus;
        boolean committedStatus;
        String failAt;
        FakeDatabase(String failAt) { this.failAt = failAt; }
        Object defaultValue(Class<?> type) {
            if (type == boolean.class) return false;
            if (type == int.class) return 0;
            if (type == long.class) return 0L;
            return null;
        }
        Connection connection() {
            return (Connection) Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[]{Connection.class}, (o,m,a)->{
                switch (m.getName()) {
                    case "setAutoCommit": return null;
                    case "prepareStatement": return statement((String)a[0]);
                    case "commit":
                        if (!transaction || !pendingStatus) throw new AssertionError("Invalid or premature commit");
                        committedRooms=pendingRooms; committedStatus=pendingStatus; commits++; transaction=false; return null;
                    case "rollback":
                        pendingRooms=0; pendingStatus=false; transaction=false; rollbacks++; return null;
                    default: return defaultValue(m.getReturnType());
                }
            });
        }
        PreparedStatement statement(String sql) {
            return (PreparedStatement) Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[]{PreparedStatement.class}, (o,m,a)->{
                if (m.getName().equals("execute")) {
                    // SQL Server starts an implicit transaction when a table is read, not merely when JDBC disables autocommit.
                    if (sql.contains("SELECT TOP (1) UserID FROM dbo.USERS")) transaction=true;
                    if (sql.contains("sp_getapplock")) {
                        if (!transaction) throw new SQLException("Transaction required", "S0001", 15626);
                        if ("lock".equals(failAt)) throw new SQLException("Busy", "S0001", 51000);
                        locked=true;
                    } else if (sql.contains("SP_ASSIGN_ROOM")) {
                        if (!transaction || !locked) throw new AssertionError("Unprotected assignment");
                        if ("room2".equals(failAt) && pendingRooms==1) throw new SQLException("Room conflict", "S0001", 50000);
                        pendingRooms++;
                    } else if (sql.contains("SP_SET_BOOKING_STATUS")) {
                        if ("status".equals(failAt)) throw new SQLException("Incomplete assignments", "S0001", 51064);
                        if (pendingRooms!=2) throw new AssertionError("Expected both rooms before status update");
                        pendingStatus=true;
                    }
                    return false;
                }
                if (m.getName().equals("getUpdateCount")) return -1;
                return defaultValue(m.getReturnType());
            });
        }
    }
    private static void run(String failAt) throws Exception {
        FakeDatabase db = new FakeDatabase(failAt);
        BookingDetail detail = new BookingDetail(); detail.setBookingDetailID(1);
        try {
            new RoomAssignmentService().writeAssignments(db.connection(), "BK0041", List.of(detail), Map.of(1,List.of("R01","R02")), 10);
            if (failAt!=null) throw new AssertionError("Failure expected");
            if (db.commits!=1 || db.rollbacks!=0 || db.committedRooms!=2 || !db.committedStatus) throw new AssertionError("Non-atomic success");
        } catch (SQLException error) {
            if (failAt==null) throw error;
            if (db.commits!=0 || db.rollbacks!=1 || db.committedRooms!=0 || db.committedStatus || db.pendingRooms!=0) throw new AssertionError("Partial assignment survived rollback");
        }
    }
    public static void main(String[] args) throws Exception {
        run(null); run("lock"); run("room2"); run("status");
        String permission = RoomAssignmentService.assignmentError(new SQLException("raw database diagnostic", "S0001",229));
        if (!permission.contains("permission") || !permission.contains("229") || permission.contains("raw database diagnostic")) throw new AssertionError("Misleading permission error");
        String unknown = RoomAssignmentService.assignmentError(new SQLException("private SQL text", "S0001",15626));
        if (!unknown.contains("15626") || unknown.contains("private SQL text")) throw new AssertionError("Missing diagnostic code");
        System.out.println("PASS: 6 JDBC-mock checks (atomic success, lock failure, partial-room rollback, status rollback, error classification)");
    }
}
