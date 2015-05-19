/*
 * gvNIX is an open source tool for rapid application development (RAD).
 * Copyright (C) 2010 Generalitat Valenciana
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
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.gvnix.web.datatables.util;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.gvnix.web.datatables.util.querydsl.paths.PolygonPath;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.core.convert.ConversionException;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.TypeDescriptor;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Iterables;
import com.mysema.query.BooleanBuilder;
import com.mysema.query.types.Order;
import com.mysema.query.types.OrderSpecifier;
import com.mysema.query.types.Predicate;
import com.mysema.query.types.expr.BooleanExpression;
import com.mysema.query.types.path.DatePath;
import com.mysema.query.types.path.NumberPath;
import com.mysema.query.types.path.PathBuilder;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Polygon;

/**
 * Querydsl utility functions
 * 
 * @author <a href="http://www.disid.com">DISID Corporation S.L.</a> made for <a
 *         href="http://www.dgti.gva.es">General Directorate for Information
 *         Technologies (DGTI)</a>
 */
public class QuerydslUtils {

    public static final String OPERATOR_GOE = "goe";
    public static final String OPERATOR_LOE = "loe";
    public static final String OPERATOR_ISNULL = "isnull";
    public static final String OPERATOR_NOTNULL = "notnull";
    public static final String G_FIL_OPE_ISNULL = "global.filters.operations.all.isnull";
    public static final String G_FIL_OPE_NOTNULL = "global.filters.operations.all.notnull";

    public static final TypeDescriptor STRING_TYPE_DESCRIPTOR = TypeDescriptor
            .valueOf(String.class);

    private static LoadingCache<Class<?>, BeanWrapper> beanWrappersCache = CacheBuilder
            .newBuilder().maximumSize(200)
            .build(new CacheLoader<Class<?>, BeanWrapper>() {
                public BeanWrapper load(Class<?> key) {
                    return new BeanWrapperImpl(key);
                }
            });

    public static final Set<Class<?>> NUMBER_PRIMITIVES = new HashSet<Class<?>>(
            Arrays.asList(new Class<?>[] { int.class, long.class, double.class,
                    float.class, short.class }));

    public static final String OPERATOR_PREFIX = "_operator_";

    private static final String SEPARATOR_FIELDS = ".";

    public static final String[] FULL_DATE_PATTERNS = new String[] {
            "dd-MM-yyyy HH:mm:ss", "dd/MM/yyyy HH:mm:ss",
            "MM-dd-yyyy HH:mm:ss", "MM/dd/yyyy HH:mm:ss", "dd-MM-yyyy HH:mm",
            "dd/MM/yyyy HH:mm", "MM-dd-yyyy HH:mm", "MM/dd/yyyy HH:mm",
            "dd-MM-yyyy", "dd/MM/yyyy", "MM-dd-yyyy", "MM/dd/yyyy",
            "dd-MMMM-yyyy HH:mm:ss", "dd/MMMM/yyyy HH:mm:ss",
            "MMMM-dd-yyyy HH:mm:ss", "MMMM/dd/yyyy HH:mm:ss",
            "dd-MMMM-yyyy HH:mm", "dd/MMMM/yyyy HH:mm", "MMMM-dd-yyyy HH:mm",
            "MMMM/dd/yyyy HH:mm", "dd-MMMM-yyyy", "dd/MMMM/yyyy",
            "MMMM-dd-yyyy", "MMMM/dd/yyyy" };

    public static final String[] FULL_DATE_PATTERNS_WITH_TIME = new String[] {
            "dd-MM-yyyy HH:mm:ss", "dd/MM/yyyy HH:mm:ss",
            "MM-dd-yyyy HH:mm:ss", "MM/dd/yyyy HH:mm:ss", "dd-MM-yyyy HH:mm",
            "dd/MM/yyyy HH:mm", "MM-dd-yyyy HH:mm", "MM/dd/yyyy HH:mm",
            "dd-MMMM-yyyy HH:mm:ss", "dd/MMMM/yyyy HH:mm:ss",
            "MMMM-dd-yyyy HH:mm:ss", "MMMM/dd/yyyy HH:mm:ss",
            "dd-MMMM-yyyy HH:mm", "dd/MMMM/yyyy HH:mm", "MMMM-dd-yyyy HH:mm",
            "MMMM/dd/yyyy HH:mm" };

    public static final String[] FULL_DATE_PAT_WO_TIME = new String[] {
            "dd-MM-yyyy", "dd/MM/yyyy", "MM-dd-yyyy", "MMMM/dd/yyyy",
            "dd-MMMM-yyyy", "dd/MMMM/yyyy", "MMMM-dd-yyyy", "MMMM/dd/yyyy" };

    public static final String[] DAY_AND_MONTH_DATE_PATTERNS = new String[] {
            "dd-MM", "dd/MM", "MM-dd", "MM/dd", "dd-MMMM", "dd/MMMM",
            "MMMM-dd", "MMMM/dd" };

    public static final String[] MONTH_AND_YEAR_DATE_PATTERNS = new String[] {
            "MM-yyyy", "MM/yyyy", "MMMM-yyyy", "MMMM/yyyy" };

