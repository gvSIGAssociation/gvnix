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
 * this program. If not, see &lt;http://www.gnu.org/copyleft/gpl.html&gt;.
 */
package org.gvnix.datatables.utils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.EntityType;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import com.github.dandelion.datatables.core.ajax.ColumnDef;
import com.github.dandelion.datatables.core.ajax.ColumnDef.SortDirection;
import com.github.dandelion.datatables.core.ajax.DatatablesCriterias;

/**
 * Datatables utility functions
 * 
 * @author gvNIX team
 */
public class DatatablesUtils {

    private static final Logger LOOGER = Logger.getLogger(DatatablesUtils.class
            .getCanonicalName());

    /**
     * Pojo which contains
     * {@link DatatablesUtils#findByCriteria(Class, EntityManager, DatatablesCriterias)}
     * result
     * 
     * @author gvNIX team
     * @param <T>
     */
    public static class FindResult<T> {

        /**
         * Create a result. This just can be done by {@link DatatablesUtils}
         * 
         * @param result
         * @param totalResultCount
         * @param isPagedResult
         * @param startItem
         * @param pageSize
         */
        private FindResult(List<T> result, long totalResultCount,
                boolean isPagedResult, long startItem, int pageSize) {
            super();
            this.result = result;
            this.totalResultCount = totalResultCount;
            this.isPagedResult = isPagedResult;
            this.startItem = startItem;
            this.pageSize = pageSize;
        }

        private final List<T> result;

        private final long totalResultCount;

        private final boolean isPagedResult;

        private final long startItem;

        private final int pageSize;

        /**
         * @return item result list
         */
        public List<T> getResult() {
            return result;
        }

        /**
         * @return total result of criteria
         */
        public long getTotalResultCount() {
            return totalResultCount;
        }

        /**
         * @return if {@link #getResult()} is paged
         */
        public boolean isPagedResult() {
            return isPagedResult;
        }

        /**
         * @return start item index of {@link #getResult()}
         */
        public long getStartItem() {
            return startItem;
        }

        /**
         * @return the page size required
         */
        public int getPageSize() {
            return pageSize;
        }

    }

    public static final Set<Class<?>> NUMBER_PRIMITIVES = new HashSet<Class<?>>(
            Arrays.asList(new Class<?>[] { int.class, long.class, double.class,
                    float.class, short.class }));

    /**
     * Execute a select query on entityClass using {@code DatatablesCriterias}
     * information for filter, sort and paginate result.
     * 
     * @param entityClass entity to use in search
     * @param entityManager {@code entityClass} {@link EntityManager}
     * @param datatablesCriterias datatables parameters for query
     * @return
     */
    public static <T> FindResult<T> findByCriteria(Class<T> entityClass,
            EntityManager entityManager, DatatablesCriterias datatablesCriterias) {
        return findByCriteria(entityClass, entityManager, datatablesCriterias,
                false);
    }

