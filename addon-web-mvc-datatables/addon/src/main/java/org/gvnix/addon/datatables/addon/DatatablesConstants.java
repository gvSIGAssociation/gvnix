/*
 * gvNIX is an open source tool for rapid application development (RAD).
 * Copyright (C) 2010 Generalitat Valenciana
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
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.gvnix.addon.datatables.addon;

import static org.springframework.roo.model.JdkJavaType.ARRAY_LIST;
import static org.springframework.roo.model.JdkJavaType.HASH_SET;
import static org.springframework.roo.model.JdkJavaType.LIST;
import static org.springframework.roo.model.JdkJavaType.MAP;
import static org.springframework.roo.model.JdkJavaType.SET;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;

import org.springframework.roo.model.DataType;
import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.model.SpringJavaType;

/**
 * Constants used in classes
 * 
 * @author <a href="http://www.disid.com">DISID Corporation S.L.</a> made for <a
 *         href="http://www.dgti.gva.es">General Directorate for Information
 *         Technologies (DGTI)</a>
 */

public class DatatablesConstants {

    static final JavaType REQUEST_METHOD = new JavaType(
            "org.springframework.web.bind.annotation.RequestMethod");

    static final JavaType CONVERSION_SERVICE = new JavaType(
            "org.springframework.core.convert.ConversionService");

    static final JavaType MESSAGE_SOURCE = new JavaType(
            "org.springframework.context.MessageSource");

    static final JavaType AUTOWIRED = new JavaType(
            "org.springframework.beans.factory.annotation.Autowired");

    static final JavaType DATATABLES_PARAMS = new JavaType(
            "com.github.dandelion.datatables.extras.spring3.ajax.DatatablesParams");

    static final JavaType LOGGER_TYPE = new JavaType("java.util.logging.Logger");

    static final JavaType LOGGER_LEVEL = new JavaType("java.util.logging.Level");

    // Method and field generation constants
    static final JavaType MAP_STRING_STRING = new JavaType(
            MAP.getFullyQualifiedTypeName(), 0, DataType.TYPE, null,
            Arrays.asList(JavaType.STRING, JavaType.STRING));

    static final JavaType MAP_STRING_OBJECT = new JavaType(
            MAP.getFullyQualifiedTypeName(), 0, DataType.TYPE, null,
            Arrays.asList(JavaType.STRING, JavaType.OBJECT));

    static final JavaType HASHMAP = new JavaType(HashMap.class);
    static final JavaType HASHMAP_STRING_STRING = new JavaType(
            HASHMAP.getFullyQualifiedTypeName(), 0, DataType.TYPE, null,
            Arrays.asList(JavaType.STRING, JavaType.STRING));
    static final JavaType HASHMAP_STRING_OBJECT = new JavaType(
            HASHMAP.getFullyQualifiedTypeName(), 0, DataType.TYPE, null,
            Arrays.asList(JavaType.STRING, JavaType.OBJECT));
    /**
     * List<Map<String,String>>
     */
    static final JavaType LIST_MAP_STRING_STRING = new JavaType(
            LIST.getFullyQualifiedTypeName(), 0, DataType.TYPE, null,
            Arrays.asList(MAP_STRING_STRING));

    static final JavaType LIST_STRING = new JavaType(
            LIST.getFullyQualifiedTypeName(), 0, DataType.TYPE, null,
            Arrays.asList(JavaType.STRING));

    static final JavaType RENDER_FOR_DATATABLES_RETURN = LIST_MAP_STRING_STRING;

    /**
     * ArrayList<Map<String,String>>
     */
    static final JavaType ARRAYLIST_MAP_STRING_STRING = new JavaType(
            ARRAY_LIST.getFullyQualifiedTypeName(), 0, DataType.TYPE, null,
            Arrays.asList(MAP_STRING_STRING));

    static final JavaType ARRAYLIST_STRING = new JavaType(
            ARRAY_LIST.getFullyQualifiedTypeName(), 0, DataType.TYPE, null,
            Arrays.asList(JavaType.STRING));

    static final JavaType DATATABLES_COLUMNDEF = new JavaType(
            "com.github.dandelion.datatables.core.ajax.ColumnDef");

    static final JavaType DATATABLES_RESPONSE = new JavaType(
            "com.github.dandelion.datatables.core.ajax.DatatablesResponse");
    /**
     * DatatablesResponse<Map<String,String>>
     */
    static final JavaType FIND_ALL_RETURN = new JavaType(
            DATATABLES_RESPONSE.getFullyQualifiedTypeName(), 0, DataType.TYPE,
            null, Arrays.asList(MAP_STRING_STRING));

    static final JavaType CHECK_FILTERS_RETURN = new JavaType(
            SpringJavaType.RESPONSE_ENTITY.getFullyQualifiedTypeName(), 0,
            DataType.TYPE, null, Arrays.asList(JavaType.STRING));

