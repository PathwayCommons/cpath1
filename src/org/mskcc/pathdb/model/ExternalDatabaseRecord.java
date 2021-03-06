// $Id: ExternalDatabaseRecord.java,v 1.22 2007-01-10 15:22:04 cerami Exp $
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
package org.mskcc.pathdb.model;

import org.apache.log4j.Logger;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;

/**
 * JavaBean to Encapsulate an External Database Record.
 * <p/>
 * PSI-MI and BioPax do not yet have controlled vocabularies for external
 * references.  For example, one PSI file might include a reference to "SWP",
 * and another file might include a reference to "SWISS-PROT".  To accommodote
 * a varying list of terms, each database record can be associated with multiple
 * controlled vocabulary terms.  The full list of terms is available via
 * the getSynonymTerms() method.
 *
 * @author Ethan Cerami
 */
public class ExternalDatabaseRecord implements Serializable {
    private int id;
    private String name;
    private String description;
    private ArrayList synTerms;
    private String masterTerm;
    private String urlPattern;
    private String homePageUrl;
    private String sampleId;
    private String pathGuideId;
    private String iconPath;
    private String iconFileExtension;
    private ReferenceType dbType;
    private Date createTime;
    private Date updateTime;

    /**
     * Gets the Database ID.
     *
     * @return External Database ID.
     */
    public int getId() {
        return id;
    }

    /**
     * Sets the Database ID.
     *
     * @param id External Database ID.
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Gets Database Name.
     *
     * @return Database Name.
     */
    public String getName() {
        return name;
    }

    /**
     * Sets Database Name.
     *
     * @param name Databse Name.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets Database Description.
     *
     * @return Database Description.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets Database Description.
     *
     * @param description Database Description.
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Gets List of Controlled Vocabulary Terms which match this database.
     *
     * @return ArrayList of String terms.
     */
    public ArrayList getSynonymTerms() {
        return this.synTerms;
    }

    /**
     * Sets List of Controlled Vocabulary Terms which match this database.
     *
     * @param terms ArrayList of String terms.
     */
    public void setSynonymTerms(ArrayList terms) {
        this.synTerms = terms;
    }

    /**
     * Gets URL for Retrieving individual record from the database.
     *
     * @return URL String.
     */
    public String getUrlPattern() {
        return urlPattern;
    }

    /**
     * Gets URL for Retrieving a specific individual record from the database.
     *
     * @param primaryId Primary ID.
     * @return URL String.
     */
    public String getUrlWithId(String primaryId) {
        String tempUrl;
        if (primaryId != null) {
            //  Hard-Coded Fix for HPRD Ids.
            primaryId = primaryId.replaceAll("HPRD_", "");
            if (urlPattern != null && urlPattern.trim().length() > 0) {
				tempUrl = cookURLPattern(urlPattern, primaryId);
                return tempUrl.replaceAll("%ID%", primaryId);
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    /**
     * Sets URL for Retrieving individual record from the database.
     *
     * @param url URL String.
     */
    public void setUrlPattern(String url) {
        this.urlPattern = url;
    }

    /**
     * Gets the home page URL.
     * @return home page URL.
     */
    public String getHomePageUrl() {
        return homePageUrl;
    }

    /**
     * Sets the home page URL.
     * @param homePageUrl home page URL.
     */
    public void setHomePageUrl(String homePageUrl) {
        this.homePageUrl = homePageUrl;
    }

    /**
     * Gets the Path Guide ID.
     *
     * <P>For details, see pathguide.org
     *
     * @return page guide ID.
     */
    public String getPathGuideId() {
        return pathGuideId;
    }

    /**
     * Sets the Path Guide ID.
     *
     * <P>For details, see pathguide.org
     *
     * @param pathGuideId Path Guide ID.
     */
    public void setPathGuideId(String pathGuideId) {
        this.pathGuideId = pathGuideId;
    }

    /**
     * Gets a Sample ID, used to generate sample live links.
     *
     * @return SampleId String.
     */
    public String getSampleId() {
        return sampleId;
    }

    /**
     * Sets a Sample ID, used to generate sample live links.
     *
     * @param sampleId Sample ID String.
     */
    public void setSampleId(String sampleId) {
        this.sampleId = sampleId;
    }

    /**
     * Gets the TimeStamp when this record was created.
     *
     * @return Time Created.
     */
    public Date getCreateTime() {
        return createTime;
    }

    /**
     * Sets the TimeStamp when this record was created.
     *
     * @param createTime Date Object.
     */
    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    /**
     * Gets the TimeStamp when this record was updated.
     *
     * @return Time Updated.
     */
    public Date getUpdateTime() {
        return updateTime;
    }

    /**
     * Sets the TimeStamp when this records was updated.
     *
     * @param updateTime Date Object.
     */
    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    /**
     * Gets the master (or normalized) CV Term.
     *
     * @return CV Term.
     */
    public String getMasterTerm() {
        return masterTerm;
    }

    /**
     * Sets the fixed (or normalized) CV Term.
     *
     * @param fixedCvTerm CV Term String.
     */
    public void setMasterTerm(String fixedCvTerm) {
        this.masterTerm = fixedCvTerm;
    }

    /**
     * Gets the External Reference Type
     *
     * @return ReferenceType Object.
     */
    public ReferenceType getDbType() {
        return dbType;
    }

    /**
     * Sets the External Reference Type Object.
     *
     * @param refType ReferenceType Object.
     */
    public void setDbType(ReferenceType refType) {
        this.dbType = refType;
    }

    /**
     * Gets the path to an external icon file.
     * @return path to an external icon file.
     */
    public String getIconPath() {
        return iconPath;
    }

    /**
     * Sets the path to an external icon file.
     * @param iconPath path to an external icon file.
     */
    public void setIconPath(String iconPath) {
        this.iconPath = iconPath;
    }

    /**
     * Gets file extension associated with icon.
     * @return file extension associated with icon.
     */
    public String getIconFileExtension() {
        return iconFileExtension;
    }

    /**
     * Sets file extension associated with icon.
     * @param iconFileExtension file extension associated with icon.
     */
    public void setIconFileExtension(String iconFileExtension) {
        this.iconFileExtension = iconFileExtension;
    }

	/**
	 * Method to support multiple URL patterns to an external datasource.
	 *
	 * Currently, (12/13) only Reactome has multiple URL patterns (see cookReactome).
	 *
	 * @param urlPattern String
     * @param primaryId String
	 * @return String
	 */
	private String cookURLPattern(String urlPattern, String primaryId) {

		return (name.equalsIgnoreCase("Reactome")) ?
			cookReactome(urlPattern, primaryId) :
			urlPattern;
	}

	/**
	 * Reactome specific helper method to cookURLPattern.
	 *
	 * The following URLs are supported by Reactome:
	 *
	 * 1) http://www.reactome.org/cgi-bin/eventbrowser?DB=gk_current&amp;ID=
	 * 2) http://www.reactome.org/cgi-bin/eventbrowser_st_id?ST_ID=
	 *
	 * By default, Reactome URL #1 is supported.  Fortunately, Reactome records
	 * that link to #2 are easily identifiable: REACT_XXXX, where XXXX is an arbitrary
	 * string of chars.  We use this fact to replace URL #1 with URL #2 if necessary.
	 *
     * @param urlPattern String
	 * @param primaryId String
	 * @return String
	 */
	private String cookReactome(String urlPattern, String primaryId) {

		return (primaryId.startsWith("REACT_")) ?
			"http://reactome.org/cgi-bin/eventbrowser_st_id?ST_ID=%ID%" :
			urlPattern;
	}
}
