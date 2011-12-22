package org.gvnix.weblayer.roo.addon;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.osgi.service.component.ComponentContext;
import org.springframework.roo.addon.entity.EntityMetadata;
import org.springframework.roo.classpath.PhysicalTypeCategory;
import org.springframework.roo.classpath.PhysicalTypeDetails;
import org.springframework.roo.classpath.PhysicalTypeIdentifier;
import org.springframework.roo.classpath.PhysicalTypeIdentifierNamingUtils;
import org.springframework.roo.classpath.PhysicalTypeMetadata;
import org.springframework.roo.classpath.details.BeanInfoUtils;
import org.springframework.roo.classpath.details.ClassOrInterfaceTypeDetails;
import org.springframework.roo.classpath.details.FieldMetadata;
import org.springframework.roo.classpath.details.MemberFindingUtils;
import org.springframework.roo.classpath.details.MethodMetadata;
import org.springframework.roo.classpath.scanner.MemberDetails;
import org.springframework.roo.classpath.scanner.MemberDetailsScanner;
import org.springframework.roo.file.monitor.event.FileDetails;
import org.springframework.roo.metadata.MetadataIdentificationUtils;
import org.springframework.roo.metadata.MetadataService;
import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.process.manager.FileManager;
import org.springframework.roo.process.manager.MutableFile;
import org.springframework.roo.project.Path;
import org.springframework.roo.project.PathResolver;
import org.springframework.roo.project.ProjectMetadata;
import org.springframework.roo.support.osgi.UrlFindingUtils;
import org.springframework.roo.support.util.Assert;
import org.springframework.roo.support.util.FileCopyUtils;
import org.springframework.roo.support.util.TemplateUtils;
import org.springframework.roo.support.util.XmlUtils;
import org.w3c.dom.Document;

/**
 * Utility methods used by the Vaadin Roo addon.
 */
public abstract class GvNIXRooUtils {

	/**
	 * Copy the contents of a directory to another, not replacing existing files
	 * in the target directory.
	 *
	 * @param fileManager
	 *            a non-null {@link FileManager} instance to use to modify the
	 *            file system
	 * @param context
	 *            a non-null {@link ComponentContext}
	 * @param sourcePath
	 *            directory
	 * @param targetDirectory
	 *            directory
	 */
	public static void copyDirectoryContents(FileManager fileManager,
			ComponentContext context, String sourcePath, String targetDirectory) {
		Assert.notNull(fileManager, "File manager required");
		Assert.notNull(context, "Component context required");
		Assert.hasText(sourcePath, "Source path required");
		Assert.hasText(targetDirectory, "Target directory required");

		if (!targetDirectory.endsWith("/")) {
			targetDirectory += "/";
		}

		if (!fileManager.exists(targetDirectory)) {
			fileManager.createDirectory(targetDirectory);
		}

		String path = TemplateUtils.getTemplatePath(GvNIXRooUtils.class,
				sourcePath);

		Set<URL> urls = UrlFindingUtils.findMatchingClasspathResources(
				context.getBundleContext(), path + "/*");
		for (URL url : urls) {
			String fileName = url.getPath().substring(
					url.getPath().lastIndexOf("/") + 1);
			if (!fileManager.exists(targetDirectory + fileName)) {
				try {
					FileCopyUtils.copy(url.openStream(), fileManager
							.createFile(targetDirectory + fileName)
							.getOutputStream());
				} catch (IOException e) {
					throw new IllegalStateException(
							"Could not copy resources for Vaadin", e);
				}
			}
		}
	}

