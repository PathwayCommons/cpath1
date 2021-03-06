// $Id: DaoInternalFamily.java,v 1.18 2010-10-08 16:21:50 grossben Exp $
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
package org.mskcc.pathdb.sql.dao;

import org.mskcc.pathdb.sql.JdbcUtil;
import org.mskcc.pathdb.model.CPathRecordType;
import org.mskcc.pathdb.model.ExternalDatabaseRecord;
import org.mskcc.pathdb.model.ExternalDatabaseSnapshotRecord;
import org.mskcc.pathdb.model.GlobalFilterSettings;
import org.mskcc.pathdb.util.CPathConstants;
import org.mskcc.pathdb.taglib.BioPaxShowFlag;
import org.mskcc.pathdb.schemas.biopax.summary.BioPaxRecordSummary;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Set;
import java.util.Date;
import java.util.ArrayList;

/**
 * Data Access Object to the internal family table.
 *
 * @author Ethan Cerami.
 */
public class DaoInternalFamily {
    private static final String INSERT_SQL =
        "INSERT INTO internal_family (`ANCESTOR_ID`, `ANCESTOR_NAME`, "
		+ "`ANCESTOR_TYPE`, `ANCESTOR_SPECIES_ID`, `ANCESTOR_SPECIES_NAME`, "
		+ "`ANCESTOR_EXTERNAL_DB_SNAPSHOT_ID`, `ANCESTOR_EXTERNAL_DB_NAME`, "
		+ "`ANCESTOR_EXTERNAL_DB_SNAPSHOT_DATE`, `ANCESTOR_EXTERNAL_DB_SNAPSHOT_VERSION`, "
        + "`DESCENDENT_ID`, `DESCENDENT_NAME`, `DESCENDENT_TYPE`) VALUES (?,?,?,?,?,?,?,?,?,?,?,?)";

    /**
     * Adds a new record.
     * @param ancestorId         ID of Ancestor record.
     * @param ancestorName       Name of Ancestor record.
     * @param ancestorType       Record type of ancestor.
	 * @param ancestorSnapshotRecord ExternalDatabaseSnapshotRecord.
	 * @param ancestorOrganismId Ancestor organism (species) Id.
	 * @param ancestorOrganism   Ancestor organism (species) name.
     * @param descendentId       ID of Descendent record.
     * @param descendentName     Name of Descendent record.
     * @param descendentType     Record type of Descendent.
     * @throws DaoException      Database access error.
     */
    public void addRecord (long ancestorId, String ancestorName, CPathRecordType ancestorType,
						   ExternalDatabaseSnapshotRecord ancestorSnapshotRecord,
						   long ancestorOrganismId, String ancestorOrganism,
						   long descendentId, String descendentName, CPathRecordType descendentType)
		throws DaoException {

        Connection con = null;
        ResultSet rs = null;
        PreparedStatement pstmt = null;
        try {
            con = JdbcUtil.getCPathConnection();
            pstmt = con.prepareStatement (INSERT_SQL);
            pstmt.setLong(1, ancestorId);
            pstmt.setString(2, ancestorName);
            pstmt.setString(3, ancestorType.toString());
            pstmt.setLong(4, ancestorOrganismId);
            pstmt.setString(5, ancestorOrganism);
            if (ancestorSnapshotRecord == null) {
                pstmt.setLong(6, -1);
                pstmt.setString(7, "N/A");
                java.sql.Date date = new java.sql.Date(new Date().getTime());
                pstmt.setDate(8, date);
                pstmt.setString(9, "N/A");
            } else {
                pstmt.setLong(6, ancestorSnapshotRecord.getId());
                pstmt.setString(7, ancestorSnapshotRecord.getExternalDatabase().getName());
                Date snapshotDate = ancestorSnapshotRecord.getSnapshotDate();
                java.sql.Date date = new java.sql.Date(snapshotDate.getTime());
                pstmt.setDate(8, date);
                pstmt.setString(9, ancestorSnapshotRecord.getSnapshotVersion());
            }
            pstmt.setLong(10, descendentId);
            pstmt.setString(11, descendentName);
            pstmt.setString(12, descendentType.toString());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(con, pstmt, rs);
        }
    }

