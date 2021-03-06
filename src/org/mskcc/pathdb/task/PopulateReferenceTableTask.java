// $id$
//------------------------------------------------------------------------------
/** Copyright (c) 2006 Memorial Sloan-Kettering Cancer Center.
 **
 ** Code written by: Ethan Cerami, Benjamin Gross
 ** Authors: Ethan Cerami, Gary Bader, Benjamin Gross, Chris Sander
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

// imports
import org.mskcc.pathdb.xdebug.XDebug;
import org.mskcc.pathdb.util.CPathConstants;
import org.mskcc.pathdb.sql.dao.DaoReference;
import org.mskcc.pathdb.sql.dao.DaoException;
import org.mskcc.pathdb.sql.dao.DaoExternalDb;
import org.mskcc.pathdb.sql.dao.DaoExternalLink;
import org.mskcc.pathdb.model.Reference;
import org.mskcc.pathdb.model.CPathRecord;
import org.mskcc.pathdb.model.ExternalLinkRecord;
import org.mskcc.pathdb.model.ExternalDatabaseRecord;

import org.jdom.Element;
import org.jdom.Document;
import org.jdom.Attribute;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.xpath.XPath;

import java.net.URL;
import java.net.URLConnection;

import java.util.List;
import java.util.ArrayList;

import java.io.Reader;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * Task to populate the reference table.
 *
 * @author Benjamin Gross
 */
public class PopulateReferenceTableTask extends Task {

	// ref to debug logger.
    private XDebug xdebug;

	// number of records to batch before fetching from ncbi
	private static final int NCBI_BATCH_SIZE = 100;

	// number of milliseconds between calls to ncbi
	private static final int NCBI_DELAY = 4000;

	// url to ncbi service
	private static final String NCBI_URL = "http://eutils.ncbi.nlm.nih.gov/entrez/eutils/efetch.fcgi?db=pubmed&retmode=xml&email=grossb%40cbio.mskcc.org&tool=cpath&id=";

	// used to capture time between calls to ncbi
	private long startTimeOfFetch = 0;

    /**
     * Constructor.
     *
     * @param consoleMode Running in Console Mode.
     * @param xdebug      XDebug Object.
     */
    public PopulateReferenceTableTask(boolean consoleMode, XDebug xdebug) {
        super("Populating Reference Table", consoleMode);
        this.xdebug = xdebug;
        ProgressMonitor pMonitor = this.getProgressMonitor();
        pMonitor.setCurrentMessage("Populating Reference Table");
    }

    /**
     * Runs the Task.
     */
    public void run() {
        try {
            executeTask();
        } catch (Exception e) {
            setThrowable(e);
        }
    }

    /**
     * Executes the task.
	 *
     * @throws DaoException
	 * @throws InterruptedException
	 * @throws IOException
	 * @throws JDOMException
     */
    public void executeTask() throws DaoException, InterruptedException, IOException, JDOMException  {

		// setup some vars
        ProgressMonitor pMonitor = this.getProgressMonitor();
		DaoExternalDb daoExternalDb = new DaoExternalDb();
		DaoExternalLink daoExternalLink = DaoExternalLink.getInstance();

		// process pubmed
        pMonitor.setCurrentMessage("Processing PubMed records");
		processPubMed(daoExternalDb, daoExternalLink, pMonitor);

		// outta here
        pMonitor.setCurrentMessage("Done");
        xdebug.stopTimer();
    }

