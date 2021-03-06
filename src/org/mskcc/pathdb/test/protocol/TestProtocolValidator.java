// $Id: TestProtocolValidator.java,v 1.21 2007-06-06 18:13:16 cerami Exp $
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
package org.mskcc.pathdb.test.protocol;

import junit.framework.TestCase;
import org.mskcc.pathdb.protocol.*;
import org.mskcc.pathdb.servlet.CPathUIConfig;

import java.util.HashMap;

/**
 * Tests the Protocol Validator.
 *
 * @author Ethan Cerami
 */
public class TestProtocolValidator extends TestCase {
    private String testName;

    /**
     * Tests the Protocol Validator, PSI-MI Mode
     *
     * @throws Exception General Error.
     */
    public void testProtocolValidatorPsiMi() throws Exception {
        testName = "Test PSI_MI Mode";

        //  Set to PSI-MI Mode
        CPathUIConfig.setWebMode(CPathUIConfig.WEB_MODE_PSI_MI);
        HashMap map = new HashMap();

        //  Try ProtocolConstants.COMMAND_GET_BY_INTERACTOR_NAME_XREF
        //  without any other parameters.  This should result in a protocol
        //  error.
        map.put(ProtocolRequest.ARG_COMMAND,
                ProtocolConstantsVersion1.COMMAND_GET_BY_INTERACTOR_NAME_XREF);
        ProtocolRequest request = new ProtocolRequest(map);
        ProtocolValidator validator = new ProtocolValidator(request);
        try {
            validator.validate(ProtocolConstantsVersion1.VERSION_1);
            fail("ProtocolException should have been thrown");
        } catch (ProtocolException e) {
            ProtocolStatusCode statusCode = e.getStatusCode();
            assertEquals(ProtocolStatusCode.MISSING_ARGUMENTS, statusCode);
        }

        //  Try getting all pathways;  this is invalid in PSI-MI Mode
        map.put(ProtocolRequest.ARG_COMMAND,
                ProtocolConstantsVersion1.COMMAND_GET_TOP_LEVEL_PATHWAY_LIST);
        request = new ProtocolRequest(map);
        validator = new ProtocolValidator(request);
        try {
            validator.validate(ProtocolConstantsVersion1.VERSION_1);
            fail("ProtocolException should have been thrown");
        } catch (ProtocolException e) {
            ProtocolStatusCode statusCode = e.getStatusCode();
            assertEquals(ProtocolStatusCode.BAD_COMMAND, statusCode);
        }
    }

