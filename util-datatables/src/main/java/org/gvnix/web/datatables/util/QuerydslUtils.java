/*
 * gvNIX. Spring Roo based RAD tool for Generalitat Valenciana     
 * Copyright (C) 2013 Generalitat Valenciana
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/copyleft/gpl.html>.
 */
package org.gvnix.web.datatables.util;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import com.mysema.query.types.Order;
import com.mysema.query.types.OrderSpecifier;
import com.mysema.query.types.Predicate;
import com.mysema.query.types.expr.BooleanExpression;
import com.mysema.query.types.path.DatePath;
import com.mysema.query.types.path.NumberPath;
import com.mysema.query.types.path.PathBuilder;

/**
 * Querydsl utility functions
 * 
 * @author gvNIX team
 */
public class QuerydslUtils {

    public static final Set<Class<?>> NUMBER_PRIMITIVES = new HashSet<Class<?>>(
            Arrays.asList(new Class<?>[] { int.class, long.class, double.class,
                    float.class, short.class }));

    /**
     * Utility for constructing where clause expressions.
     * 
     * @param entityPath Full path to entity and associations. For example:
     *        {@code Pet}, {@code Pet.owner}
     * @param fieldName Property name in the given entity path. For example:
     *        {@code name} in {@code Pet} entity, {@code firstName} in
     *        {@code Pet.owner} entity.
     * @param fieldType Property value {@code Class}
     * @param searchStr the value to find, may be null
     * @return Predicate
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static <T> Predicate createExpression(PathBuilder<T> entityPath,
            String fieldName, Class<?> fieldType, String searchStr) {

        // Check for field type in order to delegate in custom-by-type
        // create expression method
        if (String.class == fieldType) {
            return createStringExpression(entityPath, fieldName, searchStr);
        }
        else if (Boolean.class == fieldType || boolean.class == fieldType) {
            return createBooleanExpression(entityPath, fieldName, searchStr);
        }
        else if (Number.class.isAssignableFrom(fieldType)
                || NUMBER_PRIMITIVES.contains(fieldType)) {
            if (NumberUtils.isNumber(searchStr)) {
                if (BigDecimal.class.isAssignableFrom(fieldType)) {
                    return createNumberExpression(entityPath, fieldName,
                            (Class<BigDecimal>) fieldType, searchStr);
                }
                if (BigInteger.class.isAssignableFrom(fieldType)) {
                    return createNumberExpression(entityPath, fieldName,
                            (Class<BigInteger>) fieldType, searchStr);
                }
                if (Byte.class.isAssignableFrom(fieldType)) {
                    return createNumberExpression(entityPath, fieldName,
                            (Class<Byte>) fieldType, searchStr);
                }
                if (Double.class.isAssignableFrom(fieldType)) {
                    return createNumberExpression(entityPath, fieldName,
                            (Class<Double>) fieldType, searchStr);
                }
                if (Float.class.isAssignableFrom(fieldType)) {
                    return createNumberExpression(entityPath, fieldName,
                            (Class<Float>) fieldType, searchStr);
                }
                if (Integer.class.isAssignableFrom(fieldType)) {
                    return createNumberExpression(entityPath, fieldName,
                            (Class<Integer>) fieldType, searchStr);
                }
                if (Long.class.isAssignableFrom(fieldType)) {
                    return createNumberExpression(entityPath, fieldName,
                            (Class<Long>) fieldType, searchStr);
                }
                if (Short.class.isAssignableFrom(fieldType)) {
                    return createNumberExpression(entityPath, fieldName,
                            (Class<Short>) fieldType, searchStr);
                }
            }
            else {
                return null;
            }
        }
        else if (Date.class.isAssignableFrom(fieldType)
                || Calendar.class.isAssignableFrom(fieldType)) {
            return createDateExpression(entityPath, fieldName,
                    (Class<Date>) fieldType, searchStr);
        }

        else if (fieldType.isEnum()) {
            return createEnumExpression(entityPath, fieldName, searchStr,
                    (Class<? extends Enum>) fieldType);
        }
        return null;
    }

    /**
     * Return where clause expression for {@code entityPath.fieldName}.
     * <p/>
     * Expr: {@code entityPath.fieldName like ('%' + searchStr + '%')}
     * <p/>
     * Like operation is case insensitive.
     * 
     * @param entityPath Full path to entity and associations. For example:
     *        {@code Pet}, {@code Pet.owner}
     * @param fieldName Property name in the given entity path. For example:
     *        {@code name} in {@code Pet} entity, {@code firstName} in
     *        {@code Pet.owner} entity.
     * @param searchStr the value to find, may be null
     * @return BooleanExpression
     */
    public static <T> BooleanExpression createStringExpression(
            PathBuilder<T> entityPath, String fieldName, String searchStr) {
        if (StringUtils.isEmpty(searchStr)) {
            return null;
        }
        String str = "%".concat(searchStr.toLowerCase()).concat("%");
        BooleanExpression expression = entityPath.getString(fieldName).lower()
                .like(str);
        return expression;
    }