    /**
     * Execute a select query on entityClass using {@code DatatablesCriterias}
     * information for filter, sort and paginate result.
     * 
     * @param entityClass entity to use in search
     * @param entityManager {@code entityClass} {@link EntityManager}
     * @param datatablesCriterias datatables parameters for query
     * @param distinct use distinct query
     * @return
     */
    public static <T> FindResult<T> findByCriteria(Class<T> entityClass,
            EntityManager entityManager,
            DatatablesCriterias datatablesCriterias, boolean distinct) {

        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<T> query = builder.createQuery(entityClass);
        CriteriaQuery<Long> countQuery = builder.createQuery(Long.class);
        EntityType<T> entity = entityManager.getMetamodel().entity(entityClass);

        boolean isPaged = datatablesCriterias.getDisplaySize() != null
                && datatablesCriterias.getDisplaySize() > 0;

        Root<T> from = query.from(entityClass);

        Predicate filterCondition = null;
        Predicate searchCondition = null;

        try {
            // compute filters
            if (datatablesCriterias.hasOneFilteredColumn()) {
                // filters columns
                List<Predicate> condition = new ArrayList<Predicate>();
                for (ColumnDef column : datatablesCriterias.getColumnDefs()) {
                    if (column.isFilterable()
                            && StringUtils.isNotBlank(column.getSearch())) {
                        Predicate fieldlike = getFindCondition(entity, from,
                                column.getName(), column.getSearch(), builder,
                                true);
                        if (fieldlike != null) {
                            condition.add(fieldlike);
                        }
                    }
                }
                // for filter use "and" operator
                if (!condition.isEmpty()) {
                    filterCondition = builder.and(condition
                            .toArray(new Predicate[] {}));
                }

            }

            // Compute search
            String toSearch = datatablesCriterias.getSearch();
            if (StringUtils.isNotBlank(toSearch)
                    && datatablesCriterias.hasOneFilterableColumn()) {
                // Search by filterable columns
                List<Predicate> condition = new ArrayList<Predicate>();
                for (ColumnDef column : datatablesCriterias.getColumnDefs()) {
                    if (column.isFilterable()) {
                        Predicate fieldlike = getFindCondition(entity, from,
                                column.getName(), toSearch, builder, false);
                        if (fieldlike != null) {
                            condition.add(fieldlike);
                        }
                    }
                }
                // for search use "or" operator
                if (!condition.isEmpty()) {
                    searchCondition = builder.or(condition
                            .toArray(new Predicate[] {}));
                }

            }
        }
        catch (EmptyResultException e) {
            return new FindResult<T>(new ArrayList<T>(0), 0, isPaged,
                    (long) datatablesCriterias.getDisplayStart(),
                    datatablesCriterias.getDisplaySize());
        }

        // join filter and search conditions
        if (filterCondition != null && searchCondition != null) {
            // it has filter and search join with and operator
            query.where(builder.and(filterCondition, searchCondition));
            countQuery.where(builder.and(filterCondition, searchCondition));
        }
        else if (filterCondition != null) {
            // only filter
            query.where(filterCondition);
            countQuery.where(filterCondition);

        }
        else if (searchCondition != null) {
            // only search
            query.where(searchCondition);
            countQuery.where(searchCondition);
        }

        // Add order by
        if (datatablesCriterias.hasOneSortedColumn()) {
            List<Order> orderList = new ArrayList<Order>();
            for (ColumnDef column : datatablesCriterias.getSortingColumnDefs()) {
                if (column.getSortDirection() == null) {
                    continue;
                }
                Path<Object> col;
                try {
                    col = from.get(column.getName());
                }
                catch (Exception e) {
                    col = null;
                }
                if (col == null) {
                    // column not found in entity (could be a transient):
                    // ignore this column
                    LOOGER.finer("Ignoring column '"
                            .concat(column.getName())
                            .concat("': not found in entity (could be a transient?)"));
                    continue;
                }

                if (column.getSortDirection() == SortDirection.ASC) {
                    orderList.add(builder.asc(col));
                }
                else {
                    orderList.add(builder.desc(col));
                }
            }
            if (!orderList.isEmpty()) {
                query.orderBy(orderList);
            }
        }

        // Execute select query
        CriteriaQuery<T> select = query.select(from);
        CriteriaQuery<Long> count = null;

        if (distinct) {
            count = countQuery.select(builder.countDistinct(countQuery
                    .from(entityClass)));
        }
        else {
            count = countQuery.select(builder.count(countQuery
                    .from(entityClass)));
        }

        select.distinct(distinct);

        TypedQuery<T> typedQuery = entityManager.createQuery(select);

        long startItem = 0;
        int pageSize = 0;
        // pagination
        if (isPaged) {
            pageSize = datatablesCriterias.getDisplaySize();
            typedQuery.setMaxResults(pageSize);
        }
        if (datatablesCriterias.getDisplayStart() >= 0) {
            startItem = datatablesCriterias.getDisplayStart();
            typedQuery.setFirstResult((int) startItem);

        }

        List<T> elements = typedQuery.getResultList();
        long totalResultCount = elements.size();

        if (isPaged) {
            // execute count query
            totalResultCount = entityManager.createQuery(count)
                    .getSingleResult();
        }

        return new FindResult<T>(elements, totalResultCount, isPaged,
                startItem, pageSize);
    }

    /**
     * Return find condition for {@code entity.fieldName} base on
     * {@code stringExpression} expression
     * 
     * @param entity
     * @param from
     * @param fieldName
     * @param stringExpression
     * @param builder
     * @param force require an expression (if not applicable return a
     *            {@code false} expression
     * @return condition or null if (field not found || field type not supported
     *         || stringExpression not applicable)
     * @throws EmptyResultException
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static <T> Predicate getFindCondition(EntityType<T> entity,
            Root<T> from, String fieldName, String stringExpression,
            CriteriaBuilder builder, boolean force) throws EmptyResultException {
        Attribute<? super T, ?> field = null;
        try {
            field = entity.getAttribute(fieldName);
        }
        catch (IllegalArgumentException e) {
            // field not found in entity (could be a transient):
            // ignore this field
            LOOGER.finer("Ignoring column '".concat(fieldName).concat(
                    "': not found in entity (could be a transient?)"));
            return null;
        }
        if (field == null) {
            // ignore not-found and not-basic fields
            return null;
        }

        Class<?> type = field.getJavaType();
        if (String.class == type) {
            return getStringFindCondition(from, stringExpression, builder,
                    field);
        }
        else if (Boolean.class == type || boolean.class == type) {
            return getBooleanFindCondition(from, stringExpression, builder,
                    field, force);
        }
        else if (Number.class.isAssignableFrom(type)
                || NUMBER_PRIMITIVES.contains(type)) {
            if (NumberUtils.isNumber(stringExpression)) {
                return getTextSearchFindCondition(from, stringExpression,
                        builder, field, false);
            }
            else {
                if (force) {
                    throw new EmptyResultException();
                }
                return null;
            }
        }
        else if (Date.class.isAssignableFrom(type)
                || Calendar.class.isAssignableFrom(type)) {
            return getTextSearchFindCondition(from, stringExpression, builder,
                    field, true);
        }
        else if (type.isEnum()) {
            return getEnumFindCondition(from, stringExpression, builder, field,
                    (Class<? extends Enum>) type, force);
        }

        // TODO manage other types
        return null;

    }

    /**
     * Return find condition for {@code entity.fieldName} string type field base
     * on {@code likeString} expression. This method suppose {@code likeString}
     * is a plain string. The result expression will be '%' +{@code likeString}+
     * '%'. Like operation will be case insensitive.
     * 
     * @param from
     * @param likeString
     * @param builder
     * @return
     */
    private static <T> Predicate getStringFindCondition(Root<T> from,
            String likeString, CriteriaBuilder builder,
            Attribute<? super T, ?> field) {
        return builder.like(builder.lower(from.<String> get(field.getName())),
                "%".concat(likeString.toLowerCase()).concat("%"));
    }

