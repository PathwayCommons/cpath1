package org.mskcc.pathdb.task;

import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.jdom.JDOMException;
import org.mskcc.pathdb.model.ExternalDatabaseRecord;
import org.mskcc.pathdb.schemas.externalDb.ExternalDbLinkTester;
import org.mskcc.pathdb.schemas.externalDb.ExternalDbXmlUtil;
import org.mskcc.pathdb.sql.dao.DaoException;
import org.mskcc.pathdb.sql.dao.DaoExternalDb;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Task to Import an XML File of External Databases.
 *
 * @author Ethan Cerami
 */
public class ImportExternalDbTask extends Task {
    private File file;
    private boolean validateLinks;
    private static final String WARNING_MSG =
            "-->  Warning!  The sample URL provided did not work.  "
            + "The external database will be stored anyway, but you may "
            + "want to double check the url pattern / sample id.";

    /**
     * Constructor.
     * @param file  File.
     * @param consoleMode   Console Mode Flag.
     */
    public ImportExternalDbTask(File file, boolean consoleMode,
            boolean validateLinks) {
        super("Import External Database XML File", consoleMode);
        this.file = file;
        this.validateLinks = validateLinks;
    }

    /**
     * Imports the Specified XML File of External Databases.
     *
     * @return number of external databases stored.
     * 
     * @throws IOException      I/O Error.
     * @throws JDOMException    XML Error.
     * @throws DaoException     Database Access Error.
     */
    public int importFile() throws IOException, JDOMException,
            DaoException {
        DaoExternalDb daoExternalDb = new DaoExternalDb();
        ProgressMonitor pMonitor = getProgressMonitor();
        ExternalDbXmlUtil util = new ExternalDbXmlUtil(file);
        ArrayList dbList = util.getExternalDbList();
        for (int i = 0; i < dbList.size(); i++) {
            ExternalDatabaseRecord dbRecord =
                    (ExternalDatabaseRecord) dbList.get(i);
            pMonitor.setCurrentMessage("Adding External Database:  "
                    + dbRecord.getName());
            if (validateLinks) {
                if (dbRecord.getUrl() != null
                        && dbRecord.getSampleId() != null) {
                    pMonitor.setCurrentMessage("Checking Sample URL:  "
                            + dbRecord.getUrlWithId(dbRecord.getSampleId()));
                        checkUrl(dbRecord, pMonitor);
                }
            }
            daoExternalDb.addRecord(dbRecord);
        }
        return dbList.size();
    }

    private void checkUrl(ExternalDatabaseRecord dbRecord,
            ProgressMonitor pMonitor) {
        try {
            int statusCode = ExternalDbLinkTester.checkSampleLink
                    (dbRecord);
            pMonitor.setCurrentMessage("-->  Response:  " + statusCode
                    + ", " + HttpStatus.getStatusText(statusCode) + "\n");
            if (statusCode != HttpStatus.SC_OK) {
                pMonitor.setCurrentMessage(WARNING_MSG);
            }
        } catch (HttpException e) {
            pMonitor.setCurrentMessage(WARNING_MSG
                    + "  Error Message:  " + e.getReasonCode()
                    + ", " + e.getReason());
        } catch (IOException e) {
            pMonitor.setCurrentMessage(WARNING_MSG
                    + "  Error Message:  " + e.getMessage());
        }
    }
}