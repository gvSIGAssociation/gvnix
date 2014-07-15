package org.gvnix.addon.field.geo;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.springframework.roo.project.ProjectOperations;

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

    @Override
    public boolean isFieldCommandAvailable() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void addField() {
        // TODO Auto-generated method stub

    }

}