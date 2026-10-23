package service;

import jakarta.persistence.EntityManager;
import java.io.InputStream;
import java.io.OutputStream;

public class WorkSheetService {

    public void importDatabase(InputStream in, EntityManager em) throws Exception {
        new ImportDataService().importDB(in, em);
    }

    public void exportDatabase(OutputStream out) throws Exception {
        new ExportDataService().exportDB(out);
    }
}
