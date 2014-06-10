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

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.persistence.EntityManager;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.SingularAttribute;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.gvnix.web.datatables.query.SearchResults;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import com.github.dandelion.datatables.core.ajax.ColumnDef;
import com.github.dandelion.datatables.core.ajax.ColumnDef.SortDirection;
import com.github.dandelion.datatables.core.ajax.DataSet;
import com.github.dandelion.datatables.core.ajax.DatatablesCriterias;
import com.github.dandelion.datatables.core.export.ExportConf;
import com.github.dandelion.datatables.core.export.HtmlTableBuilder;
import com.github.dandelion.datatables.core.export.HtmlTableBuilder.BeforeEndStep;
import com.github.dandelion.datatables.core.export.HtmlTableBuilder.ColumnStep;
import com.github.dandelion.datatables.core.html.HtmlTable;
import com.mysema.query.BooleanBuilder;
import com.mysema.query.QueryModifiers;
import com.mysema.query.jpa.impl.JPAQuery;
import com.mysema.query.types.Order;
import com.mysema.query.types.OrderSpecifier;
import com.mysema.query.types.Predicate;
import com.mysema.query.types.expr.BooleanExpression;
import com.mysema.query.types.path.PathBuilder;

/**
 * Datatables utility functions
 * 
 * @author gvNIX team
 */
public class DatatablesUtils {

    // Logger
    private static Logger LOGGER = LoggerFactory
            .getLogger(DatatablesUtils.class);

    public static final String ROWS_ON_TOP_IDS_PARAM = "dtt_row_on_top_ids";
    private static final String SEPARATOR_FIELDS = ".";
    private static final String SEPARATOR_FIELDS_ESCAPED = "_~~_";

    /**
     * Execute a select query on entityClass using {@code DatatablesCriterias}
     * information for filter, sort and paginate result.
     * 
     * @param entityClass entity to use in search
     * @param entityManager {@code entityClass} {@link EntityManager}
     * @param datatablesCriterias datatables parameters for query
     * @return
     * @deprecated see
     *             {@link #findByCriteria(Class, Map, Map, EntityManager, DatatablesCriterias, BooleanBuilder, boolean, ConversionService, MessageSource, Object[])}
     */
    public static <T> SearchResults<T> findByCriteria(Class<T> entityClass,
            EntityManager entityManager,
            DatatablesCriterias datatablesCriterias,
            ConversionService conversionService, MessageSource messageSource) {
        return findByCriteria(entityClass, null, null, entityManager,
                datatablesCriterias, (BooleanBuilder) null, false,
                conversionService, messageSource, null);
    }

    /**
     * Execute a select query on entityClass using {@code DatatablesCriterias}
     * information for filter, sort and paginate result.
     * 
     * @param entityClass entity to use in search
     * @param entityManager {@code entityClass} {@link EntityManager}
     * @param datatablesCriterias datatables parameters for query
     * @return
     * @deprecated see
     *             {@link #findByCriteria(Class, Map, Map, EntityManager, DatatablesCriterias, BooleanBuilder, boolean, ConversionService, MessageSource, Object[])}
     */
    public static <T> SearchResults<T> findByCriteria(Class<T> entityClass,
            EntityManager entityManager, DatatablesCriterias datatablesCriterias) {
        return findByCriteria(entityClass, null, null, entityManager,
                datatablesCriterias, (BooleanBuilder) null, false, null, null,
                null);
    }

