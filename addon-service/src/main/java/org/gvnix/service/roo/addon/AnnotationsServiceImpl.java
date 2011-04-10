package org.gvnix.service.roo.addon;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.gvnix.service.roo.addon.AnnotationsService;
import org.gvnix.service.roo.addon.JavaParserService;
import org.springframework.roo.classpath.TypeLocationService;
import org.springframework.roo.classpath.details.ClassOrInterfaceTypeDetails;
import org.springframework.roo.classpath.details.MutableClassOrInterfaceTypeDetails;
import org.springframework.roo.classpath.details.annotations.AnnotationAttributeValue;
import org.springframework.roo.classpath.details.annotations.AnnotationMetadata;
import org.springframework.roo.classpath.details.annotations.AnnotationMetadataBuilder;
import org.springframework.roo.metadata.MetadataService;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.project.Dependency;
import org.springframework.roo.project.ProjectOperations;
import org.springframework.roo.project.Repository;
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
@Component
@Service
public class AnnotationsServiceImpl implements AnnotationsService {
    
    @Reference private ProjectOperations projectOperations;
    @Reference private JavaParserService javaParserService;
    @Reference private MetadataService metadataService;
    @Reference private TypeLocationService typeLocationService;

  private static Logger logger = Logger.getLogger(AnnotationsService.class
      .getName());

  /**
   * {@inheritDoc}
   */
  public void addGvNIXAnnotationsDependency() {

    // Install the add-on Google code repository and dependency needed to
    // get the annotations

    Element conf = XmlUtils.getConfiguration(this.getClass(),
        "configuration.xml");

    List<Element> repos = XmlUtils.findElements(
        "/configuration/gvnix/repositories/repository", conf);
    for (Element repo : repos) {

      projectOperations.addRepository(new Repository(repo));
    }

    List<Element> depens = XmlUtils.findElements(
        "/configuration/gvnix/dependencies/dependency", conf);
    for (Element depen : depens) {

      projectOperations.addDependency(new Dependency(depen));
    }
  }

  /**
   * {@inheritDoc}
   */
  public void addJavaTypeAnnotation(JavaType serviceClass,
                                    String annotation,
                                    List<AnnotationAttributeValue<?>> annotationAttributeValues,
                                    boolean forceUpdate) {

    // Load class or interface details.
    // If class not found an exception will be raised.
    ClassOrInterfaceTypeDetails typeDetails = typeLocationService
        .getClassOrInterface(serviceClass);

    // Check and get mutable instance
    Assert.isInstanceOf(MutableClassOrInterfaceTypeDetails.class, typeDetails,
        "Can't modify " + typeDetails.getName());
    MutableClassOrInterfaceTypeDetails mutableTypeDetails = (MutableClassOrInterfaceTypeDetails) typeDetails;

    // Check annotation defined.
    // The annotation can't be updated.
    if (javaParserService
        .isAnnotationIntroduced(annotation, mutableTypeDetails)) {

      if (forceUpdate) {
        logger.log(Level.INFO,
            "The annotation " + annotation + " is already defined in '"
                + serviceClass.getFullyQualifiedTypeName()
                + "' and will be updated.");

        mutableTypeDetails.removeTypeAnnotation(new JavaType(annotation));
      }
      else {
        logger.log(Level.FINE,
            "The annotation " + annotation + " is already defined in '"
                + serviceClass.getFullyQualifiedTypeName() + "'.");
        return;

      }
    }

    // Add annotation
    // If attributes are null, create an empty list to avoid
    // NullPointerException
    if (annotationAttributeValues == null) {

      annotationAttributeValues = new ArrayList<AnnotationAttributeValue<?>>();
    }

    // Define annotation.
    AnnotationMetadata defaultAnnotationMetadata = new AnnotationMetadataBuilder(
        new JavaType(annotation), annotationAttributeValues).build();

    // Adds annotation to the entity
    mutableTypeDetails.addTypeAnnotation(defaultAnnotationMetadata);

    // Delete from chache to update class values.
    metadataService.evict(typeDetails.getDeclaredByMetadataId());

  }

}
