// $Id: LoadExternalReferences.java,v 1.14 2006-02-22 22:47:51 grossb Exp $
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
package org.mskcc.pathdb.tool;

import org.mskcc.pathdb.sql.dao.DaoException;
import org.mskcc.pathdb.sql.transfer.MissingDataException;
import org.mskcc.pathdb.task.ImportReferencesTask;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/**
 * Loads External References from text file.
 *
 * @author Ethan Cerami
 */
public class LoadExternalReferences {

    /**
     * Loads External References from Specified File.
     *
     * @param file File.
     * @throws IOException          File Input Error.
     * @throws DaoException         Data Access Error.
     * @throws MissingDataException XML is missing data.
     */
    public void load(File file) throws DaoException, IOException,
            MissingDataException {
        FileReader reader = new FileReader(file);
        ImportReferencesTask task =
                new ImportReferencesTask(true, reader);
        task.importReferences();
    }
}
