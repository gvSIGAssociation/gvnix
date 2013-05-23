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
package org.gvnix.addon.datatables;

import static org.springframework.roo.model.JdkJavaType.ARRAY_LIST;
import static org.springframework.roo.model.JdkJavaType.HASH_SET;
import static org.springframework.roo.model.JdkJavaType.LIST;
import static org.springframework.roo.model.JdkJavaType.MAP;
import static org.springframework.roo.model.JdkJavaType.SET;

import java.util.Arrays;
import java.util.HashMap;

import org.springframework.roo.model.DataType;
import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.model.JavaType;

/**
 * Constants used in clases
 * 
 * @author gvNIX Team
 */

public class DatatablesConstants {

    static final JavaType REQUEST_METHOD = new JavaType(
            "org.springframework.web.bind.annotation.RequestMethod");

    static final JavaType CONVERSION_SERVICE = new JavaType(
            "org.springframework.core.convert.ConversionService");

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

    static final JavaType HASHMAP = new JavaType(HashMap.class);
    static final JavaType HASHMAP_STRING_STRING = new JavaType(
            HASHMAP.getFullyQualifiedTypeName(), 0, DataType.TYPE, null,
            Arrays.asList(JavaType.STRING, JavaType.STRING));
    /**
     * List<Map<String,String>>
     */
    static final JavaType LIST_MAP_STRING_STRING = new JavaType(
            LIST.getFullyQualifiedTypeName(), 0, DataType.TYPE, null,
            Arrays.asList(MAP_STRING_STRING));
    static final JavaType RENDER_FOR_DATATABLES_RETURN = LIST_MAP_STRING_STRING;

    /**
     * ArrayList<Map<String,String>>
     */
    static final JavaType RENDER_FOR_DATATABLES_RETURN_IMP = new JavaType(
            ARRAY_LIST.getFullyQualifiedTypeName(), 0, DataType.TYPE, null,
            Arrays.asList(MAP_STRING_STRING));

    static final JavaType DATATABLES_COLUMNDEF = new JavaType(
            "com.github.dandelion.datatables.core.ajax.ColumnDef");

    static final JavaType DATATABLES_RESPONSE = new JavaType(
            "com.github.dandelion.datatables.core.ajax.DatatablesResponse");
    /**
     * DatatablesResponse<Map<String,String>>
     */
    static final JavaType GET_DATATABLES_DATA_RETURN = new JavaType(
            DATATABLES_RESPONSE.getFullyQualifiedTypeName(), 0, DataType.TYPE,
            null, Arrays.asList(MAP_STRING_STRING));

    static final JavaType SET_STRING = new JavaType(
            SET.getFullyQualifiedTypeName(), 0, DataType.TYPE, null,
            Arrays.asList(JavaType.STRING));
    static final JavaType HASHSET_STRING = new JavaType(
            HASH_SET.getFullyQualifiedTypeName(), 0, DataType.TYPE, null,
            Arrays.asList(JavaType.STRING));

    static final JavaType DATATABLES_UTILS = new JavaType(
            "org.gvnix.web.datatables.util.DatatablesUtils");

    static final JavaType SEARCH_RESULTS = new JavaType(
            "org.gvnix.web.datatables.query.SearchResults");

    static final JavaType STRING_UTILS = new JavaType(
            "org.apache.commons.lang3.StringUtils");

    static final JavaType DATE_FORMAT = new JavaType("java.text.DateFormat");
    static final JavaType SIMPLE_DATE_FORMAT = new JavaType(
            "java.text.SimpleDateFormat");

    static final JavaSymbolName CRITERIA_PARAM_NAME = new JavaSymbolName(
            "criterias");
    static final JavaSymbolName ITEM_LIST_PARAM_NAME = new JavaSymbolName(
            "itemList");
    static final JavaType DATATABLES_CRITERIA_TYPE = new JavaType(
            "com.github.dandelion.datatables.core.ajax.DatatablesCriterias");
    static final JavaSymbolName RENDER_FOR_DATATABLES = new JavaSymbolName(
            "renderForDatatables");
    static final JavaSymbolName GET_DATATABLES_DATA = new JavaSymbolName(
            "getDatatablesData");
    static final JavaSymbolName LIST_DATATABLES = new JavaSymbolName(
            "listDatatables");
    static final JavaSymbolName LIST_ROO = new JavaSymbolName("list");
    static final JavaSymbolName UI_MODEL = new JavaSymbolName("uiModel");
    static final JavaSymbolName POPULATE_DATATABLES_CONFIG = new JavaSymbolName(
            "populateDatatablesConfig");

    static final JavaType DATA_SET = new JavaType(
            "com.github.dandelion.datatables.core.ajax.DataSet");

    static final JavaType DATA_SET_MAP_STRING_STRING = new JavaType(
            DATA_SET.getFullyQualifiedTypeName(), 0, DataType.TYPE, null,
            Arrays.asList(MAP_STRING_STRING));

}
