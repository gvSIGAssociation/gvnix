/*
 * Copyright 2015 DiSiD Technologies S.L.L. All rights reserved.
 * 
 * Project  : DiSiD org.gvnix.web.datatables 
 * SVN Id   : $Id$
 */
package org.gvnix.web.datatables.util;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.springframework.core.convert.TypeDescriptor;

import com.mysema.query.BooleanBuilder;
import com.mysema.query.types.Order;
import com.mysema.query.types.OrderSpecifier;
import com.mysema.query.types.Predicate;
import com.mysema.query.types.expr.BooleanExpression;
import com.mysema.query.types.path.PathBuilder;

public interface QuerydslUtilsBean {

    public static final String OPERATOR_GOE = "goe";
    public static final String OPERATOR_LOE = "loe";
    public static final String OPERATOR_ISNULL = "isnull";
    public static final String OPERATOR_NOTNULL = "notnull";
    public static final String G_FIL_OPE_ISNULL = "global.filters.operations.all.isnull";
    public static final String G_FIL_OPE_NOTNULL = "global.filters.operations.all.notnull";
    public static final TypeDescriptor STRING_TYPE_DESCRIPTOR = TypeDescriptor
            .valueOf(String.class);
    public static final Set<Class<?>> NUMBER_PRIMITIVES = new HashSet<Class<?>>(
            Arrays.asList(new Class<?>[] { int.class, long.class, double.class,
                    float.class, short.class }));
    public static final String OPERATOR_PREFIX = "_operator_";
    public static final String SEPARATOR_FIELDS = ".";
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
    public <T> BooleanBuilder createPredicateByAnd(PathBuilder<T> entity,
            Map<String, Object> searchArgs);

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
    public <T, E> BooleanBuilder createPredicateByIn(PathBuilder<T> entity,
            String fieldName, Set<E> values);

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
     * 
     */
    public <T> Predicate createExpression(PathBuilder<T> entityPath,
            String fieldName, Class<?> fieldType, String searchStr);

    /**
     * Utility for constructing where clause expressions.
     * 
     * @param entityPath Full path to entity and associations. For example:
     *        {@code Pet} , {@code Pet.owner}
     * @param fieldName Property name in the given entity path. For example:
     *        {@code name} in {@code Pet} entity, {@code firstName} in
     *        {@code Pet.owner} entity.
     * @param searchStr the value to find, may be null
     * @return predicate
     */
    public <T> Predicate createExpression(PathBuilder<T> entityPath,
            String fieldName, String searchStr);

    public <T> Predicate createNumberExpressionGenerics(
            PathBuilder<T> entityPath, String fieldName, Class<?> fieldType,
            TypeDescriptor descriptor, String searchStr);

    public <T> Predicate createNumberExpressionGenericsWithOperators(
            PathBuilder<T> entityPath, String fieldName,
            TypeDescriptor descriptor, String searchStr);

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
    public <T> BooleanExpression createObjectExpression(
            PathBuilder<T> entityPath, String fieldName, Object searchObj);

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
    public <T> BooleanExpression createObjectExpression(
            PathBuilder<T> entityPath, String fieldName, Object searchObj,
            String operator);

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
    public <T> BooleanExpression createDateExpression(
            PathBuilder<T> entityPath, String fieldName, Object searchObj,
            String operator, Class<?> fieldType);

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
    public <T> BooleanExpression createNumericExpression(
            PathBuilder<T> entityPath, String fieldName, Object searchObj,
            String operator, Class<?> fieldType);

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
    public <T> BooleanExpression createBooleanExpression(
            PathBuilder<T> entityPath, String fieldName, Object searchObj,
            String operator);

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
    public <T> BooleanExpression createStringExpression(
            PathBuilder<T> entityPath, String fieldName, Object searchObj,
            String operator);

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
    public <T> BooleanExpression createStringExpression(
            PathBuilder<T> entityPath, String fieldName, String searchStr);

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
    public <T> BooleanExpression createStringLikeExpression(
            PathBuilder<T> entityPath, String fieldName, String searchStr);

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
    public <T> BooleanExpression createStringExpressionWithOperators(
            PathBuilder<T> entityPath, String fieldName, String searchStr);

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
     * @param fieldType Property value {@code Class}
     * @param descriptor
     * @param searchStr the value to find, may be null
     * @return PredicateOperation
     */
    public <T, N extends java.lang.Number & java.lang.Comparable<?>> BooleanExpression createNumberExpression(
            PathBuilder<T> entityPath, String fieldName, Class<N> fieldType,
            TypeDescriptor descriptor, String searchStr);

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
     * @param fieldType Property value {@code Class}
     * @ param descriptor       
     * @param searchStr the value to find, may be null
     * @return PredicateOperation
     */
    public <T, N extends java.lang.Number & java.lang.Comparable<?>> BooleanExpression createNumberExpressionEqual(
            PathBuilder<T> entityPath, String fieldName, Class<N> fieldType,
            TypeDescriptor descriptor, String searchStr);

