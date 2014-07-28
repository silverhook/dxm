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
package org.jahia.test.services.usermanager;

import org.jahia.registries.ServicesRegistry;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.decorator.JCRGroupNode;
import org.jahia.services.content.decorator.JCRUserNode;
import org.jahia.services.usermanager.JahiaGroupManagerService;
import org.jahia.services.usermanager.JahiaUserManagerService;
import org.junit.*;
import org.slf4j.Logger;

import java.util.Collection;
import java.util.List;
import java.util.Properties;

import static org.junit.Assert.*;

/**
 * Group manager unit test. This unit test is not yet complete, it was only implemented to test some marginal cases.
 */
public class JahiaGroupManagerServiceTest {

    private static Logger logger = org.slf4j.LoggerFactory.getLogger(JahiaGroupManagerServiceTest.class);

    private static JahiaUserManagerService userManager;
    private static JahiaGroupManagerService groupManager;

    private static JCRUserNode user1;
    private static JCRUserNode user2;

    @BeforeClass
    public static void oneTimeSetUp() throws Exception {
        userManager = ServicesRegistry.getInstance().getJahiaUserManagerService();
        assertNotNull("JahiaUserManagerService cannot be retrieved", userManager);
        groupManager = ServicesRegistry.getInstance().getJahiaGroupManagerService();
        assertNotNull("JahiaGroupManagerService cannot be retrieved", groupManager);

        JCRSessionWrapper session = JCRSessionFactory.getInstance().getCurrentUserSession();

        // now let's create some users and groups.
        user1 = userManager.createUser("test-user1", "password", new Properties(), session);
        user2 = userManager.createUser("test-user2", "password", new Properties(), session);
        session.save();
    }

    @AfterClass
    public static void oneTimeTearDown() throws Exception {
        JCRSessionWrapper session = JCRSessionFactory.getInstance().getCurrentUserSession();
        userManager.deleteUser(user1.getPath(), session);
        userManager.deleteUser(user2.getPath(), session);
        session.save();
    }


    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
        JCRSessionFactory.getInstance().closeAllSessions();
        JCRGroupNode group = groupManager.lookupGroup(null, "test-group1");
        JCRSessionWrapper session = JCRSessionFactory.getInstance().getCurrentUserSession();
        if (group != null) {
            groupManager.deleteGroup(group.getPath(), session);
        }

        group = groupManager.lookupGroup(null, "test-group2");
        if (group != null) {
            groupManager.deleteGroup(group.getPath(), session);
        }

        group = groupManager.lookupGroup(null, "test-user1");
        if (group != null) {
            groupManager.deleteGroup(group.getPath(), session);
        }
        session.save();
        JCRSessionFactory.getInstance().closeAllSessions();
    }

    @Test
    public void testGroupDelete() throws Exception {
        JCRSessionWrapper session = JCRSessionFactory.getInstance().getCurrentUserSession();

        JCRGroupNode group1 = groupManager.createGroup(null, "test-group1", new Properties(), false, session);
        group1.addMember(user1);
        JCRGroupNode group2 = groupManager.createGroup(null, "test-group2", new Properties(), false, session);
        group2.addMember(user2);
        group2.addMember(group1);

        session.save();

        groupManager.deleteGroup(group2.getPath(), session);
        groupManager.deleteGroup(group1.getPath(), session);

        session.save();

        JCRSessionFactory.getInstance().closeAllSessions();

        group1 = groupManager.lookupGroup("test-group1:0");
        assertNull("Group 1 should have been deleted but is still available !", group1);
        group2 = groupManager.lookupGroup("test-group2:0");
        assertNull("Group 1 should have been deleted but is still available !", group2);

    }

    @Test
    public void testGroupMembership() throws Exception {
        JCRSessionWrapper session = JCRSessionFactory.getInstance().getCurrentUserSession();

        JCRGroupNode group1 = groupManager.createGroup(null, "test-group1", new Properties(), false, session);
        group1.addMember(user1);
        JCRGroupNode group2 = groupManager.createGroup(null, "test-group2", new Properties(), false, session);
        group2.addMember(user2);
        group2.addMember(group1);
        session.save();

        assertTrue("User 1 should be a transitive member of group2, as group1 is a member of group 2",
                user1.isMemberOfGroup(null, "test-group2"));
        List<String> user1GroupMembership = groupManager.getUserMembership(user1.getPath());
        assertTrue("User 1 should be a transitive member of group2, as group1 is a member of group 2",
                user1GroupMembership.contains("test-group2:0"));

        group1.removeMember(user1);
        assertFalse("User 1 should no longer be a transitive member of group2, as we have just removed it.",
                user1.isMemberOfGroup(null, "test-group2"));
        user1GroupMembership = groupManager.getUserMembership(user1.getPath());
        assertFalse("User 1 should no longer be a transitive member of group2, as we have just removed it.",
                user1GroupMembership.contains("test-group2:0"));

        groupManager.deleteGroup(group2.getPath(), session);
        groupManager.deleteGroup(group1.getPath(), session);

        session.save();

    }

    @Test
    public void testSameNameUserAndGroup() throws Exception {
        JCRSessionWrapper session = JCRSessionFactory.getInstance().getCurrentUserSession();

        JCRGroupNode group1 = groupManager.createGroup(null, "test-group1", new Properties(), false, session);
        group1.addMember(user1);
        JCRGroupNode user1Group = groupManager.createGroup(null, "test-user1", new Properties(), false, session);
        group1.addMember(user1Group);
        session.save();
        group1 = groupManager.lookupGroup("test-group1:0");
        Collection<JCRNodeWrapper> members = group1.getMembers();

        assertTrue("Test group 1 should contain user called 'test-user1'", members.contains(user1));
        assertTrue("Test group 1 should contain group called 'test-user1'", members.contains(user1Group));

        groupManager.deleteGroup(user1Group.getPath(), session);
        groupManager.deleteGroup(group1.getPath(), session);

        session.save();
    }
}
