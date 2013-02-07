/*
 * Copyright 2002-2010 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.springframework.flex.roo.addon;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.Validate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.osgi.service.component.ComponentContext;
import org.springframework.roo.file.monitor.DirectoryMonitoringRequest;
import org.springframework.roo.file.monitor.MonitoringRequest;
import org.springframework.roo.file.monitor.NotifiableFileMonitorService;
import org.springframework.roo.file.monitor.event.FileOperation;
import org.springframework.roo.file.undo.CreateDirectory;
import org.springframework.roo.file.undo.FilenameResolver;
import org.springframework.roo.file.undo.UndoManager;
import org.springframework.roo.metadata.MetadataDependencyRegistry;
import org.springframework.roo.metadata.MetadataIdentificationUtils;
import org.springframework.roo.metadata.MetadataNotificationListener;
import org.springframework.roo.metadata.MetadataService;
import org.springframework.roo.project.LogicalPath;
import org.springframework.roo.project.Path;
import org.springframework.roo.project.PathResolver;
import org.springframework.roo.project.UndoableMonitoringRequest;

/**
 * {@link MetadataNotificationListener} that monitors the filesystem for changes
 * to Flex source files.
 * 
 * @author Jeremy Grelle
 */
@Component(immediate = true)
@Service
public class FlexProjectListener implements MetadataNotificationListener {

    // TODO - Is there a better way to achieve the monitoring of the necessary
    // Flex directories?

    @Reference private FilenameResolver filenameResolver;

    @Reference private MetadataService metadataService;

    @Reference private MetadataDependencyRegistry metadataDependencyRegistry;

    @Reference private UndoManager undoManager;

    @Reference private NotifiableFileMonitorService fileMonitorService;

    private boolean pathsRegistered = false;

    protected void activate(ComponentContext context) {
        this.metadataDependencyRegistry.addNotificationListener(this);
    }

    protected void deactivate(ComponentContext context) {
        this.metadataDependencyRegistry.removeNotificationListener(this);
    }

    public void notify(String upstreamDependency, String downstreamDependency) {
        if (this.pathsRegistered) {
            return;
        }

        Validate.isTrue(
                MetadataIdentificationUtils.isValid(upstreamDependency),
                "Upstream dependency is an invalid metadata identification string ('"
                        + upstreamDependency + "')");

        if (upstreamDependency.equals(FlexProjectMetadata
                .getProjectIdentifier())) {
            // Acquire the Project Metadata, if available
            FlexProjectMetadata pmd = (FlexProjectMetadata) this.metadataService
                    .get(upstreamDependency);
            if (pmd == null) {
                return;
            }

            PathResolver pathResolver = pmd.getPathResolver();
            Validate.notNull(pathResolver,
                    "Path resolver could not be acquired from changed metadata '"
                            + pmd + "'");

            Set<FileOperation> notifyOn = new HashSet<FileOperation>();
            notifyOn.add(FileOperation.MONITORING_START);
            notifyOn.add(FileOperation.MONITORING_FINISH);
            notifyOn.add(FileOperation.CREATED);
            notifyOn.add(FileOperation.RENAMED);
            notifyOn.add(FileOperation.UPDATED);
            notifyOn.add(FileOperation.DELETED);

            for (LogicalPath path : pathResolver.getPaths()) {
                // Verify path exists and ensure it's monitored, except root
                // (which we assume is already monitored via ProcessManager)

                if (!path.isProjectRoot()) {
                    String fileIdentifier = pathResolver.getRoot(path);
                    File file = new File(fileIdentifier);
                    Validate.isTrue(
                            !file.exists()
                                    || (file.exists() && file.isDirectory()),
                            "Path '"
                                    + fileIdentifier
                                    + "' must either not exist or be a directory");
                    if (!file.exists()) {
                        // Create directory, but no notifications as that will
                        // happen once we start monitoring it below
                        new CreateDirectory(undoManager, filenameResolver, file);
                    }
                    MonitoringRequest request = new DirectoryMonitoringRequest(
                            file, true, notifyOn);
                    new UndoableMonitoringRequest(undoManager,
                            fileMonitorService, request, pmd.isValid());
                }
            }

            // Avoid doing this operation again unless the validity changes
            pathsRegistered = pmd.isValid();

            // Explicitly perform a scan now that we've added all the
            // directories we wish to monitor
            fileMonitorService.scanAll();
        }
    }

    /*
     * private final class PathResolvingAwareFilenameResolver implements
     * FilenameResolver {
     * 
     * public String getMeaningfulName(File file) { Validate.notNull(file,
     * "File required"); return
     * FlexProjectListener.this.pathResolver.getFriendlyName
     * (FileDetails.getCanonicalPath(file)); } }
     */

}
