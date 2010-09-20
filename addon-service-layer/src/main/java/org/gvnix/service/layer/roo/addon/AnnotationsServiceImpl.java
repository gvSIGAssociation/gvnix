package org.gvnix.service.layer.roo.addon;

import java.util.ArrayList;
import java.util.List;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.springframework.roo.classpath.details.ClassOrInterfaceTypeDetails;
import org.springframework.roo.classpath.details.MutableClassOrInterfaceTypeDetails;
import org.springframework.roo.classpath.details.annotations.AnnotationAttributeValue;
import org.springframework.roo.classpath.details.annotations.AnnotationMetadata;
import org.springframework.roo.classpath.details.annotations.DefaultAnnotationMetadata;
import org.springframework.roo.classpath.operations.ClasspathOperations;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.project.Dependency;
import org.springframework.roo.project.ProjectOperations;
import org.springframework.roo.project.Property;
import org.springframework.roo.support.util.Assert;
import org.springframework.roo.support.util.XmlUtils;
import org.w3c.dom.Element;

/**
 * Utilities to manage annotations.
 * 
 * @author Mario Martínez Sánchez ( mmartinez at disid dot com ) at <a
 *         href="http://www.disid.com">DiSiD Technologies S.L.</a> made for <a
 *         href="http://www.cit.gva.es">Conselleria d'Infraestructures i
 *         Transport</a>
 */
@Component(immediate = true)
@Service
public class AnnotationsServiceImpl implements AnnotationsService {
    
    @Reference
    private ProjectOperations projectOperations;
    @Reference
    private JavaParserService javaParserService;
    @Reference
    private ClasspathOperations classpathOperations;

    /**
     * {@inheritDoc}
     */
    public void addGvNIXAnnotationsDependency() {

	List<Element> databaseDependencies = XmlUtils.findElements(
		"/configuration/gvnix/dependencies/dependency", XmlUtils
			.getConfiguration(this.getClass(),
				"gvnix-annotation-dependencies.xml"));

	for (Element dependencyElement : databaseDependencies) {
	    
	    projectOperations
		    .dependencyUpdate(new Dependency(dependencyElement));
	}
    }

    /**
     * {@inheritDoc}
     */
    public void addJavaTypeAnnotation(JavaType serviceClass, String annotation,
	    List<AnnotationAttributeValue<?>> annotationAttributeValues) {

	// Load class or interface details.
	// If class not found an exception will be raised.
	ClassOrInterfaceTypeDetails typeDetails = classpathOperations
		.getClassOrInterface(serviceClass);

	// Check and get mutable instance
	Assert.isInstanceOf(MutableClassOrInterfaceTypeDetails.class,
		typeDetails, "Can't modify " + typeDetails.getName());
	MutableClassOrInterfaceTypeDetails mutableTypeDetails = (MutableClassOrInterfaceTypeDetails) typeDetails;

	List<? extends AnnotationMetadata> typeAnnotations = mutableTypeDetails
		.getTypeAnnotations();

	// Check annotation defined.
	// TODO: The annotation can't be updated yet.
	Assert.isTrue(!javaParserService.isAnnotationIntroduced(annotation,
		mutableTypeDetails), "The annotation " + annotation
		+ " can't be updated yet with service command.");

	// Add annotation
	for (AnnotationMetadata typeAnnotation : typeAnnotations) {

	    if (typeAnnotation.getAnnotationType().getFullyQualifiedTypeName()
		    .equals(annotation)) {

		mutableTypeDetails
			.removeTypeAnnotation(new JavaType(annotation));
	    }
	}

	// If no attributes, create an empty list to avoid null error
	if (annotationAttributeValues == null) {

	    annotationAttributeValues = new ArrayList<AnnotationAttributeValue<?>>();
	}

	// Define annotation.
	AnnotationMetadata defaultAnnotationMetadata = new DefaultAnnotationMetadata(
		new JavaType(annotation), annotationAttributeValues);

	// Adds GvNIXWebService to the entity
	mutableTypeDetails.addTypeAnnotation(defaultAnnotationMetadata);
    }

}