    /**
     * Return where clause expression for number properties by casting it to
     * string before check its value.
     * <p/>
     * Querydsl Expr:
     * {@code entityPath.fieldName.stringValue() like ('%' + searchStr + '%')}
     * Database operation:
     * {@code str(entity.fieldName) like ('%' + searchStr + '%')}
     * <p/>
     * Like operation is case sensitive.
     * 
     * @param entityPath Full path to entity and associations. For example:
     *        {@code Pet}, {@code Pet.owner}
     * @param fieldName Property name in the given entity path. For example:
     *        {@code weight} in {@code Pet} entity, {@code age} in
     *        {@code Pet.owner} entity.
     * @param searchStr the value to find, may be null
     * @return PredicateOperation
     */
    public static <T, N extends java.lang.Number & java.lang.Comparable<?>> BooleanExpression createNumberExpression(
            PathBuilder<T> entityPath, String fieldName, Class<N> fieldType,
            String searchStr) {
        if (StringUtils.isEmpty(searchStr)) {
            return null;
        }
        NumberPath<N> numberExpression = entityPath.getNumber(fieldName,
                fieldType);
        BooleanExpression expression = numberExpression.stringValue().like(
                "%".concat(searchStr).concat("%"));
        return expression;
    }

    /**
     * Return where clause expression for date properties by casting it to
     * string before check its value.
     * <p/>
     * Querydsl Expr:
     * {@code entityPath.fieldName.stringValue() like ('%' + searchStr + '%')}
     * Database operation:
     * {@code str(entity.fieldName) like ('%' + searchStr + '%')}
     * <p/>
     * Like operation is case sensitive.
     * 
     * @param entityPath Full path to entity and associations. For example:
     *        {@code Pet}, {@code Pet.owner}
     * @param fieldName Property name in the given entity path. For example:
     *        {@code weight} in {@code Pet} entity, {@code age} in
     *        {@code Pet.owner} entity.
     * @param searchStr the value to find, may be null
     * @return PredicateOperation
     */
    public static <T, C extends java.lang.Comparable<?>> BooleanExpression createDateExpression(
            PathBuilder<T> entityPath, String fieldName, Class<C> fieldType,
            String searchStr) {
        if (StringUtils.isEmpty(searchStr)) {
            return null;
        }
        DatePath<C> dateExpression = entityPath.getDate(fieldName, fieldType);
        BooleanExpression expression = dateExpression.stringValue().like(
                "%".concat(searchStr).concat("%"));
        return expression;
    }

