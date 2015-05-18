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
package org.gvnix.addon.web.mvc.addon.jquery;

import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.gvnix.addon.web.mvc.annotations.jquery.GvNIXWebJQuery;
import org.springframework.roo.classpath.PhysicalTypeIdentifierNamingUtils;
import org.springframework.roo.metadata.AbstractMetadataItem;
import org.springframework.roo.metadata.MetadataIdentificationUtils;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.project.LogicalPath;

/**
 * JQuery views meta-data.
 * <p/>
 * This meta-data is registered as downstream of {@link JQueryMetadata}. When
 * {@link GvNIXWebJQuery} is added to any Java Controller the
 * {@link JQueryMetadataProvider} receives the notification and propagates it to
 * {@link JQueryMetadata} downstreams, including this meta-data.
 * <p/>
 * When the provider of this meta-data (
 * {@link JQueryJspMetadataListener#get(String)} ) is notified it updates the
 * view related to the Controller in which {@link GvNIXWebJQuery} was added.
 * Note upstream token contains the identifier of the target Java type and the
 * identifier of applied meta-data.
 * <p/>
 * Note this meta-data item neither contains nor composes details of the related
 * Java type because it doesn't create any ITD.
 * 
 * @author <a href="http://www.disid.com">DISID Corporation S.L.</a> made for
 *         <a href="http://www.dgti.gva.es">General Directorate for Information Technologies (DGTI)</a>
 * @since 1.1.0
 */
public class JQueryJspMetadata extends AbstractMetadataItem {

    private static final String PROVIDES_TYPE_STRING = JQueryJspMetadata.class
            .getName();
    private static final String PROVIDES_TYPE = MetadataIdentificationUtils
            .create(PROVIDES_TYPE_STRING);

    private final JQueryMetadata jqueryMetadata;

    public JQueryJspMetadata(String identifier, JQueryMetadata jqueryMetadata) {
        super(identifier);
        Validate.isTrue(isValid(identifier), "Metadata identification string '"
                .concat(identifier).concat("' does not appear to be a valid"));
        this.jqueryMetadata = jqueryMetadata;
    }

    @Override
    public String toString() {
        ToStringBuilder tsc = new ToStringBuilder(this);
        tsc.append("identifier", getId());
        tsc.append("valid", valid);
        tsc.append("JQueryMetadata ID",
                jqueryMetadata != null ? jqueryMetadata.getId() : "");
        return tsc.toString();
    }

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

}
