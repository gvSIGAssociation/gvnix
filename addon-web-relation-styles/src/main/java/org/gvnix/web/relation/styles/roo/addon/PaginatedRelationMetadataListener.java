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
package org.gvnix.web.relation.styles.roo.addon;

import java.util.*;
import java.util.logging.Logger;

import org.apache.felix.scr.annotations.*;
import org.osgi.service.component.ComponentContext;
import org.springframework.roo.addon.beaninfo.BeanInfoMetadata;
import org.springframework.roo.addon.entity.EntityMetadata;
import org.springframework.roo.addon.mvc.jsp.JspMetadata;
import org.springframework.roo.addon.web.mvc.controller.WebScaffoldMetadata;
import org.springframework.roo.classpath.details.*;
import org.springframework.roo.metadata.*;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.process.manager.FileManager;
import org.springframework.roo.process.manager.MutableFile;
import org.springframework.roo.project.Path;
import org.springframework.roo.project.PathResolver;
import org.springframework.roo.support.util.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Listens for {@link WebScaffoldMetadata} and produces JSPs when requested by
 * that metadata.
 * 
 * 
 * @author Ricardo García Fernández( rgarcia at disid dot com ) at <a
 *         href="http://www.disid.com">DiSiD Technologies S.L.</a> made for <a
 *         href="http://www.cit.gva.es">Conselleria d'Infraestructures i
 *         Transport</a>
 */
