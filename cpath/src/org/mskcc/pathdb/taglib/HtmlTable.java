package org.mskcc.pathdb.taglib;

import org.mskcc.pathdb.protocol.ProtocolConstants;
import org.mskcc.pathdb.protocol.ProtocolRequest;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.TagSupport;

/**
 * Abstract Custom Tag for outputting HTML Tables.
 *
 * @author Ethan Cerami.
 */
public abstract class HtmlTable extends TagSupport {
    private StringBuffer html = new StringBuffer();

    /**
     * Executes JSP Custom Tag
     *
     * @return SKIP_BODY Option.
     * @throws JspException Exception in writing to JspWriter.
     */
    public int doStartTag() throws JspException {
        JspWriter out = null;
        html = new StringBuffer();
        if (pageContext != null) {
            out = pageContext.getOut();
        }
        try {
            subDoStartTag();
            if (out != null) {
                out.println(html.toString());
            }
        } catch (Exception e) {
            //  Must specify initCause explicitly
            //  If you pass exception to the Constructor, the
            //  JspException will only save the error message,
            //  and not the actual root cause object.
            JspException jspException = new JspException();
            jspException.initCause(e);
            throw jspException;
        }
        return TagSupport.SKIP_BODY;
    }

    /**
     * Must be implemented by subclass.
     *
     * @throws Exception All Exceptions.
     */
    protected abstract void subDoStartTag() throws Exception;

    protected void createHeader(String title) {
        append("<div id='axial' class='h3'>");
        append("<h3>" + title + "</h3>");
        append("</div>");
    }

    /**
     * Starts HTML Table.
     */
    protected void startTable() {
        append("<table border='1' cellspacing='2' cellpadding='3' "
                + "width='100%'>");
    }

    /**
     * Ends HTML Table.
     */
    protected void endTable() {
        append("</table>");
    }


    /**
     * Appends to String Buffer.
     */
    protected void append(String text) {
        html.append(text + "\n");
    }

    /**
     * Start New Html Row.
     */
    protected void startRow() {
        append("<tr>");
    }

    /**
     * Start New Html Row.
     */
    protected void startRow(int row) {
        if (row % 2 == 0) {
            append("<tr class='a'>");
        } else {
            append("<tr class='b'>");
        }
    }

    /**
     * Ends Html Row.
     */
    protected void endRow() {
        html.append("</tr>");
    }

    /**
     * Creates Table Headers.
     *
     * @param headers Array of String headers.
     */
    protected void createTableHeaders(String[] headers) {
        append("<tr>");
        for (int i = 0; i < headers.length; i++) {
            append("<th>");
            append(headers[i]);
            append("</th>");
        }
        append("</tr>");
    }

    /**
     * Outputs Individial Data Field.
     */
    protected void outputDataField(Object data) {
        outputDataField(data, null);
    }

    /**
     * Outputs Individual Data Field (with URL Link).
     */
    protected void outputDataField(Object data, String url) {
        if (data != null) {
            append("<td valign='TOP'>");
            if (url == null) {
                html.append(data);
            } else {
                outputLink(data.toString(), url);
            }
            append("</td>");
        } else {
            append("<td>----</td>");
        }
    }

    /**
     * Outputs Link.
     */
    protected void outputLink(String name, String url) {
        append("<A HREF=\"" + url + "\">");
        append(name);
        append("</A>");
    }

    /**
     * Outputs Link with Alt Tag
     */
    protected void outputLink(String name, String url, String alt) {
        append("<A TITLE=\"" + alt + "\" HREF=\"" + url + "\">");
        append(name);
        append("</A>");
    }

    /**
     * Gets HTML String.
     * Primarily used by the JUnit DiagnosticTestResults Case Class.
     *
     * @return HTML String.
     */
    public String getHtml() {
        return html.toString();
    }

    /**
     * Gets Internal Link to "get interactions".
     *
     * @param id Unique ID.
     * @return URL back to CPath.
     */
    protected String getInteractionLink(String id, String format) {
        ProtocolRequest request = new ProtocolRequest();
        request.setCommand(ProtocolConstants.COMMAND_GET_BY_KEYWORD);
        request.setVersion(ProtocolConstants.CURRENT_VERSION);
        request.setFormat(format);
        request.setQuery(id);
        return request.getUri();
    }

    /**
     * Gets Internal Link to All Interactions for Specified Organism.
     *
     * @param taxonomyId TaxonomyId
     * @return URL back to CPath.
     */
    protected String getOrganismLink(int taxonomyId) {
        ProtocolRequest request = new ProtocolRequest();
        request.setCommand(ProtocolConstants.COMMAND_GET_BY_KEYWORD);
        request.setVersion(ProtocolConstants.CURRENT_VERSION);
        request.setFormat(ProtocolConstants.FORMAT_HTML);
        request.setOrganism(Integer.toString(taxonomyId));
        return request.getUri();
    }
}