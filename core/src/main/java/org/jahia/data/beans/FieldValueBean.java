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
 * in Jahia's FLOSS exception. You should have received a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license"
 * 
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Limited. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */

package org.jahia.data.beans;

import static org.jahia.data.fields.FieldTypes.BIGTEXT;
import static org.jahia.data.fields.FieldTypes.BOOLEAN;
import static org.jahia.data.fields.FieldTypes.CATEGORY;
import static org.jahia.data.fields.FieldTypes.DATE;
import static org.jahia.data.fields.FieldTypes.FILE;
import static org.jahia.data.fields.FieldTypes.FLOAT;
import static org.jahia.data.fields.FieldTypes.INTEGER;
import static org.jahia.data.fields.FieldTypes.PAGE;
import static org.jahia.data.fields.FieldTypes.SMALLTEXT;
import static org.jahia.data.fields.FieldTypes.SMALLTEXT_SHARED_LANG;
import static org.jahia.data.fields.FieldTypes.typeName;

import java.util.Date;
import java.util.List;

import org.apache.axis.utils.StringUtils;
import org.jahia.data.fields.FieldTypes;
import org.jahia.data.files.JahiaFileField;

/**
 * Represents the value of a Jahia field depending on the field type. Exposed
 * into the page scope by the &lt;template:field/&gt; tag.
 * 
 * @author Sergiy Shyrkov
 */
public class FieldValueBean<E> extends ValueBaseBean<E> {

    private FieldBean field;

    private Object rawValue;

    private String value;

    /**
     * Initializes an instance of this class.
     * 
     * @param field
     *            the field bean
     * @param value
     *            the string representation of the field's value
     * @param rawValue
     *            the corresponding bean instance for the field value, depending
     *            on its type
     */
    public FieldValueBean(FieldBean field, String value, Object rawValue) {
        this.field = field;
        this.value = value;
        this.rawValue = rawValue;
    }

    private void assertType(int... requiredFieldTypes) {
        boolean supported = false;
        for (int requiredType : requiredFieldTypes) {
            if (field.getFieldType() == requiredType) {
                supported = true;
                break;
            }
        }
        if (!supported) {
            throw new UnsupportedOperationException(
                    "This operation is not supported for the field of type '"
                            + typeName[field.getFieldType()] + "'");
        }
    }

    /**
     * Returns the corresponding bean for the field value, depending on its
     * type.
     * 
     * @return the corresponding bean for the field value, depending on its type
     * @deprecated use {@link #getRawValue()} instead
     */
    public Object getBean() {
        return rawValue;
    }

    /**
     * Returns a boolean value. Throws {@link UnsupportedOperationException} in
     * case the field is not of type {@link FieldTypes#BOOLEAN}.
     * 
     * @return a boolean value
     */
    public Boolean getBoolean() {
        assertType(BOOLEAN);

        return (Boolean) rawValue;
    }

    /**
     * Returns a list of {@link CategoryBean} instances. Throws
     * {@link UnsupportedOperationException} in case the field is not of type
     * {@link FieldTypes#CATEGORY}.
     * 
     * @return a category value
     */
    public List<CategoryBean> getCategory() {
        assertType(CATEGORY);
        return (List<CategoryBean>) rawValue;
    }

    /**
     * Returns a date value. Throws {@link UnsupportedOperationException} in
     * case the field is not of type {@link FieldTypes#DATE}.
     * 
     * @return an integer value
     */
    public Date getDate() {
        assertType(DATE);

        return (Date) rawValue;
    }

    /**
     * Returns the corresponding field bean.
     * 
     * @return the corresponding field bean
     */
    public FieldBean getField() {
        return field;
    }

    /**
     * Returns a {@link JahiaFileField}. Throws
     * {@link UnsupportedOperationException} in case the field is not of type
     * {@link FieldTypes#FILE}.
     * 
     * @return an integer value
     */
    public JahiaFileField getFile() {
        assertType(FILE);

        return (JahiaFileField) rawValue;
    }

    /**
     * Returns a float value. Throws {@link UnsupportedOperationException} in
     * case the field is not of type {@link FieldTypes#FLOAT}.
     * 
     * @return a float value
     */
    public Float getFloat() {
        assertType(FLOAT);
        return (Float) rawValue;
    }

    /**
     * Returns an integer value. Throws {@link UnsupportedOperationException} in
     * case the field is not of type {@link FieldTypes#INTEGER}.
     * 
     * @return an integer value
     */
    public Integer getInteger() {
        assertType(INTEGER);

        return (Integer) rawValue;
    }

    /**
     * Returns a {@link PageBean}. Throws {@link UnsupportedOperationException}
     * in case the field is not of type {@link FieldTypes#PAGE}.
     * 
     * @return an integer value
     */
    public PageBean getPage() {
        assertType(PAGE);

        return (PageBean) rawValue;
    }

    public Object getRawValue() {
        return rawValue;
    }

    /**
     * Returns a resource value as an instance of the {@link ResourceBean}.
     * 
     * @return a resource value as an instance of the {@link ResourceBean}
     */
    public ResourceBean getResource() {
        return (ResourceBean) rawValue;
    }

    /**
     * Returns a text value. Throws {@link UnsupportedOperationException} in
     * case the field is not of type {@link FieldTypes#SMALLTEXT},
     * {@link FieldTypes#SMALLTEXT_SHARED_LANG} or {@link FieldTypes#BIGTEXT}.
     * 
     * @return a text value
     */
    public String getText() {
        assertType(SMALLTEXT, SMALLTEXT_SHARED_LANG, BIGTEXT);
        return getValue();
    }

    /**
     * Returns the type of the field (see {@link FieldTypes}).
     * 
     * @return the type of the field
     */
    public int getType() {
        return field.getFieldType();
    }

    /**
     * Returns a string representation of the field value.
     * 
     * @return a string representation of the field value
     */
    public String getValue() {
        return value;
    }

    @Override
    public boolean isEmpty() {
        boolean isEmpty = false;
        switch (field.getFieldType()) {
        case PAGE:
            isEmpty = rawValue == null;
            break;

        case CATEGORY:
            isEmpty = getCategory().isEmpty();
            break;

        case FILE:
            isEmpty = getFile() == null || !getFile().isDownloadable();
            break;

        default:
            isEmpty = StringUtils.isEmpty(value);
            break;
        }
        return isEmpty;
    }

    @Override
    public String toString() {
        return getValue();
    }

}
