/*
 * gvNIX. Spring Roo based RAD tool for Generalitat Valenciana Copyright (C)
 * 2013 Generalitat Valenciana
 * 
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/copyleft/gpl.html>.
 */
package org.gvnix.addon.datatables;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.gvnix.support.ItdBuilderHelper;
import org.springframework.roo.addon.web.mvc.controller.details.FinderMetadataDetails;
import org.springframework.roo.classpath.details.FieldMetadata;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.model.JdkJavaType;

/**
 * @author gvNIX Team
 */
public class FinderToDslHelper {

    public static final List<JavaType> NUMBERS = Collections
            .unmodifiableList(Arrays.asList(new JavaType[] {
                    JavaType.INT_OBJECT, JavaType.INT_PRIMITIVE,
                    JavaType.LONG_OBJECT, JavaType.LONG_PRIMITIVE,
                    JavaType.FLOAT_OBJECT, JavaType.FLOAT_PRIMITIVE,
                    JavaType.DOUBLE_OBJECT, JavaType.DOUBLE_PRIMITIVE,
                    JdkJavaType.BIG_INTEGER, JdkJavaType.BIG_DECIMAL }));

    public static final List<JavaType> DATES = Collections
            .unmodifiableList(Arrays.asList(new JavaType[] { JdkJavaType.DATE,
                    JdkJavaType.CALENDAR, JdkJavaType.TIMESTAMP }));

    private final FinderMetadataDetails finderMethod;
    private final QueryHolderTokens query;
    private final ItdBuilderHelper itdHelper;

    public FinderToDslHelper(FinderMetadataDetails finderMethod,
            QueryHolderTokens query, ItdBuilderHelper itdHelper) {
        this.finderMethod = finderMethod;
        this.query = query;
        this.itdHelper = itdHelper;
    }

    public Object getLikeExpression(String fieldName) {
        return ".like(\"%\".concat(".concat(fieldName).concat(
                ").concat(\"%\"))");
    }

    public Object getBetweenExpression(String fieldName) {
        String capitalized = StringUtils.capitalize(fieldName);
        String min = "min".concat(capitalized);
        String max = "max".concat(capitalized);
        return String.format(".between(%s,%s)", min, max);
    }

    public Object getGreaterThanEqualseExpression(String fieldName) {
        return String.format(".goe(%s)", fieldName);
    }

    public Object getGreaterThanExpression(String fieldName) {
        return String.format(".gt(%s)", fieldName);
    }

    public Object getLessThanEqualsExpression(String fieldName) {
        return String.format(".loe(%s)", fieldName);
    }

    public Object getLessThanExpression(String fieldName) {
        return String.format(".lt(%s)", fieldName);
    }

    public Object getNotEqualExpression(String fieldName) {
        return String.format(".ne(%s)", fieldName);
    }

    public String getDslOr(String conditionExpression) {
        return String.format("baseSearch.or(%s);", conditionExpression);
    }

    public String getDslAnd(String conditionExpression) {
        return String.format("baseSearch.and(%s);", conditionExpression);
    }

    public Object getEqualExpression(String fieldName) {
        return String.format(".eq(%s)", fieldName);
    }

    public Object getToLowerOperatorFor(JavaType fieldType) {
        if (JavaType.STRING.equals(fieldType)) {
            return ".toLowerCase()";
        }
        return ".stringValue().toLowerCase()";
    }

    public Object getDslGetterFor(String fieldName, JavaType fieldType) {
        if (JavaType.STRING.equals(fieldType)) {
            return ".getString(\"".concat(fieldName).concat("\")");
        }
        else if (NUMBERS.contains(fieldType)) {
            return String.format(".getNumber(\"%s\", %s.class)", fieldName,
                    itdHelper.getFinalTypeName(fieldType));
        }
        else if (DATES.contains(fieldType)) {
            return String.format(".getDate(\"%s\", %s.class)", fieldName,
                    itdHelper.getFinalTypeName(fieldType));
        }
        return String.format(".get(\"%s\")", fieldName);
    }

    public JavaType getFieldTypeOfFinder(String fieldName) {
        String paramName;
        for (FieldMetadata field : finderMethod.getFinderMethodParamFields()) {
            paramName = field.getFieldName().getSymbolName();
            if (paramName.equalsIgnoreCase(fieldName)) {
                return field.getFieldType();
            }
            else if (paramName.startsWith("min") || paramName.startsWith("max")) {
                // for params used on between comparations
                if (paramName.substring(3).equalsIgnoreCase(fieldName)) {
                    return field.getFieldType();
                }
            }
        }
        return null;
    }
}
