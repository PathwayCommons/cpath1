package org.mskcc.pathdb.sql.query;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.mskcc.dataservices.core.EmptySetException;
import org.mskcc.dataservices.mapper.MapPsiToInteractions;
import org.mskcc.dataservices.mapper.MapperException;
import org.mskcc.dataservices.schemas.psi.EntrySet;
import org.mskcc.pathdb.model.CPathRecord;
import org.mskcc.pathdb.sql.dao.DaoCPath;
import org.mskcc.pathdb.sql.dao.DaoException;
import org.mskcc.pathdb.sql.dao.DaoInternalLink;
import org.mskcc.pathdb.util.PsiBuilder;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Performs an Interaction Query.
 * Description of Query:  Retrieve all interactions for "YER006W".
 *
 * @author Ethan Cerami
 */
public class InteractionQuery {
    private EntrySet entrySet;
    private ArrayList interactions;
    private String xml;

    /**
     * Constructor.
     * @param interactorName Unique Interactor Name.
     * @throws QueryException Error Performing Query.
     * @throws EmptySetException No Results Found.
     */
    public InteractionQuery(String interactorName) throws QueryException,
            EmptySetException {
        interactions = new ArrayList();
        try {
            DaoCPath cpath = new DaoCPath();
            CPathRecord record = cpath.getRecordByName(interactorName);
            if (record != null) {
                entrySet = aggregateXml(record);
                mapToInteractions();
                xml = this.generateXml();
            } else {
                throw new EmptySetException();
            }
        } catch (DaoException e) {
            throw new QueryException("DaoException:  " + e.getMessage());
        } catch (ValidationException e) {
            throw new QueryException("ValidationException:  "
                    + e.getMessage());
        } catch (MarshalException e) {
            throw new QueryException("MarshalException:  " + e.getMessage());
        } catch (MapperException e) {
            throw new QueryException("MapperException:  " + e.getMessage());
        }
    }

    /**
     * Gets the EntrySet Object or Results.
     * @return PSI-MI Entry Set Castor Object.
     */
    public EntrySet getEntrySet() {
        return entrySet;
    }

    /**
     * Gets the ArrayList of Interaction Results.
     * @return ArrayList of Interaction Objects.
     */
    public ArrayList getInteractions() {
        return interactions;
    }

    /**
     * Gets XML Response String.
     * @return XML Response String.
     */
    public String getXml() {
        return this.xml;
    }

    /**
     * Generates XML from Entry Set Object.
     */
    private String generateXml() throws ValidationException, MarshalException {
        StringWriter writer = new StringWriter();
        entrySet.marshal(writer);
        String xml = writer.toString();
        return xml;
    }

    /**
     * Maps PSI to Data Service Interaction objects.
     */
    private void mapToInteractions() throws MarshalException,
            ValidationException, MapperException {
        StringWriter writer = new StringWriter();
        entrySet.marshal(writer);
        String xml = writer.toString();
        interactions = new ArrayList();
        MapPsiToInteractions mapper = new MapPsiToInteractions(xml,
                interactions);
        mapper.doMapping();
    }

    /**
     * Generates PSI XML.
     */
    private EntrySet aggregateXml(CPathRecord record)
            throws DaoException, MarshalException, ValidationException {
        HashMap interactorMap = new HashMap();
        DaoInternalLink linker = new DaoInternalLink();
        PsiBuilder psiBuilder = new PsiBuilder();
        long id = record.getId();
        ArrayList interactions = linker.getInternalLinksWithLookup(id);
        for (int i = 0; i < interactions.size(); i++) {
            CPathRecord intxRecord = (CPathRecord) interactions.get(i);
            long intxId = intxRecord.getId();
            ArrayList interactors =
                    linker.getInternalLinksWithLookup(intxId);
            addInteractorsToMap(interactorMap, interactors);
        }
        EntrySet entrySet = psiBuilder.generatePsi(interactorMap.values(),
                interactions);
        return entrySet;
    }

    /**
     * Adds List of Interactors to Non-redundant Interactor Map.
     */
    private void addInteractorsToMap(HashMap interactorMap, ArrayList
            interactors) {
        for (int i = 0; i < interactors.size(); i++) {
            CPathRecord record = (CPathRecord) interactors.get(i);
            long id = record.getId();
            interactorMap.put(Long.toString(id), record);
        }
    }
}