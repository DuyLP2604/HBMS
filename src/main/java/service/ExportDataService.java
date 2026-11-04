package service;

import java.io.OutputStream;

public class ExportDataService {
    public void exportDB(OutputStream output) throws Exception {
        new DatabaseBackupService().exportDB(output);
    }
}
