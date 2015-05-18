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
package org.gvnix.web.json;

import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * JSON response container to send action result to browser in JSON format.
 * <p/>
 * It contains errors (binding, validation, etc), status code and the list of
 * bean objects processed. Spring MVC provides the facility {@link ResponseBody}
 * that converts this object to JSON output.
 * <p/>
 * Note that bindingResult contains the errors is a Map of Map to be able to
 * identify which errors belongs to each object. When elements are array or
 * list, Map key will be the index on <em>value</em> list or object property.
 * 
 * JSON example (when <em>T</em> is a List):
 * 
 * <pre>
 *   {
 *     OBJECT_INDEX : { FIELD1_NAME : FIELD_ERROR_MSG, FIELD2_NAME : FIELD_ERROR_MSG, ...},
 *     OBJECT_INDEX2 : { FIELD1_NAME : FIELD_ERROR_MSG,
 *         FIELD_OBJECT_NAME : { SUBOBJECT_FIELD: FIELD_ERROR_MSG, ... }
 *         FIELD_LIST_NAME: {
 *              OBJECT_FIELD_ITEM_INDEX : {ITEM_LIST_FIELD: FIELD_ERROR_MSG, ... },
 *              OBJECT_FIELD_ITEM_INDEX2 : {ITEM_LIST_FIELD: FIELD_ERROR_MSG, ... },
 *         },
 *         ...
 *     },
 *     ...
 *   }
 * </pre>
 * 
 * JSON example (when <em>T</em> is a Object):
 * 
 * <pre>
 * { FIELD1_NAME : FIELD_ERROR_MSG,
 *      FIELD_OBJECT_NAME : { SUBOBJECT_FIELD: FIELD_ERROR_MSG, ... }
 *      FIELD_LIST_NAME: {
 *              OBJECT_FIELD_ITEM_INDEX : {ITEM_LIST_FIELD: FIELD_ERROR_MSG, ... },
 *              OBJECT_FIELD_ITEM_INDEX2 : {ITEM_LIST_FIELD: FIELD_ERROR_MSG, ... },
 *      },
 *      ...
 * }
 * 
 * </pre>
 * 
 * @author <a href="http://www.disid.com">DISID Corporation S.L.</a> made for
 *         <a href="http://www.dgti.gva.es">General Directorate for Information Technologies (DGTI)</a>
 * @since TODO: Class version
 * @see BindingResultSerializer
 */
public class JsonResponse<T> {

    /**
     * Operation result
     */
    private String status;

    /**
     * Global exception message
     */
    private String exceptionMessage;

    /**
     * Binding and validation problems
     */
    private BindingResult bindingResult;

    /**
     * Request values result
     */
    private T value;

    /**
     * OID of {@link #value}
     */
    private Object oid;

    /**
     * @return request status result
     */
    public String getStatus() {
        return status;
    }

    /**
     * Sets request status result
     * 
     * @param status
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * @return exception message
     */
    public String getExceptionMessage() {
        return exceptionMessage;
    }

    /**
     * Sets exception message
     * 
     * @param cause
     */
    public void setExceptionMessage(String cause) {
        this.exceptionMessage = cause;
    }

    /**
     * @return request result
     */
    public T getValue() {
        return value;
    }

    /**
     * Sets request result
     * 
     * @param value
     */
    public void setValue(T value) {
        this.value = value;
    }

    /**
     * @return binding and validation errors messages
     */
    public BindingResult getBindingResult() {
        return bindingResult;
    }

    /**
     * Sets binding and validation errors messages
     * 
     * @param bindingResult
     */
    public void setBindingResult(BindingResult bindingResult) {
        this.bindingResult = bindingResult;
    }

    public Object getOid() {
        return oid;
    }

    public void setOid(Object oid) {
        this.oid = oid;
    }
}