    /**
     * Return where clause expression for date properties, trying to parse the
     * value to find to date and comparing it to the value of the date; if the
     * value to find cannot be parsed to date, then try to cast the value to
     * string before check it.
     * <p/>
     * <ul>
     * <li>If value to find {@code searchStr} can be parsed using the patterns
     * <em>dd-MM-yyyy HH:mm:ss</em> or <em>dd-MM-yyyy HH:mm</em> or
     * <em>dd-MM-yyyy</em> to {@code searchDate}, then search by specific date:
     * <p/>
     * - Querydsl Expr: {@code entityPath.fieldName = searchDate}
     * <p/>
     * - Database operation: {@code entity.fieldName = searchDate}</li>
     * <li>If value to find {@code searchStr} can be parsed using the pattern
     * <em>dd-MM</em> to {@code searchDate}, then search by specific day and
     * month:
     * <p/>
     * - Querydsl Expr:
     * {@code entityPath.fieldName.dayOfMonth() = searchDate.day and entityPath.fieldName.month() = searchDate.month}
     * <p/>
     * - Database operation:
     * {@code dayofmonth(entity.fieldName) = searchDate.day && month(entity.fieldName) = searchDate.month}
     * </li>
     * <li>If value to find {@code searchStr} can be parsed using the pattern
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
     * <li>If value to find cannot be parsed as date, then try to cast the value
     * to string before check it:
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
     * @param fieldType Property value {@code Class}
     * @param searchStr the value to find, may be null
     * @return PredicateOperation
     */
    public <T, C extends java.lang.Comparable<?>> BooleanExpression createDateExpression(
            PathBuilder<T> entityPath, String fieldName, Class<C> fieldType,
            String searchStr);

