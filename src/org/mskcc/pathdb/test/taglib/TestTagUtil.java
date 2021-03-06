// $Id: TestTagUtil.java,v 1.10 2006-02-22 22:47:51 grossb Exp $
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
package org.mskcc.pathdb.test.taglib;

import junit.framework.TestCase;
import org.mskcc.dataservices.schemas.psi.NamesType;
import org.mskcc.pathdb.taglib.TagUtil;

/**
 * Tests the TagUtil Class.
 *
 * @author Ethan Cerami
 */
public class TestTagUtil extends TestCase {
    private String testName;

    /**
     * Tests the getLabel method.
     */
    public void testGetLabel() {
        testName = "Test the Get Label Functionality";
        //  Test 1
        NamesType name = new NamesType();
        name.setShortLabel("TNFB");
        name.setFullName("TNF-Beta");
        String label = TagUtil.getLabel(name);
        assertEquals("TNFB: TNF-Beta", label);

        //  Test 2
        name = new NamesType();
        name.setShortLabel("TNFB");
        label = TagUtil.getLabel(name);
        assertEquals("TNFB", label);

        //  Test 3
        name = new NamesType();
        name.setFullName("TNF-Beta");
        name.setShortLabel("");
        label = TagUtil.getLabel(name);
        assertEquals("TNF-Beta", label);

        //  Test 4
        name = new NamesType();
        label = TagUtil.getLabel(name);
        assertEquals(TagUtil.NAME_NOT_AVAILABLE, label);

        //  Test 5
        label = TagUtil.getLabel(null);
        assertEquals(TagUtil.NAME_NOT_AVAILABLE, label);

        // Test 6
        name = new NamesType();
        name.setFullName("TNF-Beta\n   Alpha");
        label = TagUtil.getLabel(name);
        assertEquals("TNF-Beta Alpha", label);
    }

    /**
     * Tests the truncateLabel method.
     */
    public void testTruncateLabel() {
        testName = "Test the Truncate Label Functionality";
        //  Test 1
        String label = TagUtil.truncateLabel
                ("This is a test of a long protein name with a very long name");
        assertEquals("This is a test of a long protein name wi...", label);

        //  Test 2
        label = TagUtil.truncateLabel("A Short Protein");
        assertEquals("A Short Protein", label);
    }

    /**
     * Tests the CreateLink method.
     */
    public void testCreateLink() {
        testName = "Test the Creation of HTML Links";
        String link = TagUtil.createLink("Tip", "http://www.yahoo.com",
                "Click for yahoo");
        assertEquals("<A TITLE='Tip' HREF='http://www.yahoo.com'>"
                + "Click for yahoo</A>", link);
    }

    /**
     * Gets Name of Test.
     *
     * @return Name of Test.
     */
    public String getName() {
        return "Test the Html Tag Utility, used by custom JSP Tags:  " + testName;
    }
}
