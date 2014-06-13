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
package org.jahia.taglibs.jcr.node;

import org.jahia.data.templates.JahiaTemplatesPackage;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.content.JCRContentUtils;
import org.jahia.utils.i18n.Messages;
import org.jahia.utils.i18n.ResourceBundles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.nodetypes.*;
import org.jahia.services.content.nodetypes.initializers.ChoiceListInitializer;
import org.jahia.services.content.nodetypes.initializers.ChoiceListInitializerService;
import org.jahia.services.content.nodetypes.initializers.ChoiceListValue;
import org.jahia.taglibs.AbstractJahiaTag;
import org.jahia.utils.Patterns;

import javax.jcr.RepositoryException;
import javax.servlet.jsp.JspException;
import java.util.*;

/**
 * This Tag allows access to specific property of a node.
 * <p/>
 *
 * @author cmailleux
 */
public class JCRPropertyInitializerTag extends AbstractJahiaTag {
    private static final long serialVersionUID = 3235254134426302521L;
    private transient static Logger logger = LoggerFactory.getLogger(JCRPropertyInitializerTag.class);
    private JCRNodeWrapper node;
    private String nodeType;
    private String name;
    private String var;
    private String initializers;

    public void setNode(JCRNodeWrapper node) {
        this.node = node;
    }

    public void setNodeType(String nodeType) {
        this.nodeType = nodeType;
    }

    /**
     * Default processing of the start tag, returning SKIP_BODY.
     *
     * @return SKIP_BODY
     * @throws javax.servlet.jsp.JspException if an error occurs while processing this tag
     * @see javax.servlet.jsp.tagext.Tag#doStartTag()
     */
    @Override
    public int doStartTag() throws JspException {
        return SKIP_BODY;
    }

    /**
     * Default processing of the end tag returning EVAL_PAGE.
     *
     * @return EVAL_PAGE
     * @throws javax.servlet.jsp.JspException if an error occurs while processing this tag
     * @see javax.servlet.jsp.tagext.Tag#doEndTag()
     */
    @Override
    public int doEndTag() throws JspException {
        try {
            ExtendedNodeType type = null;
            if (nodeType != null) {
                type = NodeTypeRegistry.getInstance().getNodeType(nodeType);
            } else if (node != null) {
                type = node.getPrimaryNodeType();
            }
            if (type != null) {
                final List<ExtendedItemDefinition> extendedItemDefinitionList = type.getItems();
                for (ExtendedItemDefinition definition : extendedItemDefinitionList) {
                    if (definition.getName().equals(name)) {
                        Map<String, String> map;
                        if(initializers==null) {
                            map = definition.getSelectorOptions();
                        } else {
                            map = new LinkedHashMap<String, String>();
                            String[] strings = Patterns.COMMA.split(initializers);
                            for (String string : strings) {
                                map.put(string, "");
                            }
                        }
                        if (map.size() > 0) {
                            final Map<String, ChoiceListInitializer> initializers = ChoiceListInitializerService.getInstance().getInitializers();
                            List<ChoiceListValue> listValues = null;
                            final HashMap<String, Object> context = new HashMap<String, Object>();
                            context.put("contextNode",node);
                            for (Map.Entry<String, String> entry : map.entrySet()) {
                                if (initializers.containsKey(entry.getKey())) {
                                    listValues = initializers.get(entry.getKey()).getChoiceListValues(
                                            (ExtendedPropertyDefinition) definition, entry.getValue(), listValues,
                                            getRenderContext().getMainResourceLocale(), context
                                    );
                                }
                            }
                            if (listValues != null) {
                                pageContext.setAttribute(var, listValues);
                            }
                        } else if (definition instanceof ExtendedPropertyDefinition) {
                            JahiaTemplatesPackage pkg = ((ExtendedPropertyDefinition) definition).getDeclaringNodeType().getTemplatePackage();
                            ResourceBundle rb = ResourceBundles.get(pkg != null ? pkg : ServicesRegistry.getInstance().getJahiaTemplateManagerService().getTemplatePackageById("default"), getRenderContext().getMainResourceLocale());
                            final ExtendedPropertyDefinition propertyDefinition = (ExtendedPropertyDefinition) definition;
                            List<ChoiceListValue> listValues = new ArrayList<ChoiceListValue>();
                            String resourceBundleKey = definition.getResourceBundleKey();
                            for (String value : propertyDefinition.getValueConstraints()) {
                                String display = Messages.get(rb, resourceBundleKey + "." + JCRContentUtils.replaceColon(value), value);
                                listValues.add(new ChoiceListValue(display, null, new ValueImpl(value, propertyDefinition.getRequiredType())));
                            }
                            pageContext.setAttribute(var, listValues);
                        }
                    }
                }

            }
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        } finally {
            resetState();
        }
        return EVAL_PAGE;
    }

    /**
     * Specify the name of the property you want to get value of.
     *
     * @param name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * If you do not want to output directly the value of the property (call javax.jcr.Value.getString())
     * The define a value for this.
     *
     * @param var The name in the pageContext in which you will find the javax.jcr.Value or javax.jcr.Value[] object associated with this property
     */
    public void setVar(String var) {
        this.var = var;
    }

    public void setInitializers(String initializers) {
        this.initializers = initializers;
    }

    @Override
    protected void resetState() {
        name = null;
        node = null;
        nodeType = null;
        var = null;
        super.resetState();
    }
}