<%@ page import="org.mskcc.pathdb.action.BaseAction"%>
<%@ taglib uri="/WEB-INF/taglib/cbio-taglib.tld" prefix="cbio" %>
<%@ page errorPage = "JspError.jsp" %>
<% request.setAttribute(BaseAction.ATTRIBUTE_TITLE, "Browse Pathway(s)"); %>

<jsp:include page="../global/redesign/header.jsp" flush="true" />

<div>
<h1>Browse Pathways:</h1>
</div>
<cbio:pathwayListTable/>
<P>&nbsp;
<P>&nbsp;
<P>&nbsp;
<jsp:include page="../global/redesign/footer.jsp" flush="true" />