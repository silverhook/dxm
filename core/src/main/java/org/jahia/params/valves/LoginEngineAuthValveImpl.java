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
package org.jahia.params.valves;

import org.jahia.api.Constants;
import org.jahia.bin.Login;
import org.jahia.pipelines.PipelineException;
import org.jahia.pipelines.valves.ValveContext;
import org.jahia.services.SpringContextSingleton;
import org.jahia.services.content.decorator.JCRUserNode;
import org.jahia.services.preferences.user.UserPreferencesHelper;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.usermanager.JahiaUserManagerService;
import org.jahia.settings.SettingsBean;
import org.jahia.utils.LanguageCodeConverters;
import org.jahia.utils.Patterns;
import org.slf4j.Logger;
import org.springframework.context.ApplicationEvent;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.*;

/**
 * @author Thomas Draier
 */
public class LoginEngineAuthValveImpl extends BaseAuthValve {
    public static final String ACCOUNT_LOCKED = "account_locked";
    public static final String BAD_PASSWORD = "bad_password";
    private static final transient Logger logger = org.slf4j.LoggerFactory.getLogger(LoginEngineAuthValveImpl.class);
    public static final String LOGIN_TAG_PARAMETER = "doLogin";
    public static final String OK = "ok";
    public static final String UNKNOWN_USER = "unknown_user";
    public static final String USE_COOKIE = "useCookie";
    public static final String VALVE_RESULT = "login_valve_result";

    private CookieAuthConfig cookieAuthConfig;

    private boolean fireLoginEvent = false;
    private String preserveSessionAttributes = null;
    private JahiaUserManagerService userManagerService;

    public void setFireLoginEvent(boolean fireLoginEvent) {
        this.fireLoginEvent = fireLoginEvent;
    }

    public class LoginEvent extends ApplicationEvent {
        private static final long serialVersionUID = -7356560804745397662L;
        
        private JahiaUser jahiaUser;
        private AuthValveContext authValveContext;

        public LoginEvent(Object source, JahiaUser jahiaUser, AuthValveContext authValveContext) {
            super(source);
            this.jahiaUser = jahiaUser;
            this.authValveContext = authValveContext;
        }

        public JahiaUser getJahiaUser() {
            return jahiaUser;
        }

        public AuthValveContext getAuthValveContext() {
            return authValveContext;
        }
    }

    public void setPreserveSessionAttributes(String preserveSessionAttributes) {
        this.preserveSessionAttributes = preserveSessionAttributes;
    }

    private void enforcePasswordPolicy(JCRUserNode theUser) {
//        PolicyEnforcementResult evalResult = ServicesRegistry.getInstance().getJahiaPasswordPolicyService().
//                enforcePolicyOnLogin(theUser);
//        if (!evalResult.isSuccess()) {
//            EngineMessages policyMsgs = evalResult.getEngineMessages();
//            EngineMessages resultMessages = new EngineMessages();
//            for (Object o : policyMsgs.getMessages()) {
//                resultMessages.add((EngineMessage) o);
//            }
//        }
    }

