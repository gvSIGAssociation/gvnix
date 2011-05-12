/*
 * gvNIX. Spring Roo based RAD tool for Conselleria d'Infraestructures
 * i Transport - Generalitat Valenciana
 * Copyright (C) 2010 CIT - Generalitat Valenciana
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

import java.lang.reflect.Modifier;
import java.util.*;

import org.springframework.roo.addon.entity.EntityMetadata;
import org.springframework.roo.classpath.PhysicalTypeIdentifierNamingUtils;
import org.springframework.roo.classpath.PhysicalTypeMetadata;
import org.springframework.roo.classpath.details.*;
import org.springframework.roo.classpath.details.annotations.*;
import org.springframework.roo.classpath.itd.AbstractItdTypeDetailsProvidingMetadataItem;
import org.springframework.roo.classpath.itd.InvocableMemberBodyBuilder;
import org.springframework.roo.metadata.MetadataIdentificationUtils;
import org.springframework.roo.model.*;
import org.springframework.roo.project.Path;
import org.springframework.roo.support.util.Assert;
import org.springframework.roo.support.util.StringUtils;

/**
 * This type produces metadata for a new ITD. It uses an 
 * {@link ItdTypeDetailsBuilder} provided by 
 * {@link AbstractItdTypeDetailsProvidingMetadataItem} to register a field in 
 * the ITD and a new method.
 * 
 * @author Ricardo García Fernández at <a href="http://www.disid.com">DiSiD Technologies S.L.</a> made for <a href="http://www.cit.gva.es">Conselleria d'Infraestructures i Transport</a>
 * @author Enrique Ruiz (eruiz at disid dot com) at <a href="http://www.disid.com">DiSiD Technologies S.L.</a> made for <a href="http://www.cit.gva.es">Conselleria d'Infraestructures i Transport</a>
 */
public class ScreenMetadata extends AbstractItdTypeDetailsProvidingMetadataItem {

    private static final String PROVIDES_TYPE_STRING = ScreenMetadata.class.getName();
    private static final String PROVIDES_TYPE = MetadataIdentificationUtils.create(PROVIDES_TYPE_STRING);

    private static final JavaType ENTITY_MANAGER = new JavaType(
	    "javax.persistence.EntityManager");
    private static final JavaType QUERY = new JavaType(
	    "javax.persistence.Query");

    private EntityMetadata entityMetadata;

    public ScreenMetadata(String identifier, JavaType aspectName,
	    PhysicalTypeMetadata governorPhysicalTypeMetadata,
	    EntityMetadata entityMetadata) {
	super(identifier, aspectName, governorPhysicalTypeMetadata);

	Assert.isTrue(isValid(identifier), "Metadata identification string '"
		.concat(identifier).concat("' does not appear to be a valid"));

	if (!isValid()) {
	    return;
	}

	this.entityMetadata = entityMetadata;

	// Retrieve the fields that are defined as OneToMany relationship.
	List<FieldMetadata> fieldMetadataList = MemberFindingUtils
		.getFieldsWithAnnotation(governorTypeDetails,
			new JavaType("javax.persistence.OneToMany"));

	if (!fieldMetadataList.isEmpty()) {
	    // Generate the get paginated methods for each 1-n relationship
	    // entities.

	    String relationName;
	    StringBuilder methodName;
	    String entityName = null;

	    for (FieldMetadata fieldMetadata : fieldMetadataList) {

		methodName = new StringBuilder();
		methodName.append("get");
		
		relationName = fieldMetadata.getFieldName()
			.getReadableSymbolName();
		methodName.append(StringUtils.capitalize(relationName));
		
		methodName.append("RelatedEntries");

		// Retrieve relation type class name.
		JavaType relationSet = fieldMetadata.getFieldType();
		// ignoring java.util.Map field types (see ROO-194)
		if (relationSet.equals(new JavaType(Map.class.getName()))) {
		    continue;
		}
		if (relationSet.getFullyQualifiedTypeName().equals(
			Set.class.getName())) {
		    if (relationSet.getParameters().size() != 1) {
			throw new IllegalArgumentException(
				"A set is defined without specification of its type (via generics) - unable to create view for it");
		    }
		    relationSet = relationSet.getParameters().get(0);
		}

		// Retrieve Annotation 'mappedBy' attribute value name.
		StringAttributeValue mappedByAttributeValue;
		for (AnnotationMetadata annotation : fieldMetadata
			.getAnnotations()) {
		    if (annotation.getAnnotationType().compareTo(
			    new JavaType("javax.persistence.OneToMany")) == 0) {

			mappedByAttributeValue = ((StringAttributeValue) annotation
				.getAttribute(new JavaSymbolName("mappedBy")));

			Assert.isTrue(mappedByAttributeValue != null,
				"There must be defined 'mappedBy' attribute in @OneToMany field'"
					+ fieldMetadata.getFieldName()
						.getReadableSymbolName()
					+ "' in class '"
					+ governorTypeDetails.getName()
						.getFullyQualifiedTypeName()
					+ "'.");

			entityName = mappedByAttributeValue.getValue();
		    }
		}

		Assert.isTrue(entityName != null,
			"There must be defined 'mappedBy' attribute in @OneToMany field'"
				+ fieldMetadata.getFieldName()
					.getReadableSymbolName()
				+ "' in class '"
				+ governorTypeDetails.getName()
					.getFullyQualifiedTypeName() + "'.");

		MethodMetadata getEntityRelatedEntriesMethod = getEntityRelatedEntriesMethod(
			methodName.toString(), entityName, relationSet);

		builder.addMethod(getEntityRelatedEntriesMethod);

		// Create a representation of the desired output ITD
		itdTypeDetails = builder.build();

	    }
	}
    }

