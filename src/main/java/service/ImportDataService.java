package service;

import java.io.InputStream;

public class ImportDataService {
    public void importDB(InputStream input) throws Exception {
        new DatabaseBackupService().importDB(input);
    }
}
