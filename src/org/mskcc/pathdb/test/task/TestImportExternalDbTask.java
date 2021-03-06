// $Id: TestImportExternalDbTask.java,v 1.10 2007-01-02 16:56:14 cerami Exp $
//------------------------------------------------------------------------------
/** Copyright (c) 2006 Memorial Sloan-Kettering Cancer Center.
 **
 ** Code written by: Ethan Cerami
 ** Authors: Ethan Cerami, Gary Bader, Chris Sander
 **
 ** This library is free software; you can redistribute it and/or modify it
 ** under the terms of the GNU Lesser General Public License as published
 ** by the Free Software Foundation; either version 2.1 of the License, or
 ** any later version.
 **
 ** This library is distributed in the hope that it will be useful, but
 ** WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
 ** MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
 ** documentation provided hereunder is on an "as is" basis, and
 ** Memorial Sloan-Kettering Cancer Center
 ** has no obligations to provide maintenance, support,
 ** updates, enhancements or modifications.  In no event shall
 ** Memorial Sloan-Kettering Cancer Center
 ** be liable to any party for direct, indirect, special,
 ** incidental or consequential damages, including lost profits, arising
 ** out of the use of this software and its documentation, even if
 ** Memorial Sloan-Kettering Cancer Center
 ** has been advised of the possibility of such damage.  See
 ** the GNU Lesser General Public License for more details.
 **
 ** You should have received a copy of the GNU Lesser General Public License
 ** along with this library; if not, write to the Free Software Foundation,
 ** Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 **/
package org.mskcc.pathdb.test.task;

import junit.framework.TestCase;
import org.mskcc.pathdb.model.ExternalDatabaseRecord;
import org.mskcc.pathdb.sql.dao.DaoException;
import org.mskcc.pathdb.sql.dao.DaoExternalDb;
import org.mskcc.pathdb.task.ImportExternalDbTask;
import org.mskcc.pathdb.util.cache.EhCache;

import javax.swing.*;
import java.io.File;

/**
 * Tests the Importing of External Databases.
 *
 * @author Ethan Cerami
 */
public class TestImportExternalDbTask extends TestCase {

    /**
     * Tests the Importing of External Databases.
     *
     * @throws Exception All Exceptions.
     */
    public void testImport() throws Exception {
        //  Start Cache with Clean Slate
        EhCache.initCache();
        EhCache.resetAllCaches();

        File file = new File("testData/externalDb/external_db.xml");
        ImportExternalDbTask task = new ImportExternalDbTask(file, false,
                false);
        int numRecords = task.importFile();
        assertEquals(2, numRecords);

        DaoExternalDb dao = new DaoExternalDb();
        ExternalDatabaseRecord dbRecord = dao.getRecordByTerm("YHO");
        assertEquals("Yahoo", dbRecord.getName());
        assertEquals("http://www.yahoo.com", dbRecord.getHomePageUrl());
        assertEquals("XYZ123", dbRecord.getPathGuideId());

        //  Verify icon file extension was set correctly
        dbRecord = dao.getRecordByTerm("YHO");
        assertEquals ("png", dbRecord.getIconFileExtension());
    }

    /**
     * Gets Test Description.
     *
     * @return Description.
     */
    public String getName() {
        return "Test that we can successfully import a list of "
                + "external database records into MySQL";
    }

}
