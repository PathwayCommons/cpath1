package org.mskcc.pathdb.lucene;

import org.apache.lucene.document.Field;
import org.mskcc.dataservices.schemas.psi.*;
import org.mskcc.pathdb.sql.assembly.XmlAssembly;
import org.mskcc.pathdb.util.XmlStripper;
import org.mskcc.pathdb.util.XmlUtil;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Encapsulates a PSI-MI Interaction Record scheduled for indexing in Lucene.
 * <P>
 * Indexes the following fields:
 * <UL>
 * <LI>All terms in the defaul All Field.
 * <LI>All interactor information, including name(s), organism, and external
 * references.
 * <LI>All interactor cPath IDs.
 * <LI>All interaction information, including pmids, database source,
 * and interaction type.
 * <LI>Interaction cPath ID.
 * </UL>
 *
 * @author Ethan Cerami
 */
public class PsiInteractionToIndex implements ItemToIndex {

    /**
     * Lucene Field for Interactor Information.
     */
    public static final String FIELD_INTERACTOR = "interactor";

    /**
     * Lucene Field for Organism Information.
     */
    public static final String FIELD_ORGANISM = "organism";

    /**
     * Lucene Field for Storing Pub Med ID.
     */
    public static final String FIELD_PMID = "pmid";

    /**
     * Lucene Field for Storing Interaction Type Information.
     */
    public static final String FIELD_EXPERIMENT_TYPE =
            "experiment_type";

    /**
     * Lucene Field for Storing Database Name.
     */
    public static final String FIELD_DATABASE = "database";

    /**
     * Internal List of all Fields scheduled for Indexing.
     */
    private ArrayList fields = new ArrayList();

    /**
     * Constructor.
     * Only available within the Lucene package.
     * The only way to construct the object is via the Factory class.
     *
     * @param xmlAssembly XmlAssembly.
     * @throws IOException Input Output Error.
     */
    PsiInteractionToIndex(long cpathId, XmlAssembly xmlAssembly)
            throws IOException {
        EntrySet entrySet = (EntrySet) xmlAssembly.getXmlObject();

        //  Index All Interactors and Interactions.
        for (int i = 0; i < entrySet.getEntryCount(); i++) {
            Entry entry = entrySet.getEntry(i);
            InteractorList interactorList = entry.getInteractorList();
            indexInteractorData(interactorList);
            InteractionList interactionList = entry.getInteractionList();
            indexInteractionData(interactionList);
        }

        //  Index All Terms -->  Default Field.
        String xml = xmlAssembly.getXmlString();
        String terms = XmlStripper.stripTags(xml);
        fields.add(Field.Text(LuceneIndexer.FIELD_ALL, terms));

        //  Index cPath ID
        fields.add(Field.Text(LuceneIndexer.FIELD_INTERACTION_ID,
                Long.toString(cpathId)));
    }

    /**
     * Gets Total Number of Fields to Index.
     *
     * @return total number of fields to index.
     */
    public int getNumFields() {
        return fields.size();
    }

    /**
     * Gets Field at specified index.
     *
     * @param index Index value.
     * @return Lucene Field Object.
     */
    public Field getField(int index) {
        return (Field) fields.get(index);
    }

    /**
     * Indexes All Interactors.
     * This includes all names, xrefs, and organism data.
     *
     * @param interactorList List of Interactors.
     */
    private void indexInteractorData(InteractorList interactorList) {
        StringBuffer interactorIdTokens = new StringBuffer();
        StringBuffer interactorTokens = new StringBuffer();
        StringBuffer organismTokens = new StringBuffer();
        int size = interactorList.getProteinInteractorCount();
        for (int i = 0; i < size; i++) {
            ProteinInteractorType protein =
                    interactorList.getProteinInteractor(i);
            appendNameTokens(protein.getNames(), interactorTokens);
            appendToken(interactorIdTokens, protein.getId());
            appendXrefTokens(protein.getXref(), interactorTokens);
            appendOrganismTokens(protein, organismTokens);
        }
        fields.add(Field.Text(FIELD_INTERACTOR, interactorTokens.toString()));
        fields.add(Field.Text(LuceneIndexer.FIELD_INTERACTOR_ID,
                interactorIdTokens.toString()));
        fields.add(Field.Text(FIELD_ORGANISM, organismTokens.toString()));
    }

