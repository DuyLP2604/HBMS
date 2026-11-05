package service;

import java.io.InputStream;

/** Restores database data from an Excel workbook. */
public class ImportDataService {
    /** Pass the uploaded workbook to the database backup importer. */
    public void importDB(InputStream input) throws Exception {
        new DatabaseBackupService().importDB(input);
    }
}
