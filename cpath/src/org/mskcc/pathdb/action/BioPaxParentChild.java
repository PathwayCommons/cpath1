package org.mskcc.pathdb.action;

import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionForm;
import org.mskcc.pathdb.xdebug.XDebug;
import org.mskcc.pathdb.sql.dao.DaoCPath;
import org.mskcc.pathdb.sql.dao.DaoInternalLink;
import org.mskcc.pathdb.sql.dao.DaoException;
import org.mskcc.pathdb.model.CPathRecord;
import org.mskcc.pathdb.model.TypeCount;
import org.mskcc.pathdb.model.GlobalFilterSettings;
import org.mskcc.pathdb.schemas.biopax.summary.EntitySummaryParser;
import org.mskcc.pathdb.schemas.biopax.summary.EntitySummary;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.Set;
import java.util.Iterator;

/**
 * Shows BioPAX Parent Child Data.
 *
 * @author Ethan Cerami.
 */
public class BioPaxParentChild extends BaseAction {
    public static int MAX_RECORDS = 10;

    public ActionForward subExecute (ActionMapping mapping, ActionForm form,
            HttpServletRequest request, HttpServletResponse response, XDebug xdebug)
            throws Exception {
        DaoCPath dao = DaoCPath.getInstance();
        String id = request.getParameter("id");
        String command = request.getParameter("command");
        String type = request.getParameter("type");
        String startIndex = request.getParameter("startIndex");
        String maxRecords = request.getParameter("maxRecords");
        CPathRecord record = null;
        if (id != null) {
            xdebug.logMsg(this, "Using cPath ID:  " + id);
            record = dao.getRecordById(Long.parseLong(id));
            xdebug.logMsg(this, "cPath Record Name:  " + record.getName());
        }

        DaoInternalLink daoLinker = new DaoInternalLink();

        //  Determine Filter Settings
        HttpSession session = request.getSession();
        GlobalFilterSettings filterSettings = (GlobalFilterSettings) session.getAttribute
                (GlobalFilterSettings.GLOBAL_FILTER_SETTINGS);
        if (filterSettings == null) {
            filterSettings = new GlobalFilterSettings();
            session.setAttribute(GlobalFilterSettings.GLOBAL_FILTER_SETTINGS,
                    filterSettings);
        }
        xdebug.logMsg(this, "Determining Global Filter Settings");

        int taxId = getTaxonomyIdFilter(filterSettings, xdebug);
        long snapshotIds[] = getSnapshotFilter(filterSettings, xdebug);

        ArrayList records;
        int start = 0;
        if (startIndex != null) {
            start = Integer.parseInt(startIndex);
        }
        int max = MAX_RECORDS;
        if (maxRecords != null) {
            max = Integer.parseInt(maxRecords);
        }

        //  Get parent or child elements.
        if (command != null && command.equals("getParents")) {
            records = getParents(xdebug, daoLinker, id, taxId, snapshotIds, type,
                    start, max);
        } else {
            records = getChildren(xdebug, daoLinker, id, taxId, snapshotIds, type,
                    start, max);
        }

        //  Get entity summaries
        if (records != null) {
            request.setAttribute("RECORD_LIST", records);
            ArrayList summaryList = new ArrayList();
            for (int i=0; i<records.size(); i++) {
                CPathRecord record0 = (CPathRecord) records.get(i);
                EntitySummaryParser parser = new EntitySummaryParser(record0.getId());
                EntitySummary summary = parser.getEntitySummary();
                summaryList.add(summary);
                xdebug.logMsg(this, "Got summary for:  [cPath ID: " + summary.getRecordID()
                    + "] --> " + summary.getName());
            }
            request.setAttribute("SUMMARY_LIST", summaryList);
            request.setAttribute("START", start);
            request.setAttribute("MAX", max);
        }
        return mapping.findForward(BaseAction.FORWARD_SUCCESS);
    }