	public static void installFromTemplateIfNeeded(FileManager fileManager,
			String destinationFile, String templateBaseName,
			Map<String, String> substitutions) {
		if (!fileManager.exists(destinationFile)) {
			InputStream templateInputStream = TemplateUtils.getTemplate(
					GvNIXRooUtils.class, templateBaseName + "-template");
			try {
				// Read template and insert the user's package
				String input = FileCopyUtils
						.copyToString(new InputStreamReader(templateInputStream));
				for (Map.Entry<String, String> entry : substitutions.entrySet()) {
					input = input.replace(entry.getKey(), entry.getValue());
				}

				// Output the file for the user
				MutableFile mutableFile = fileManager
						.createFile(destinationFile);
				FileCopyUtils.copy(input.getBytes(),
						mutableFile.getOutputStream());
			} catch (IOException ioe) {
				throw new IllegalStateException("Unable to create '"
						+ destinationFile + "'", ioe);
			}
		}
	}

	/**
	 * Writes an XML document to a file on the disk if it does not have
	 * identical content.
	 *
	 * @param fileManager
	 *            a non-null {@link FileManager} instance to use to modify the
	 *            file system
	 * @param fileName
	 * @param proposed
	 *            XML document to write
	 * @return true if the disk was changed (file was created or updated)
	 */
	public static boolean writeXmlToDiskIfNecessary(FileManager fileManager,
			String fileName, Document proposed) {
		Assert.notNull(fileManager, "File manager required");
		Assert.hasText(fileName, "File name required");

		// Build a string representation of the XML file
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		XmlUtils.writeXml(XmlUtils.createIndentingTransformer(),
				byteArrayOutputStream, proposed);
		String xmlContent = byteArrayOutputStream.toString();

		// If mutableFile becomes non-null, it means we need to use it to write
		// out the contents of xmlContent to the file
		MutableFile mutableFile = null;
		if (fileManager.exists(fileName)) {
			// First verify if the file has even changed
			File f = new File(fileName);
			String existing = null;
			try {
				existing = FileCopyUtils.copyToString(new FileReader(f));
			} catch (IOException ignoreAndJustOverwriteIt) {
			}

			if (!xmlContent.equals(existing)) {
				mutableFile = fileManager.updateFile(fileName);
			}

		} else {
			mutableFile = fileManager.createFile(fileName);
			Assert.notNull(mutableFile, "Could not create XML file '"
					+ fileName + "'");
		}

		try {
			if (mutableFile != null) {
				// We need to write the file out (it's a new file, or the
				// existing file has different contents)
				FileCopyUtils.copy(xmlContent, new OutputStreamWriter(
						mutableFile.getOutputStream()));
				// Return and indicate we wrote out the file
				return true;
			}
		} catch (IOException ioe) {
			throw new IllegalStateException("Could not output '"
					+ mutableFile.getCanonicalPath() + "'", ioe);
		}

		// A file existed, but it contained the same content, so we return false
		return false;
	}

	/**
	 * Checks if a field exists (directly) in a type.
	 *
	 * @param fieldName
	 * @param governorTypeDetails
	 * @return field found or null if none
	 */
	public static FieldMetadata fieldExists(JavaSymbolName fieldName,
			ClassOrInterfaceTypeDetails governorTypeDetails) {
		// We have no access to method parameter information, so we scan by name
		// alone and treat any match as authoritative
		// We do not scan the superclass, as the caller is expected to know
		// we'll only scan the current class
		for (FieldMetadata field : governorTypeDetails.getDeclaredFields()) {
			if (field.getFieldName().equals(fieldName)) {
				// Found a field with the expected name
				return field;
			}
		}
		return null;
	}

	/**
	 * Checks if a method exists (directly) in a type. Used to avoid duplicate
	 * method generation when creating ITD.
	 *
	 * @param methodName
	 * @param governorTypeDetails
	 * @return method found or null if none
	 */
	public static MethodMetadata methodExists(JavaSymbolName methodName,
			ClassOrInterfaceTypeDetails governorTypeDetails) {
		// We have no access to method parameter information, so we scan by name
		// alone and treat any match as authoritative
		// We do not scan the superclass, as the caller is expected to know
		// we'll only scan the current class
		for (MethodMetadata method : governorTypeDetails.getDeclaredMethods()) {
			if (method.getMethodName().equals(methodName)) {
				// Found a method of the expected name; we won't check method
				// parameters though
				return method;
			}
		}
		return null;
	}
	
