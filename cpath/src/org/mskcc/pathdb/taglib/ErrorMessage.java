package org.mskcc.pathdb.taglib;

import org.apache.lucene.queryParser.ParseException;
import org.apache.struts.util.PropertyMessageResources;
import org.mskcc.pathdb.action.admin.AdminWebLogging;
import org.mskcc.pathdb.controller.ProtocolException;
import org.xml.sax.SAXParseException;

import javax.servlet.ServletContext;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpSession;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Stack;

/**
 * Outpus all Error Messages/Exceptions to the End User.
 *
 * @author Ethan Cerami
 */
public class ErrorMessage extends HtmlTable {
    private Throwable throwable;
    private static final String STRUTS_MESSAGE_RESOURCE =
            "org.apache.struts.action.MESSAGE";
    private static final String MSG_ERROR_HEADER =
            "error.header";
    private static final String MSG_ERROR_INTERNAL =
            "error.internal";
    private static final String MSG_ERROR_INTERNAL_WITH_DETAILS =
            "error.internalWithDetail";
    private static final String MSG_ERROR_PARSING =
            "error.parsing";
    private static final String MSG_ERROR_MISSING_SEARCH_TERM =
            "error.missingSearchTerm";
    private static final String MSG_LUCENE_INDEX_NOT_FOUND =
            "error.luceneIndexNotFound";


    /**
     * Sets the Throwable object with error/exception information.
     *
     * @param throwable Throwable Object.
     */
    public void setThrowable(Throwable throwable) {
        this.throwable = throwable;
    }

    /**
     * Executes JSP Custom Tag
     *
     * @throws Exception Exception in writing to JspWriter.
     */
    public void subDoStartTag() throws Exception {
        boolean xdebugFlag = getXDebugFlag();
        ServletContext servletContext = pageContext.getServletContext();
        PropertyMessageResources resource =
                (PropertyMessageResources) servletContext.getAttribute
                (STRUTS_MESSAGE_RESOURCE);

        String header = resource.getMessage(MSG_ERROR_HEADER);
        this.append("<div class=\"errormessage\">");
        this.append("<p><strong>" + header + "</strong></p>");
        Throwable rootCause = getRootCause(throwable);
        outputUserMessage(rootCause, resource);
        if (xdebugFlag) {
            outputDiagnostics(throwable, rootCause);
        }
        this.append("</div>");
    }

    /**
     * Initializes Custom Tag.
     */
    private boolean getXDebugFlag() {
        boolean xdebugFlag = false;
        HttpSession session = pageContext.getSession();
        ServletRequest request = pageContext.getRequest();
        String xdebugSession = (String) session.getAttribute
                (AdminWebLogging.WEB_LOGGING);
        String xdebugParameter = request.getParameter
                (AdminWebLogging.WEB_LOGGING);
        if (xdebugSession != null || xdebugParameter != null) {
            xdebugFlag = true;
        }
        return xdebugFlag;
    }

    /**
     * Outputs User Message.
     */
    private void outputUserMessage(Throwable rootCause,
            PropertyMessageResources resource) {
        String userMsg = resource.getMessage(MSG_ERROR_INTERNAL);
        if (rootCause instanceof ParseException) {
            userMsg = resource.getMessage(MSG_ERROR_PARSING);
        } else if (rootCause instanceof ProtocolException) {
            ProtocolException pException = (ProtocolException) rootCause;
            userMsg = pException.getWebErrorMessage();
        } else if (rootCause instanceof FileNotFoundException) {
            userMsg = resource.getMessage(MSG_LUCENE_INDEX_NOT_FOUND);
        } else if (rootCause instanceof SAXParseException) {
            SAXParseException sexc = (SAXParseException) rootCause;
            userMsg = "Your XML document contains the "
                    + "following error:<P>" + rootCause.getMessage()
                    + "<P>Error occurred at line number:   "
                    + sexc.getLineNumber()
                    + "<P>Please correct the error and try again.";
        }
        this.append(userMsg);
    }

    /**
     * Outputs Full Diagnostics.
     */
    private void outputDiagnostics(Throwable throwable, Throwable rootCause) {
        append("<P>Root Cause Message:  "
                + rootCause.getMessage());
        append("<P>Root Cause Class:  "
                + rootCause.getClass().getName());
        StringWriter writer = new StringWriter();
        PrintWriter pWriter = new PrintWriter(writer);
        throwable.printStackTrace(pWriter);
        append("<FONT SIZE=-1><PRE>\n"
                + writer.toString() + "\n</PRE></FONT>");
    }

    /**
     * Gets the Root Cause of this Exception.
     */
    private Throwable getRootCause(Throwable throwable) {
        Stack stack = new Stack();
        stack.push(throwable);
        try {
            Throwable temp = throwable.getCause();
            while (temp != null) {
                stack.push(temp);
                temp = temp.getCause();
            }
            return (Throwable) stack.pop();
        } catch (NullPointerException e) {
            return throwable;
        }
    }
}