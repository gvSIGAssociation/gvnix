package org.gvnix.service.roo.addon.addon;

import java.util.List;

import org.springframework.roo.classpath.details.annotations.AnnotationAttributeValue;
import org.springframework.roo.model.JavaType;

/**
 * Utilities to manage annotations.
 * 
 * @author <a href="http://www.disid.com">DISID Corporation S.L.</a> made for
 *         <a href="http://www.dgti.gva.es">General Directorate for Information Technologies (DGTI)</a>
 */
public interface AnnotationsService {

    /**
     * Add repository and dependency with this addon.
     * <p>
     * Repository and dependency with this addon required in project because
     * annotations from this addon are used along project.
     * </p>
     * <p>
     * Dependency will be updated if version is newer.
     * </p>
     */
    public void addAddonDependency();

    /**
     * Add an annotation to a JavaType with some attributes.
     * <p>
     * If annotation already assined on class, message will be raised.
     * </p>
     * 
     * @param serviceClass Java type to add de annotation
     * @param annotation Annotation class full name, null if not
     * @param annotationAttributeValues Attribute list for the annotation
     * @param forceUpdate overrides annotation value if is true.
     */
    public void addJavaTypeAnnotation(JavaType serviceClass, String annotation,
            List<AnnotationAttributeValue<?>> annotationAttributeValues,
            boolean forceUpdate);

}