	/**
	 * Gets the member details for a class.
	 * 
	 * @param type
	 * @param metadataService
	 * @param memberDetailsScanner
	 * @param requestingClass class name requesting the accessors, usually this.getClass().getName()
	 * @return
	 */
	public static MemberDetails getMemberDetails(JavaType type,
			MetadataService metadataService,
			MemberDetailsScanner memberDetailsScanner, String requestingClass) {
		PhysicalTypeMetadata physicalTypeMetadata = (PhysicalTypeMetadata) metadataService
				.get(PhysicalTypeIdentifier.createIdentifier(type,
						Path.SRC_MAIN_JAVA));
		Assert.notNull(physicalTypeMetadata,
				"The type " + type.getFullyQualifiedTypeName()
						+ " does not exist or can not be found.");
		PhysicalTypeDetails ptd = physicalTypeMetadata
				.getMemberHoldingTypeDetails();
		Assert.isInstanceOf(ClassOrInterfaceTypeDetails.class, ptd);
		ClassOrInterfaceTypeDetails classOrInterfaceTypeDetails = (ClassOrInterfaceTypeDetails) ptd;

		return memberDetailsScanner.getMemberDetails(requestingClass,
				classOrInterfaceTypeDetails);
	}

	/**
	 * Returns the properties of a type and the corresponding accessors (public parameterless get/is methods), excluding accessors for version and id properties of an entity.
	 * 
	 * @param type
	 * @param entityMetadata entity metadata or null to not skip id and version accessors
	 * @param memberDetails
	 * @param withMutatorOnly only return accessors that have a mutator
	 * @return
	 */
	public static Map<JavaSymbolName, MethodMetadata> getAccessors(
			JavaType type, EntityMetadata entityMetadata,
			MemberDetails memberDetails, boolean withMutatorOnly) {
		// normal properties of the type, excluding ID and version properties
		Map<JavaSymbolName, MethodMetadata> properties = new LinkedHashMap<JavaSymbolName, MethodMetadata>();
		for (MethodMetadata method : MemberFindingUtils.getMethods(memberDetails)) {
			if (BeanInfoUtils.isAccessorMethod(method)
					&& (entityMetadata == null || !GvNIXRooUtils.isIdOrVersionAccessor(
							entityMetadata, method))) {
				JavaSymbolName propertyName = BeanInfoUtils
						.getPropertyNameForJavaBeanMethod(method);
				if (null != propertyName) {
					FieldMetadata field = BeanInfoUtils.getFieldForPropertyName(
							memberDetails, propertyName);
					if (field != null
							&& (!withMutatorOnly || BeanInfoUtils
									.hasAccessorAndMutator(field, memberDetails))) {
						properties.put(propertyName, method);
					}
				}
			}
		}
		return properties;
	}

	public static Map<JavaSymbolName, JavaType> getSpecialDomainTypes(
			MetadataService metadataService, JavaType javaType,
			MemberDetails memberDetails, boolean includeEmbedded) {
		Map<JavaSymbolName, JavaType> specialTypes = new LinkedHashMap<JavaSymbolName, JavaType>();
		
		EntityMetadata em = (EntityMetadata) metadataService.get(EntityMetadata
				.createIdentifier(javaType, Path.SRC_MAIN_JAVA));
		if (em == null) {
			// unable to get entity metadata so it is not a Entity in our
			// project anyway.
			return specialTypes;
		}

		Map<JavaSymbolName, MethodMetadata> accessors = GvNIXRooUtils
				.getAccessors(javaType, em, memberDetails, true);

		for (JavaSymbolName propertyName : accessors.keySet()) {
			FieldMetadata fieldMetadata = BeanInfoUtils
					.getFieldForPropertyName(memberDetails, propertyName);

			JavaType type = accessors.get(propertyName).getReturnType();

			if (type.isCommonCollectionType()) {
				for (JavaType genericType : type.getParameters()) {
					if (isDomainTypeInProject(metadataService, genericType)) {
						specialTypes.put(propertyName, genericType);
					}
				}
			} else {
				if (isDomainTypeInProject(metadataService, type)
						&& (includeEmbedded || !isEmbeddedFieldType(fieldMetadata))) {
					specialTypes.put(propertyName, type);
				}
			}
		}
		return specialTypes;
	}

