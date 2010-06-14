/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Solutions Group SA. All rights reserved.
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
 * in Jahia's FLOSS exception. You should have received a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license
 *
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Solutions Group SA. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */
package org.jahia.taglibs.functions;

import net.htmlparser.jericho.Source;
import net.htmlparser.jericho.TextExtractor;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.jahia.bin.Jahia;
import org.jahia.params.ProcessingContext;
import org.jahia.services.content.JCRContentUtils;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.notification.SubscriptionService;
import org.jahia.services.rbac.PermissionIdentity;
import org.jahia.services.rbac.RoleIdentity;
import org.jahia.services.usermanager.JahiaUser;

import javax.jcr.RangeIterator;
import javax.servlet.jsp.JspTagException;
import java.util.*;

/**
 * Custom functions, which are exposed into the template scope.
 *
 * @author Sergiy Shyrkov
 */
public class Functions {

    public static String attributes(Map<String, Object> attributes) {
        StringBuilder out = new StringBuilder();

        for (Map.Entry<String, Object> attr : attributes.entrySet()) {
            out.append(attr.getKey()).append("=\"").append(
                    attr.getValue() != null ? StringEscapeUtils.escapeXml(attr
                            .getValue().toString()) : "").append("\" ");
        }

        return out.toString();
    }

    public static Object defaultValue(Object value, Object defaultValue) {
        return (value != null && (!(value instanceof String) || (((String) value)
                .length() > 0))) ? value : defaultValue;
    }

    public static String removeHtmlTags(String value) {
        Source source = new Source(value);
        TextExtractor textExtractor = source.getTextExtractor();
        textExtractor.setExcludeNonHTMLElements(true);
        textExtractor.setConvertNonBreakingSpaces(false);
        textExtractor.setIncludeAttributes(false);
        return textExtractor.toString();
    }

    public static Boolean memberOf(String groups) {
        boolean result = false;
        final ProcessingContext jParams = Jahia.getThreadParamBean();
        final String[] groupArray = StringUtils.split(groups, ',');
        for (String aGroupArray : groupArray) {
            final String groupName = aGroupArray.trim();
            if (JCRSessionFactory.getInstance().getCurrentUser().isMemberOfGroup(jParams.getSiteID(), groupName)) {
                return true;
            }
        }

        return result;
    }

    public static Boolean notMemberOf(String groups) {
        boolean result = true;
        final ProcessingContext jParams = Jahia.getThreadParamBean();
        final String[] groupArray = StringUtils.split(groups, ',');
        for (String aGroupArray : groupArray) {
            String groupName = aGroupArray.trim();
            if (JCRSessionFactory.getInstance().getCurrentUser().isMemberOfGroup(jParams.getSiteID(),
                    groupName)) {
                return false;
            }
        }

        return result;
    }

    public static String stringConcatenation(String value, String appendix1, String appendix2) {
        final StringBuffer buff = new StringBuffer();
        if (value != null) {
            buff.append(value);
        }
        if (appendix1 != null) {
            buff.append(appendix1);
        }
        if (appendix2 != null) {
            buff.append(appendix2);
        }
        return buff.toString();
    }


    /**
     * Returns <code>true</code> if the subscriptions entry with the specified
     * data exists.
     *
     * @param objectKey the key of the content object that is the source of events
     * @param eventType the type of an event to be notified about
     * @param username  the user to be notified
     * @param siteId    the ID of the site owning the content object in question
     * @return <code>true</code> if the subscriptions entry with the specified
     *         data exists
     */
    public static boolean isSubscribed(String objectKey, String eventType,
                                       String username, int siteId) {

        return SubscriptionService.getInstance().isSubscribed(objectKey,
                eventType, username, siteId);
    }

    /**
     * Returns <code>true</code> if the subscriptions entry with the specified
     * data does not exist.
     *
     * @param objectKey the key of the content object that is the source of events
     * @param eventType the type of an event to be notified about
     * @param username  the user to be notified
     * @param siteId    the ID of the site owning the content object in question
     * @return <code>true</code> if the subscriptions entry with the specified
     *         data does not exist
     */
    public static boolean isNotSubscribed(String objectKey, String eventType,
                                          String username, int siteId) {

        return !SubscriptionService.getInstance().isSubscribed(objectKey,
                eventType, username, siteId);
    }

    public static String removeDuplicates(String initString, String separator) {
        final String[] fullString = initString.split(separator);
        StringBuilder finalString = new StringBuilder();
        String tmpString = initString;
        for (String s:fullString) {
            if (tmpString.contains(s)) {
                finalString.append(s);
                if (finalString.length() > 0) {
                    finalString.append(separator);
                }
                tmpString = tmpString.replaceAll(s,"");
            }
        }
        return finalString.toString();
    }

    public static int countOccurences(String initString, String searchString) {
        final String[] fullString = ("||||" + initString + "||||").split(searchString);
        return fullString.length - 1;
    }

    /**
     * Checks if the current user is included in the specified logical role or
     * has all specified roles if multiple are specified (comma-separated).
     * 
     * @param role the role identifier to check for. Multiple roles can be
     *            specified in a comma-separated form. In such case this method
     *            evaluates to <code>true</code> only if the current user has
     *            all the specified roles.
     * @return if the current user is included in the specified logical role or
     *         has all specified roles if multiple are specified (comma-separated)
     */
    public static Boolean isUserInRole(String role) {
        boolean hasIt = false;
        JahiaUser user = JCRSessionFactory.getInstance().getCurrentUser();
        if (user != null) {
            final String[] roles = StringUtils.split(role, ',');
            for (String roleToCheck : roles) {
                hasIt = user.hasRole(new RoleIdentity(roleToCheck.trim()));
                if (!hasIt) {
                    break;
                }
            }
        }

        return hasIt;
    }

