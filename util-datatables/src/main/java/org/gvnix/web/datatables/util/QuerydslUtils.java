/*
 * gvNIX. Spring Roo based RAD tool for Generalitat Valenciana     
 * Copyright (C) 2013 Generalitat Valenciana
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.gvnix.web.datatables.util;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.time.DateUtils;

import com.mysema.query.BooleanBuilder;
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
     * Creates a WHERE clause by the intersection of the given search-arguments
     * 
     * @param entity Entity {@link PathBuilder}. It represents the entity for
     *        class generation and alias-usage for path generation.
     *        <p/>
     *        Example: To retrieve a {@code Customer} with the first name 'Bob'
     *        entity must be a {@link PathBuilder} created for {@code Customer}
     *        class and searchArgs must contain the entry
     *        {@code 'firstName':'Bob'}
     * @param searchArgs Search arguments to be used to create the WHERE clause.
     *        It can contain {@code _operator_} entries for each field that want
     *        to use its own operator. By default {@code EQUALS} operator is
     *        used.
     *        <p/>
     *        Operator entry example: {@code _operator_weight = LT} the
     *        expression for {@code weight} field will do a less-than value
     *        comparison
     * @return the WHERE clause
     */
    public static <T> BooleanBuilder createPredicateByAnd(
            PathBuilder<T> entity, Map<String, Object> searchArgs) {

        // Using BooleanBuilder, a cascading builder for
        // Predicate expressions
        BooleanBuilder predicate = new BooleanBuilder();
        if (searchArgs.isEmpty()) {
            return predicate;
        }

        // Build the predicate
        if (searchArgs != null && !searchArgs.isEmpty()) {
            for (Entry<String, Object> entry : searchArgs.entrySet()) {
                // TODO: use the operator
                // searchArgs can contain "_operator_" entries for each field
                predicate.and(createObjectExpression(entity, entry.getKey(),
                        entry.getValue()));
            }
        }
        return predicate;
    }

    /**
     * Creates a WHERE clause to specify given {@code fieldName} must be equal
     * to one element of the provided Collection.
     * 
     * @param entity Entity {@link PathBuilder}. It represents the entity for
     *        class generation and alias-usage for path generation.
     *        <p/>
     *        Example: To retrieve a {@code Customer} with the first name 'Bob'
     *        entity must be a {@link PathBuilder} created for {@code Customer}
     *        class and searchArgs must contain the entry
     *        {@code 'firstName':'Bob'}
     * @param fieldName Property name in the given entity path. For example:
     *        {@code name} in {@code Pet} entity, {@code firstName} in
     *        {@code Pet.owner} entity.
     * @param values the Set of values to find the given field name, may be null
     * @return the WHERE clause
     */
    public static <T, E> BooleanBuilder createPredicateByIn(
            PathBuilder<T> entity, String fieldName, Set<E> values) {

        // Using BooleanBuilder, a cascading builder for
        // Predicate expressions
        BooleanBuilder predicate = new BooleanBuilder();
        if (StringUtils.isEmpty(fieldName) || values.isEmpty()) {
            return predicate;
        }

        // Build the predicate
        predicate.and(createCollectionExpression(entity, fieldName, values));

        return predicate;
    }

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
            return createStringLikeExpression(entityPath, fieldName, searchStr);
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
     * Return equal expression for {@code entityPath.fieldName}.
     * <p/>
     * Expr: {@code entityPath.fieldName eq searchObj}
     * 
     * @param entityPath Full path to entity and associations. For example:
     *        {@code Pet}, {@code Pet.owner}
     * @param fieldName Property name in the given entity path. For example:
     *        {@code name} in {@code Pet} entity, {@code firstName} in
     *        {@code Pet.owner} entity.
     * @param searchObj the value to find, may be null
     * @return BooleanExpression
     */
    public static <T> BooleanExpression createObjectExpression(
            PathBuilder<T> entityPath, String fieldName, Object searchObj) {
        if (searchObj == null) {
            return null;
        }
        BooleanExpression expression = entityPath.get(fieldName).eq(searchObj);
        return expression;
    }

    /**
     * Return equal expression for {@code entityPath.fieldName}.
     * <p/>
     * Expr: {@code entityPath.fieldName eq 'searchStr'}
     * <p/>
     * Equal operation is case insensitive.
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
        BooleanExpression expression = entityPath.getString(fieldName).lower()
                .eq(searchStr.toLowerCase());
        return expression;
    }

    /**
     * Return like expression for {@code entityPath.fieldName}.
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
    public static <T> BooleanExpression createStringLikeExpression(
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
	 * Return where clause expression for date properties, trying to parse the
	 * value to find to date and comparing it to the value of the date; if the
	 * value to find cannot be parsed to date, then try to cast the value to
	 * string before check it.
	 * <p/>
	 * <ul>
	 * <li>
	 * If value to find {@code searchStr} can be parsed using the patterns
	 * <em>dd-MM-yyyy HH:mm:ss</em> or <em>dd-MM-yyyy HH:mm</em> or 
	 * <em>dd-MM-yyyy</em> to {@code searchDate}, then search by specific date:
	 * <p/>
	 * - Querydsl Expr:
	 * {@code entityPath.fieldName = searchDate}
	 * <p/>
	 * - Database operation:
	 * {@code entity.fieldName = searchDate}
	 * </li>
	 * <li>
	 * If value to find {@code searchStr} can be parsed using the pattern
	 * <em>dd-MM</em> to {@code searchDate}, then search by specific day and
	 * month:
	 * <p/>
	 * - Querydsl Expr:
	 * {@code entityPath.fieldName.dayOfMonth() = searchDate.day and entityPath.fieldName.month() = searchDate.month}
	 * <p/>
	 * - Database operation:
	 * {@code dayofmonth(entity.fieldName) = searchDate.day && month(entity.fieldName) = searchDate.month}
	 * </li>
	 * <li>
	 * If value to find {@code searchStr} can be parsed using the pattern
	 * <em>MM-aaaa</em> to {@code searchDate}, then obtain the first day of the
	 * month for that year and the last day of the month for that year and check
	 * that value is into between theses values:
	 * <p/>
	 * - Querydsl Expr:
	 * {@code entityPath.fieldName.between(searchDate.firstDayOfMonth, searchDate.lastDayOfMonth)}
	 * <p/>
	 * - Database operation:
	 * {@code entity.fieldName between searchDate.firstDayOfMonth and searchDate.lastDayOfMonth}
	 * </li>
	 * <li>
	 * If value to find cannot be parsed as date, then try to cast the value to
	 * string before check it:
	 * <p/>
	 * - Querydsl Expr:
	 * {@code entityPath.fieldName.stringValue() like ('%' + searchStr + '%')}
	 * <p/>
	 * - Database operation:
	 * {@code str(entity.fieldName) like ('%' + searchStr + '%')}
	 * <p/>
	 * Note that like operation is case sensitive.
	 * </li>
	 * </ul>
	 * 
	 * @param entityPath
	 *            Full path to entity and associations. For example: {@code Pet}
	 *            , {@code Pet.owner}
	 * @param fieldName
	 *            Property name in the given entity path. For example:
	 *            {@code weight} in {@code Pet} entity, {@code age} in
	 *            {@code Pet.owner} entity.
	 * @param searchStr
	 *            the value to find, may be null
	 * @return PredicateOperation
	 */
    public static <T, C extends java.lang.Comparable<?>> BooleanExpression createDateExpression(
            PathBuilder<T> entityPath, String fieldName, Class<C> fieldType,
            String searchStr) {
        if (StringUtils.isEmpty(searchStr)) {
            return null;
        }

        DatePath<C> dateExpression = entityPath.getDate(fieldName, fieldType);

		// Search by full date
		String[] parsePatterns = new String[] { "dd-MM-yyyy HH:mm:ss",
				"dd/MM/yyyy HH:mm:ss", "MM-dd-yyyy HH:mm:ss",
				"MM/dd/yyyy HH:mm:ss", "dd-MM-yyyy HH:mm", "dd/MM/yyyy HH:mm",
				"MM-dd-yyyy HH:mm", "MM/dd/yyyy HH:mm", "dd-MM-yyyy",
				"dd/MM/yyyy", "MM-dd-yyyy", "MM/dd/yyyy" };
		try {
			Date searchDate = DateUtils.parseDateStrictly(searchStr, parsePatterns);
			return dateExpression.eq((fieldType.cast(searchDate)));
		}
        catch (Exception e) {
            // do nothing, and try the next parsing
        }
        
		// Search by day and month
		parsePatterns = new String[] { "dd-MM", "dd/MM", "MM-dd", "MM/dd" };
		try {
			Date searchDate = DateUtils.parseDateStrictly(searchStr,
					parsePatterns);
			Calendar searchCal = Calendar.getInstance();
			searchCal.setTime(searchDate);
			return dateExpression
					.dayOfMonth().eq(searchCal.get(Calendar.DAY_OF_MONTH))
					.and(dateExpression.month().eq(
							searchCal.get(Calendar.MONTH) + 1));
        }
        catch (Exception e) {
            // do nothing, and try the next parsing
        }

        // Search by month and year
		parsePatterns = new String[] { "MM-yyyy", "MM/yyyy" };
		try {
			Date searchDate = DateUtils.parseDateStrictly(searchStr,
					parsePatterns);
			Calendar searchCal = Calendar.getInstance();
			searchCal.setTime(searchDate);

            // from 1st day of the month
            Calendar monthStartCal = Calendar.getInstance();
			monthStartCal.set(searchCal.get(Calendar.YEAR),
					searchCal.get(Calendar.MONTH), 0, 23, 59, 59);
			monthStartCal.set(Calendar.MILLISECOND, 999);

            // to last day of the month
            Calendar monthEndCal = Calendar.getInstance();
			monthEndCal.set(searchCal.get(Calendar.YEAR),
					(searchCal.get(Calendar.MONTH) + 1), 0, 23, 59, 59);
            monthEndCal.set(Calendar.MILLISECOND, 999);
			return dateExpression.between(
					fieldType.cast(monthStartCal.getTime()),
					fieldType.cast(monthEndCal.getTime()));
        }
        catch (Exception e) {
            // do nothing, and try the next parsing
        }

        // Search by year
        // NOT NEEDED; JUST USE DEFAULT EXPRESSION
        /*
		parsePatterns = new String[] { "yyyy" };
		try {
			if (searchStr.length() == 4) {
				Date searchDate = DateUtils.parseDateStrictly(searchStr,
						parsePatterns);
				Calendar searchCal = Calendar.getInstance();
				searchCal.setTime(searchDate);

				// from 1st day of the year
				Calendar monthStartYear = Calendar.getInstance();
				monthStartYear.set(searchCal.get(Calendar.YEAR),
						Calendar.JANUARY, 1, 0, 0, 0);

				// to last day of the year
				Calendar monthEndYear = Calendar.getInstance();
				monthEndYear.set(searchCal.get(Calendar.YEAR),
						searchCal.getActualMaximum(Calendar.MONTH) + 1,
						searchCal.getActualMaximum(Calendar.DAY_OF_MONTH), 23,
						59, 59);
            	monthEndYear.set(Calendar.MILLISECOND, 999);

				// year condition: date_col >= value AND date_col <= value
				return dateExpression.between(
						fieldType.cast(monthStartYear.getTime()),
						fieldType.cast(monthEndYear.getTime()));
			}
		}
        catch (Exception e) {
            // do nothing, at the end the method returns the default expression
        }
        */

		// Default expression
		return dateExpression.stringValue().like(
				"%".concat(searchStr).concat("%"));
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
     * Return IN expression for {@code entityPath.fieldName}.
     * <p/>
     * Expr: {@code entityPath.fieldName IN ( values )}
     * 
     * @param entityPath Full path to entity and associations. For example:
     *        {@code Pet}, {@code Pet.owner}
     * @param fieldName Property name in the given entity path. For example:
     *        {@code name} in {@code Pet} entity, {@code firstName} in
     *        {@code Pet.owner} entity.
     * @param values the Set of values to find the given field name, may be null
     * @return BooleanExpression
     */
    public static <T, E> BooleanExpression createCollectionExpression(
            PathBuilder<T> entityPath, String fieldName, Set<E> values) {
        if (StringUtils.isEmpty(fieldName) || values.isEmpty()) {
            return null;
        }

        BooleanExpression expression = entityPath.get(fieldName).in(values);
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