	/**
	 * Process the pubmed records.
	 *
	 * @param daoExternalDb DaoExternalDb
	 * @param daoExternalLink DaoExternalLink
	 * @throws DaoException
	 * @throws InterruptedException
	 * @throws IOException	 
	 * @throws JDOMException
	 */
	public void processPubMed(DaoExternalDb daoExternalDb, DaoExternalLink daoExternalLink,
            ProgressMonitor pMonitor)
		throws DaoException, InterruptedException, IOException, JDOMException {
		
		// grab all pubmed records
		ExternalDatabaseRecord dbRecord = daoExternalDb.getRecordByName("PubMed");
		ArrayList<ExternalLinkRecord> externalLinks =
			daoExternalLink.getRecordByDbAndLinkedToId(dbRecord.getId(), null);

		// vars used for record processing
		ArrayList<String> recordsToFetch = new ArrayList<String>();
		DaoReference daoReference = new DaoReference();

		// interate over the records
		for (ExternalLinkRecord externalLinkRecord : externalLinks) {

			// get the linked to id
			String linkedToId = externalLinkRecord.getLinkedToId();

            // if this is not a number, continue
            try {
                Integer.parseInt(linkedToId);
            } catch (NumberFormatException e) {
                continue;                            
            }

            // is this already in reference table ?
			// if so, check for completeness (no N/A strings)
			// if complete, skip it, if not complete, fetch data from ncbi
			Reference reference = daoReference.getRecord(linkedToId, dbRecord.getId());
			if (completeReference(reference)) continue;
			// if the reference is not null, but not complete, delete it from table
			if (reference != null) daoReference.deleteRecordById(reference.getId());

			// ok, batch this record and fetch from ncbi if 
			recordsToFetch.add(linkedToId);
			if (recordsToFetch.size() == NCBI_BATCH_SIZE) {
				processPubMedBatch(recordsToFetch, false, pMonitor);
				recordsToFetch = new ArrayList<String>();
			}
		}

		// any records left to process ?
		if (!recordsToFetch.isEmpty()) {
			processPubMedBatch(recordsToFetch, false, pMonitor);
			recordsToFetch = new ArrayList<String>();
		}

		// process evidence records
		List<Reference> evidenceReferences = daoReference.getEvidenceRecords(dbRecord.getId());
		for (Reference reference : evidenceReferences) {
			if (completeReference(reference)) continue;
			daoReference.deleteRecordById(reference.getId());

			// ok, batch this record and fetch from ncbi if 
			recordsToFetch.add(reference.getId());
			if (recordsToFetch.size() == NCBI_BATCH_SIZE) {
				processPubMedBatch(recordsToFetch, true, pMonitor);
				recordsToFetch = new ArrayList<String>();
			}
		}

		// any records left to process ?
		if (recordsToFetch.isEmpty()) return;
		processPubMedBatch(recordsToFetch, true, pMonitor);
	}

	/**
	 * Process batch of PubMed records.
	 *
	 * @param recordsToFetch List<String>
	 * @param isEvidenceRecords boolean
	 * @throws DaoException
	 * @throws InterruptedException
	 * @throws IOException
	 * @throws JDOMException
	 */
	public void processPubMedBatch(List<String> recordsToFetch, boolean isEvidenceRecords, ProgressMonitor pMonitor)
		throws DaoException, InterruptedException, IOException, JDOMException {

		// construct query
		String urlString = NCBI_URL;
		for (String id : recordsToFetch) {
			urlString += id + ",";
		}
        pMonitor.setCurrentMessage("Retrieving PubMed records from NCBI.  Num records:  "
                + recordsToFetch.size());
        urlString = urlString.substring(0, urlString.length()-1);  // remove last ","
				
		// if necessary, delay before fetch
		while (System.currentTimeMillis() - startTimeOfFetch < NCBI_DELAY) {
			sleep(100);
		}

		// fetch
		startTimeOfFetch = System.currentTimeMillis();
		URL ncbiURL = new URL(urlString);
		URLConnection urlConnection = ncbiURL.openConnection();
		BufferedReader reader =
			new BufferedReader( new InputStreamReader(urlConnection.getInputStream()));

		// parse data
		parsePubMedData(reader, isEvidenceRecords, ncbiURL, pMonitor);
	}