    public void invoke(Object context, ValveContext valveContext) throws PipelineException {
        if (!isEnabled()) {
            valveContext.invokeNext(context);
            return;
        }
        
        final AuthValveContext authContext = (AuthValveContext) context;
        final HttpServletRequest httpServletRequest = authContext.getRequest();

        JCRUserNode theUser = null;
        boolean ok = false;

        if (isLoginRequested(httpServletRequest)) {

            final String username = httpServletRequest.getParameter("username");
            final String password = httpServletRequest.getParameter("password");
            final String site = httpServletRequest.getParameter("site");

            if ((username != null) && (password != null)) {
                // Check if the user has site access ( even though it is not a user of this site )
                theUser = userManagerService.lookupUser(username,site);
                if (theUser != null) {
                    if (theUser.verifyPassword(password)) {
                        if (!theUser.isAccountLocked()) {
                            ok = true;
                        } else {
                            logger.warn("Login failed: account for user " + theUser.getName() + " is locked.");
                            httpServletRequest.setAttribute(VALVE_RESULT, ACCOUNT_LOCKED);
                        }
                    } else {
                        logger.warn("Login failed: user " + theUser.getName() + " provided bad password.");
                        httpServletRequest.setAttribute(VALVE_RESULT, BAD_PASSWORD);
                    }
                } else {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Login failed. Unknown username " + username + ".");
                    }
                    httpServletRequest.setAttribute(VALVE_RESULT, UNKNOWN_USER);
                }
            }
        }
        if (ok) {
            if (logger.isDebugEnabled()) {
                logger.debug("User " + theUser + " logged in.");
            }

            // if there are any attributes to conserve between session, let's copy them into a map first
            Map<String, Object> savedSessionAttributes = preserveSessionAttributes(httpServletRequest);

            if (httpServletRequest.getSession(false) != null) {
                httpServletRequest.getSession().invalidate();
            }

            // if there were saved session attributes, we restore them here.
            restoreSessionAttributes(httpServletRequest, savedSessionAttributes);

            httpServletRequest.setAttribute(VALVE_RESULT, OK);
            authContext.getSessionFactory().setCurrentUser(theUser.getJahiaUser());

            // do a switch to the user's preferred language
            if (SettingsBean.getInstance().isConsiderPreferredLanguageAfterLogin()) {
                Locale preferredUserLocale = UserPreferencesHelper.getPreferredLocale(theUser, LanguageCodeConverters.resolveLocaleForGuest(httpServletRequest));
                httpServletRequest.getSession().setAttribute(Constants.SESSION_LOCALE, preferredUserLocale);
            }

            String useCookie = httpServletRequest.getParameter(USE_COOKIE);
            if ((useCookie != null) && ("on".equals(useCookie))) {
                // the user has indicated he wants to use cookie authentication
                CookieAuthValveImpl.createAndSendCookie(authContext, theUser, cookieAuthConfig);
            }

            enforcePasswordPolicy(theUser);
            // The following was deactivated for performance reasons. We should instead look at doing this with Camel
            // or some other asynchronous way.
            //theUser.setProperty(Constants.JCR_LASTLOGINDATE,
            //        String.valueOf(System.currentTimeMillis()));

            if (fireLoginEvent) {
                SpringContextSingleton.getInstance().publishEvent(new LoginEvent(this, theUser.getJahiaUser(), authContext));
            }

        } else {
            valveContext.invokeNext(context);
        }
    }

    private Map<String, Object> preserveSessionAttributes(HttpServletRequest httpServletRequest) {
        Map<String,Object> savedSessionAttributes = new HashMap<String,Object>();
        if ((preserveSessionAttributes != null) &&
            (httpServletRequest.getSession(false) != null) &&
                (preserveSessionAttributes.length() > 0)) {
            String[] sessionAttributeNames = Patterns.TRIPLE_HASH.split(preserveSessionAttributes);
            HttpSession session = httpServletRequest.getSession(false);
            for (String sessionAttributeName : sessionAttributeNames) {
                Object attributeValue = session.getAttribute(sessionAttributeName);
                if (attributeValue != null) {
                    savedSessionAttributes.put(sessionAttributeName, attributeValue);
                }
            }
        }
        return savedSessionAttributes;
    }

    private void restoreSessionAttributes(HttpServletRequest httpServletRequest, Map<String, Object> savedSessionAttributes) {
        if (savedSessionAttributes.size() > 0) {
            HttpSession session = httpServletRequest.getSession();
            for (Map.Entry<String,Object> savedSessionAttribute : savedSessionAttributes.entrySet()) {
                session.setAttribute(savedSessionAttribute.getKey(), savedSessionAttribute.getValue());
            }
        }
    }

    protected boolean isLoginRequested(HttpServletRequest request) {
        String doLogin = request.getParameter(LOGIN_TAG_PARAMETER);
        if (doLogin != null) {
            return Boolean.valueOf(doLogin) || "1".equals(doLogin);
        } else if ("/cms".equals(request.getServletPath())) {
            return Login.getMapping().equals(request.getPathInfo());
        }

        return false;
    }

    public void setCookieAuthConfig(CookieAuthConfig cookieAuthConfig) {
        this.cookieAuthConfig = cookieAuthConfig;
    }

    public void setUserManagerService(JahiaUserManagerService userManagerService) {
        this.userManagerService = userManagerService;
    }

}
