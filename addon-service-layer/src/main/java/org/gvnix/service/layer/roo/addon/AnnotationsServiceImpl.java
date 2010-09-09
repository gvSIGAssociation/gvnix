package org.gvnix.service.layer.roo.addon;

import java.util.List;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.springframework.roo.project.Dependency;
import org.springframework.roo.project.ProjectOperations;
import org.springframework.roo.project.Property;
import org.springframework.roo.support.util.XmlUtils;
import org.w3c.dom.Element;

@Component(immediate = true)
@Service
public class AnnotationsServiceImpl implements AnnotationsService {
    
    @Reference
    private ProjectOperations projectOperations;
    
    /**
     * {@inheritDoc}
     */
    public void addGvNIXAnnotationsDependency() {

	List<Element> projectProperties = XmlUtils.findElements(
		"/configuration/gvnix/properties/*", XmlUtils.getConfiguration(
			this.getClass(), "properties.xml"));
	for (Element property : projectProperties) {
	    projectOperations.addProperty(new Property(property));
	}

	List<Element> databaseDependencies = XmlUtils.findElements(
		"/configuration/gvnix/dependencies/dependency", XmlUtils
			.getConfiguration(this.getClass(),
				"gvnix-annotation-dependencies.xml"));
	for (Element dependencyElement : databaseDependencies) {
	    projectOperations
		    .dependencyUpdate(new Dependency(dependencyElement));
	}
    }
    
}
