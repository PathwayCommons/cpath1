package org.mskcc.pathdb.tool;

import org.mskcc.pathdb.model.*;
import org.mskcc.pathdb.sql.JdbcUtil;
import org.mskcc.pathdb.sql.dao.*;
import org.mskcc.pathdb.task.ProgressMonitor;
import org.mskcc.pathdb.util.ExternalDatabaseConstants;
import org.mskcc.pathdb.util.tool.ConsoleUtil;

import java.io.File;
import java.io.IOException;
import java.io.FileWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Collection;

/**
 * Command Line Utility to Dump Gene Sets.
 *
 * This classes supports two data formats:
 * 
 * 1)  GSEA GMT: Gene Matrix Transposed file format (*.gmt) Format.
 * Format is described at:
 * http://www.broad.mit.edu/cancer/software/gsea/wiki/index.php/Data_formats
 *
 * 2)  Pathway Commons Gene Set format:  Similar to the GSEA GMT format, except that all
 * participants are micro-encoded with multiple identifiers. For example, each participant
 * is specified as: CPATH_ID:UNIPROT_ACCESION:GENE_SYMBOL:ENTREZ_GENE_ID.
 *
 * It also creates the a directory structure like so:
 *
 * - snapshots
 * ---- gsea
 * ------- by_species
 * ------- by_source
 * ---- gene_sets
 * ------- by_species
 * ------- by source
 */
public class DumpGeneSets {
    private ProgressMonitor pMonitor;
    private File outDir;
    private File gseaDir;
    private File pcDir;
    private final static String TAB = "\t";
    private final static String COLON = ":";
    private static final int BLOCK_SIZE = 1000;
    private static final int GSEA_OUTPUT = 1;
    private static final int PC_OUTPUT = 2;

    //  HashMap that will contain multiple open file writers
    private HashMap<String, FileWriter> fileWriters = new HashMap <String, FileWriter>();

    /**
     * Constructor.
     *
     * @param pMonitor Progress Monitor.
     */
    public DumpGeneSets(ProgressMonitor pMonitor, File outDir) throws IOException {
        this.pMonitor = pMonitor;
        this.outDir = outDir;
        ToolInit.initProps();
    }

    /**
     * Gene Set Dump.
     */
    public void dumpGeneSets() throws DaoException, IOException, SQLException {
        DaoCPath dao = DaoCPath.getInstance();
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        CPathRecord record = null;
        long maxIterId = dao.getMaxCpathID();
        pMonitor.setMaxValue((int) maxIterId);

        //  Initialize output directories
        initDirs();

        //  Iterate through all cPath Records in blocks
        try {
            for (int id = 0; id <= maxIterId; id = id + BLOCK_SIZE + 1) {
                // setup start/end id to fetch
                long startId = id;
                long endId = id + BLOCK_SIZE;
                if (endId > maxIterId) endId = maxIterId;

                con = JdbcUtil.getCPathConnection();
                pstmt = con.prepareStatement("select * from cpath WHERE "
                        + " CPATH_ID BETWEEN " + startId + " and " + endId
                        + " order by CPATH_ID ");
                rs = pstmt.executeQuery();

                while (rs.next()) {
                    if (pMonitor != null) {
                        pMonitor.incrementCurValue();
                        ConsoleUtil.showProgress(pMonitor);
                    }
                    record = dao.extractRecord(rs);

                    //  Only dump pathway records
                    if (record.getType() == CPathRecordType.PATHWAY) {
                        dumpPathwayRecord(record);
                    }
                }
                JdbcUtil.closeAll(con, pstmt, rs);
            }
        } finally {
            Collection<FileWriter> fds = fileWriters.values();
            for (FileWriter fileWriter:  fds) {
                fileWriter.close();
            }
            JdbcUtil.closeAll(con, pstmt, rs);
        }
    }

