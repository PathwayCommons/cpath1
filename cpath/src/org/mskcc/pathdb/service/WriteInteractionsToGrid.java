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
package org.mskcc.pathdb.service;

import org.mskcc.dataservices.bio.AttributeBag;
import org.mskcc.dataservices.bio.Interaction;
import org.mskcc.dataservices.bio.Interactor;
import org.mskcc.dataservices.bio.vocab.InteractionVocab;
import org.mskcc.dataservices.core.DataServiceException;
import org.mskcc.dataservices.live.DataServiceBase;
import org.mskcc.dataservices.live.DataServiceFactory;
import org.mskcc.dataservices.services.WriteInteractions;
import org.mskcc.dataservices.services.WriteInteractors;
import org.mskcc.pathdb.sql.JdbcUtil;
import org.mskcc.pathdb.sql.dao.DaoException;
import org.mskcc.pathdb.sql.dao.DaoInteraction;
import org.mskcc.pathdb.sql.dao.DaoInteractor;
import org.mskcc.pathdb.util.CPathConstants;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Conditionally Saves Interactions to the GRID INTERACTIONS Table.
 * If Interaction already exists within database, no action is taken.
 * Otherwise, new Interaction is saved in database.
 *
 * @author Ethan Cerami
 */
public class WriteInteractionsToGrid extends DataServiceBase
        implements WriteInteractions {
    private Connection con;

    /**
     * Write Interactions to GRID.
     * @param interactions ArrayList of Interaction Objects.
     * @return Number of Interactions Saved.
     * @throws DataServiceException Error Connecting to Data Service.
     */
    public int writeInteractions(ArrayList interactions)
            throws DataServiceException {
        int numSavedInteractions = 0;
        ArrayList interactors = createInteractorSet(interactions);
        int numSavedInteractors = saveInteractors(interactors);

        try {
            DaoInteractor daoInteractor = new DaoInteractor();
            HashMap localIdMap = daoInteractor.getLocalInteractorIds
                    (interactors);
            numSavedInteractions = saveInteractions(interactions, localIdMap);
        } catch (DaoException e) {
            throw new DataServiceException(e);
        }
        return numSavedInteractions;
    }

    /**
     * Gets Server Response.
     * @return Server Response Message.
     */
    public String getServerResponse() {
        return null;
    }

    /**
     * Conditionally Add New Interactors to Database.
     */
    private int saveInteractors(ArrayList interactors)
            throws DataServiceException {
        DataServiceFactory factory = DataServiceFactory.getInstance();
        WriteInteractors service = (WriteInteractors) factory.getService
                (CPathConstants.WRITE_INTERACTORS_TO_GRID);
        int numSaved = service.writeInteractors(interactors);
        return numSaved;
    }

    /**
     * Conditionally Saves all Specified Interactions.
     */
    private int saveInteractions(ArrayList interactions,
            HashMap localIdMap) throws DaoException, DataServiceException {
        DaoInteraction daoInteraction = new DaoInteraction();
        int counter = 0;
        for (int i = 0; i < interactions.size(); i++) {
            Interaction interaction = (Interaction) interactions.get(i);
            boolean exists = daoInteraction.interactionExists(interaction,
                    getLocation());
            if (!exists) {
                counter += saveInteraction(interaction, localIdMap, false);
            }
        }
        return counter;
    }

    /**
     * Saves Individual Interaction.
     * @param interaction Interaction Object.
     * @param isTest Is this a Test Interaction.
     * @return number of new interactions successfully added to database.
     * @throws DaoException Error Retrieving Data.
     */
    private int saveInteraction(Interaction interaction, HashMap localIdMap,
            boolean isTest) throws DaoException {
        ArrayList interactors = interaction.getInteractors();
        Interactor interactor0 = (Interactor) interactors.get(0);
        Interactor interactor1 = (Interactor) interactors.get(1);

        String localId0 = (String) localIdMap.get(interactor0.getName());
        String localId1 = (String) localIdMap.get(interactor1.getName());

        String expSystem = getAttribute(interaction,
                InteractionVocab.EXPERIMENTAL_SYSTEM_NAME);
        String owner = getAttribute(interaction, InteractionVocab.OWNER);
        String direction = (String) interaction.getAttribute(
                InteractionVocab.DIRECTION);
        if (direction == null) {
            direction = "AB";
        }
        String pmidStr = DaoInteraction.getPmids(interaction);

        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getCPathConnection();
            con = JdbcUtil.getGridConnection();
            pstmt = con.prepareStatement
                    ("INSERT INTO interactions (geneA, geneB, "
                    + "experimental_system, owner, pubmed_id, direction, "
                    + "bait_allele, prey_allele, deprecated, status)"
                    + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
            pstmt.setString(1, localId0);
            pstmt.setString(2, localId1);
            pstmt.setString(3, expSystem);
            pstmt.setString(4, owner);
            pstmt.setString(5, pmidStr);
            pstmt.setString(6, direction);
            pstmt.setString(7, "Not Reported");
            pstmt.setString(8, "Not Reported");
            pstmt.setString(9, "F");
            if (isTest) {
                pstmt.setString(10, "JUNIT");
            } else {
                pstmt.setString(10, "NEW");
            }
            int rows = pstmt.executeUpdate();
            return rows;
        } catch (ClassNotFoundException e) {
            throw new DaoException("ClassNotFoundException:  "
                    + e.getMessage());
        } catch (SQLException e) {
            throw new DaoException("SQLException:  " + e.getMessage());
        } finally {
            JdbcUtil.closeAll(con, pstmt, rs);
        }
    }


    /**
     * Sets Attribute.  If none if specified, use the value, "Not Reported."
     */
    private String getAttribute(AttributeBag bag, String key) {
        String value = (String) bag.getAttribute(key);
        if (value == null || value.equals("")) {
            value = "Not Reported";
        }
        return value;
    }

    /**
     * Creates a set of unique interactors.
     */
    private ArrayList createInteractorSet(ArrayList interactions) {
        HashMap map = new HashMap();
        for (int i = 0; i < interactions.size(); i++) {
            Interaction interaction = (Interaction) interactions.get(i);
            ArrayList interactors = interaction.getInteractors();
            for (int j = 0; j < interactors.size(); j++) {
                Interactor interactor = (Interactor) interactors.get(j);
                String name = interactor.getName();
                if (!map.containsKey(name)) {
                    map.put(name, interactor);
                }
            }
        }
        return createArrayList(map);
    }

    /**
     * Creates Array list for Specified HashMap.
     */
    private ArrayList createArrayList(HashMap map) {
        ArrayList interactors = new ArrayList();
        Iterator keys = map.keySet().iterator();
        while (keys.hasNext()) {
            String key = (String) keys.next();
            Interactor interactor = (Interactor) map.get(key);
            interactors.add(interactor);
        }
        return interactors;
    }
}