	/**
	 * Parses PubMed data retrieved from NCBI e-fetch web service
	 *
	 * @param xml Reader
	 * @param isEvidenceRecords boolean
	 * @throws DaoException
	 * @throws IOException
	 * @throws JDOMException
	 */
	private void parsePubMedData(Reader xml, boolean isEvidenceRecords, URL ncbiURL, ProgressMonitor pMonitor)
            throws DaoException, IOException, JDOMException {

        //  setup jdom
        SAXBuilder builder = new SAXBuilder();
        Document ncbiDoc = builder.build(xml);

        //  get the pubmed article set
        Element root = ncbiDoc.getRootElement();
		XPath xpath = XPath.newInstance("/PubmedArticleSet/*");
		List<Element> articleList = xpath.selectNodes(root);
        if (articleList != null && articleList.size() > 0) {

			// init a DaoReference Object
			DaoReference daoReference = new DaoReference();
			
			// iterate over the article set
			for (Element article : articleList) {

                // only process pubmed articles, not pubmed book articles
                if ("PubmedBookArticle".equals(article.getName())) {
                    continue;
                }

				// reference object to add to reference table
				Reference reference = new Reference();
				// get pointer to medline citation element
				Element medlineCitationElement = xpathElement(article,"MedlineCitation");
				// id
				reference.setId(xpathQuery(medlineCitationElement, "PMID", true));
                pMonitor.setCurrentMessage("Processing PMID:  " + reference.getId());
                // database
				reference.setDatabase("PubMed");
				// year
				String year = xpathQuery(medlineCitationElement,
                    "Article/Journal/JournalIssue/PubDate/Year", false);
                if (!year.equals(CPathRecord.NA_STRING)) {
                    reference.setYear(year);
                } else {
                    String date = xpathQuery(medlineCitationElement,
                        "Article/Journal/JournalIssue/PubDate/MedlineDate", false);
                    if (date != null) {
                        reference.setYear(date);
                    }
                }
                // article title
				reference.setTitle(xpathQuery(medlineCitationElement, "Article/ArticleTitle", false));
				// authors
				Element authorListRootElement = xpathElement(medlineCitationElement, "Article/AuthorList");
                if (authorListRootElement != null) {
                    Attribute completeYN = authorListRootElement.getAttribute("CompleteYN");
                    xpath = XPath.newInstance("Article/AuthorList/*");
                    List<Element> authorElementList = xpath.selectNodes(medlineCitationElement);
                    if (authorElementList != null && authorElementList.size() > 0) {
                        reference.setAuthors(constructAuthorList(authorElementList,
                             (completeYN != null) ? completeYN.getValue() : "N"));
                    } else {
                        setNoAuthorsListed(reference);
                    }
                } else {
                    setNoAuthorsListed(reference);
                }
                // source
				reference.setSource(constructSourceString(medlineCitationElement));
				// is evidence flag
				reference.setIsEvidenceReference(isEvidenceRecords);
				// add object to db
				daoReference.addReference(reference);
                String title = reference.getTitle();
                if (title.length() > 70) {
                    title = title.substring(0, 70) + "...";
                }
                pMonitor.setCurrentMessage("-->  Title:  " + title);
            }
		} else {
            pMonitor.setCurrentMessage("No data available for:  " + ncbiURL.toString());
        }
	}

    private void setNoAuthorsListed (Reference reference) {
        String authors[] = new String[1];
        authors[0] = "[No authors listed]";
        reference.setAuthors(authors);
    }

    /**
     * Assists with xml/xpath query
     *
     * @param root Element
     * @param query String
     * @return Element
     * @throws JDOMException
     */
    private Element xpathElement(Element root, String query) throws JDOMException {

        XPath xpath = XPath.newInstance(query);
        Element e = (Element)xpath.selectSingleNode(root);
        if (e != null) {
            return e;
        }
        return null;
    }

