// $Id: BinaryInteractionAssemblyFactory.java,v 1.5 2010-02-02 17:21:02 grossben Exp $
//------------------------------------------------------------------------------
/** Copyright (c) 2008 Memorial Sloan-Kettering Cancer Center.
 **
 ** Code written by: Ethan Cerami, Benjamin Gross
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
package org.mskcc.pathdb.schemas.binary_interaction.assembly;

// imports
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.BioPAXLevel;
import org.biopax.paxtools.io.simpleIO.SimpleReader;

import org.mskcc.pathdb.schemas.binary_interaction.util.BinaryInteractionUtil;

import java.util.List;
import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;

/**
 * Factory for instantiating BinaryInteractionAssembly objects.
 *
 * @author Benjamin Gross.
 */
public class BinaryInteractionAssemblyFactory {

	/**
	 * z-order enumeration
	 */
	public static enum AssemblyType {
		SIF;
	}

    /**
     * Creates a BinaryAssembly based on specified AssemblyType
     *
	 * @param assemblyType AssemblyType
	 * @param binaryInteractionUtil BinaryInteractionUtil
	 * @param ruleTypes List<String>
	 * @param owlXML String
	 * @return BinaryInteractionAssembly
     */
    public static BinaryInteractionAssembly createAssembly(AssemblyType assemblyType,
														   BinaryInteractionUtil binaryInteractionUtil,
														   List<String> ruleTypes,
														   String owlXML) {

		return BinaryInteractionAssemblyFactory.createAssembly(assemblyType, binaryInteractionUtil, ruleTypes, false, owlXML);
	}

    /**
     * Creates a BinaryAssembly based on specified AssemblyType
     *
	 * @param assemblyType AssemblyType
	 * @param binaryInteractionUtil BinaryInteractionUtil
	 * @param ruleTypes List<String>
	 * @param reduceComplexes boolean
	 * @param owlXML String
	 * @return BinaryInteractionAssembly
     */
    public static BinaryInteractionAssembly createAssembly(AssemblyType assemblyType,
														   BinaryInteractionUtil binaryInteractionUtil,
														   List<String> ruleTypes,
														   boolean reduceComplexes,
														   String owlXML) {
		// construct paxtools model with this owlXML
		Model level2 = null;
		SimpleReader handler = new SimpleReader();
		try {
			level2 = handler.convertFromOWL(new ByteArrayInputStream(owlXML.getBytes("UTF-8")));
		}
		catch (UnsupportedEncodingException e) {
			// should never get here
			level2 = handler.convertFromOWL(new ByteArrayInputStream(owlXML.getBytes()));
		}

		// return the proper assembly type
		if (assemblyType == AssemblyType.SIF) {
			return new SIFAssembly(level2, binaryInteractionUtil, ruleTypes, reduceComplexes);
		}

		// shouldnt get here
		return null;
	}
}