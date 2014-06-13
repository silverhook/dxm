/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *     Copyright (C) 2002-2014 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ======================================================================================
 *
 *     IF YOU DECIDE TO CHOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     "This program is free software; you can redistribute it and/or
 *     modify it under the terms of the GNU General Public License
 *     as published by the Free Software Foundation; either version 2
 *     of the License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program; if not, write to the Free Software
 *     Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 *     As a special exception to the terms and conditions of version 2.0 of
 *     the GPL (or any later version), you may redistribute this Program in connection
 *     with Free/Libre and Open Source Software ("FLOSS") applications as described
 *     in Jahia's FLOSS exception. You should have received a copy of the text
 *     describing the FLOSS exception, also available here:
 *     http://www.jahia.com/license"
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ======================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 *
 *
 * ==========================================================================================
 * =                                   ABOUT JAHIA                                          =
 * ==========================================================================================
 *
 *     Rooted in Open Source CMS, Jahia’s Digital Industrialization paradigm is about
 *     streamlining Enterprise digital projects across channels to truly control
 *     time-to-market and TCO, project after project.
 *     Putting an end to “the Tunnel effect”, the Jahia Studio enables IT and
 *     marketing teams to collaboratively and iteratively build cutting-edge
 *     online business solutions.
 *     These, in turn, are securely and easily deployed as modules and apps,
 *     reusable across any digital projects, thanks to the Jahia Private App Store Software.
 *     Each solution provided by Jahia stems from this overarching vision:
 *     Digital Factory, Workspace Factory, Portal Factory and eCommerce Factory.
 *     Founded in 2002 and headquartered in Geneva, Switzerland,
 *     Jahia Solutions Group has its North American headquarters in Washington DC,
 *     with offices in Chicago, Toronto and throughout Europe.
 *     Jahia counts hundreds of global brands and governmental organizations
 *     among its loyal customers, in more than 20 countries across the globe.
 *
 *     For more information, please visit http://www.jahia.com
 */
package org.jahia.services.content.nodetypes;

import org.apache.commons.lang.StringUtils;
import org.jahia.api.Constants;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.utils.Patterns;

import javax.jcr.RepositoryException;
import javax.jcr.nodetype.NoSuchNodeTypeException;
import java.util.*;

/**
 * Help to find allowed sub node types and references
 */
public class ConstraintsHelper {

    public static String getConstraints(JCRNodeWrapper node) throws RepositoryException {
        return getConstraints(node, null);
    }

    public static String getConstraints(JCRNodeWrapper node, String targetChildName) throws RepositoryException {
        List<ExtendedNodeType> list = new ArrayList<ExtendedNodeType>(Arrays.asList(node.getMixinNodeTypes()));
        list.add(node.getPrimaryNodeType());
        return getConstraints(list, targetChildName);
    }

    public static String getConstraints(List<ExtendedNodeType> nodeType, String targetChildName) {
        StringBuilder constraints = new StringBuilder();
        Set<String> nodeTypeNames = new HashSet<String>();

        if (targetChildName == null) {
            // first let's retrieve the child node types for unstructured node definitions
            for (ExtendedNodeType type : nodeType) {
                Set<String> cons = type.getUnstructuredChildNodeDefinitions().keySet();
                for (String s : cons) {
                    if (!s.equals(Constants.JAHIANT_TRANSLATION) &&
                            !s.equals(Constants.JAHIANT_REFERENCEINFIELD) &&
                            !nodeTypeNames.contains(s)) {
                        if (constraints.length() > 0) {
                            constraints.append(' ');
                        }
                        constraints.append(s);
                        nodeTypeNames.add(s);
                    }
                }
            }
        }

        if (targetChildName != null) {
            // now let's retrieve the structured child node definitions
            for (ExtendedNodeType type : nodeType) {
                Map<String, ExtendedNodeDefinition> nodeDefinitions = type.getChildNodeDefinitionsAsMap();
                for (Map.Entry<String, ExtendedNodeDefinition> nodeDefinitionEntry : nodeDefinitions.entrySet()) {
                    String childName = nodeDefinitionEntry.getKey();
                    boolean useCurrentChild = true;
                    if (targetChildName != null && !targetChildName.equals("*")) {
                        if (!childName.equals(targetChildName)) {
                            useCurrentChild = false;
                        }
                    }
                    if (useCurrentChild) {
                        String[] typeNames = nodeDefinitionEntry.getValue().getRequiredPrimaryTypeNames();
                        for (String typeName : typeNames) {
                            if (!typeName.equals("jnt:conditionalVisibility") &&
                                    !typeName.equals("jnt:vanityUrls") &&
                                    !typeName.equals("jnt:acl") &&
                                    !nodeTypeNames.contains(typeName)) {
                                if (constraints.length() > 0) {
                                    constraints.append(' ');
                                }
                                constraints.append(typeName);
                                nodeTypeNames.add(typeName);
                            }
                        }
                    }
                }
            }
        }
        return constraints.toString();
    }

