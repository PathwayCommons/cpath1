package org.mskcc.pathdb.util;

import org.jdom.Attribute;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

import java.io.StringReader;
import java.io.IOException;
import java.util.List;

/**
 * Utility Class for stripping out all XML Markup from a document,
 * and returning all element and attributes values only.
 *
 * @author Ethan Cerami
 */
public class XmlStripper {

    /**
     * Strips all XML Markup from Document.
     * @param xml XML Document.
     * @return String of Tokens.
     * @throws IOException Error in JDOM.
     */
    public static String stripTags(String xml) throws IOException {
        StringBuffer textBuffer = new StringBuffer();
        try {
            StringReader reader = new StringReader(xml);
            SAXBuilder saxBuilder = new SAXBuilder();
            org.jdom.Document jdomDoc = saxBuilder.build(reader);
            Element element = jdomDoc.getRootElement();
            processElement(element, textBuffer);
        } catch (JDOMException e) {
            e.printStackTrace();
        }
        return textBuffer.toString();
    }

    /**
     * Recursive Method for Processing Elements.
     * @param element Element.
     * @param textBuffer StringBuffer.
     */
    private static void processElement(Element element,
            StringBuffer textBuffer) {
        //  Extract Text
        String text = element.getTextTrim();
        if (text.length() > 0) {
            appendText(textBuffer, text);
        }

        //  Extract Attribute Values
        List attributes = element.getAttributes();
        for (int i = 0; i < attributes.size(); i++) {
            Attribute attribute = (Attribute) attributes.get(i);
            String value = attribute.getValue();
            appendText(textBuffer, value);
        }

        //  Recursively process all children.
        List children = element.getChildren();
        for (int i = 0; i < children.size(); i++) {
            Element child = (Element) children.get(i);
            processElement(child, textBuffer);
        }
    }

    private static void appendText(StringBuffer textBuffer, String text) {
        if (textBuffer.length() > 0) {
            textBuffer.append(" ");
        }
        textBuffer.append(text);
    }
}