/*
 * gvNIX. Spring Roo based RAD tool for Conselleria d'Infraestructures i
 * Transport - Generalitat Valenciana Copyright (C) 2010 CIT - Generalitat
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
package org.gvnix.dynamic.configuration.roo.addon;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.gvnix.dynamic.configuration.roo.addon.entity.DynComponent;
import org.gvnix.dynamic.configuration.roo.addon.entity.DynComponentList;
import org.gvnix.dynamic.configuration.roo.addon.entity.DynConfiguration;
import org.gvnix.dynamic.configuration.roo.addon.entity.DynConfigurationList;
import org.gvnix.dynamic.configuration.roo.addon.entity.DynProperty;
import org.gvnix.dynamic.configuration.roo.addon.entity.DynPropertyList;
import org.springframework.roo.process.manager.FileManager;
import org.springframework.roo.project.LogicalPath;
import org.springframework.roo.project.Path;
import org.springframework.roo.project.ProjectOperations;
import org.springframework.roo.support.util.XmlUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Manage POM.
 * 
 * @author Mario Martínez Sánchez ( mmartinez at disid dot com ) at <a
 *         href="http://www.disid.com">DiSiD Technologies S.L.</a> made for <a
 *         href="http://www.cit.gva.es">Conselleria d'Infraestructures i
 *         Transport</a>
 */
@Component
@Service
public class PomManagerImpl implements PomManager {

    private static final String RESOURCES_PATH = "src/main/resources";

    @Reference
    private ProjectOperations projectOperations;
    @Reference
    private FileManager fileManager;

    /**
     * {@inheritDoc}
     */
    public DynConfigurationList export(DynConfigurationList dynConfs) {

        // Pom root element
        String pom = projectOperations.getPathResolver().getIdentifier(
                LogicalPath.getInstance(Path.ROOT, ""), "pom.xml");
        Document doc = XmlUtils.readXml(fileManager.getInputStream(pom));

        // Iterate stored dynamic configurations for export to pom
        for (DynConfiguration dynConf : dynConfs) {

            Element prof = exportConfiguration(doc, dynConf);

            // Iterate components of dynamic configuration
            DynComponentList dynComps = dynConf.getComponents();
            for (DynComponent dynComp : dynComps) {

                Element props = exportComponent(doc, prof);

                // Iterate properties of dynamic configuration
                DynPropertyList dynProps = dynComp.getProperties();
                for (DynProperty dynProp : dynProps) {

                    exportProperty(doc, props, dynProp);
                }
            }

            // Update POM configuration before store vars to avoid overwrite
            fileManager.createOrUpdateTextFileIfRequired(pom,
                    XmlUtils.nodeToString(doc), true);
        }

        return dynConfs;
    }

    /**
     * Write a configurations into POM profile.
     * 
     * @param doc Pom document
     * @param dynConf
     * @return New configuration element
     */
    protected Element exportConfiguration(Document doc, DynConfiguration dynConf) {

        Element root = doc.getDocumentElement();

        // <project>
        // <profiles>
        // <profile>
        // <id>test</id>
        // <activation>
        // <activeByDefault>true</activeByDefault>
        // </activation>
        // </profile>
        // </profiles>
        // </project>

        // Profiles section: find or create if not exists
        Element profs = XmlUtils.findFirstElement("/project/profiles", root);
        if (profs == null) {

            profs = doc.createElement("profiles");
            root.appendChild(profs);
        }

        // Remove profile if already exists
        Element prof = XmlUtils.findFirstElement("profile/id[text()='"
                + dynConf.getName() + "']/..", profs);
        if (prof != null) {

            profs.removeChild(prof);
        }

        // Create a profile section for this dynamic configuration
        prof = doc.createElement("profile");
        profs.appendChild(prof);

        // Create an identifier for profile
        Element id = doc.createElement("id");
        id.setTextContent(dynConf.getName());
        prof.appendChild(id);

        // If dynamic configuration is active: profile active by default
        if (dynConf.isActive()) {

            // Create an activation section
            Element activation = doc.createElement("activation");
            prof.appendChild(activation);

            Element active = doc.createElement("activeByDefault");
            active.setTextContent("true");
            activation.appendChild(active);
        }

        return prof;
    }

    /**
     * Write a component into POM profile.
     * 
     * @param doc Pom document
     * @param prof Profile element
     * @return New component element
     */
    protected Element exportComponent(Document doc, Element prof) {

        Element root = doc.getDocumentElement();

        // <resources>
        // <resource>
        // <directory>src/main/resources</directory>
        // <filtering>true</filtering>
        // </resource>
        // </resources>

        // Build section: find or create if not exists
        Element build = XmlUtils.findFirstElement("/project/build", root);
        if (build == null) {

            build = doc.createElement("build");
            root.appendChild(build);
        }

        Element resos;

        // Resources section: find or create if not exists
        resos = XmlUtils.findFirstElement("resources", build);
        if (resos == null) {

            resos = doc.createElement("resources");
            build.appendChild(resos);
        }

        // Find resource section with directory and filter
        Element reso = XmlUtils.findFirstElement("resource/directory"
                + "[text()='" + RESOURCES_PATH
                + "']/../filtering[text()='true']/..", resos);

        // Remove resource if already exists
        if (reso != null) {

            resos.removeChild(reso);
        }

        // Create resource section with directory and filter
        reso = doc.createElement("resource");
        resos.appendChild(reso);
        Element dir = doc.createElement("directory");
        dir.setTextContent(RESOURCES_PATH);
        reso.appendChild(dir);
        Element filter = doc.createElement("filtering");
        filter.setTextContent("true");
        reso.appendChild(filter);

        // <properties>
        // </properties>

        // Properties section: find or create if not exists
        Element props = XmlUtils.findFirstElement("properties", prof);
        if (props == null) {
            props = doc.createElement("properties");
            prof.appendChild(props);
        }

        return props;
    }

    /**
     * Write a property into POM profile.
     * <p>
     * No write property if null value.
     * </p>
     * 
     * @param doc Pom document
     * @param props Properties element
     * @return New property element
     */
    protected void exportProperty(Document doc, Element props,
            DynProperty dynProp) {

        String key = dynProp.getKey().replace('/', '.').replace('[', '.')
                .replace(']', '.').replace('@', '.').replace(':', '.')
                .replace('-', '.').replace('%', '.');
        while (key.startsWith(".")) {
            key = key.substring(1, key.length());
        }
        while (key.endsWith(".")) {
            key = key.substring(0, key.length() - 1);
        }
        while (key.contains("..")) {
            key = key.replace("..", ".");
        }

        // Create property if not empty (no write property if null value)
        if (dynProp.getValue() != null) {

            // <log4j.rootLogger>INFO, stdout</log4j.rootLogger>

            // Create a property element for this dynamic property
            Element prop = doc.createElement(key);
            props.appendChild(prop);

            // Store this dynamic property value in pom and replace
            // dynamic property value with a var
            prop.setTextContent(dynProp.getValue());
        }

        dynProp.setValue("${" + key + "}");
    }
}
