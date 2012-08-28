/*
 * Copyright 2002-2010 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.flex.roo.addon.mojos;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;
import org.osgi.service.component.ComponentContext;
import org.springframework.roo.file.monitor.MonitoringRequest;
import org.springframework.roo.project.DelegatePathResolver;
import org.springframework.roo.project.LogicalPath;
import org.springframework.roo.project.PhysicalPath;

/**
 * {@link PathResolver} implementation that resolves paths using the conventions of the Flex Mojos Maven plugin.
 * 
 * TODO Changed extends type from AbstractPathResolver to DelegatePathResolver, it's ok ?
 *
 * @author Jeremy Grelle
 */
@Component(immediate = true)
@Service(FlexPathResolver.class)
public class FlexMojosPathResolver extends DelegatePathResolver implements FlexPathResolver {

    private final List<PhysicalPath> pathInformation = new ArrayList<PhysicalPath>();

    protected void activate(ComponentContext context) {
        String workingDir = context.getBundleContext().getProperty("roo.working.directory");
        File root = MonitoringRequest.getInitialMonitoringRequest(workingDir).getFile();
        // TODO Changed from PathInformation to PhysicalPath and from Path to LogicalPath, it's ok ?
        this.pathInformation.add(new PhysicalPath(FlexPath.SRC_MAIN_FLEX.getLogicalPath(), new File(root, "src/main/flex")));
        this.pathInformation.add(new PhysicalPath(FlexPath.LIBS.getLogicalPath(), new File(root, "libs")));
    }

    public List<LogicalPath> getFlexSourcePaths() {
        List<LogicalPath> flexSourcePaths = new ArrayList<LogicalPath>();
        for (LogicalPath path : getPaths()) {
        	// TODO Is this comparation like "path instanceof FlexPath" when FlexPath was a class ?
            if (path.equals(FlexPath.SRC_MAIN_FLEX.getLogicalPath()) || path.equals(FlexPath.LIBS.getLogicalPath())) {
                flexSourcePaths.add(path);
            }
        }
        return flexSourcePaths;
    }

    protected List<PhysicalPath> getPathInformation() {
        return this.pathInformation;
    }

}