    /**
     * Adds a new record.
     * @param ancestorId        ID of Ancestor record.
     * @param ancestorType      Record type of ancestor.
	 * @param ancestorSnapshotRecord ExternalDatabaseSnapshotRecord.
	 * @param ancestorOrganismId Ancestor organism (species) Id.
	 * @param ancestorOrganism  Ancestor organism (species) name.
     * @param descendentIds     IDs of Descendent record.
     * @param descendentTypes   Record types of Descendent.
     * @throws DaoException     Database access error.
     */
    public void addRecords (long ancestorId, String ancestorName, CPathRecordType ancestorType,
							ExternalDatabaseSnapshotRecord ancestorSnapshotRecord,
							long ancestorOrganismId, String ancestorOrganism,
							ArrayList descendentIds, ArrayList descendentNames, ArrayList descendentTypes)
		throws DaoException {

		if (CPathConstants.CPATH_DO_ASSERT) {
			assert ( descendentIds.size() == descendentNames.size() &&
					 descendentNames.size() == descendentTypes.size()) :
			"DaoInternalFamily: descendentIds[].size() != descendentNames[].size() != descendentTypes[].size()";
		}

        Connection con = null;
        ResultSet rs = null;
        PreparedStatement pstmt = null;

        //  Use a Batch statement:  results in faster performance.  For details, see:
        //  http://media.datadirect.com/download/docs/jdbc/jdbcref/jdbcdesign.html
        try {
            con = JdbcUtil.getCPathConnection();
            pstmt = con.prepareStatement (INSERT_SQL);
            for (int i=0; i <descendentIds.size(); i++) {
                pstmt.setLong(1, ancestorId);
				pstmt.setString(2, ancestorName);
                pstmt.setString(3, ancestorType.toString());
				pstmt.setLong(4, ancestorOrganismId);
				pstmt.setString(5, ancestorOrganism);
                pstmt.setLong(6, ancestorSnapshotRecord.getId());
				pstmt.setString(7, ancestorSnapshotRecord.getExternalDatabase().getName());
				Date snapshotDate = ancestorSnapshotRecord.getSnapshotDate();
				java.sql.Date date = new java.sql.Date(snapshotDate.getTime());
				pstmt.setDate(8, date);
				pstmt.setString(9, ancestorSnapshotRecord.getSnapshotVersion());
                pstmt.setLong(10, (Long) descendentIds.get(i));
                pstmt.setString(11, (String) descendentNames.get(i));
                pstmt.setString(12, (String) descendentTypes.get(i));
                pstmt.addBatch();
            }
            pstmt.executeBatch();
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(con, pstmt, rs);
        }
    }

