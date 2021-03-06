// $Id: ExecuteBinaryInteraction.java,v 1.16 2010-11-08 21:41:25 grossben Exp $
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
package org.mskcc.pathdb.action.web_api.binary_interaction_mode;

import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.lucene.queryParser.ParseException;
import org.apache.log4j.Logger;
import org.mskcc.pathdb.xdebug.XDebug;
import org.mskcc.pathdb.xdebug.XDebugUtil;
import org.mskcc.pathdb.protocol.ProtocolRequest;
import org.mskcc.pathdb.protocol.ProtocolException;
import org.mskcc.pathdb.protocol.ProtocolStatusCode;
import org.mskcc.pathdb.protocol.ProtocolConstantsVersion2;
import org.mskcc.pathdb.protocol.ProtocolConstantsVersion3;
import org.mskcc.pathdb.sql.query.QueryException;
import org.mskcc.pathdb.sql.query.GetNeighborsCommand;
import org.mskcc.pathdb.sql.assembly.AssemblyException;
import org.mskcc.pathdb.sql.assembly.XmlAssembly;
import org.mskcc.pathdb.sql.dao.DaoException;
import org.mskcc.pathdb.sql.dao.DaoExternalLink;
import org.mskcc.pathdb.action.web_api.WebApiUtil;
import org.mskcc.pathdb.util.ExternalDatabaseConstants;
import org.mskcc.pathdb.model.ExternalLinkRecord;
import org.mskcc.pathdb.schemas.binary_interaction.util.BinaryInteractionUtil;
import org.mskcc.pathdb.schemas.binary_interaction.assembly.BinaryInteractionAssembly;
import org.mskcc.pathdb.schemas.binary_interaction.assembly.BinaryInteractionAssemblyFactory;
import org.biopax.paxtools.io.sif.BinaryInteractionType;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.io.IOException;

/**
 * Binary Interaction Web Mode:  Response is Binary Interaction.
 *
 * @author Ethan Cerami, Benjamin Gross.
 */
public class ExecuteBinaryInteraction {
    private static Logger log = Logger.getLogger(ExecuteBinaryInteraction.class);

    /**
     * Processes Client Request.
     *
     * @param xdebug          XDebug Object.
     * @param protocolRequest Protocol Request Object.
     * @param request         Http Servlet Request Object.
     * @param response        Http Servlet Response Object.
     * @param mapping         Struts Action Mapping Object.
     * @return Struts Action Forward Object.
     * @throws org.mskcc.pathdb.sql.query.QueryException
     *                                    Query Error.
     * @throws java.io.IOException        I/O Error.
     * @throws org.mskcc.pathdb.sql.assembly.AssemblyException
     *                                    XML Assembly Error.
     * @throws org.apache.lucene.queryParser.ParseException
     *                                    Lucene Parsing Error.
     * @throws org.mskcc.pathdb.protocol.ProtocolException
     *                                    Protocol Error.
     * @throws org.mskcc.pathdb.sql.dao.DaoException
     *                                    Database Error.
     * @throws CloneNotSupportedException Cloning Error.
     */
    public ActionForward processRequest(XDebug xdebug, ProtocolRequest protocolRequest,
            HttpServletRequest request, HttpServletResponse response, ActionMapping mapping)
            throws QueryException, IOException, AssemblyException, ParseException, ProtocolException,
				   DaoException, CloneNotSupportedException, NumberFormatException {

        boolean debugMode = XDebugUtil.xdebugIsEnabled(request);
        XmlAssembly xmlAssembly = WebApiUtil.fetchXmlAssembly(xdebug, protocolRequest, debugMode);
        if (xmlAssembly == null || xmlAssembly.isEmpty()) {
            String q = protocolRequest.getQuery();
            throw new ProtocolException(ProtocolStatusCode.NO_RESULTS_FOUND,
                    "No Results Found for:  " + q);
        }

		// determine binary interaction assembly type
		BinaryInteractionAssemblyFactory.AssemblyType binaryInteractionAssemblyType = null;
		if (protocolRequest.getOutput().equals(ProtocolConstantsVersion2.FORMAT_BINARY_SIF)) {
			binaryInteractionAssemblyType = BinaryInteractionAssemblyFactory.AssemblyType.SIF;
		}

		// contruct rule types
		List<String> binaryInteractionRuleTypes = getRuleTypes(protocolRequest);

		// get binary interaction assembly
		BinaryInteractionAssembly assembly =
			BinaryInteractionAssemblyFactory.createAssembly(binaryInteractionAssemblyType,
															new BinaryInteractionUtil(),
															binaryInteractionRuleTypes,
															xmlAssembly.getXmlString());

		// filter out unwanted interactions
		log.info("************************ ExecuteBinaryInteraction.processRequest(): sif assembly string before conversion:\n");
		log.info(assembly.getBinaryInteractionString());
		Double version = new Double(protocolRequest.getVersion());
		Double version3 = new Double(ProtocolConstantsVersion3.VERSION_3);
		String outputIDType = protocolRequest.getOutputIDType();
		boolean cookOutputIDs = (outputIDType != null && !outputIDType.equals(ExternalDatabaseConstants.INTERNAL_DATABASE));
		HashMap<String, String> outputIDMap = new HashMap<String, String>();
		StringBuffer sifBuffer = new StringBuffer("");
		String[] binaryInteractions = (assembly.getBinaryInteractionString() != null) ?
			assembly.getBinaryInteractionString().split("\n") : null;
		if (binaryInteractions != null && binaryInteractions.length > 0) {
			//Map<String, String> tagMap = BinaryInteractionType.getTagMap();
			for (String binaryInteraction : binaryInteractions) {
				// sif format:  ID\tINTERACTION_TYPE\tID
				String[] components = binaryInteraction.split("\t");
				if (components.length == 3) {
					if (binaryInteractionRuleTypes.contains(components[1])) {
						// cook output ids if necessary
						if (cookOutputIDs) {
							components[0] = getOutputID(outputIDType, components[0], outputIDMap);
							components[2] = getOutputID(outputIDType, components[2], outputIDMap);
						}
						sifBuffer.append(components[0] + "\t" + components[1] + "\t" + components[2] + "\n");
					}
				}
			}
		}
		WebApiUtil.returnText(response, sifBuffer.toString());
		log.info("************************ ExecuteBinaryInteraction.processRequest(): sif assembly string after conversion:\n");
		log.info(sifBuffer.toString());

        //  Return null here, because we do not want Struts to do any forwarding.
        return null;
    }

