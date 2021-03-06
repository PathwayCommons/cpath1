// $Id: BioPaxUtil.java,v 1.13 2008-07-01 21:23:15 cerami Exp $
//------------------------------------------------------------------------------
/** Copyright (c) 2006 Memorial Sloan-Kettering Cancer Center.
 **
 ** Code written by: Ethan Cerami
 ** Authors: Ethan Cerami, Gary Bader, Chris Sander
 **
 ** This library is free software; you can redistribute it and/or modify it
 ** under the terms of the GNU Lesser General Public License as published
 ** by the Free Software Foundation; either version 2.1 of the License, or
 ** any later version.
 **
 ** This library is distributed in the hope that it will be useful, but
 ** WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
 ** MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
 ** documentation provided hereunder is on an "as is" basis, and
 ** Memorial Sloan-Kettering Cancer Center
 ** has no obligations to provide maintenance, support,
 ** updates, enhancements or modifications.  In no event shall
 ** Memorial Sloan-Kettering Cancer Center
 ** be liable to any party for direct, indirect, special,
 ** incidental or consequential damages, including lost profits, arising
 ** out of the use of this software and its documentation, even if
 ** Memorial Sloan-Kettering Cancer Center
 ** has been advised of the possibility of such damage.  See
 ** the GNU Lesser General Public License for more details.
 **
 ** You should have received a copy of the GNU Lesser General Public License
 ** along with this library; if not, write to the Free Software Foundation,
 ** Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 **/
package org.mskcc.pathdb.util.biopax;

import org.jdom.*;
import org.jdom.input.SAXBuilder;
import org.mskcc.pathdb.schemas.biopax.BioPaxConstants;
import org.mskcc.pathdb.schemas.biopax.OwlConstants;
import org.mskcc.pathdb.util.rdf.RdfConstants;
import org.mskcc.pathdb.util.rdf.RdfUtil;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

/**
 * BioPax Utility Class.
 *
 * @author Ethan Cerami
 */
public class  BioPaxUtil {
    private HashMap rdfResources = new HashMap();
    private ArrayList pathwayList = new ArrayList();
    private ArrayList interactionList = new ArrayList();
    private ArrayList physicalEntityList = new ArrayList();
    private ArrayList ontologyList = new ArrayList();
    private BioPaxConstants bioPaxConstants = new BioPaxConstants();
    private ArrayList errorList = new ArrayList();
    private Document bioPaxDoc;
    private HashMap pathwayMembershipMap;
    private HashSet visitedNodeSet;
    private HashSet referenceSet;

    /**
     * Constructor.
     *
     * @param reader Reader Object.
     * @throws IOException   Input/Output Error.
     * @throws JDOMException XML Error.
     */
    public BioPaxUtil(Reader reader)
            throws IOException, JDOMException {
        loadDocument(reader);
    }

    /**
     * Gets HashMap of All RDF Resources, keyed by RDF ID.
     *
     * @return HashMap of All RDF Resources, keyed by RDF ID.
     */
    public HashMap getRdfResourceMap() {
        return rdfResources;
    }

    /**
     * Gets the Root Element of the BioPAX Tree.
     *
     * @return JDOM Element Object.
     */
    public Element getRootElement() {
        return this.bioPaxDoc.getRootElement();
    }

    /**
     * Loads the Specified Document.
     */
    private void loadDocument(Reader reader) throws JDOMException, IOException {
        pathwayMembershipMap = new HashMap();
        referenceSet = new HashSet();

        //  Read in File via JDOM SAX Builder
        SAXBuilder builder = new SAXBuilder();
        bioPaxDoc = builder.build(reader);

        //  Get Root Element
        Element root = bioPaxDoc.getRootElement();

        //  First Step:  Inspect Tree to categorize all RDF Resources
        categorizeResources(root, null);

        //  Second Step:  Validate that all RDF links point to actual
        //  RDF Resources, defined in the document.
        validateResourceLinks(root);

        //  Third Step:  Determine Pathway Memberhip
        determinePathwayMembership();
    }

    private void determinePathwayMembership() {
        //  First, determine top-level pathways
        ArrayList topLevelPathways = new ArrayList();
        for (int i = 0; i < pathwayList.size(); i++) {
            Element pathway = (Element) pathwayList.get(i);
            Attribute idAttribute = pathway.getAttribute
                    (RdfConstants.ID_ATTRIBUTE, RdfConstants.RDF_NAMESPACE);
            if (idAttribute != null) {
                String rdfId = idAttribute.getValue();

                //  If nothing references this pathway, it is considered
                //  a "top-level" pathway.
                if (!referenceSet.contains(rdfId)) {
                    topLevelPathways.add(pathway);
                }
            }
        }

        //  Then, iterate through all top-level pathways.
        for (int i = 0; i < topLevelPathways.size(); i++) {
            visitedNodeSet = new HashSet();
            Element pathway = (Element) topLevelPathways.get(i);
            traversePathway(pathway, pathway);
        }
    }

