// $Id: SearchCommand.java,v 1.13 2009-07-21 16:55:12 cerami Exp $
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
package org.mskcc.pathdb.sql.query;

import org.apache.lucene.search.Hits;
import org.apache.lucene.queryParser.ParseException;
import org.apache.log4j.Logger;
import org.mskcc.pathdb.lucene.LuceneReader;
import org.mskcc.pathdb.lucene.RequestAdapter;
import org.mskcc.pathdb.lucene.LuceneResults;
import org.mskcc.pathdb.model.XmlRecordType;
import org.mskcc.pathdb.protocol.ProtocolConstantsVersion1;
import org.mskcc.pathdb.protocol.ProtocolRequest;
import org.mskcc.pathdb.sql.assembly.AssemblyException;
import org.mskcc.pathdb.sql.assembly.XmlAssembly;
import org.mskcc.pathdb.sql.assembly.XmlAssemblyFactory;
import org.mskcc.pathdb.sql.dao.DaoException;
import org.mskcc.pathdb.taglib.Pager;

import java.io.IOException;

/**
 * Searches cPath by keyword or organism.
 *
 * @author Ethan Cerami
 */
class SearchCommand extends Query {
    private String searchTerms;
    private ProtocolRequest request;
    private boolean debugMode;
    private Logger log = Logger.getLogger(SearchCommand.class);


    /**
     * Constructor.
     * Only available via Factory Class.
     *
     * @param request ProtocolRequest Object.
     */
    SearchCommand(ProtocolRequest request, boolean debugMode) {
        this.request = request;
        this.debugMode = debugMode;
        this.searchTerms = RequestAdapter.getSearchTerms(request);
    }

    /**
     * Executes Query.
     */
    protected XmlAssembly executeSub() throws QueryException,
            IOException, AssemblyException, DaoException, ParseException {
        xdebug.logMsg(this, "Searching Lucene full text index. "
                + "Using search term(s):  " + searchTerms);
        log.info("Searching Lucene full text index. "
                + "Using search term(s):  " + searchTerms);
        LuceneReader indexer = new LuceneReader();
        try {
            XmlAssembly xmlAssembly;
            Hits hits = indexer.executeQuery(searchTerms);
            xdebug.logMsg(this, "Total Number of Matching Hits "
                    + "Found:  " + hits.length());
            Pager pager = new Pager(request, hits.length());
            LuceneResults luceneResults = new LuceneResults(pager, indexer.getQuery(), indexer.getIndexSearcher(),
                    hits, null, null, debugMode);
            long[] cpathIds = luceneResults.getCpathIds();
            xmlAssembly = createXmlAssembly(cpathIds, hits);
            xmlAssembly.setNumHits(hits.length());
            return xmlAssembly;
        } finally {
            indexer.close();
        }
    }

    /**
     * Creates XML Assembly.
     */
    private XmlAssembly createXmlAssembly(long[] cpathIds, Hits hits)
            throws AssemblyException {
        XmlAssembly xmlAssembly;
        //  Branch here, based on request.getFormat() and protocol
        if (cpathIds != null && cpathIds.length > 0) {
            if (request.getFormat().equals(ProtocolConstantsVersion1.FORMAT_BIO_PAX)) {
                xmlAssembly = XmlAssemblyFactory.createXmlAssembly(cpathIds,
                        XmlRecordType.BIO_PAX, hits.length(),
                        XmlAssemblyFactory.XML_FULL, true, xdebug);
            } else if (request.getFormat().equals(ProtocolConstantsVersion1.FORMAT_PSI_MI)
                    || request.getFormat().equals(ProtocolConstantsVersion1.FORMAT_XML)) {
                log.info("Use optimized code flag:  " + request.getUseOptimizedCode());
                if (request.getUseOptimizedCode()) {
                    xmlAssembly = XmlAssemblyFactory.createXmlAssembly(cpathIds,
                        XmlRecordType.PSI_MI, hits.length(),
                        XmlAssemblyFactory.XML_FULL_STRING_ONLY, true, xdebug);
                } else {
                    xmlAssembly = XmlAssemblyFactory.createXmlAssembly(cpathIds,
                        XmlRecordType.PSI_MI, hits.length(),
                        XmlAssemblyFactory.XML_FULL, false, xdebug);
                }
            } else {
                xmlAssembly = XmlAssemblyFactory.createXmlAssembly(cpathIds,
                    XmlRecordType.PSI_MI, hits.length(),
                    XmlAssemblyFactory.XML_FULL, true, xdebug);
            }
        } else {
            xmlAssembly = XmlAssemblyFactory.createEmptyXmlAssembly(xdebug);
        }
        return xmlAssembly;
    }

}
