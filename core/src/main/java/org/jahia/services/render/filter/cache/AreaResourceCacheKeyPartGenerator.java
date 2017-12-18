/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2018 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ===================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 */
package org.jahia.services.render.filter.cache;

import org.apache.commons.lang.StringUtils;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

 /*
 * Cache key used to set the areaResource attribute in the request
 * areaResource is the content generated by the template:area tag or
 * the component jnt:area
 */
public class AreaResourceCacheKeyPartGenerator implements CacheKeyPartGenerator, RenderContextTuner {

    public static final String AREA_PATH = "org.jahia.areaPath";
    public static final String SAVED_AREA_PATH = "org.jahia.savedAreaPath";

    private static final Logger logger = LoggerFactory.getLogger(AclCacheKeyPartGenerator.class);

    private boolean disabled;

    @Override
    public String getKey() {
        return AREA_PATH;
    }

    @Override
    public String getValue(Resource resource, RenderContext renderContext, Properties properties) {
        if (isDisabled()) {
            return "";
        }
        // area path is defined by the tag area or the parent fragment
        String areaPath = (String) renderContext.getRequest().getAttribute(AREA_PATH);
        String savedAreaPath = (String) renderContext.getRequest().getAttribute(SAVED_AREA_PATH);
        if (logger.isDebugEnabled()) {
            logger.debug("READ value - areaPath: {} savedAreaPath: {} resource: {}", new String[] {areaPath, savedAreaPath, resource.getPath()});
        }
        if (areaPath != null) {
            return areaPath;
        } else if (savedAreaPath != null) {
            return savedAreaPath;
        }
        return "";
    }

    @Override
    public String replacePlaceholders(RenderContext renderContext, String keyPart) {
        return keyPart;
    }

    @Override
    public Object prepareContextForContentGeneration(String value, Resource resource, RenderContext renderContext) {
        if (!isDisabled() && StringUtils.isNotEmpty(value)) {
            // set the request attribute for the next render chain
            renderContext.getRequest().setAttribute(SAVED_AREA_PATH, value);
            return value;
        }
        return null;
    }

    @Override
    public void restoreContextAfterContentGeneration(String value, Resource resource, RenderContext renderContext, Object original) {
        if (!isDisabled() && original != null) {
            renderContext.getRequest().removeAttribute(SAVED_AREA_PATH);
        }
    }

    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }

    public boolean isDisabled() {
        return disabled;
    }
}