    static final JavaType SET_STRING = new JavaType(
            SET.getFullyQualifiedTypeName(), 0, DataType.TYPE, null,
            Arrays.asList(JavaType.STRING));
    static final JavaType HASHSET_STRING = new JavaType(
            HASH_SET.getFullyQualifiedTypeName(), 0, DataType.TYPE, null,
            Arrays.asList(JavaType.STRING));

    static final JavaType COLLECTIONS = new JavaType(Collections.class);

    static final JavaType DATATABLES_UTILS_BEAN = new JavaType(
            "org.gvnix.web.datatables.util.DatatablesUtilsBean");

    static final JavaType SEARCH_RESULTS = new JavaType(
            "org.gvnix.web.datatables.query.SearchResults");

    static final JavaType STRING_UTILS = new JavaType(
            "org.apache.commons.lang3.StringUtils");

    static final JavaType DATE_FORMAT = new JavaType("java.text.DateFormat");
    static final JavaType SIMPLE_DATE_FORMAT = new JavaType(
            "java.text.SimpleDateFormat");

    static final JavaType REDIRECT_ATTRIBUTES = new JavaType(
            "org.springframework.web.servlet.mvc.support.RedirectAttributes");

    static final JavaType VALID = new JavaType("javax.validation.Valid");

    static final JavaSymbolName CRITERIA_PARAM_NAME = new JavaSymbolName(
            "criterias");
    static final JavaSymbolName ITEM_LIST_PARAM_NAME = new JavaSymbolName(
            "itemList");
    static final JavaType DATATABLES_CRITERIA_TYPE = new JavaType(
            "com.github.dandelion.datatables.core.ajax.DatatablesCriterias");
    static final JavaSymbolName RENDER_FOR_DATATABLES = new JavaSymbolName(
            "renderForDatatables");
    static final JavaSymbolName LIST_DATATABLES = new JavaSymbolName(
            "listDatatables");
    static final JavaSymbolName GET_COLUMN_TYPE = new JavaSymbolName(
            "getColumnType");
    static final JavaSymbolName GET_I18N_TEXT = new JavaSymbolName(
            "geti18nText");
    static final JavaSymbolName LIST_ROO = new JavaSymbolName("list");
    static final JavaSymbolName UI_MODEL = new JavaSymbolName("uiModel");
    static final JavaSymbolName REDIRECT_MODEL = new JavaSymbolName(
            "redirectModel");
    static final JavaSymbolName POPULATE_DATATABLES_CONFIG = new JavaSymbolName(
            "populateDatatablesConfig");
    static final JavaSymbolName POPULATE_PARAMETERS_MAP = new JavaSymbolName(
            "populateParametersMap");
    static final JavaSymbolName GET_PROPERTY_MAP = new JavaSymbolName(
            "getPropertyMap");
    static final JavaSymbolName POPULATE_ITEM_FOR_RENDER = new JavaSymbolName(
            "populateItemForRender");
    static final JavaSymbolName CHECK_FILTER_EXPRESSIONS = new JavaSymbolName(
            "checkFilterExpressions");

    static final JavaType DATA_SET = new JavaType(
            "com.github.dandelion.datatables.core.ajax.DataSet");

    static final JavaType DATA_SET_MAP_STRING_STRING = new JavaType(
            DATA_SET.getFullyQualifiedTypeName(), 0, DataType.TYPE, null,
            Arrays.asList(MAP_STRING_STRING));

    static final JavaType HTTP_SERVLET_REQUEST = new JavaType(
            "javax.servlet.http.HttpServletRequest");
    static final JavaType HTTP_SERVLET_RESPONSE = new JavaType(
            "javax.servlet.http.HttpServletResponse");
    static final JavaType HTTP_SERVLET_RESPONSE_WRAPPER = new JavaType(
            "javax.servlet.http.HttpServletResponseWrapper");
    static final JavaType REQUEST_DISPATCHER = new JavaType(
            "javax.servlet.RequestDispatcher");
    static final JavaType SERVLET_EXCEPTION = new JavaType(
            "javax.servlet.ServletException");

    static final JavaType WEB_REQUEST = new JavaType(
            "org.springframework.web.context.request.WebRequest");

    static final JavaType EXTENDED_MODEL_MAP = new JavaType(
            "org.springframework.ui.ExtendedModelMap");

    static final JavaType BEAN_PROPERTY_BINDING_RESULT = new JavaType(
            "org.springframework.validation.BeanPropertyBindingResult");

    static final JavaSymbolName REQUEST_PARAM_NAME = new JavaSymbolName(
            "request");
    static final JavaSymbolName RESPONSE_PARAM_NAME = new JavaSymbolName(
            "response");

