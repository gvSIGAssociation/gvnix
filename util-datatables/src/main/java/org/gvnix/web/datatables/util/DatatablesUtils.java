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
package org.gvnix.web.datatables.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.EntityManager;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.gvnix.web.datatables.query.SearchResults;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.core.convert.ConversionService;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import com.github.dandelion.datatables.core.ajax.ColumnDef;
import com.github.dandelion.datatables.core.ajax.ColumnDef.SortDirection;
import com.github.dandelion.datatables.core.ajax.DataSet;
import com.github.dandelion.datatables.core.ajax.DatatablesCriterias;
import com.mysema.query.BooleanBuilder;
import com.mysema.query.QueryModifiers;
import com.mysema.query.jpa.impl.JPAQuery;
import com.mysema.query.types.Order;
import com.mysema.query.types.OrderSpecifier;
import com.mysema.query.types.path.PathBuilder;

/**
 * Datatables utility functions
 * 
 * @author gvNIX team
 */
public class DatatablesUtils {

    // Logger
    private static Logger logger = LoggerFactory
            .getLogger(DatatablesUtils.class);

    /**
     * Execute a select query on entityClass using {@code DatatablesCriterias}
     * information for filter, sort and paginate result.
     * 
     * @param entityClass entity to use in search
     * @param entityManager {@code entityClass} {@link EntityManager}
     * @param datatablesCriterias datatables parameters for query
     * @return
     */
    public static <T> SearchResults<T> findByCriteria(Class<T> entityClass,
            EntityManager entityManager, DatatablesCriterias datatablesCriterias) {
        return findByCriteria(entityClass, null, null, entityManager,
                datatablesCriterias, (BooleanBuilder) null, false);
    }

    /**
     * Execute a select query on entityClass using {@code DatatablesCriterias}
     * information for filter, sort and paginate result.
     * 
     * @param entityClass entity to use in search
     * @param entityManager {@code entityClass} {@link EntityManager}
     * @param datatablesCriterias datatables parameters for query
     * @param baseSearchValuesMap (optional) base filter values
     * @return
     */
    public static <T> SearchResults<T> findByCriteria(Class<T> entityClass,
            EntityManager entityManager,
            DatatablesCriterias datatablesCriterias,
            Map<String, Object> baseSearchValuesMap) {
        return findByCriteria(entityClass, null, null, entityManager,
                datatablesCriterias, baseSearchValuesMap, false);
    }

    /**
     * Execute a select query on entityClass using {@code DatatablesCriterias}
     * information for filter, sort and paginate result.
     * 
     * @param entityClass entity to use in search
     * @param filterByAssociations (optional) for each related entity to join
     *            contain as key the name of the association and as value the
     *            List of related entity fields to filter by
     * @param orderByAssociations (optional) for each related entity to order
     *            contain as key the name of the association and as value the
     *            List of related entity fields to order by
     * @param entityManager {@code entityClass} {@link EntityManager}
     * @param datatablesCriterias datatables parameters for query
     * @return
     */
    public static <T> SearchResults<T> findByCriteria(Class<T> entityClass,
            Map<String, List<String>> filterByAssociations,
            Map<String, List<String>> orderByAssociations,
            EntityManager entityManager, DatatablesCriterias datatablesCriterias) {
        return findByCriteria(entityClass, filterByAssociations,
                orderByAssociations, entityManager, datatablesCriterias,
                (Map<String, Object>) null, false);
    }