    /**
     * Indexes all Interactions.
     * This includes all pmids, interaction detection data, and database source.
     *
     * @param interactionList List of Interactions.
     */
    private void indexInteractionData(InteractionList interactionList) {
        StringBuffer pmidTokens = new StringBuffer();
        StringBuffer interactionTypeTokens = new StringBuffer();
        StringBuffer dbTokens = new StringBuffer();
        for (int i = 0; i < interactionList.getInteractionCount(); i++) {
            InteractionElementType interaction =
                    interactionList.getInteraction(i);
            ExperimentList experimentList = interaction.getExperimentList();
            if (experimentList != null) {
                appendExperimentTokens(experimentList, pmidTokens,
                        interactionTypeTokens);
            }
            appendXrefTokens(interaction.getXref(), dbTokens);
        }
        fields.add(Field.Text(FIELD_PMID, pmidTokens.toString()));
        fields.add(Field.Text(FIELD_EXPERIMENT_TYPE,
                interactionTypeTokens.toString()));
        fields.add(Field.Text(FIELD_DATABASE, dbTokens.toString()));
    }

    /**
     * Appends Experimental Data Tokens.
     */
    private void appendExperimentTokens(ExperimentList experimentList,
            StringBuffer pmidTokens, StringBuffer interactionTypeTokens) {
        for (int i = 0; i < experimentList.getExperimentListItemCount(); i++) {
            ExperimentListItem expItem =
                    experimentList.getExperimentListItem(i);
            if (expItem != null) {
                ExperimentType expType = expItem.getExperimentDescription();
                if (expType != null) {
                    BibrefType bibRef = expType.getBibref();
                    if (bibRef != null) {
                        XrefType xref = bibRef.getXref();
                        appendXrefTokens(xref, pmidTokens);
                    }
                    CvType cvType = expType.getInteractionDetection();
                    appendCvTypeTokens(cvType, interactionTypeTokens);
                }
            }
        }
    }

    /**
     * Appends Organism Tokens.
     */
    private void appendOrganismTokens(ProteinInteractorType protein,
            StringBuffer tokens) {
        Organism organism = protein.getOrganism();
        if (organism != null) {
            int ncbiTaxId = organism.getNcbiTaxId();
            appendToken(tokens, Integer.toString(ncbiTaxId));
            appendNameTokens(organism.getNames(), tokens);
        }
    }

    /**
     * Appends Name Tokens.
     */
    private void appendNameTokens(NamesType names, StringBuffer tokens) {
        if (names != null) {
            String shortName = names.getShortLabel();
            String fullName = names.getFullName();
            appendToken(tokens, XmlUtil.normalizeText(shortName));
            appendToken(tokens, XmlUtil.normalizeText(fullName));
        }
    }

    /**
     * Appends CV Tokens.
     */
    private void appendCvTypeTokens(CvType cvType,
            StringBuffer tokens) {
        if (cvType != null) {
            this.appendNameTokens(cvType.getNames(), tokens);
            this.appendXrefTokens(cvType.getXref(), tokens);
        }
    }

    /**
     * Appends Xref Tokens.
     */
    private void appendXrefTokens(XrefType xref, StringBuffer tokens) {
        if (xref != null) {
            DbReferenceType primaryRef = xref.getPrimaryRef();
            appendDbRefTokens(tokens, primaryRef);
            for (int i = 0; i < xref.getSecondaryRefCount(); i++) {
                DbReferenceType secondaryRef = xref.getSecondaryRef(i);
                appendDbRefTokens(tokens, secondaryRef);
            }
        }
    }

    /**
     * Appends DbRef Tokens.
     */
    private String appendDbRefTokens(StringBuffer tokens,
            DbReferenceType dbRef) {
        if (dbRef != null) {
            appendToken(tokens, dbRef.getDb());
            appendToken(tokens, dbRef.getId());
        }
        return tokens.toString();
    }

    /**
     * Appends New Token to List.
     */
    private void appendToken(StringBuffer tokens, String token) {
        if (token != null && token.length() > 0) {
            tokens.append(token + " ");
        }
    }
}