    /**
     * Checks if the current user is included in the specified logical role for
     * the current site or has all specified roles if multiple are specified
     * (comma-separated).
     * 
     * @param role the role identifier to check for. Multiple roles can be
     *            specified in a comma-separated form. In such case this method
     *            evaluates to <code>true</code> only if the current user has
     *            all the specified roles.
     * @return if the current user is included in the specified logical role for
     *         current site or has all specified roles if multiple are specified
     *         (comma-separated)
     */
    public static Boolean isUserInRoleForSite(String role, String siteKey) {
        boolean hasIt = false;
        JahiaUser user = JCRSessionFactory.getInstance().getCurrentUser();
        if (user != null) {
            final String[] roles = StringUtils.split(role, ',');
            for (String roleToCheck : roles) {
                hasIt = user.hasRole(new RoleIdentity(roleToCheck.trim(), siteKey));
                if (!hasIt) {
                    break;
                }
            }
        }

        return hasIt;
    }

    /**
     * Checks if the current user has the specified permission or
     * has all specified permissions if multiple are specified (comma-separated).
     * 
     * @param permission the permission identifier to check for. Multiple permissions can be
     *            specified in a comma-separated form. In such case this method
     *            evaluates to <code>true</code> only if the current user has
     *            all the specified permissions.
     * @return if the current user has the specified permission or
     * has all specified permissions if multiple are specified (comma-separated)
     */
    public static Boolean isUserPermitted(String permission) {
        boolean hasIt = false;
        JahiaUser user = JCRSessionFactory.getInstance().getCurrentUser();
        if (user != null) {
            final String[] roles = StringUtils.split(permission, ',');
            for (String permissionToCheck : roles) {
                hasIt = user.isPermitted(new PermissionIdentity(permissionToCheck.trim()));
                if (!hasIt) {
                    break;
                }
            }
        }

        return hasIt;
    }

    /**
     * Checks if the current user has the specified site-level permission for
     * the current site or has all specified permissions if multiple are
     * specified (comma-separated).
     * 
     * @param permission the permission identifier to check for. Multiple
     *            permissions can be specified in a comma-separated form. In
     *            such case this method evaluates to <code>true</code> only if
     *            the current user has all the specified permissions.
     * @return if the current user has the specified permission or has all
     *         specified permissions if multiple are specified (comma-separated)
     */
    public static Boolean isUserPermittedForSite(String permission, String siteKey) {
        boolean hasIt = false;
        JahiaUser user = JCRSessionFactory.getInstance().getCurrentUser();
        if (user != null) {
            final String[] roles = StringUtils.split(permission, ',');
            for (String permissionToCheck : roles) {
                hasIt = user.isPermitted(new PermissionIdentity(permissionToCheck.trim(), siteKey));
                if (!hasIt) {
                    break;
                }
            }
        }

        return hasIt;
    }

    /**
     * Checks if the provided target object can be found in the source. The
     * search is done, depending on the source parameter type. It can be either
     * {@link String}, {@link Collection} or an array of objects.
     * 
     * @param source the source to search in
     * @param target the object to search for
     * @return <code>true</code> if the target object is present in the source
     */
    public static boolean contains(Object source, Object target) {
        if (source == null) {
            throw new IllegalArgumentException("The source cannot be null");
        }
        boolean found = false;
        if (source instanceof Collection<?>) {
            found = ((Collection<?>) source).contains(target);
        } else if (source instanceof Object[]) {
            found = ArrayUtils.contains((Object[]) source, target);
        } else {
            found = target != null ? source.toString().contains(target.toString()) : false;
        }

        return found;
    }
    
    public static long length(Object obj) throws JspTagException {
        return (obj != null && obj instanceof RangeIterator) ? JCRContentUtils.size((RangeIterator) obj)
                : org.apache.taglibs.standard.functions.Functions.length(obj);
    }

    /**
     * Reverse the content of a list. Only works with some List.
     *
     * @param list List<T> list to be reversed.
     * @return <code>java.util.List</code> the reversed list.
     */
    public static <T> List<T> reverse(Collection<T> list) {
        List<T> copy = new ArrayList<T>();
        copy.addAll(list);
        Collections.reverse(copy);
        return copy;
    }
    
    /**
     * Checks if the current object is iterable so that it can be used in an c:forEach
     * tag.
     * 
     * @param object the object to be checked if it is iterable
     * @return if the current object is iterable return true otherwise false
     */
    public static Boolean isIterable(Object o) {
        boolean isIt = false;
        if (o instanceof Object[] || o instanceof boolean[] || o instanceof byte[]
                || o instanceof char[] || o instanceof short[] || o instanceof int[]
                || o instanceof long[] || o instanceof float[] || o instanceof double[]
                || o instanceof Collection<?> || o instanceof Iterator<?>
                || o instanceof Enumeration<?> || o instanceof Map<?, ?> || o instanceof String) {
            isIt = true;
        }

        return isIt;
    }

}