    /**
     * Builds a list of usable reference types for the specified node types and constraints. If the no node types
     * were specified it will default to the nt:base node type.
     * <p/>
     * This method will also inspect all the value constraints that are set on all reference nodes and if there
     * are none it will use the default jmix:droppableContent type.
     *
     * @param constraints
     * @param nodeTypes
     * @return
     * @throws NoSuchNodeTypeException
     */
    public static String getReferenceTypes(String constraints, String nodeTypes) throws NoSuchNodeTypeException {
        StringBuffer buffer = new StringBuffer();
        List<ExtendedNodeType> refs =
                NodeTypeRegistry.getInstance().getNodeType("jmix:nodeReference").getSubtypesAsList();

        List<ExtendedNodeType> nodeTypesList = new ArrayList<ExtendedNodeType>();
        List<ExtendedNodeType> referencesNodeTypesList = new ArrayList<ExtendedNodeType>();

        if (StringUtils.isEmpty(nodeTypes)) {
            nodeTypesList.add(NodeTypeRegistry.getInstance().getNodeType("nt:base"));
        } else {
            for (String s : Patterns.SPACE.split(nodeTypes)) {
                ExtendedNodeType nt = NodeTypeRegistry.getInstance().getNodeType(s);
                if (nt.isNodeType("jmix:nodeReference")) {
                    referencesNodeTypesList.add(nt);
                } else {
                    nodeTypesList.add(nt);
                }
            }
        }
        final String[] constraintsArray = Patterns.SPACE.split(constraints);
        for (ExtendedNodeType ref : refs) {
            if (ref.getPropertyDefinitionsAsMap().get("j:node") != null) {
                for (String s : constraintsArray) {
                    if (s.equals("nt:base") && constraintsArray.length > 1) {
                        continue;
                    }
                    if (ref.isNodeType(s)) {
                        String[] refConstraints = ref.getPropertyDefinitionsAsMap().get("j:node").getValueConstraints();
                        if (refConstraints.length == 0) {
                            refConstraints = new String[]{"jmix:droppableContent"};
                        }
                        List<String> finalConstraints = new ArrayList<String>();
                        for (String refConstraint : refConstraints) {
                            for (ExtendedNodeType nt : nodeTypesList) {
                                // if a node type is descending from a reference constraint, then use the node type
                                if (nt.isNodeType(refConstraint)) {
                                    finalConstraints.add(nt.getName());
                                    // otherwise if the reference constraint is descending from a node type, use the constraint
                                } else if (NodeTypeRegistry.getInstance().getNodeType(refConstraint).isNodeType(nt.getName())) {
                                    finalConstraints.add(refConstraint);
                                }
                            }
                        }

                        refConstraints = finalConstraints.toArray(new String[finalConstraints.size()]);
                        if (refConstraints.length > 0) {
                            buffer.append(ref.getName());
                            buffer.append("[");
                            if (refConstraints.length > 0) {
                                for (int i = 0; i < refConstraints.length; i++) {
                                    buffer.append(refConstraints[i]);
                                    if (i + 1 < refConstraints.length) {
                                        buffer.append(",");
                                    }
                                }
                            }
                            buffer.append("] ");
                        }
                        break;
                    }
                }
            }
        }
        for (ExtendedNodeType ref : referencesNodeTypesList) {
            if (ref.getPropertyDefinitionsAsMap().get("j:node") != null) {
                String[] refConstraints = ref.getPropertyDefinitionsAsMap().get("j:node").getValueConstraints();
                buffer.append(ref.getName());
                buffer.append("[");
                if (refConstraints.length > 0) {
                    for (int i = 0; i < refConstraints.length; i++) {
                        buffer.append(refConstraints[i]);
                        if (i + 1 < refConstraints.length) {
                            buffer.append(",");
                        }
                    }
                }
                buffer.append("] ");
            }
        }
        return buffer.toString().trim();
    }
}
