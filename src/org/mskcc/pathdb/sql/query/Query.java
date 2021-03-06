// $Id: Query.java,v 1.18 2006-02-22 22:47:51 grossb Exp $
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
package org.mskcc.pathdb.sql.query;

import org.mskcc.pathdb.sql.assembly.XmlAssembly;
import org.mskcc.pathdb.xdebug.XDebug;

/**
 * Abstract Base Class for all queries.
 *
 * @author Ethan Cerami
 */
abstract class Query {
    protected XDebug xdebug;

    /**
     * Executes Query.
     *
     * @param xdebug XDebug Object.
     * @return XmlAssembly XML Assembly Object.
     * @throws QueryException Error Executing Query.
     */
    public XmlAssembly execute(XDebug xdebug) throws QueryException {
        this.xdebug = xdebug;
        xdebug.logMsg(this, "Executing Query Type:  "
                + getClass().getName());
        try {
            return executeSub();
        } catch (Exception e) {
            throw new QueryException(e.getMessage(), e);
        }
    }

    /**
     * Must be subclassed.
     *
     * @return XmlAssembly XML Assembly Object.
     * @throws Exception All Exceptions.
     */
    protected abstract XmlAssembly executeSub() throws Exception;
}