    /**
     * Initializes the Output Directories. This method creates a structure like so:
     * - gsea
     * ---- by_species
     * ---- by_source
     * - gene_sets
     * ---- by_species
     * ---- by source
     * 
     * @throws IOException IO Errors.
     */
    private void initDirs() throws IOException {
        if (!outDir.exists()) {
            outDir.mkdir();
        }
        gseaDir = initDir (outDir, "gsea");
        pcDir = initDir (outDir, "gene_sets");
    }

    /**
     * Initializes output directories.  This method creates a structure like so:
     * - gsea
     * ---- by_species
     * ---- by_source
     */
    private File initDir (File baseDir, String targetDir) {
        File newDir = new File (baseDir, targetDir);
        if (!newDir.exists()) {
            newDir.mkdir();
        }
        File bySpeciesDir = getBySpeciesDir (newDir);
        if (!bySpeciesDir.exists()) {
            bySpeciesDir.mkdir();
        }
        File byDataSourceDir = getBySourceDir (newDir);
        if (!byDataSourceDir.exists()) {
            byDataSourceDir.mkdir();
        }
        return newDir;
    }

    /**
     * Gets the by_species directory.
     */
    private File getBySpeciesDir (File baseDir) {
        return new File (baseDir, "by_species");
    }

    /**
     * Gets the by_source directory.
     */
    private File getBySourceDir (File baseDir) {
        return new File (baseDir, "by_source");
    }

    /**
     * Dumps the Pathway Record in the specified file format.
     */
    private void dumpPathwayRecord(CPathRecord record)
            throws DaoException, IOException {

        //  Gets the Database Term
        DaoExternalDbSnapshot daoSnapshot = new DaoExternalDbSnapshot();
        long snapshotId = record.getSnapshotId();
        ExternalDatabaseSnapshotRecord snapshotRecord =
                daoSnapshot.getDatabaseSnapshot(snapshotId);
        String dbTerm = snapshotRecord.getExternalDatabase().getMasterTerm();

        DaoInternalFamily daoInternalFamily = new DaoInternalFamily();
        long[] descendentIds = daoInternalFamily.getDescendentIds(record.getId(),
                CPathRecordType.PHYSICAL_ENTITY);

        //  Get XRefs for all Participants
        ArrayList <HashMap <String, String>> xrefList =
                new ArrayList <HashMap <String, String>>();
        for (long descendentId : descendentIds) {
            HashMap <String, String> xrefMap = getXRefMap (descendentId);
            xrefList.add (xrefMap);
        }

        //  Dump to both file formats.
        outputGeneSet(record, dbTerm, descendentIds, xrefList, GSEA_OUTPUT);
        outputGeneSet(record, dbTerm, descendentIds, xrefList, PC_OUTPUT);
    }

    /**
     * Actual Output of the Gene Set.
     */
    private void outputGeneSet(CPathRecord record, String dbTerm, long[] descendentIds,
            ArrayList<HashMap<String, String>> xrefList, int outputFormat) throws IOException,
            DaoException {
        StringBuffer line = new StringBuffer();
        line.append (record.getName() + TAB);
        line.append (dbTerm + TAB);

        int numParticipantsOutput = 0;
        for (int i=0; i < descendentIds.length; i++) {
            long descendentId = descendentIds[i];
            HashMap <String, String> xrefMap = xrefList.get(i);
            String geneSymbol = xrefMap.get(ExternalDatabaseConstants.GENE_SYMBOL);
            String entrezGeneId = xrefMap.get(ExternalDatabaseConstants.ENTREZ_GENE);
            String uniprotAccession = xrefMap.get(ExternalDatabaseConstants.UNIPROT);
            if (outputFormat == GSEA_OUTPUT) {
                if (geneSymbol != null) {
                    numParticipantsOutput++;
                    line.append (geneSymbol + TAB);
                }
            } else {
                numParticipantsOutput++;
                line.append (descendentId + COLON);
                line.append (getXRef (uniprotAccession) + COLON);
                line.append (getXRef (geneSymbol) + COLON);
                line.append (getXRef (entrezGeneId));
                line.append (TAB);
            }
        }
        line.append ("\n");

        //  Append to the correct output files
        if (numParticipantsOutput > 0) {
            appendToOrganismFile (line.toString(), record.getNcbiTaxonomyId(), outputFormat);
            appendToDataSourceFile (line.toString(), dbTerm, outputFormat);
        }
    }

