<html>

<%@ page import="org.mskcc.pathdb.form.WebUIBean"%>
<%@ page import="org.mskcc.pathdb.action.BaseAction"%>
<%@ page import="org.mskcc.pathdb.servlet.CPathUIConfig"%>
<%@ page import="org.mskcc.pathdb.action.web_api.binary_interaction_mode.ExecuteBinaryInteraction" %>
<%@ page import="java.util.List" %>
<%@ page errorPage = "JspError.jsp" %>
<%@ taglib uri="/WEB-INF/taglib/cbio-taglib.tld" prefix="cbio" %>

<%
	// title
	WebUIBean webUIBean = CPathUIConfig.getWebUIBean();
    String title = (String) request.getAttribute(BaseAction.ATTRIBUTE_TITLE);
    if (title == null) {
        title = webUIBean.getApplicationName();
    } else {
        title = webUIBean.getApplicationName() + "::" + title;
    }


    // get binary interaction rules
    List<String> binaryInteractionRules = ExecuteBinaryInteraction.getRuleTypesForDisplay();
%>

<head>
<title><%=title%></title>
<script type="text/javascript" src="jsp/javascript/highslide/highslide-full.js"/></script>
<script type="text/javascript">
    hs.graphicsDir = 'jsp/javascript/highslide/graphics/';
    hs.outlineType = 'rounded-white';
    hs.outlineWhileAnimating = true;
    hs.showCredits = false;
</script>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1"/>
<link rel="stylesheet" href="jsp/javascript/highslide/highslide-with-html.css" type="text/css" />
</head>

<body>

<div>
<table cellpadding="2" cellspacing="2" width="100%">
<tr>
    <td><h2>Visual Legend</h2></td>
</tr>
</table>

<table cellpadding="2" cellspacing="2" width="100%">
<tr bgcolor="#DDDDDD">
    <td VALIGN=CENTER>
        <h3>Shape</h3>
    </td>
    <td VALIGN=CENTER>
        <h3>Node Type</h3>
    </td>
</tr>
<tr>
    <td VALIGN=CENTER>
	    <img src='jsp/images/sif_rules/ellipse.png' align='ABSMIDDLE'>
    </td>
    <td VALIGN=CENTER>
        <span class="rule">Protein or Small Molecule</span>
    </td>
</tr>
<tr>
    <td VALIGN=CENTER>
	  <img src='jsp/images/sif_rules/hexagon.png' align='ABSMIDDLE'>
    </td>
    <td VALIGN=CENTER>
	  <span class="rule">Complex</span>
    </td>
</tr>
</table>

<table cellpadding="2" cellspacing="2">
<tr bgcolor="#DDDDDD">
    <td VALIGN=CENTER>
	    <h3>Shape</h3>
    </td>
    <td VALIGN=CENTER>
        <h3>Edge Type</h3>
    </td>
</tr>
<%
    for (String rule : binaryInteractionRules) {
		String ruleDesc = ExecuteBinaryInteraction.getRuleTypeDescription(rule);
        if (!ruleDesc.trim().endsWith(".")) {
            ruleDesc = ruleDesc.concat(".");
        }
        out.println("<tr>");
		out.println("<td valign=top align=center><img src='jsp/images/sif_rules/" + rule + "_LEGEND_SIF.png'/>");
		//out.println("<td valign=center><span class=\"rule\"><a href=\"\"" +
		//			" onmouseover=\"return overlib('<DIV CLASS=popup><DIV CLASS=popup_caption>" + rule + "</DIV>" +
		//			"<DIV CLASS=popup_text>" + ruleDesc + "</DIV></DIV>', FULLHTML, WRAP, CELLPAD, 5, OFFSETY, -25);" +
		//			" return true;\" onmouseout=\"return nd();\">" + rule + "</a></span><td>");
		out.println("<td valign=center><span class=\"rule\"><a href=\"#\"" +
		            " onclick=\"return hs.htmlExpand(this, {contentId: '" + rule + "-html" + "', width: 300})\" class=\"highslide\">" +
		            rule + "</a></span></td>");
        out.println("</tr>");
		out.println("<div class=\"highslide-html-content\" id=\"" + rule + "-html" + "\">");
		out.println("<div class=\"highslide-header\">");
		out.println("<ul>");
		out.println("<li class=\"highslide-move\">");
		out.println("<a href=\"#\" onclick=\"return false\">Move</a>");
		out.println("</li>");
  		out.println("<li class=\"highslide-close\">");
		out.println("<a href=\"#\" onclick=\"return hs.close(this)\">Close</a>");
		out.println("</li>");
		out.println("</ul>");
		out.println("</div>"); // highslide-header
		out.println("<div class=\"highslide-body\">");
  		out.println("<h5>" + rule  + "</h5>");
		out.println(ruleDesc);
  		out.println("</div>"); // highslide-body
  		out.println("</div>"); // highslide-html-content
    }
%>
<tr>
<td colspan="2"><a href="http://www.pathwaycommons.org/pc/sif_interaction_rules.do" target="_blank">more information</a></td>
</tr>
</table>
</div>
</body>
</html>