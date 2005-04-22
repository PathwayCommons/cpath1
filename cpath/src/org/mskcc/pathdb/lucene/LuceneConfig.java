/** Copyright (c) 2004 Memorial Sloan-Kettering Cancer Center.
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
package org.mskcc.pathdb.lucene;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.mskcc.dataservices.util.PropertyManager;

import java.io.File;

/**
 * Lucene Config Class.
 *
 * @author Ethan Cerami
 */
public class LuceneConfig {

    /**
     * Default Lucene Field for Storing All Terms.
     */
    public static final String FIELD_ALL = "all";

    /**
     * Lucene Field for Storing Intractor CPath ID.
     */
    public static final String FIELD_INTERACTOR_ID = "interactor_id";

    /**
     * Lucene Field for Storing CPath ID.
     */
    public static final String FIELD_CPATH_ID = "cpath_id";

    /**
     * Lucene Field for Organism Information.
     */
    public static final String FIELD_ORGANISM = "organism";

    /**
     * Text Index Directory.
     */
    public static final String INDEX_DIR_PREFIX = "textIndex";

    /**
     * Lucene Directory System Property
     */
    public static final String PROPERTY_LUCENE_DIR = "lucene.dir";

    /**
     * Space Character
     */
    public static final String SPACE = " ";

    /**
     * Gets Directory for Full Text Indexer.
     *
     * @return Directory Location.
     */
    public static String getLuceneDirectory() {
        PropertyManager manager = PropertyManager.getInstance();
        String dir = manager.getProperty(PROPERTY_LUCENE_DIR);
        //  dir should only be null when run from the command line.        
        if (dir == null) {
            String cPathHome = System.getProperty("CPATH_HOME");
            dir = cPathHome + File.separator + "build" + File.separator
                    + "WEB-INF" + File.separator + INDEX_DIR_PREFIX;
        }
        return dir;
    }

    /**
     * Gets Analyzer.
     * Index and Query must use the same Analyzer.
     *
     * @return Analyzer Object.
     */
    public static Analyzer getLuceneAnalyzer() {
        return new StandardAnalyzer();
    }
}