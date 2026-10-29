package service;

import java.io.*;
import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.sql.*;
import java.util.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/** Exercises actual Excel bytes and JDBC transaction/error handling without a live server. */
public class DatabaseBackupRegressionTest {
    static int checks;
    static void check(boolean condition, String message) { if (!condition) throw new AssertionError(message); checks++; }
    static Object fallback(Class<?> type) {
        if (type == boolean.class) return false;
        if (type == int.class) return 0;
        if (type == long.class) return 0L;
        return null;
    }
    static class Database {
        int commits, rollbacks, inserts;
        boolean triggersEnabled=true, identityInsert=false, lateFailure=false;
        Map<Integer,Object> restored;
        ResultSet rows(List<Map<Object,Object>> data) {
            int[] index={-1};
            return (ResultSet)Proxy.newProxyInstance(getClass().getClassLoader(),new Class<?>[]{ResultSet.class},(o,m,a)->{
                if (m.getName().equals("next")) return ++index[0]<data.size();
                if (m.getName().startsWith("get") && a!=null && a.length==1) {
                    Object value=data.get(index[0]).get(a[0]);
                    if (m.getName().equals("getString")) return value==null?null:value.toString();
                    if (m.getName().equals("getInt")) return ((Number)value).intValue();
                    return value;
                }
                return fallback(m.getReturnType());
            });
        }
        Map<Object,Object> column(String table,String name,int type,boolean identity) {
            Map<Object,Object> row=new HashMap<>();
            row.put("TABLE_NAME",table); row.put("COLUMN_NAME",name); row.put("DATA_TYPE",type);
            row.put("IS_AUTOINCREMENT",identity?"YES":"NO");return row;
        }
        DatabaseMetaData metadata() {
            return (DatabaseMetaData)Proxy.newProxyInstance(getClass().getClassLoader(),new Class<?>[]{DatabaseMetaData.class},(o,m,a)->{
                String table=a!=null && a.length>2?(String)a[2]:"";
                if (m.getName().equals("getPrimaryKeys")) return rows(List.of(Map.of("COLUMN_NAME",table.equals("CUSTOMER_WALLET")?"WalletID":"ID")));
                if (m.getName().equals("getColumns")) {
                    if (table.equals("CUSTOMER_WALLET")) return rows(List.of(column(table,"WalletID",Types.BIGINT,true),column(table,"CustomerID",Types.CHAR,false),column(table,"ComputedBalance",Types.DECIMAL,false)));
                    return rows(List.of(column(table,"ID",Types.INTEGER,false),column(table,"ComputedBalance",Types.DECIMAL,false)));
                }
                return fallback(m.getReturnType());
            });
        }
        Connection connection() {
            return (Connection)Proxy.newProxyInstance(getClass().getClassLoader(),new Class<?>[]{Connection.class},(o,m,a)->{
                if (m.getName().equals("getMetaData")) return metadata();
                if (m.getName().equals("prepareStatement")) return statement((String)a[0]);
                if (m.getName().equals("commit")) commits++;
                if (m.getName().equals("rollback")) {rollbacks++;triggersEnabled=true;identityInsert=false;restored=null;}
                return fallback(m.getReturnType());
            });
        }
        PreparedStatement statement(String sql) {
            Map<Integer,Object> parameters=new HashMap<>();
            boolean write=sql.startsWith("UPDATE") || sql.startsWith("IF NOT EXISTS");
            return (PreparedStatement)Proxy.newProxyInstance(getClass().getClassLoader(),new Class<?>[]{PreparedStatement.class},(o,m,a)->{
                String name=m.getName();
                if (name.startsWith("set") && a!=null && a[0] instanceof Integer) {parameters.put((Integer)a[0],name.equals("setNull")?null:a[1]);return null;}
                if (name.equals("executeQuery")) {
                    if (sql.contains("sys.columns")) return rows(List.of(Map.of(1,"ComputedBalance")));
                    if (sql.contains("sys.triggers")) return rows(List.of(Map.of(1,"TR_WALLET_ValidateTransaction",2,"WALLET_TRANSACTION")));
                    if (sql.contains("FROM dbo.[CUSTOMER_WALLET]")) return rows(List.of(Map.of(1,9007199254740993L,2,"CUS001")));
                    return rows(List.of());
                }
                if (name.equals("execute")) {
                    if (sql.startsWith("DISABLE TRIGGER")) triggersEnabled=false;
                    if (sql.startsWith("ENABLE TRIGGER")) triggersEnabled=true;
                    if (sql.startsWith("SET IDENTITY_INSERT")) identityInsert=sql.endsWith(" ON");
                    if (write) {check(identityInsert,"Wallet identity must be preserved on the same connection");inserts++;restored=new HashMap<>(parameters);}
                    return write;
                }
                if (name.equals("getResultSet")) return rows(List.of());
                if (name.equals("getUpdateCount")) return -1;
                if (name.equals("getMoreResults")) {if(write && lateFailure)throw new SQLException("Late restore error");return false;}
                return fallback(m.getReturnType());
            });
        }
    }
    static class Backups extends DatabaseBackupService {
        final Database database=new Database();
        @Override protected Connection openConnection() {return database.connection();}
        @Override protected void clearCache() { }
    }
    public static void main(String[] args) throws Exception {
        Backups exporter=new Backups();ByteArrayOutputStream bytes=new ByteArrayOutputStream();exporter.exportDB(bytes);
        byte[] workbookBytes=bytes.toByteArray();
        try(Workbook workbook=WorkbookFactory.create(new ByteArrayInputStream(workbookBytes))) {
            check(workbook.getNumberOfSheets()==24,"All physical business and ledger tables are exported");
            Sheet wallet=workbook.getSheet("CUSTOMER_WALLET");
            check(wallet.getRow(0).getLastCellNum()==2,"Computed columns are excluded");
            check("9007199254740993".equals(wallet.getRow(1).getCell(0).getStringCellValue()),"Bigint identity survives Excel numeric precision limits");
        }
        Backups importer=new Backups();importer.importDB(new ByteArrayInputStream(workbookBytes));
        check(importer.database.commits==1 && importer.database.rollbacks==0,"Restore commits once");
        check(importer.database.triggersEnabled && !importer.database.identityInsert,"Restore restores trigger and identity state");
        check(importer.database.restored.containsValue(9007199254740993L),"Original identity is bound as an exact long");
        Backups failed=new Backups();failed.database.lateFailure=true;
        try {failed.importDB(new ByteArrayInputStream(workbookBytes));throw new AssertionError("Expected restore failure");}
        catch(IllegalArgumentException expected) { }
        check(failed.database.commits==0 && failed.database.rollbacks==1,"Errors after a result set roll back the full restore");
        check(failed.database.triggersEnabled && !failed.database.identityInsert && failed.database.restored==null,"Failure leaves no restored partial rows or changed trigger state");
        try(Workbook workbook=WorkbookFactory.create(new ByteArrayInputStream(workbookBytes))) {
            workbook.getSheet("CUSTOMER_WALLET").getRow(1).getCell(0).setBlank();
            ByteArrayOutputStream invalid=new ByteArrayOutputStream();workbook.write(invalid);
            Backups missingKey=new Backups();
            try{missingKey.importDB(new ByteArrayInputStream(invalid.toByteArray()));throw new AssertionError("Expected missing key rejection");}
            catch(IllegalArgumentException expected){check(expected.getMessage().contains("CUSTOMER_WALLET row 2"),"Import identifies the invalid sheet and row");}
            check(missingKey.database.commits==0 && missingKey.database.rollbacks==1,"Invalid keys cannot partially restore a backup");
        }
        System.out.println("PASS: "+checks+" Excel backup/restore regression checks");
    }
}
