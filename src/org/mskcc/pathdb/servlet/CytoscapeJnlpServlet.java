// $Id: CytoscapeJnlpServlet.java,v 1.17 2008-12-01 21:11:43 grossben Exp $
//------------------------------------------------------------------------------
/** Copyright (c) 2007 Memorial Sloan-Kettering Cancer Center.
 **
 ** Code written by: Ethan Cerami, Benjamin Gross
 ** Authors: Ethan Cerami, Gary Bader, Chris Sander, Benjamin Gross
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
package org.mskcc.pathdb.servlet;

import org.mskcc.pathdb.protocol.ProtocolRequest;
import org.mskcc.pathdb.protocol.ProtocolConstantsVersion1;
import org.mskcc.pathdb.protocol.ProtocolConstantsVersion3;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import java.net.URLEncoder;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;

/**
 * JNLP Servlet.
 *
 * @author Benjamin Gross
 */
public final class CytoscapeJnlpServlet extends HttpServlet {

    /**
     * Initializes Servlet with parameters in web.xml file.
     *
     * @throws ServletException Servlet Initialization Error.
     */
    public void init() throws ServletException {
        super.init();
    }

    /**
     * Shutdown the Servlet.
     */
    public void destroy() {
        super.destroy();
    }

    /**
     * Handles Client Request.
     *
     * @param req Http Servlet Request.
     * @param res Http Servlet Response.
     * @throws ServletException All Servlet Errors.
     * @throws IOException      All Input/Output Errors.
     */
    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

		// get record id
        String recordID = request.getParameter("id");
		if (recordID == null) return;

		// get command
		String command = request.getParameter("command");
		if (command == null) return;

		// get neighborhood title
		String networkViewTitle = request.getParameter("network_view_title");
		networkViewTitle = (networkViewTitle == null) ? "" : networkViewTitle;

		// data sources
		String dataSources = request.getParameter("data_source");
		dataSources = (dataSources == null) ? "" : dataSources;

		// encode some parameters
		try {
			networkViewTitle = URLEncoder.encode(networkViewTitle, "UTF-8");
			dataSources = URLEncoder.encode(dataSources, "UTF-8");
		}
		catch (UnsupportedEncodingException e) {
			// can't see this happening, but lets at least encoded the spaces
			networkViewTitle = networkViewTitle.replaceAll(" ", "%20");
			dataSources = dataSources.replaceAll(" ", "%20");
		}

		// set content type on response object
		response.setContentType("application/x-java-jnlp-file");

		// construct url to pathway commons, ie http://www.pathwaycommons.org/
		String urlToPathwayCommons = ((StringBuffer)request.getRequestURL()).toString();
		urlToPathwayCommons = urlToPathwayCommons.substring(0, urlToPathwayCommons.lastIndexOf("/"));

		// contruct url to retrieve record, ie include web services api call
		String urlToRetrieveRecord = (urlToPathwayCommons +
									  "/webservice.do?" +
									  ProtocolRequest.ARG_VERSION + "=" + ProtocolConstantsVersion3.VERSION_3 +
									  "&" + ProtocolRequest.ARG_COMMAND + "=" + command +
									  "&" + ProtocolRequest.ARG_OUTPUT + "=" + ProtocolConstantsVersion1.FORMAT_BIO_PAX +
									  "&" + ProtocolRequest.ARG_QUERY + "=" + recordID +
									  "&" + ProtocolRequest.ARG_DATA_SOURCE + "=" + dataSources);

