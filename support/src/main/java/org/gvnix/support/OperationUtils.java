/*
 * gvNIX. Spring Roo based RAD tool for Conselleria d'Infraestructures
 * i Transport - Generalitat Valenciana
 * Copyright (C) 2010, 2011 CIT - Generalitat Valenciana
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
package org.gvnix.support;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.springframework.roo.metadata.MetadataService;
import org.springframework.roo.process.manager.FileManager;
import org.springframework.roo.process.manager.MutableFile;
import org.springframework.roo.project.Path;
import org.springframework.roo.project.PathResolver;
import org.springframework.roo.project.ProjectMetadata;
import org.springframework.roo.support.util.FileCopyUtils;
import org.springframework.roo.support.util.StringUtils;
import org.springframework.roo.support.util.TemplateUtils;

/**
 * Utils for Command classes.
 * 
 * @author Mario Martínez (mmartinez at disid dot com) at <a
 *         href="http://www.disid.com">DiSiD Technologies S.L.</a> made for <a
 *         href="http://www.cit.gva.es">Conselleria d'Infraestructures i
 *         Transport</a>
 */
public class OperationUtils {

    /**
     * Check if there is a project available.
     * 
     * @param metadataService
     *            Metadata Service component
     * @return Is project available ?
     */
    public static boolean isProjectAvailable(MetadataService metadataService) {
        return getPathResolver(metadataService) != null;
    }

    /**
     * Get the path resolver or null if there is no project available.
     * 
     * @param metadataService
     *            Metadata Service component
     * @return Path resolver or null
     */
    public static PathResolver getPathResolver(MetadataService metadataService) {

        ProjectMetadata projectMetadata = (ProjectMetadata) metadataService
                .get(ProjectMetadata.getProjectIdentifier());
        if (projectMetadata == null) {

            return null;
        }

        return projectMetadata.getPathResolver();
    }

    /**
     * Check if current project is a Spring MVC one
     * 
     * @param metadataService
     *            Metadata Service component
     * @param fileManager
     *            FileManager component
     * @return
     */
    public static boolean isSpringMvcProject(MetadataService metadataService,
            FileManager fileManager) {
        PathResolver pathResolver = getPathResolver(metadataService);
        String webXmlPath = pathResolver.getIdentifier(Path.SRC_MAIN_WEBAPP,
                "WEB-INF/spring/webmvc-config.xml");

        if (!fileManager.exists(webXmlPath)) {
            return false;
        }
        return true;
    }

    /**
     * Installs the Dialog Java class
     * 
     * @param packageFullName
     *            fullyQualifiedName of destination package for Dialog Bean. ie.
     *            <code>com.disid.myapp.web.dialog</code>
     * @param pathResolver
     * @param fileManager
     */
    public static void installWebDialogClass(String packageFullName,
            PathResolver pathResolver, FileManager fileManager) {

        String classFullName = pathResolver.getIdentifier(Path.SRC_MAIN_JAVA,
                packageFullName.concat(".Dialog").replace(".", File.separator)
                        .concat(".java"));

        installJavaClassFromTemplate(packageFullName, classFullName,
                "Dialog.java-template", pathResolver, fileManager);
    }

    /**
     * Installs in project a Java Class based on a template.
     * <p>
     * Note that class template must be a resource available in this module.
     * <p>
     * <strong>This method only performs a replacement of the pattern ${PACKAGE}
     * in the template</strong>
     * 
     * @param packageFullName
     * @param classFullName
     * @param javaClassTemplateName
     * @param pathResolver
     * @param fileManager
     */
    private static void installJavaClassFromTemplate(String packageFullName,
            String classFullName, String javaClassTemplateName,
            PathResolver pathResolver, FileManager fileManager) {

        MutableFile mutableClass = null;
        if (!fileManager.exists(classFullName)) {
            mutableClass = fileManager.createFile(classFullName);

            InputStream template = TemplateUtils.getTemplate(
                    OperationUtils.class, javaClassTemplateName);

            String javaTemplate;
            try {
                javaTemplate = FileCopyUtils
                        .copyToString(new InputStreamReader(template));

                // Replace package definition
                javaTemplate = StringUtils.replace(javaTemplate, "${PACKAGE}",
                        packageFullName);

                // Write final java file
                FileCopyUtils.copy(javaTemplate.getBytes(),
                        mutableClass.getOutputStream());
            } catch (IOException ioe) {
                throw new IllegalStateException("Unable load ".concat(
                        javaClassTemplateName).concat(", ioe"));
            } finally {
                try {
                    template.close();
                } catch (IOException e) {
                    throw new IllegalStateException("Error creating ".concat(
                            classFullName).concat(" in project"), e);
                }
            }
        }
    }
}