    /**
     * Execute a select query on entityClass using {@code DatatablesCriterias}
     * information for filter, sort and paginate result.
     * 
     * @param entityClass entity to use in search
     * @param entityManager {@code entityClass} {@link EntityManager}
     * @param datatablesCriterias datatables parameters for query
     * @param baseSearchValuesMap (optional) base filter values
     * @param conversionService required by filter-by-expression and rows-on-top
     *        (otherwise optional)
     * @param messageSource required by filter-by-expression (otherwise
     *        optional)
     * @return
     */
    public static <T> SearchResults<T> findByCriteria(Class<T> entityClass,
            EntityManager entityManager,
            DatatablesCriterias datatablesCriterias,
            Map<String, Object> baseSearchValuesMap,
            ConversionService conversionService, MessageSource messageSource) {
        return findByCriteria(entityClass, null, null, entityManager,
                datatablesCriterias, baseSearchValuesMap, false,
                conversionService, messageSource);
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
     * @deprecated see
     *             {@link #findByCriteria(Class, Map, Map, EntityManager, DatatablesCriterias, BooleanBuilder, boolean, ConversionService, MessageSource, Object[])}
     */
    public static <T> SearchResults<T> findByCriteria(Class<T> entityClass,
            EntityManager entityManager,
            DatatablesCriterias datatablesCriterias,
            Map<String, Object> baseSearchValuesMap) {
        return findByCriteria(entityClass, null, null, entityManager,
                datatablesCriterias, baseSearchValuesMap, false, null, null);
    }

    /**
     * Execute a select query on entityClass using {@code DatatablesCriterias}
     * information for filter, sort and paginate result.
     * 
     * @param entityClass entity to use in search
     * @param filterByAssociations (optional) for each related entity to join
     *        contain as key the name of the association and as value the List
     *        of related entity fields to filter by
     * @param orderByAssociations (optional) for each related entity to order
     *        contain as key the name of the association and as value the List
     *        of related entity fields to order by
     * @param entityManager {@code entityClass} {@link EntityManager}
     * @param datatablesCriterias datatables parameters for query
     * @param conversionService required by filter-by-expression and rows-on-top
     *        (otherwise optional)
     * @param messageSource required by filter-by-expression (otherwise
     *        optional)
     * @return
     * @deprecated see
     *             {@link #findByCriteria(Class, Map, Map, EntityManager, DatatablesCriterias, BooleanBuilder, boolean, ConversionService, MessageSource, Object[])}
     */
    public static <T> SearchResults<T> findByCriteria(Class<T> entityClass,
            Map<String, List<String>> filterByAssociations,
            Map<String, List<String>> orderByAssociations,
            EntityManager entityManager,
            DatatablesCriterias datatablesCriterias,
            ConversionService conversionService, MessageSource messageSource) {
        return findByCriteria(entityClass, filterByAssociations,
                orderByAssociations, entityManager, datatablesCriterias, null,
                false, conversionService, messageSource, null);
    }

    /**
     * Execute a select query on entityClass using {@code DatatablesCriterias}
     * information for filter, sort and paginate result.
     * 
     * @param entityClass entity to use in search
     * @param filterByAssociations (optional) for each related entity to join
     *        contain as key the name of the association and as value the List
     *        of related entity fields to filter by
     * @param orderByAssociations (optional) for each related entity to order
     *        contain as key the name of the association and as value the List
     *        of related entity fields to order by
     * @param entityManager {@code entityClass} {@link EntityManager}
     * @param datatablesCriterias datatables parameters for query
     * @return
     * @deprecated see
     *             {@link #findByCriteria(Class, Map, Map, EntityManager, DatatablesCriterias, BooleanBuilder, boolean, ConversionService, MessageSource, Object[])}
     */
    public static <T> SearchResults<T> findByCriteria(Class<T> entityClass,
            Map<String, List<String>> filterByAssociations,
            Map<String, List<String>> orderByAssociations,
            EntityManager entityManager, DatatablesCriterias datatablesCriterias) {
        return findByCriteria(entityClass, filterByAssociations,
                orderByAssociations, entityManager, datatablesCriterias, null,
                false, null, null, null);
    }

    /**
     * Execute a select query on entityClass using {@code DatatablesCriterias}
     * information for filter, sort and paginate result.
     * <p/>
     * This method can receive rows-on-top as parameter on
     * <code>baseSearchValueMap</code> using {@link #ROWS_ON_TOP_IDS_PARAM}
     * name.
     * 
     * @param entityClass entity to use in search
     * @param filterByAssociations (optional) for each related entity to join
     *        contain as key the name of the association and as value the List
     *        of related entity fields to filter by
     * @param orderByAssociations (optional) for each related entity to order
     *        contain as key the name of the association and as value the List
     *        of related entity fields to order by
     * @param entityManager {@code entityClass} {@link EntityManager}
     * @param datatablesCriterias datatables parameters for query
     * @param baseSearchValuesMap (optional) base filter values
     * @param conversionService required by filter-by-expression and rows-on-top
     *        (otherwise optional)
     * @param messageSource required by filter-by-expression (otherwise
     *        optional)
     * @return
     */
    public static <T> SearchResults<T> findByCriteria(Class<T> entityClass,
            Map<String, List<String>> filterByAssociations,
            Map<String, List<String>> orderByAssociations,
            EntityManager entityManager,
            DatatablesCriterias datatablesCriterias,
            Map<String, Object> baseSearchValuesMap,
            ConversionService conversionService, MessageSource messageSource) {
        return findByCriteria(entityClass, filterByAssociations,
                orderByAssociations, entityManager, datatablesCriterias,
                baseSearchValuesMap, false, conversionService, messageSource);
    }

    /**
     * Execute a select query on entityClass using {@code DatatablesCriterias}
     * information for filter, sort and paginate result.
     * 
     * @param entityClass entity to use in search
     * @param filterByAssociations (optional) for each related entity to join
     *        contain as key the name of the association and as value the List
     *        of related entity fields to filter by
     * @param orderByAssociations (optional) for each related entity to order
     *        contain as key the name of the association and as value the List
     *        of related entity fields to order by
     * @param entityManager {@code entityClass} {@link EntityManager}
     * @param datatablesCriterias datatables parameters for query
     * @param baseSearchValuesMap (optional) base filter values
     * @return
     * @deprecated see
     *             {@link #findByCriteria(Class, EntityManager, DatatablesCriterias, Map, ConversionService, MessageSource)}
     */
    public static <T> SearchResults<T> findByCriteria(Class<T> entityClass,
            Map<String, List<String>> filterByAssociations,
            Map<String, List<String>> orderByAssociations,
            EntityManager entityManager,
            DatatablesCriterias datatablesCriterias,
            Map<String, Object> baseSearchValuesMap) {
        return findByCriteria(entityClass, filterByAssociations,
                orderByAssociations, entityManager, datatablesCriterias,
                baseSearchValuesMap, false, null, null);
    }

    /**
     * Execute a select query on entityClass using <a
     * href="http://www.querydsl.com/">Querydsl</a> which enables the
     * construction of type-safe SQL-like queries.
     * 
     * @param entityClass entity to use in search
     * @param filterByAssociations (optional) for each related entity to join
     *        contain as key the name of the association and as value the List
     *        of related entity fields to filter by
     * @param orderByAssociations (optional) for each related entity to order
     *        contain as key the name of the association and as value the List
     *        of related entity fields to order by
     * @param entityManager {@code entityClass} {@link EntityManager}
     * @param datatablesCriterias datatables parameters for query
     * @param baseSearchValuesMap (optional) base filter values
     * @param distinct use distinct query
     * @return
     * @deprecated see
     *             {@link #findByCriteria(Class, EntityManager, DatatablesCriterias, Map,boolean, ConversionService, MessageSource)}
     */
    public static <T, E extends Comparable<?>> SearchResults<T> findByCriteria(
            Class<T> entityClass,
            Map<String, List<String>> filterByAssociations,
            Map<String, List<String>> orderByAssociations,
            EntityManager entityManager,
            DatatablesCriterias datatablesCriterias,
            Map<String, Object> baseSearchValuesMap, boolean distinct)
            throws IllegalArgumentException {

        return findByCriteria(entityClass, filterByAssociations,
                orderByAssociations, entityManager, datatablesCriterias,
                baseSearchValuesMap, distinct, null, null);
    }

    /**
     * Execute a select query on entityClass using <a
     * href="http://www.querydsl.com/">Querydsl</a> which enables the
     * construction of type-safe SQL-like queries.
     * <p/>
     * This method can receive rows-on-top as parameter on
     * <code>baseSearchValueMap</code> using {@link #ROWS_ON_TOP_IDS_PARAM}
     * name.
     * 
     * @param entityClass entity to use in search
     * @param filterByAssociations (optional) for each related entity to join
     *        contain as key the name of the association and as value the List
     *        of related entity fields to filter by
     * @param orderByAssociations (optional) for each related entity to order
     *        contain as key the name of the association and as value the List
     *        of related entity fields to order by
     * @param entityManager {@code entityClass} {@link EntityManager}
     * @param datatablesCriterias datatables parameters for query
     * @param baseSearchValuesMap (optional) base filter values
     * @param distinct use distinct query
     * @param conversionService required by filter-by-expression and rows-on-top
     *        (otherwise optional)
     * @param messageSource required by filter-by-expression (otherwise
     *        optional)
     * @return
     */
    public static <T, E extends Comparable<?>> SearchResults<T> findByCriteria(
            Class<T> entityClass,
            Map<String, List<String>> filterByAssociations,
            Map<String, List<String>> orderByAssociations,
            EntityManager entityManager,
            DatatablesCriterias datatablesCriterias,
            Map<String, Object> baseSearchValuesMap, boolean distinct,
            ConversionService conversionService, MessageSource messageSource)
            throws IllegalArgumentException {

        Assert.notNull(entityClass);

        // Query DSL builder
        PathBuilder<T> entity = new PathBuilder<T>(entityClass, "entity");

        Object[] rowsOnTopIds = null;
        // Predicate for base query
        BooleanBuilder basePredicate;
        if (baseSearchValuesMap != null) {
            // Handle ROWS_ON_TOP_IDS_PARAM param
            Object tmpObject = baseSearchValuesMap.get(ROWS_ON_TOP_IDS_PARAM);

            if (tmpObject != null) {
                // Check if value is an array, otherwise
                if (tmpObject.getClass().isArray()) {
                    rowsOnTopIds = (Object[]) tmpObject;
                }
                else {
                    rowsOnTopIds = new Object[] { tmpObject };
                }
                Map<String, Object> newBaseSearch = new HashMap<String, Object>(
                        baseSearchValuesMap);
                newBaseSearch.remove(ROWS_ON_TOP_IDS_PARAM);
                basePredicate = QuerydslUtils.createPredicateByAnd(entity,
                        newBaseSearch);
            }
            else {
                basePredicate = QuerydslUtils.createPredicateByAnd(entity,
                        baseSearchValuesMap);
            }
        }
        else {
            basePredicate = new BooleanBuilder();
        }

        return findByCriteria(entityClass, filterByAssociations,
                orderByAssociations, entityManager, datatablesCriterias,
                basePredicate, distinct, conversionService, messageSource,
                rowsOnTopIds);
    }

    /**
     * Execute a select query on entityClass using <a
     * href="http://www.querydsl.com/">Querydsl</a> which enables the
     * construction of type-safe SQL-like queries.
     * 
     * @param entityClass entity to use in search
     * @param filterByAssociations (optional) for each related entity to join
     *        contain as key the name of the association and as value the List
     *        of related entity fields to filter by
     * @param orderByAssociations (optional) for each related entity to order
     *        contain as key the name of the association and as value the List
     *        of related entity fields to order by
     * @param entityManager {@code entityClass} {@link EntityManager}
     * @param datatablesCriterias datatables parameters for query
     * @param basePredicate (optional) base filter conditions
     * @param distinct use distinct query
     * @return
     * @deprecated {@link #findByCriteria(Class, Map, Map, EntityManager, DatatablesCriterias, BooleanBuilder, boolean, ConversionService, MessageSource, Object[])}
     */
    public static <T, E extends Comparable<?>> SearchResults<T> findByCriteria(
            Class<T> entityClass,
            Map<String, List<String>> filterByAssociations,
            Map<String, List<String>> orderByAssociations,
            EntityManager entityManager,
            DatatablesCriterias datatablesCriterias,
            BooleanBuilder basePredicate, boolean distinct)
            throws IllegalArgumentException {

        return findByCriteria(entityClass, filterByAssociations,
                orderByAssociations, entityManager, datatablesCriterias,
                basePredicate, distinct, null, null, null);
    }

    /**
     * Execute a select query on entityClass using <a
     * href="http://www.querydsl.com/">Querydsl</a> which enables the
     * construction of type-safe SQL-like queries.
     * 
     * @param entityClass entity to use in search
     * @param filterByAssociations (optional) for each related entity to join
     *        contain as key the name of the association and as value the List
     *        of related entity fields to filter by
     * @param orderByAssociations (optional) for each related entity to order
     *        contain as key the name of the association and as value the List
     *        of related entity fields to order by
     * @param entityManager {@code entityClass} {@link EntityManager}
     * @param datatablesCriterias datatables parameters for query
     * @param basePredicate (optional) base filter conditions
     * @param distinct use distinct query
     * @return
     * @deprecated see
     *             {@link #findByCriteria(Class, Map, Map, EntityManager, DatatablesCriterias, BooleanBuilder, boolean, ConversionService, MessageSource, Object[])}
     */
    public static <T, E extends Comparable<?>> SearchResults<T> findByCriteria(
            Class<T> entityClass,
            Map<String, List<String>> filterByAssociations,
            Map<String, List<String>> orderByAssociations,
            EntityManager entityManager,
            DatatablesCriterias datatablesCriterias,
            BooleanBuilder basePredicate, boolean distinct,
            ConversionService conversionService, MessageSource messageSource)
            throws IllegalArgumentException {

        Assert.notNull(entityClass);

        // Query DSL builder
        PathBuilder<T> entity = new PathBuilder<T>(entityClass, "entity");

        return findByCriteria(entity, filterByAssociations,
                orderByAssociations, entityManager, datatablesCriterias,
                basePredicate, distinct, conversionService, messageSource, null);
    }

    /**
     * Execute a select query on entityClass using {@code DatatablesCriterias}
     * information for filter, sort and paginate result.
     * <p/>
     * This method can receive rows-on-top as parameter on
     * <code>baseSearchValueMap</code> using {@link #ROWS_ON_TOP_IDS_PARAM}
     * name.
     * 
     * @param entityClass entity to use in search
     * @param filterByAssociations (optional) for each related entity to join
     *        contain as key the name of the association and as value the List
     *        of related entity fields to filter by
     * @param orderByAssociations (optional) for each related entity to order
     *        contain as key the name of the association and as value the List
     *        of related entity fields to order by
     * @param entityManager {@code entityClass} {@link EntityManager}
     * @param datatablesCriterias datatables parameters for query
     * @param basePredicate (optional) base filter
     * @param conversionService required by filter-by-expression and rows-on-top
     *        (otherwise optional)
     * @param messageSource required by filter-by-expression (otherwise
     *        optional)
     * @param rowsOnTopIds (optional) array with id of rows to show on top of
     *        result list
     * @return
     * @throws IllegalArgumentException
     */
    public static <T, E extends Comparable<?>> SearchResults<T> findByCriteria(
            Class<T> entityClass,
            Map<String, List<String>> filterByAssociations,
            Map<String, List<String>> orderByAssociations,
            EntityManager entityManager,
            DatatablesCriterias datatablesCriterias,
            BooleanBuilder basePredicate, boolean distinct,
            ConversionService conversionService, MessageSource messageSource,
            Object[] rowsOnTopIds) throws IllegalArgumentException {

        Assert.notNull(entityClass);

        // Query DSL builder
        PathBuilder<T> entity = new PathBuilder<T>(entityClass, "entity");

        return findByCriteria(entity, filterByAssociations,
                orderByAssociations, entityManager, datatablesCriterias,
                basePredicate, distinct, conversionService, messageSource,
                rowsOnTopIds);
    }

    /**
     * Execute a select query on entityClass using <a
     * href="http://www.querydsl.com/">Querydsl</a> which enables the
     * construction of type-safe SQL-like queries.
     * 
     * @param entity builder for entity to use in search. Represents the entity
     *        and gives access to its properties for query purposes
     * @param filterByAssociations (optional) for each related entity to join
     *        contain as key the name of the association and as value the List
     *        of related entity fields to filter by
     * @param orderByAssociations (optional) for each related entity to order
     *        contain as key the name of the association and as value the List
     *        of related entity fields to order by
     * @param entityManager {@code entityClass} {@link EntityManager}
     * @param datatablesCriterias datatables parameters for query
     * @param basePredicate (optional) base filter conditions
     * @param conversionService required by filter-by-expression and rows-on-top
     *        (otherwise optional)
     * @param distinct use distinct query
     * @return
     * @deprecated see
     *             {@link #findByCriteria(Class, EntityManager, DatatablesCriterias, Map, ConversionService, MessageSource)}
     */
    public static <T, E extends Comparable<?>> SearchResults<T> findByCriteria(
            PathBuilder<T> entity,
            Map<String, List<String>> filterByAssociations,
            Map<String, List<String>> orderByAssociations,
            EntityManager entityManager,
            DatatablesCriterias datatablesCriterias,
            BooleanBuilder basePredicate, ConversionService conversionService,
            MessageSource messageSource) throws IllegalArgumentException {
        return findByCriteria(entity, filterByAssociations,
                orderByAssociations, entityManager, datatablesCriterias,
                basePredicate, false, conversionService, messageSource, null);
    }

    /**
     * Execute a select query on entityClass using <a
     * href="http://www.querydsl.com/">Querydsl</a> which enables the
     * construction of type-safe SQL-like queries.
     * 
     * @param entity builder for entity to use in search. Represents the entity
     *        and gives access to its properties for query purposes
     * @param filterByAssociations (optional) for each related entity to join
     *        contain as key the name of the association and as value the List
     *        of related entity fields to filter by
     * @param orderByAssociations (optional) for each related entity to order
     *        contain as key the name of the association and as value the List
     *        of related entity fields to order by
     * @param entityManager {@code entityClass} {@link EntityManager}
     * @param datatablesCriterias datatables parameters for query
     * @param basePredicate (optional) base filter conditions
     * @return
     * @deprecated see
     *             {@link #findByCriteria(PathBuilder, Map, Map, EntityManager, DatatablesCriterias, BooleanBuilder, boolean, ConversionService, MessageSource, Object[])}
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
                basePredicate, false, null, null, null);
    }

    /**
     * Execute a select query on entityClass using <a
     * href="http://www.querydsl.com/">Querydsl</a> which enables the
     * construction of type-safe SQL-like queries.
     * 
     * @param entity builder for entity to use in search. Represents the entity
     *        and gives access to its properties for query purposes
     * @param entityManager {@code entityClass} {@link EntityManager}
     * @param datatablesCriterias datatables parameters for query
     * @param basePredicate (optional) base filter conditions
     * @param conversionService required by filter-by-expression and rows-on-top
     *        (otherwise optional)
     * @param messageSource required by filter-by-expression (otherwise
     *        optional)
     * @return
     * @deprecated see
     *             {@link #findByCriteria(PathBuilder, Map, Map, EntityManager, DatatablesCriterias, BooleanBuilder, boolean, ConversionService, MessageSource, Object[])}
     */
    public static <T, E extends Comparable<?>> SearchResults<T> findByCriteria(
            PathBuilder<T> entity, EntityManager entityManager,
            DatatablesCriterias datatablesCriterias,
            BooleanBuilder basePredicate, ConversionService conversionService,
            MessageSource messageSource) throws IllegalArgumentException {
        return findByCriteria(entity, null, null, entityManager,
                datatablesCriterias, basePredicate, false, conversionService,
                messageSource, null);
    }

    /**
     * Execute a select query on entityClass using <a
     * href="http://www.querydsl.com/">Querydsl</a> which enables the
     * construction of type-safe SQL-like queries.
     * 
     * @param entity builder for entity to use in search. Represents the entity
     *        and gives access to its properties for query purposes
     * @param entityManager {@code entityClass} {@link EntityManager}
     * @param datatablesCriterias datatables parameters for query
     * @param basePredicate (optional) base filter conditions
     * @param conversionService required by filter-by-expression and rows-on-top
     *        (otherwise optional)
     * @param messageSource required by filter-by-expression (otherwise
     *        optional)
     * @param rowsOnTopIds (optional) array with id of rows to show on top of
     *        result list
     * @return
     * @throws IllegalArgumentException
     */
    public static <T, E extends Comparable<?>> SearchResults<T> findByCriteria(
            PathBuilder<T> entity, EntityManager entityManager,
            DatatablesCriterias datatablesCriterias,
            BooleanBuilder basePredicate, ConversionService conversionService,
            MessageSource messageSource, Object[] rowsOnTopIds)
            throws IllegalArgumentException {
        return findByCriteria(entity, null, null, entityManager,
                datatablesCriterias, basePredicate, false, conversionService,
                messageSource, rowsOnTopIds);
    }

    /**
     * Execute a select query on entityClass using <a
     * href="http://www.querydsl.com/">Querydsl</a> which enables the
     * construction of type-safe SQL-like queries.
     * 
     * @param entity builder for entity to use in search. Represents the entity
     *        and gives access to its properties for query purposes
     * @param entityManager {@code entityClass} {@link EntityManager}
     * @param datatablesCriterias datatables parameters for query
     * @param basePredicate (optional) base filter conditions
     * @return
     * @deprecated see
     *             {@link #findByCriteria(PathBuilder, Map, Map, EntityManager, DatatablesCriterias, BooleanBuilder, boolean, ConversionService, MessageSource, Object[])}
     */
    public static <T, E extends Comparable<?>> SearchResults<T> findByCriteria(
            PathBuilder<T> entity, EntityManager entityManager,
            DatatablesCriterias datatablesCriterias,
            BooleanBuilder basePredicate) throws IllegalArgumentException {
        return findByCriteria(entity, null, null, entityManager,
                datatablesCriterias, basePredicate, false, null, null, null);
    }

    /**
     * Execute a select query on entityClass using <a
     * href="http://www.querydsl.com/">Querydsl</a> which enables the
     * construction of type-safe SQL-like queries.
     * 
     * @param entity builder for entity to use in search. Represents the entity
     *        and gives access to its properties for query purposes
     * @param filterByAssociations (optional) for each related entity to join
     *        contain as key the name of the association and as value the List
     *        of related entity fields to filter by
     * @param orderByAssociations (optional) for each related entity to order
     *        contain as key the name of the association and as value the List
     *        of related entity fields to order by
     * @param entityManager {@code entityClass} {@link EntityManager}
     * @param datatablesCriterias datatables parameters for query
     * @param basePredicate (optional) base filter conditions
     * @param distinct use distinct query
     * @param conversionService required by filter-by-expression and rows-on-top
     *        (otherwise optional)
     * @param messageSource required by filter-by-expression (otherwise
     *        optional)
     * @return
     * @deprecated see
     *             {@link #findByCriteria(PathBuilder, Map, Map, EntityManager, DatatablesCriterias, BooleanBuilder, boolean, ConversionService, MessageSource, Object[])}
     */
    public static <T, E extends Comparable<?>> SearchResults<T> findByCriteria(
            PathBuilder<T> entity,
            Map<String, List<String>> filterByAssociations,
            Map<String, List<String>> orderByAssociations,
            EntityManager entityManager,
            DatatablesCriterias datatablesCriterias,
            BooleanBuilder basePredicate, boolean distinct,
            ConversionService conversionService, MessageSource messageSource)
            throws IllegalArgumentException {

        return findByCriteria(entity, null, null, entityManager,
                datatablesCriterias, basePredicate, false, null, null, null);
    }

    /**
     * Execute a select query on entityClass using <a
     * href="http://www.querydsl.com/">Querydsl</a> which enables the
     * construction of type-safe SQL-like queries.
     * 
     * @param entity builder for entity to use in search. Represents the entity
     *        and gives access to its properties for query purposes
     * @param filterByAssociations (optional) for each related entity to join
     *        contain as key the name of the association and as value the List
     *        of related entity fields to filter by
     * @param orderByAssociations (optional) for each related entity to order
     *        contain as key the name of the association and as value the List
     *        of related entity fields to order by
     * @param entityManager {@code entityClass} {@link EntityManager}
     * @param datatablesCriterias datatables parameters for query
     * @param basePredicate (optional) base filter conditions
     * @param distinct use distinct query
     * @param conversionService required by filter-by-expression and rows-on-top
     *        (otherwise optional)
     * @param messageSource required by filter-by-expression (otherwise
     *        optional)
     * @param rowsOnTopIds (optional) array with id of rows to show on top of
     *        result list
     * @return
     * @throws IllegalArgumentException
     */
    public static <T, E extends Comparable<?>> SearchResults<T> findByCriteria(
            PathBuilder<T> entity,
            Map<String, List<String>> filterByAssociations,
            Map<String, List<String>> orderByAssociations,
            EntityManager entityManager,
            DatatablesCriterias datatablesCriterias,
            BooleanBuilder basePredicate, boolean distinct,
            ConversionService conversionService, MessageSource messageSource,
            Object[] rowsOnTopIds) throws IllegalArgumentException {
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

        query = prepareQueryAssociationMap(entity, filterByAssociations,
                datatablesCriterias, findInAllColumns, query, associationMap);

        // ----- Query WHERE clauses -----

        // Filters by column. Using BooleanBuilder, a cascading builder for
        // Predicate expressions
        BooleanBuilder filtersByColumnPredicate = new BooleanBuilder();

        // Filters by table (for all columns)
        BooleanBuilder filtersByTablePredicate = new BooleanBuilder();

        try {

            // Build the filters by column expression
            if (datatablesCriterias.hasOneFilteredColumn()) {

                filtersByColumnPredicate = prepareQueryFilterPart(entity,
                        filterByAssociations, datatablesCriterias,
                        associationMap, filtersByColumnPredicate,
                        conversionService, messageSource);
            }

            // Build the query to search the given value in all columns
            filtersByTablePredicate = prepareQuerySearchPart(entity,
                    filterByAssociations, datatablesCriterias,
                    findInAllColumns, associationMap, filtersByTablePredicate,
                    conversionService);
        }
        catch (Exception e) {
            SearchResults<T> searchResults = new SearchResults<T>(
                    new ArrayList<T>(0), 0, isPaged, new Long(
                            datatablesCriterias.getDisplayStart()), new Long(
                            datatablesCriterias.getDisplaySize()), 0);
            return searchResults;
        }

        // ----- Query ORDER BY -----

        List<OrderSpecifier<?>> orderSpecifiersList = prepareQueryOrder(entity,
                orderByAssociations, datatablesCriterias, associationMap);

        // ----- Query results paging -----

        Long offset = null;
        Long limit = null;

        if (isPaged) {
            limit = new Long(datatablesCriterias.getDisplaySize());
        }
        if (datatablesCriterias.getDisplayStart() != null
                && datatablesCriterias.getDisplayStart() >= 0) {
            offset = new Long(datatablesCriterias.getDisplayStart());
        }

        // ------- manage Rows-on-top ----

        List<T> firstRows = null;

        // Decrease limits if firstRowsIds is used
        if (rowsOnTopIds != null) {

            // Coherce row-on-top ids types
            Object[] cohercedRowsOnTopId = new Object[rowsOnTopIds.length];

            EntityType entityMetamodel = entityManager.getMetamodel().entity(
                    entity.getType());
            // We always have just one id. This id can be an Embedded Id
            Class idType = entityMetamodel.getIdType().getJavaType();
            SingularAttribute idAttr = (SingularAttribute) entityMetamodel
                    .getId(idType);

            Object curId;
            for (int i = 0; i < rowsOnTopIds.length; i++) {
                curId = rowsOnTopIds[i];
                if (curId.getClass() != idType) {
                    cohercedRowsOnTopId[i] = conversionService.convert(curId,
                            idType);
                }
                else {
                    cohercedRowsOnTopId[i] = curId;
                }
            }

            // Create expression for rows-on-top
            BooleanExpression firstRowsInExpression = QuerydslUtils
                    .createCollectionExpression(entity, idAttr.getName(),
                            Arrays.asList(cohercedRowsOnTopId));

            // Exclude firstRows from base query
            basePredicate = basePredicate.and(firstRowsInExpression.not());

            // Gets rows on top
            JPAQuery firstRowsQuery = new JPAQuery(entityManager);
            firstRows = firstRowsQuery.from(entity)
                    .where(firstRowsInExpression).list(entity);

            // Adjust limit with rows-on-top found
            if (limit != null) {
                limit = limit - firstRows.size();
            }

        }

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

        // Calculate the total amount of rows taking in account datatables
        // search and paging criterias. When results are paginated we
        // must execute a count query, otherwise the size of matched rows List
        // is the total amount of rows
        long totalResultCount = 0;
        if (isPaged) {
            totalResultCount = query.count();
        }

        // If offset value is bigger than total results,
        // offset needs start on 0
        if (offset > totalResultCount) {
            offset = new Long(0);
        }

        // QueryModifiers combines limit and offset
        QueryModifiers queryModifiers = new QueryModifiers(limit, offset);

        // List ordered and paginated results. An empty list is returned for no
        // results.
        elements = query
                .orderBy(
                        orderSpecifiersList
                                .toArray(new OrderSpecifier[orderSpecifiersList
                                        .size()])).restrict(queryModifiers)
                .list(entity);

        if (!isPaged) {
            totalResultCount = elements.size();
        }

        // Calculate the total amount of entities including base filters only
        long totalBaseCount = baseQuery.count();

        if (firstRows != null) {
            // Adjust result with rows-on-top
            totalResultCount = totalResultCount + firstRows.size();
            totalBaseCount = totalBaseCount + firstRows.size();
            elements.addAll(0, firstRows);
        }

        // Create a new SearchResults instance
        if (offset == null) {
            offset = new Long(0);
        }
        if (limit == null) {
            limit = totalBaseCount;
        }
        SearchResults<T> searchResults = new SearchResults<T>(elements,
                totalResultCount, isPaged, offset, limit, totalBaseCount);

        return searchResults;
    }

    /**
     * Prepares associationMap for findByCriteria
     * 
     * @param entity
     * @param filterByAssociations
     * @param datatablesCriterias
     * @param findInAllColumns
     * @param query
     * @param associationMap
     * @return
     */
    private static <T> JPAQuery prepareQueryAssociationMap(
            PathBuilder<T> entity,
            Map<String, List<String>> filterByAssociations,
            DatatablesCriterias datatablesCriterias, boolean findInAllColumns,
            JPAQuery query, Map<String, PathBuilder<?>> associationMap) {
        for (ColumnDef column : datatablesCriterias.getColumnDefs()) {

            // true if the search must include this column
            boolean findInColumn = StringUtils.isNotEmpty(column.getSearch());

            // If no joins given for this column, don't add the JOIN to query
            // to improve performance
            String associationName = unescapeDot(column.getName());
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
        return query;
    }

    /**
     * Prepares filter part for a query of findByCriteria
     * 
     * @param entity
     * @param filterByAssociations
     * @param datatablesCriterias
     * @param associationMap
     * @param filtersByColumnPredicate
     * @return
     */
    private static <T> BooleanBuilder prepareQueryFilterPart(
            PathBuilder<T> entity,
            Map<String, List<String>> filterByAssociations,
            DatatablesCriterias datatablesCriterias,
            Map<String, PathBuilder<?>> associationMap,
            BooleanBuilder filtersByColumnPredicate,
            ConversionService conversionService, MessageSource messageSource) {
        // Add filterable columns only
        for (ColumnDef column : datatablesCriterias.getColumnDefs()) {

            // Each column has its own search by value
            String searchStr = column.getSearch();

            // true if the search must include this column
            boolean findInColumn = column.isFilterable()
                    && StringUtils.isNotEmpty(searchStr);

            if (findInColumn) {

                // Entity field name and type
                String fieldName = unescapeDot(column.getName());
                Class<?> fieldType = QuerydslUtils.getFieldType(fieldName,
                        entity);
                TypeDescriptor descriptor = QuerydslUtils.getTypeDescriptor(
                        fieldName, entity);

                // On column search, connect where clauses together by
                // AND
                // because we want found the records which columns
                // match with column filters
                filtersByColumnPredicate = filtersByColumnPredicate
                        .and(QuerydslUtils.createExpression(entity, fieldName,
                                descriptor, searchStr, conversionService,
                                messageSource));

                // TODO: Este codigo se puede pasar a QuerydslUtils ?

                // If column is an association and there are given
                // join attributes, add those attributes to WHERE
                // predicates
                List<String> attributes = filterByAssociations.get(fieldName);
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
                                .findPropertyType(associationFieldName,
                                        ArrayUtils
                                                .<Class<?>> toArray(fieldType));

                        // On association search, connect
                        // associated entity where clauses by OR
                        // because all assoc entity properties are
                        // inside the same column and any of its
                        // property value can match with given search
                        // value
                        filtersByAssociationPredicate = filtersByAssociationPredicate
                                .or(QuerydslUtils.createExpression(
                                        associationPath, associationFieldName,
                                        associationFieldType, searchStr));
                    }

                    filtersByColumnPredicate = filtersByColumnPredicate
                            .and(filtersByAssociationPredicate.getValue());
                }
            }
        }
        return filtersByColumnPredicate;
    }

    /**
     * Prepare search part for a query of findByCriteria
     * 
     * @param entity
     * @param filterByAssociations
     * @param datatablesCriterias
     * @param findInAllColumns
     * @param associationMap
     * @param filtersByTablePredicate
     * @return
     */
    private static <T> BooleanBuilder prepareQuerySearchPart(
            PathBuilder<T> entity,
            Map<String, List<String>> filterByAssociations,
            DatatablesCriterias datatablesCriterias, boolean findInAllColumns,
            Map<String, PathBuilder<?>> associationMap,
            BooleanBuilder filtersByTablePredicate,
            ConversionService conversionService) {
        String searchStr = datatablesCriterias.getSearch();
        if (findInAllColumns) {
            boolean expressionExists = false;
            // Add filterable columns only
            for (ColumnDef column : datatablesCriterias.getColumnDefs()) {
                if (column.isFilterable()) {

                    // Entity field name and type
                    String fieldName = unescapeDot(column.getName());
                    Class<?> fieldType = QuerydslUtils.getFieldType(fieldName,
                            entity);
                    TypeDescriptor descriptor = QuerydslUtils
                            .getTypeDescriptor(fieldName, entity);

                    // Find in all columns means we want to find given
                    // value in at least one entity property, so we must
                    // join the where clauses by OR
                    Predicate expression = QuerydslUtils.createExpression(
                            entity, fieldName, descriptor, searchStr,
                            conversionService);
                    if (expression != null) {
                        filtersByTablePredicate = filtersByTablePredicate
                                .or(expression);
                        expressionExists = true;
                    }

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
                                    .or(QuerydslUtils.createExpression(
                                            associationPath,
                                            associationFieldName,
                                            associationFieldType, searchStr));
                        }
                    }
                }
            }
            // If expression is null returns error to returns an empty
            // DataSource
            if (!expressionExists) {
                throw new RuntimeException("Expression cannot be null");
            }
        }
        return filtersByTablePredicate;
    }

    /**
     * prepares order part for a query of findByCriteria
     * 
     * @param entity
     * @param orderByAssociations
     * @param datatablesCriterias
     * @param associationMap
     * @return
     */
    @SuppressWarnings("unchecked")
    private static <E extends Comparable<?>, T> List<OrderSpecifier<?>> prepareQueryOrder(
            PathBuilder<T> entity,
            Map<String, List<String>> orderByAssociations,
            DatatablesCriterias datatablesCriterias,
            Map<String, PathBuilder<?>> associationMap) {
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
                String fieldName = unescapeDot(column.getName());
                Class<E> fieldType = (Class<E>) QuerydslUtils.getFieldType(
                        fieldName, entity);

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
                    continue;
                }
            }
        }
        return orderSpecifiersList;
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
     *        contains one pattern for each entity Date field keyed by field
     *        name. For Roo compatibility the key could follow the pattern
     *        {@code uncapitalize( ENTITY ) + "_" + lower_case( FIELD ) + "_date_format"}
     *        too
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
        Map<String, SimpleDateFormat> dateFormatters = new HashMap<String, SimpleDateFormat>(
                datePatterns.size());

        // Prepare required fields
        Set<String> fields = new HashSet<String>();
        fields.add(pkFieldName);

        // Add fields from request
        for (ColumnDef colum : columns) {
            fields.add(colum.getName());
        }

        BeanWrapperImpl entityBean = null;
        String valueStr = null;
        // Populate each row, note a row is a Map containing
        // fieldName = fieldValue
        for (T entity : entities) {
            Map<String, String> row = new HashMap<String, String>(fields.size());
            if (entityBean == null) {
                entityBean = new BeanWrapperImpl(entity);
            }
            else {
                entityBean.setWrappedInstance(entity);
            }
            for (String fieldName : fields) {

                String unescapedFieldName = unescapeDot(fieldName);

                // check if property exists (trace it else)
                if (!entityBean.isReadableProperty(unescapedFieldName)) {
                    LOGGER.debug("Property ["
                            .concat(unescapedFieldName)
                            .concat("] not found in bean [")
                            .concat(StringUtils.defaultString(
                                    entity.toString(), "{null}")).concat("]"));
                    continue;
                }

                // Convert field value to string
                valueStr = convertFieldValueToString(datePatterns,
                        dateFormatters, conversionService, entityBean, entity,
                        fieldName, unescapedFieldName);
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
     * Convert a field value to string
     * 
     * @param datePatterns
     * @param dateFormatters
     * @param conversionService
     * @param entityBean
     * @param entity
     * @param fieldName
     * @param unescapedFieldName
     * @return
     */
    private static <T> String convertFieldValueToString(
            Map<String, Object> datePatterns,
            Map<String, SimpleDateFormat> dateFormatters,
            ConversionService conversionService, BeanWrapperImpl entityBean,
            T entity, String fieldName, String unescapedFieldName) {
        try {
            Object value = null;
            TypeDescriptor fieldDesc = entityBean
                    .getPropertyTypeDescriptor(unescapedFieldName);
            TypeDescriptor strDesc = TypeDescriptor.valueOf(String.class);
            value = entityBean.getPropertyValue(unescapedFieldName);
            if (value == null) {
                return "";
            }

            // For dates
            if (Date.class.isAssignableFrom(value.getClass())
                    || Calendar.class.isAssignableFrom(value.getClass())) {
                SimpleDateFormat formatter = getDateFormatter(datePatterns,
                        dateFormatters, entityBean.getWrappedClass(),
                        unescapedFieldName);
                if (formatter != null) {
                    if (Calendar.class.isAssignableFrom(value.getClass())) {
                        // Gets Date instance as SimpleDateFormat
                        // doesn't works with Calendar
                        value = ((Calendar) value).getTime();
                    }
                    return formatter.format(value);
                }
            }
            // Try to use conversion service (uses field descrition
            // to handle field format annotations)
            if (conversionService.canConvert(fieldDesc, strDesc)) {
                return (String) conversionService.convert(value, fieldDesc,
                        strDesc);
            }
            else {
                return ObjectUtils.getDisplayString(value);
            }
        }
        catch (Exception ex) {
            // debug getting value problem
            LOGGER.error("Error getting value of field [".concat(fieldName)
                    .concat("]").concat(" in bean [").concat(entity.toString())
                    .concat("]"), ex);
            return "";
        }
    }

    /**
     * Get Date formatter by field name
     * <p/>
     * If no pattern found, try standard Roo key
     * {@code uncapitalize( ENTITY ) + "_" + lower_case( FIELD ) + "_date_format"}
     * 
     * @param datePatterns Contains field name and related data pattern
     * @param entityClass Entity class to which the field belong to
     * @param fieldName Field to search pattern
     * @return
     */
    private static SimpleDateFormat getDateFormatter(
            Map<String, Object> datePatterns,
            Map<String, SimpleDateFormat> dateFormatters, Class<?> entityClass,
            String fieldName) {

        SimpleDateFormat result = null;
        String lowerCaseFieldName = fieldName.toLowerCase();
        result = dateFormatters.get(lowerCaseFieldName);
        if (result != null) {
            return result;
        }
        else if (dateFormatters.containsKey(lowerCaseFieldName)) {
            return null;
        }

        // Get pattern by field name
        String pattern = (String) datePatterns.get(lowerCaseFieldName);
        if (StringUtils.isEmpty(pattern)) {
            // Try to get the name of entity class (without javassit suffix)
            String baseClass = StringUtils.substringBefore(
                    entityClass.getSimpleName(), "$");// );"_$");
            // try to get pattern by Roo key
            String rooKey = StringUtils.uncapitalize(baseClass).concat("_")
                    .concat(lowerCaseFieldName).concat("_date_format");

            pattern = (String) datePatterns.get(rooKey);
        }
        if (!StringUtils.isEmpty(pattern)) {
            result = new SimpleDateFormat(pattern);
        }
        dateFormatters.put(lowerCaseFieldName, result);
        return result;
    }

    /**
     * Constructs the {@code HtmlTable} used to export the data.
     * <p />
     * It uses the parameters of the request to check if the column is
     * exportable or not, these parameters are named:
     * <ul>
     * <li>{@code [export_type_extension]ExportColumns}, where
     * <emp>[export_type_extension]</emp> is the extension of the format to
     * export, for example: {@code csvExportColumns}</li>
     * <li>{@code allExportColumns}</li>
     * </ul>
     * <p />
     * Also uses the parameter {@code columnsTitle} to indicate the title of
     * each column, this parameter has as value a {@code String} with the format
     * of a Map as follows:
     * 
     * <pre>
     * {property1||value1, property2||value2, ... , propertyN||valueN}
     * </pre>
     * 
     * @param data the data to make the {@code HtmlTable}.
     * @param criterias the {@code DatatablesCriterias}.
     * @param exportConf the {@code ExportConf}.
     * @param request the {@code HttpServletRequest}.
     * @return the {@code HtmlTable} used to export the data.
     */
    public static HtmlTable makeHtmlTable(List<Map<String, String>> data,
            DatatablesCriterias criterias, ExportConf exportConf,
            HttpServletRequest request) {

        ColumnStep tableBuilder = new HtmlTableBuilder<Map<String, String>>()
                .newBuilder("tableId", data, request);

        // Obtain exportable columns
        String exportTypeExtension = StringUtils.lowerCase(exportConf.getType()
                .getExtension());
        String thisFormatExportColumnsStr = request
                .getParameter(exportTypeExtension.concat("ExportColumns"));
        if (StringUtils.isEmpty(thisFormatExportColumnsStr)) {
            thisFormatExportColumnsStr = "";
        }
        String allFormatExportColumnsStr = request
                .getParameter("allExportColumns");
        if (StringUtils.isEmpty(allFormatExportColumnsStr)) {
            allFormatExportColumnsStr = "";
        }
        List<String> thisFormatExporColumns = Arrays.asList(StringUtils.split(
                thisFormatExportColumnsStr, ","));
        List<String> allFormatExportColumns = Arrays.asList(StringUtils.split(
                allFormatExportColumnsStr, ","));

        BeforeEndStep columns = null;
        if (!allFormatExportColumns.isEmpty()
                || !thisFormatExporColumns.isEmpty()) {

            // Obtain the column titles
            Map<String, String> columnsTitleMap = new HashMap<String, String>();
            String columnsTitleStr = request.getParameter("columnsTitle");
            columnsTitleStr = StringUtils.substring(columnsTitleStr, 1,
                    (columnsTitleStr.length() - 1));
            List<String> columnsTitleList = Arrays.asList(StringUtils.split(
                    columnsTitleStr, ","));
            for (String columnsTitle : columnsTitleList) {
                String[] columsTitleArray = StringUtils.split(columnsTitle,
                        "||");
                if (columsTitleArray.length == 2) {
                    columnsTitleMap.put(columsTitleArray[0].trim(),
                            columsTitleArray[1].trim());
                }
            }

            List<ColumnDef> columnDefs = criterias.getColumnDefs();
            for (ColumnDef columnDef : columnDefs) {
                String columnProperty = columnDef.getName();
                if (allFormatExportColumns.contains(columnProperty)
                        || thisFormatExporColumns.contains(columnProperty)) {
                    String columnTitle = columnsTitleMap.get(columnProperty);
                    if (StringUtils.isBlank(columnTitle)) {
                        columnTitle = columnProperty;
                    }
                    columnTitle = StringUtils.replace(columnTitle, "~~", ",");
                    columns = tableBuilder.column()
                            .fillWithProperty(columnProperty)
                            .title(columnTitle);
                }
            }
        }
        if (columns == null) {
            columns = tableBuilder.column().fillWithProperty("-").title("---");
        }

        return columns.configureExport(exportConf).build();
    }

    /**
     * Unescapes the string to represent it with dot ".", that is, the default
     * character used to indicate attributes of an object (p.e. "user.name")
     * 
     * @param str the string to unescape.
     * @return the string unescaped.
     */
    private static String unescapeDot(String str) {
        return str.replace(SEPARATOR_FIELDS_ESCAPED, SEPARATOR_FIELDS);
    }

    /**
     * 
     * Check if filter expression is correct for the input type
     * 
     * @param type
     * @param expression
     * @param messageSource
     * @return
     */
    public static boolean checkFilterExpressions(Class type, String expression,
            MessageSource messageSource) {
        // By default filter is not correct

        Set<Class<?>> numberPrimitives = new HashSet<Class<?>>(
                Arrays.asList(new Class<?>[] { int.class, long.class,
                        double.class, float.class, short.class }));

        // Checking String filters
        if (String.class == type) {
            return checkStringFilters(expression, messageSource);
        }
        else if (Boolean.class == type || boolean.class == type) {
            return checkBooleanFilters(expression, messageSource);
        }
        else if (Number.class.isAssignableFrom(type)
                || numberPrimitives.contains(type)) {
            return checkNumericFilters(expression, messageSource);
        }
        else if (Date.class.isAssignableFrom(type)
                || Calendar.class.isAssignableFrom(type)) {
            return checkDateFilters(expression, messageSource);
        }
        return false;
    }

    public static boolean checkStringFilters(String expression,
            MessageSource messageSource) {
        // All operations
        String endsOperation = "ENDS";
        String startsOperation = "STARTS";
        String containsOperation = "CONTAINS";
        String isEmptyOperation = "ISEMPTY";
        String isNotEmptyOperation = "ISNOTEMPTY";
        String isNullOperation = "ISNULL";
        String isNotNullOperation = "NOTNULL";

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
            isNullOperation = messageSource.getMessage(
                    "global.filters.operations.all.isnull", null,
                    LocaleContextHolder.getLocale());
            isNotNullOperation = messageSource.getMessage(
                    "global.filters.operations.all.notnull", null,
                    LocaleContextHolder.getLocale());
        }

        // If written expression is ENDS operation
        Pattern endsOperator = Pattern.compile(String.format(
                "%s[(]([a-zA-Z\\s\\d]*)[)]", endsOperation));
        Matcher endsMatcher = endsOperator.matcher(expression);

        if (endsMatcher.matches()) {
            return true;
        }

        // If written expression is STARTS operation
        Pattern startsOperator = Pattern.compile(String.format("%s[(](.+)[)]$",
                startsOperation));
        Matcher startsMatcher = startsOperator.matcher(expression);

        if (startsMatcher.matches()) {
            return true;
        }

        // If written expression is CONTAINS operation
        Pattern containsOperator = Pattern.compile(String.format(
                "%s[(](.+)[)]$", containsOperation));
        Matcher containsMatcher = containsOperator.matcher(expression);

        if (containsMatcher.matches()) {
            return true;
        }

        // If written expression is ISEMPTY operation
        Pattern isEmptyOperator = Pattern.compile(String.format("%s",
                isEmptyOperation));
        Matcher isEmptyMatcher = isEmptyOperator.matcher(expression);
        if (isEmptyMatcher.matches()) {
            return true;

        }

        // If written expression is ISNOTEMPTY operation
        Pattern isNotEmptyOperator = Pattern.compile(String.format("%s",
                isNotEmptyOperation));
        Matcher isNotEmptyMatcher = isNotEmptyOperator.matcher(expression);
        if (isNotEmptyMatcher.matches()) {
            return true;

        }

        // If written expression is ISNULL operation
        Pattern isNullOperator = Pattern.compile(String.format("%s",
                isNullOperation));
        Matcher isNullMatcher = isNullOperator.matcher(expression);
        if (isNullMatcher.matches()) {
            return true;

        }

        // If written expression is ISNOTNULL operation
        Pattern isNotNullOperator = Pattern.compile(String.format("%s",
                isNotNullOperation));
        Matcher isNotNullMatcher = isNotNullOperator.matcher(expression);
        if (isNotNullMatcher.matches()) {
            return true;

        }

        // If written expression is a symbol operation expression

        // Getting expressions with symbols
        Pattern symbolOperator = Pattern.compile("[=]?(.+)");
        Matcher symbolMatcher = symbolOperator.matcher(expression);

        if (symbolMatcher.matches()) {
            return true;
        }

        return false;
    }

    public static boolean checkBooleanFilters(String expression,
            MessageSource messageSource) {
        // Getting all operations
        String trueOperation = "TRUE";
        String falseOperation = "FALSE";
        String isNullOperation = "ISNULL";
        String isNotNullOperation = "NOTNULL";

        if (messageSource != null) {
            trueOperation = messageSource.getMessage(
                    "global.filters.operations.boolean.true", null,
                    LocaleContextHolder.getLocale());
            falseOperation = messageSource.getMessage(
                    "global.filters.operations.boolean.false", null,
                    LocaleContextHolder.getLocale());
            isNullOperation = messageSource.getMessage(
                    "global.filters.operations.all.isnull", null,
                    LocaleContextHolder.getLocale());
            isNotNullOperation = messageSource.getMessage(
                    "global.filters.operations.all.notnull", null,
                    LocaleContextHolder.getLocale());
        }

        // If written function is TRUE
        Pattern trueOperator = Pattern.compile(String.format("%s",
                trueOperation));
        Matcher trueMatcher = trueOperator.matcher(expression);

        if (trueMatcher.matches()) {
            return true;
        }

        // If written function is FALSE
        Pattern falseOperator = Pattern.compile(String.format("%s",
                falseOperation));
        Matcher falseMatcher = falseOperator.matcher(expression);

        if (falseMatcher.matches()) {
            return true;
        }

        // If written expression is ISNULL operation
        Pattern isNullOperator = Pattern.compile(String.format("%s",
                isNullOperation));
        Matcher isNullMatcher = isNullOperator.matcher(expression);
        if (isNullMatcher.matches()) {
            return true;

        }

        // If written expression is ISNOTNULL operation
        Pattern isNotNullOperator = Pattern.compile(String.format("%s",
                isNotNullOperation));
        Matcher isNotNullMatcher = isNotNullOperator.matcher(expression);
        if (isNotNullMatcher.matches()) {
            return true;

        }

        return false;
    }

    public static boolean checkNumericFilters(String expression,
            MessageSource messageSource) {
        if (NumberUtils.isNumber(expression)) {
            return true;
        }
        else {
            // Getting expressions with symbols
            Pattern symbolOperator = Pattern
                    .compile("([!=><][=>]?)([-]?[\\d.,]*)");
            Matcher symbolMatcher = symbolOperator.matcher(expression);

            if (symbolMatcher.matches()) {

                String symbolExpression = symbolMatcher.group(1);
                String value = symbolMatcher.group(2);

                if (!StringUtils.isBlank(value)) {
                    if (symbolExpression.equals("=")
                            || symbolExpression.equals("==")) {
                        return true;
                    }
                    else if (symbolExpression.equals(">")
                            || symbolExpression.equals(">>")) {
                        return true;
                    }
                    else if (symbolExpression.equals("<")) {
                        return true;
                    }
                    else if (symbolExpression.equals(">=")) {
                        return true;
                    }
                    else if (symbolExpression.equals("<=")) {
                        return true;
                    }
                    else if (symbolExpression.equals("!=")
                            || symbolExpression.equals("<>")) {
                        return true;
                    }
                }
            }

            // Get all operations
            String isNullOperation = "ISNULL";
            String isNotNullOperation = "NOTNULL";
            String betweenOperation = "BETWEEN";

            if (messageSource != null) {
                isNullOperation = messageSource.getMessage(
                        "global.filters.operations.all.isnull", null,
                        LocaleContextHolder.getLocale());
                isNotNullOperation = messageSource.getMessage(
                        "global.filters.operations.all.notnull", null,
                        LocaleContextHolder.getLocale());
                betweenOperation = messageSource.getMessage(
                        "global.filters.operations.number.between", null,
                        LocaleContextHolder.getLocale());
            }

            // If written function is BETWEEN function
            Pattern betweenFunctionOperator = Pattern.compile(String.format(
                    "%s[(]([-]?[\\d.,]*);([-]?[\\d.,]*)[)]", betweenOperation));
            Matcher betweenFunctionMatcher = betweenFunctionOperator
                    .matcher(expression);

            if (betweenFunctionMatcher.matches()) {
                // Getting valueFrom and valueTo
                String valueFrom = betweenFunctionMatcher.group(1);
                String valueTo = betweenFunctionMatcher.group(2);
                if (!StringUtils.isBlank(valueFrom)
                        && !StringUtils.isBlank(valueTo)) {
                    return true;
                }
            }

            // If written expression is ISNULL operation
            Pattern isNullOperator = Pattern.compile(String.format("%s",
                    isNullOperation));
            Matcher isNullMatcher = isNullOperator.matcher(expression);
            if (isNullMatcher.matches()) {
                return true;

            }

            // If written expression is ISNOTNULL operation
            Pattern isNotNullOperator = Pattern.compile(String.format("%s",
                    isNotNullOperation));
            Matcher isNotNullMatcher = isNotNullOperator.matcher(expression);
            if (isNotNullMatcher.matches()) {
                return true;

            }
        }
        return false;
    }

    public static boolean checkDateFilters(String expression,
            MessageSource messageSource) {

        // All possible operations
        String date = "DATE";
        String year = "YEAR";
        String month = "MONTH";
        String day = "DAY";
        String between = "BETWEEN";
        String isNullOperation = "ISNULL";
        String isNotNullOperation = "NOTNULL";

        String datePattern = "dd/MM/yyyy";

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
            isNullOperation = messageSource.getMessage(
                    "global.filters.operations.all.isnull", null,
                    LocaleContextHolder.getLocale());
            isNotNullOperation = messageSource.getMessage(
                    "global.filters.operations.all.notnull", null,
                    LocaleContextHolder.getLocale());
            datePattern = messageSource.getMessage(
                    "global.filters.operations.date.pattern", null,
                    LocaleContextHolder.getLocale());
        }

        // Getting simpleDateFormat
        DateFormat dateFormat = new SimpleDateFormat(datePattern);

        // If written expression is ISNULL operation
        Pattern isNullOperator = Pattern.compile(String.format("%s",
                isNullOperation));
        Matcher isNullMatcher = isNullOperator.matcher(expression);
        if (isNullMatcher.matches()) {
            return true;

        }

        // If written expression is ISNOTNULL operation
        Pattern isNotNullOperator = Pattern.compile(String.format("%s",
                isNotNullOperation));
        Matcher isNotNullMatcher = isNotNullOperator.matcher(expression);
        if (isNotNullMatcher.matches()) {
            return true;

        }

        // Creating regex to get DATE operator
        Pattern dateOperator = Pattern.compile(String.format(
                "%s[(]([\\d\\/]*)[)]", date));
        Matcher dateMatcher = dateOperator.matcher(expression);

        if (dateMatcher.matches()) {
            try {
                String dateValue = dateMatcher.group(1);
                Date dateToFilter = dateFormat.parse(dateValue);

                Calendar searchCal = Calendar.getInstance();
                searchCal.setTime(dateToFilter);

                return true;
            }
            catch (ParseException e) {
                return false;
            }
        }

        // Creating regex to get YEAR operator
        Pattern yearOperator = Pattern.compile(String.format(
                "%s[(]([\\d]*)[)]", year));
        Matcher yearMatcher = yearOperator.matcher(expression);

        if (yearMatcher.matches()) {
            return true;
        }

        // Creating regex to get MONTH operator
        Pattern monthOperator = Pattern.compile(String.format(
                "%s[(]([\\d]*)[)]", month));
        Matcher monthMatcher = monthOperator.matcher(expression);

        if (monthMatcher.matches()) {
            return true;
        }

        // Creating regex to get DAY operator
        Pattern dayOperator = Pattern.compile(String.format("%s[(]([\\d]*)[)]",
                day));
        Matcher dayMatcher = dayOperator.matcher(expression);

        if (dayMatcher.matches()) {
            return true;
        }

        // Creating regex to get BETWEEN operator
        Pattern betweenOperator = Pattern.compile(String.format(
                "%s[(]([\\d\\/]*);([\\d\\/]*)[)]", between));
        Matcher betweenMatcher = betweenOperator.matcher(expression);

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

                    return true;

                }
                catch (Exception e) {
                    return false;
                }
            }

        }

        return false;
    }
}
