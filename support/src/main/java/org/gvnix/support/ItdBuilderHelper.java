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
 * this program. If not, see &lt;http://www.gnu.org/copyleft/gpl.html&gt;.
 */
package org.gvnix.support;

import java.util.ArrayList;
import java.util.List;

import org.springframework.roo.classpath.details.annotations.ArrayAttributeValue;
import org.springframework.roo.classpath.details.annotations.StringAttributeValue;
import org.springframework.roo.classpath.itd.AbstractItdTypeDetailsProvidingMetadataItem;
import org.springframework.roo.model.ImportRegistrationResolver;
import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.model.JavaType;

/**
 * Helper which provides utilities for a ITD generator (Metadata)
 * 
 * @author gvNIX Team
 */
public class ItdBuilderHelper {

    protected final AbstractItdTypeDetailsProvidingMetadataItem metadata;
    protected final ImportRegistrationResolver importResolver;

    /**
     * Constructor
     * 
     * @param metadata
     * @param importResolver (usually to get use
     *            <code>builder.getImportRegistrationResolver()</code>)
     */
    public ItdBuilderHelper(
            AbstractItdTypeDetailsProvidingMetadataItem metadata,
            ImportRegistrationResolver importResolver) {
        this.metadata = metadata;
        this.importResolver = importResolver;
    }

    /**
     * Gets final names to use of a type in method body after import resolver.
     * 
     * @param type
     * @return name to use in method body
     */
    public String getFinalTypeName(JavaType type) {
        return type.getNameIncludingTypeParameters(false, importResolver);
    }

    /**
     * Create an annotation value from string array
     * 
     * @param name
     * @param stringValues
     * @return
     */
    public ArrayAttributeValue<StringAttributeValue> toAttributeValue(
            String name, Iterable<String> stringValues) {
        List<StringAttributeValue> stringAttributeValues = new ArrayList<StringAttributeValue>();
        JavaSymbolName ignored = new JavaSymbolName("ignored");

        for (String str : stringValues) {
            stringAttributeValues.add(new StringAttributeValue(ignored, str));
        }

        return new ArrayAttributeValue<StringAttributeValue>(
                new JavaSymbolName(name), stringAttributeValues);
    }

}
