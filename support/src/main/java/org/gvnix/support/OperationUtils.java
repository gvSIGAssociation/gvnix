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
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.util.Collection;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.osgi.service.component.ComponentContext;
import org.springframework.roo.metadata.MetadataService;
import org.springframework.roo.process.manager.FileManager;
import org.springframework.roo.process.manager.MutableFile;
import org.springframework.roo.project.LogicalPath;
import org.springframework.roo.project.Path;
import org.springframework.roo.project.PathResolver;
import org.springframework.roo.project.ProjectMetadata;
import org.springframework.roo.project.ProjectOperations;
import org.springframework.roo.support.osgi.OSGiUtils;
import org.springframework.roo.support.util.FileUtils;

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
     * @param metadataService Metadata Service component
     * @return Is project available ?
     */
    public static boolean isProjectAvailable(MetadataService metadataService,
            ProjectOperations projectOperations) {
        return getPathResolver(metadataService, projectOperations) != null;
    }

    /**
     * Get the path resolver or null if there is no project available.
     * 
     * @param metadataService Metadata Service component
     * @param projectOperations Project operations component
     * @return Path resolver or null
     */
    public static PathResolver getPathResolver(MetadataService metadataService,
            ProjectOperations projectOperations) {

        ProjectMetadata projectMetadata = (ProjectMetadata) metadataService
                .get(ProjectMetadata.getProjectIdentifier(projectOperations
                        .getFocusedModuleName()));
        if (projectMetadata == null) {

            return null;
        }

        return projectOperations.getPathResolver();
    }

    /**
     * Check if current project is a Spring MVC one
     * <p>
     * Search webmvc-config.xml file exists.
     * </p>
     * 
     * @param metadataService Metadata Service component
     * @param fileManager File manager component
     * @param projectOperations Project operations component
     * @return Is a Spring MVC project ?
     */
    public static boolean isSpringMvcProject(MetadataService metadataService,
            FileManager fileManager, ProjectOperations projectOperations) {

        PathResolver pathResolver = getPathResolver(metadataService,
                projectOperations);
        String webXmlPath = pathResolver.getIdentifier(
                LogicalPath.getInstance(Path.SRC_MAIN_WEBAPP, ""),
                "WEB-INF/spring/webmvc-config.xml");

        return fileManager.exists(webXmlPath);
    }

    /**
     * Check if current project is a web project
     * <p>
     * Search web.xml file exists.
     * </p>
     * 
     * @param metadataService Metadata Service component
     * @param fileManager File manager component
     * @param projectOperations Project operations component
     * @return Is a web project ?
     */
    public static boolean isWebProject(MetadataService metadataService,
            FileManager fileManager, ProjectOperations projectOperations) {

        PathResolver pathResolver = getPathResolver(metadataService,
                projectOperations);
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
            InputStream template = FileUtils.getInputStream(
                    OperationUtils.class, javaClassTemplateName);

            String javaTemplate;
            try {
                javaTemplate = IOUtils
                        .toString(new InputStreamReader(template));

                // Replace package definition
                javaTemplate = StringUtils.replace(javaTemplate, "${PACKAGE}",
                        packageFullName);

                // Write final java file
                mutableClass = fileManager.createFile(classFullName);

                OutputStream outputStream = null;
                InputStream inputStream = null;
                try {
                    outputStream = mutableClass.getOutputStream();
                    inputStream = IOUtils.toInputStream(javaTemplate);
                    IOUtils.copy(inputStream, outputStream);
                }
                finally {
                    IOUtils.closeQuietly(outputStream);
                    IOUtils.closeQuietly(inputStream);
                }

            }
            catch (IOException ioe) {
                throw new IllegalStateException("Unable load ".concat(
                        javaClassTemplateName).concat(", ioe"));
            }
            finally {
                try {
                    template.close();
                }
                catch (IOException e) {
                    throw new IllegalStateException("Error creating ".concat(
                            classFullName).concat(" in project"), e);
                }
            }
        }
    }

    /**
     * Updates files in source path into target directory path. <strong>Useful
     * for copy/update binary resources (images) from Addon bundle resources to
     * destination directory</strong>. For text resources (tagx, jspx, ...) use
     * <code>AbstractOperations.copyDirectoryContents(..)</code> instead
     * 
     * @param sourceAntPath the source path
     * @param targetDirectory the target directory
     * @param fileManager
     * @param context
     * @param clazz which owns the resources in source path
     * @see org.springframework.roo.classpath.operations.AbstractOperations.
     *      copyDirectoryContents(String, String, boolean)
     */
    public static void updateDirectoryContents(String sourceAntPath,
            String targetDirectory, FileManager fileManager,
            ComponentContext context, Class<?> clazz) {
        StringUtils.isNotBlank(sourceAntPath);
        StringUtils.isNotBlank(targetDirectory);

        if (!targetDirectory.endsWith("/")) {
            targetDirectory += "/";
        }

        if (!fileManager.exists(targetDirectory)) {
            fileManager.createDirectory(targetDirectory);
        }

        String path = FileUtils.getPath(clazz, sourceAntPath);
        Collection<URL> urls = OSGiUtils.findEntriesByPattern(
                context.getBundleContext(), path);
        Validate.notNull(urls,
                "Could not search bundles for resources for Ant Path '" + path
                        + "'");
        for (URL url : urls) {
            String fileName = url.getPath().substring(
                    url.getPath().lastIndexOf("/") + 1);
            try {
                if (!fileManager.exists(targetDirectory + fileName)) {

                    OutputStream outputStream = null;
                    try {
                        outputStream = fileManager.createFile(
                                targetDirectory + fileName).getOutputStream();
                        IOUtils.copy(url.openStream(), outputStream);
                    }
                    finally {
                        IOUtils.closeQuietly(outputStream);
                    }
                }
                else {

                    OutputStream outputStream = null;
                    try {
                        outputStream = fileManager.updateFile(
                                targetDirectory + fileName).getOutputStream();
                        IOUtils.copy(url.openStream(), outputStream);
                    }
                    finally {
                        IOUtils.closeQuietly(outputStream);
                    }
                }
            }
            catch (IOException e) {
                throw new IllegalStateException(
                        "Encountered an error during updating of resources for the add-on.",
                        e);
            }
        }

    }
}
