<%@ page import="org.mskcc.pathdb.sql.dao.DaoCPath,
                 org.mskcc.pathdb.model.CPathRecordType,
                 org.mskcc.pathdb.util.security.XssFilter,
                 org.mskcc.pathdb.lucene.OrganismStats,
                 org.mskcc.pathdb.xdebug.XDebug,
                 java.text.DecimalFormat,
                 java.text.NumberFormat"%>
<%@ page import="org.mskcc.pathdb.form.WebUIBean"%>
<%@ page import="org.mskcc.pathdb.servlet.CPathUIConfig"%>

<%
try {
    DaoCPath dao = DaoCPath.getInstance();
    int numPathways = dao.getNumEntities(CPathRecordType.PATHWAY);
    int numInteractions = dao.getNumEntities(CPathRecordType.INTERACTION);
    int numPhysicalEntities = dao.getNumEntities
            (CPathRecordType.PHYSICAL_ENTITY);
    NumberFormat formatter = new DecimalFormat("#,###,###");
    WebUIBean webUIBean = CPathUIConfig.getWebUIBean();

%>
<b><%= webUIBean.getApplicationName()%> Quick Stats:</b>
<TABLE>
    <TR>
        <TD>Number of Pathways:</TD>
        <TD><%= formatter.format(numPathways) %></TD>
    <TR>
        <TD>Number of Interactions:</TD>
        <TD><%= formatter.format(numInteractions) %></TD>
    </TR>
    <TR>
        <TD>Number of Physical Entities:</TD>
        <TD><%= formatter.format(numPhysicalEntities) %></TD>
    </TR>
</TABLE>
<% } catch (Exception e) {
} %>