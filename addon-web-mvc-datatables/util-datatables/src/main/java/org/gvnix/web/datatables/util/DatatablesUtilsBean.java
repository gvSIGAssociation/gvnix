/*
 * Copyright 2015 DiSiD Technologies S.L.L. All rights reserved.
 * 
 * Project  : DiSiD org.gvnix.web.datatables 
 * SVN Id   : $Id$
 */
package org.gvnix.web.datatables.util;

import java.text.DateFormat;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;

import org.gvnix.web.datatables.query.SearchResults;
import org.springframework.core.convert.ConversionService;

import com.github.dandelion.datatables.core.ajax.ColumnDef;
import com.github.dandelion.datatables.core.ajax.DataSet;
import com.github.dandelion.datatables.core.ajax.DatatablesCriterias;
import com.github.dandelion.datatables.core.export.ExportConf;
import com.github.dandelion.datatables.core.html.HtmlTable;
import com.mysema.query.BooleanBuilder;
import com.mysema.query.jpa.impl.JPAQuery;
import com.mysema.query.types.path.PathBuilder;

public interface DatatablesUtilsBean {

    public static final String ISNULL_OPE = "ISNULL";
    public static final String NOTNULL_OPE = "NOTNULL";

    public static final String G_ISNULL_OPE = "global.filters.operations.all.isnull";
    public static final String G_NOTNULL_OPE = "global.filters.operations.all.notnull";

    public static final String ROWS_ON_TOP_IDS_PARAM = "dtt_row_on_top_ids";
    public static final String SEPARATOR_FIELDS = ".";
    public static final String SEPARATOR_FIELDS_ESCAPED = "_~~_";

    /**
     * Creates and returns a new JPAQuery instance for the provided
     * EntityManager
     * 
     * @param em ActiveRecord JPA EntityManager
     * @return JPAQuery instance for provided EntityManager
     */
    public JPAQuery newJPAQuery(EntityManager em);

    /**
     * 
     * 
     * @param name
     * @return
     */
    public boolean isSpecialFilterParameters(String name);

    /**
     * Execute a select query on entityClass using {@code DatatablesCriterias}
     * information for filter, sort and paginate result.
     * 
     * @param entityClass entity to use in search
     * @param datatablesCriterias datatables parameters for query
     * @return
     */
    public <T> SearchResults<T> findByCriteria(Class<T> entityClass,
            DatatablesCriterias datatablesCriterias);

    /**
     * Execute a select query on entityClass using {@code DatatablesCriterias}
     * information for filter, sort and paginate result.
     * 
     * @param entityClass entity to use in search
     * @param datatablesCriterias datatables parameters for query
     * @param baseSearchValuesMap (optional) base filter values
     * @return
     */
    public <T> SearchResults<T> findByCriteria(Class<T> entityClass,
            DatatablesCriterias datatablesCriterias,
            Map<String, Object> baseSearchValuesMap);

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
     * @param datatablesCriterias datatables parameters for query
     * @return
     */
    public <T> SearchResults<T> findByCriteria(Class<T> entityClass,
            Map<String, List<String>> filterByAssociations,
            Map<String, List<String>> orderByAssociations,
            DatatablesCriterias datatablesCriterias);

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
     * @param datatablesCriterias datatables parameters for query
     * @param baseSearchValuesMap (optional) base filter values
     * @return
     */
    public <T> SearchResults<T> findByCriteria(Class<T> entityClass,
            Map<String, List<String>> filterByAssociations,
            Map<String, List<String>> orderByAssociations,
            DatatablesCriterias datatablesCriterias,
            Map<String, Object> baseSearchValuesMap);

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
     * @param datatablesCriterias datatables parameters for query
     * @param baseSearchValuesMap (optional) base filter values
     * @param distinct use distinct query
     * @return
     */
    public <T, E extends Comparable<?>> SearchResults<T> findByCriteria(
            Class<T> entityClass,
            Map<String, List<String>> filterByAssociations,
            Map<String, List<String>> orderByAssociations,
            DatatablesCriterias datatablesCriterias,
            Map<String, Object> baseSearchValuesMap, boolean distinct);

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
     * @param datatablesCriterias datatables parameters for query
     * @param basePredicate (optional) base filter conditions
     * @param distinct use distinct query
     * @return
     */
    public <T, E extends Comparable<?>> SearchResults<T> findByCriteria(
            Class<T> entityClass,
            Map<String, List<String>> filterByAssociations,
            Map<String, List<String>> orderByAssociations,
            DatatablesCriterias datatablesCriterias,
            BooleanBuilder basePredicate, boolean distinct);

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
     * @param datatablesCriterias datatables parameters for query
     * @param basePredicate (optional) base filter
     * @param distinct use distinct query
     * @param rowsOnTopIds (optional) array with id of rows to show on top of
     *        result list
     * @return
     * @throws IllegalArgumentException
     */
    public <T, E extends Comparable<?>> SearchResults<T> findByCriteria(
            Class<T> entityClass,
            Map<String, List<String>> filterByAssociations,
            Map<String, List<String>> orderByAssociations,
            DatatablesCriterias datatablesCriterias,
            BooleanBuilder basePredicate, boolean distinct,
            Object[] rowsOnTopIds);

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
     * @param datatablesCriterias datatables parameters for query
     * @param basePredicate (optional) base filter conditions
     * @return
     */
    public <T, E extends Comparable<?>> SearchResults<T> findByCriteria(
            PathBuilder<T> entity,
            Map<String, List<String>> filterByAssociations,
            Map<String, List<String>> orderByAssociations,
            DatatablesCriterias datatablesCriterias,
            BooleanBuilder basePredicate);

