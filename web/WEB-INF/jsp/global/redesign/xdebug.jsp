<%@ page import="org.mskcc.pathdb.xdebug.XDebug,
                 org.mskcc.pathdb.xdebug.XDebugMessage,
                 org.mskcc.pathdb.xdebug.XDebugParameter,
                 java.util.Date,
                 java.util.Enumeration,
                 org.mskcc.pathdb.action.admin.AdminWebLogging,
                 java.util.ArrayList" %>
<%@ page import="org.mskcc.pathdb.util.CPathConstants"%>
<%@ page import="org.mskcc.pathdb.servlet.CPathUIConfig"%>
<%@ page import="java.util.StringTokenizer"%>
<%
XDebug xdebug= (XDebug) request.getAttribute("xdebug");
if (xdebug != null) {
    xdebug.stopTimer();
    Enumeration enum0 = request.getAttributeNames();
    while (enum0.hasMoreElements()) {
        String name = (String) enum0.nextElement();
        Object value = request.getAttribute(name);
        xdebug.addParameter(XDebugParameter.REQUEST_ATTRIBUTE_TYPE, name,
                value.toString());
    }
%>
<%
    String xdebugSession = (String) session.getAttribute
            (AdminWebLogging.WEB_LOGGING);
    String xdebugParameter = request.getParameter(AdminWebLogging.WEB_LOGGING);
    if (xdebugSession != null || xdebugParameter != null) {
%>
<table class="xdebug" width="100%">
    <tr bgcolor="#DDDDDD" valign="top">
		<td align="left">
            cPath Version
        </td>
		<td colspan="2">
            <%= CPathConstants.VERSION %>
        </td>
    </tr>
    <tr bgcolor="#DDDDDD" valign="top">
		<td align="left">
            Total Time to Generate Page
        </td>
		<td colspan="2">
            <%= xdebug.getTimeElapsed() %> ms
        </td>
    </tr>
    <tr bgcolor="#DDDDDD" valign="top">
		<td align="left">
            Current Time
        </td>
		<td colspan="2">
            <%= new Date() %> ms
        </td>
	</tr>
    <tr bgcolor="#DDDDDD" valign="top">
        <td align="left">Web Skin</td>
        <td colspan="2">
            <%= CPathUIConfig.getWebSkin() %>
        </td>
    </tr>
    <tr bgcolor="#DDDDDD" valign="top">
		<td align="left">
            Class Name
        </td>
		<td align="left" colspan="2">
            Message
        </td>
	</tr>

	<%--
				***********************************
				Output Log Messages
				***********************************
	--%>
	<%
		ArrayList messages = xdebug.getDebugMessages();
		for (int msgIndex=0; msgIndex<messages.size(); msgIndex++) {
			XDebugMessage msg = (XDebugMessage) messages.get(msgIndex);
	%>
		<tr bgcolor="#ccccff" valign="top">
			<td>
                <%= wrapText(msg.getClassName()) %>
            </td>
			<td colspan="2">
				<font color="<%= msg.getColor() %>">
				<%= wrapText(msg.getMessage()) %>
				</font>
            </td>
		</tr>
    <% } %>
	<%--
				***********************************
				Output Parameter Values
				***********************************
	--%>
	<tr bgcolor="#DDDDDD">
		<td align="left">Parameter Type</td>
		<td align="left">Name</td>
		<td align="left">Value</td>
	</tr>
	<%
		ArrayList parameters = xdebug.getParameters();
		String bgcolor;
		for (int paramIndex=0; paramIndex<parameters.size(); paramIndex++) {
			XDebugParameter param = (XDebugParameter)
                    parameters.get(paramIndex);
			if (param.getType()==XDebugParameter.USER_TYPE)
                bgcolor = "#C9FFD3";
			else if (param.getType()==XDebugParameter.HTTP_HEADER_TYPE)
                bgcolor="LIGHTBLUE";
			else if (param.getType()==XDebugParameter.COOKIE_TYPE)
                bgcolor="PINK";
			else if (param.getType()==XDebugParameter.SESSION_TYPE)
                bgcolor="#FFCBFF";
			else if (param.getType()==XDebugParameter.ENVIRONMENT_TYPE)
                bgcolor="LIGHTYELLOW";
			else if (param.getType()==XDebugParameter.HTTP_TYPE)
                bgcolor="LIGHTGREEN";
			else if (param.getType()==XDebugParameter.REQUEST_ATTRIBUTE_TYPE)
                bgcolor="LIGHTYELLOW";
            else if (param.getType()==XDebugParameter.SERVLET_CONTEXT_TYPE)
                bgcolor="LIGHTBLUE";
			else bgcolor = "WHITE";
	%>

		<tr bgcolor="<%= bgcolor %>">
			<td valign="top"><%= wrapText(param.getTypeName()) %></td>
			<td valign="top"><%= wrapText(param.getName()) %></td>
			<td valign="top"><%= wrapText(param.getValue()) %></td>
		</tr>
		<% } %>
</table>
<% } %>
<% } %>

<%!

//  This is a little hack to replace .'s with .'s followed by a zero-width spacer.
//  Enables browsers to wrap long words within tables
private String wrapText (String text) {
    if (text != null) {
        text = text.replaceAll("\\.", "\\. ");
        text = text.replaceAll("&", "&amp;");
        return text;
    } else {
        return new String ("Not Available");
    }
}
%>
