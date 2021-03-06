package org.mskcc.pathdb.bb.action;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.mskcc.pathdb.model.*;
import org.mskcc.pathdb.sql.dao.*;
import org.mskcc.pathdb.xdebug.XDebug;
import org.mskcc.pathdb.action.BaseAction;
import org.mskcc.pathdb.bb.sql.dao.DaoBBPathway;
import org.mskcc.pathdb.bb.sql.dao.DaoBBInternalLink;
import org.mskcc.pathdb.bb.sql.dao.DaoBBGene;
import org.mskcc.pathdb.bb.model.BBPathwayRecord;
import org.mskcc.pathdb.bb.model.BBInternalLinkRecord;
import org.mskcc.pathdb.bb.model.BBPathwayRecord;
import org.mskcc.pathdb.bb.model.BBGeneRecord;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.util.ArrayList;

public class WebService extends BaseAction {

    public ActionForward subExecute (ActionMapping mapping, ActionForm form,
            HttpServletRequest request, HttpServletResponse response, XDebug xdebug)
            throws Exception {
        response.setContentType("text/plain");
        PrintWriter writer = response.getWriter();
        String action = request.getParameter("action");
		if (action != null) {
			if (action.equals("getMembers")) {
				getMembers(request, writer);
			}
			else if (action.equals("getPathways")) {
				getPathways(request, writer);
			}
        }
        return null;
    }

	private void getMembers (HttpServletRequest request, PrintWriter writer) throws DaoException {

        String q = request.getParameter("q");
        if (q == null || q.equals("")) {
            writer.println("Ooops.  Please specify a Pathway ID, e.g. q=1");
        }
		else {
			StringBuffer buffer = new StringBuffer();
			if (buffer.length() == 0) {
				getMembersFromCPathTable(q, buffer);
			}
			if (buffer.length() == 0) {
				writer.println("Ooops.  No matches found for Pathway ID: " + q);
			}
			else {
				writer.println("#Generated by Bare Bones Pathway Commons\n");
				writer.println(buffer.toString());
			}
		}
	}

	private void getMembersFromBBTable (String pathwayID, StringBuffer buffer) throws DaoException {

		// get pathway record
		DaoBBPathway daoBBPathway = new DaoBBPathway();
		BBPathwayRecord pathwayRecord = daoBBPathway.getBBPathway(pathwayID);
		if (pathwayRecord == null) return;

		// get descendents
		DaoBBInternalLink daoBBInternalLink = new DaoBBInternalLink();
		ArrayList<BBInternalLinkRecord> links = daoBBInternalLink.getLinksByPathwayID(pathwayRecord.getPathwayID());
		if (links.size() > 0) {
			buffer.append("#Pathway Name: " + pathwayRecord.getPathwayName() + "\n");
			buffer.append("#Data Source: " + pathwayRecord.getSource() + "\n");
			buffer.append("#Name[TAB][ENTREZ GENE ID || ChEBI ID || NO_IDS]\n");
			DaoBBGene daoBBGene = new DaoBBGene();
			for (BBInternalLinkRecord linkRecord : links) {
				BBGeneRecord geneRecord = daoBBGene.getBBGene(linkRecord.getEntrezGeneID());
				if (geneRecord != null) {
					buffer.append(geneRecord.getGeneName() + "\t" +
								  "ENTREZ_GENE:" + geneRecord.getEntrezGeneID() + "\n");
				}
			}
		}
	}

    private void getMembersFromCPathTable (String pathwayID, StringBuffer buffer) throws DaoException {
		
		DaoCPath dao = DaoCPath.getInstance();
		DaoExternalDbSnapshot daoSnapshot = new DaoExternalDbSnapshot();
		CPathRecord record = dao.getRecordById(Integer.parseInt(pathwayID));
		buffer.append("#Pathway Name: " + record.getName() + "\n");
		ExternalDatabaseSnapshotRecord snapshot =
			daoSnapshot.getDatabaseSnapshot(record.getSnapshotId());
		buffer.append("#Data Source: " + snapshot.getExternalDatabase().getMasterTerm() + "\n");
		buffer.append("#Name[TAB][UNIPROT_ID || ENTREZ GENE ID || ChEBI ID || NO_IDS]\n");
		DaoInternalFamily daoFamily = new DaoInternalFamily();
		long peIds[] = daoFamily.getDescendentIds(Integer.parseInt(pathwayID),
												  CPathRecordType.PHYSICAL_ENTITY);
		DaoExternalLink daoExternalLink = DaoExternalLink.getInstance();
		for (int i=0; i<peIds.length; i++) {
			CPathRecord peRecord = dao.getRecordById(peIds[i]);
			//  Don't show complexes
			if (!peRecord.getSpecificType().equalsIgnoreCase("complex")) {
				buffer.append(peRecord.getName());
				ArrayList xrefList = daoExternalLink.getRecordsByCPathId(peRecord.getId());
				int counter = 0;
				buffer.append("\t");
				for (int j=0; j<xrefList.size(); j++) {
					ExternalLinkRecord xref = (ExternalLinkRecord) xrefList.get(j);
					String dbTerm = xref.getExternalDatabase().getMasterTerm();
					if (dbTerm.equals("UNIPROT")) {
						buffer.append(xref.getLinkedToId() + " ");
						counter++;
					}
				}
				if (counter == 0) {
					buffer.append("NO_IDS");
				}
				buffer.append("\n");
			}
		}
    }

