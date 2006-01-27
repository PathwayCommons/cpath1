/** Copyright (c) 2005 Memorial Sloan-Kettering Cancer Center.
 **
 ** Code written by: Benjamin Gross
 ** Authors: Ethan Cerami, Benjamin Gross, Gary Bader, Chris Sander
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
package org.mskcc.pathdb.test.schemas.biopax.summary;

// imports
import java.util.ArrayList;

import junit.framework.TestCase;

import org.mskcc.pathdb.schemas.biopax.summary.InteractionSummary;
import org.mskcc.pathdb.schemas.biopax.summary.InteractionSummaryComponent;
import org.mskcc.pathdb.schemas.biopax.summary.InteractionParser;

/**
 * Tests the InteractionParser Class.
 *
 * @author Benjamin Gross.
 */
public class TestInteractionParser extends TestCase {

    /**
     * Tests conversion interaction parsing.
     *
     * @throws Exception All Exceptions.
     */
    public void testConversionInteractionParser() throws Exception {

		// shared vars
		int cnt = 0;
		ArrayList components = null;
		InteractionSummaryComponent summaryComponent = null;

		// perform the interaction parsing
		InteractionParser interactionParser = new InteractionParser(8);
		InteractionSummary interactionSummary = interactionParser.getInteractionSummary();

		// test left side
		components = interactionSummary.getLeftSideComponents();
		cnt = components.size();

		// left side count
		assertTrue(cnt == 2);

		// left side names
		summaryComponent = (InteractionSummaryComponent)components.get(0);
		assertEquals("a-D-glu", summaryComponent.getName());
		summaryComponent = (InteractionSummaryComponent)components.get(1);
		assertEquals("ATP", summaryComponent.getName());

		// test right side
		components = interactionSummary.getRightSideComponents();
		cnt = components.size();

		// right side count
		assertTrue(cnt == 2);

		// right side names
		summaryComponent = (InteractionSummaryComponent)components.get(0);
		assertEquals("ADP", summaryComponent.getName());
		summaryComponent = (InteractionSummaryComponent)components.get(1);
		assertEquals("a-D-glu-6-p", summaryComponent.getName());
	}


    /**
     * Gets Name of Test.
     *
     * @return Name of Test.
     */
    public String getName() {
        return "Test the InteractionParser Class";
    }
}