    /**
     * Return where clause expression for non-String
     * {@code entityPath.fieldName} by transforming it to text before check its
     * value.
     * <p/>
     * Expr:
     * {@code entityPath.fieldName.as(String.class) like ('%' + searchStr + '%')}
     * <p/>
     * Like operation is case insensitive.
     * 
     * @param entityPath Full path to entity and associations. For example:
     *        {@code Pet}, {@code Pet.owner}
     * @param fieldName Property name in the given entity path. For example:
     *        {@code weight} in {@code Pet} entity, {@code age} in
     *        {@code Pet.owner} entity.
     * @param searchStr the value to find, may be null
     * @param enumClass Enumeration type. Needed to enumeration values
     * @return BooleanExpression
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static <T> BooleanExpression createEnumExpression(
            PathBuilder<T> entityPath, String fieldName, String searchStr,
            Class<? extends Enum> enumClass) {
        if (StringUtils.isEmpty(searchStr)) {
            return null;
        }
        // Filter string to search than cannot be a identifier
        if (!StringUtils.isAlphanumeric(StringUtils.lowerCase(searchStr))) {
            return null;
        }

        // TODO i18n of enum name

        // normalize search string
        searchStr = StringUtils.trim(searchStr).toLowerCase();

        // locate enums matching by name
        Set matching = new HashSet();

        Enum<?> enumValue;
        String enumStr;

        for (Field enumField : enumClass.getDeclaredFields()) {
            if (enumField.isEnumConstant()) {
                enumStr = enumField.getName();
                enumValue = Enum.valueOf(enumClass, enumStr);

                // Check enum name contains string to search
                if (enumStr.toLowerCase().contains(searchStr)) {
                    // Add to matching enum
                    matching.add(enumValue);
                    continue;
                }

                // Check using toString
                enumStr = enumValue.toString();
                if (enumStr.toLowerCase().contains(searchStr)) {
                    // Add to matching enum
                    matching.add(enumValue);
                }
            }
        }
        if (matching.isEmpty()) {
            return null;
        }

        // create a enum in matching condition
        BooleanExpression expression = entityPath.get(fieldName).in(matching);
        return expression;
    }

    /**
     * Return where clause expression for {@code Boolean} fields by transforming
     * the given {@code searchStr} to {@code Boolean} before check its value.
     * <p/>
     * Expr: {@code entityPath.fieldName eq (TRUE | FALSE)}
     * 
     * @param entityPath Full path to entity and associations. For example:
     *        {@code Pet}, {@code Pet.owner}
     * @param fieldName Property name in the given entity path. For example:
     *        {@code weight} in {@code Pet} entity, {@code age} in
     *        {@code Pet.owner} entity.
     * @param searchStr the boolean value to find, may be null. Supported string
     *        are: si, yes, true, on, no, false, off
     * @return BooleanExpression
     */
    public static <T> BooleanExpression createBooleanExpression(
            PathBuilder<T> entityPath, String fieldName, String searchStr) {
        if (StringUtils.isBlank(searchStr)) {
            return null;
        }

        Boolean value = null;

        // I18N: Spanish (normalize search value: trim start-end and lower case)
        if ("si".equals(StringUtils.trim(searchStr).toLowerCase())) {
            value = Boolean.TRUE;
        }
        else {
            value = BooleanUtils.toBooleanObject(searchStr);
        }

        // if cannot parse to boolean or null input
        if (value == null) {
            return null;
        }

        BooleanExpression expression = entityPath.getBoolean(fieldName).eq(
                value);
        return expression;
    }

    /**
     * Create an order-by-element in a Query instance
     * 
     * @param entityPath Full path to entity and associations. For example:
     *        {@code Pet}, {@code Pet.owner}
     * @param fieldName Property name in the given entity path. For example:
     *        {@code weight} in {@code Pet} entity, {@code age} in
     *        {@code Pet.owner} entity.
     * @param fieldType Property value {@code Class}. Must implements
     *        {@link Comparable}
     * @param order ascending or descending order
     * @return
     */
    public static <T, E extends Comparable<?>> OrderSpecifier<?> createOrderSpecifier(
            PathBuilder<T> entityPath, String fieldName, Class<E> fieldType,
            Order order) {
        OrderSpecifier<?> orderBy = null;

        // Get the OrderSpecifier
        if (order == Order.ASC) {
            orderBy = entityPath.getComparable(fieldName, fieldType).asc();
        }
        else if (order == Order.DESC) {
            orderBy = entityPath.getComparable(fieldName, fieldType).desc();
        }
        return orderBy;
    }
}
