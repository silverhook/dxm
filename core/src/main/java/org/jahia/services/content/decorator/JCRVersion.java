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
package org.jahia.services.content.decorator;

import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionWrapper;

import javax.jcr.RepositoryException;
import javax.jcr.UnsupportedRepositoryOperationException;
import javax.jcr.lock.Lock;
import javax.jcr.lock.LockException;
import javax.jcr.version.Version;
import java.util.Calendar;

/**
 * 
 * User: toto
 * Date: Mar 16, 2009
 * Time: 3:51:32 PM
 * 
 */
public class JCRVersion extends JCRNodeDecorator implements Version {
    public JCRVersion(JCRNodeWrapper node) {
        super(node);
    }

    public Version getRealNode() {
        return (Version) super.getRealNode();
    }

    public JCRVersionHistory getContainingHistory() throws RepositoryException {
        return (JCRVersionHistory) getProvider().getNodeWrapper(getRealNode().getContainingHistory(), getSession());
    }

    public Calendar getCreated() throws RepositoryException {
        return getRealNode().getCreated();
    }

    public JCRVersion[] getSuccessors() throws RepositoryException {
        Version[] versions = getRealNode().getSuccessors();
        JCRVersion[] jcrversions = new JCRVersion[versions.length];
        for (int i = 0; i < versions.length; i++) {
            jcrversions[i] = (JCRVersion) getProvider().getNodeWrapper(versions[i], getSession());
        }
        return jcrversions;
    }

    public JCRVersion[] getPredecessors() throws RepositoryException {
        Version[] versions = getRealNode().getPredecessors();
        JCRVersion[] jcrversions = new JCRVersion[versions.length];
        for (int i = 0; i < versions.length; i++) {
            jcrversions[i] = (JCRVersion) getProvider().getNodeWrapper(versions[i], getSession());
        }
        return jcrversions;
    }

    public JCRNodeWrapper getFrozenNode() throws RepositoryException {
        return getProvider().getNodeWrapper(getRealNode().getFrozenNode(), getSession());
    }

    public JCRVersion getLinearSuccessor() throws RepositoryException {
        Version linearSuccessor = getRealNode().getLinearSuccessor();
        return linearSuccessor != null ? (JCRVersion) getProvider().getNodeWrapper(linearSuccessor, (JCRSessionWrapper) getSession()) : null;
    }

    public JCRVersion getLinearPredecessor() throws RepositoryException {
        final Version linearPredecessor = getRealNode().getLinearPredecessor();
        return linearPredecessor != null ? (JCRVersion) getProvider().getNodeWrapper(linearPredecessor, getSession()) : null;
    }

    @Override
    public Lock getLock() throws RepositoryException {
        throw new LockException("Version node are not locakble");
    }

    @Override
    public void checkout() throws RepositoryException {
        throw new UnsupportedRepositoryOperationException("Version node could not be checkout");
    }
}