	public static boolean isIdOrVersionAccessor(EntityMetadata entityMetadata,
			MethodMetadata accessor) {
		return accessor.getMethodName().equals(
				entityMetadata.getIdentifierAccessor().getMethodName())
				|| (entityMetadata.getVersionAccessor() != null && accessor
						.getMethodName().equals(
								entityMetadata.getVersionAccessor()
										.getMethodName()));
	}

	public static boolean isEnumType(MetadataService metadataService,
			JavaType type) {
		PhysicalTypeMetadata physicalTypeMetadata = (PhysicalTypeMetadata) metadataService
				.get(PhysicalTypeIdentifierNamingUtils.createIdentifier(
						PhysicalTypeIdentifier.class.getName(), type,
						Path.SRC_MAIN_JAVA));
		if (physicalTypeMetadata != null) {
			PhysicalTypeDetails details = physicalTypeMetadata
					.getMemberHoldingTypeDetails();
			if (details != null) {
				if (details.getPhysicalTypeCategory().equals(
						PhysicalTypeCategory.ENUMERATION)) {
					return true;
				}
			}
		}
		return false;
	}

	public static boolean isDomainTypeInProject(MetadataService metadataService,
			JavaType javaType) {
		// we are only interested if the type is part of our application
		if (metadataService.get(PhysicalTypeIdentifier.createIdentifier(
				javaType, Path.SRC_MAIN_JAVA)) != null) {
			return true;
		}
		return false;
	}

	public static boolean isEmbeddedFieldType(FieldMetadata field) {
		return MemberFindingUtils.getAnnotationOfType(field.getAnnotations(),
				new JavaType("javax.persistence.Embedded")) != null;
	}

	/**
	 * Finds metadata IDs for all types in the project providing a given
	 * metadata type.
	 *
	 * @param metadataService
	 * @param fileManager
	 * @param providesType
	 *            metadata type for which to scan
	 * @return list of MID strings representing the Java types
	 */
	public static List<String> scanPotentialTypeMids(
			MetadataService metadataService, FileManager fileManager,
			String providesType) {
		// Scan for every .java file in the main source branch of the project
		// and create a MID of type providesType for each
		List<String> allMids = new ArrayList<String>();
		ProjectMetadata projectMetadata = (ProjectMetadata) metadataService
				.get(ProjectMetadata.getProjectIdentifier());
		PathResolver pathResolver = projectMetadata.getPathResolver();

		Path path = Path.SRC_MAIN_JAVA;
		FileDetails srcRoot = new FileDetails(new File(
				pathResolver.getRoot(path)), null);
		String antPath = pathResolver.getRoot(path) + File.separatorChar + "**"
				+ File.separatorChar + "*.java";

		for (FileDetails fileDetails : fileManager.findMatchingAntPath(antPath)) {
			String fullPath = srcRoot.getRelativeSegment(fileDetails
					.getCanonicalPath());
			// ditch the first / and .java
			fullPath = fullPath.substring(1, fullPath.lastIndexOf(".java"))
					.replace(File.separatorChar, '.');
			JavaType javaType;
			try {
				javaType = new JavaType(fullPath);
			} catch (RuntimeException loopToNextFile) { // ROO-1022
				continue;
			}
			String mid = PhysicalTypeIdentifierNamingUtils.createIdentifier(
					providesType, javaType, path);
			allMids.add(mid);
		}

		return allMids;
	}

	public static boolean isNotificationForJavaType(String mid) {
		return MetadataIdentificationUtils.getMetadataClass(mid).equals(
				MetadataIdentificationUtils
						.getMetadataClass(PhysicalTypeIdentifier
								.getMetadataIdentiferType()));
	}

}
