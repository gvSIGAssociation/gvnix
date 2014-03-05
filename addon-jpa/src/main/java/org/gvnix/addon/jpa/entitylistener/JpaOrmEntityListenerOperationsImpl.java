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
package org.gvnix.addon.jpa.entitylistener;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Logger;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.springframework.roo.classpath.TypeLocationService;
import org.springframework.roo.classpath.operations.AbstractOperations;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.process.manager.MutableFile;
import org.springframework.roo.project.LogicalPath;
import org.springframework.roo.project.Path;
import org.springframework.roo.project.PathResolver;
import org.springframework.roo.project.ProjectOperations;
import org.springframework.roo.support.logging.HandlerUtils;
import org.springframework.roo.support.util.XmlUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Default implementation of {@link JpaOrmEntityListenerOperations}
 * 
 * @author gvNIX Team
 * 
 */
@Component
@Service
public class JpaOrmEntityListenerOperationsImpl extends AbstractOperations
        implements JpaOrmEntityListenerOperations {

    private static final String ORM_XML_TEMPLATE_LOCATION = "orm.xml";

    private static final String ORM_XML_LOCATION = "META-INF/orm.xml";

    private static final String ENTITY_LISTENERS_TAG = "entity-listeners";

    private static final String ENTITY_LISTENER_TAG = "entity-listener";

    private static final String CLASS_ATTRIBUTE = "class";

    private static final Logger LOGGER = HandlerUtils
            .getLogger(JpaOrmEntityListenerOperationsImpl.class);

    @Reference
    private ProjectOperations projectOperations;

    @Reference
    private TypeLocationService typeLocationService;

    @Reference
    private JpaOrmEntityListenerRegistry registry;

    /**
     * {@inheritDoc}
     */
    @Override
    public void addEntityListener(JpaOrmEntityListener definition,
            String sourceMetadataProvider) {

        // Load xml file
        Pair<MutableFile, Document> loadFileResult = loadOrmFile(true);
        MutableFile ormXmlMutableFile = loadFileResult.getLeft();
        Document ormXml = loadFileResult.getRight();
        Element root = ormXml.getDocumentElement();

        // xml modification flag
        boolean modified = false;

        JavaType entityClass = definition.getEntityClass();

        // get entity element for entity class
        Pair<Element, Boolean> entityElementResult = getOrCreateEntityElement(
                ormXml, root, entityClass);
        Element entityElement = entityElementResult.getLeft();
        modified = modified || entityElementResult.getRight();

        // Get entity-listener element
        Pair<Element, Boolean> entityListenersElementResult = getOrCreateEntityListenersElement(
                ormXml, entityElement);
        Element entityListenerElement = entityListenersElementResult.getLeft();
        modified = modified || entityListenersElementResult.getRight();

        // find all listener for this entity
        List<Element> entityListenerElements = XmlUtils.findElements(
                ENTITY_LISTENER_TAG, entityListenerElement);

        // Remove listener which classes can't be found on project
        if (cleanUpMissingListeners(entityClass, entityListenerElement,
                entityListenerElements)) {
            modified = true;
            // get entityListenerElements again
            entityListenerElements = XmlUtils.findElements(
                    "/".concat(ENTITY_LISTENER_TAG), entityListenerElement);
        }

        JavaType listenerClass = definition.getListenerClass();

        // Find listener-element index
        int currentIndex = indexOfListener(entityListenerElements,
                listenerClass);

        if (currentIndex < 0) {
            // Not found: create element
            getOrCreateListenerElement(ormXml, entityListenerElement,
                    listenerClass, sourceMetadataProvider);
            modified = true;
        }

        // If there is more than one listeners
        if (entityElement.getElementsByTagName(ENTITY_LISTENER_TAG).getLength() > 1) {
            // check listeners order
            modified = adjustEntityListenerOrder(ormXml, entityListenerElement,
                    entityListenerElements) || modified;
        }

        if (modified) {
            // If there is any changes on orm.xml save it
            XmlUtils.writeXml(ormXmlMutableFile.getOutputStream(), ormXml);
        }
    }

    /**
     * Adjust listener order as is registered on
     * {@link JpaOrmEntityListenerRegistry}
     * 
     * @param ormXml
     * @param entityListenerElement
     * @param entityListenerElements
     * @return true if xml elements had been changed
     */
    private boolean adjustEntityListenerOrder(Document ormXml,
            Element entityListenerElement, List<Element> entityListenerElements) {

        // Prepare a Pair list which is a representation of current
        // entity-listener
        List<Pair<String, String>> currentOrder = new ArrayList<Pair<String, String>>();
        for (Element currentElement : entityListenerElements) {
            // Each Pair: key (left) = entity-listener class; value (right)
            // metadataProvider id
            currentOrder.add(ImmutablePair.of(
                    currentElement.getAttribute(CLASS_ATTRIBUTE),
                    getEntityListenerElementType(currentElement)));
        }

        // Create a comparator which can sort the list based on order configured
        // on registry
        ListenerOrderComparator comparator = new ListenerOrderComparator(
                registry.getListenerOrder());

        // Clone the Pair list and sort it
        List<Pair<String, String>> ordered = new ArrayList<Pair<String, String>>(
                currentOrder);
        Collections.sort(ordered, comparator);

        // Check if elements order is different form original
        boolean changeOrder = false;
        Pair<String, String> currentPair, orderedPair;
        for (int i = 0; i < currentOrder.size(); i++) {
            currentPair = currentOrder.get(i);
            orderedPair = ordered.get(i);
            if (!StringUtils.equals(currentPair.getKey(), orderedPair.getKey())) {
                changeOrder = true;
                break;
            }
        }

        if (!changeOrder) {
            // Order is correct: nothing to do
            return false;
        }

        // List for new elements to add
        List<Node> newList = new ArrayList<Node>(entityListenerElements.size());

        // Iterate over final ordered list
        int curIndex;
        Node cloned, old;
        for (int i = 0; i < ordered.size(); i++) {
            orderedPair = ordered.get(i);
            // Gets old listener XML node
            curIndex = indexOfListener(entityListenerElements,
                    orderedPair.getKey());
            old = entityListenerElements.get(curIndex);

            // Clone old node and add to new elements list
            cloned = old.cloneNode(true);
            newList.add(cloned);

            // Remove old listener node from parent
            entityListenerElement.removeChild(old);
        }

        // Add listeners xml nodes to parent again in final order
        for (Node node : newList) {
            entityListenerElement.appendChild(node);
        }

        return true;
    }

    /**
     * Iterate over entityListenerElements to check if referred class exists on
     * project. Remove elements which missing class.
     * 
     * @param entityListenerElement
     * @param entityListenerElements
     * @return
     */
    private boolean cleanUpMissingListeners(JavaType entity,
            Element entityListenerElement, List<Element> entityListenerElements) {
        boolean changed = false;

        String classOfListener;
        for (Element current : entityListenerElements) {
            classOfListener = current.getAttribute(CLASS_ATTRIBUTE);
            if (typeLocationService.getPhysicalTypeIdentifier(new JavaType(
                    classOfListener)) == null) {
                // class not found
                entityListenerElement.removeChild(current);
                LOGGER.info(String
                        .format("Removing missing entity-listenen '%s' of entity '%s' from orm.xml",
                                classOfListener, entity));
                changed = true;
            }
        }
        return changed;
    }

    /**
     * Gets index of the element which attribute <code>class</code> is
     * <code>listenerClass</code>
     * 
     * @param entityListenersElements
     * @param listenerClass
     * @return
     */
    private int indexOfListener(List<Element> entityListenersElements,
            JavaType listenerClass) {
        return indexOfListener(entityListenersElements,
                listenerClass.getFullyQualifiedTypeName());
    }

    /**
     * Gets index of the element which attribute <code>class</code> is
     * <code>className</code>
     * 
     * @param entityListenersElements
     * @param className
     * @return
     */
    private int indexOfListener(List<Element> entityListenersElements,
            String className) {
        Element current;
        for (int i = 0; i < entityListenersElements.size(); i++) {
            current = entityListenersElements.get(i);
            if (className.equals(current.getAttribute(CLASS_ATTRIBUTE))) {
                return i;
            }
        }
        // Not found
        return -1;
    }

    /**
     * @param current
     * @return
     */
    private String getEntityListenerElementType(Element current) {
        Element description = XmlUtils
                .findFirstElement("/description", current);
        if (description == null) {
            return null;
        }
        return description.getTextContent();
    }

    /**
     * Gets the <code>entity-listener</code> xml element for
     * <code>listenerClass</code>. Creates it if not found.
     * 
     * @param document XML document instance
     * @param entityListenerElement parent XML Element
     * @param listenerClass listener class to search/create
     * @param sourceMetadataProvider metadaProviderId of listener class
     * @return Element found/created, true if element has been created
     */
    private Pair<Element, Boolean> getOrCreateListenerElement(
            Document document, Element entityListenerElement,
            JavaType listenerClass, String sourceMetadataProvider) {

        Pair<Element, Boolean> result = getOrCreateElement(
                document,
                entityListenerElement,
                ENTITY_LISTENER_TAG,
                ImmutablePair.of(CLASS_ATTRIBUTE,
                        listenerClass.getFullyQualifiedTypeName()));

        // If has not been changed
        if (!result.getRight()) {
            return result;
        }

        // Add source MetadataProviderId on description child tag
        Element element = result.getLeft();
        Element description = document.createElement("description");
        description.setTextContent(sourceMetadataProvider);
        element.appendChild(description);

        return ImmutablePair.of(element, true);
    }

    /**
     * Gets or create <code>entity-listeners</code> xml element and add it as
     * child of <code>entity</code> xml element
     * 
     * @param document XML document
     * @param entityElement entity element
     * @return the new xml element
     */
    private Pair<Element, Boolean> getOrCreateEntityListenersElement(
            Document document, Element entityElement) {
        return getOrCreateElement(document, entityElement,
                ENTITY_LISTENERS_TAG, null);
    }

    /**
     * Gets or creates a xml element (with tag-name <code>elementName</code>) on
     * <code>parent</code>.
     * <p/>
     * If <code>attributeValue</code> is provided will be used to search and
     * applied to the creation.
     * 
     * @param document xml document instance
     * @param parent node to add the new xml element
     * @param elementName new xml tag name
     * @param attributeValue (optional) attribute name + attribute value
     * @return Element found; true if element is new
     */
    private Pair<Element, Boolean> getOrCreateElement(Document document,
            Element parent, String elementName,
            Pair<String, String> attributeValue) {
        boolean changed = false;

        // prepare xpath expression to search for element
        StringBuilder sbXpath = new StringBuilder();
        sbXpath.append(elementName);
        if (attributeValue != null) {
            sbXpath.append("[@");
            sbXpath.append(attributeValue.getKey());
            sbXpath.append("='");
            sbXpath.append(attributeValue.getValue());
            sbXpath.append("']");
        }
        String xpath = sbXpath.toString();

        // Search for element
        Element targetElement = XmlUtils.findFirstElement(xpath, parent);

        if (targetElement == null) {
            // Not found: create it
            targetElement = document.createElement(elementName);
            if (attributeValue != null) {
                targetElement.setAttribute(attributeValue.getKey(),
                        attributeValue.getValue());
            }
            parent.appendChild(targetElement);

            // search again
            targetElement = XmlUtils.findFirstElement(xpath, parent);
            if (targetElement == null) {
                // something went worng
                throw new IllegalStateException("Can't create ".concat(xpath)
                        .concat(" element"));
            }
            changed = true;
        }

        return ImmutablePair.of(targetElement, changed);
    }

    /**
     * Gets or creates the <code>entity</code> xml element
     * 
     * @param document XML document
     * @param root element
     * @param entityClass
     * @return the new xml element, changes made
     */
    private Pair<Element, Boolean> getOrCreateEntityElement(Document document,
            Element root, JavaType entityClass) {

        return getOrCreateElement(
                document,
                root,
                "entity",
                ImmutablePair.of(CLASS_ATTRIBUTE,
                        entityClass.getFullyQualifiedTypeName()));
    }

    /**
     * Gets orm.xml path on project.
     * <p/>
     * if <code>create</code> Creates it from add-on resources if not exists.
     * 
     * @param create file if not exists
     * @return orm.xml file path
     */
    private String getOrmXmlPath(boolean create) {
        PathResolver pathResolver = projectOperations.getPathResolver();

        String ormXmlPath = pathResolver.getIdentifier(
                LogicalPath.getInstance(Path.SRC_MAIN_RESOURCES, ""),
                ORM_XML_LOCATION);
        if (!fileManager.exists(ormXmlPath)) {
            if (!create) {
                return null;
            }
            InputStream ins = null;
            try {
                ins = getClass().getResourceAsStream(ORM_XML_TEMPLATE_LOCATION);
                String contents = IOUtils.toString(ins);
                fileManager.createOrUpdateTextFileIfRequired(ormXmlPath,
                        contents, false);
                fileManager.commit();
            }
            catch (Exception e) {
                throw new IllegalStateException("Can't create orm.xml", e);
            }
            finally {
                IOUtils.closeQuietly(ins);
            }
        }
        return ormXmlPath;
    }

    /**
     * Comparator to allow adjust order of entityListeners from the order
     * registered on {@link JpaOrmEntityListenerRegistry}
     * <p/>
     * This compares instances of {@link Pair} which left (or key) element is
     * listener class and right (or value) is the metadataProviderId.
     * 
     * @author jmvivo
     * 
     */
    private class ListenerOrderComparator implements
            Comparator<Pair<String, String>> {

        private List<String> dependencyOrder;

        public ListenerOrderComparator(List<String> dependencyOrder) {
            this.dependencyOrder = dependencyOrder;
        }

        @Override
        public int compare(Pair<String, String> arg0, Pair<String, String> arg1) {
            int dependencyIndex0 = dependencyOrder.indexOf(arg0.getValue());
            int dependencyIndex1 = dependencyOrder.indexOf(arg1.getValue());

            if (dependencyIndex0 < dependencyIndex1) {
                return -1;
            }
            else if (dependencyIndex0 > dependencyIndex1) {
                return 1;
            }
            return arg0.getKey().compareTo(arg1.getKey());
        }

    }

    @Override
    public void cleanUpEntityListeners(JavaType entity) {
        // Load xml file
        Pair<MutableFile, Document> loadFileResult = loadOrmFile(false);
        if (loadFileResult == null) {
            // orm.xml not exists: nothing to do
            return;
        }
        MutableFile ormXmlMutableFile = loadFileResult.getLeft();
        Document ormXml = loadFileResult.getRight();
        Element root = ormXml.getDocumentElement();

        // xml modification flag
        boolean modified = false;

        // get entity element for entity class
        Pair<Element, Boolean> entityElementResult = getOrCreateEntityElement(
                ormXml, root, entity);
        Element entityElement = entityElementResult.getLeft();
        modified = modified || entityElementResult.getRight();

        if (modified) {
            // entity element do not exists on orm.xml: nothing to clean up
            return;
        }

        // Get entity-listener element
        Pair<Element, Boolean> entityListenersElementResult = getOrCreateEntityListenersElement(
                ormXml, entityElement);
        Element entityListenerElement = entityListenersElementResult.getLeft();
        modified = modified || entityListenersElementResult.getRight();

        if (modified) {
            // entity-listeners element do not exists on orm.xml: nothing to
            // clean up
            return;
        }

        // find all listener for this entity
        List<Element> entityListenerElements = XmlUtils.findElements(
                ENTITY_LISTENER_TAG, entityListenerElement);

        if (entityListenerElements == null || entityListenerElements.isEmpty()) {
            // no entity-listener element found on orm.xml: nothing to clean up
            return;
        }

        // Remove listener which classes can't be found on project
        if (cleanUpMissingListeners(entity, entityListenerElement,
                entityListenerElements)) {
            modified = true;
        }

        if (!modified) {
            // is all right: to do
            return;
        }

        if (modified) {
            // If there is any changes on orm.xml save it
            XmlUtils.writeXml(ormXmlMutableFile.getOutputStream(), ormXml);
        }
    }

    /**
     * Load the orm.xml file
     * 
     * @param create file if not exists
     * 
     * @return {@link MutableFile} and {@link Document}
     */
    private Pair<MutableFile, Document> loadOrmFile(boolean create) {
        // Get the orm.xml path (and create it if not exists)
        String ormXmlPath = getOrmXmlPath(create);

        if (ormXmlPath == null) {
            return null;
        }

        // Load xml file
        MutableFile ormXmlMutableFile = null;
        Document ormXml;
        try {
            // Get orm.xml content and parse it
            ormXmlMutableFile = fileManager.updateFile(ormXmlPath);
            ormXml = XmlUtils.getDocumentBuilder().parse(
                    ormXmlMutableFile.getInputStream());
        }
        catch (Exception e) {
            LOGGER.severe("Error loading file '".concat(ormXmlPath).concat("'"));
            throw new IllegalStateException("Error loading file '".concat(
                    ormXmlPath).concat("'"), e);
        }

        return new ImmutablePair<MutableFile, Document>(ormXmlMutableFile,
                ormXml);
    }
}