    /**
     * Get BeanWrapper instance for klass. <b>Warning<b>: BeanWrapper returned
     * is not Thread-safe!!!
     * 
     * @param klass
     * @return
     */
    private static BeanWrapper getBeanWrapper(Class<?> klass) {
        BeanWrapper beanWrapper;
        try {
            beanWrapper = beanWrappersCache.get(klass);
        }
        catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
        return beanWrapper;
    }

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
     * @param conversionService required to transform values
     * @return the WHERE clause
     */
    public static <T> BooleanBuilder createPredicateByAnd(
            PathBuilder<T> entity, Map<String, Object> searchArgs,
            ConversionService conversionService) {

        // Using BooleanBuilder, a cascading builder for
        // Predicate expressions
        BooleanBuilder predicate = new BooleanBuilder();
        if (searchArgs == null || searchArgs.isEmpty()) {
            return predicate;
        }

        // Build the predicate
        for (Entry<String, Object> entry : searchArgs.entrySet()) {
            String key = entry.getKey();

            // searchArgs can contain dtt_bbox attribute
            if (key.equals(DatatablesUtils.BOUNDING_BOX_PARAM)) {
                // Getting bbox to Search
                String bBoxToSearch = ((String[]) entry.getValue())[0];
                Geometry bBoxGeometry = null;
                try {
                    bBoxGeometry = conversionService.convert(bBoxToSearch,
                            Geometry.class);
                }
                catch (Exception e) {
                    try {
                        // Legacy bbox parameter support (no WKT string)
                        bBoxGeometry = conversionService.convert(
                                String.format("POLYGON((%s))", bBoxToSearch),
                                Geometry.class);
                    }
                    catch (Exception e1) {
                        throw new RuntimeException(
                                String.format(
                                        "Error getting map Bounding Box on QuerydslUtils from string: '%s'",
                                        bBoxToSearch), e);
                    }
                }
                // Getting fields to filter using bbox
                if (searchArgs.get(DatatablesUtils.BOUNDING_BOX_FIELDS_PARAM) != null
                        && bBoxGeometry != null) {
                    String bBoxFields = ((String[]) searchArgs
                            .get(DatatablesUtils.BOUNDING_BOX_FIELDS_PARAM))[0];
                    String[] separatedFields = StringUtils.split(bBoxFields,
                            ",");
                    for (String field : separatedFields) {
                        predicate.or(createIntersectsExpression(entity, field,
                                bBoxGeometry));
                    }
                }
            }
            else if (!key.startsWith(OPERATOR_PREFIX)
                    && !key.equals(DatatablesUtils.BOUNDING_BOX_FIELDS_PARAM)) { // searchArgs
                                                                                 // can
                // contain "_operator_"
                // entries for each
                // field
                Object valueToSearch = entry.getValue();
                String operator = (String) searchArgs.get(OPERATOR_PREFIX
                        .concat(key));

                // If value to search is a collection, creates a predicate for
                // each object of the collection
                if (valueToSearch instanceof Collection) {
                    @SuppressWarnings("unchecked")
                    Collection<Object> valueColl = (Collection<Object>) valueToSearch;
                    for (Object valueObj : valueColl) {
                        predicate.and(createObjectExpression(entity, key,
                                valueObj, operator, conversionService));
                    }
                }
                else {
                    predicate.and(createObjectExpression(entity, key,
                            valueToSearch, operator, conversionService));
                }
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
     *        {@code Pet} , {@code Pet.owner}
     * @param fieldName Property name in the given entity path. For example:
     *        {@code name} in {@code Pet} entity, {@code firstName} in
     *        {@code Pet.owner} entity.
     * @param fieldType Property value {@code Class}
     * @param searchStr the value to find, may be null
     * @return Predicate
     * @deprecated
     */
    public static <T> Predicate createExpression(PathBuilder<T> entityPath,
            String fieldName, Class<?> fieldType, String searchStr) {
        return createExpression(entityPath, fieldName, searchStr, null);
    }

    /**
     * Utility for constructing where clause expressions.
     * 
     * @param entityPath Full path to entity and associations. For example:
     *        {@code Pet} , {@code Pet.owner}
     * @param fieldName Property name in the given entity path. For example:
     *        {@code name} in {@code Pet} entity, {@code firstName} in
     *        {@code Pet.owner} entity.
     * @param fieldType Property value {@code Class}
     * @param searchStr the value to find, may be null
     * @return Predicate
     * @deprecated
     */
    public static <T> Predicate createExpression(PathBuilder<T> entityPath,
            String fieldName, String searchStr) {
        return createExpression(entityPath, fieldName, searchStr, null);
    }

    /**
     * Utility for constructing where clause expressions.
     * 
     * @param entityPath Full path to entity and associations. For example:
     *        {@code Pet} , {@code Pet.owner}
     * @param fieldName Property name in the given entity path. For example:
     *        {@code name} in {@code Pet} entity, {@code firstName} in
     *        {@code Pet.owner} entity.
     * @param fieldType Property value {@code Class}
     * @param searchStr the value to find, may be null
     * @return Predicate
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static <T> Predicate createExpression(PathBuilder<T> entityPath,
            String fieldName, String searchStr,
            ConversionService conversionService) {

        TypeDescriptor descriptor = getTypeDescriptor(fieldName, entityPath);
        if (descriptor == null) {
            throw new IllegalArgumentException(String.format(
                    "Can't found field '%s' on entity '%s'", fieldName,
                    entityPath.getType()));
        }
        Class<?> fieldType = descriptor.getType();

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
            return createNumberExpressionGenerics(entityPath, fieldName,
                    fieldType, descriptor, searchStr, conversionService);
        }
        else if (Date.class.isAssignableFrom(fieldType)
                || Calendar.class.isAssignableFrom(fieldType)) {
            BooleanExpression expression = createDateExpression(entityPath,
                    fieldName, (Class<Date>) fieldType, searchStr);
            return expression;
        }

        else if (fieldType.isEnum()) {
            return createEnumExpression(entityPath, fieldName, searchStr,
                    (Class<? extends Enum>) fieldType);
        }
        return null;
    }

    /**
     * Method to create Bounding box intersects expression
     * 
     * @param entityPath
     * @param boundingBox
     * @param fieldName
     * @return
     */
    public static <T> Predicate createIntersectsExpression(
            PathBuilder<T> entityPath, String fieldName, Geometry boundingBox) {
        PolygonPath<Polygon> polygonPath = new PolygonPath<Polygon>(entityPath,
                fieldName);
        BooleanExpression intersectsExpression = polygonPath
                .intersects(boundingBox);
        return intersectsExpression;
    }

    /**
     * Utility for constructing where clause expressions.
     * 
     * @param entityPath Full path to entity and associations. For example:
     *        {@code Pet} , {@code Pet.owner}
     * @param fieldName Property name in the given entity path. For example:
     *        {@code name} in {@code Pet} entity, {@code firstName} in
     *        {@code Pet.owner} entity.
     * @param fieldType Property value {@code Class}
     * @param searchStr the value to find, may be null
     * @return Predicate
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static <T> Predicate createExpression(PathBuilder<T> entityPath,
            String fieldName, String searchStr,
            ConversionService conversionService, MessageSource messageSource) {

        TypeDescriptor descriptor = getTypeDescriptor(fieldName, entityPath);
        if (descriptor == null) {
            throw new IllegalArgumentException(String.format(
                    "Can't found field '%s' on entity '%s'", fieldName,
                    entityPath.getType()));
        }
        Class<?> fieldType = descriptor.getType();

        // Check for field type in order to delegate in custom-by-type
        // create expression method
        if (String.class == fieldType) {
            return createStringExpressionWithOperators(entityPath, fieldName,
                    searchStr, conversionService, messageSource);
        }
        else if (Boolean.class == fieldType || boolean.class == fieldType) {
            return createBooleanExpressionWithOperators(entityPath, fieldName,
                    searchStr, conversionService, messageSource);
        }
        else if (Number.class.isAssignableFrom(fieldType)
                || NUMBER_PRIMITIVES.contains(fieldType)) {
            return createNumberExpressionGenericsWithOperators(entityPath,
                    fieldName, descriptor, searchStr, conversionService,
                    messageSource);
        }
        else if (Date.class.isAssignableFrom(fieldType)
                || Calendar.class.isAssignableFrom(fieldType)) {
            String datePattern = "dd/MM/yyyy";
            if (messageSource != null) {
                datePattern = messageSource.getMessage(
                        "global.filters.operations.date.pattern", null,
                        LocaleContextHolder.getLocale());
            }
            BooleanExpression expression = createDateExpressionWithOperators(
                    entityPath, fieldName, (Class<Date>) fieldType, searchStr,
                    conversionService, messageSource, datePattern);
            return expression;
        }

        else if (fieldType.isEnum()) {
            return createEnumExpression(entityPath, fieldName, searchStr,
                    (Class<? extends Enum>) fieldType);
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public static <T> Predicate createNumberExpressionGenerics(
            PathBuilder<T> entityPath, String fieldName, Class<?> fieldType,
            TypeDescriptor descriptor, String searchStr,
            ConversionService conversionService) {
        Predicate numberExpression = null;

        if (isNumber(searchStr, conversionService, descriptor)) {
            if (BigDecimal.class.isAssignableFrom(fieldType)) {
                numberExpression = createNumberExpression(entityPath,
                        fieldName, (Class<BigDecimal>) fieldType, descriptor,
                        searchStr, conversionService);
            }
            if (BigInteger.class.isAssignableFrom(fieldType)) {
                numberExpression = createNumberExpression(entityPath,
                        fieldName, (Class<BigInteger>) fieldType, descriptor,
                        searchStr, conversionService);
            }
            if (Byte.class.isAssignableFrom(fieldType)) {
                numberExpression = createNumberExpression(entityPath,
                        fieldName, (Class<Byte>) fieldType, descriptor,
                        searchStr, conversionService);
            }
            if (Double.class.isAssignableFrom(fieldType)
                    || double.class == fieldType) {
                numberExpression = createNumberExpression(entityPath,
                        fieldName, (Class<Double>) fieldType, descriptor,
                        searchStr, conversionService);
            }
            if (Float.class.isAssignableFrom(fieldType)
                    || float.class == fieldType) {
                numberExpression = createNumberExpression(entityPath,
                        fieldName, (Class<Float>) fieldType, descriptor,
                        searchStr, conversionService);
            }
            if (Integer.class.isAssignableFrom(fieldType)
                    || int.class == fieldType) {
                numberExpression = createNumberExpression(entityPath,
                        fieldName, (Class<Integer>) fieldType, descriptor,
                        searchStr, conversionService);
            }
            if (Long.class.isAssignableFrom(fieldType)
                    || long.class == fieldType) {
                numberExpression = createNumberExpression(entityPath,
                        fieldName, (Class<Long>) fieldType, descriptor,
                        searchStr, conversionService);
            }
            if (Short.class.isAssignableFrom(fieldType)
                    || short.class == fieldType) {
                numberExpression = createNumberExpression(entityPath,
                        fieldName, (Class<Short>) fieldType, descriptor,
                        searchStr, conversionService);
            }
        }
        return numberExpression;
    }

    @SuppressWarnings("unchecked")
    public static <T> Predicate createNumberExpressionGenericsWithOperators(
            PathBuilder<T> entityPath, String fieldName,
            TypeDescriptor descriptor, String searchStr,
            ConversionService conversionService, MessageSource messageSource) {
        Predicate numberExpression = null;

        Class<?> fieldType = descriptor.getType();

        if (isNumber(searchStr, conversionService, descriptor)) {
            if (BigDecimal.class.isAssignableFrom(fieldType)) {
                numberExpression = createNumberExpressionEqual(entityPath,
                        fieldName, (Class<BigDecimal>) fieldType, descriptor,
                        searchStr, conversionService);
            }
            if (BigInteger.class.isAssignableFrom(fieldType)) {
                numberExpression = createNumberExpressionEqual(entityPath,
                        fieldName, (Class<BigInteger>) fieldType, descriptor,
                        searchStr, conversionService);
            }
            if (Byte.class.isAssignableFrom(fieldType)) {
                numberExpression = createNumberExpressionEqual(entityPath,
                        fieldName, (Class<Byte>) fieldType, descriptor,
                        searchStr, conversionService);
            }
            if (Double.class.isAssignableFrom(fieldType)
                    || double.class == fieldType) {
                numberExpression = createNumberExpressionEqual(entityPath,
                        fieldName, (Class<Double>) fieldType, descriptor,
                        searchStr, conversionService);
            }
            if (Float.class.isAssignableFrom(fieldType)
                    || float.class == fieldType) {
                numberExpression = createNumberExpressionEqual(entityPath,
                        fieldName, (Class<Float>) fieldType, descriptor,
                        searchStr, conversionService);
            }
            if (Integer.class.isAssignableFrom(fieldType)
                    || int.class == fieldType) {
                numberExpression = createNumberExpressionEqual(entityPath,
                        fieldName, (Class<Integer>) fieldType, descriptor,
                        searchStr, conversionService);
            }
            if (Long.class.isAssignableFrom(fieldType)
                    || long.class == fieldType) {
                numberExpression = createNumberExpressionEqual(entityPath,
                        fieldName, (Class<Long>) fieldType, descriptor,
                        searchStr, conversionService);
            }
            if (Short.class.isAssignableFrom(fieldType)
                    || short.class == fieldType) {
                numberExpression = createNumberExpressionEqual(entityPath,
                        fieldName, (Class<Short>) fieldType, descriptor,
                        searchStr, conversionService);
            }
        }
        else {
            // If is not a number, can be possible that exists a filter
            // expression.
            if (BigDecimal.class.isAssignableFrom(fieldType)) {
                numberExpression = getNumericFilterExpression(entityPath,
                        fieldName, (Class<BigDecimal>) fieldType, descriptor,
                        searchStr, conversionService, messageSource);
            }
            if (BigInteger.class.isAssignableFrom(fieldType)) {
                numberExpression = getNumericFilterExpression(entityPath,
                        fieldName, (Class<BigInteger>) fieldType, descriptor,
                        searchStr, conversionService, messageSource);
            }
            if (Byte.class.isAssignableFrom(fieldType)) {
                numberExpression = getNumericFilterExpression(entityPath,
                        fieldName, (Class<Byte>) fieldType, descriptor,
                        searchStr, conversionService, messageSource);
            }
            if (Double.class.isAssignableFrom(fieldType)
                    || double.class == fieldType) {
                numberExpression = getNumericFilterExpression(entityPath,
                        fieldName, (Class<Double>) fieldType, descriptor,
                        searchStr, conversionService, messageSource);
            }
            if (Float.class.isAssignableFrom(fieldType)
                    || float.class == fieldType) {
                numberExpression = getNumericFilterExpression(entityPath,
                        fieldName, (Class<Float>) fieldType, descriptor,
                        searchStr, conversionService, messageSource);
            }
            if (Integer.class.isAssignableFrom(fieldType)
                    || int.class == fieldType) {
                numberExpression = getNumericFilterExpression(entityPath,
                        fieldName, (Class<Integer>) fieldType, descriptor,
                        searchStr, conversionService, messageSource);
            }
            if (Long.class.isAssignableFrom(fieldType)
                    || long.class == fieldType) {
                numberExpression = getNumericFilterExpression(entityPath,
                        fieldName, (Class<Long>) fieldType, descriptor,
                        searchStr, conversionService, messageSource);
            }
            if (Short.class.isAssignableFrom(fieldType)
                    || short.class == fieldType) {
                numberExpression = getNumericFilterExpression(entityPath,
                        fieldName, (Class<Short>) fieldType, descriptor,
                        searchStr, conversionService, messageSource);
            }
        }
        return numberExpression;
    }

    /**
     * Return equal expression for {@code entityPath.fieldName}.
     * <p/>
     * Expr: {@code entityPath.fieldName eq searchObj}
     * 
     * @param entityPath Full path to entity and associations. For example:
     *        {@code Pet} , {@code Pet.owner}
     * @param fieldName Property name in the given entity path. For example:
     *        {@code name} in {@code Pet} entity, {@code firstName} in
     *        {@code Pet.owner} entity.
     * @param searchObj the value to find, may be null
     * @return BooleanExpression
     */
    public static <T> BooleanExpression createObjectExpression(
            PathBuilder<T> entityPath, String fieldName, Object searchObj,
            ConversionService conversionService) {
        return createObjectExpression(entityPath, fieldName, searchObj, null,
                conversionService);
    }

    /**
     * Return an expression for {@code entityPath.fieldName} with the
     * {@code operator} or "equal" by default.
     * <p/>
     * Expr: {@code entityPath.fieldName eq searchObj}
     * 
     * @param entityPath Full path to entity and associations. For example:
     *        {@code Pet} , {@code Pet.owner}
     * @param fieldName Property name in the given entity path. For example:
     *        {@code name} in {@code Pet} entity, {@code firstName} in
     *        {@code Pet.owner} entity.
     * @param searchObj the value to find, may be null
     * @param operator the operator to use into the expression. Supported
     *        operators:
     *        <ul>
     *        <li>For all types: {@code eq}, {@code in}, {@code ne},
     *        {@code notIn}, {@code isNull} and {@code isNotNull}.</li> <li> For
     *        strings and numbers: {@code goe}, {@code gt}, {@code loe},
     *        {@code lt} and {@code like}.</li> <li> For booleans: {@code goe},
     *        {@code gt}, {@code loe} and {@code lt}.</li> <li> For dates:
     *        {@code goe}, {@code gt}, {@code before}, {@code loe}, {@code lt}
     *        and {@code after}. </li>
     *        </ul>
     * @return BooleanExpression
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static <T> BooleanExpression createObjectExpression(
            PathBuilder<T> entityPath, String fieldName, Object searchObj,
            String operator, ConversionService conversionService) {
        if (searchObj == null) {
            return null;
        }

        TypeDescriptor typeDescriptor = getTypeDescriptor(fieldName, entityPath);
        if (typeDescriptor == null) {
            throw new IllegalArgumentException(String.format(
                    "Can't found field '%s' on entity '%s'", fieldName,
                    entityPath.getType()));
        }

        if (StringUtils.isBlank(operator)
                || StringUtils.equalsIgnoreCase(operator, "eq")) {
            return entityPath.get(fieldName).eq(searchObj);
        }
        else if (StringUtils.equalsIgnoreCase(operator, "in")) {
            return entityPath.get(fieldName).in(searchObj);
        }
        else if (StringUtils.equalsIgnoreCase(operator, "ne")) {
            return entityPath.get(fieldName).ne(searchObj);
        }
        else if (StringUtils.equalsIgnoreCase(operator, "notIn")) {
            return entityPath.get(fieldName).notIn(searchObj);
        }
        else if (StringUtils.equalsIgnoreCase(operator, OPERATOR_ISNULL)) {
            return entityPath.get(fieldName).isNull();
        }
        else if (StringUtils.equalsIgnoreCase(operator, "isNotNull")) {
            return entityPath.get(fieldName).isNotNull();
        }

        Class<?> fieldType = getFieldType(fieldName, entityPath);
        if (String.class == fieldType && String.class == searchObj.getClass()) {
            return createStringExpression(entityPath, fieldName, searchObj,
                    operator);
        }
        else if ((Boolean.class == fieldType || boolean.class == fieldType)
                && String.class == searchObj.getClass()) {
            return createBooleanExpression(entityPath, fieldName, searchObj,
                    operator);
        }
        else if ((Number.class.isAssignableFrom(fieldType) || NUMBER_PRIMITIVES
                .contains(fieldType))
                && String.class == searchObj.getClass()
                && isValidValueFor((String) searchObj, typeDescriptor,
                        conversionService)) {
            return createNumericExpression(entityPath, fieldName, searchObj,
                    operator, fieldType);

        }
        else if ((Date.class.isAssignableFrom(fieldType) || Calendar.class
                .isAssignableFrom(fieldType))
                && String.class == searchObj.getClass()) {
            return createDateExpression(entityPath, fieldName, searchObj,
                    operator, fieldType);
        }
        else if (fieldType.isEnum() && String.class == searchObj.getClass()) {
            return createEnumExpression(entityPath, fieldName,
                    (String) searchObj, (Class<? extends Enum>) fieldType);
        }

        return entityPath.get(fieldName).eq(searchObj);
    }

    /**
     * Check if a string is valid for a type <br/>
     * If conversion service is not provided try to check by apache commons
     * utilities. <b>TODO</b> in this (no-conversionService) case just
     * implemented for numerics
     * 
     * @param string
     * @param typeDescriptor
     * @param conversionService (optional)
     * @return
     */
    private static boolean isValidValueFor(String string,
            TypeDescriptor typeDescriptor, ConversionService conversionService) {
        if (conversionService != null) {
            try {
                conversionService.convert(string, STRING_TYPE_DESCRIPTOR,
                        typeDescriptor);
            }
            catch (ConversionException e) {
                return false;
            }
            return true;
        }
        else {
            Class<?> fieldType = typeDescriptor.getType();
            if (Number.class.isAssignableFrom(fieldType)
                    || NUMBER_PRIMITIVES.contains(fieldType)) {
                return NumberUtils.isNumber(string);
            }
            // TODO implement other types
            return true;
        }
    }

    /**
     * Return an expression for {@code entityPath.fieldName} (for Dates) with
     * the {@code operator} or "equal" by default.
     * <p/>
     * Expr: {@code entityPath.fieldName eq searchObj}
     * 
     * @param entityPath
     * @param fieldName
     * @param searchObj
     * @param operator
     * @param fieldType
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <T> BooleanExpression createDateExpression(
            PathBuilder<T> entityPath, String fieldName, Object searchObj,
            String operator, Class<?> fieldType) {
        DatePath<Date> dateExpression = entityPath.getDate(fieldName,
                (Class<Date>) fieldType);
        try {
            Date value = DateUtils.parseDateStrictly((String) searchObj,
                    FULL_DATE_PATTERNS);
            if (StringUtils.equalsIgnoreCase(operator, OPERATOR_GOE)) {
                return dateExpression.goe(value);
            }
            else if (StringUtils.equalsIgnoreCase(operator, "gt")
                    || StringUtils.equalsIgnoreCase(operator, "after")) {
                return dateExpression.gt(value);
            }
            else if (StringUtils.equalsIgnoreCase(operator, OPERATOR_LOE)) {
                return dateExpression.loe(value);
            }
            else if (StringUtils.equalsIgnoreCase(operator, "lt")
                    || StringUtils.equalsIgnoreCase(operator, "before")) {
                return dateExpression.lt(value);
            }
        }
        catch (ParseException e) {
            return entityPath.get(fieldName).eq(searchObj);
        }
        return entityPath.get(fieldName).eq(searchObj);
    }

    /**
     * Return an expression for {@code entityPath.fieldName} (for Numerics) with
     * the {@code operator} or "equal" by default.
     * <p/>
     * Expr: {@code entityPath.fieldName eq searchObj}
     * 
     * @param entityPath
     * @param fieldName
     * @param searchObj
     * @param operator
     * @param fieldType
     * @return
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static <T> BooleanExpression createNumericExpression(
            PathBuilder<T> entityPath, String fieldName, Object searchObj,
            String operator, Class<?> fieldType) {
        NumberPath numberExpression = null;
        if (BigDecimal.class.isAssignableFrom(fieldType)) {
            numberExpression = entityPath.getNumber(fieldName,
                    (Class<BigDecimal>) fieldType);
        }
        else if (BigInteger.class.isAssignableFrom(fieldType)) {
            numberExpression = entityPath.getNumber(fieldName,
                    (Class<BigInteger>) fieldType);
        }
        else if (Byte.class.isAssignableFrom(fieldType)) {
            numberExpression = entityPath.getNumber(fieldName,
                    (Class<Byte>) fieldType);
        }
        else if (Double.class.isAssignableFrom(fieldType)
                || double.class == fieldType) {
            numberExpression = entityPath.getNumber(fieldName,
                    (Class<Double>) fieldType);
        }
        else if (Float.class.isAssignableFrom(fieldType)
                || float.class == fieldType) {
            numberExpression = entityPath.getNumber(fieldName,
                    (Class<Float>) fieldType);
        }
        else if (Integer.class.isAssignableFrom(fieldType)
                || int.class == fieldType) {
            numberExpression = entityPath.getNumber(fieldName,
                    (Class<Integer>) fieldType);
        }
        else if (Long.class.isAssignableFrom(fieldType)
                || long.class == fieldType) {
            numberExpression = entityPath.getNumber(fieldName,
                    (Class<Long>) fieldType);
        }
        else if (Short.class.isAssignableFrom(fieldType)
                || short.class == fieldType) {
            numberExpression = entityPath.getNumber(fieldName,
                    (Class<Short>) fieldType);
        }
        if (numberExpression != null) {
            Number value = NumberUtils.createNumber((String) searchObj);
            if (StringUtils.equalsIgnoreCase(operator, OPERATOR_GOE)) {
                return numberExpression.goe(value);
            }
            else if (StringUtils.equalsIgnoreCase(operator, "gt")) {
                return numberExpression.gt(value);
            }
            else if (StringUtils.equalsIgnoreCase(operator, "like")) {
                return numberExpression.like((String) searchObj);
            }
            else if (StringUtils.equalsIgnoreCase(operator, OPERATOR_LOE)) {
                return numberExpression.loe(value);
            }
            else if (StringUtils.equalsIgnoreCase(operator, "lt")) {
                return numberExpression.lt(value);
            }
        }
        return entityPath.get(fieldName).eq(searchObj);
    }

    /**
     * Return an expression for {@code entityPath.fieldName} (for Booleans) with
     * the {@code operator} or "equal" by default.
     * <p/>
     * Expr: {@code entityPath.fieldName eq searchObj}
     * 
     * @param entityPath
     * @param fieldName
     * @param searchObj
     * @param operator
     * @return
     */
    public static <T> BooleanExpression createBooleanExpression(
            PathBuilder<T> entityPath, String fieldName, Object searchObj,
            String operator) {
        Boolean value = BooleanUtils.toBooleanObject((String) searchObj);
        if (value != null) {
            if (StringUtils.equalsIgnoreCase(operator, OPERATOR_GOE)) {
                return entityPath.getBoolean(fieldName).goe(value);
            }
            else if (StringUtils.equalsIgnoreCase(operator, "gt")) {
                return entityPath.getBoolean(fieldName).gt(value);
            }
            else if (StringUtils.equalsIgnoreCase(operator, OPERATOR_LOE)) {
                return entityPath.getBoolean(fieldName).loe(value);
            }
            else if (StringUtils.equalsIgnoreCase(operator, "lt")) {
                return entityPath.getBoolean(fieldName).lt(value);
            }
        }
        return entityPath.get(fieldName).eq(searchObj);
    }

    /**
     * Return an expression for {@code entityPath.fieldName} (for Strings) with
     * the {@code operator} or "equal" by default.
     * <p/>
     * Expr: {@code entityPath.fieldName eq searchObj}
     * 
     * @param entityPath
     * @param fieldName
     * @param searchObj
     * @param operator
     * @return
     */
    public static <T> BooleanExpression createStringExpression(
            PathBuilder<T> entityPath, String fieldName, Object searchObj,
            String operator) {
        if (StringUtils.equalsIgnoreCase(operator, OPERATOR_GOE)) {
            return entityPath.getString(fieldName).goe((String) searchObj);
        }
        else if (StringUtils.equalsIgnoreCase(operator, "gt")) {
            return entityPath.getString(fieldName).gt((String) searchObj);
        }
        else if (StringUtils.equalsIgnoreCase(operator, OPERATOR_LOE)) {
            return entityPath.getString(fieldName).loe((String) searchObj);
        }
        else if (StringUtils.equalsIgnoreCase(operator, "lt")) {
            return entityPath.getString(fieldName).lt((String) searchObj);
        }
        else if (StringUtils.equalsIgnoreCase(operator, "like")) {
            return entityPath.getString(fieldName).like((String) searchObj);
        }
        return entityPath.get(fieldName).eq(searchObj);
    }

    /**
     * Return equal expression for {@code entityPath.fieldName}.
     * <p/>
     * Expr: {@code entityPath.fieldName eq 'searchStr'}
     * <p/>
     * Equal operation is case insensitive.
     * 
     * @param entityPath Full path to entity and associations. For example:
     *        {@code Pet} , {@code Pet.owner}
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
     *        {@code Pet} , {@code Pet.owner}
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
     * Return like expression for {@code entityPath.fieldName}.
     * <p/>
     * Expr: {@code entityPath.fieldName like ('%' + searchStr + '%')}
     * <p/>
     * Like operation is case insensitive.
     * 
     * @param entityPath Full path to entity and associations. For example:
     *        {@code Pet} , {@code Pet.owner}
     * @param fieldName Property name in the given entity path. For example:
     *        {@code name} in {@code Pet} entity, {@code firstName} in
     *        {@code Pet.owner} entity.
     * @param searchStr the value to find, may be null
     * @return BooleanExpression
     */
    public static <T> BooleanExpression createStringExpressionWithOperators(
            PathBuilder<T> entityPath, String fieldName, String searchStr,
            ConversionService conversionService, MessageSource messageSource) {
        if (StringUtils.isEmpty(searchStr)) {
            return null;
        }

        // All operations
        String endsOperation = "ENDS";
        String startsOperation = "STARTS";
        String containsOperation = "CONTAINS";
        String isEmptyOperation = "ISEMPTY";
        String isNotEmptyOperation = "ISNOTEMPTY";
        String isNullOperation = OPERATOR_ISNULL;
        String isNotNullOperation = OPERATOR_NOTNULL;

        if (messageSource != null) {
            endsOperation = messageSource.getMessage(
                    "global.filters.operations.string.ends", null,
                    LocaleContextHolder.getLocale());
            startsOperation = messageSource.getMessage(
                    "global.filters.operations.string.starts", null,
                    LocaleContextHolder.getLocale());
            containsOperation = messageSource.getMessage(
                    "global.filters.operations.string.contains", null,
                    LocaleContextHolder.getLocale());
            isEmptyOperation = messageSource.getMessage(
                    "global.filters.operations.string.isempty", null,
                    LocaleContextHolder.getLocale());
            isNotEmptyOperation = messageSource.getMessage(
                    "global.filters.operations.string.isnotempty", null,
                    LocaleContextHolder.getLocale());
            isNullOperation = messageSource.getMessage(G_FIL_OPE_ISNULL, null,
                    LocaleContextHolder.getLocale());
            isNotNullOperation = messageSource.getMessage(G_FIL_OPE_NOTNULL,
                    null, LocaleContextHolder.getLocale());
        }

        // If written expression is ENDS operation
        Pattern endsOperator = Pattern.compile(String.format("%s[(](.+)[)]$",
                endsOperation));
        Matcher endsMatcher = endsOperator.matcher(searchStr);

        if (endsMatcher.matches()) {
            // Getting value
            String value = endsMatcher.group(1);

            String str = "%".concat(value.toLowerCase());
            return entityPath.getString(fieldName).lower().like(str);
        }

        // If written expression is STARTS operation
        Pattern startsOperator = Pattern.compile(String.format("%s[(](.+)[)]$",
                startsOperation));
        Matcher startsMatcher = startsOperator.matcher(searchStr);

        if (startsMatcher.matches()) {
            // Getting value
            String value = startsMatcher.group(1);

            String str = value.toLowerCase().concat("%");
            return entityPath.getString(fieldName).lower().like(str);
        }

        // If written expression is CONTAINS operation
        Pattern containsOperator = Pattern.compile(String.format(
                "%s[(](.+)[)]$", containsOperation));
        Matcher containsMatcher = containsOperator.matcher(searchStr);

        if (containsMatcher.matches()) {
            // Getting value
            String value = containsMatcher.group(1);

            String str = "%".concat(value.toLowerCase()).concat("%");
            return entityPath.getString(fieldName).lower().like(str);
        }

        // If written expression is ISEMPTY operation
        Pattern isEmptyOperator = Pattern.compile(String.format("%s",
                isEmptyOperation));
        Matcher isEmptyMatcher = isEmptyOperator.matcher(searchStr);
        if (isEmptyMatcher.matches()) {
            return entityPath.getString(fieldName).isEmpty()
                    .or(entityPath.getString(fieldName).isNull());

        }

        // If written expression is ISNOTEMPTY operation
        Pattern isNotEmptyOperator = Pattern.compile(String.format("%s",
                isNotEmptyOperation));
        Matcher isNotEmptyMatcher = isNotEmptyOperator.matcher(searchStr);
        if (isNotEmptyMatcher.matches()) {
            return entityPath.getString(fieldName).isNotEmpty()
                    .and(entityPath.getString(fieldName).isNotNull());

        }

        // If written expression is ISNULL operation
        Pattern isNullOperator = Pattern.compile(String.format("%s",
                isNullOperation));
        Matcher isNullMatcher = isNullOperator.matcher(searchStr);
        if (isNullMatcher.matches()) {
            return entityPath.getString(fieldName).isNull();

        }

        // If written expression is ISNOTNULL operation
        Pattern isNotNullOperator = Pattern.compile(String.format("%s",
                isNotNullOperation));
        Matcher isNotNullMatcher = isNotNullOperator.matcher(searchStr);
        if (isNotNullMatcher.matches()) {
            return entityPath.getString(fieldName).isNotNull();

        }

        // If written expression is a symbol operation expression

        // Getting expressions with symbols
        Pattern symbolOperator = Pattern.compile("[=]?(.+)");
        Matcher symbolMatcher = symbolOperator.matcher(searchStr);

        if (symbolMatcher.matches()) {

            String value = symbolMatcher.group(1);

            // operator is not necessary. Always is =
            return entityPath.getString(fieldName).lower()
                    .eq(value.toLowerCase());
        }

        return null;
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
     *        {@code Pet} , {@code Pet.owner}
     * @param fieldName Property name in the given entity path. For example:
     *        {@code weight} in {@code Pet} entity, {@code age} in
     *        {@code Pet.owner} entity.
     * @param searchStr the value to find, may be null
     * @return PredicateOperation
     */
    public static <T, N extends java.lang.Number & java.lang.Comparable<?>> BooleanExpression createNumberExpression(
            PathBuilder<T> entityPath, String fieldName, Class<N> fieldType,
            TypeDescriptor descriptor, String searchStr,
            ConversionService conversionService) {
        if (StringUtils.isBlank(searchStr)) {
            return null;
        }
        NumberPath<N> numberExpression = entityPath.getNumber(fieldName,
                fieldType);

        BooleanExpression expression = null;

        if (conversionService != null) {
            try {
                Object number = conversionService.convert(searchStr,
                        STRING_TYPE_DESCRIPTOR, descriptor);
                if (number == null) {
                    expression = numberExpression.stringValue().like(
                            "%".concat(searchStr).concat("%"));
                }
                else {
                    String toSearch = number.toString();
                    if (number instanceof BigDecimal
                            && ((BigDecimal) number).scale() > 1) {
                        // For bigDecimal trim 0 in decimal part
                        toSearch = StringUtils.stripEnd(toSearch, "0");
                        if (StringUtils.endsWith(toSearch, ".")) {
                            // prevent "#." strings
                            toSearch = toSearch.concat("0");
                        }
                    }
                    expression = numberExpression.stringValue().like(
                            "%".concat(toSearch).concat("%"));
                }
            }
            catch (ConversionException e) {
                expression = numberExpression.stringValue().like(
                        "%".concat(searchStr).concat("%"));
            }
        }
        else {
            expression = numberExpression.stringValue().like(
                    "%".concat(searchStr).concat("%"));
        }
        return expression;
    }

/**
     * Return where clause expression for number properties by casting it to
     * string before check its value.
     * <p/>
     * Querydsl Expr:
     * {@code entityPath.fieldName.stringValue() eq searchStr
     * Database operation:
     * {@code str(entity.fieldName) = searchStr
     * <p/>
     * Like operation is case sensitive.
     *
     * @param entityPath Full path to entity and associations. For example:
     *        {@code Pet} , {@code Pet.owner}
     * @param fieldName Property name in the given entity path. For example:
     *        {@code weight} in {@code Pet} entity, {@code age} in
     *        {@code Pet.owner} entity.
     * @param searchStr the value to find, may be null
     * @return PredicateOperation
     */
    @SuppressWarnings("unchecked")
    public static <T, N extends java.lang.Number & java.lang.Comparable<?>> BooleanExpression createNumberExpressionEqual(
            PathBuilder<T> entityPath, String fieldName, Class<N> fieldType,
            TypeDescriptor descriptor, String searchStr,
            ConversionService conversionService) {
        if (StringUtils.isEmpty(searchStr)) {
            return null;
        }
        NumberPath<N> numberExpression = entityPath.getNumber(fieldName,
                fieldType);

        TypeDescriptor strDesc = STRING_TYPE_DESCRIPTOR;

        if (conversionService != null) {
            try {
                return numberExpression.eq((N) conversionService.convert(
                        searchStr, strDesc, descriptor));
            }
            catch (ConversionException ex) {
                return numberExpression.stringValue().like(
                        "%".concat(searchStr).concat("%"));
            }
        }
        else {
            return numberExpression.stringValue().like(
                    "%".concat(searchStr).concat("%"));
        }
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
     * - Querydsl Expr: {@code entityPath.fieldName = searchDate}
     * <p/>
     * - Database operation: {@code entity.fieldName = searchDate}</li>
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
     * Note that like operation is case sensitive.</li>
     * </ul>
     * 
     * @param entityPath Full path to entity and associations. For example:
     *        {@code Pet} , {@code Pet.owner}
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

        BooleanExpression expression;

        // Search by full date
        String[] parsePatterns = null;
        try {
            parsePatterns = FULL_DATE_PATTERNS_WITH_TIME;
            Date searchDate = DateUtils.parseDateStrictly(searchStr,
                    parsePatterns);
            Calendar searchCal = Calendar.getInstance();
            searchCal.setTime(searchDate);
            expression = dateExpression.eq((fieldType.cast(searchCal)));
        }
        catch (Exception e) {
            // do nothing, and try the next parsing
            expression = null;
        }

        if (expression == null) {
            try {
                parsePatterns = FULL_DATE_PAT_WO_TIME;
                Date searchDate = DateUtils.parseDateStrictly(searchStr,
                        parsePatterns);
                Calendar searchCal = Calendar.getInstance();
                searchCal.setTime(searchDate);
                expression = dateExpression
                        .dayOfMonth()
                        .eq(searchCal.get(Calendar.DAY_OF_MONTH))
                        .and(dateExpression.month().eq(
                                searchCal.get(Calendar.MONTH) + 1))
                        .and(dateExpression.year().eq(
                                searchCal.get(Calendar.YEAR)));
            }
            catch (Exception e) {
                // do nothing, and try the next parsing
                expression = null;
            }
        }

        if (expression == null) {
            // Search by day and month
            parsePatterns = DAY_AND_MONTH_DATE_PATTERNS;
            try {
                Date searchDate = DateUtils.parseDateStrictly(searchStr,
                        parsePatterns);
                Calendar searchCal = Calendar.getInstance();
                searchCal.setTime(searchDate);
                expression = dateExpression
                        .dayOfMonth()
                        .eq(searchCal.get(Calendar.DAY_OF_MONTH))
                        .and(dateExpression.month().eq(
                                searchCal.get(Calendar.MONTH) + 1));
            }
            catch (Exception e) {
                // do nothing, and try the next parsing
                expression = null;
            }
        }

        // Search by month and year
        if (expression == null) {
            parsePatterns = MONTH_AND_YEAR_DATE_PATTERNS;
            try {
                Date searchDate = DateUtils.parseDateStrictly(searchStr,
                        parsePatterns);
                Calendar searchCal = Calendar.getInstance();
                searchCal.setTime(searchDate);

                // from 1st day of the month
                Calendar monthStartCal = Calendar.getInstance();
                monthStartCal.set(searchCal.get(Calendar.YEAR),
                        searchCal.get(Calendar.MONTH), 1, 23, 59, 59);
                monthStartCal.set(Calendar.MILLISECOND, 999);

                // to last day of the month
                Calendar monthEndCal = Calendar.getInstance();
                monthEndCal.set(searchCal.get(Calendar.YEAR),
                        (searchCal.get(Calendar.MONTH) + 1), 1, 23, 59, 59);
                monthEndCal.set(Calendar.MILLISECOND, 999);

                expression = dateExpression.between(
                        fieldType.cast(monthStartCal),
                        fieldType.cast(monthEndCal));
            }
            catch (Exception e) {
                // do nothing, and try the next parsing
                expression = null;
            }
        }

        // Search by year
        // NOT NEEDED; JUST USE DEFAULT EXPRESSION
        if (expression == null) {
            // Default expression
            expression = dateExpression.stringValue().like(
                    "%".concat(searchStr).concat("%"));
        }

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
     * - Querydsl Expr: {@code entityPath.fieldName = searchDate}
     * <p/>
     * - Database operation: {@code entity.fieldName = searchDate}</li>
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
     * Note that like operation is case sensitive.</li>
     * </ul>
     * 
     * @param entityPath Full path to entity and associations. For example:
     *        {@code Pet} , {@code Pet.owner}
     * @param fieldName Property name in the given entity path. For example:
     *        {@code weight} in {@code Pet} entity, {@code age} in
     *        {@code Pet.owner} entity.
     * @param searchStr the value to find, may be null
     * @return PredicateOperation
     */
    public static <T, C extends java.lang.Comparable<?>> BooleanExpression createDateExpressionWithOperators(
            PathBuilder<T> entityPath, String fieldName, Class<C> fieldType,
            String searchStr, ConversionService conversionService,
            MessageSource messageSource, String datePattern) {
        if (StringUtils.isEmpty(searchStr)) {
            return null;
        }

        DatePath<C> dateExpression = entityPath.getDate(fieldName, fieldType);

        // Getting simpleDateFormat
        DateFormat dateFormat = new SimpleDateFormat(datePattern);

        // All possible operations
        String date = "DATE";
        String year = "YEAR";
        String month = "MONTH";
        String day = "DAY";
        String between = "BETWEEN";
        String isNullOperation = OPERATOR_ISNULL;
        String isNotNullOperation = OPERATOR_NOTNULL;

        if (messageSource != null) {
            date = messageSource.getMessage(
                    "global.filters.operations.date.date", null,
                    LocaleContextHolder.getLocale());
            year = messageSource.getMessage(
                    "global.filters.operations.date.year", null,
                    LocaleContextHolder.getLocale());
            month = messageSource.getMessage(
                    "global.filters.operations.date.month", null,
                    LocaleContextHolder.getLocale());
            day = messageSource.getMessage(
                    "global.filters.operations.date.day", null,
                    LocaleContextHolder.getLocale());
            between = messageSource.getMessage(
                    "global.filters.operations.date.between", null,
                    LocaleContextHolder.getLocale());
            isNullOperation = messageSource.getMessage(G_FIL_OPE_ISNULL, null,
                    LocaleContextHolder.getLocale());
            isNotNullOperation = messageSource.getMessage(G_FIL_OPE_NOTNULL,
                    null, LocaleContextHolder.getLocale());
        }

        // If written expression is ISNULL operation
        Pattern isNullOperator = Pattern.compile(String.format("%s",
                isNullOperation));
        Matcher isNullMatcher = isNullOperator.matcher(searchStr);
        if (isNullMatcher.matches()) {
            return dateExpression.isNull();

        }

        // If written expression is ISNOTNULL operation
        Pattern isNotNullOperator = Pattern.compile(String.format("%s",
                isNotNullOperation));
        Matcher isNotNullMatcher = isNotNullOperator.matcher(searchStr);
        if (isNotNullMatcher.matches()) {
            return dateExpression.isNotNull();

        }

        // Creating regex to get DATE operator
        Pattern dateOperator = Pattern.compile(String.format(
                "%s[(]([\\d\\/]*)[)]", date));
        Matcher dateMatcher = dateOperator.matcher(searchStr);

        if (dateMatcher.matches()) {
            try {
                String dateValue = dateMatcher.group(1);
                Date dateToFilter = dateFormat.parse(dateValue);

                Calendar searchCal = Calendar.getInstance();
                searchCal.setTime(dateToFilter);

                return dateExpression.eq(conversionService.convert(searchCal,
                        fieldType));

            }
            catch (ParseException e) {
                return null;
            }
        }

        // Creating regex to get YEAR operator
        Pattern yearOperator = Pattern.compile(String.format(
                "%s[(]([\\d]*)[)]", year));
        Matcher yearMatcher = yearOperator.matcher(searchStr);

        if (yearMatcher.matches()) {

            String value = yearMatcher.group(1);

            return dateExpression.year().eq(Integer.parseInt(value));
        }

        // Creating regex to get MONTH operator
        Pattern monthOperator = Pattern.compile(String.format(
                "%s[(]([\\d]*)[)]", month));
        Matcher monthMatcher = monthOperator.matcher(searchStr);

        if (monthMatcher.matches()) {

            String value = monthMatcher.group(1);

            return dateExpression.month().eq(Integer.parseInt(value));
        }

        // Creating regex to get DAY operator
        Pattern dayOperator = Pattern.compile(String.format("%s[(]([\\d]*)[)]",
                day));
        Matcher dayMatcher = dayOperator.matcher(searchStr);

        if (dayMatcher.matches()) {

            String value = dayMatcher.group(1);

            return dateExpression.dayOfMonth().eq(Integer.parseInt(value));
        }

        // Creating regex to get BETWEEN operator
        Pattern betweenOperator = Pattern.compile(String.format(
                "%s[(]([\\d\\/]*);([\\d\\/]*)[)]", between));
        Matcher betweenMatcher = betweenOperator.matcher(searchStr);

        if (betweenMatcher.matches()) {

            String valueFrom = betweenMatcher.group(1);
            String valueTo = betweenMatcher.group(2);

            if (StringUtils.isNotBlank(valueFrom)
                    && StringUtils.isNotBlank(valueTo)) {

                try {

                    Date dateFrom = dateFormat.parse(valueFrom);
                    Date dateTo = dateFormat.parse(valueTo);

                    Calendar dateFromCal = Calendar.getInstance();
                    dateFromCal.setTime(dateFrom);

                    Calendar dateToCal = Calendar.getInstance();
                    dateToCal.setTime(dateTo);

                    return dateExpression.between(
                            conversionService.convert(dateFromCal, fieldType),
                            conversionService.convert(dateToCal, fieldType));

                }
                catch (Exception e) {
                    return null;
                }
            }

        }

        return null;
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
     *        {@code Pet} , {@code Pet.owner}
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
     *        {@code Pet} , {@code Pet.owner}
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
     * Return where clause expression for {@code Boolean} fields by transforming
     * the given {@code searchStr} to {@code Boolean} before check its value.
     * <p/>
     * Expr: {@code entityPath.fieldName eq (TRUE | FALSE)}
     * 
     * @param entityPath Full path to entity and associations. For example:
     *        {@code Pet} , {@code Pet.owner}
     * @param fieldName Property name in the given entity path. For example:
     *        {@code weight} in {@code Pet} entity, {@code age} in
     *        {@code Pet.owner} entity.
     * @param searchStr the boolean value to find, may be null. Supported string
     *        are: si, yes, true, on, no, false, off
     * @return BooleanExpression
     */
    public static <T> BooleanExpression createBooleanExpressionWithOperators(
            PathBuilder<T> entityPath, String fieldName, String searchStr,
            ConversionService conversionService, MessageSource messageSource) {
        if (StringUtils.isBlank(searchStr)) {
            return null;
        }

        // Getting all operations
        String trueOperation = "TRUE";
        String falseOperation = "FALSE";
        String isNullOperation = OPERATOR_ISNULL;
        String isNotNullOperation = OPERATOR_NOTNULL;

        if (messageSource != null) {
            trueOperation = messageSource.getMessage(
                    "global.filters.operations.boolean.true", null,
                    LocaleContextHolder.getLocale());
            falseOperation = messageSource.getMessage(
                    "global.filters.operations.boolean.false", null,
                    LocaleContextHolder.getLocale());
            isNullOperation = messageSource.getMessage(G_FIL_OPE_ISNULL, null,
                    LocaleContextHolder.getLocale());
            isNotNullOperation = messageSource.getMessage(G_FIL_OPE_NOTNULL,
                    null, LocaleContextHolder.getLocale());
        }

        // If written function is TRUE
        Pattern trueOperator = Pattern.compile(String.format("%s",
                trueOperation));
        Matcher trueMatcher = trueOperator.matcher(searchStr);

        if (trueMatcher.matches()) {
            return entityPath.getBoolean(fieldName).eq(Boolean.TRUE);
        }

        // If written function is FALSE
        Pattern falseOperator = Pattern.compile(String.format("%s",
                falseOperation));
        Matcher falseMatcher = falseOperator.matcher(searchStr);

        if (falseMatcher.matches()) {
            return entityPath.getBoolean(fieldName).eq(Boolean.FALSE);
        }

        // If written expression is ISNULL operation
        Pattern isNullOperator = Pattern.compile(String.format("%s",
                isNullOperation));
        Matcher isNullMatcher = isNullOperator.matcher(searchStr);
        if (isNullMatcher.matches()) {
            return entityPath.getBoolean(fieldName).isNull();

        }

        // If written expression is ISNOTNULL operation
        Pattern isNotNullOperator = Pattern.compile(String.format("%s",
                isNotNullOperation));
        Matcher isNotNullMatcher = isNotNullOperator.matcher(searchStr);
        if (isNotNullMatcher.matches()) {
            return entityPath.getBoolean(fieldName).isNotNull();

        }

        return null;
    }

    /**
     * Return IN expression for {@code entityPath.fieldName}.
     * <p/>
     * Expr: <br/>
     * entityPath.fieldName IN ( values ) <br/>
     * <br/>
     * If values.size() > 500 its generates: <br/>
     * Expr: <br/>
     * (entityPath.fieldName IN ( values[0-500] ) OR [entityPath.fieldName IN (
     * values[501-100]... ])) <br/>
     * <br/>
     * 
     * @param entityPath Full path to entity and associations. For example:
     *        {@code Pet} , {@code Pet.owner}
     * @param fieldName Property name in the given entity path. For example:
     *        {@code name} in {@code Pet} entity, {@code firstName} in
     *        {@code Pet.owner} entity.
     * @param values the Set of values to find the given field name, may be null
     * @return BooleanExpression
     */
    public static <T, E> BooleanExpression createCollectionExpression(
            PathBuilder<T> entityPath, String fieldName, Collection<E> values) {
        if (StringUtils.isEmpty(fieldName) || values.isEmpty()) {
            return null;
        }

        if (values.size() > 500) {
            BooleanExpression expression = null;
            Iterable<List<E>> collectionParts = Iterables
                    .partition(values, 500);
            for (List<E> part : collectionParts) {
                if (expression == null) {
                    expression = doCreateCollectionExpression(entityPath,
                            fieldName, part);
                }
                else {
                    expression = expression.or(doCreateCollectionExpression(
                            entityPath, fieldName, part));
                }
            }
            return expression;
        }
        else {
            return doCreateCollectionExpression(entityPath, fieldName, values);
        }
    }

    public static <T, E> BooleanExpression doCreateCollectionExpression(
            PathBuilder<T> entityPath, String fieldName, Collection<E> values) {
        BooleanExpression expression = entityPath.get(fieldName).in(values);
        return expression;
    }

    /**
     * Create an order-by-element in a Query instance
     * 
     * @param entityPath Full path to entity and associations. For example:
     *        {@code Pet} , {@code Pet.owner}
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

    /**
     * This method returns the query expression based on String expression
     * user-written.
     * 
     * Expression can be "=", ">", "<", ">=", "<=", "<>", "!=",
     * "ENTRENUMERO(n1;n2)"
     * 
     * @param searchStr
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <T, N extends java.lang.Number & java.lang.Comparable<?>> BooleanExpression getNumericFilterExpression(
            PathBuilder<T> entityPath, String fieldName, Class<N> fieldType,
            TypeDescriptor descriptor, String searchStr,
            ConversionService conversionService, MessageSource messageSource) {

        if (StringUtils.isEmpty(searchStr)) {
            return null;
        }

        TypeDescriptor strDesc = STRING_TYPE_DESCRIPTOR;

        NumberPath<N> numberExpression = entityPath.getNumber(fieldName,
                fieldType);

        // If written expression is a symbol operation expression

        // Getting expressions with symbols
        Pattern symbolOperator = Pattern.compile("([!=><][=>]?)([-]?[\\d.,]*)");
        Matcher symbolMatcher = symbolOperator.matcher(searchStr);

        if (symbolMatcher.matches()) {

            String symbolExpression = symbolMatcher.group(1);
            String value = symbolMatcher.group(2);

            if (!StringUtils.isBlank(value)) {

                Object valueConverted = conversionService.convert(value,
                        strDesc, descriptor);

                if (symbolExpression.equals("=")
                        || symbolExpression.equals("==")) {
                    return numberExpression.eq((N) valueConverted);
                }
                else if (symbolExpression.equals(">")
                        || symbolExpression.equals(">>")) {
                    return numberExpression.gt((N) valueConverted);
                }
                else if (symbolExpression.equals("<")) {
                    return numberExpression.lt((N) valueConverted);
                }
                else if (symbolExpression.equals(">=")) {
                    return numberExpression.goe((N) valueConverted);
                }
                else if (symbolExpression.equals("<=")) {
                    return numberExpression.loe((N) valueConverted);
                }
                else if (symbolExpression.equals("!=")
                        || symbolExpression.equals("<>")) {
                    return numberExpression.ne((N) valueConverted);
                }
            }
        }

        // Get all operations
        String isNullOperation = OPERATOR_ISNULL;
        String isNotNullOperation = OPERATOR_NOTNULL;
        String betweenOperation = "BETWEEN";

        if (messageSource != null) {
            isNullOperation = messageSource.getMessage(G_FIL_OPE_ISNULL, null,
                    LocaleContextHolder.getLocale());
            isNotNullOperation = messageSource.getMessage(G_FIL_OPE_NOTNULL,
                    null, LocaleContextHolder.getLocale());
            betweenOperation = messageSource.getMessage(
                    "global.filters.operations.number.between", null,
                    LocaleContextHolder.getLocale());
        }

        // If written function is BETWEEN function
        Pattern betweenFunctionOperator = Pattern.compile(String.format(
                "%s[(]([-]?[\\d.,]*);([-]?[\\d.,]*)[)]", betweenOperation));
        Matcher betweenFunctionMatcher = betweenFunctionOperator
                .matcher(searchStr);

        if (betweenFunctionMatcher.matches()) {
            // Getting valueFrom and valueTo
            String valueFrom = betweenFunctionMatcher.group(1);
            String valueTo = betweenFunctionMatcher.group(2);

            Object valueFromConverted = conversionService.convert(valueFrom,
                    strDesc, descriptor);
            Object valueToConverted = conversionService.convert(valueTo,
                    strDesc, descriptor);

            if (!StringUtils.isBlank(valueFrom)
                    && !StringUtils.isBlank(valueTo)) {
                return numberExpression.between((N) valueFromConverted,
                        (N) valueToConverted);
            }
        }

        // If written expression is ISNULL operation
        Pattern isNullOperator = Pattern.compile(String.format("%s",
                isNullOperation));
        Matcher isNullMatcher = isNullOperator.matcher(searchStr);
        if (isNullMatcher.matches()) {
            return numberExpression.isNull();

        }

        // If written expression is ISNOTNULL operation
        Pattern isNotNullOperator = Pattern.compile(String.format("%s",
                isNotNullOperation));
        Matcher isNotNullMatcher = isNotNullOperator.matcher(searchStr);
        if (isNotNullMatcher.matches()) {
            return numberExpression.isNotNull();

        }

        return null;
    }

    /**
     * Obtains the class type of the property named as {@code fieldName} of the
     * entity.
     * 
     * @param fieldName the field name.
     * @param entity the entity with a property named as {@code fieldName}
     * @return the class type
     */
    public static <T> Class<?> getFieldType(String fieldName,
            PathBuilder<T> entity) {
        TypeDescriptor descriptor = getTypeDescriptor(fieldName, entity);
        return descriptor.getType();
    }

    /**
     * Obtains the class type of the property named as {@code fieldName} of the
     * entity.
     * 
     * @param fieldName the field name.
     * @param entity the entity with a property named as {@code fieldName}
     * @return the class type
     */
    public static <T> Class<?> getFieldType1(String fieldName,
            PathBuilder<T> entity) {
        Class<?> entityType = entity.getType();
        String fieldNameToFindType = fieldName;

        // Makes the array of classes to find fieldName agains them
        Class<?>[] classArray = ArrayUtils.<Class<?>> toArray(entityType);
        if (fieldName.contains(SEPARATOR_FIELDS)) {
            String[] fieldNameSplitted = StringUtils.split(fieldName,
                    SEPARATOR_FIELDS);
            for (int i = 0; i < fieldNameSplitted.length - 1; i++) {
                Class<?> fieldType = BeanUtils.findPropertyType(
                        fieldNameSplitted[i],
                        ArrayUtils.<Class<?>> toArray(entityType));
                classArray = ArrayUtils.add(classArray, fieldType);
                entityType = fieldType;
            }
            fieldNameToFindType = fieldNameSplitted[fieldNameSplitted.length - 1];
        }

        return BeanUtils.findPropertyType(fieldNameToFindType, classArray);
    }

    /**
     * Obtains the descriptor of the filtered field
     * 
     * @param fieldName
     * @param entity
     * @return
     */
    public static <T> TypeDescriptor getTypeDescriptor(String fieldName,
            PathBuilder<T> entity) {
        Class<?> entityType = entity.getType();
        if (entityType == Object.class) {
            // Remove from path the root "entity" alias
            String fromRootPath = entity.toString().replaceFirst("^[^.]+[.]",
                    "");
            TypeDescriptor fromRoot = getTypeDescriptor(fromRootPath, entity
                    .getRoot().getType());
            if (fromRoot == null) {
                return null;
            }
            entityType = fromRoot.getType();
        }
        return getTypeDescriptor(fieldName, entityType);
    }

    /**
     * Obtains the descriptor of the filtered field
     * 
     * @param fieldName
     * @param entityType
     * @return
     */
    public static <T> TypeDescriptor getTypeDescriptor(String fieldName,
            Class<T> entityType) {
        String fieldNameToFindType = fieldName;
        BeanWrapper beanWrapper = getBeanWrapper(entityType);

        TypeDescriptor fieldDescriptor = null;
        Class<?> propType = null;
        // Find recursive the las beanWrapper
        if (fieldName.contains(SEPARATOR_FIELDS)) {
            String[] fieldNameSplitted = StringUtils.split(fieldName,
                    SEPARATOR_FIELDS);
            for (int i = 0; i < fieldNameSplitted.length - 1; i++) {
                propType = beanWrapper.getPropertyType(fieldNameSplitted[i]);
                if (propType == null) {
                    throw new IllegalArgumentException(String.format(
                            "Property %s not found in %s (request %s.%s)",
                            fieldNameSplitted[i],
                            beanWrapper.getWrappedClass(), entityType,
                            fieldName));
                }
                beanWrapper = getBeanWrapper(propType);
            }
            fieldNameToFindType = fieldNameSplitted[fieldNameSplitted.length - 1];
        }
        fieldDescriptor = beanWrapper
                .getPropertyTypeDescriptor(fieldNameToFindType);

        return fieldDescriptor;
    }

    /**
     * This method checks if the search string can be converted to a number
     * using conversionService with locale.
     * 
     * @param searchStr
     * @param conversionService
     * @param descriptor
     * @return
     */
    public static boolean isNumber(String searchStr,
            ConversionService conversionService, TypeDescriptor descriptor) {

        return isValidValueFor(searchStr, descriptor, conversionService);

    }
}
