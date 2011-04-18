/*
 * gvNIX. Spring Roo based RAD tool for Conselleria d'Infraestructures
 * i Transport - Generalitat Valenciana
 * Copyright (C) 2010 CIT - Generalitat Valenciana
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
/**
 * 
 */
package org.gvnix.service.roo.addon.security;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.springframework.roo.metadata.MetadataService;
import org.springframework.roo.process.manager.FileManager;
import org.springframework.roo.project.Dependency;
import org.springframework.roo.project.ProjectOperations;
import org.springframework.roo.support.util.Assert;
import org.springframework.roo.support.util.TemplateUtils;
import org.springframework.roo.support.util.XmlUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Implementation of {@link SecurityService}
 * 
 * @author Jose Manuel Vivó Arnal ( jmvivo at disid dot com ) at <a
 *         href="http://www.disid.com">DiSiD Technologies S.L.</a> made for <a
 *         href="http://www.cit.gva.es">Conselleria d'Infraestructures i
 *         Transport</a>
 * 
 */
@Component
@Service
public class SecurityServiceImpl implements SecurityService {

    private static final String DEPENDENCIES_FILE = "dependencies-wss4j.xml";

    @Reference
    private MetadataService metadataService;
    @Reference
    private FileManager fileManager;
    @Reference
    private ProjectOperations projectOperations;

    /**
     * {@inheritDoc}
     * 
     * <p>
     * This performs this operations:
     * </p>
     * <ul>
     * <li>Install Apache WSSJ4 depenency in pom</li>
     * <li>Creates axis <code>client-config.wsdd</code> file</li>
     * </ul>
     * 
     **/
    public void setupWSSJ4() {
        addDependencies();
        createAxisClientConfigFile();
    }

    /**
     * Creates <code>client-config.wssd</code> file in application resources
     * folder
     */
    private void createAxisClientConfigFile() {
        // TODO Auto-generated method stub

    }

    /**
     * Adds Apache wss4j dependency to application pom.
     */
    protected void addDependencies() {

        // TODO Unify distinct dependencies files in only one

        InputStream templateInputStream = TemplateUtils.getTemplate(getClass(),
                DEPENDENCIES_FILE);
        Assert.notNull(templateInputStream,
                "Can't adquire dependencies file ".concat(DEPENDENCIES_FILE));

        Document dependencyDoc;
        try {

            dependencyDoc = XmlUtils.getDocumentBuilder().parse(
                    templateInputStream);
        } catch (Exception e) {

            throw new IllegalStateException(e);
        }

        Element dependencies = (Element) dependencyDoc.getFirstChild();

        List<Element> dependecyElementList = XmlUtils.findElements(
                "/dependencies/dependency", dependencies);
        List<Dependency> dependencyList = new ArrayList<Dependency>();

        for (Element element : dependecyElementList) {
            dependencyList.add(new Dependency(element));
        }
        projectOperations.addDependencies(dependencyList);
    }

}
