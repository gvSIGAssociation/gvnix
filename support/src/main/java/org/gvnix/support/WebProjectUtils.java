/*
 * gvNIX. Spring Roo based RAD tool for Conselleria d'Infraestructures i
 * Transport - Generalitat Valenciana Copyright (C) 2010, 2011 CIT - Generalitat
 * Valenciana
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
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.gvnix.support;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.springframework.roo.metadata.MetadataService;
import org.springframework.roo.process.manager.FileManager;
import org.springframework.roo.project.LogicalPath;
import org.springframework.roo.project.Path;
import org.springframework.roo.project.PathResolver;
import org.springframework.roo.project.ProjectOperations;
import org.springframework.roo.support.util.XmlUtils;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * Utils for web projects.
 * 
 * @author Enrique Ruiz (eruiz at disid dot com) at <a
 *         href="http://www.disid.com">DiSiD Technologies S.L.</a> made for <a
 *         href="http://www.cit.gva.es">Conselleria d'Infraestructures i
 *         Transport</a>
 */
public class WebProjectUtils {

    /**
     * Check if current project is a Spring MVC one
     * <p/>
     * Search webmvc-config.xml file exists.
     * 
     * @param metadataService Metadata Service component
     * @param fileManager File manager component
     * @param projectOperations Project operations component
     * @return Is a Spring MVC project ?
     */
    public static boolean isSpringMvcProject(MetadataService metadataService,
            FileManager fileManager, ProjectOperations projectOperations) {

        PathResolver pathResolver = OperationUtils.getPathResolver(
                metadataService, projectOperations);
        String webXmlPath = pathResolver.getIdentifier(
                LogicalPath.getInstance(Path.SRC_MAIN_WEBAPP, ""),
                "WEB-INF/spring/webmvc-config.xml");

        return fileManager.exists(webXmlPath);
    }

    /**
     * Check if current project is a web project
     * <p/>
     * Search web.xml file exists.
     * 
     * @param metadataService Metadata Service component
     * @param fileManager File manager component
     * @param projectOperations Project operations component
     * @return Is a web project ?
     */
    public static boolean isWebProject(MetadataService metadataService,
            FileManager fileManager, ProjectOperations projectOperations) {

        PathResolver pathResolver = OperationUtils.getPathResolver(
                metadataService, projectOperations);
        String webXmlPath = pathResolver.getIdentifier(
                LogicalPath.getInstance(Path.SRC_MAIN_WEBAPP, ""),
                "WEB-INF/web.xml");

        return fileManager.exists(webXmlPath);
    }

    /**
     * Installs the Dialog Java class
     * 
     * @param packageFullName fullyQualifiedName of destination package for
     *            Dialog Bean. ie. <code>com.disid.myapp.web.dialog</code>
     * @param pathResolver
     * @param fileManager
     */
    public static void installWebDialogClass(String packageFullName,
            PathResolver pathResolver, FileManager fileManager) {

        String classFullName = pathResolver.getIdentifier(
                LogicalPath.getInstance(Path.SRC_MAIN_JAVA, ""),
                packageFullName.concat(".Dialog").replace(".", File.separator)
                        .concat(".java"));

        OperationUtils.installJavaClassFromTemplate(packageFullName,
                classFullName, "Dialog.java-template", pathResolver,
                fileManager);
    }

    // TODO: hasServlet method
    // public static boolean hasServlet(final Document webXml, final String
    // className) {
    // if (XmlUtils.findFirstElement(
    // "/web-app/servlet[servlet-class='".concat(className).concat("']"),
    // webXml.getElementRoot()) != null) {
    // return true;
    // }
    // }
}