    /**
     * Execute a select query on entityClass using {@code DatatablesCriterias}
     * information for filter, sort and paginate result.
     * 
     * @param entityClass entity to use in search
     * @param filterByAssociations (optional) for each related entity to join
     *            contain as key the name of the association and as value the
     *            List of related entity fields to filter by
     * @param orderByAssociations (optional) for each related entity to order
     *            contain as key the name of the association and as value the
     *            List of related entity fields to order by
     * @param entityManager {@code entityClass} {@link EntityManager}
     * @param datatablesCriterias datatables parameters for query
     * @param baseSearchValuesMap (optional) base filter values
     * @return
     */
    public static <T> SearchResults<T> findByCriteria(Class<T> entityClass,
            Map<String, List<String>> filterByAssociations,
            Map<String, List<String>> orderByAssociations,
            EntityManager entityManager,
            DatatablesCriterias datatablesCriterias,
            Map<String, Object> baseSearchValuesMap) {
        return findByCriteria(entityClass, filterByAssociations,
                orderByAssociations, entityManager, datatablesCriterias,
                baseSearchValuesMap, false);
    }

    /**
     * Execute a select query on entityClass using <a
     * href="http://www.querydsl.com/">Querydsl</a> which enables the
     * construction of type-safe SQL-like queries.
     * 
     * @param entityClass entity to use in search
     * @param filterByAssociations (optional) for each related entity to join
     *            contain as key the name of the association and as value the
     *            List of related entity fields to filter by
     * @param orderByAssociations (optional) for each related entity to order
     *            contain as key the name of the association and as value the
     *            List of related entity fields to order by
     * @param entityManager {@code entityClass} {@link EntityManager}
     * @param datatablesCriterias datatables parameters for query
     * @param baseSearchValuesMap (optional) base filter values
     * @param distinct use distinct query
     * @return
     */
    public static <T, E extends Comparable<?>> SearchResults<T> findByCriteria(
            Class<T> entityClass,
            Map<String, List<String>> filterByAssociations,
            Map<String, List<String>> orderByAssociations,
            EntityManager entityManager,
            DatatablesCriterias datatablesCriterias,
            Map<String, Object> baseSearchValuesMap, boolean distinct)
            throws IllegalArgumentException {

        Assert.notNull(entityClass);

        // Query DSL builder
        PathBuilder<T> entity = new PathBuilder<T>(entityClass, "entity");

        // Predicate for base query
        BooleanBuilder basePredicate;
        if (baseSearchValuesMap != null) {
            basePredicate = QuerydslUtils.createPredicateByAnd(entity,
                    baseSearchValuesMap);
        }
        else {
            basePredicate = new BooleanBuilder();
        }

        return findByCriteria(entityClass, filterByAssociations,
                orderByAssociations, entityManager, datatablesCriterias,
                basePredicate, distinct);
    }

    /**
     * Execute a select query on entityClass using <a
     * href="http://www.querydsl.com/">Querydsl</a> which enables the
     * construction of type-safe SQL-like queries.
     * 
     * @param entityClass entity to use in search
     * @param filterByAssociations (optional) for each related entity to join
     *            contain as key the name of the association and as value the
     *            List of related entity fields to filter by
     * @param orderByAssociations (optional) for each related entity to order
     *            contain as key the name of the association and as value the
     *            List of related entity fields to order by
     * @param entityManager {@code entityClass} {@link EntityManager}
     * @param datatablesCriterias datatables parameters for query
     * @param basePredicate (optional) base filter conditions
     * @param distinct use distinct query
     * @return
     */
    public static <T, E extends Comparable<?>> SearchResults<T> findByCriteria(
            Class<T> entityClass,
            Map<String, List<String>> filterByAssociations,
            Map<String, List<String>> orderByAssociations,
            EntityManager entityManager,
            DatatablesCriterias datatablesCriterias,
            BooleanBuilder basePredicate, boolean distinct)
            throws IllegalArgumentException {

        Assert.notNull(entityClass);

        // Query DSL builder
        PathBuilder<T> entity = new PathBuilder<T>(entityClass, "entity");

        return findByCriteria(entity, filterByAssociations,
                orderByAssociations, entityManager, datatablesCriterias,
                basePredicate, distinct);
    }

