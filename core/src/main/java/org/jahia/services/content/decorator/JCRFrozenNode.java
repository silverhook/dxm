/**
 *
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Limited. All rights reserved.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 * As a special exception to the terms and conditions of version 2.0 of
 * the GPL (or any later version), you may redistribute this Program in connection
 * with Free/Libre and Open Source Software ("FLOSS") applications as described
 * in Jahia's FLOSS exception. You should have recieved a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license"
 *
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Limited. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */
package org.jahia.services.content.decorator;

import org.apache.log4j.Logger;
import org.jahia.api.Constants;
import org.jahia.bin.Jahia;
import org.jahia.data.files.JahiaFile;
import org.jahia.data.files.JahiaFileField;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.content.JCRNodeWrapper;

import javax.jcr.RepositoryException;
import java.util.Properties;

/**
 * Created by IntelliJ IDEA.
 *
 * @author : rincevent
 * @since : JAHIA 6.1
 *        Created : 6 juil. 2009
 */
public class JCRFrozenNode extends JCRNodeDecorator {
    private transient static Logger logger = Logger.getLogger(JCRFrozenNode.class);
    protected Exception exception = null;

    public JCRFrozenNode(JCRNodeWrapper node) {
        super(node);
    }

    @Override
    public String getUrl() {
        String frozenPrimaryType = getPropertyAsString("jcr:frozenPrimaryType");
        if (frozenPrimaryType.equals(Constants.JAHIANT_FILE)) {
            try {
                return getProvider().getHttpPath() + this.getPropertyAsString("j:fullpath") + "?v=" + getParent().getName();
            } catch (RepositoryException e) {
                logger.error("Error while retrieving fullpath property", e);
            }

        }
        return super.getUrl();
    }

    @Override
    public JahiaFileField getJahiaFileField() {
        JahiaFileField fField;
        String uri;
        uri = this.getPropertyAsString("j:fullpath");
        String owner = "root:0";

        String contentType = "application/binary";
        int lastDot = uri.lastIndexOf(".");
        if (lastDot > -1) {
            String mimeType = Jahia.getStaticServletConfig().getServletContext().getMimeType(uri.substring(uri.lastIndexOf("/") + 1).toLowerCase());
            if (mimeType != null) {
                contentType = mimeType;
            }
        }

        JahiaFile file = null;
        try {
            file = new JahiaFile(-1, // filemanager id
                    -1, // folder id
                    owner,
                    uri + "?v=" + getParent().getName(), // realname
                    getStorageName(), // storage name
                    System.currentTimeMillis(), // modif date
                    getFileContent().getContentLength(), // size
                    contentType, // type
                    getName(), // title
                    "", // descr
                    String.valueOf(ServicesRegistry.getInstance()
                            .getJahiaVersionService().getCurrentVersionID()), // version
                    JahiaFile.STATE_ACTIVE);
        } catch (RepositoryException e) {
            e.printStackTrace();
        }
        fField = new JahiaFileField(file, new Properties());
        fField.setID(0);
        fField.setDownloadUrl(getUrl());
        fField.setThumbnailUrl(getThumbnailUrl("thumbnail"));
        try {
            if (hasProperty("j:width") && hasProperty("j:height")) {
                fField.setOrientation(getProperty("j:width").getLong() >= getProperty("j:height").getLong() ? "landscape" : "portrait");
            }
        } catch (RepositoryException e) {
            logger.debug("Can't get orientation", e);
        }
        return fField;
    }
}