    private ArrayList getChildren (XDebug xdebug, DaoInternalLink daoLinker, String id, int taxId,
            long[] snapshotIds, String type, int start, int max)
            throws DaoException {
        xdebug.logMsg(this, "Determing types of all child elements");
        ArrayList childTypes = daoLinker.getChildrenTypes(Long.parseLong(id),
                taxId, snapshotIds, xdebug);

        if (childTypes.size() ==0) {
                xdebug.logMsg(this, "No child types found");
        }
        for (int i=0; i<childTypes.size(); i++) {
            TypeCount typeCount = (TypeCount) childTypes.get(i);
            xdebug.logMsg(this, "Specific type:  " + typeCount.getType()
                + " -->  " + typeCount.getCount() + " records");
        }

        if (type != null) {
            xdebug.logMsg(this, "Getting children.  Restricting results to records of type:  "
                    + type);
            xdebug.logMsg (this, "Start Index is set to:  " + start);
            xdebug.logMsg (this, "Max Records is set to:  " + max);
            ArrayList records = daoLinker.getChildren(Long.parseLong(id), taxId, snapshotIds,
                    type, start, max, xdebug);
            if (records.size() ==0) {
                xdebug.logMsg(this, "No children found");
            }
            for (int j=0; j<records.size(); j++) {
                CPathRecord childRecord = (CPathRecord) records.get(j);
                xdebug.logMsg(this, "[cPathID:  " + childRecord.getId() + "]  "
                    + childRecord.getSpecificType() + ":  " + childRecord.getName());
            }
            return records;
        }
        return null;
    }

    private ArrayList getParents (XDebug xdebug, DaoInternalLink daoLinker, String id, int taxId,
            long[] snapshotIds, String type, int start, int max)
            throws DaoException {
        xdebug.logMsg(this, "Determing types of all parent elements");
        ArrayList parentTypes = daoLinker.getParentTypes(Long.parseLong(id),
                taxId, snapshotIds, xdebug);


        if (parentTypes.size() ==0) {
            xdebug.logMsg (this, "No parent types found");
        }
        for (int i=0; i<parentTypes.size(); i++) {
            TypeCount typeCount = (TypeCount) parentTypes.get(i);
            xdebug.logMsg(this, "Specific type:  " + typeCount.getType()
                + " -->  " + typeCount.getCount() + " records");
        }

        if (type != null) {
            xdebug.logMsg(this, "Getting parents.  Restricting results to records of type:  "
                + type);
            xdebug.logMsg (this, "Start Index is set to:  " + start);
            xdebug.logMsg (this, "Max Records is set to:  " + max);
            ArrayList records = daoLinker.getParents(Long.parseLong(id), taxId, snapshotIds,
                    type, start, max, xdebug);
            if (records.size() ==0) {
                xdebug.logMsg(this, "No parents found");
            }
            for (int j=0; j<records.size(); j++) {
                CPathRecord childRecord = (CPathRecord) records.get(j);
                xdebug.logMsg(this, "[cPathID:  " + childRecord.getId() + "]  "
                    + childRecord.getSpecificType() + ":  " + childRecord.getName());
            }
            return records;
        }
        return null;
    }

    /**
     * Determine Organism Filter.
     */
    private int getTaxonomyIdFilter (GlobalFilterSettings filterSettings, XDebug xdebug) {
        int taxId = -1;
        Set organismSet = filterSettings.getOrganismTaxonomyIdSet();
        Iterator organismIterator = organismSet.iterator();
        while (organismIterator.hasNext()) {
            Integer ncbiTaxonomyId = (Integer) organismIterator.next();
            if (ncbiTaxonomyId == GlobalFilterSettings.ALL_ORGANISMS_FILTER_VALUE) {
                xdebug.logMsg (this, "Organism Filter set to:  ALL ORGANISMS");
            } else {
                xdebug.logMsg (this, "Organism Filter set to:  " + ncbiTaxonomyId);
                taxId = ncbiTaxonomyId;
            }
        }
        return taxId;
    }

    /**
     * Determine Data Source Filter.
     */
    private long[] getSnapshotFilter (GlobalFilterSettings filterSettings, XDebug xdebug) {
        Set snapshotSet = filterSettings.getSnapshotIdSet();
        long snapshotIds [] = new long[snapshotSet.size()];
        Iterator snapshotIterator = snapshotSet.iterator();
        int index = 0;
        while (snapshotIterator.hasNext()) {
            Long snapshotId = (Long) snapshotIterator.next();
            xdebug.logMsg (this, "Snapshot Filter set to:  " + snapshotId);
            snapshotIds[index++] = snapshotId;
        }
        return snapshotIds;

    }
}
