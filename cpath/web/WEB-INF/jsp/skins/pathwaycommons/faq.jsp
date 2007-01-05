<h1>Frequently Asked Questions (FAQs)</h1>
<ul>
    <li><a href="#general">General Questions</a></li>
    <li><a href="#bio">Questions targeted for Biologists</a></li>
    <li><a href="#comp_bio">Questions targeted for Computational Biologists and Software Developers</a></li>
</ul>
<h2><a name="general">General Questions</a></h2>
<h3>What is Pathway Commons?</h3>
<p>Pathway Commons is a collection of publicly available pathways from multiple organisms.
It provides researchers with convenient access to a comprehensive collection of pathways from
multiple sources represented in a common language. Access is via a web portal for browsing,
query and download. Database providers can share their pathway data via a common repository
and avoid duplication and reduce software development costs. Bioinformatics software developers
can increase efficiency by sharing software components.
</p>
<h3>Is Pathway Commons free?</h3>
<p>Yes, it's free! Pathway Commons distributes pathway content with the intellectual
property restrictions of the source database.  However, only databases that are freely available
or free to academics are included.
</p>
<h3>Doesn't Pathway Commons compete with other pathway databases?</h3>
<p>No. Pathway Commons does not compete with or duplicate efforts of pathway databases or
software tool providers. Pathway Commons will add value to these existing efforts by providing
a shared resource for publishing, distributing and querying pathway information. Existing
database groups will provide pathway curation, Pathway Commons will provide a mechanism and
the technology for sharing. A vital aspect of Pathway Commons is clear author attribution.
Curation teams at existing databases must be supported as much as possible by researchers to
ensure they can keep performing their valuable work.
</p>
<h3>What are the future plans for Pathway Commons?</h3>
<p>The Pathway Commons work group will provide software systems to collect, store and integrate
pathway data from database groups, with clear author attribution; store, validate, index and
maintain the information to enable efficient, maximum quality access; distribute pathway
information to the scientific public; and, provide a basic set of end user software for
browsing and analysis.</p>
<h3>What pathway databases exist?</h3>
<p>We have compiled a comprehensive list of pathway databases at
<a href="http://www.pathguide.org">Pathguide</a>.</p>

<h2><a name="bio">For Biologists</a></h2>
<h3>What can I do with this information?</h3>
<p>You can browse available pathway information and answer questions such as:</p>
<ul>
    <li>What proteins interact with my favorite protein?</li>
    <li>What pathways involve my favorite protein?</li>
    <li>Is my favorite protein involved in transport events or biochemical reactions?</li>
    <li>What enzymes use my favorite metabolite as a substrate?</li>
    <li>And many more...</li>
</ul>
<h3>How were the pathways collected?</h3>
<p>Pathways were downloaded directly from source databases. Each source pathway database has
been created differently, some by manual extraction of pathway information from the literature
and some by computational prediction. Pathway Commons provides a filtering mechanism to allow
the user to view only chosen subsets of information, such as only the manually curated subset.
</p>
<h3>What kind of information is part of each pathway?</h3>
<p>Pathways from different databases are defined by different levels of detail. Details that
may be included are proteins, small molecules, DNA, RNA, complexes and their cellular locations,
different types of physical interactions, such as molecular interaction, biochemical reaction,
catalysis, complex assembly and transport, post-translational protein modifications, original
citations, experimental evidence and links to other databases e.g. of protein sequence annotation.
Some information is only available in the downloaded BioPAX files.
</p>
<h3>How good is the quality of Pathway Commons?</h3>
<p>The quality of Pathway Commons pathways is dependent on the quality of the pathways from
source databases. Pathway Commons allows users to filter data by various criteria, including
data source, which should allow viewing a restricted subset of high quality data. In the future,
Pathway Commons will implement published algorithms to automatically assess data quality and allow
this as an additional filter.
</p>

<h2><a name="comp_bio">For Computational Biologists and Software developers</a></h2>
<h3>What can I do with this information?</h3>
You can download the pathways in BioPAX Level 2 format for global analysis.
<a href="http://www.biopax.org">Details about the BioPAX format</a>
<h3>How many pathways are part of Pathway Commons?</h3>
<p>Please see the statistics on the homepage for up to the minute information.</p>
<h3>What is cPath?</h3>
<p>cPath is open source database software that runs Pathway Commons website. You can download it
for your own use from <a href="http://cbio.mskcc.org/dev_site/cpath/">the cPath developer site</a>.
</p>
<p>
Main features include:
</p>
<ul>
<li>Import pipeline capable of aggregating pathway and interaction data sets from multiple sources,
including: MINT, IntAct, HPRD, DIP, BioCyc, KEGG, PUMA2, Reactome and others available in
PSI-MI or BioPAX format.</li>
<li>Import/Export support for the Proteomics Standards Initiative Molecular Interaction
(<a href="http://psidev.sourceforge.net/mi/xml/doc/user/">PSI-MI</a>) and the Biological
Pathways Exchange (<a href="http://www.biopax.org">BioPAX</a>) XML formats.</li>
<li>Data visualization and analysis via <a href="http://www.cytoscape.org">Cytoscape</a>.</li>
<li>Simple HTTP URL based XML web service.</li>
<li>Complete software is <a href="http://cbio.mskcc.org/dev_site/cpath/">freely available for
local install</a>.  Easy to install and administer.</li>
<li>Partly funded by the U.S. National Cancer Institute, via the Cancer Biomedical
Informatics Grid (<a href="https://cabig.nci.nih.gov/">caBIG</a>), and aims to meet
"silver-level" requirements for software interoperability and data exchange.</li>
</ul>