    /**
     * Return find condition for {@code entity.fieldName} enum type field base
     * on {@code likeString} expression. This method suppose {@code likeString}
     * is a plain string. Uses an IN operator by every matching enum constant.
     * Enum constant is compare by {@code name} attribute an {@code toString}
     * method.
     * 
     * @param from
     * @param stringToSearch
     * @param builder
     * @param field
     * @param enumClass
     * @param force expression else throws a {@link EmptyResultException}
     * @return
     * @throws EmptyResultException
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    private static <T> Predicate getEnumFindCondition(Root<T> from,
            String stringToSearch, CriteriaBuilder builder,
            Attribute<? super T, ?> field, Class<? extends Enum> enumClass,
            boolean force) throws EmptyResultException {

        // TODO i18n of enum name

        // Filter string to search than cann't be a identifier
        if (!StringUtils.isAlphanumeric(stringToSearch)) {
            return null;
        }
        stringToSearch = stringToSearch.toLowerCase();

        // locate enums matching by name
        Set matching = new HashSet();

        Enum<?> enumValue;
        String enumStr;

        for (Field enumField : enumClass.getDeclaredFields()) {
            if (enumField.isEnumConstant()) {
                enumStr = enumField.getName();
                enumValue = Enum.valueOf(enumClass, enumStr);

                // Check enum name contains string to search
                if (enumStr.toLowerCase().contains(stringToSearch)) {
                    // Add to matching enum
                    matching.add(enumValue);
                    continue;
                }

                // Check using toString
                enumStr = enumValue.toString();
                if (enumStr.toLowerCase().contains(stringToSearch)) {
                    // Add to matching enum
                    matching.add(enumValue);
                }
            }
        }
        if (matching.isEmpty()) {
            // no matching constants
            if (force) {
                throw new EmptyResultException();
            }
            return null;
        }

        // create a enum in manching condition
        return from.<String> get(field.getName()).in(matching);
    }

    /**
     * Return find condition for {@code entity.fieldName} based on text
     * representation of the field. This condition try to tranform value to text
     * and use {@code like} operator.
     * 
     * @param from
     * @param likeString
     * @param builder
     * @param field
     * @param caseInsensitive
     * @return
     */
    private static <T> Predicate getTextSearchFindCondition(Root<T> from,
            String likeString, CriteriaBuilder builder,
            Attribute<? super T, ?> field, boolean caseInsensitive) {
        Expression<String> fieldExpression = from.<String> get(field.getName())
                .as(String.class);
        if (caseInsensitive) {
            fieldExpression = builder.lower(fieldExpression);
        }
        return builder
                .like(fieldExpression, "%".concat(likeString).concat("%"));
    }

    /**
     * Return find condition for {@code entity.fieldName} boolean type field
     * base on {@code booleanExpression} expression. This method try to
     * transform the expression to a boolean and compares by equals.
     * 
     * @param from
     * @param booleanExpression
     * @param builder
     * @param field
     * @param force to generate expression else throws EmptyResultException
     * @return
     * @throws EmptyResultException
     * @see BooleanUtils#toBooleanObject(String)
     */
    private static <T> Predicate getBooleanFindCondition(Root<T> from,
            String booleanExpression, CriteriaBuilder builder,
            Attribute<? super T, ?> field, boolean force)
            throws EmptyResultException {

        if (StringUtils.isBlank(booleanExpression)) {
            return null;
        }

        booleanExpression = booleanExpression.trim().toLowerCase();

        Boolean value = null;
        if ("si".equals(booleanExpression)) {
            // support to Spanish yes
            value = Boolean.TRUE;
        }
        else {
            value = BooleanUtils.toBooleanObject(booleanExpression);
        }
        if (value == null) {
            if (force) {
                throw new EmptyResultException();
            }
            return null;
        }
        else {
            return builder.equal(from.<String> get(field.getName()),
                    builder.literal(value));

        }
    }

    /**
     * This exception is used by
     * {@link DatatablesUtils#getFindCondition(EntityType, Root, String, String, CriteriaBuilder, boolean)}
     * to notify there is a condition that makes the query return none register. <br>
     * So, there is no point in continue evaluating or execute the query.
     * 
     * @author jmvivo
     */
    private static class EmptyResultException extends Exception {

        /**
		 * 
		 */
        private static final long serialVersionUID = -2092608552005392751L;

    }
}
