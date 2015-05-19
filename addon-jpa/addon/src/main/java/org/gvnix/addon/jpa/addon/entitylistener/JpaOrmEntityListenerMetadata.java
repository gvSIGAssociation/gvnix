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
package org.gvnix.addon.jpa.addon.entitylistener;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.springframework.roo.classpath.PhysicalTypeIdentifierNamingUtils;
import org.springframework.roo.metadata.AbstractMetadataItem;
import org.springframework.roo.metadata.MetadataIdentificationUtils;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.project.LogicalPath;

/**
 * Metadata for support JPA entity listener registry of gvNIX add-ons.
 * <p/>
 * This is generated base on metadata providers register on
 * {@link JpaOrmEntityListenerRegistry}.
 * <p/>
 * Metadata of metadata-providers registered on registry must implement the
 * {@link JpaOrmEntityListener} interface
 * 
 * 
 * @author <a href="http://www.disid.com">DISID Corporation S.L.</a> made for <a
 *         href="http://www.dgti.gva.es">General Directorate for Information
 *         Technologies (DGTI)</a>
 * @since 1.1.0
 */
public class JpaOrmEntityListenerMetadata extends AbstractMetadataItem {

    private static final String METADATA_SOURCE_DELIMITER = "||";

    private static final String PROVIDES_TYPE_STRING = JpaOrmEntityListenerMetadata.class
            .getName();
    private static final String PROVIDES_TYPE = MetadataIdentificationUtils
            .create(PROVIDES_TYPE_STRING);

    /**
     * Referred metadata
     */
    private final JpaOrmEntityListener entityListener;

    public JpaOrmEntityListenerMetadata(String identifier,
            JpaOrmEntityListener entityListener) {
        super(getBaseId(identifier));
        Validate.isTrue(isValid(identifier), "Metadata identification string '"
                + identifier + "' does not appear to be a valid");
        Validate.notNull(entityListener, "JpaOrmEntityListener required");

        this.entityListener = entityListener;
    }

    @Override
    public String toString() {
        ToStringBuilder tsc = new ToStringBuilder(this);
        tsc.append("identifier", getId());
        tsc.append("valid", valid);
        tsc.append("pattern metadata id", entityListener.getId());
        return tsc.toString();
    }

    public static final String getMetadataIdentiferType() {
        return PROVIDES_TYPE;
    }

    /**
     * Creates a Metadata identifier which includes the listener class and
     * source (related) metadata.
     * 
     * @param javaType
     * @param path
     * @param sourceProviderId
     * @return
     */
    public static final String createIdentifier(JavaType javaType,
            LogicalPath path, String sourceProviderId) {
        return PhysicalTypeIdentifierNamingUtils
                .createIdentifier(PROVIDES_TYPE_STRING, javaType, path)
                .concat(METADATA_SOURCE_DELIMITER).concat(sourceProviderId);
    }

    /**
     * @param metadataIdentificationString
     * @return listener class
     */
    public static final JavaType getJavaType(String metadataIdentificationString) {
        return PhysicalTypeIdentifierNamingUtils.getJavaType(
                PROVIDES_TYPE_STRING, StringUtils
                        .substringBefore(metadataIdentificationString,
                                METADATA_SOURCE_DELIMITER));
    }

    /**
     * @param metadataIdentificationString
     * @return listener logical path
     */
    public static final LogicalPath getPath(String metadataIdentificationString) {
        return PhysicalTypeIdentifierNamingUtils.getPath(PROVIDES_TYPE_STRING,
                StringUtils.substringBefore(metadataIdentificationString,
                        METADATA_SOURCE_DELIMITER));
    }

    /**
     * @param metadataIdentificationString
     * @return if metadata is valid
     */
    public static boolean isValid(String metadataIdentificationString) {
        return PhysicalTypeIdentifierNamingUtils.isValid(PROVIDES_TYPE_STRING,
                StringUtils.substringBefore(metadataIdentificationString,
                        METADATA_SOURCE_DELIMITER))
                && StringUtils.contains(metadataIdentificationString,
                        METADATA_SOURCE_DELIMITER)
                && StringUtils.isNotBlank(StringUtils
                        .substringAfter(metadataIdentificationString,
                                METADATA_SOURCE_DELIMITER));
    }

    /**
     * @param metadataIdentificationString
     * @return gets base metadata id of this metadata (whitout source
     *         information)
     */
    public static final String getBaseId(String metadataIdentificationString) {
        return StringUtils.substringBefore(metadataIdentificationString,
                METADATA_SOURCE_DELIMITER);
    }

    /**
     * @param metadataIdentificationString
     * @return gets source metatada id
     */
    public static final String getSorceId(String metadataIdentificationString) {
        return StringUtils.substringAfter(metadataIdentificationString,
                METADATA_SOURCE_DELIMITER);
    }
}
