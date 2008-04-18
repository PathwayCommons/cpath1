// $Id: CountExternalIdsTask.java,v 1.1 2008-04-18 17:36:39 cerami Exp $
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
package org.mskcc.pathdb.task;

import org.mskcc.pathdb.model.*;
import org.mskcc.pathdb.schemas.biopax.BioPaxConstants;
import org.mskcc.pathdb.sql.dao.DaoCPath;
import org.mskcc.pathdb.sql.dao.DaoException;
import org.mskcc.pathdb.sql.dao.DaoExternalLink;
import org.mskcc.pathdb.util.tool.ConsoleUtil;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Given a TaxonomyId, this class locates all proteins records
 * for the specified organism, and calculates how many of these records have
 * the specified external identifier.
 *
 * @author Ethan Cerami.
 */
public class CountExternalIdsTask extends Task {
    private static final String AFFYMETRIX_NAME = "AFFYMETRIX";
    private static final String ENTREZ_GENE_NAME = "ENTREZ_GENE";
    private int taxonomyId;
    private int affyCount = 0;
    private int totalNumRecords;
    private HashMap dbMap;
    private int numEntitiesWithoutXrefs;
    private ArrayList entitiesWithOutXRefs = new ArrayList();
    private String target;

    /**
     * Constructor.
     *
     * @param taxonomyId  NCBI Taxonomy ID.
     * @param externalIdType:  0 = Affymetrix;  1 = Entrez Gene.
     * @param consoleMode Console Flag.  Set to true for console tools.
     * @throws DaoException Error Connecting to Database.
     */
    public CountExternalIdsTask(int taxonomyId, int externalIdType,
            boolean consoleMode)
            throws DaoException {
        super("Counting External IDs IDs", consoleMode);
        if (externalIdType == 0) {
            target = AFFYMETRIX_NAME;
        } else if (externalIdType == 1) {
            target = ENTREZ_GENE_NAME;
        } else {
            throw new IllegalArgumentException ("Invalid option:  " + externalIdType);
        }
        ProgressMonitor pMonitor = this.getProgressMonitor();
        pMonitor.setCurrentMessage("Counting Externals IDs for Organism  "
                + " -->  NCBI Taxonomy ID:  " + taxonomyId + ", External ID:  " + target);
        this.taxonomyId = taxonomyId;
        this.execute();
        pMonitor.setCurrentMessage("\nTotal Number of Physical Entities "
                + "for NCBI Taxonomy ID " + taxonomyId + ":  "
                + this.totalNumRecords);

        if (totalNumRecords > 0) {
            double percent = (affyCount / (double) totalNumRecords) * 100.0;
            DecimalFormat formatter = new DecimalFormat("###,###.##");
            String percentOut = formatter.format(percent);
            pMonitor.setCurrentMessage("Of these, " + affyCount
                    + " (" + percentOut
                    + "%) have " + target +  " IDs.");
        }

        pMonitor.setCurrentMessage("\nOf those physical entities without " + target
                + " IDs, the following databases were found:  ");
        Iterator keys = dbMap.keySet().iterator();
        while (keys.hasNext()) {
            String dbName = (String) keys.next();
            Integer counter = (Integer) dbMap.get(dbName);
            if (consoleMode) {
                System.out.println(dbName + ":  " + counter);
            }
        }
        pMonitor.setCurrentMessage("\nTotal Number of physical entities that have no "
                + "external database identifiers:  " + numEntitiesWithoutXrefs);
        pMonitor.setCurrentMessage("\nThe following physical entities have no "
                + "external database identifiers:  ");
        for (int i = 0; i < entitiesWithOutXRefs.size(); i++) {
            CPathRecord record = (CPathRecord) entitiesWithOutXRefs.get(i);
            pMonitor.setCurrentMessage(record.getName() + ", [cPath ID:  "
                    + record.getId() + "]");
        }
    }

    /**
     * Gets Total Number of Physical Entities for Organism.
     *
     * @return integer value.
     */
    public int getTotalNumRecords() {
        return this.totalNumRecords;
    }

    /**
     * Gets Number of Physical Entities for Organism that contain an Affymetrix
     * identifier.
     *
     * @return integer value.
     */
    public int getNumRecordsWithAffymetrixIds() {
        return this.affyCount;
    }

    /**
     * Performs LookUp.
     *
     * @throws DaoException Error Connecting to Database.
     */
    private void execute() throws DaoException {
        dbMap = new HashMap();
        ProgressMonitor pMonitor = this.getProgressMonitor();

        //  Retrieve all Physical Entities for Specified Organism
        DaoCPath dao = DaoCPath.getInstance();
        ArrayList records = dao.getPhysicalEntityRecordByTaxonomyID (taxonomyId);

        this.totalNumRecords = 0;
        pMonitor.setMaxValue(records.size());
        pMonitor.setCurValue(1);

        //  Examine Each Physical Entity
        for (int i = 0; i < records.size(); i++) {
            ConsoleUtil.showProgress(pMonitor);
            CPathRecord record = (CPathRecord) records.get(i);
            String xmlContent = record.getXmlContent();

            XmlRecordType xmlType = record.getXmlType();
            String specificType = record.getSpecificType();
            if (xmlType.equals(XmlRecordType.PSI_MI)
                    || xmlType.equals(XmlRecordType.BIO_PAX)
                    && specificType.equals(BioPaxConstants.PROTEIN)) {
                totalNumRecords++;
                if (xmlContent.toUpperCase().indexOf(target) > -1) {
                    affyCount++;
                } else {
                    trackOtherIds(record);
                }
            }
            pMonitor.incrementCurValue();
        }
    }

    private void trackOtherIds(CPathRecord record)
            throws DaoException {
        DaoExternalLink daoExternalLinker = DaoExternalLink.getInstance();
        ArrayList externalLinkList = daoExternalLinker.getRecordsByCPathId
                (record.getId());
        if (externalLinkList.size() == 0) {
            numEntitiesWithoutXrefs++;
            recordEmptyEntity(record);
        } else {
//            System.out.println("Record:  " + record.getId());
            for (int i = 0; i < externalLinkList.size(); i++) {
                ExternalLinkRecord externalLink = (ExternalLinkRecord)
                        externalLinkList.get(i);
                ExternalDatabaseRecord externalDb =
                        externalLink.getExternalDatabase();
//                System.out.println("---  " + externalDb.getName()
//                    + ":  " + externalLink.getLinkedToId());
                incrementMapCounter(externalDb);
            }
        }
    }

    private void recordEmptyEntity(CPathRecord record) {
        entitiesWithOutXRefs.add(record);
    }

    private void incrementMapCounter(ExternalDatabaseRecord externalDb) {
        String key = externalDb.getName();
        if (dbMap.containsKey(key)) {
            Integer counter = (Integer) dbMap.get(key);
            counter = new Integer(counter.intValue() + 1);
            dbMap.put(key, counter);
        } else {
            dbMap.put(key, new Integer(1));
        }
    }
}