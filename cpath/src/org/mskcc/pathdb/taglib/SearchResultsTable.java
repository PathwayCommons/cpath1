/** Copyright (c) 2003 Institute for Systems Biology, University of
 ** California at San Diego, and Memorial Sloan-Kettering Cancer Center.
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
 ** documentation provided hereunder is on an "as is" basis, and the
 ** Institute for Systems Biology, the University of California at San Diego
 ** and/or Memorial Sloan-Kettering Cancer Center
 ** have no obligations to provide maintenance, support,
 ** updates, enhancements or modifications.  In no event shall the
 ** Institute for Systems Biology, the University of California at San Diego
 ** and/or Memorial Sloan-Kettering Cancer Center
 ** be liable to any party for direct, indirect, special,
 ** incidental or consequential damages, including lost profits, arising
 ** out of the use of this software and its documentation, even if the
 ** Institute for Systems Biology, the University of California at San
 ** Diego and/or Memorial Sloan-Kettering Cancer Center
 ** have been advised of the possibility of such damage.  See
 ** the GNU Lesser General Public License for more details.
 **
 ** You should have received a copy of the GNU Lesser General Public License
 ** along with this library; if not, write to the Free Software Foundation,
 ** Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 **/
package org.mskcc.pathdb.taglib;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.search.Hits;
import org.mskcc.pathdb.controller.ProtocolConstants;
import org.mskcc.pathdb.controller.ProtocolRequest;
import org.mskcc.pathdb.lucene.LuceneIndexer;
import org.mskcc.pathdb.sql.dao.DaoException;
import org.mskcc.pathdb.sql.query.QueryException;

import java.io.IOException;

/**
 * Custom JSP Tag for Displaying Search Results Table.
 *
 * @author Ethan Cerami
 */
public class SearchResultsTable extends HtmlTable {
    private int HITS_PER_PAGE = 10;
    private ProtocolRequest pRequest;
    private String uid;
    private Pager pager;

    /**
     * Sets UID Parameter.
     * @param request ProtocolRequest.
     */
    public void setProtocolRequest(ProtocolRequest request) {
        this.pRequest = request;
        this.uid = request.getUid();
    }

    /**
     * Start Tag Processing.
     * @throws DaoException Database Access Error.
     */
    protected void subDoStartTag() throws DaoException, QueryException,
            IOException {
        LuceneIndexer lucene = new LuceneIndexer();
        Hits hits = lucene.executeQuery(uid);
        this.pager = new Pager (pRequest, hits.length());
        createHeader(hits);
        String headers[] = {"", "Name", "Description", "Interactions"};
        createTableHeaders(headers);
        outputResults(hits);
        endTable();
        lucene.closeIndexSearcher();
    }

    private void createHeader(Hits hits) {
        String title = "Matching Results found for:  " + uid.toUpperCase();
        append("<table valign=top width=100% cellpadding=7 border=0 "
                + "cellspacing=0>"
                + "<tr><td colspan=3 bgcolor=#666699><u>"
                + "<b><big>" + title + "</big>"
                + "</b></u><br></td>");
        append("<td align=right nowrap width=1%>"
            +"<font face=arial size=-1>");
        if (hits.length() > 0) {
            append (pager.getHeaderHtml());
        }
        append ("</td></tr>");
        append ("</table>");
        append("<table valign=top width=100% cellpadding=7 border=0 "
                + "cellspacing=0>");
    }

    /**
     * Outputs Interaction Data.
     */
    private void outputResults(Hits hits) throws IOException {
        if (hits.length()== 0) {
            append("<TR>");
            append("<TD COLSPAN=4>No Matching Results found!");
            append("</TR>");
        }
        for (int i = pager.getStartIndex(); i < pager.getEndIndex(); i++) {
            append("<TR>");
            Document doc = hits.doc(i);
            Field name = doc.getField(LuceneIndexer.FIELD_NAME);
            Field desc = doc.getField(LuceneIndexer.FIELD_DESCRIPTION);
            append("<TD VALIGN=TOP WIDTH=20>");
            append(Integer.toString(i+1)+".");
            append("</TD>");
            append("<TD VALIGN=TOP WIDTH=60>");
            append(name.stringValue());
            append("</TD>");
            outputDataField(desc.stringValue());
            outputInteractionLink(name.stringValue());
            append("</TR>");
        }
    }

    private void outputInteractionLink(String uid) {
        String url = this.getInteractionLink
                (uid, ProtocolConstants.FORMAT_HTML);
        append("<TD>");
        this.outputLink("View Interactions", url);
        append("</TD>");
    }
}