    /**
     * Execute a select query on entityClass using <a
     * href="http://www.querydsl.com/">Querydsl</a> which enables the
     * construction of type-safe SQL-like queries.
     * 
     * @param entity builder for entity to use in search. Represents the entity
     *        and gives access to its properties for query purposes
     * @param datatablesCriterias datatables parameters for query
     * @param basePredicate (optional) base filter conditions
     * @return
     */
    public <T, E extends Comparable<?>> SearchResults<T> findByCriteria(
            PathBuilder<T> entity, DatatablesCriterias datatablesCriterias,
            BooleanBuilder basePredicate);

    /**
     * Execute a select query on entityClass using <a
     * href="http://www.querydsl.com/">Querydsl</a> which enables the
     * construction of type-safe SQL-like queries.
     * 
     * @param entity builder for entity to use in search. Represents the entity
     *        and gives access to its properties for query purposes
     * @param datatablesCriterias datatables parameters for query
     * @param basePredicate (optional) base filter conditions
     * @param rowsOnTopIds (optional) array with id of rows to show on top of
     *        result list
     * @return
     * @throws IllegalArgumentException
     */
    public <T, E extends Comparable<?>> SearchResults<T> findByCriteria(
            PathBuilder<T> entity, DatatablesCriterias datatablesCriterias,
            BooleanBuilder basePredicate, Object[] rowsOnTopIds);

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
     * @param datatablesCriterias datatables parameters for query
     * @param basePredicate (optional) base filter conditions
     * @param distinct use distinct query
     * @return
     */
    public <T, E extends Comparable<?>> SearchResults<T> findByCriteria(
            PathBuilder<T> entity,
            Map<String, List<String>> filterByAssociations,
            Map<String, List<String>> orderByAssociations,
            DatatablesCriterias datatablesCriterias,
            BooleanBuilder basePredicate, boolean distinct);

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
     * @param datatablesCriterias datatables parameters for query
     * @param basePredicate (optional) base filter conditions
     * @param distinct use distinct query
     * @param rowsOnTopIds (optional) array with id of rows to show on top of
     *        result list
     * @return
     * @throws IllegalArgumentException
     */
    public <T, E extends Comparable<?>> SearchResults<T> findByCriteria(
            PathBuilder<T> entity,
            Map<String, List<String>> filterByAssociations,
            Map<String, List<String>> orderByAssociations,
            DatatablesCriterias datatablesCriterias,
            BooleanBuilder basePredicate, boolean distinct,
            Object[] rowsOnTopIds);

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
    public <T> JPAQuery prepareQueryAssociationMap(PathBuilder<T> entity,
            Map<String, List<String>> filterByAssociations,
            DatatablesCriterias datatablesCriterias, boolean findInAllColumns,
            JPAQuery query, Map<String, PathBuilder<?>> associationMap);

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
     * @return
     */
    public <T> DataSet<Map<String, String>> populateDataSet(List<T> entities,
            String pkFieldName, long totalRecords, long totalDisplayRecords,
            List<ColumnDef> columns, Map<String, Object> datePatterns);

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
    public HtmlTable makeHtmlTable(List<Map<String, String>> data,
            DatatablesCriterias criterias, ExportConf exportConf,
            HttpServletRequest request);

    /**
     * 
     * Check if filter expression is correct for the input type
     * 
     * @param type
     * @param expression
     * @return
     */
    public boolean checkFilterExpressions(Class<?> type, String expression);

    public boolean checkStringFilters(String expression);

    public boolean checkBooleanFilters(String expression);

    public boolean checkNumericFilters(String expression);

    public boolean checkDateFilters(String expression);
}
