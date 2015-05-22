/*
 * Copyright 2015 DiSiD Technologies S.L.L. All rights reserved.
 * 
 * Project  : DiSiD org.gvnix.web.datatables 
 * SVN Id   : $Id$
 */
package org.gvnix.web.datatables.util.impl;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
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
import org.gvnix.web.datatables.util.EntityManagerProvider;
import org.gvnix.web.datatables.util.QuerydslUtilsBean;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.factory.annotation.Autowired;
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

public class QuerydslUtilsBeanImpl implements QuerydslUtilsBean {

    @Autowired
    private ConversionService conversionService;

    @Autowired
    private MessageSource messageSource;

    @Autowired
    private EntityManagerProvider entityManagerProvider;

    private static LoadingCache<Class<?>, BeanWrapper> beanWrappersCache = CacheBuilder
            .newBuilder().maximumSize(200)
            .build(new CacheLoader<Class<?>, BeanWrapper>() {

                public BeanWrapper load(Class<?> key) {
                    return new BeanWrapperImpl(key);
                }
            });

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
     * {@inheritDoc}
     */
    @Override
    public <T> BooleanBuilder createPredicateByAnd(PathBuilder<T> entity,
            Map<String, Object> searchArgs) {

        // Using BooleanBuilder, a cascading builder for
        // Predicate expressions
        BooleanBuilder predicate = new BooleanBuilder();
        if (searchArgs == null || searchArgs.isEmpty()) {
            return predicate;
        }

        // Build the predicate
        for (Entry<String, Object> entry : searchArgs.entrySet()) {
            String key = entry.getKey();
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
                    predicate.and(createObjectExpression(entity, key, valueObj,
                            operator));
                }
            }
            else {
                predicate.and(createObjectExpression(entity, key,
                        valueToSearch, operator));
            }
        }
        return predicate;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T, E> BooleanBuilder createPredicateByIn(PathBuilder<T> entity,
            String fieldName, Set<E> values) {

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
     * {@inheritDoc}
     */
    @Override
    public <T> Predicate createExpression(PathBuilder<T> entityPath,
            String fieldName, Class<?> fieldType, String searchStr) {

        return createExpression(entityPath, fieldName, searchStr);

    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public <T> Predicate createExpression(PathBuilder<T> entityPath,
            String fieldName, String searchStr) {
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
                    searchStr);
        }
        else if (Boolean.class == fieldType || boolean.class == fieldType) {
            return createBooleanExpressionWithOperators(entityPath, fieldName,
                    searchStr);
        }
        else if (Number.class.isAssignableFrom(fieldType)
                || NUMBER_PRIMITIVES.contains(fieldType)) {
            return createNumberExpressionGenericsWithOperators(entityPath,
                    fieldName, descriptor, searchStr);
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
                    datePattern);
            return expression;
        }

        else if (fieldType.isEnum()) {
            return createEnumExpression(entityPath, fieldName, searchStr,
                    (Class<? extends Enum>) fieldType);
        }
        return null;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Predicate createNumberExpressionGenerics(
            PathBuilder<T> entityPath, String fieldName, Class<?> fieldType,
            TypeDescriptor descriptor, String searchStr) {
        Predicate numberExpression = null;

        if (isNumber(searchStr, descriptor)) {
            if (BigDecimal.class.isAssignableFrom(fieldType)) {
                numberExpression = createNumberExpression(entityPath,
                        fieldName, (Class<BigDecimal>) fieldType, descriptor,
                        searchStr);
            }
            if (BigInteger.class.isAssignableFrom(fieldType)) {
                numberExpression = createNumberExpression(entityPath,
                        fieldName, (Class<BigInteger>) fieldType, descriptor,
                        searchStr);
            }
            if (Byte.class.isAssignableFrom(fieldType)) {
                numberExpression = createNumberExpression(entityPath,
                        fieldName, (Class<Byte>) fieldType, descriptor,
                        searchStr);
            }
            if (Double.class.isAssignableFrom(fieldType)
                    || double.class == fieldType) {
                numberExpression = createNumberExpression(entityPath,
                        fieldName, (Class<Double>) fieldType, descriptor,
                        searchStr);
            }
            if (Float.class.isAssignableFrom(fieldType)
                    || float.class == fieldType) {
                numberExpression = createNumberExpression(entityPath,
                        fieldName, (Class<Float>) fieldType, descriptor,
                        searchStr);
            }
            if (Integer.class.isAssignableFrom(fieldType)
                    || int.class == fieldType) {
                numberExpression = createNumberExpression(entityPath,
                        fieldName, (Class<Integer>) fieldType, descriptor,
                        searchStr);
            }
            if (Long.class.isAssignableFrom(fieldType)
                    || long.class == fieldType) {
                numberExpression = createNumberExpression(entityPath,
                        fieldName, (Class<Long>) fieldType, descriptor,
                        searchStr);
            }
            if (Short.class.isAssignableFrom(fieldType)
                    || short.class == fieldType) {
                numberExpression = createNumberExpression(entityPath,
                        fieldName, (Class<Short>) fieldType, descriptor,
                        searchStr);
            }
        }
        return numberExpression;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Predicate createNumberExpressionGenericsWithOperators(
            PathBuilder<T> entityPath, String fieldName,
            TypeDescriptor descriptor, String searchStr) {
        Predicate numberExpression = null;

        Class<?> fieldType = descriptor.getType();

        if (isNumber(searchStr, descriptor)) {
            if (BigDecimal.class.isAssignableFrom(fieldType)) {
                numberExpression = createNumberExpressionEqual(entityPath,
                        fieldName, (Class<BigDecimal>) fieldType, descriptor,
                        searchStr);
            }
            if (BigInteger.class.isAssignableFrom(fieldType)) {
                numberExpression = createNumberExpressionEqual(entityPath,
                        fieldName, (Class<BigInteger>) fieldType, descriptor,
                        searchStr);
            }
            if (Byte.class.isAssignableFrom(fieldType)) {
                numberExpression = createNumberExpressionEqual(entityPath,
                        fieldName, (Class<Byte>) fieldType, descriptor,
                        searchStr);
            }
            if (Double.class.isAssignableFrom(fieldType)
                    || double.class == fieldType) {
                numberExpression = createNumberExpressionEqual(entityPath,
                        fieldName, (Class<Double>) fieldType, descriptor,
                        searchStr);
            }
            if (Float.class.isAssignableFrom(fieldType)
                    || float.class == fieldType) {
                numberExpression = createNumberExpressionEqual(entityPath,
                        fieldName, (Class<Float>) fieldType, descriptor,
                        searchStr);
            }
            if (Integer.class.isAssignableFrom(fieldType)
                    || int.class == fieldType) {
                numberExpression = createNumberExpressionEqual(entityPath,
                        fieldName, (Class<Integer>) fieldType, descriptor,
                        searchStr);
            }
            if (Long.class.isAssignableFrom(fieldType)
                    || long.class == fieldType) {
                numberExpression = createNumberExpressionEqual(entityPath,
                        fieldName, (Class<Long>) fieldType, descriptor,
                        searchStr);
            }
            if (Short.class.isAssignableFrom(fieldType)
                    || short.class == fieldType) {
                numberExpression = createNumberExpressionEqual(entityPath,
                        fieldName, (Class<Short>) fieldType, descriptor,
                        searchStr);
            }
        }
        else {
            // If is not a number, can be possible that exists a filter
            // expression.
            if (BigDecimal.class.isAssignableFrom(fieldType)) {
                numberExpression = getNumericFilterExpression(entityPath,
                        fieldName, (Class<BigDecimal>) fieldType, descriptor,
                        searchStr);
            }
            if (BigInteger.class.isAssignableFrom(fieldType)) {
                numberExpression = getNumericFilterExpression(entityPath,
                        fieldName, (Class<BigInteger>) fieldType, descriptor,
                        searchStr);
            }
            if (Byte.class.isAssignableFrom(fieldType)) {
                numberExpression = getNumericFilterExpression(entityPath,
                        fieldName, (Class<Byte>) fieldType, descriptor,
                        searchStr);
            }
            if (Double.class.isAssignableFrom(fieldType)
                    || double.class == fieldType) {
                numberExpression = getNumericFilterExpression(entityPath,
                        fieldName, (Class<Double>) fieldType, descriptor,
                        searchStr);
            }
            if (Float.class.isAssignableFrom(fieldType)
                    || float.class == fieldType) {
                numberExpression = getNumericFilterExpression(entityPath,
                        fieldName, (Class<Float>) fieldType, descriptor,
                        searchStr);
            }
            if (Integer.class.isAssignableFrom(fieldType)
                    || int.class == fieldType) {
                numberExpression = getNumericFilterExpression(entityPath,
                        fieldName, (Class<Integer>) fieldType, descriptor,
                        searchStr);
            }
            if (Long.class.isAssignableFrom(fieldType)
                    || long.class == fieldType) {
                numberExpression = getNumericFilterExpression(entityPath,
                        fieldName, (Class<Long>) fieldType, descriptor,
                        searchStr);
            }
            if (Short.class.isAssignableFrom(fieldType)
                    || short.class == fieldType) {
                numberExpression = getNumericFilterExpression(entityPath,
                        fieldName, (Class<Short>) fieldType, descriptor,
                        searchStr);
            }
        }
        return numberExpression;

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> BooleanExpression createObjectExpression(
            PathBuilder<T> entityPath, String fieldName, Object searchObj) {
        return createObjectExpression(entityPath, fieldName, searchObj, null);

    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public <T> BooleanExpression createObjectExpression(
            PathBuilder<T> entityPath, String fieldName, Object searchObj,
            String operator) {
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
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    public <T> BooleanExpression createDateExpression(
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
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public <T> BooleanExpression createNumericExpression(
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
     * {@inheritDoc}
     */
    @Override
    public <T> BooleanExpression createBooleanExpression(
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
     * {@inheritDoc}
     */
    @Override
    public <T> BooleanExpression createStringExpression(
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
     * {@inheritDoc}
     */
    @Override
    public <T> BooleanExpression createStringExpression(
            PathBuilder<T> entityPath, String fieldName, String searchStr) {
        if (StringUtils.isEmpty(searchStr)) {
            return null;
        }
        BooleanExpression expression = entityPath.getString(fieldName).lower()
                .eq(searchStr.toLowerCase());
        return expression;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> BooleanExpression createStringLikeExpression(
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
     * {@inheritDoc}
     */
    @Override
    public <T> BooleanExpression createStringExpressionWithOperators(
            PathBuilder<T> entityPath, String fieldName, String searchStr) {
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
     * {@inheritDoc}
     */
    @Override
    public <T, N extends Number & Comparable<?>> BooleanExpression createNumberExpression(
            PathBuilder<T> entityPath, String fieldName, Class<N> fieldType,
            TypeDescriptor descriptor, String searchStr) {
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
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    public <T, N extends Number & Comparable<?>> BooleanExpression createNumberExpressionEqual(
            PathBuilder<T> entityPath, String fieldName, Class<N> fieldType,
            TypeDescriptor descriptor, String searchStr) {
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
     * {@inheritDoc}
     */
    @Override
    public <T, C extends Comparable<?>> BooleanExpression createDateExpression(
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
     * {@inheritDoc}
     */
    @Override
    public <T, C extends Comparable<?>> BooleanExpression createDateExpressionWithOperators(
            PathBuilder<T> entityPath, String fieldName, Class<C> fieldType,
            String searchStr, String datePattern) {
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
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public <T> BooleanExpression createEnumExpression(
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
     * {@inheritDoc}
     */
    @Override
    public <T> BooleanExpression createBooleanExpression(
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
     * {@inheritDoc}
     */
    @Override
    public <T> BooleanExpression createBooleanExpressionWithOperators(
            PathBuilder<T> entityPath, String fieldName, String searchStr) {
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
     * {@inheritDoc}
     */
    @Override
    public <T, E> BooleanExpression createCollectionExpression(
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

    @Override
    public <T, E> BooleanExpression doCreateCollectionExpression(
            PathBuilder<T> entityPath, String fieldName, Collection<E> values) {
        BooleanExpression expression = entityPath.get(fieldName).in(values);
        return expression;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T, E extends Comparable<?>> OrderSpecifier<?> createOrderSpecifier(
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
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    public <T, N extends Number & Comparable<?>> BooleanExpression getNumericFilterExpression(
            PathBuilder<T> entityPath, String fieldName, Class<N> fieldType,
            TypeDescriptor descriptor, String searchStr) {
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
     * {@inheritDoc}
     */
    @Override
    public <T> Class<?> getFieldType(String fieldName, PathBuilder<T> entity) {
        TypeDescriptor descriptor = getTypeDescriptor(fieldName, entity);
        return descriptor.getType();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> Class<?> getFieldType1(String fieldName, PathBuilder<T> entity) {
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
     * {@inheritDoc}
     */
    @Override
    public <T> TypeDescriptor getTypeDescriptor(String fieldName,
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
     * {@inheritDoc}
     */
    @Override
    public <T> TypeDescriptor getTypeDescriptor(String fieldName,
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
     * {@inheritDoc}
     */
    @Override
    public boolean isNumber(String searchStr, TypeDescriptor descriptor) {
        return isValidValueFor(searchStr, descriptor, conversionService);
    }

    protected ConversionService getConversionService() {
        return conversionService;
    }

    protected MessageSource getMessageSource() {
        return messageSource;
    }

    protected EntityManagerProvider getEntityManagerProvider() {
        return entityManagerProvider;
    }

    protected static LoadingCache<Class<?>, BeanWrapper> getBeanWrappersCache() {
        return beanWrappersCache;
    }

}
