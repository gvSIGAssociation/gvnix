package org.gvnix.addon.field.geo;

import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.model.JavaType;

/**
 * Interface of commands that are available via the Roo shell.
 * 
 * @since 1.1.1
 */
public interface FieldGeoOperations {

    /**
     * 
     * @return
     */
    boolean isFieldCommandAvailable();

    /**
     * @param entity
     * @param fieldType
     * @param name
     * 
     */
    void addField(JavaSymbolName fieldName, FieldGeoTypes fieldGeoType,
            JavaType entity);

}