    /**
     * Return where clause expression for date properties, trying to parse the
     * value to find to date and comparing it to the value of the date; if the
     * value to find cannot be parsed to date, then try to cast the value to
     * string before check it.
     * <p/>
     * <ul>
     * <li>If value to find {@code searchStr} can be parsed using the patterns
     * <em>dd-MM-yyyy HH:mm:ss</em> or <em>dd-MM-yyyy HH:mm</em> or
     * <em>dd-MM-yyyy</em> to {@code searchDate}, then search by specific date:
     * <p/>
     * - Querydsl Expr: {@code entityPath.fieldName = searchDate}
     * <p/>
     * - Database operation: {@code entity.fieldName = searchDate}</li>
     * <li>If value to find {@code searchStr} can be parsed using the pattern
     * <em>dd-MM</em> to {@code searchDate}, then search by specific day and
     * month:
     * <p/>
     * - Querydsl Expr:
     * {@code entityPath.fieldName.dayOfMonth() = searchDate.day and entityPath.fieldName.month() = searchDate.month}
     * <p/>
     * - Database operation:
     * {@code dayofmonth(entity.fieldName) = searchDate.day && month(entity.fieldName) = searchDate.month}
     * </li>
     * <li>If value to find {@code searchStr} can be parsed using the pattern
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
     * <li>If value to find cannot be parsed as date, then try to cast the value
     * to string before check it:
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
     * @param fieldType Property value {@code Class}
     * @param searchStr the value to find, may be null
     * @param datePattern
     * @return PredicateOperation
     */
    public <T, C extends java.lang.Comparable<?>> BooleanExpression createDateExpressionWithOperators(
            PathBuilder<T> entityPath, String fieldName, Class<C> fieldType,
            String searchStr, String datePattern);

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
    @SuppressWarnings({ "rawtypes" })
    public <T> BooleanExpression createEnumExpression(
            PathBuilder<T> entityPath, String fieldName, String searchStr,
            Class<? extends Enum> enumClass);

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
    public <T> BooleanExpression createBooleanExpression(
            PathBuilder<T> entityPath, String fieldName, String searchStr);

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
    public <T> BooleanExpression createBooleanExpressionWithOperators(
            PathBuilder<T> entityPath, String fieldName, String searchStr);

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
    public <T, E> BooleanExpression createCollectionExpression(
            PathBuilder<T> entityPath, String fieldName, Collection<E> values);

    public <T, E> BooleanExpression doCreateCollectionExpression(
            PathBuilder<T> entityPath, String fieldName, Collection<E> values);

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
    public <T, E extends Comparable<?>> OrderSpecifier<?> createOrderSpecifier(
            PathBuilder<T> entityPath, String fieldName, Class<E> fieldType,
            Order order);

    /**
     * This method returns the query expression based on String expression
     * user-written. Expression can be "=", ">", "<", ">=", "<=", "<>", "!=",
     * "ENTRENUMERO(n1;n2)"
     * 
     * @param entityPath Full path to entity and associations. For example:
     *        {@code Pet} , {@code Pet.owner}
     * @param fieldName Property name in the given entity path. For example:
     *        {@code weight} in {@code Pet} entity, {@code age} in
     *        {@code Pet.owner} entity.
     * @param fieldType Property value {@code Class}. Must implements
     *        {@link Comparable}
     * @param descriptor
     * @param searchStr
     * @return
     */
    public <T, N extends java.lang.Number & java.lang.Comparable<?>> BooleanExpression getNumericFilterExpression(
            PathBuilder<T> entityPath, String fieldName, Class<N> fieldType,
            TypeDescriptor descriptor, String searchStr);

    /**
     * Obtains the class type of the property named as {@code fieldName} of the
     * entity.
     * 
     * @param fieldName the field name.
     * @param entity the entity with a property named as {@code fieldName}
     * @return the class type
     */
    public <T> Class<?> getFieldType(String fieldName, PathBuilder<T> entity);

    /**
     * Obtains the class type of the property named as {@code fieldName} of the
     * entity.
     * 
     * @param fieldName the field name.
     * @param entity the entity with a property named as {@code fieldName}
     * @return the class type
     */
    public <T> Class<?> getFieldType1(String fieldName, PathBuilder<T> entity);

    /**
     * Obtains the descriptor of the filtered field
     * 
     * @param fieldName
     * @param entity
     * @return
     */
    public <T> TypeDescriptor getTypeDescriptor(String fieldName,
            PathBuilder<T> entity);

    /**
     * Obtains the descriptor of the filtered field
     * 
     * @param fieldName
     * @param entityType
     * @return
     */
    public <T> TypeDescriptor getTypeDescriptor(String fieldName,
            Class<T> entityType);

    /**
     * This method checks if the search string can be converted to a number
     * using conversionService with locale.
     * 
     * @param searchStr
     * @param conversionService
     * @param descriptor
     * @return
     */
    public boolean isNumber(String searchStr, TypeDescriptor descriptor);
}
