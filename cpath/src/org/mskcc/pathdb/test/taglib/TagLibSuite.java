package org.mskcc.pathdb.test.taglib;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Suite of all Formatter Unit Tests.
 *
 * @author Ethan Cerami
 */
public class TagLibSuite extends TestCase {

    /**
     * The suite method runs all the tests.
     * @return Suite of JUnit tests.
     */
    public static Test suite() {
        TestSuite suite = new TestSuite();
        suite.addTestSuite(TestInteractionTable.class);
        suite.setName("TagLib Tests");
        return suite;
    }
}