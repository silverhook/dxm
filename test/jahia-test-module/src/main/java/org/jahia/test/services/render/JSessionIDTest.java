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
package org.jahia.test.services.render;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.jahia.bin.Jahia;
import org.jahia.settings.SettingsBean;
import org.jahia.test.JahiaTestCase;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * This test case verify that the jsessionid parameter is correctly removed from the URL
 */
public class JSessionIDTest extends JahiaTestCase {
    private HttpClient httpClient;
    private String jsessionid;

    @Before
    public void setUp() {
        httpClient = new HttpClient();
        jsessionid = "jsessionid";
    }

    @After
    public void tearDown() {

    }

    @Test
    public void testJsessionIdExists() throws Exception {
        findJSessionId(false);

    }

    @Test
    public void testJsessionIdRemoved() throws Exception {
        findJSessionId(true);
    }

//    public void testJsessionIdWithCache() throws Exception {
//        SettingsBean.getInstance().setDisableJsessionIdParameter(false);
//        SettingsBean.getInstance().setJsessionIdParameterName(jsessionid);
//        httpClient.getParams().setCookiePolicy(CookiePolicy.IGNORE_COOKIES);
//
//        GetMethod displayLoginMethod = new GetMethod(getBaseServerURL() + Jahia.getContextPath()+"/administration");
//        int result = httpClient.executeMethod(displayLoginMethod);
//        String responseBodyAsString = displayLoginMethod.getResponseBodyAsString();
//
//        Pattern p = Pattern.compile("action=\"([^\"]*)\"");
//        Matcher m = p.matcher(responseBodyAsString);
//        assertTrue (m.find());
//
//        String url = m.group(1);
//
//        p = Pattern.compile("name=\"redirect\" value=\"([^\"]*)\"");
//        m = p.matcher(responseBodyAsString);
//        assertTrue (m.find());
//
//        String redirect = m.group(1);
//        PostMethod login = new PostMethod(getBaseServerURL() + url);
//        login.setParameter("username","root");
//        login.setParameter("password","root1234");
//        login.setParameter("doLogin","true");
//        login.setParameter("redirect",redirect);
//        result = httpClient.executeMethod(login);
//
//        String sessionid = StringUtils.substringAfter(login.getURI().toString(), ";"+jsessionid);
//        assertNotNull("Session id is not in the url !",sessionid);
//
//        GetMethod home = new GetMethod(getBaseServerURL() + Jahia.getContextPath()+"/cms/render/live/en/users/root.user-home.html;"+jsessionid+sessionid);
//        result = httpClient.executeMethod(home);
//        System.out.println(home.getResponseBodyAsString());
//        System.out.println("ok");
//    }

    private void findJSessionId(boolean removeJsessionId) throws IOException {
        SettingsBean.getInstance().setDisableJsessionIdParameter(removeJsessionId);
        SettingsBean.getInstance().setJsessionIdParameterName(jsessionid);

        GetMethod displayLoginMethod = new GetMethod(getBaseServerURL() + Jahia.getContextPath()+"/start");
        int statusCode = httpClient.executeMethod(displayLoginMethod);
        
        assertEquals("Method failed: " + displayLoginMethod.getStatusLine(), HttpStatus.SC_UNAUTHORIZED, statusCode);
        
        String responseBodyAsString = displayLoginMethod.getResponseBodyAsString();

        Pattern p = Pattern.compile("action=\"([^\"]*)\"");
        Matcher m = p.matcher(responseBodyAsString);
        assertTrue (m.find());

        String url = m.group(1);

        assertEquals("jsession ID is not "+(removeJsessionId?"removed":"present")+" in administration login url.", removeJsessionId, !url.contains("jsessionid"));
    }

}
