/*
 * gvNIX. Spring Roo based RAD tool for Generalitat Valenciana     
 * Copyright (C) 2013 Generalitat Valenciana
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
 * along with this program.  If not, see <http://www.gnu.org/copyleft/gpl.html>.
 */
package org.gvnix.support;

import java.util.Map;

import org.osgi.service.component.ComponentContext;
import org.springframework.roo.metadata.MetadataService;
import org.springframework.roo.process.manager.FileManager;
import org.springframework.roo.project.PathResolver;
import org.springframework.roo.project.ProjectOperations;

/**
 * Utils for Command classes.
 * 
 * @author gvNIX Team
 */
public interface OperationUtils {

    /**
     * Check if there is a project available.
     * 
     * @param metadataService Metadata Service component
     * @return Is project available ?
     */
    public boolean isProjectAvailable(MetadataService metadataService,
            ProjectOperations projectOperations);

    /**
     * Get the path resolver or null if there is no project available.
     * 
     * @param metadataService Metadata Service component
     * @param projectOperations Project operations component
     * @return Path resolver or null
     */
    public PathResolver getPathResolver(MetadataService metadataService,
            ProjectOperations projectOperations);

    /**
     * Check if current project is a Spring MVC one
     * <p/>
     * Search webmvc-config.xml file exists.
     * 
     * @param metadataService Metadata Service component
     * @param fileManager File manager component
     * @param projectOperations Project operations component
     * @return Is a Spring MVC project ?
     * @deprecated Use
     *             {@link WebProjectUtils#isSpringMvcProject(MetadataService, FileManager, ProjectOperations)}
     */
    public boolean isSpringMvcProject(MetadataService metadataService,
            FileManager fileManager, ProjectOperations projectOperations);

    /**
     * Check if current project is a web project
     * <p/>
     * Search web.xml file exists.
     * 
     * @param metadataService Metadata Service component
     * @param fileManager File manager component
     * @param projectOperations Project operations component
     * @return Is a web project ?
     * @deprecated Use
     *             {@link WebProjectUtils#isWebProject(MetadataService, FileManager, ProjectOperations)}
     */
    public boolean isWebProject(MetadataService metadataService,
            FileManager fileManager, ProjectOperations projectOperations);

    /**
     * Installs the Dialog Java class
     * 
     * @param packageFullName fullyQualifiedName of destination package for
     *        Dialog Bean. ie. <code>com.disid.myapp.web.dialog</code>
     * @param pathResolver
     * @param fileManager
     * @deprecated Use
     *             {@link WebProjectUtils#installWebDialogClass(String, PathResolver, FileManager)}
     */
    public void installWebDialogClass(String packageFullName,
            PathResolver pathResolver, FileManager fileManager);

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
    public void installJavaClassFromTemplate(String packageFullName,
            String classFullName, String javaClassTemplateName,
            PathResolver pathResolver, FileManager fileManager);

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
     * @param toReplace (optional) map of additional patterns to replace
     * @param pathResolver
     * @param fileManager
     */
    public void installJavaClassFromTemplate(String packageFullName,
            String classFullName, String javaClassTemplateName,
            Map<String, String> toReplace, PathResolver pathResolver,
            FileManager fileManager);

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
    public void updateDirectoryContents(String sourceAntPath,
            String targetDirectory, FileManager fileManager,
            ComponentContext context, Class<?> clazz);
}