    /**
     * Recursively Traverse an Entire Pathway, in order to determine
     * pathway membership of all elements in the document.
     */
    private void traversePathway(Element e, Element rootLevelPathway) {
        boolean keepTraversingTree = true;
        if (e != rootLevelPathway) {
            //  Get an RDF ID Attribute, if there is one
            Attribute idAttribute = e.getAttribute
                    (RdfConstants.ID_ATTRIBUTE, RdfConstants.RDF_NAMESPACE);

            //  Get a pointer to an RDF resource, if there is one.
            Attribute pointerAttribute = e.getAttribute
                    (RdfConstants.RESOURCE_ATTRIBUTE,
                            RdfConstants.RDF_NAMESPACE);

            if (idAttribute != null) {
                //  Case 1:  The element has an RDF ID attribute.
                this.setPathwayMembership(e, rootLevelPathway);
            } else if (pointerAttribute != null) {
                //  Case 2:  The element has an RDF Resource/Pointer Attribute
                String uri = RdfUtil.removeHashMark
                        (pointerAttribute.getValue());
                Element referencedResource = (Element) rdfResources.get(uri);
                if (referencedResource != null) {
                    if (visitedNodeSet.contains(uri)) {
                        //  If we have already been here, stop traversing.
                        //  Prevents Circular References.
                        keepTraversingTree = false;
                    } else {
                        setPathwayMembership(referencedResource,
                                rootLevelPathway);
                        visitedNodeSet.add(uri);

                        //  Now, keep walking from the referenced resource
                        e = referencedResource;
                    }
                }
            }
        }

        //  Traverse through all children.
        if (keepTraversingTree) {
            List children = e.getChildren();
            for (int i = 0; i < children.size(); i++) {
                Element child = (Element) children.get(i);
                traversePathway(child, rootLevelPathway);
            }
        }
    }

    /**
     * Categorizes the document into top-level components:  pathways,
     * interactions, and physical entities.
     */
    private void categorizeResources(Element e, Element rootLevelPathway) {

        //  First, separate out any OWL Specific Elements
        String namespaceUri = e.getNamespaceURI();
        if (namespaceUri.equals(OwlConstants.OWL_NAMESPACE_URI)) {
            ontologyList.add(e);
            return;
        }

        //  Get an RDF ID Attribute, if available
        Attribute idAttribute = e.getAttribute(RdfConstants.ID_ATTRIBUTE,
                RdfConstants.RDF_NAMESPACE);

        //  Set an RDF Pointer Attribute, if available
        Attribute pointerAttribute = e.getAttribute
                (RdfConstants.RESOURCE_ATTRIBUTE,
                        RdfConstants.RDF_NAMESPACE);

        if (idAttribute != null) {
            //  Store element to hashmap, keyed by RDF ID
            if (rdfResources.containsKey(idAttribute.getValue())) {
                errorList.add("Element:  " + e
                        + " declares RDF ID:  " + idAttribute.getValue()
                        + ", but a resource with this ID already exists.");
            } else {
                rdfResources.put(idAttribute.getValue(), e);
            }

            // If this is not a top-level element, it is implicitly
            // referenced via the XML hierarchy.  Therefore, add it to the
            // referenceSet
            if (!(e instanceof Parent)) {
                Element parent = (Element) e.getParent();
                if (!parent.getName().equals(RdfConstants.RDF_ROOT_NAME)) {
                    referenceSet.add(idAttribute.getValue());
                }
            }
        } else if (pointerAttribute != null) {
            // If we point to something, mark it in the referenceSet
            String uri = RdfUtil.removeHashMark(pointerAttribute.getValue());
            referenceSet.add(uri);
        }

        //  Categorize into separate bins
        String name = e.getName();
        if (bioPaxConstants.isPathway(name)) {
            pathwayList.add(e);
        } else if (bioPaxConstants.isInteraction((name))) {
            interactionList.add(e);
        } else if (bioPaxConstants.isPhysicalEntity(name)) {
            physicalEntityList.add(e);
        }

        //  Traverse through all children of current element
        List children = e.getChildren();
        for (int i = 0; i < children.size(); i++) {
            Element child = (Element) children.get(i);
            categorizeResources(child, rootLevelPathway);
        }
    }

    private void setPathwayMembership(Element e, Element rootLevelPathway) {
        if (e != null && rootLevelPathway != null) {
            Attribute elementIdAttribute = e.getAttribute
                    (RdfConstants.ID_ATTRIBUTE, RdfConstants.RDF_NAMESPACE);
            Attribute pathwayIdAttribute = rootLevelPathway.getAttribute
                    (RdfConstants.ID_ATTRIBUTE, RdfConstants.RDF_NAMESPACE);
            if (elementIdAttribute != null && pathwayIdAttribute != null) {
                String elementId = elementIdAttribute.getValue();
                String pathwayId = pathwayIdAttribute.getValue();

                //  Entity can be part of several pathways.
                if (pathwayMembershipMap.containsKey(elementId)) {
                    ArrayList list = (ArrayList)
                            pathwayMembershipMap.get(elementId);
                    if (!list.contains(pathwayId)) {
                        list.add(pathwayId);
                    }
                } else {
                    ArrayList list = new ArrayList();
                    list.add(pathwayId);
                    pathwayMembershipMap.put(elementId, list);
                }
            }
        }
    }

    /**
     * Validates that all RDF Links are valid.
     */
    private void validateResourceLinks(Element e) {
        //  Get an RDF Resource Attribute, if available
        Attribute resourceAttribute = e.getAttribute
                (RdfConstants.RESOURCE_ATTRIBUTE,
                        RdfConstants.RDF_NAMESPACE);

        //  Ignore all OWL Specific Elements
        String namespaceUri = e.getNamespaceURI();
        if (namespaceUri.equals(OwlConstants.OWL_NAMESPACE_URI)) {
            return;
        }

        if (resourceAttribute != null) {
            String key = RdfUtil.removeHashMark(resourceAttribute.getValue());
            if (!rdfResources.containsKey(key)) {
                errorList.add("Element:  " + e
                        + " references:  " + key + ", but no such resource "
                        + "exists in document.");
            }
        }

        //  Traverse through all children of current element
        List children = e.getChildren();
        for (int i = 0; i < children.size(); i++) {
            Element child = (Element) children.get(i);
            validateResourceLinks(child);
        }
    }
}
