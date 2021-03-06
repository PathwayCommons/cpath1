// $Id: TestHtmlUtil.java,v 1.8 2006-12-22 18:46:29 cerami Exp $
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
package org.mskcc.pathdb.test.util;

import junit.framework.TestCase;
import org.mskcc.pathdb.util.html.HtmlUtil;

/**
 * Tests the HtmlUtil Class.
 *
 * @author Ethan Cerami
 */
public class TestHtmlUtil extends TestCase {
    private String testName;

    /**
     * Tests the Truncate Words Method.
     */
    public void testTruncationMethod0() {
        testName = "Simulation truncation of sequence string.";
        String truncated = HtmlUtil.truncateLongWords
                ("here is a sequence: AAAAAAAAAAAAAAAAAA more stuff", 10);
        assertEquals("here is a sequence: AAAAAAAAAA [Cont.] more stuff",
                truncated);
    }

    /**
     * Tests the Truncate Words Method.
     */
    public void testTruncationMetho10() {
        testName = "Simulation truncation of HTML string.";
        String truncated = HtmlUtil.truncateLongWords
                ("<b>here is a sequence: AAAAAAAAAAAAAAAAAA</b> more stuff", 10);
        assertEquals("<b>here is a sequence: AAAAAAAAAA [Cont.]</b> more stuff",
                truncated);
    }


    /**
     * Gets Name of Test.
     *
     * @return Name of Test.
     */
    public String getName() {
        return "Test the HTML Utility Class:  " + testName;
    }
}
