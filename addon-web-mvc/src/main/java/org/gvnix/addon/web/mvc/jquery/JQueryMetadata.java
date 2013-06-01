/*
 * gvNIX. Spring Roo based RAD tool for Generalitat Valenciana     
 * Copyright (C) 2013 Generalitat Valenciana
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
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/copyleft/gpl.html>.
 */
package org.gvnix.addon.web.mvc.jquery;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.springframework.roo.addon.web.mvc.controller.scaffold.RooWebScaffold;
import org.springframework.roo.addon.web.mvc.controller.scaffold.WebScaffoldAnnotationValues;
import org.springframework.roo.classpath.PhysicalTypeIdentifierNamingUtils;
import org.springframework.roo.classpath.PhysicalTypeMetadata;
import org.springframework.roo.classpath.details.ItdTypeDetails;
import org.springframework.roo.classpath.itd.AbstractItdMetadataProvider;
import org.springframework.roo.classpath.itd.AbstractItdTypeDetailsProvidingMetadataItem;
import org.springframework.roo.metadata.MetadataIdentificationUtils;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.project.LogicalPath;

/**
 * Meta-data related to {@link GvNIXWebJQuery}.
 * <p/>
 * Meta-data to create/manage the ITD type details. Note
 * {@link AbstractItdMetadataProvider#get(String)} is which will create the
 * physical {@code .aj} file using the info this meta-data provides via {
 * {@link #getMemberHoldingTypeDetails()}.
 * <p/>
 * Note this meta-data item neither contains nor composes details of the related
 * Java type because it doesn't create any ITD.
 * 
 * @author gvNIX Team
 * @since 1.1.0
 */
public class JQueryMetadata extends AbstractItdTypeDetailsProvidingMetadataItem {

    private static final String PROVIDES_TYPE_STRING = JQueryMetadata.class
            .getName();
    private static final String PROVIDES_TYPE = MetadataIdentificationUtils
            .create(PROVIDES_TYPE_STRING);

    private final WebScaffoldAnnotationValues webScaffoldAnnotationValues;

    /**
     * Creates meta-data instance by delegating in
     * {@link AbstractItdTypeDetailsProvidingMetadataItem}
     * <p/>
     * This constructor creates an empty {@link ItdTypeDetails} that causes Roo
     * doesn't generate any ITD file.
     * 
     * @param identifier
     * @param aspectName
     * @param governorPhysicalTypeMetadata
     */
    public JQueryMetadata(String identifier, JavaType aspectName,
            PhysicalTypeMetadata governorPhysicalTypeMetadata,
            WebScaffoldAnnotationValues webScaffoldAnnotationValues) {

        super(identifier, aspectName, governorPhysicalTypeMetadata);

        this.webScaffoldAnnotationValues = webScaffoldAnnotationValues;

        // Create a representation of the desired output ITD
        itdTypeDetails = builder.build();
    }

    /**
     * @return {@link RooWebScaffold} annotation values
     */
    public WebScaffoldAnnotationValues getWebScaffoldAnnotationValues() {
        return webScaffoldAnnotationValues;
    }

    @Override
    public String toString() {
        ToStringBuilder tsc = new ToStringBuilder(this);
        tsc.append("identifier", getId());
        tsc.append("valid", valid);
        return tsc.toString();
    }

    public static final String getMetadataIdentiferType() {
        return PROVIDES_TYPE;
    }

    /**
     * Creates the meta-data ID for this class name.
     * 
     * @param javaType the fully-qualified name of the user project type to
     *        which the meta-data relates
     * @param path the path to this type within the project
     * @return
     */
    public static final String createIdentifier(JavaType javaType,
            LogicalPath path) {
        return PhysicalTypeIdentifierNamingUtils.createIdentifier(
                PROVIDES_TYPE_STRING, javaType, path);
    }

    /**
     * Gets the user project type with which the given meta-data ID is
     * associated.
     * 
     * @param metadataIdentificationString the ID of the meta-data instance
     * @return
     */
    public static final JavaType getJavaType(String metadataIdentificationString) {
        return PhysicalTypeIdentifierNamingUtils.getJavaType(
                PROVIDES_TYPE_STRING, metadataIdentificationString);
    }

    /**
     * Parses the user project path from the given meta-data ID.
     * 
     * @param metadataIdentificationString the ID of the meta-data instance
     * @return
     */
    public static final LogicalPath getPath(String metadataIdentificationString) {
        return PhysicalTypeIdentifierNamingUtils.getPath(PROVIDES_TYPE_STRING,
                metadataIdentificationString);
    }

    /**
     * True if the given meta-data id appears to identify an instance this
     * meta-data class
     * 
     * @param metadataIdentificationString the ID to evaluate
     * @return
     */
    public static boolean isValid(String metadataIdentificationString) {
        return PhysicalTypeIdentifierNamingUtils.isValid(PROVIDES_TYPE_STRING,
                metadataIdentificationString);
    }

}
