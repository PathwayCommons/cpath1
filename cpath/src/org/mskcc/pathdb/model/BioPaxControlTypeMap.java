// $Id: BioPaxControlTypeMap.java,v 1.6 2006-02-23 17:34:51 grossb Exp $
//------------------------------------------------------------------------------
/** Copyright (c) 2006 Memorial Sloan-Kettering Cancer Center.
 **
 ** Code written by: Ethan Cerami, Benjamin Gross
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
package org.mskcc.pathdb.model;

import java.util.HashMap;

/**
 * Hashmap which maps BioPax Control Type to Plain English.
 *
 * @author Benjamin Gross
 */
public class BioPaxControlTypeMap extends HashMap {

    /**
     * Constructor.
     */
    public BioPaxControlTypeMap() {
		put("INHIBITION", "inhibits");
		put("ACTIVATION", "activates");
		put("INHIBITION-ALLOSTERIC", "allosterically inhibits");
		put("INHIBITION-COMPETITIVE", "competitively inhibits");
		put("INHIBITION-IRREVERSIBLE", "irreversibly inhibits");
		put("INHIBITION-NONCOMPETITIVE", "noncompetitively inhibits");
		put("INHIBITION-OTHER", "inhibits (other)");
		put("INHIBITION-UNCOMPETITIVE", "uncompetitively inhibits");
		put("INHIBITION-UNKMECH", "inhibits");
		put("ACTIVATION-NONALLOSTERIC", "nonallosterically activates");
		put("ACTIVATION-ALLOSTERIC", "allosterically activates");
		put("ACTIVATION-UNKMECH", "activates");
    }
}
