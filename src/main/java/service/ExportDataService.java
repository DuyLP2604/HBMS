package service;

import java.io.OutputStream;

/** Writes database backup data to an Excel workbook. */
public class ExportDataService {
    /** Pass the output stream to the database backup exporter. */
    public void exportDB(OutputStream output) throws Exception {
        new DatabaseBackupService().exportDB(output);
    }
}
