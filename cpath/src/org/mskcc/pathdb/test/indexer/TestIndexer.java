package org.mskcc.pathdb.test.indexer;

import junit.framework.TestCase;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.search.Hits;
import org.mskcc.dataservices.util.ContentReader;
import org.mskcc.pathdb.lucene.*;
import org.mskcc.pathdb.sql.query.QueryException;
import org.mskcc.pathdb.sql.assembly.XmlAssembly;
import org.mskcc.pathdb.sql.assembly.XmlAssemblyFactory;
import org.mskcc.pathdb.util.XmlStripper;
import org.mskcc.pathdb.xdebug.XDebug;

import java.io.IOException;

/**
 * Tests the StoreXmlToIndexer and the QueryIndexer Classes.
 *
 * @author Ethan Cerami
 */
public class TestIndexer extends TestCase {

    /**
     * Tests the Full Text Indexer.
     * @throws Exception All Exceptions.
     */
    public void testIndexer() throws Exception {
        XDebug xdebug = new XDebug();
        XmlAssembly assembly = XmlAssemblyFactory.getXmlAssembly(4, xdebug);
        ItemToIndex item = IndexFactory.createItemToIndex (4, assembly);
        LuceneIndexer lucene = new LuceneIndexer();
        lucene.initIndex();
        lucene.addRecord(item);

        queryInteraction("chaperonin");
        queryInteraction("interactor:chaperonin");
        queryInteraction("P06139");
        queryInteraction("interactor:P06139");
        queryInteraction("Escherichia coli");
        queryInteraction("organism:Escherichia coli");
        queryInteraction("organism:562");
        queryInteraction("Genetic");
        queryInteraction("\"MI:0045\"");
        queryInteraction("interaction_type:\"MI:0045\"");
        queryInteraction("interaction_type:Genetic");
        queryInteraction("pmid:11821039");
        queryInteraction("database:DIP");
    }

    /**
     * Validates Query.
     */
    private void queryInteraction(String terms) throws QueryException,
            IOException {
        LuceneIndexer lucene = new LuceneIndexer();
        Hits hits = lucene.executeQuery(terms);
        assertEquals(1, hits.length());
        Document doc = hits.doc(0);
        Field id = doc.getField(LuceneIndexer.FIELD_CPATH_ID);
        assertEquals("4", id.stringValue());
    }

    /**
     * Tests the XML Stripper Utility.
     * @throws Exception All Exceptions.
     */
    public void testStripper() throws Exception {
        ContentReader reader = new ContentReader();
        String file = new String("testData/psi_sample_mixed.xml");
        String xml = reader.retrieveContent(file);

        //  Test the XML Stripper.
        String text = XmlStripper.stripTags(xml);
        int index = text.indexOf("classical two hybrid");
        assertTrue(index > 0);
    }
}