	/**
	 * Gets list of binary interaction rule types for all rule classes
	 * display (ie web api docs, protocol request argument validation).
	 *
	 * Rules are in Rule.RuleType format, this code changes
	 * with "_".
	 *
	 * @return List<String>
	 */
	public static List<String> getRuleTypesForDisplay() {

		// get rule types
		List<String> ruleTypes = (new BinaryInteractionUtil()).getRuleTypes();

		// sort
		Collections.sort(ruleTypes);

		// outta here
		return ruleTypes;
	}

	/**
	 * Given a rule type, returns the rule type description.
	 *
	 * @param ruleType String
	 * @return String
	 */
	public static String getRuleTypeDescription(String ruleType) {
		return (new BinaryInteractionUtil()).getRuleTypeDescription(ruleType);
	}

	/*
	 * This code takes a rule in display format (getRuleTypesForDisplay()),
	 * and returns a rule type valid for paxtools consumption
	 *
	 * @param protocolRequest ProtocolRequest
	 * @return List<String>
	 */
	private List<String> getRuleTypes(ProtocolRequest protocolRequest) {

		// list to return
		List<String> toReturn = null;

        String[] binaryInteractionRules = protocolRequest.getBinaryInteractionRules();
		if (binaryInteractionRules != null) {
			toReturn = new ArrayList<String>();
			for (String ruleType : binaryInteractionRules) {
				toReturn.add(ruleType);
			}
		}
		else {
			toReturn = (new BinaryInteractionUtil()).getRuleTypes();
		}

		// outta here
		return toReturn;
	}

	/*
	 * Given an internal (cPath) id, returns an external id given the output id type.
	 * Takes a map to add external id to speedup processing.
	 *
	 * @param outputIDType String
	 * @param internalID long
	 * @param Map<String, String>
	 * @return String
	 * @throws DaoException
	 * @throws NumberFormatException
	 */
	private String getOutputID(String outputIDType, String internalID, Map<String, String> outputIDMap) throws DaoException, NumberFormatException {

		// convert long to string
		String externalID = internalID;

		// if external id already exists, let return it
		if (outputIDMap.containsKey(internalID)) {
			return outputIDMap.get(internalID);
		}

		// need to determine external id
        DaoExternalLink daoExternalLinker = DaoExternalLink.getInstance();
		ArrayList<ExternalLinkRecord> externalLinkRecords =
			daoExternalLinker.getRecordsByCPathId(Long.valueOf(internalID));
		for (ExternalLinkRecord externalLinkRecord : externalLinkRecords) {
			String masterTerm = externalLinkRecord.getExternalDatabase().getMasterTerm();
			if (masterTerm.equals(outputIDType)) {
				externalID = externalLinkRecord.getLinkedToId();
				outputIDMap.put(internalID, externalID);
				return externalID;
			}
		}

		// made it here, no external id found
		externalID = GetNeighborsCommand.NOT_SPECIFIED;
		outputIDMap.put(internalID, externalID);
		return externalID;
	}
}