    /**
     * Execute a select query on entityClass using <a
     * href="http://www.querydsl.com/">Querydsl</a> which enables the
     * construction of type-safe SQL-like queries.
     * 
     * @param entity builder for entity to use in search. Represents the entity
     *            and gives access to its properties for query purposes
     * @param filterByAssociations (optional) for each related entity to join
     *            contain as key the name of the association and as value the
     *            List of related entity fields to filter by
     * @param orderByAssociations (optional) for each related entity to order
     *            contain as key the name of the association and as value the
     *            List of related entity fields to order by
     * @param entityManager {@code entityClass} {@link EntityManager}
     * @param datatablesCriterias datatables parameters for query
     * @param basePredicate (optional) base filter conditions
     * @param distinct use distinct query
     * @return
     */
    public static <T, E extends Comparable<?>> SearchResults<T> findByCriteria(
            PathBuilder<T> entity,
            Map<String, List<String>> filterByAssociations,
            Map<String, List<String>> orderByAssociations,
            EntityManager entityManager,
            DatatablesCriterias datatablesCriterias,
            BooleanBuilder basePredicate) throws IllegalArgumentException {
        return findByCriteria(entity, filterByAssociations,
                orderByAssociations, entityManager, datatablesCriterias,
                basePredicate, false);
    }

    /**
     * Execute a select query on entityClass using <a
     * href="http://www.querydsl.com/">Querydsl</a> which enables the
     * construction of type-safe SQL-like queries.
     * 
     * @param entity builder for entity to use in search. Represents the entity
     *            and gives access to its properties for query purposes
     * @param entityManager {@code entityClass} {@link EntityManager}
     * @param datatablesCriterias datatables parameters for query
     * @param basePredicate (optional) base filter conditions
     * @param distinct use distinct query
     * @return
     */
    public static <T, E extends Comparable<?>> SearchResults<T> findByCriteria(
            PathBuilder<T> entity, EntityManager entityManager,
            DatatablesCriterias datatablesCriterias,
            BooleanBuilder basePredicate) throws IllegalArgumentException {
        return findByCriteria(entity, null, null, entityManager,
                datatablesCriterias, basePredicate, false);
    }

