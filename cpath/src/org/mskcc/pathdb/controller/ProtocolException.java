package org.mskcc.pathdb.controller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.XMLOutputter;

import java.io.IOException;
import java.io.StringWriter;

/**
 * Encapsulates a Violation of the Data Service Protocol.
 *
 * @author cerami
 */
public class ProtocolException extends Exception {
    /**
     * Status Code Object.
     */
    private ProtocolStatusCode statusCode;

    /**
     * Error Message Details.
     */
    private String details;

    /**
     * Error XML Element Name.
     */
    private static final String ERROR_ELEMENT = "error";

    /**
     * Error Code XML Element Name.
     */
    private static final String ERROR_CODE_ELEMENT = "error_code";

    /**
     * Error Message XML Element Name.
     */
    private static final String ERROR_MESSAGE_ELEMENT = "error_msg";

    /**
     * Error Message XML Element Name.
     */
    private static final String ERROR_DETAILS_ELEMENT = "error_details";

    /**
     * Logger.
     */
    private Log log = LogFactory.getLog(this.getClass());

    /**
     * Constructor.
     * @param statusCode Protocol Status Code.
     */
    public ProtocolException(ProtocolStatusCode statusCode) {
        this.statusCode = statusCode;
    }

    /**
     * Constructor.
     * @param statusCode Protocol Status Code.
     * @param details Error Message Details.
     */
    public ProtocolException(ProtocolStatusCode statusCode, String details) {
        this.statusCode = statusCode;
        this.details = details;
    }

    /**
     * Gets the Status Code.
     * @return Status Code.
     */
    public ProtocolStatusCode getStatusCode() {
        return statusCode;
    }

    /**
     * Gets XML Representation of Error.
     * @return XML String containing Error Message.
     */
    public String toXml() {
        Document document = createXmlDocument();
        XMLOutputter outputter = new XMLOutputter();
        outputter.setIndent(true);
        outputter.setNewlines(true);
        StringWriter writer = new StringWriter();
        try {
            outputter.output(document, writer);
        } catch (IOException e) {
            log.error("Exception thrown while outputting XML Error Protocol:  "
                    + e.getMessage());
        }
        return writer.toString();
    }

    /**
     * Creates JDOM Representation of XML Document.
     * @return JDOM Document object.
     */
    private Document createXmlDocument() {
        Document document = new Document();
        Element errorElement = new Element(ERROR_ELEMENT);
        Element errorCodeElement = new Element(ERROR_CODE_ELEMENT);
        errorCodeElement.setText(Integer.toString(statusCode.getErrorCode()));
        Element errorMsgElement = new Element(ERROR_MESSAGE_ELEMENT);
        errorMsgElement.setText(statusCode.getErrorMsg());
        document.setRootElement(errorElement);
        errorElement.addContent(errorCodeElement);
        errorElement.addContent(errorMsgElement);

        if (details != null) {
            Element errorDetailsElement = new Element(ERROR_DETAILS_ELEMENT);
            errorDetailsElement.setText(details);
            errorElement.addContent(errorDetailsElement);
        }
        return document;
    }
}