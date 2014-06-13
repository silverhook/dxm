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

public class ExtendedPropertyType {
    /*
     * The supported property types.
     */
    public static final int STRING = 1;
    public static final int BINARY = 2;
    public static final int LONG = 3;
    public static final int DOUBLE = 4;
    public static final int DATE = 5;
    public static final int BOOLEAN = 6;
    public static final int NAME = 7;
    public static final int PATH = 8;
    public static final int REFERENCE = 9;
    public static final int WEAKREFERENCE = 10;
    public static final int URI = 11;
    public static final int DECIMAL = 12;

    public static final int MULTIPLE_OFFSET = 256;

    /*
     * Undefined type.
     */
    public static final int UNDEFINED = 0;

    /*
     * The names of the supported property types,
     * as used in serialization.
     */
    public static final String TYPENAME_STRING = "String";
    public static final String TYPENAME_BINARY = "Binary";
    public static final String TYPENAME_LONG = "Long";
    public static final String TYPENAME_DOUBLE = "Double";
    public static final String TYPENAME_DATE = "Date";
    public static final String TYPENAME_BOOLEAN = "Boolean";
    public static final String TYPENAME_NAME = "Name";
    public static final String TYPENAME_PATH = "Path";
    public static final String TYPENAME_REFERENCE = "Reference";
    public static final String TYPENAME_WEAKREFERENCE = "WeakReference";
    public static final String TYPENAME_URI = "Uri";
    public static final String TYPENAME_DECIMAL = "Decimal";

    /*
     * String representation of <i>undefined</i> type.
     */
    public static final String TYPENAME_UNDEFINED = "undefined";

    /**
     * Returns the name of the specified <code>type</code>,
     * as used in serialization.
     *
     * @param type the property type
     * @return the name of the specified <code>type</code>
     * @throws IllegalArgumentException if <code>type</code>
     *                                  is not a valid property type.
     */
    public static String nameFromValue(int type) {
	switch (type) {
	    case STRING:
		return TYPENAME_STRING;
	    case BINARY:
		return TYPENAME_BINARY;
	    case BOOLEAN:
		return TYPENAME_BOOLEAN;
	    case LONG:
		return TYPENAME_LONG;
	    case DOUBLE:
		return TYPENAME_DOUBLE;
	    case DATE:
		return TYPENAME_DATE;
	    case NAME:
		return TYPENAME_NAME;
	    case PATH:
		return TYPENAME_PATH;
	    case REFERENCE:
		return TYPENAME_REFERENCE;
	    case WEAKREFERENCE:
		return TYPENAME_WEAKREFERENCE;
	    case URI:
		return TYPENAME_URI;
	    case DECIMAL:
		return TYPENAME_DECIMAL;
	    case UNDEFINED:
		return TYPENAME_UNDEFINED;
	    default:
		throw new IllegalArgumentException("unknown type: " + type);
	}
    }

    /**
     * Returns the numeric constant value of the type with the specified name.
     *
     * @param name the name of the property type
     * @return the numeric constant value
     * @throws IllegalArgumentException if <code>name</code>
     *                                  is not a valid property type name.
     */
    public static int valueFromName(String name) {
	if (name.equals(TYPENAME_STRING)) {
	    return STRING;
	} else if (name.equals(TYPENAME_BINARY)) {
	    return BINARY;
	} else if (name.equals(TYPENAME_BOOLEAN)) {
	    return BOOLEAN;
	} else if (name.equals(TYPENAME_LONG)) {
	    return LONG;
	} else if (name.equals(TYPENAME_DOUBLE)) {
	    return DOUBLE;
	} else if (name.equals(TYPENAME_DATE)) {
	    return DATE;
	} else if (name.equals(TYPENAME_NAME)) {
	    return NAME;
	} else if (name.equals(TYPENAME_PATH)) {
	    return PATH;
	} else if (name.equals(TYPENAME_REFERENCE)) {
	    return REFERENCE;
	} else if (name.equals(TYPENAME_WEAKREFERENCE)) {
	    return WEAKREFERENCE;
	} else if (name.equals(TYPENAME_URI)) {
	    return URI;
	} else if (name.equals(TYPENAME_DECIMAL)) {
	    return DECIMAL;
	} else if (name.equals(TYPENAME_UNDEFINED)) {
	    return UNDEFINED;
	} else {
	    throw new IllegalArgumentException("unknown type: " + name);
	}
    }

    /**
     * private constructor to prevent instantiation
     */
    private ExtendedPropertyType() {
    }

}