    /**
     * Execute a select query on entityClass using <a
     * href="http://www.querydsl.com/">Querydsl</a> which enables the
     * construction of type-safe SQL-like queries.
     * 
     * @param entity builder for entity to use in search. Represents the entity
     *            and gives access to its properties for query purposes
     * @param filterByAssociations (optional) for each related entity to join
     *            contain as key the name of the association and as value the
     *            List of related entity fields to filter by
     * @param orderByAssociations (optional) for each related entity to order
     *            contain as key the name of the association and as value the
     *            List of related entity fields to order by
     * @param entityManager {@code entityClass} {@link EntityManager}
     * @param datatablesCriterias datatables parameters for query
     * @param basePredicate (optional) base filter conditions
     * @param distinct use distinct query
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <T, E extends Comparable<?>> SearchResults<T> findByCriteria(
            PathBuilder<T> entity,
            Map<String, List<String>> filterByAssociations,
            Map<String, List<String>> orderByAssociations,
            EntityManager entityManager,
            DatatablesCriterias datatablesCriterias,
            BooleanBuilder basePredicate, boolean distinct)
            throws IllegalArgumentException {

        // Check arguments aren't null
        Assert.notNull(entityManager);
        Assert.notNull(datatablesCriterias);

        // If null, create empty Map to avoid control code overload
        if (CollectionUtils.isEmpty(filterByAssociations)) {
            filterByAssociations = new HashMap<String, List<String>>();
        }
        if (CollectionUtils.isEmpty(orderByAssociations)) {
            orderByAssociations = new HashMap<String, List<String>>();
        }

        // true if data results must be paginated
        boolean isPaged = datatablesCriterias.getDisplaySize() != null
                && datatablesCriterias.getDisplaySize() > 0;

        // true if the search must take in account all columns
        boolean findInAllColumns = StringUtils.isNotEmpty(datatablesCriterias
                .getSearch()) && datatablesCriterias.hasOneFilterableColumn();

        // ----- Create queries -----

        // query will take in account datatables search, order and paging
        // criterias
        JPAQuery query = new JPAQuery(entityManager);
        query = query.from(entity);

        // baseQuery will use base search values only in order to count
        // all for success paging
        JPAQuery baseQuery = new JPAQuery(entityManager);
        baseQuery = baseQuery.from(entity);

        // ----- Entity associations for Query JOINs, ORDER BY, ... -----

        Map<String, PathBuilder<?>> associationMap = new HashMap<String, PathBuilder<?>>();

        for (ColumnDef column : datatablesCriterias.getColumnDefs()) {

            // true if the search must include this column
            boolean findInColumn = StringUtils.isNotEmpty(column.getSearch());

            // If no joins given for this column, don't add the JOIN to query
            // to improve performance
            String associationName = column.getName();
            if (!filterByAssociations.containsKey(associationName)) {
                continue;
            }

            // If column is not sortable and is not filterable, don't add the
            // JOIN to query to improve performance
            if (!column.isSortable() && !column.isFilterable()) {
                continue;
            }

            // If column is not sortable and no search value provided,
            // don't add the JOIN to query to improve performance
            if (!column.isSortable() && !findInColumn && !findInAllColumns) {
                continue;
            }

            // Here the column is sortable or it is filterable and column search
            // value or all-column search value is provided
            PathBuilder<?> associationPath = entity.get(associationName);
            query = query.join(associationPath);

            // Store join path for later use in where
            associationMap.put(associationName, associationPath);
        }

        // ----- Query WHERE clauses -----

        // Filters by column. Using BooleanBuilder, a cascading builder for
        // Predicate expressions
        BooleanBuilder filtersByColumnPredicate = new BooleanBuilder();

        // Filters by table (for all columns)
        BooleanBuilder filtersByTablePredicate = new BooleanBuilder();

        try {

            // Build the filters by column expression
            if (datatablesCriterias.hasOneFilteredColumn()) {

                // Add filterable columns only
                for (ColumnDef column : datatablesCriterias.getColumnDefs()) {

                    // Each column has its own search by value
                    String searchStr = column.getSearch();

                    // true if the search must include this column
                    boolean findInColumn = column.isFilterable()
                            && StringUtils.isNotEmpty(searchStr);

                    if (findInColumn) {

                        // Entity field name and type
                        String fieldName = column.getName();
                        Class<?> fieldType = BeanUtils
                                .findPropertyType(fieldName, ArrayUtils
                                        .<Class<?>> toArray(entity.getType()));

                        // On column search, connect where clauses together by
                        // AND
                        // because we want found the records which columns
                        // match with column filters
                        filtersByColumnPredicate = filtersByColumnPredicate
                                .and(QuerydslUtils.createExpression(entity,
                                        fieldName, fieldType, searchStr));

                        // TODO: Este codigo se puede pasar a QuerydslUtils ?

                        // If column is an association and there are given
                        // join attributes, add those attributes to WHERE
                        // predicates
                        List<String> attributes = filterByAssociations
                                .get(fieldName);
                        if (attributes != null && attributes.size() > 0) {

                            // Filters of associated entity properties
                            BooleanBuilder filtersByAssociationPredicate = new BooleanBuilder();

                            PathBuilder<?> associationPath = associationMap
                                    .get(fieldName);
                            List<String> associationFields = filterByAssociations
                                    .get(fieldName);

                            for (String associationFieldName : associationFields) {

                                // Get associated entity field type
                                Class<?> associationFieldType = BeanUtils
                                        .findPropertyType(
                                                associationFieldName,
                                                ArrayUtils
                                                        .<Class<?>> toArray(fieldType));

                                // On association search, connect
                                // associated entity where clauses by OR
                                // because all assoc entity properties are
                                // inside the same column and any of its
                                // property value can match with given search
                                // value
                                filtersByAssociationPredicate = filtersByAssociationPredicate
                                        .or(QuerydslUtils
                                                .createExpression(
                                                        associationPath,
                                                        associationFieldName,
                                                        associationFieldType,
                                                        searchStr));
                            }

                            filtersByColumnPredicate = filtersByColumnPredicate
                                    .and(filtersByAssociationPredicate
                                            .getValue());
                        }
                    }
                }
            }

            // Build the query to search the given value in all columns
            String searchStr = datatablesCriterias.getSearch();
            if (findInAllColumns) {

                // Add filterable columns only
                for (ColumnDef column : datatablesCriterias.getColumnDefs()) {
                    if (column.isFilterable()) {

                        // Entity field name and type
                        String fieldName = column.getName();
                        Class<?> fieldType = BeanUtils
                                .findPropertyType(fieldName, ArrayUtils
                                        .<Class<?>> toArray(entity.getType()));

                        // Find in all columns means we want to find given
                        // value in at least one entity property, so we must
                        // join the where clauses by OR
                        filtersByTablePredicate = filtersByTablePredicate
                                .or(QuerydslUtils.createExpression(entity,
                                        fieldName, fieldType, searchStr));

                        // If column is an association and there are given
                        // join attributes, add those attributes to WHERE
                        // predicates
                        List<String> attributes = filterByAssociations
                                .get(fieldName);
                        if (attributes != null && attributes.size() > 0) {
                            PathBuilder<?> associationPath = associationMap
                                    .get(fieldName);
                            List<String> associationFields = filterByAssociations
                                    .get(fieldName);

                            for (String associationFieldName : associationFields) {

                                // Get associated entity field type
                                Class<?> associationFieldType = BeanUtils
                                        .findPropertyType(
                                                associationFieldName,
                                                ArrayUtils
                                                        .<Class<?>> toArray(fieldType));
                                filtersByTablePredicate = filtersByTablePredicate
                                        .or(QuerydslUtils
                                                .createExpression(
                                                        associationPath,
                                                        associationFieldName,
                                                        associationFieldType,
                                                        searchStr));
                            }
                        }
                    }
                }
            }
        }
        catch (Exception e) {
            SearchResults<T> searchResults = new SearchResults<T>(
                    new ArrayList<T>(0), 0, isPaged, new Long(
                            datatablesCriterias.getDisplayStart()), new Long(
                            datatablesCriterias.getDisplaySize()), 0);
            return searchResults;
        }

        // ----- Query ORDER BY -----

        List<OrderSpecifier<?>> orderSpecifiersList = new ArrayList<OrderSpecifier<?>>();

        if (datatablesCriterias.hasOneSortedColumn()) {
            for (ColumnDef column : datatablesCriterias.getSortingColumnDefs()) {

                // If column is not sortable, don't add it to order by clauses
                if (!column.isSortable()) {
                    continue;
                }

                // If no sort direction provided, don't add this column to
                // order by clauses
                if (column.getSortDirection() == null) {
                    continue;
                }

                // Convert Datatables sort direction to Querydsl order
                Order order = Order.DESC;
                if (column.getSortDirection() == SortDirection.ASC) {
                    order = Order.ASC;
                }

                // Entity field name and type. Type must extend Comparable
                // interface
                String fieldName = column.getName();
                Class<E> fieldType = (Class<E>) BeanUtils.findPropertyType(
                        fieldName,
                        ArrayUtils.<Class<?>> toArray(entity.getType()));

                List<String> attributes = orderByAssociations.get(fieldName);
                try {
                    // If column is an association and there are given
                    // order by attributes, add those attributes to ORDER BY
                    // clauses
                    if (attributes != null && attributes.size() > 0) {
                        PathBuilder<?> associationPath = associationMap
                                .get(fieldName);
                        List<String> associationFields = orderByAssociations
                                .get(fieldName);

                        for (String associationFieldName : associationFields) {

                            // Get associated entity field type
                            Class<E> associationFieldType = (Class<E>) BeanUtils
                                    .findPropertyType(
                                            associationFieldName,
                                            ArrayUtils
                                                    .<Class<?>> toArray(fieldType));
                            orderSpecifiersList.add(QuerydslUtils
                                    .createOrderSpecifier(associationPath,
                                            associationFieldName,
                                            associationFieldType, order));
                        }
                    }
                    // Otherwise column is an entity property
                    else {
                        orderSpecifiersList.add(QuerydslUtils
                                .createOrderSpecifier(entity, fieldName,
                                        fieldType, order));
                    }
                }
                catch (Exception ex) {
                    // Do nothing, on class cast exception order specifier will
                    // be null
                }
            }
        }

        // ----- Query results paging -----

        Long offset = null;
        Long limit = null;

        if (isPaged) {
            limit = new Long(datatablesCriterias.getDisplaySize());
        }
        if (datatablesCriterias.getDisplayStart() >= 0) {
            offset = new Long(datatablesCriterias.getDisplayStart());
        }

        // QueryModifiers combines limit and offset
        QueryModifiers queryModifiers = new QueryModifiers(limit, offset);

        // ----- Execute the query -----
        List<T> elements = null;

        // Compose the final query and update query var to be used to count
        // total amount of rows if needed

        if (distinct) {
            query = query.distinct();
        }

        // Predicate for base query
        if (basePredicate == null) {
            basePredicate = new BooleanBuilder();
        }

        // query projection to count all entities without paging
        baseQuery.where(basePredicate);

        // query projection to be used to get the results and to count filtered
        // results
        query = query.where(basePredicate.and(
                filtersByColumnPredicate.getValue()).and(
                filtersByTablePredicate.getValue()));

        // List ordered and paginated results. An empty list is returned for no
        // results.
        elements = query
                .orderBy(
                        orderSpecifiersList
                                .toArray(new OrderSpecifier[orderSpecifiersList
                                        .size()])).restrict(queryModifiers)
                .list(entity);

        // Calculate the total amount of rows taking in account datatables
        // search and paging criterias. When results are paginated we
        // must execute a count query, otherwise the size of matched rows List
        // is the total amount of rows
        long totalResultCount;
        if (isPaged) {
            totalResultCount = query.count();
        }
        else {
            totalResultCount = elements.size();
        }

        // Calculate the total amount of entities including base filters only
        long totalBaseCount = baseQuery.count();

        // Create a new SearchResults instance
        SearchResults<T> searchResults = new SearchResults<T>(elements,
                totalResultCount, isPaged, offset, limit, totalBaseCount);

        return searchResults;
    }

    /**
     * Populate a {@link DataSet} from given entity list.
     * <p/>
     * Field values will be converted to String using given
     * {@link ConversionService} and Date fields will be converted to Date using
     * {@link DateFormat} with given date patterns.
     * 
     * @param entities List of T entities to convert to Datatables data
     * @param pkFieldName The T entity field that contains the PK
     * @param totalRecords Total amount of records
     * @param totalDisplayRecords Amount of records found
     * @param columns {@link ColumnDef} list
     * @param datePatterns Patterns to convert Date fields to String. The Map
     *            contains one pattern for each entity Date field keyed by field
     *            name. For Roo compatibility the key could follow the pattern
     *            {@code lower_case( ENTITY ) + "_" + lower_case( FIELD ) + "_date_format"}
     *            too
     * @param conversionService
     * @return
     */
    public static <T> DataSet<Map<String, String>> populateDataSet(
            List<T> entities, String pkFieldName, long totalRecords,
            long totalDisplayRecords, List<ColumnDef> columns,
            Map<String, Object> datePatterns,
            ConversionService conversionService) {

        // Check arguments aren't null
        Assert.notNull(pkFieldName);
        Assert.notNull(columns);
        Assert.notNull(conversionService);

        // Map of data rows
        List<Map<String, String>> rows = new ArrayList<Map<String, String>>(
                entities.size());

        if (CollectionUtils.isEmpty(entities)) {
            return new DataSet<Map<String, String>>(rows, 0l, 0l);
        }

        // If null, create empty Map to avoid control code overload
        if (CollectionUtils.isEmpty(datePatterns)) {
            datePatterns = new HashMap<String, Object>();
        }

        // Prepare required fields
        Set<String> fields = new HashSet<String>();
        fields.add(pkFieldName);

        // Add fields from request
        for (ColumnDef colum : columns) {
            fields.add(colum.getName());
        }

        // Date formatters
        DateFormat defaultFormat = SimpleDateFormat.getDateInstance();

        // Populate each row, note a row is a Map containing
        // fieldName = fieldValue
        for (Object entity : entities) {
            Map<String, String> row = new HashMap<String, String>(fields.size());
            BeanWrapper entityBean = new BeanWrapperImpl(entity);
            for (String fieldName : fields) {

                // check if property exists (trace it else)
                if (!entityBean.isReadableProperty(fieldName)) {
                    logger.debug("Property [".concat(fieldName)
                            .concat("] not found in bean [")
                            .concat(entity.toString()).concat("]"));
                    continue;
                }
                Object value = null;
                String valueStr = null;

                // Convert field value to string
                try {
                    value = entityBean.getPropertyValue(fieldName);
                    if (Calendar.class.isAssignableFrom(value.getClass())) {
                        value = ((Calendar) value).getTime();
                    }
                    if (Date.class.isAssignableFrom(value.getClass())) {
                        String pattern = getPattern(datePatterns,
                                entityBean.getWrappedClass(), fieldName);
                        DateFormat format = StringUtils.isEmpty(pattern) ? defaultFormat
                                : new SimpleDateFormat(pattern);
                        valueStr = format.format(value);
                    }
                    else if (conversionService.canConvert(value.getClass(),
                            String.class)) {
                        valueStr = conversionService.convert(value,
                                String.class);
                    }
                    else {
                        valueStr = ObjectUtils.getDisplayString(value);
                    }
                }
                catch (Exception ex) {

                    // debug getting value problem
                    logger.error(
                            "Error getting value of field [".concat(fieldName)
                                    .concat("]").concat(" in bean [")
                                    .concat(entity.toString()).concat("]"), ex);
                }
                row.put(fieldName, valueStr);

                // Set PK value as DT_RowId
                // Note when entity has composite PK Roo generates the need
                // convert method and adds it to ConversionService, so
                // when processed field is the PK the valueStr is the
                // composite PK instance marshalled to JSON notation and
                // Base64 encoded
                if (pkFieldName.equalsIgnoreCase(fieldName)) {
                    row.put("DT_RowId", valueStr);
                }
            }
            rows.add(row);
        }
        DataSet<Map<String, String>> dataSet = new DataSet<Map<String, String>>(
                rows, totalRecords, totalDisplayRecords);
        return dataSet;
    }

    /**
     * Get Date pattern by field name
     * <p/>
     * If no pattern found, try standard Roo key
     * {@code lower_case( ENTITY ) + "_" + lower_case( FIELD ) + "_date_format"}
     * 
     * @param datePatterns Contains field name and related data pattern
     * @param entityClass Entity class to which the field belong to
     * @param fieldName Field to search pattern
     * @return
     */
    private static String getPattern(Map<String, Object> datePatterns,
            Class<?> entityClass, String fieldName) {

        // Get pattern by field name
        String pattern = (String) datePatterns.get(fieldName.toLowerCase());
        if (!StringUtils.isEmpty(pattern)) {
            return pattern;
        }

        // Otherwise get pattern by Roo key
        String rooKey = entityClass.getName().toLowerCase().concat("_")
                .concat(fieldName.toLowerCase()).concat("_date_format");

        pattern = (String) datePatterns.get(rooKey);
        return pattern;
    }
}
