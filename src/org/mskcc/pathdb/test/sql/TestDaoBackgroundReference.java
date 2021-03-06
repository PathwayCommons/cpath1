// $Id: TestDaoBackgroundReference.java,v 1.6 2006-02-22 22:47:51 grossb Exp $
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
package org.mskcc.pathdb.test.sql;

import junit.framework.TestCase;
import org.mskcc.pathdb.model.BackgroundReferencePair;
import org.mskcc.pathdb.model.ReferenceType;
import org.mskcc.pathdb.sql.dao.DaoBackgroundReferences;

/**
 * Tests the DaoIdentity class.
 *
 * @author Ethan Cerami
 */
public class TestDaoBackgroundReference extends TestCase {

    /**
     * Tests Data Access:  Create, Get, Delete.
     *
     * @throws Exception All Exceptions.
     */
    public void testAccess() throws Exception {
        DaoBackgroundReferences dao = new DaoBackgroundReferences();

        //  First, try adding a sample record with invalid DB Ids.
        //  This should trigger an exception.
        BackgroundReferencePair pair = new BackgroundReferencePair
                (100, "ABCD", 200, "XYZ", ReferenceType.PROTEIN_UNIFICATION);
        try {
            dao.addRecord(pair, true);
            fail("Illegal Argument Exception should have been thrown.  "
                    + " DB1 and DB2 are not stored in the database.");
        } catch (IllegalArgumentException e) {
            String msg = e.getMessage();
        }

        //  Now, try adding a sample record with an empty Id.
        //  This should trigger an exception.
        pair = new BackgroundReferencePair(1, "ABCD", 2, "",
                ReferenceType.PROTEIN_UNIFICATION);
        try {
            dao.addRecord(pair, true);
            fail("Illegal Argument Exception should have been thrown.  "
                    + " ID2 is null");
        } catch (IllegalArgumentException e) {
            String msg = e.getMessage();
        }

        //  Now, try adding a sample PROTEIN_UNIFICATION record with a
        //  LINK_OUT Reference.  This should trigger an Exception.
        pair = new BackgroundReferencePair(1, "ABCD", 3, "XYZ",
                ReferenceType.PROTEIN_UNIFICATION);
        try {
            dao.addRecord(pair, true);
            fail("Illegal Argument Exception should have been thrown.  "
                    + " Second Database not a PROTEIN_UNIFICATION Database.");
        } catch (IllegalArgumentException e) {
            String msg = e.getMessage();
        }

        //  Now, try adding a sample LINK_OUT record with two
        //  PROTEIN_UNIFICATION References.  This should trigger an Exception.
        pair = new BackgroundReferencePair(1, "ABCD", 6, "XYZ",
                ReferenceType.LINK_OUT);
        try {
            dao.addRecord(pair, true);
            fail("Illegal Argument Exception should have been thrown.  "
                    + " Both databases are of type: PROTEIN_UNIFICATION.");
        } catch (IllegalArgumentException e) {
            String msg = e.getMessage();
        }

        //  Now, try adding a valid sample PROTEIN_UNIFICATION record
        pair = new BackgroundReferencePair(1, "ABCD", 6, "XYZ",
                ReferenceType.PROTEIN_UNIFICATION);
        boolean success = dao.addRecord(pair, true);
        assertTrue(success);

        //  Verify that the record 1:ABCD <--> 6:XYZ now exists within
        //  the database
        BackgroundReferencePair record2 = dao.getRecord(pair);
        assertTrue(record2 != null);
        assertEquals(1, record2.getDbId1());
        assertEquals(6, record2.getDbId2());
        assertEquals("ABCD", record2.getLinkedToId1());
        assertEquals("XYZ", record2.getLinkedToId2());
        assertEquals(ReferenceType.PROTEIN_UNIFICATION,
                record2.getReferenceType());

        //  Verify that the record 6:XYZ <--> 1:ABCD generates the same hit.
        record2 = dao.getRecord(new BackgroundReferencePair(6, "XYZ",
                1, "ABCD", ReferenceType.PROTEIN_UNIFICATION));
        assertTrue(record2 != null);
        assertEquals(1, record2.getDbId1());
        assertEquals(6, record2.getDbId2());
        assertEquals("ABCD", record2.getLinkedToId1());
        assertEquals("XYZ", record2.getLinkedToId2());
        assertEquals(ReferenceType.PROTEIN_UNIFICATION,
                record2.getReferenceType());

        //  Now Delete it
        success = dao.deleteRecordById(record2.getPrimaryId());
        assertTrue(success);

        //  Verify that record is indeed deleted.
        record2 = dao.getRecord(pair);
        assertTrue(record2 == null);
    }

    /**
     * Gets Name of Test.
     *
     * @return Name of Test.
     */
    public String getName() {
        return "Test the MySQL BackgroundReference Data Access Object (DAO)";
    }
}
