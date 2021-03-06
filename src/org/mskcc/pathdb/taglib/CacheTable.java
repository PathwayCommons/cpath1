// $Id: CacheTable.java,v 1.17 2007-04-16 20:18:31 cerami Exp $
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
package org.mskcc.pathdb.taglib;

import org.mskcc.pathdb.model.XmlCacheRecord;
import org.mskcc.pathdb.protocol.ProtocolRequest;
import org.mskcc.pathdb.sql.dao.DaoException;
import org.mskcc.pathdb.sql.dao.DaoXmlCache;
import org.mskcc.pathdb.util.security.XssFilter;
import org.mskcc.pathdb.xdebug.XDebug;
import org.apache.log4j.Logger;

import javax.servlet.http.HttpUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.StringTokenizer;

/**
 * Custom JSP Tag for Displaying the Cache Contents.
 *
 * @author Ethan Cerami
 */
public class CacheTable extends HtmlTable {
    private static Logger log = Logger.getLogger(CacheTable.class);

    /**
     * Executes JSP Custom Tag
     *
     * @throws Exception Exception in writing to JspWriter.
     */
    public void subDoStartTag () throws Exception {
        String headers[] = {"#", "URL", "Command",
                "Query", "Format", "Organism",
                "Last Used"};
        createHeader("XML Cache Contents");
        startTable();
        createTableHeaders(headers);
        outputRecords();
        endTable();
    }

    /**
     * Output Cached Records.
     */
    private void outputRecords () throws DaoException {
        DaoXmlCache dao = new DaoXmlCache(new XDebug());
        ArrayList records = dao.getAllRecords();
        if (records.size() == 0) {
            startRow();
            append("<td colspan='5'>No Records in Cache</td>");
            endRow();
        } else {
            for (int i = 0; i < records.size(); i++) {
                XmlCacheRecord record = (XmlCacheRecord) records.get(i);
                startRow(i);
                outputDataField(Integer.toString(i));
                outputProtocolRequest(record);
                outputDataField(record.getLastUsed());
                endRow();
            }
        }
    }

    private void outputProtocolRequest (XmlCacheRecord record) {
        try {
            String url = record.getUrl();
            url = url.replaceAll("&amp;", "&");
            Hashtable params1 = HttpUtils.parseQueryString(url);
            HashMap params2 = XssFilter.filterAllParameters(params1);
            ProtocolRequest request = new ProtocolRequest(params2);
            outputDataField("<a href='" + request.getUri() + "'>URL Link</a>");
            outputDataField(request.getCommand());
            outputDataField(request.getQuery());
            outputDataField(request.getFormat());
            outputDataField(request.getOrganism());
        } catch (Exception e) {
            append("<td>" + e.getMessage() + "</td>");
            append("<td>N/A</td>");
        }
    }
}
