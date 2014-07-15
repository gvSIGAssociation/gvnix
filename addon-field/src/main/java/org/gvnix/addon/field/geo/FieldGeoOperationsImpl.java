package org.gvnix.addon.field.geo;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.springframework.roo.classpath.TypeLocationService;
import org.springframework.roo.classpath.TypeManagementService;
import org.springframework.roo.classpath.details.ClassOrInterfaceTypeDetails;
import org.springframework.roo.classpath.details.FieldMetadataBuilder;
import org.springframework.roo.classpath.details.annotations.AnnotationMetadataBuilder;
import org.springframework.roo.classpath.operations.jsr303.FieldDetails;
import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.model.ReservedWords;
import org.springframework.roo.process.manager.FileManager;
import org.springframework.roo.project.Path;
import org.springframework.roo.project.PathResolver;
import org.springframework.roo.project.ProjectOperations;
import org.springframework.roo.support.util.FileUtils;

/**
 * Implementation of {@link FieldGeoOperations} interface.
 * 
 * @since 1.1.1
 */
@Component
@Service
public class FieldGeoOperationsImpl implements FieldGeoOperations {

	/**
	 * Get a reference to the ProjectOperations from the underlying OSGi
	 * container. Make sure you are referencing the Roo bundle which contains
	 * this service in your add-on pom.xml.
	 */
	@Reference
	private ProjectOperations projectOperations;

	@Reference
	private TypeLocationService typeLocationService;

	@Reference
	private TypeManagementService typeManagementService;

	@Reference
	private PathResolver pathResolver;

	@Reference
	private FileManager fileManager;

	private static final JavaType HIBERNATE_TYPE_ANNOTATION = new JavaType(
			"org.hibernate.annotations.Type");

	/**
	 * This method checks if field geo is available to execute
	 */
	@Override
	public boolean isFieldCommandAvailable() {
		return projectOperations.isFocusedProjectAvailable()
				&& projectOperations
						.isFeatureInstalledInFocusedModule("gvnix-geo-persistence");
	}

	/**
	 * This method adds new file to the specified Entity.
	 * 
	 * @param JavaSymbolName
	 * @param fieldGeoTypes
	 * @param entity
	 * 
	 */
	@Override
	public void addField(JavaSymbolName fieldName, FieldGeoTypes fieldGeoType,
			JavaType entity) {

		final ClassOrInterfaceTypeDetails cid = typeLocationService
				.getTypeDetails(entity);
		final String physicalTypeIdentifier = cid.getDeclaredByMetadataId();

		// Getting fieldType and fieldDetails
		JavaType fieldType = new JavaType(fieldGeoType.toString());
		FieldDetails fieldDetails = new FieldDetails(physicalTypeIdentifier,
				fieldType, fieldName);

		// Checking not reserved words on fieldName
		ReservedWords
				.verifyReservedWordsNotPresent(fieldDetails.getFieldName());

		// Adding Annotation @Type
		List<AnnotationMetadataBuilder> fieldAnnotations = new ArrayList<AnnotationMetadataBuilder>();
		AnnotationMetadataBuilder typeAnnotation = new AnnotationMetadataBuilder(
				HIBERNATE_TYPE_ANNOTATION);
		typeAnnotation.addStringAttribute("type",
				"org.hibernate.spatial.GeometryType");
		fieldAnnotations.add(typeAnnotation);
		fieldDetails.setAnnotations(fieldAnnotations);

		// Adding Modifier
		fieldDetails.setModifiers(Modifier.PRIVATE);

		final FieldMetadataBuilder fieldBuilder = new FieldMetadataBuilder(
				fieldDetails);

		// Adding field to entity
		typeManagementService.addField(fieldBuilder.build());

		// Adding field types.xml on resources Entity package to parse Hibernate
		// Geometry Types
		addTypesXmlFile(entity);

	}

	/**
	 * This method creates types.xml file into src/main/resources/*
	 * 
	 * TODO: Improve <!ENTITY declaration on DOCTYPE
	 * 
	 * @param entity
	 */
	private void addTypesXmlFile(JavaType entity) {
		// Getting current entity package
		String entityPackage = entity.getPackage()
				.getFullyQualifiedPackageName();
		String entityPackageFolder = entityPackage.replaceAll("[.]", "/");

		// Setting types.xml location using entity package
		final String typesXmlPath = pathResolver.getFocusedIdentifier(
				Path.SRC_MAIN_RESOURCES,
				String.format("/%s/types.xml", entityPackageFolder));

		InputStream inputStream = null;
		OutputStream outputStream = null;
		try {
			inputStream = FileUtils.getInputStream(getClass(), "types.xml");
			if (!fileManager.exists(typesXmlPath)) {
				outputStream = fileManager.createFile(typesXmlPath)
						.getOutputStream();
			}
			if (outputStream != null) {
				IOUtils.copy(inputStream, outputStream);
			}
		} catch (final IOException ioe) {
			throw new IllegalStateException(ioe);
		} finally {
			IOUtils.closeQuietly(inputStream);
			if (outputStream != null) {
				IOUtils.closeQuietly(outputStream);
			}

		}
		// Modifying created file
		if (outputStream != null) {
			PrintWriter writer = new PrintWriter(outputStream);
			writer.println(" <!DOCTYPE hibernate-mapping PUBLIC");
			writer.println("\"-//Hibernate/Hibernate Mapping DTD 3.0//EN\"");
			writer.println("\"http://hibernate.sourceforge.net/hibernate-mapping-3.0.dtd\" [");
			writer.println(String.format(
					"<!ENTITY types SYSTEM \"classpath://%s/types.xml\">",
					entityPackageFolder));
			writer.println("]>");
			writer.println("");
			writer.println(String.format("<hibernate-mapping package=\"%s\">",
					entityPackage));
			writer.println("<typedef name=\"geometry\" class=\"org.hibernate.spatial.GeometryType\"/>");
			writer.println("</hibernate-mapping>");
			writer.flush();
			writer.close();

		}
	}
}