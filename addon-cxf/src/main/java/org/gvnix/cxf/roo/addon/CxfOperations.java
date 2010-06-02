package org.gvnix.cxf.roo.addon;

import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.project.Path;

public interface CxfOperations {

    public abstract boolean isProjectAvailable();

    /**
     * Checks if CXF support is installed in this project (by looking for
     * cxf-servlet.xml file)
     *
     * @return
     */
    boolean isCxfInstalled();

    /**
	 *
	 */
    void setupCxf();

    /**
     * @param ifaceName
     * @param path
     */
    void newService(JavaType ifaceName, Path path);


    /**
     * @param opeName
     * @param returnType
     * @param paramNames
     * @param paramTypes
     * @param ifaceType
     */
    void addServiceOperation(JavaSymbolName opeName, JavaType returnType,
	    String paramNames, String paramTypes, JavaType ifaceType);

}