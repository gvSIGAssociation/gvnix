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
package org.gvnix.addon.jpa.addon.audit;

import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.gvnix.addon.jpa.addon.audit.providers.RevisionLogRevisionEntityMetadataBuilder;
import org.gvnix.addon.jpa.addon.audit.providers.RevisionLogRevisionEntityMetadataBuilder.Context;
import org.gvnix.addon.jpa.annotations.audit.GvNIXJpaAuditRevisionEntity;
import org.gvnix.support.ItdBuilderHelper;
import org.springframework.roo.classpath.PhysicalTypeIdentifierNamingUtils;
import org.springframework.roo.classpath.PhysicalTypeMetadata;
import org.springframework.roo.classpath.itd.AbstractItdTypeDetailsProvidingMetadataItem;
import org.springframework.roo.metadata.MetadataIdentificationUtils;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.project.LogicalPath;

/**
 * ITD generator for {@link GvNIXJpaAuditRevisionEntity} annotation.
 * 
 * @author <a href="http://www.disid.com">DISID Corporation S.L.</a> made for <a
 *         href="http://www.dgti.gva.es">General Directorate for Information
 *         Technologies (DGTI)</a>
 * @since 1.3.0
 */
public class JpaAuditRevisionEntityMetadata extends
        AbstractItdTypeDetailsProvidingMetadataItem {

    // Constants
    private static final String PROVIDES_TYPE_STRING = JpaAuditRevisionEntityMetadata.class
            .getName();
    private static final String PROVIDES_TYPE = MetadataIdentificationUtils
            .create(PROVIDES_TYPE_STRING);

    public static final String getMetadataIdentiferType() {
        return PROVIDES_TYPE;
    }

    public static final String createIdentifier(JavaType javaType,
            LogicalPath path) {
        return PhysicalTypeIdentifierNamingUtils.createIdentifier(
                PROVIDES_TYPE_STRING, javaType, path);
    }

    public static final JavaType getJavaType(String metadataIdentificationString) {
        return PhysicalTypeIdentifierNamingUtils.getJavaType(
                PROVIDES_TYPE_STRING, metadataIdentificationString);
    }

    public static final LogicalPath getPath(String metadataIdentificationString) {
        return PhysicalTypeIdentifierNamingUtils.getPath(PROVIDES_TYPE_STRING,
                metadataIdentificationString);
    }

    public static boolean isValid(String metadataIdentificationString) {
        return PhysicalTypeIdentifierNamingUtils.isValid(PROVIDES_TYPE_STRING,
                metadataIdentificationString);
    }

    /**
     * Itd builder herlper
     */
    private final ItdBuilderHelper helper;

    private final JpaAuditRevisionEntityAnnotationValues annotationValues;
    private final RevisionLogRevisionEntityMetadataBuilder revisionLogBuilder;
    private final JavaType userType;
    private final JavaType userService;

    private final boolean userTypeIsEntity;
    private final boolean userTypeIsUserDetails;

    private final boolean usePattern;

    private final String dateTimepattern;

    private final String dateTimeStyle;

    private Context buildContext;

    public JpaAuditRevisionEntityMetadata(String identifier,
            JavaType aspectName,
            PhysicalTypeMetadata governorPhysicalTypeMetadata,
            JpaAuditRevisionEntityAnnotationValues annotationValues,
            RevisionLogRevisionEntityMetadataBuilder revisionLogBuilder,
            JavaType userType, JavaType userService, boolean userTypeIsEntity,
            boolean userTypeIsUserDetails, boolean usePattern,
            String dateTimepattern, String dateTimeStyle) {
        super(identifier, aspectName, governorPhysicalTypeMetadata);
        Validate.isTrue(isValid(identifier), "Metadata identification string '"
                + identifier + "' does not appear to be a valid");

        // Helper itd generation
        this.helper = new ItdBuilderHelper(this, governorPhysicalTypeMetadata,
                builder.getImportRegistrationResolver());

        this.annotationValues = annotationValues;

        this.revisionLogBuilder = revisionLogBuilder;

        this.userType = userType;

        this.userService = userService;

        this.userTypeIsEntity = userTypeIsEntity;

        this.userTypeIsUserDetails = userTypeIsUserDetails;

        this.usePattern = usePattern;
        this.dateTimepattern = dateTimepattern;
        this.dateTimeStyle = dateTimeStyle;

        this.buildContext = new BuildContext(getId(), helper,
                governorPhysicalTypeMetadata.getType(), this.userType,
                this.userService, this.userTypeIsEntity,
                this.userTypeIsUserDetails, this.usePattern,
                this.dateTimepattern, this.dateTimeStyle);

        this.revisionLogBuilder.initialize(builder, buildContext);

        this.revisionLogBuilder.fillEntity();

        this.revisionLogBuilder.done();

        // Create a representation of the desired output ITD
        itdTypeDetails = builder.build();
    }

    public String toString() {
        final ToStringBuilder builder = new ToStringBuilder(this);
        builder.append("identifier", getId());
        builder.append("valid", valid);
        builder.append("aspectName", aspectName);
        builder.append("destinationType", destination);
        builder.append("governor", governorPhysicalTypeMetadata.getId());
        builder.append("itdTypeDetails", itdTypeDetails);
        return builder.toString();
    }

    /**
     * Gets final names to use of a type in method body after import resolver.
     * 
     * @param type
     * @return name to use in method body
     */
    @SuppressWarnings("unused")
    private String getFinalTypeName(JavaType type) {
        return type.getNameIncludingTypeParameters(false,
                builder.getImportRegistrationResolver());
    }

    /**
     * @return annotation values of metadata
     */
    public JpaAuditRevisionEntityAnnotationValues getAnnotationValues() {
        return annotationValues;
    }

    /**
     * Class which contains generation time metadata information useful for
     * {@link RevisionLogRevisionEntityMetadataBuilder}
     * 
     * @author <a href="http://www.disid.com">DISID Corporation S.L.</a> made
     *         for <a href="http://www.dgti.gva.es">General Directorate for
     *         Information Technologies (DGTI)</a>
     */
    private class BuildContext implements Context {

        private final ItdBuilderHelper helper;

        private final String metadataId;

        private final JavaType entity;

        private final JavaType userType;

        private final JavaType userService;

        private final boolean userTypeIsEntity;

        private final boolean userTypeIsUserDetails;

        private final boolean usePattern;

        private final String dateTimepattern;

        private final String dateTimeStyle;

        public BuildContext(String metadataId, ItdBuilderHelper helper,
                JavaType entity, JavaType userType, JavaType userService,
                boolean userTypeIsEntity, boolean userTypeIsUserDetails,
                boolean usePattern, String dateTimepattern, String dateTimeStyle) {
            super();
            this.metadataId = metadataId;
            this.helper = helper;
            this.entity = entity;
            this.userType = userType;
            this.userService = userService;
            this.userTypeIsEntity = userTypeIsEntity;
            this.userTypeIsUserDetails = userTypeIsUserDetails;
            this.usePattern = usePattern;
            this.dateTimepattern = dateTimepattern;
            this.dateTimeStyle = dateTimeStyle;
        }

        /** {@inheritDoc} */
        @Override
        public ItdBuilderHelper getHelper() {
            return helper;
        }

        /** {@inheritDoc} */
        @Override
        public String getMetadataId() {
            return metadataId;
        }

        /** {@inheritDoc} */
        @Override
        public JavaType getEntity() {
            return entity;
        }

        /** {@inheritDoc} */
        @Override
        public JavaType getUserType() {
            return userType;
        }

        /** {@inheritDoc} */
        @Override
        public JavaType getUserService() {
            return userService;
        }

        /** {@inheritDoc} */
        @Override
        public boolean getUserTypeIsEntity() {
            return userTypeIsEntity;
        }

        /** {@inheritDoc} */
        @Override
        public boolean getUserTypeIsUserDetails() {
            return userTypeIsUserDetails;
        }

        /** {@inheritDoc} */
        @Override
        public boolean usePatternForTimestamp() {
            return usePattern;
        }

        /** {@inheritDoc} */
        @Override
        public String getPatternForTimestamp() {
            return dateTimepattern;
        }

        /** {@inheritDoc} */
        @Override
        public String getTimestampStyle() {
            return dateTimeStyle;
        }
    }

    /**
     * @return revision entity JavaType
     */
    public JavaType getType() {
        return governorPhysicalTypeMetadata.getType();
    }

    /**
     * @return current RevisionLog builder
     */
    public RevisionLogRevisionEntityMetadataBuilder getRevisionLogBuilder() {
        return revisionLogBuilder;
    }
}