@Component(immediate = true)
@Service
public class PaginatedRelationMetadataListener implements // MetadataProvider,
	MetadataNotificationListener {

    @Reference
    private MetadataDependencyRegistry metadataDependencyRegistry;

    @Reference
    private MetadataService metadataService;

    @Reference
    private FileManager fileManager;

    @Reference
    private PathResolver pathResolver;

    private WebScaffoldMetadata webScaffoldMetadata;

    private EntityMetadata entityMetadata;

    private BeanInfoMetadata beanInfoMetadata;

    private static Logger logger = Logger.getLogger(PaginatedRelationMetadataListener.class.getName());

    protected void activate(ComponentContext context) {
	metadataDependencyRegistry.addNotificationListener(this);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.springframework.roo.metadata.MetadataNotificationListener#notify(
     * java.lang.String, java.lang.String)
     */
    public void notify(String upstreamDependency, String downstreamDependency) {

	if (MetadataIdentificationUtils.getMetadataClass(upstreamDependency)
		.equals(
			MetadataIdentificationUtils
				.getMetadataClass(JspMetadata
					.getMetadataIdentiferType()))) {

	    // Work out the MIDs of the other metadata we depend on
	    String annotationPath = "javax.persistence.OneToMany";
	    List<FieldMetadata> oneToManyFieldMetadatas = getAnnotatedFields(
		    upstreamDependency, annotationPath);

	    // Retrieve the associated jspx (show an update).
	    if (!oneToManyFieldMetadatas.isEmpty()) {

		updateView("show", oneToManyFieldMetadatas, "tab");
	    }
	}

    }

    /** return indicates if disk file was changed (ie updated or created) */
    private boolean writeToDiskIfNecessary(String jspFilename, Document proposed) {
	// TODO:
	return true;
    }

    /**
     * Retrieves the fields of the entity wrapped by the controller with the
     * defined annotation.
     * 
     * @param upstreamDependency
     *            JspMetadata to retrieve Entity Controller information.
     * @param annotationPath
     *            Complete Class name of the annotation. ie:
     *            "javax.persistence.OneToMany"
     * @return FieldMetada {@link List} list of entity fields with the
     *         annotation. If there are any fields, returns an empty ArrayList.
     */
    private List<FieldMetadata> getAnnotatedFields(String upstreamDependency,
	    String annotationPath) {

	JavaType javaType = JspMetadata.getJavaType(upstreamDependency);
	Path webPath = JspMetadata.getPath(upstreamDependency);
	String webScaffoldMetadataKey = WebScaffoldMetadata.createIdentifier(
		javaType, webPath);
	WebScaffoldMetadata webScaffoldMetadata = (WebScaffoldMetadata) metadataService
		.get(webScaffoldMetadataKey);

	this.webScaffoldMetadata = webScaffoldMetadata;

	// BeanInfoMetadata
	String beanInfoMetadataKey = webScaffoldMetadata
		.getIdentifierForBeanInfoMetadata();

	BeanInfoMetadata beanInfoMetadata = (BeanInfoMetadata) metadataService
		.get(beanInfoMetadataKey);

	this.beanInfoMetadata = beanInfoMetadata;

	// Retrieve Controller Entity
	String entityMetadataInfo = webScaffoldMetadata
		.getIdentifierForEntityMetadata();

	JavaType entityJavaType = EntityMetadata
		.getJavaType(entityMetadataInfo);

	Path path = EntityMetadata.getPath(entityMetadataInfo);

	String entityMetadataKey = EntityMetadata.createIdentifier(
		entityJavaType, path);
	EntityMetadata entityMetadata = (EntityMetadata) metadataService
		.get(entityMetadataKey);

	this.entityMetadata = entityMetadata;

	// We need to abort if we couldn't find dependent metadata
	if (beanInfoMetadata == null || !beanInfoMetadata.isValid()
		|| entityMetadata == null || !entityMetadata.isValid()) {
	    // Can't get hold of the entity we are needing to build JSPs for
	    return null;
	}

	DefaultItdTypeDetails defaultItdTypeDetails = (DefaultItdTypeDetails) entityMetadata
		.getItdTypeDetails();

	// Retrieve the fields that are defined as OneToMany relationship.
	List<FieldMetadata> fieldMetadataList = MemberFindingUtils
		.getFieldsWithAnnotation(defaultItdTypeDetails.getGovernor(),
			new JavaType(annotationPath));

	return fieldMetadataList;
    }

    /**
     * Returns the jspx file updated to show the relations using defined view
     * tagx.
     * 
     * @param viewName
     *            Name of the view to update.
     * @param oneToManyFieldMetadatas
     *            List of relation fields to show.
     * 
     * @param selectedDecorator
     *            Decorator element name to show realtionships.
     * 
     * @return Updated Document with the new fields.
     */
    private Document updateView(String viewName,
	    List<FieldMetadata> oneToManyFieldMetadatas,
	    String selectedDecorator) {
	
	DefaultItdTypeDetails defaultItdTypeDetails = (DefaultItdTypeDetails) entityMetadata
		.getItdTypeDetails();

	String entityName = defaultItdTypeDetails.getGovernor().getName()
		.getSimpleTypeName();

	String controllerPath = webScaffoldMetadata.getAnnotationValues()
		.getPath();

	if (controllerPath.startsWith("/")) {
	    controllerPath = controllerPath.substring(1);
	}

	String jspxPath = pathResolver.getIdentifier(Path.SRC_MAIN_WEBAPP,
		"WEB-INF/views/" + controllerPath + "/" + viewName + ".jspx");

	Assert.isTrue(fileManager.exists(jspxPath), viewName
		+ ".jspx not found");

	Document jspxView;
	MutableFile jspxMutableFile = null;

	try {
	    jspxMutableFile = fileManager.updateFile(jspxPath);
	    jspxView = XmlUtils.getDocumentBuilder().parse(
		    jspxMutableFile.getInputStream());
	} catch (Exception e) {
	    throw new IllegalStateException(e);
	}
	Element documentRoot = jspxView.getDocumentElement();

	// Loop for each OneToMany field.

	Element defaultField = null;

	// Views to create.
	Element groupViews = null;
	Element views = null;

	Element divView = XmlUtils.findFirstElement("/div", documentRoot);
	Assert.isTrue(divView != null, "There is no div tagx defined in "
		+ viewName + ".jspx.");

	// Add the new view if doesn't exists
	groupViews = XmlUtils.findFirstElement("/div/" + selectedDecorator
		+ "[@id='relations']", documentRoot);

	// Create element for group views.
	// relations="urn:jsptagdir:/WEB-INF/tags/relations"
	if (groupViews == null) {

	    // Add tagx attributes to div bean.
	    divView.setAttribute("xmlns:relations",
		    "urn:jsptagdir:/WEB-INF/tags/relations");
	    divView.setAttribute("xmlns:relation",
		    "urn:jsptagdir:/WEB-INF/tags/relations/decorators");
	    divView.setAttribute("xmlns:table",
		    "urn:jsptagdir:/WEB-INF/tags/form/fields");

	    // xmlns:relations="urn:jsptagdir:/WEB-INF/tags/relations"
	    // xmlns:relation="urn:jsptagdir:/WEB-INF/tags/relations/decorators"
	    // xmlns:table="urn:jsptagdir:/WEB-INF/tags/form/fields"

	    groupViews = new XmlElementBuilder(
		    "relations:" + selectedDecorator, jspxView).addAttribute(
		    "id", "relations").build();
	}

	// Add the relationship views.
	for (FieldMetadata fieldMetadata : oneToManyFieldMetadatas) {

	    // "/div/display[@field='/" + exceptionViewName + "']"

	    if (viewName.compareTo("show") == 0) {

		defaultField = XmlUtils.findFirstElement(
			"/div/show/display[@field='"
				+ fieldMetadata.getFieldName() + "']",
			documentRoot);
	    } else if (viewName.compareTo("update") == 0) {
		defaultField = XmlUtils.findFirstElement(
			"/div/update/simple[@field='"
				+ fieldMetadata.getFieldName() + "']",
			documentRoot);
	    }

	    if (defaultField == null) {
		break;
	    }

	    // Don't show the default view of relationship.
	    defaultField.setAttribute("render", "false");

	    // relation="urn:jsptagdir:/WEB-INF/tags/relations/decorators"

	    String propertyName = fieldMetadata.getFieldName().getSymbolName();

	    views = new XmlElementBuilder("relation:" + selectedDecorator
		    + "view", jspxView)
		    .addAttribute("data",
			    "${" + StringUtils.uncapitalize(entityName) + "}")
		    .addAttribute("id",
			    "s:" + fieldMetadata.getFieldName().getSymbolName())
		    .addAttribute("field",
			    fieldMetadata.getFieldName().getSymbolName())
		    .addAttribute("fieldId",
			    "l:" + fieldMetadata.getFieldName().getSymbolName())
		    .addAttribute("messageCode", "entity.reference.not.managed")
		    .addAttribute("messageCodeAttribute", entityName)
		    .addAttribute(
			    "path",
			    "/"
				    + StringUtils
					    .uncapitalize(fieldMetadata
						    .getFieldType()
						    .getSimpleTypeName()))
		    .addAttribute("delete", "false").addAttribute("dataResult",
			    propertyName + "list").addAttribute(
			    "relatedEntitySet",
			    "${" + StringUtils.uncapitalize(entityName) + "."
				    + propertyName + "}").build();

	    groupViews.appendChild(views);
	    // <relation:tabview
	    // data="${person}"
	    // id="s:org.gvnix.test.relation.list.table.domain.Person.coches"
	    // field="coches"
	    // fieldId="l:org.gvnix.test.relation.list.table.domain.Person.coches"
	    // messageCode="entity.reference.not.managed"
	    // messageCodeAttribute="Person"
	    // path="/cars"
	    // delete="false"
	    // dataResult="cocheslist"
	    // relatedEntitySet="${person.coches}">

	    logger.warning("Created element:\n" + views.toString());
	}

	jspxView.getLastChild().appendChild(groupViews);
	return jspxView;
    }

}
