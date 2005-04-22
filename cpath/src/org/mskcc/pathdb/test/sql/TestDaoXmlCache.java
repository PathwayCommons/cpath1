/** Copyright (c) 2004 Memorial Sloan-Kettering Cancer Center.
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
package org.mskcc.pathdb.test.sql;

import junit.framework.TestCase;
import org.mskcc.pathdb.model.XmlCacheRecord;
import org.mskcc.pathdb.model.XmlRecordType;
import org.mskcc.pathdb.sql.assembly.XmlAssembly;
import org.mskcc.pathdb.sql.assembly.XmlAssemblyFactory;
import org.mskcc.pathdb.sql.dao.DaoXmlCache;
import org.mskcc.pathdb.xdebug.XDebug;

import java.util.ArrayList;

/**
 * Tests the DaoXmlCache Class.
 *
 * @author Ethan Cerami
 */
public class TestDaoXmlCache extends TestCase {
    private static final String HASH_KEY = "sander#bader#123";
    private static final String URL = "webservice.do?example";

    /**
     * Tests DaoXmlCache.
     *
     * @throws Exception All Exceptions.
     */
    public void testAccess() throws Exception {
        XDebug xdebug = new XDebug();
        DaoXmlCache dao = new DaoXmlCache(xdebug);

        //  Clear Cache (starts with clean slate)
        dao.deleteAllRecords();

        //  Add a new record, and verify success.
        XmlAssembly assembly = XmlAssemblyFactory.createXmlAssembly
                (4, 1, xdebug);
        String originalXml = assembly.getXmlString();
        assembly.setNumHits(100);

        boolean success = dao.addRecord(HASH_KEY, URL, assembly);
        assertTrue(success);

        //  Get the record, and match xml content.
        assembly = dao.getXmlAssemblyByKey(HASH_KEY);
        assertTrue(assembly.getXmlString().length() > 50);
        assertEquals(originalXml, assembly.getXmlString());
        assertEquals(100, assembly.getNumHits());
        assertEquals(XmlRecordType.PSI_MI, assembly.getXmlType());

        //  Update the record, and verify success.
        assembly = XmlAssemblyFactory.createXmlAssembly(4, 1, xdebug);
        assembly.setNumHits(200);

        success = dao.updateXmlAssemblyByKey(HASH_KEY, assembly);
        assertTrue(success);

        //  Get the record again, and match against new xml content.
        assembly = dao.getXmlAssemblyByKey(HASH_KEY);
        assertTrue(assembly.getXmlString().length() > 50);
        assertEquals(originalXml, assembly.getXmlString());
        assertEquals(200, assembly.getNumHits());

        //  Get all Records (there should only be 1)
        ArrayList records = dao.getAllRecords();
        assertEquals(1, records.size());
        XmlCacheRecord record = (XmlCacheRecord) records.get(0);
        assertEquals(URL, record.getUrl());
        assertEquals(HASH_KEY, record.getMd5());
        assertEquals(200, record.getNumHits());

        //  Delete record, and verify success.
        success = dao.deleteRecordByKey(HASH_KEY);
        assertTrue(success);

        //  Verify that record is no longer here, and that no exceptions
        //  occur.
        assembly = dao.getXmlAssemblyByKey(HASH_KEY);
        assertEquals(null, assembly);
    }

    /**
     * Tests Auto-Purge Feature.
     *
     * @throws Exception All Exceptions.
     */
    public void testAutoPurge() throws Exception {
        XDebug xdebug = new XDebug();
        DaoXmlCache dao = new DaoXmlCache(xdebug);

        //  Override default values
        dao.setMaxCacheRecords(100);
        dao.setPurgeIncrement(50);

        //  Purge Cache;  starts with clean slate
        dao.deleteAllRecords();

        //  Get a Sample Assembly
        XmlAssembly assembly = XmlAssemblyFactory.createXmlAssembly
                (4, 1, xdebug);
        assembly.setNumHits(100);

        //  Add 500 Records
        for (int i = 0; i < 200; i++) {
            String hash = "hash" + i;
            dao.addRecord(hash, hash, assembly);
        }

        //  Verify we now have 50 records in cache
        ArrayList records = dao.getAllRecords();
        assertEquals(50, records.size());
    }
}