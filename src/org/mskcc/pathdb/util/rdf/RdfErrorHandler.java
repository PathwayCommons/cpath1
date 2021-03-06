// $Id: RdfErrorHandler.java,v 1.6 2006-06-09 19:22:04 cerami Exp $
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
package org.mskcc.pathdb.util.rdf;

import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import java.util.ArrayList;

/**
 * RDF Error Handler.
 * <p/>
 * Stores all RDF/XML fatal errors, errors, and warnings.
 *
 * @author Ethan Cerami
 */
public class RdfErrorHandler implements ErrorHandler {
    private ArrayList warningList = new ArrayList();
    private ArrayList errorList = new ArrayList();
    private ArrayList fatalErrorList = new ArrayList();

    /**
     * Warning Handler.
     *
     * @param exception SAXParseException Object.
     * @throws SAXException SAXException Object.
     */
    public void warning(SAXParseException exception) throws SAXException {
        warningList.add(exception);
    }

    /**
     * Error Handler.
     *
     * @param exception SAXParseException Object.
     * @throws SAXException SAXException Object.
     */
    public void error(SAXParseException exception) throws SAXException {
        errorList.add(exception);
    }

    /**
     * Fatal Error Handler.
     *
     * @param exception SAXParseException Object.
     * @throws SAXException SAXException Object.
     */
    public void fatalError(SAXParseException exception) throws SAXException {
        fatalErrorList.add(exception);
    }

    /**
     * Gets List of Warnings.
     *
     * @return ArrayList of SAXParseException Objects.
     */
    public ArrayList getWarningList() {
        return warningList;
    }

    /**
     * Gets List of Errors.
     *
     * @return ArrayList of SAXParseException Objects.
     */
    public ArrayList getErrorList() {
        return errorList;
    }

    /**
     * Gets List of Fatal Errors.
     *
     * @return ArrayList of SAXParseException Objects.
     */
    public ArrayList getFatalErrorList() {
        return fatalErrorList;
    }
}