    private String getXRef (String id) {
        if (id == null) {
            return "NA";
        } else {
            return id;
        }
    }

    /**
     * Appends to a Data Source File.
     */
    private void appendToDataSourceFile (String line, String dbTerm, int outputFormat)
        throws IOException {
        String fdKey = dbTerm + outputFormat;
        String fileExtension = getFileExtension (outputFormat);
        FileWriter writer = fileWriters.get(fdKey);
        File dir = getBySourceDir (getFormatSpecificDir(outputFormat));
        if (writer == null) {
            writer = new FileWriter (new File (dir, dbTerm.toLowerCase() + fileExtension));
            fileWriters.put(fdKey, writer);
        }
        writer.write(line);
    }

    /**
     * Appends to an Organism File.
     */
    private void appendToOrganismFile (String line, int ncbiTaxonomyId, int outputFormat)
            throws IOException, DaoException {
        String fdKey = Integer.toString(ncbiTaxonomyId) + outputFormat;
        String fileExtension = getFileExtension (outputFormat);
        FileWriter writer = fileWriters.get(fdKey);
        File dir = getBySpeciesDir (getFormatSpecificDir(outputFormat));
        if (writer == null) {
            DaoOrganism daoOrganism = new DaoOrganism();
            Organism organism = daoOrganism.getOrganismByTaxonomyId(ncbiTaxonomyId);
            String speciesName = organism.getSpeciesName().replaceAll(" ", "_");
            writer = new FileWriter (new File (dir, speciesName.toLowerCase() + fileExtension));
            fileWriters.put(fdKey, writer);
        }
        writer.write(line);
    }

    private String getFileExtension (int outputFormat) {
        if (outputFormat == GSEA_OUTPUT) {
            return ".gmt";
        } else {
            return ".txt";
        }
    }

    /**
     * Gets the format specific base directory.
     * @param outputFormat  Output Format.
     * @return Directory.
     */
    private File getFormatSpecificDir(int outputFormat) {
        File baseDir;
        if (outputFormat == GSEA_OUTPUT) {
            baseDir = gseaDir;
        } else {
            baseDir = pcDir;
        }
        return baseDir;
    }

    /**
     * Gene Symbol Look up.
     */
    private HashMap <String, String> getXRefMap(long cpathId) throws DaoException {
        HashMap <String, String> xrefMap = new HashMap <String, String> ();
        DaoExternalLink daoExternalLink = DaoExternalLink.getInstance();
        ArrayList<ExternalLinkRecord> xrefList = daoExternalLink.getRecordsByCPathId(cpathId);
        for (ExternalLinkRecord xref : xrefList) {
            String dbMasterTerm = xref.getExternalDatabase().getMasterTerm();
            String xrefId = xref.getLinkedToId();
            xrefMap.put(dbMasterTerm, xrefId);
        }
        return xrefMap;
    }

    /**
     * Command Line Usage.
     *
     * @param args Must include UniProt File Name.
     * @throws java.io.IOException IO Error.
     */
    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            System.out.println("command line usage:  dumpGeneSets.pl <output_dir>");
            System.exit(1);
        }
        ProgressMonitor pMonitor = new ProgressMonitor();
        pMonitor.setConsoleMode(true);

        File outDir = new File(args[0]);
        System.out.println("Writing out to:  " + outDir.getAbsolutePath());
        DumpGeneSets dumper = new DumpGeneSets(pMonitor, outDir);
        dumper.dumpGeneSets();

        ArrayList<String> warningList = pMonitor.getWarningList();
        System.out.println("Total number of warning messages:  " + warningList.size());
        int i = 1;
        for (String warning : warningList) {
            System.out.println("Warning #" + i + ":  " + warning);
            i++;
        }
    }
}