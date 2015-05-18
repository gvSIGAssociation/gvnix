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

import org.apache.commons.lang3.StringUtils;
import org.gvnix.addon.datatables.annotations.GvNIXDatatables;
import org.springframework.roo.classpath.PhysicalTypeMetadata;
import org.springframework.roo.classpath.details.ClassOrInterfaceTypeDetails;
import org.springframework.roo.classpath.details.annotations.populator.AbstractAnnotationValues;
import org.springframework.roo.classpath.details.annotations.populator.AutoPopulate;
import org.springframework.roo.classpath.details.annotations.populator.AutoPopulationUtils;
import org.springframework.roo.model.JavaType;

/**
 * Represents a parsed {@link GvNIXDatatables} values
 * 
 * @author <a href="http://www.disid.com">DISID Corporation S.L.</a> made for
 *         <a href="http://www.dgti.gva.es">General Directorate for Information Technologies (DGTI)</a>
 */
public class DatatablesAnnotationValues extends AbstractAnnotationValues {

    public static final JavaType DATATABLES_ANNOTATION = new JavaType(
            GvNIXDatatables.class);

    @AutoPopulate
    private boolean ajax = true;
    @AutoPopulate
    private String mode = null;
    @AutoPopulate
    private String[] detailFields = {};
    @AutoPopulate
    private boolean inlineEditing = false;
    @AutoPopulate
    private String baseFilter;

    public DatatablesAnnotationValues(
            final ClassOrInterfaceTypeDetails governorPhysicalTypeDetails) {
        super(governorPhysicalTypeDetails, DATATABLES_ANNOTATION);
        AutoPopulationUtils.populate(this, annotationMetadata);
    }

    /**
     * Constructor
     * 
     * @param governorPhysicalTypeMetadata
     */
    public DatatablesAnnotationValues(
            final PhysicalTypeMetadata governorPhysicalTypeMetadata) {
        super(governorPhysicalTypeMetadata, DATATABLES_ANNOTATION);
        AutoPopulationUtils.populate(this, annotationMetadata);
    }

    /**
     * @return use AJAX data mode
     */
    public boolean isAjax() {
        return ajax;
    }

    /**
     * @return use standard visualization mode
     */
    public boolean isStandardMode() {
        return StringUtils.isBlank(mode);
    }

    /**
     * @return page to render inside table cell
     */
    public String getMode() {
        return mode;
    }

    public String[] getDetailFields() {
        return detailFields;
    }

    /**
     * @return if user could edit elements in-line (inside the table)
     */
    public boolean isInlineEditing() {
        return inlineEditing;
    }

    /**
     * 
     * @return base filter as a string
     */

    public String getBaseFilter() {
        return baseFilter;
    }
}