		// write out the data
		writeJNLPData(urlToPathwayCommons, urlToRetrieveRecord, networkViewTitle, dataSources,
					  new PrintStream(response.getOutputStream()));
    }

	/**
	 * Writes jnlp file to out stream.
	 *
	 * @param urltoPathwayCommons String
	 * @param urlToRetrieveRecord String
	 * @param networkViewTitle String
	 * @param dataSources String
     * @param out PrintStream
	 * @throws IOException
	 */
	private void writeJNLPData(String urlToPathwayCommons,
							   String urlToRetrieveRecord,
							   String networkViewTitle,
							   String dataSources,
							   PrintStream out) throws IOException {

        out.print("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                   "<jnlp codebase=\"" + urlToPathwayCommons + "/jsp/cytoscape\">\n" +
                     "<security>\n" +
                       "<all-permissions />\n" +
                     "</security>\n" +
                     "<information>\n" +
                       "<title>Cytoscape Webstart</title>\n" +
                       "<vendor>Cytoscape Collaboration</vendor>\n" +
                       "<homepage href=\"http://cytoscape.org\" />\n" +
                       "<offline-allowed />\n" +
                     "</information>\n" +
                     "<resources>\n" +
                       "<j2se version=\"1.5+\" max-heap-size=\"1024M\" />\n" +
				       "<property name=\"biopax.network_view_title\" value=\"" + networkViewTitle + "\"/>\n" +
				       "<property name=\"biopax.web_services_url\" value=\"" + urlToPathwayCommons + "\"/>\n" +
				       "<property name=\"biopax.data_sources\" value=\"" + dataSources + "\"/>\n" +
                    "    <jar href=\"cytoscape.jar\" />\n" +
                "    <jar href=\"lib/activation.jar\" />\n" +
                "    <jar href=\"lib/biojava-1.4.jar\" />\n" +
                "    <jar href=\"lib/colt.jar\" />\n" +
                "    <jar href=\"lib/coltginy.jar\" />\n" +
                "    <jar href=\"lib/com-nerius-math-xform.jar\" />\n" +
                "    <jar href=\"lib/commons-cli-1.x-cytoscape-custom.jar\" />\n" +
                "    <jar href=\"lib/concurrent.jar\" />\n" +
                "    <jar href=\"lib/cytoscape-cruft-obo.jar\" />\n" +
                "    <jar href=\"lib/cytoscape-geom-rtree.jar\" />\n" +
                "    <jar href=\"lib/cytoscape-geom-spacial.jar\" />\n" +
                "    <jar href=\"lib/cytoscape-graph-dynamic.jar\" />\n" +
                "    <jar href=\"lib/cytoscape-graph-fixed.jar\" />\n" +
                "    <jar href=\"lib/cytoscape-render-export.jar\" />\n" +
                "    <jar href=\"lib/cytoscape-render-immed.jar\" />\n" +
                "    <jar href=\"lib/cytoscape-render-stateful.jar\" />\n" +
                "    <jar href=\"lib/cytoscape-task.jar\" />\n" +
                "    <jar href=\"lib/cytoscape-util-intr.jar\" />\n" +
                "    <jar href=\"lib/ding.jar\" />\n" +
                "    <jar href=\"lib/FastInfoset.jar\" />\n" +
                "    <jar href=\"lib/fing.jar\" />\n" +
                "    <jar href=\"lib/freehep-export-2.1.1.jar\" />\n" +
                "    <jar href=\"lib/freehep-graphics2d-2.1.1.jar\" />\n" +
                "    <jar href=\"lib/freehep-graphicsio-2.1.1.jar\" />\n" +
                "    <jar href=\"lib/freehep-graphicsio-java-2.1.1.jar\" />\n" +
                "    <jar href=\"lib/freehep-graphicsio-ps-2.1.1.jar\" />\n" +
                "    <jar href=\"lib/freehep-graphicsio-svg-2.1.1.jar\" />\n" +
                "    <jar href=\"lib/freehep-io-2.0.2.jar\" />\n" +
                "    <jar href=\"lib/freehep-jas-plotter-2.2.jar\" />\n" +
                "    <jar href=\"lib/freehep-swing-2.0.3.jar\" />\n" +
                "    <jar href=\"lib/freehep-util-2.0.2.jar\" />\n" +
                "    <jar href=\"lib/freehep-xml-2.1.1.jar\" />\n" +
                "    <jar href=\"lib/giny.jar\" />\n" +
                "    <jar href=\"lib/glf.jar\" />\n" +
                "    <jar href=\"lib/http.jar\" />\n" +
                "    <jar href=\"lib/i4jruntime.jar\" />\n" +
                "    <jar href=\"lib/itext-2.0.4.jar\" />\n" +
                "    <jar href=\"lib/jaxb-api.jar\" />\n" +
                "    <jar href=\"lib/jaxb-impl.jar\" />\n" +
                "    <jar href=\"lib/jaxws-api.jar\" />\n" +
                "    <jar href=\"lib/jaxws-rt.jar\" />\n" +
                "    <jar href=\"lib/jaxws-tools.jar\" />\n" +
                "    <jar href=\"lib/jdom-1.0.jar\" />\n" +
                "    <jar href=\"lib/jhall.jar\" />\n" +
                "    <jar href=\"lib/jnlp.jar\" />\n" +
                "    <jar href=\"lib/jsr173_1.0_api.jar\" />\n" +
                "    <jar href=\"lib/jsr181-api.jar\" />\n" +
                "    <jar href=\"lib/jsr250-api.jar\" />\n" +
                "    <jar href=\"lib/junit.jar\" />\n" +
                "    <jar href=\"lib/l2fprod-common-all.jar\" />\n" +
                "    <jar href=\"lib/looks-2.1.4.jar\" />\n" +
                "    <jar href=\"lib/phoebe.jar\" />\n" +
                "    <jar href=\"lib/piccolo.jar\" />\n" +
                "    <jar href=\"lib/resolver.jar\" />\n" +
                "    <jar href=\"lib/saaj-api.jar\" />\n" +
                "    <jar href=\"lib/saaj-impl.jar\" />\n" +
                "    <jar href=\"lib/sjsxp.jar\" />\n" +
                "    <jar href=\"lib/stax-ex.jar\" />\n" +
                "    <jar href=\"lib/streambuffer.jar\" />\n" +
                "    <jar href=\"lib/swing-layout-1.0.1.jar\" />\n" +
                "    <jar href=\"lib/swingx-2006_10_27.jar\" />\n" +
                "    <jar href=\"lib/tclib.jar\" />\n" +
                "    <jar href=\"lib/undo.support.jar\" />\n" +
                "    <jar href=\"lib/violinstrings-1.0.2.jar\" />\n" +
                "    <jar href=\"lib/wizard.jar\" />\n" +
                "    <!--These are the plugins you wish to load, edit as necessary.-->\n" +
                "    <jar href=\"plugins/AutomaticLayout.jar\" />\n" +
                "    <jar href=\"plugins/biopax.jar\" />\n" +
                "    <jar href=\"plugins/browser.jar\" />\n" +
                "    <jar href=\"plugins/cPath.jar\" />\n" +
                "    <jar href=\"plugins/cpath2.jar\" />\n" +
                "    <jar href=\"plugins/CytoscapeEditor.jar\" />\n" +
                "    <jar href=\"plugins/filter.jar\" />\n" +
                "    <jar href=\"plugins/filters.jar\" />\n" +
                "    <jar href=\"plugins/GraphMerge.jar\" />\n" +
                "    <jar href=\"plugins/linkout.jar\" />\n" +
                "    <jar href=\"plugins/ManualLayout.jar\" />\n" +
                "    <jar href=\"plugins/psi_mi.jar\" />\n" +
                "    <jar href=\"plugins/quick_find.jar\" />\n" +
                "    <jar href=\"plugins/SBMLReader.jar\" />\n" +
                "    <jar href=\"plugins/TableImport.jar\" />\n" +
                "    <jar href=\"plugins/yLayouts.jar\" />" +
                "    </resources>\n" +
                     "<!--This starts-up Cytoscape, specify your plugins to load, and other command line arguments.  Plugins not specified here will not be loaded.-->\n" +
                     "<application-desc main-class=\"cytoscape.CyMain\">\n" +
                       "<argument>-p</argument>\n" +
                       "<argument>csplugins.layout.LayoutPlugin</argument>\n" +
                       "<argument>-p</argument>\n" +
                       "<argument>org.mskcc.biopax_plugin.plugin.BioPaxPlugIn</argument>\n" +
                       "<argument>-p</argument>\n" +
                       "<argument>browser.AttributeBrowserPlugin</argument>\n" +
                       "<argument>-p</argument>\n" +
                       "<argument>org.cytoscape.coreplugin.cpath2.plugin.CPathPlugIn2</argument>\n" +
                       "<argument>-p</argument>\n" +
                       "<argument>cytoscape.editor.CytoscapeEditorPlugin</argument>\n" +
                       "<argument>-p</argument>\n" +
                       "<argument>filter.cytoscape.CsFilter</argument>\n" +
                       "<argument>-p</argument>\n" +
                       "<argument>GraphMerge.GraphMerge</argument>\n" +
                       "<argument>-p</argument>\n" +
                       "<argument>linkout.LinkOutPlugin</argument>\n" +
                       "<argument>-p</argument>\n" +
                       "<argument>ManualLayout.ManualLayoutPlugin</argument>\n" +
                       "<argument>-p</argument>\n" +
                       "<argument>org.cytoscape.coreplugin.psi_mi.plugin.PsiMiPlugIn</argument>\n" +
                       "<argument>-p</argument>\n" +
                       "<argument>csplugins.quickfind.plugin.QuickFindPlugIn</argument>\n" +
                       "<argument>-p</argument>\n" +
                       "<argument>sbmlreader.SBMLReaderPlugin</argument>\n" +
                       "<argument>-p</argument>\n" +
                       "<argument>edu.ucsd.bioeng.coreplugin.tableImport.TableImportPlugin</argument>\n" +
                       "<argument>-p</argument>\n" +
                       "<argument>yfiles.YFilesLayoutPlugin</argument>\n" +
                       "<argument>-N</argument>\n" + 
                       "<argument>" + urlToRetrieveRecord + "</argument>\n" +
                     "</application-desc>\n" +
				  "</jnlp>\n");
	}
}

