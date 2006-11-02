<%@ page import="org.mskcc.pathdb.action.BaseAction,
                 org.mskcc.pathdb.form.WebUIBean,
                 org.mskcc.pathdb.servlet.CPathUIConfig,
                 org.mskcc.pathdb.lucene.LuceneConfig"%>
<%@ page import="org.mskcc.pathdb.sql.dao.DaoExternalDbSnapshot"%>
<%@ page import="java.util.ArrayList"%>
<%@ page import="org.mskcc.pathdb.model.ExternalDatabaseSnapshotRecord"%>
<%@ page import="org.mskcc.pathdb.taglib.DbSnapshotInfo"%>
<%@ page import="org.mskcc.pathdb.model.GlobalFilterSettings"%>
<%@ page errorPage = "JspError.jsp" %>
<%@ taglib uri="/WEB-INF/taglib/cbio-taglib.tld" prefix="cbio" %>

<%
// get tag line
WebUIBean webUIBean = CPathUIConfig.getWebUIBean();

// title
String title = webUIBean.getApplicationName() + "::Filter";
request.setAttribute(BaseAction.ATTRIBUTE_TITLE, title);
%>

<jsp:include page="../global/header.jsp" flush="true" />
<div id="content">
<h1>Restrict my search results to the following data sources:</h1>

<table CELLPADDING=0 CELLSPACING=5>
<form action="storeFilters.do">
    <%
    String referer = request.getHeader("Referer");
    if (referer != null && ! (referer.indexOf("filter.do") > 0)) {
        session.setAttribute("Referer", referer);
    }
    GlobalFilterSettings settings = (GlobalFilterSettings)
            session.getAttribute(GlobalFilterSettings.GLOBAL_FILTER_SETTINGS);
    if (settings == null) {
        settings = new GlobalFilterSettings();
        session.setAttribute(GlobalFilterSettings.GLOBAL_FILTER_SETTINGS, settings);
    }
    DaoExternalDbSnapshot dao = new DaoExternalDbSnapshot();
    ArrayList list = dao.getAllDatabaseSnapshots();

    // process records
    if (list.size() == 0) {
        out.println("<TR>");
        out.println("<TD>No Data Sources Available</TD>");
        out.println("</TR>");
    } else {
        for (int i = 0; i < list.size(); i++) {
            ExternalDatabaseSnapshotRecord snapshotRecord =
                    (ExternalDatabaseSnapshotRecord) list.get(i);
            out.println("<TR><TD>");
            out.println("<INPUT TYPE=CHECKBOX NAME=SNAPSHOT_ID VALUE="
                + snapshotRecord.getId());
            if (settings.isSnapshotSelected(snapshotRecord.getId())) {
                out.println(" CHECKED");
            }
            out.println(">");
            out.println("&nbsp;&nbsp;" +
                    DbSnapshotInfo.getDbSnapshotHtml(snapshotRecord.getId()));
            out.println("</INPUT>");
            out.println("</TD></TR>");
        }
        out.println("<TR><TD><BR>");
        out.println("<INPUT TYPE=SUBMIT VALUE='Set Global Filters'>");
        out.println("</TD></TR>");
    }
    %>
</form>
</table>
</div>
<jsp:include page="../global/footer.jsp" flush="true" />