    /**
     * Tests the Protocol Validator, BioPAX Mode
     *
     * @throws Exception General Error.
     */
    public void testProtocolValidatorBioPax() throws Exception {
        testName = "Test BioPAX Mode";

        //  Set to BioPAX Mode
        CPathUIConfig.setWebMode(CPathUIConfig.WEB_MODE_BIOPAX);

        //  Validate GET_BY_CPATH_ID Command;  this should pass
        //  w/o any validation errors.
        HashMap map = new HashMap();
        map.put(ProtocolRequest.ARG_COMMAND,
                ProtocolConstants.COMMAND_GET_RECORD_BY_CPATH_ID);
        map.put(ProtocolRequest.ARG_VERSION, ProtocolConstantsVersion1.VERSION_1);
        map.put(ProtocolRequest.ARG_QUERY, "1235");
        map.put(ProtocolRequest.ARG_FORMAT, ProtocolConstantsVersion1.FORMAT_BIO_PAX);
        ProtocolRequest request = new ProtocolRequest(map);
        ProtocolValidator validator = new ProtocolValidator(request);
        try {
            validator.validate(ProtocolConstantsVersion1.VERSION_1);
        } catch (ProtocolException e) {
            e.printStackTrace();
            fail("ProtocolException should not have been thrown");
        }

        //  Try the same query as above, except this time, set q to
        //  a non-number.  This should result in a validation error.
        map.put(ProtocolRequest.ARG_QUERY, "ABCD");
        request = new ProtocolRequest(map);
        validator = new ProtocolValidator(request);
        try {
            validator.validate(ProtocolConstantsVersion1.VERSION_1);
            fail("ProtocolException should have been thrown");
        } catch (ProtocolException e) {
            assertEquals(ProtocolStatusCode.INVALID_ARGUMENT,
                    e.getStatusCode());
        }

        //  Try the same query as above, except this time, set format to
        //  PSI-MI.  This should result in a validation error, because
        //  BioPAX is the only valid format.
        map.put(ProtocolRequest.ARG_QUERY, "12345");
        map.put(ProtocolRequest.ARG_FORMAT, ProtocolConstantsVersion1.FORMAT_PSI_MI);
        request = new ProtocolRequest(map);
        validator = new ProtocolValidator(request);
        try {
            validator.validate(ProtocolConstantsVersion1.VERSION_1);
            fail("ProtocolException should have been thrown");
        } catch (ProtocolException e) {
            assertEquals(ProtocolStatusCode.BAD_FORMAT,
                    e.getStatusCode());
        }

        //  Try getting all pathways.  This should result in no validation
        //  errors.
        map = new HashMap();
        map.put(ProtocolRequest.ARG_COMMAND,
                ProtocolConstantsVersion1.COMMAND_GET_TOP_LEVEL_PATHWAY_LIST);
        map.put(ProtocolRequest.ARG_VERSION, ProtocolConstantsVersion1.VERSION_1);
        map.put(ProtocolRequest.ARG_FORMAT, ProtocolConstantsVersion1.FORMAT_BIO_PAX);
        request = new ProtocolRequest(map);
        validator = new ProtocolValidator(request);
        try {
            validator.validate(ProtocolConstantsVersion1.VERSION_1);
        } catch (ProtocolException e) {
            fail("ProtocolException should not have been thrown");
        }
    }

    /**
     * Tests the Protocol Validator, with Empty Parameters.
     *
     * @throws Exception General Error.
     */
    public void testEmptyParameterSet() throws Exception {
        testName = "Test Empty Parameter Case";

        HashMap map = new HashMap();
        ProtocolRequest request = new ProtocolRequest(map);
        ProtocolValidator validator = new ProtocolValidator(request);
        try {
            validator.validate(ProtocolConstantsVersion1.VERSION_1);
            fail("ProtocolException should have been thrown");
        } catch (NeedsHelpException e) {
            request.getCommand();  // Do Nothing.
        }
    }

    /**
     * Tests the Max Hits Parameter
     *
     * @throws Exception All Exceptions.
     */
    public void testMaxHits() throws Exception {
        testName = "Test Maximum Number of Hits";

        //  Set to PSI-MI Mode
        CPathUIConfig.setWebMode(CPathUIConfig.WEB_MODE_PSI_MI);

        HashMap map = new HashMap();
        map.put(ProtocolRequest.ARG_COMMAND,
                ProtocolConstants.COMMAND_GET_BY_KEYWORD);
        ProtocolRequest request = new ProtocolRequest(map);
        request.setFormat(ProtocolConstantsVersion1.FORMAT_XML);
        int maxHits = ProtocolConstantsVersion1.MAX_NUM_HITS + 1;
        request.setMaxHits(Integer.toString(maxHits));
        ProtocolValidator validator = new ProtocolValidator(request);
        try {
            validator.validate(ProtocolConstantsVersion1.VERSION_1);
            fail("ProtocolException should have been thrown");
        } catch (ProtocolException e) {
            ProtocolStatusCode code = e.getStatusCode();
            assertEquals(ProtocolStatusCode.INVALID_ARGUMENT, code);
        }
    }

    /**
     * Gets Name of Test.
     *
     * @return Name of Test.
     */
    public String getName() {
        return "Test the Protocol Validator, "
                + "used for all HTML/XML Web Requests:  " + testName;
    }
}