    /**
     * Retrieves the method to obtain the list of paginated relations.
     * 
     * @param methodName
     *            MethodName definition created using entity relationship name.
     * @param entityName
     *            Entity which has the relations.
     * @param enclosingRelation
     *            Relation JavaType to create the query and return type for the
     *            method.
     * @return
     */
    public MethodMetadata getEntityRelatedEntriesMethod(String methodName,
	    String entityName, JavaType enclosingRelation) {

	// Compute the relevant toString method name
	JavaSymbolName javaMethodName = new JavaSymbolName(methodName);

	// Parameters types declaration.
	List<JavaType> paramTypes = new ArrayList<JavaType>();
	paramTypes.add(JavaType.INT_OBJECT);
	paramTypes.add(JavaType.INT_OBJECT);

	// Param Names
	List<JavaSymbolName> paramNames = new ArrayList<JavaSymbolName>();
	paramNames.add(new JavaSymbolName("page"));
	paramNames.add(new JavaSymbolName("pageSize"));

	// See if the type itself declared the method
	MethodMetadata result = MemberFindingUtils.getDeclaredMethod(
		governorTypeDetails, javaMethodName, paramTypes);
	if (result != null) {
	    return result;
	}

	// Create the method using a InvocableMemberBodyBuilder.
	InvocableMemberBodyBuilder bodyBuilder = new InvocableMemberBodyBuilder();

	MethodMetadata entityManagerMethod = entityMetadata
		.getEntityManagerMethod();

	// StringBuilder
	StringBuilder stringBuilder = new StringBuilder();

	// Create EntityManager.
	stringBuilder.append(" em = "
		+ governorTypeDetails.getName().getSimpleTypeName() + "."
		+ entityManagerMethod.getMethodName().getSymbolName() + "();");

	bodyBuilder.appendFormalLine(ENTITY_MANAGER
		.getNameIncludingTypeParameters(false, builder
			.getImportRegistrationResolver())
		+ stringBuilder.toString());

	// Create query
	stringBuilder = new StringBuilder();
	stringBuilder.append(" q = em.createQuery(\"select "
		+ StringUtils.uncapitalize(enclosingRelation
			.getSimpleTypeName())
			+ " from "
		+ enclosingRelation.getSimpleTypeName()
			+ " "
		+ StringUtils.uncapitalize(enclosingRelation
			.getSimpleTypeName())
			+ " where "
		+ StringUtils.uncapitalize(enclosingRelation
			.getSimpleTypeName()) + "."
			+ StringUtils.uncapitalize(entityName) + " = :"
		+ StringUtils.uncapitalize(entityName) + "\");");

	// Create body resolving imports.
	bodyBuilder.appendFormalLine(QUERY.getNameIncludingTypeParameters(
		false, builder.getImportRegistrationResolver())
		+ stringBuilder.toString());

	// Add parameters to the query.
	stringBuilder = new StringBuilder();
	stringBuilder.append("q.setParameter(\""
		+ StringUtils.uncapitalize(entityName) + "\", this);");

	bodyBuilder.appendFormalLine(stringBuilder.toString());

	// Calculate result parameters to paginate.
	stringBuilder = new StringBuilder();
	stringBuilder
		.append("q.setFirstResult((page.intValue() - 1) * pageSize.intValue()).setMaxResults(pageSize.intValue());");

	bodyBuilder.appendFormalLine(stringBuilder.toString());

	// Return result
	stringBuilder = new StringBuilder();
	stringBuilder.append("return q.getResultList();");

	bodyBuilder.appendFormalLine(stringBuilder.toString());

	// Define returnType
	JavaType enclosingJavaType = new JavaType(enclosingRelation
		.getFullyQualifiedTypeName());
	List<JavaType> typeParams = new ArrayList<JavaType>();
	typeParams.add(enclosingJavaType);

	JavaType returnType = new JavaType("java.util.List", 0, DataType.TYPE,
		null, typeParams);
	
	// DiSiD: Roo uses now builder instead of constructor to create method metadata
//	// return new DefaultMethodMetadata
//	return new DefaultMethodMetadata(getId(), Modifier.PUBLIC, 
//		javaMethodName, returnType, AnnotatedJavaType.convertFromJavaTypes(paramTypes), paramNames, 
//		new ArrayList<AnnotationMetadata>(), null, bodyBuilder
//			.getOutput());
	MethodMetadataBuilder methodBuilder = new MethodMetadataBuilder(
		getId(), Modifier.PUBLIC, javaMethodName, returnType,
		AnnotatedJavaType.convertFromJavaTypes(paramTypes), paramNames,
		bodyBuilder);
	return methodBuilder.build(); // Build and return a MethodMetadata instance
	
    }

    public static final String getMetadataIdentiferType() {
	return PROVIDES_TYPE;
    }

    public static final String createIdentifier(JavaType javaType, Path path) {
	return PhysicalTypeIdentifierNamingUtils.createIdentifier(
		PROVIDES_TYPE_STRING, javaType, path);
    }

    public static final JavaType getJavaType(String metadataIdentificationString) {
	return PhysicalTypeIdentifierNamingUtils.getJavaType(
		PROVIDES_TYPE_STRING, metadataIdentificationString);
    }

    public static final Path getPath(String metadataIdentificationString) {
	return PhysicalTypeIdentifierNamingUtils.getPath(
		PROVIDES_TYPE_STRING, metadataIdentificationString);
    }

    public static boolean isValid(String metadataIdentificationString) {
	return PhysicalTypeIdentifierNamingUtils.isValid(
		PROVIDES_TYPE_STRING, metadataIdentificationString);
    }
}