    private void getPathways (HttpServletRequest request, PrintWriter writer) throws DaoException {
        String q = request.getParameter("q");
        if (q == null || q.equals("")) {
            writer.println("Ooops.  Please specify an Entrez Gene ID, e.g. q=1950");
        } else {
			StringBuffer buffer = new StringBuffer();
			getPathwaysFromBBTable(q, buffer);
			getPathwaysFromCPathTable(q, buffer);
            if (buffer.length() == 0) {
                writer.println("Ooops.   No matches found for ENTREZ_GENE: " + q);
            }
			else {
                writer.println("#Generated by Bare Bones Pathway Commons");
				writer.println("#PATHWAY_NAME[TAB]PATHWAY_SOURCE[TAB]URL");
				writer.println(buffer.toString());
			}
        }
    }

	private void getPathwaysFromBBTable (String geneID, StringBuffer buffer) throws DaoException {

		DaoBBInternalLink daoBBInternalLink = new DaoBBInternalLink();
		ArrayList<BBInternalLinkRecord> links = daoBBInternalLink.getLinksByGeneID(geneID);
		if (links.size() > 0) {
			DaoBBPathway daoBBPathway = new DaoBBPathway();
			for (BBInternalLinkRecord linkRecord : links) {
				BBPathwayRecord pathwayRecord = daoBBPathway.getBBPathway(linkRecord.getPathwayID());
				if (pathwayRecord != null) {
					buffer.append(pathwayRecord.getPathwayName() + "\t" +
								  pathwayRecord.getSource() + "\t" +
								  pathwayRecord.getURL() + "\n");
				}
			}
		}
	}

    private void getPathwaysFromCPathTable (String geneID, StringBuffer buffer) throws DaoException {

		DaoExternalDb dao = new DaoExternalDb();
		ExternalDatabaseRecord dbRecord = dao.getRecordByTerm("ENTREZ_GENE");
		DaoExternalLink linker = DaoExternalLink.getInstance();
		ArrayList recordList = linker.getRecordByDbAndLinkedToId(dbRecord.getId(), geneID);
		if (recordList.size() == 0) return;

		DaoInternalFamily daoFamily = new DaoInternalFamily();
		ExternalLinkRecord externalLink = (ExternalLinkRecord) recordList.get(0);
		long pathwayIds[] = daoFamily.getAncestorIds(externalLink.getCpathId(),
													 CPathRecordType.PATHWAY);
		for (int i = 0; i < pathwayIds.length; i++) {
			DaoCPath daoCPath = DaoCPath.getInstance();
			CPathRecord record = daoCPath.getRecordById(pathwayIds[i]);
			long snapshotId = record.getSnapshotId();
			DaoExternalDbSnapshot daoSnapshot = new DaoExternalDbSnapshot();
			ExternalDatabaseSnapshotRecord snapshotRecord = daoSnapshot.getDatabaseSnapshot(snapshotId);
			ExternalDatabaseRecord databaseRecord = snapshotRecord.getExternalDatabase();
			String url = getURL(linker, record, databaseRecord);
			url = (url == null) ? "" : url;
			buffer.append(record.getName() + "\t" +
						  databaseRecord.getMasterTerm() + "\t" +
						  url + "\n");
		}
    }

	private String getURL(DaoExternalLink linker, CPathRecord record, ExternalDatabaseRecord databaseRecord)
		throws DaoException {

		ArrayList<ExternalLinkRecord> externalLinks = linker.getRecordsByCPathId(record.getId());
		for (ExternalLinkRecord externalLink : externalLinks) {
			if (externalLink.getExternalDatabase().getName().equals(databaseRecord.getName())) {
				return externalLink.getWebLink();
			}
			
		}
		return null;
	}
}