    static final JavaType STRING_WRITER = new JavaType(StringWriter.class);
    static final JavaType PRINT_WRITER = new JavaType(PrintWriter.class);
    static final JavaType IO_EXCEPTION = new JavaType(IOException.class);

    static final JavaType JODA_DATETIME_FORMAT = new JavaType(
            "org.joda.time.format.DateTimeFormat");
    static final JavaType LOCALE = new JavaType(Locale.class);

    static final JavaType QDSL_BOOLEAN_BUILDER = new JavaType(
            "com.mysema.query.BooleanBuilder");
    static final JavaType QDSL_PATH_BUILDER = new JavaType(
            "com.mysema.query.types.path.PathBuilder");
    static final JavaType QDSL_JPA_QUERY = new JavaType(
            "com.mysema.query.jpa.impl.JPAQuery");

    static final JavaType ENUMERAITON = new JavaType(Enumeration.class);
    static final JavaType ENUMERATION_STRING = new JavaType(
            ENUMERAITON.getFullyQualifiedTypeName(), 0, DataType.TYPE, null,
            Arrays.asList(MAP_STRING_STRING));

    static final JavaType COLLECTION_UTILS = new JavaType(
            "org.apache.commons.collections.CollectionUtils");

    static final JavaType BEAN_WRAPPER = new JavaType(
            "org.springframework.beans.BeanWrapper");
    static final JavaType BEAN_WRAPPER_IMP = new JavaType(
            "org.springframework.beans.BeanWrapperImpl");

    static final String URN_TAG_DATATABLES = "urn:jsptagdir:/WEB-INF/tags/datatables";

    static final String DTTBL_ANN_DET_FIELDS_ATTR = "detailFields";

    static final String LIST_DTTBLS_DET_MTHD_NAME = "listDatatablesDetail";

    static final String REQUEST_PARAMETER_NAME = "request";

    static final String RQST_MAP_ANN_LIST = "/list";

    static final String RQST_MAP_ANN_VAL_NAME = "value";

    static final String RQST_MAP_ANN_VAL_HTML = "text/html";

    static final String RQST_MAP_ANN_PROD_NAME = "produces";

    static final JavaSymbolName UPDATE_JSON_FORMS_METHOD = new JavaSymbolName(
            "updateJsonForms");
    static final JavaSymbolName CREATE_JSON_FORM_METHOD = new JavaSymbolName(
            "createJsonForm");

    static final JavaSymbolName RENDER_UPDATE_FORMS_METHOD = new JavaSymbolName(
            "renderUpdateForm");

    static final JavaSymbolName IDS_PARAM_NAME = new JavaSymbolName("ids");

    static final JavaType ARRAY_UTILS = new JavaType(
            "org.apache.commons.lang3.ArrayUtils");

    static final JavaType QUERYDSL_UTILS_BEAN = new JavaType(
            "org.gvnix.web.datatables.util.QuerydslUtilsBean");

    static final JavaType BINDING_RESULT = new JavaType(
            "org.springframework.validation.BindingResult");

    static final JavaType DATATABLES_EXPORT = new JavaType(
            "com.github.dandelion.datatables.core.export.DatatablesExport");
    static final JavaType DATATABLES_EXPORT_CONF = new JavaType(
            "com.github.dandelion.datatables.core.export.ExportConf");
    static final JavaType DATATABLES_EXPORT_UTILS = new JavaType(
            "com.github.dandelion.datatables.core.export.ExportUtils");
    static final JavaType DATATABLES_EXPORT_TYPE = new JavaType(
            "com.github.dandelion.datatables.core.export.ExportType");
    static final JavaType DATATABLES_CSV_EXPORT = new JavaType(
            "com.github.dandelion.datatables.core.export.CsvExport");
    static final JavaType DATATABLES_PDF_EXPORT = new JavaType(
            "com.github.dandelion.datatables.extras.export.itext.PdfExport");
    static final JavaType DATATABLES_XLS_EXPORT = new JavaType(
            "com.github.dandelion.datatables.extras.export.poi.XlsExport");
    static final JavaType DATATABLES_XLSX_EXPORT = new JavaType(
            "com.github.dandelion.datatables.extras.export.poi.XlsxExport");
    static final JavaType DATATABLES_XML_EXPORT = new JavaType(
            "com.github.dandelion.datatables.core.export.XmlExport");
    static final JavaType DATATABLES_EXPORT_EXCEPTION = new JavaType(
            "com.github.dandelion.datatables.core.exception.ExportException");
    static final JavaType DATATABLES_HTML_TABLE = new JavaType(
            "com.github.dandelion.datatables.core.html.HtmlTable");

    static final JavaSymbolName DATATABLES_EXPORT_NAME = new JavaSymbolName(
            "datatablesExport");
    static final JavaSymbolName DATATABLES_EXPORT_TYPE_NAME = new JavaSymbolName(
            "exportType");
}