    /**
    public lon
     * Gets all descendents of this ancestor.
     * @param ancestorId        ID of ancestor.
     * @return array of all descendent IDs.
     * @throws DaoException     Database access error.
     */
    public long[] getDescendentIds (long ancestorId)
        throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        ArrayList list = new ArrayList();
        try {
            con = JdbcUtil.getCPathConnection();
            pstmt = con.prepareStatement
                    ("select DESCENDENT_ID from internal_family where "
                            + "ANCESTOR_ID = ?");
            pstmt.setLong(1, ancestorId);
            return getDescendentIds(pstmt, rs, list);
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(con, pstmt, rs);
        }
    }

    /**
     * Gets all descendents of this ancestor, which are of type:
     * CPathRecordType.
     * @param ancestorId        ID of ancestor.
     * @param descendentType              CPathRecord Type of descendent.
     * @return array of all descendent IDs.
     * @throws DaoException     Database access error.
     */
    public long[] getDescendentIds (long ancestorId, CPathRecordType
            descendentType) throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        ArrayList list = new ArrayList();
        try {
            con = JdbcUtil.getCPathConnection();
            pstmt = con.prepareStatement
                    ("select * from internal_family where "
                            + "ANCESTOR_ID = ? AND DESCENDENT_TYPE = ?");
            pstmt.setLong(1, ancestorId);
            pstmt.setString(2, descendentType.toString());
            return getDescendentIds(pstmt, rs, list);
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(con, pstmt, rs);
        }
    }

    /**
     * Gets the number of ancestor records of this descendent, which are of type:
     * CPathRecordType.
     * @param descendentId ID of descendent.
     * @param ancestorType CPathRecord Type of ancestor.
	 * @param snapshotIdSet Set<Long> - used to filter result set based on db sources
	 * @param organismIdSet Set<Integer> used to filter result set based on orgainism id
     * @return long number of ancestor records
     * @throws DaoException Database access error.
     */
    public Integer getAncestorIdCount (long descendentId, CPathRecordType ancestorType,
										 Set<Long> snapshotIdSet, Set<Integer> organismIdSet) throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            con = JdbcUtil.getCPathConnection();

			// datasource filter
			String dataSourceFilter = getDataSourceFilterString(snapshotIdSet);
			// organism filter
			String organismFilter = getOrganismFilterString(organismIdSet);
			// prepare the statement
            pstmt = con.prepareStatement 
                    ("select count(*) from internal_family where " +
                     "DESCENDENT_ID = ? AND ANCESTOR_TYPE = ?" + dataSourceFilter + organismFilter);
            pstmt.setLong(1, descendentId);
            pstmt.setString(2, ancestorType.toString());
			int lc = 3;
			for (Long snapshotId : snapshotIdSet) {
				pstmt.setLong(lc++, snapshotId);
			}
			if (organismFilter.length() > 0) {
				for (Integer organismId : organismIdSet) {
					pstmt.setInt(lc++, organismId);
				}
			}
			// excute the query
			rs = pstmt.executeQuery();
			rs.next();
			return rs.getInt(1);
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(con, pstmt, rs);
        }
    }

    /**
     * Gets the number of descendent records of this ancestor, which are of type:
     * CPathRecordType.
     * @param ancestorId ID of ancestor.
     * @param descendentType CPathRecord Type of descendent.
     * @return long number of descendent records
     * @throws DaoException Database access error.
     */
    public Integer getDescendentIdCount (long ancestorId, CPathRecordType descendentType) throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getCPathConnection();
            pstmt = con.prepareStatement
                    ("select count(*) from internal_family where "
                            + "ANCESTOR_ID = ? AND DESCENDENT_TYPE = ?");
            pstmt.setLong(1, ancestorId);
            pstmt.setString(2, descendentType.toString());
			rs = pstmt.executeQuery();
			rs.next();
			return rs.getInt(1);
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(con, pstmt, rs);
        }
    }

	/**
	 * Gets all ancestort summary records of this descendent,
	 * which are of type CPathRecordType
	 *
     * @param descendentId ID of descendent.
     * @param ancestorType CPathRecord Type of ancestor.
	 * @param summarySet Set<BioPaxRecordSummary> - this gets populate by reference
	 * @param snapshotIdSet Set<Long> - used to filter result set based on db sources
	 * @param organismIdSet Set<Integer> used to filter result set based on orgainism id
	 * @param offset integer - arg 1 to sql select, limit attribute (starts at 0)
	 * @param rowCount integer - arg 2 to sql select, limit attribute
	 * @return Integer - total number of molecules in db..required to render "show 1 - 20 of XXX" headers
     * @throws DaoException Database access error.
	 */
    public Integer getAncestorSummaries (long descendentId,
										 CPathRecordType ancestorType,											
										 Set<BioPaxRecordSummary> summarySet,
										 Set<Long> snapshotIdSet,
										 Set<Integer> organismIdSet,
										 int offset, int rowCount) throws DaoException {

		int lc;
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
			// perform the query
            con = JdbcUtil.getCPathConnection();
			// datasource filter
			String dataSourceFilter = getDataSourceFilterString(snapshotIdSet);
			// organism filter
			String organismFilter = getOrganismFilterString(organismIdSet);
			// construct query
			String query = 
				"select ANCESTOR_ID, ANCESTOR_NAME, ANCESTOR_SPECIES_NAME, ANCESTOR_EXTERNAL_DB_SNAPSHOT_ID, " + 
				"ANCESTOR_EXTERNAL_DB_NAME, ANCESTOR_EXTERNAL_DB_SNAPSHOT_DATE, ANCESTOR_EXTERNAL_DB_SNAPSHOT_VERSION " +
				"from internal_family where DESCENDENT_ID = ? AND ANCESTOR_TYPE = ? " + dataSourceFilter + organismFilter +
				" ORDER BY ANCESTOR_NAME LIMIT " + offset + ", " + rowCount;
			// prepare the statement
            pstmt = con.prepareStatement(query);
            pstmt.setLong(1, descendentId);
            pstmt.setString(2, ancestorType.toString());
			lc = 3;
			for (Long snapshotId : snapshotIdSet) {
				pstmt.setLong(lc++, snapshotId);
			}
			if (organismFilter.length() > 0) {
				for (Integer organismId : organismIdSet) {
					pstmt.setInt(lc++, organismId);
				}
			}
			// excute the query
            getAncestorSummaries(pstmt, rs, summarySet);
			return getAncestorIdCount(descendentId, ancestorType, snapshotIdSet, organismIdSet);
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(con, pstmt, rs);
        }
    }

	/**
	 * Gets all descendent summary records of this ancestor,
	 * which are of type CPathRecordType
	 *
     * @param ancestorId ID of ancestor.
     * @param descendentType CPathRecord Type of descendent.
	 * @param summarySet Set<BioPaxRecordSummary> - this gets populate by reference
	 * @param offset integer - arg 1 to sql select, limit attribute (starts at 0)
	 * @param rowCount integer - arg 2 to sql select, limit attribute
	 * @return Integer - total number of molecules in db..required to render "show 1 - 20 of XXX" headers
     * @throws DaoException Database access error.
	 */
    public Integer getDescendentSummaries (long ancestorId,
										   CPathRecordType descendentType,											
										   Set<BioPaxRecordSummary> summarySet,
										   int offset, int rowCount) throws DaoException {

        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
			// perform the query
            con = JdbcUtil.getCPathConnection();
			// get count
			String query =
				"select `DESCENDENT_ID`, `DESCENDENT_NAME` from internal_family " +
				"where ANCESTOR_ID = ? AND DESCENDENT_TYPE = ? ORDER BY DESCENDENT_NAME LIMIT " +
				offset + ", " + rowCount;
            pstmt = con.prepareStatement(query);
            pstmt.setLong(1, ancestorId);
            pstmt.setString(2, descendentType.toString());
            getDescendentSummaries(pstmt, rs, summarySet);
			return getDescendentIdCount(ancestorId, descendentType);
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(con, pstmt, rs);
        }
    }

    /**
     * Gets all ancestors of this record, which are of type:
     * CPathRecordType.
     * @param cPathId        ID of record.
     * @param ancestorType   CPathRecord Type of ancestor.
     * @return array of all ancestor IDs.
     * @throws DaoException     Database access error.
     */
    public long[] getAncestorIds (long cPathId, CPathRecordType ancestorType) throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        ArrayList list = new ArrayList();
        try {
            con = JdbcUtil.getCPathConnection();
            pstmt = con.prepareStatement
                    ("select * from internal_family where "
                            + "DESCENDENT_ID = ? AND ANCESTOR_TYPE = ?");
            pstmt.setLong(1, cPathId);
            pstmt.setString(2, ancestorType.toString());
            return getAncestorIds(pstmt, rs, list);
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(con, pstmt, rs);
        }
    }

    /**
     * Deletes all existing rrecords.
     *
     * @throws DaoException Error Accessing Database.
     */
    public void deleteAllRecords() throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getCPathConnection();
            pstmt = con.prepareStatement("TRUNCATE TABLE internal_family");
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(con, pstmt, rs);
        }
    }

    private long[] getDescendentIds(PreparedStatement pstmt, ResultSet rs, ArrayList list)
            throws SQLException {
        rs = pstmt.executeQuery();
        while (rs.next()) {
            long descendentId = rs.getLong("DESCENDENT_ID");
            list.add(new Long(descendentId));
        }
        long ids[] = new long[list.size()];
        for (int i = 0; i < list.size(); i++) {
            Long idLong = (Long) list.get(i);
            ids[i] = idLong.longValue();
        }
        return ids;
    }

    private void getAncestorSummaries(PreparedStatement pstmt,
									  ResultSet rs,
									  Set<BioPaxRecordSummary> summarySet) throws SQLException {

		// execute the query 
        rs = pstmt.executeQuery();
        while (rs.next()) {
			BioPaxRecordSummary summary = new BioPaxRecordSummary();
			summary.setRecordID(rs.getLong(1));
			summary.setName(rs.getString(2));
			summary.setOrganism(rs.getString(3));
			Long snapshotId = rs.getLong(4);
			ExternalDatabaseRecord databaseRecord = new ExternalDatabaseRecord();
			databaseRecord.setName(rs.getString(5));
			java.sql.Date date = rs.getDate(6);
			String snapshotVersion = rs.getString(7);
			ExternalDatabaseSnapshotRecord snapshotRecord =
				new ExternalDatabaseSnapshotRecord
				(databaseRecord, new Date(date.getTime()), snapshotVersion, 0, 0, 0);
			snapshotRecord.setId(snapshotId);
			summary.setExternalDatabaseSnapshotRecord(snapshotRecord);
			summarySet.add(summary);
        }
    }

    private void getDescendentSummaries(PreparedStatement pstmt,
										ResultSet rs,
										Set<BioPaxRecordSummary> summarySet) throws SQLException {

		// execute the query 
        rs = pstmt.executeQuery();
        while (rs.next()) {
			BioPaxRecordSummary summary = new BioPaxRecordSummary();
			summary.setRecordID(rs.getLong(1));
			summary.setName(rs.getString(2));
			summarySet.add(summary);
        }
    }

    private long[] getAncestorIds(PreparedStatement pstmt, ResultSet rs, ArrayList list)
            throws SQLException {
        rs = pstmt.executeQuery();
        while (rs.next()) {
            long descendentId = rs.getLong("ANCESTOR_ID");
            list.add(new Long(descendentId));
        }
        long ids[] = new long[list.size()];
        for (int i = 0; i < list.size(); i++) {
            Long idLong = (Long) list.get(i);
            ids[i] = idLong.longValue();
        }
        return ids;
    }

	private String getDataSourceFilterString(Set<Long> snapshotIdSet) {

		StringBuffer dataSourceFilterBuf = new StringBuffer();
		int snapshotIdSetSize = snapshotIdSet.size();
		for (int lc = 0; lc < snapshotIdSetSize; lc++) {
			String query = ((lc == 0) ? "AND (" : "OR") + " ANCESTOR_EXTERNAL_DB_SNAPSHOT_ID = ? ";
			dataSourceFilterBuf.append(query);
		}
		if (snapshotIdSetSize > 0) dataSourceFilterBuf.append(") ");

		// outta here
		return dataSourceFilterBuf.toString();
	}

	private String getOrganismFilterString(Set<Integer> organismIdSet) {

		StringBuffer organismFilterBuf = new StringBuffer("");
		int organismIdSetSize = organismIdSet.size();
		boolean createOrganismFilter = (organismIdSetSize > 0);

		int lc = -1;
		for (Integer i : organismIdSet) {
			if (i == GlobalFilterSettings.ALL_ORGANISMS_FILTER_VALUE) {
				createOrganismFilter = false;
				break;
			}
			String query = ((++lc == 0) ? "AND (" : "OR") + " ANCESTOR_SPECIES_ID = ? ";
			organismFilterBuf.append(query);
		}
		if (createOrganismFilter) organismFilterBuf.append(")");

		// outta here
		return organismFilterBuf.toString();
	}
}