	/**
	 * Assists with xml/xpath query
	 *
	 * @param root Element
	 * @param query String
	 * @param checkAssert boolean
	 * @return String
	 * @throws JDOMException
	 */
	private String xpathQuery(Element root, String query, boolean checkAssert)
		throws JDOMException {

		XPath xpath = XPath.newInstance(query);
		Element e = (Element)xpath.selectSingleNode(root);
		if (e != null && e.getTextNormalize().length() > 0) {
			return e.getTextNormalize();
		}
		else {
			if (checkAssert) {
				if (CPathConstants.CPATH_DO_ASSERT) {
					assert false :
					"PopulateReferenceTableTask.parsePubMedData(), missing: " + query;
				}
			}
			else {
				return CPathRecord.NA_STRING;
			}
		}
		return null;
	}

	/**
	 * Constructs array of author names
	 *
	 * @param authorElementList List<Element>
	 * @param completeYN String
	 * @return String[]
	 * @throws JDOMException
	 */
	private String[] constructAuthorList(List<Element> authorElementList, String completeYN)
		throws JDOMException {

		// iterate over the authorElementList
		ArrayList<String> authorStrList = new ArrayList<String>();
		for (Element author : authorElementList) {
			XPath xpath = XPath.newInstance("LastName");
			Element lastName = (Element)xpath.selectSingleNode(author);
			if (lastName != null && lastName.getTextNormalize().length() > 0) {
				String authorStr = lastName.getTextNormalize();
				xpath = XPath.newInstance("Initials");
				Element initials = (Element)xpath.selectSingleNode(author);
				if (initials != null && initials.getTextNormalize().length() > 0) {
					authorStr += ", " + initials.getTextNormalize();
				}
				authorStrList.add(authorStr);
			}
		}

		// convert to String[]
		if (authorStrList.size() > 0) {
			if (completeYN.equals("N")) {
				authorStrList.add("et al.");
			}
			int lc = -1;
			String[] authors = new String[authorStrList.size()];
			for (String author : authorStrList) authors[++lc] = author;
			return authors;
		}
		
		// outta here
		return null;
	}

	/**
	 * Constructs source string
	 *
	 * @param medlineCitationElement Element
	 * @return String
	 * @throws JDOMException
	 */
	private String constructSourceString(Element medlineCitationElement) throws JDOMException {

		String returnString = "";

		String sourceTitle = xpathQuery(medlineCitationElement, "Article/Journal/Title", false);
		String sourceVolume = xpathQuery(medlineCitationElement, "Article/Journal/JournalIssue/Volume", false);
		String sourceIssue = xpathQuery(medlineCitationElement, "Article/Journal/JournalIssue/Issue", false);
		String sourcePages = xpathQuery(medlineCitationElement, "Article/Pagination/MedlinePgn", false);

		returnString += (sourceTitle != null && sourceTitle.length() > 0) ? (sourceTitle + " ") : "";
		returnString += (sourceVolume != null && sourceVolume.length() > 0) ? (sourceVolume) : "";
		returnString += (sourceIssue != null && sourceIssue.length() > 0) ? ("(" + sourceIssue + ")") : "";
		returnString += (sourcePages != null && sourcePages.length() > 0) ? (":" + sourcePages) : "";

		// outta here
		return returnString;
	}

	/**
	 * Checks a reference object for missing resources
	 *
	 * @param reference Reference
	 * @return boolean
	 */
	private boolean completeReference(Reference reference) {

		// check for null ref
		if (reference == null) return false;

		if (reference.getDatabase().equals(CPathRecord.NA_STRING)) return false;
		if (reference.getYear().equals(CPathRecord.NA_STRING)) return false;
		if (reference.getTitle().equals(CPathRecord.NA_STRING)) return false;
		if (reference.getSource().equals(CPathRecord.NA_STRING)) return false;
		String[] authors = reference.getAuthors();
		if (authors.length == 1 && authors[0].equals(CPathRecord.NA_STRING)) return false;

		// outta here
		return true;
	}
}
