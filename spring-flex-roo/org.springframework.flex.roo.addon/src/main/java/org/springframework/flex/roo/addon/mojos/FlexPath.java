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

import org.apache.commons.lang3.Validate;
import org.springframework.roo.project.LogicalPath;
import org.springframework.roo.project.Path;

/**
 * Representation of a conventional Flex Mojos source path.
 * 
 * TODO Location on each enumeration element is required ?
 *
 * @author Jeremy Grelle
 */
public enum FlexPath {

    SRC_MAIN_FLEX("src/main/flex"),
    
    LIBS("libs");

    private final String defaultLocation;

    /**
     * Constructor
     * 
     * @param defaultLocation the location relative to the module's root
     *            directory in which this path is located by default (can't be
     *            <code>null</code>)
     */
    private FlexPath(final String defaultLocation) {
        Validate.notNull(defaultLocation, "Default location is required");
        this.defaultLocation = defaultLocation;
    }
    
    public LogicalPath getLogicalPath() {
    	// TODO Is this method to obtain the LogicalPath the best ?
    	return LogicalPath.getInstance(Path.ROOT, this.defaultLocation);
    }
    
}
