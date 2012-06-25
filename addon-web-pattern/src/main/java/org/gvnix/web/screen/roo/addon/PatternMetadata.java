/*
 * gvNIX. Spring Roo based RAD tool for Conselleria d'Infraestructures
 * i Transport - Generalitat Valenciana
 * Copyright (C) 2010, 2011 CIT - Generalitat Valenciana
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.gvnix.web.screen.roo.addon;

import java.util.List;
import java.util.Map;
import java.util.SortedMap;

import org.springframework.roo.addon.propfiles.PropFileOperations;
import org.springframework.roo.addon.web.mvc.controller.details.DateTimeFormatDetails;
import org.springframework.roo.addon.web.mvc.controller.details.JavaTypeMetadataDetails;
import org.springframework.roo.addon.web.mvc.controller.scaffold.WebScaffoldAnnotationValues;
import org.springframework.roo.addon.web.mvc.controller.scaffold.mvc.WebScaffoldMetadata;
import org.springframework.roo.classpath.PhysicalTypeIdentifierNamingUtils;
import org.springframework.roo.classpath.PhysicalTypeMetadata;
import org.springframework.roo.classpath.details.FieldMetadata;
import org.springframework.roo.classpath.details.ItdTypeDetailsBuilder;
import org.springframework.roo.classpath.details.MethodMetadata;
import org.springframework.roo.classpath.details.annotations.StringAttributeValue;
import org.springframework.roo.classpath.itd.AbstractItdTypeDetailsProvidingMetadataItem;
import org.springframework.roo.metadata.MetadataIdentificationUtils;
import org.springframework.roo.metadata.MetadataService;
import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.process.manager.FileManager;
import org.springframework.roo.project.Path;
import org.springframework.roo.project.PathResolver;
import org.springframework.roo.support.util.Assert;

/**
 * This type produces metadata for a new ITD. It uses an
 * {@link ItdTypeDetailsBuilder} provided by
 * {@link AbstractItdTypeDetailsProvidingMetadataItem} to register a field in
 * the ITD and a new method.
 * 
 * @author Óscar Rovira (orovira at disid dot com) at <a
 *         href="http://www.disid.com">DiSiD Technologies S.L.</a> made for <a
 *         href="http://www.cit.gva.es">Conselleria d'Infraestructures i
 *         Transport</a>
 * @since 0.8
 */
public class PatternMetadata extends AbstractPatternMetadata {

    private static final String PROVIDES_TYPE_STRING = PatternMetadata.class.getName();
    private static final String PROVIDES_TYPE = MetadataIdentificationUtils.create(PROVIDES_TYPE_STRING);

    public PatternMetadata(String mid, JavaType aspect, PhysicalTypeMetadata controller, WebScaffoldMetadata webScaffoldMetadata,
    		WebScaffoldAnnotationValues webScaffoldValues, List<StringAttributeValue> patterns, List<MethodMetadata> controllerMethods, 
            List<FieldMetadata> controllerFields, SortedMap<JavaType, JavaTypeMetadataDetails> entitiesDetails,
            SortedMap<JavaType, JavaTypeMetadataDetails> typesForPopulate, Map<JavaType, Map<JavaSymbolName, DateTimeFormatDetails>> relationsDateTypes, 
            MetadataService metadataService, PropFileOperations propFileOperations, PathResolver pathResolver, FileManager fileManager,
            Map<JavaSymbolName, DateTimeFormatDetails> dateTypes) {
    	
        super(mid, aspect, controller, webScaffoldMetadata, webScaffoldValues, patterns,
                controllerMethods, controllerFields, entitiesDetails, typesForPopulate,
                relationsDateTypes, metadataService, pathResolver, fileManager, dateTypes);
        
        Assert.isTrue(isValid(mid), "Metadata identification string '" + mid + "' does not appear to be a valid");
    }

    // Typically, no changes are required beyond this point

    public static final String getMetadataIdentiferType() {
    	
        return PROVIDES_TYPE;
    }

    public static final String createIdentifier(JavaType controller, Path path) {
    	
        return PhysicalTypeIdentifierNamingUtils.createIdentifier(PROVIDES_TYPE_STRING, controller, path);
    }

    public static final JavaType getJavaType(String mid) {
    	
        return PhysicalTypeIdentifierNamingUtils.getJavaType(PROVIDES_TYPE_STRING, mid);
    }

    public static final Path getPath(String mid) {
    	
        return PhysicalTypeIdentifierNamingUtils.getPath(PROVIDES_TYPE_STRING, mid);
    }

    public static boolean isValid(String mid) {
    	
        return PhysicalTypeIdentifierNamingUtils.isValid(PROVIDES_TYPE_STRING, mid);
    }
    
}
