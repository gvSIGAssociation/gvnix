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

import java.io.*;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.logging.Logger;

import org.apache.felix.scr.annotations.*;
import org.osgi.service.component.ComponentContext;
import org.springframework.roo.addon.beaninfo.BeanInfoMetadata;
import org.springframework.roo.addon.entity.EntityMetadata;
import org.springframework.roo.addon.mvc.jsp.*;
import org.springframework.roo.addon.web.mvc.controller.WebScaffoldMetadata;
import org.springframework.roo.classpath.PhysicalTypeMetadata;
import org.springframework.roo.classpath.PhysicalTypeMetadataProvider;
import org.springframework.roo.classpath.details.*;
import org.springframework.roo.classpath.itd.ItdMetadataScanner;
import org.springframework.roo.file.monitor.event.FileDetails;
import org.springframework.roo.metadata.*;
import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.process.manager.FileManager;
import org.springframework.roo.process.manager.MutableFile;
import org.springframework.roo.process.manager.event.*;
import org.springframework.roo.project.Path;
import org.springframework.roo.project.PathResolver;
import org.springframework.roo.support.util.*;
import org.w3c.dom.*;

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

    @Reference private MetadataDependencyRegistry metadataDependencyRegistry;

    @Reference private MetadataService metadataService;

    @Reference private FileManager fileManager;

    @Reference private PathResolver pathResolver;

    @Reference private PhysicalTypeMetadataProvider physicalTypeMetadataProvider;

    @Reference private ItdMetadataScanner itdMetadataScanner;

    @Reference
    private ProcessManagerStatusProvider processManagerStatus;

    private StatusListener statusListener;

    private WebScaffoldMetadata webScaffoldMetadata;

    private EntityMetadata entityMetadata;

    private BeanInfoMetadata beanInfoMetadata;

    @Reference
    private PaginatedRelationTableActivationInfo paginatedRelationTableActivationInfo;

    private ComponentContext context;

    private List<String> upstreamDependencyList = new ArrayList<String>();

    private static Logger logger = Logger
	    .getLogger(PaginatedRelationMetadataListener.class.getName());

    protected void activate(ComponentContext context) {
	this.context = context;
	metadataDependencyRegistry.addNotificationListener(this);
	statusListener = new StatusListener(this);
	processManagerStatus.addProcessManagerStatusListener(statusListener);
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see
     * org.springframework.roo.metadata.MetadataNotificationListener#notify(
     * java.lang.String, java.lang.String)
     */
    public void notify(String upstreamDependency, String downstreamDependency) {

	if (!paginatedRelationTableActivationInfo.isProjectAvailable()
		|| !paginatedRelationTableActivationInfo
			.isWebScaffoldGenerated()
		|| !paginatedRelationTableActivationInfo.isActivated()) {
	    return;
	}

	if (MetadataIdentificationUtils.getMetadataClass(upstreamDependency)
		.equals(
			MetadataIdentificationUtils
				.getMetadataClass(JspMetadata
					.getMetadataIdentiferType()))) {

	    // Add upstreamDependency to a Stack.
	    upstreamDependencyList.add(upstreamDependency);
	}

    }

    /**
     * 
     * return indicates if disk file was changed (ie updated or created)
     */
    private boolean writeToDiskIfNecessary(String jspFilename, Document proposed) {

	String controllerPath = webScaffoldMetadata.getAnnotationValues()
		.getPath();

	if (controllerPath.startsWith("/")) {
	    controllerPath = controllerPath.substring(1);
	}

	String jspxPath = pathResolver
		.getIdentifier(Path.SRC_MAIN_WEBAPP, "WEB-INF/views/"
			+ controllerPath + "/" + jspFilename + ".jspx");

	Assert.isTrue(fileManager.exists(jspxPath), jspFilename
		+ ".jspx not found");

	ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
	XmlUtils.writeXml(XmlUtils.createIndentingTransformer(),
		byteArrayOutputStream, proposed);
	String proposedString = byteArrayOutputStream.toString();

	MutableFile mutableFile = null;
	if (fileManager.exists(jspxPath)) {
	    String originalString = null;

	    try {
		originalString = FileCopyUtils.copyToString(new FileReader(
			jspxPath));
	    } catch (Exception e) {
		throw new IllegalStateException("Could not get the file:\t'"
			+ jspxPath + "'", e.getCause());
	    }

	    if (!proposedString.equals(originalString)) {
		mutableFile = fileManager.updateFile(jspxPath);
	    }

	    try {
		if (mutableFile != null) {

		    FileCopyUtils.copy(proposedString, new OutputStreamWriter(
			    mutableFile.getOutputStream()));
		    fileManager.scan();
		}
	    } catch (IOException ioe) {
		throw new IllegalStateException("Could not output '"
			+ mutableFile.getCanonicalPath() + "'", ioe);
	    }
	}

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

	try {
	    jspxView = XmlUtils.getDocumentBuilder().parse(
		    fileManager.getInputStream(jspxPath));
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


	// There aren't relationships to update or remove.
	if (oneToManyFieldMetadatas.isEmpty() && groupViews == null) {
	    return null;
	} else
	// It doesn't have relationships. Remove old relation views and
	// attributes.
	if (oneToManyFieldMetadatas.isEmpty() && groupViews != null) {

	    divView.removeAttribute("xmlns:relations");
	    divView.removeAttribute("xmlns:relation");
	    divView.removeAttribute("xmlns:table");

	    groupViews.getParentNode().removeChild(groupViews);

	    return jspxView;
	}

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

	}

	if (groupViews != null) {

	    groupViews.getParentNode().removeChild(groupViews);
	}

	// Create group views.
	groupViews = new XmlElementBuilder("relations:" + selectedDecorator,
		jspxView).addAttribute("id", "relations").build();

	WebScaffoldMetadata relatedWebScaffoldMetadata;

	// Show initialized table properties.
	String create = "false";
	String delete = "false";
	String update = "false";

	// Add the relationship views.
	for (FieldMetadata fieldMetadata : oneToManyFieldMetadatas) {

	    if (viewName.compareTo("show") == 0) {

		defaultField = XmlUtils.findFirstElement(
			"/div/show/display[@field='"
				+ fieldMetadata.getFieldName() + "']",
			documentRoot);
		delete = "false";
	    } else if (viewName.compareTo("update") == 0) {
		defaultField = XmlUtils.findFirstElement(
			"/div/update/simple[@field='"
				+ fieldMetadata.getFieldName() + "']",
			documentRoot);
		delete = "true";
	    }

	    if ((defaultField != null)
		    && ((defaultField.getAttribute("render").compareTo("") == 0) || (defaultField
			    .getAttribute("render").compareTo("true") == 0))) {
		// Don't show the default view of relationship.
		defaultField.setAttribute("render", "false");
	    }

	    // Retrieve Related Entities Metadata
	    JavaType relationshipJavaType = fieldMetadata.getFieldType();
	    // ignoring java.util.Map field types (see ROO-194)
	    if (relationshipJavaType.getFullyQualifiedTypeName().equals(
		    Set.class.getName())) {

		Assert
			.isTrue(
				relationshipJavaType.getParameters().size() == 1,
				"A set is defined without specification of its type (via generics) - unable to create view for it");
	    }

	    relationshipJavaType = relationshipJavaType.getParameters().get(0);
	    relatedWebScaffoldMetadata = getRelatedEntityWebScaffoldMetadata(relationshipJavaType);

	    // Check if exists the path.
	    String path;

	    // If doesn't exist related controller to entity relation.
	    // Don't enable table action properties.
	    if (relatedWebScaffoldMetadata == null) {
		create = "false";
		delete = "false";
		update = "false";
		path = "/";
	    } else {
		create = "true";
		update = "true";
		path = "/".concat(relatedWebScaffoldMetadata
			.getAnnotationValues().getPath());
	    }

	    String propertyName = fieldMetadata.getFieldName().getSymbolName();

	    views = new XmlElementBuilder("relation:".concat(selectedDecorator)
		    .concat("view"), jspxView)
		    .addAttribute(
			    "data",
			    "${".concat(StringUtils.uncapitalize(entityName))
				    .concat("}"))
		    .addAttribute(
			    "id",
		    "s:".concat(
			    beanInfoMetadata.getJavaBean()
				    .getFullyQualifiedTypeName())
					    .concat(".").concat(propertyName))
		    .addAttribute("field",
			    fieldMetadata.getFieldName().getSymbolName())
		    .addAttribute(
			    "fieldId",
		    "l:".concat(
			    beanInfoMetadata.getJavaBean()
				    .getFullyQualifiedTypeName())
					    .concat(".").concat(propertyName))
		    .addAttribute("messageCode", "entity.reference.not.managed")
		    .addAttribute("messageCodeAttribute", entityName)
		    .addAttribute("path", path).addAttribute("delete", delete)
		    .addAttribute("create", create).addAttribute("update",
			    update).addAttribute("dataResult",
			    propertyName.concat("list")).addAttribute(
			    "relatedEntitySet",
			    "${".concat(StringUtils.uncapitalize(entityName))
				    .concat(".").concat(propertyName).concat(
					    "}")).build();

	    // Create column fields and group the elements created.

	    String idEntityMetadata = physicalTypeMetadataProvider
		    .findIdentifier(relationshipJavaType);

	    Set<MetadataItem> metadata = itdMetadataScanner
		    .getMetadata(idEntityMetadata);

	    BeanInfoMetadata relatedBeanInfoMetadata = null;

	    for (MetadataItem item : metadata) {
		if (item instanceof BeanInfoMetadata) {
		    relatedBeanInfoMetadata = (BeanInfoMetadata) item;
		    break;
		}
	    }

	    Assert.notNull(relatedBeanInfoMetadata,
		    "There is no metadata related for this identifier:\t"
			    + idEntityMetadata);

	    List<FieldMetadata> fieldMetadataList = getElegibleFields(relatedBeanInfoMetadata);
	    Element columnField;

	    String relatedEntityClassName = relatedBeanInfoMetadata
		    .getItdTypeDetails().getName().getFullyQualifiedTypeName();
	    String property;
	    String z = "user-managed";

	    for (FieldMetadata relatedEntityfieldMetadata : fieldMetadataList) {

		property = relatedEntityfieldMetadata.getFieldName()
			.getSymbolName();
		columnField = new XmlElementBuilder("table:column", jspxView)
			.addAttribute(
				"id",
				"s:".concat(relatedEntityClassName.concat(".")
					.concat(property))).addAttribute(
				"property", property).addAttribute("z", z)
			.build();

		views.appendChild(columnField);
	    }

	    groupViews.appendChild(views);
	}

	    jspxView.getLastChild().appendChild(groupViews);
	    return jspxView;
    }

    /**
     * Retrieve the associated WebScaffoldMetada from a JavaType.
     * 
     * @param relationshipJavaType
     *            Relationship JavaType
     * @return
     */
    private WebScaffoldMetadata getRelatedEntityWebScaffoldMetadata(
	    JavaType relationshipJavaType) {

	WebScaffoldMetadata relationshipMetadata = null;

	FileDetails srcRoot = new FileDetails(new File(pathResolver
		.getRoot(Path.SRC_MAIN_JAVA)), null);
	String antPath = pathResolver.getRoot(Path.SRC_MAIN_JAVA)
		+ File.separatorChar + "**" + File.separatorChar + "*.java";
	SortedSet<FileDetails> entries = fileManager
		.findMatchingAntPath(antPath);

	for (FileDetails file : entries) {
	    String fullPath = srcRoot.getRelativeSegment(file
		    .getCanonicalPath());
	    fullPath = fullPath.substring(1, fullPath.lastIndexOf(".java"))
		    .replace(File.separatorChar, '.'); // ditch the first /
	    // and .java
	    JavaType javaType = new JavaType(fullPath);
	    String id = physicalTypeMetadataProvider.findIdentifier(javaType);
	    if (id != null) {
		PhysicalTypeMetadata ptm = (PhysicalTypeMetadata) metadataService
			.get(id);
		if (ptm == null
			|| ptm.getPhysicalTypeDetails() == null
			|| !(ptm.getPhysicalTypeDetails() instanceof ClassOrInterfaceTypeDetails)) {
		    continue;
		}

		ClassOrInterfaceTypeDetails cid = (ClassOrInterfaceTypeDetails) ptm
			.getPhysicalTypeDetails();
		if (Modifier.isAbstract(cid.getModifier())) {
		    continue;
		}

		Set<MetadataItem> metadata = itdMetadataScanner.getMetadata(id);

		for (MetadataItem item : metadata) {

		    if (item instanceof EntityMetadata) {
			EntityMetadata em = (EntityMetadata) item;

			if (relationshipJavaType.compareTo(em
				.getItdTypeDetails().getName()) == 0) {

			    Set<String> downstream = metadataDependencyRegistry
				    .getDownstream(em.getId());
			    // check to see if this entity metadata has a web
			    // scaffold metadata listening to it
			    for (String ds : downstream) {
				if (WebScaffoldMetadata.isValid(ds)) {
				    // there is already a controller for this
				    // entity
				    String entityMetadataKey = WebScaffoldMetadata
					    .createIdentifier(
						    WebScaffoldMetadata
							    .getJavaType(ds),
						    WebScaffoldMetadata
							    .getPath(ds));
				    relationshipMetadata = (WebScaffoldMetadata) metadataService
					    .get(entityMetadataKey);
				    return relationshipMetadata;
				}
			    }
			}
		    }
		}
	    }
	}
	return relationshipMetadata;
    }

    /**
     * Retrieve BeanInfoMetadata fields.
     * 
     * @return {@link FieldMetadata} list.
     */
    private List<FieldMetadata> getElegibleFields(
	    BeanInfoMetadata beanInfoMetadata) {
	List<FieldMetadata> fields = new ArrayList<FieldMetadata>();
	for (MethodMetadata method : beanInfoMetadata.getPublicAccessors(false)) {
	    JavaSymbolName propertyName = BeanInfoMetadata
		    .getPropertyNameForJavaBeanMethod(method);
	    FieldMetadata field = beanInfoMetadata
		    .getFieldForPropertyName(propertyName);

	    if (field != null && hasMutator(field, beanInfoMetadata)) {

		// Never include id field (it shouldn't normally have a mutator
		// anyway, but the user might have added one)
		if (MemberFindingUtils
			.getAnnotationOfType(field.getAnnotations(),
				new JavaType("javax.persistence.Id")) != null) {
		    continue;
		}
		// Never include version field (it shouldn't normally have a
		// mutator anyway, but the user might have added one)
		if (MemberFindingUtils.getAnnotationOfType(field
			.getAnnotations(), new JavaType(
			"javax.persistence.Version")) != null) {
		    continue;
		}
		fields.add(field);
	    }
	}
	return fields;
    }

    private boolean hasMutator(FieldMetadata fieldMetadata,
	    BeanInfoMetadata beanInfoMetadata) {
	for (MethodMetadata mutator : beanInfoMetadata.getPublicMutators()) {
	    if (fieldMetadata.equals(beanInfoMetadata
		    .getFieldForPropertyName(BeanInfoMetadata
			    .getPropertyNameForJavaBeanMethod(mutator))))
		return true;
	}
	return false;
    }

    class StatusListener implements ProcessManagerStatusListener {

	PaginatedRelationMetadataListener paginatedRelationMetadataListener;

	public StatusListener(
		PaginatedRelationMetadataListener paginatedRelationMetadataListener) {
	    super();
	    this.paginatedRelationMetadataListener = paginatedRelationMetadataListener;
	}

	public void onProcessManagerStatusChange(
		ProcessManagerStatus oldStatus, ProcessManagerStatus newStatus) {
	    if (newStatus == ProcessManagerStatus.AVAILABLE) {

		// logger.warning("Operations delayed.");
		paginatedRelationMetadataListener.performDelayed();
	    }

	}

    }

    /**
     * Execute delayed operations.
     * 
     */
    public void performDelayed() {

	// Work out the MIDs of the other metadata we depend on
	String annotationPath = "javax.persistence.OneToMany";

	Iterator<String> iter = upstreamDependencyList.iterator();
	if (!iter.hasNext()) {
	    return;
	}

	String upstreamDependency;

	while (iter.hasNext()) {
	    upstreamDependency = iter.next();

	    List<FieldMetadata> oneToManyFieldMetadatas = getAnnotatedFields(
		    upstreamDependency, annotationPath);

	    // Retrieve the associated jspx (show an update).

	    Document showDocument = updateView("show", oneToManyFieldMetadatas,
		    "tab");
	    if (showDocument != null) {
		writeToDiskIfNecessary("show", showDocument);
	    }

	    Document updateDocument = updateView("update",
		    oneToManyFieldMetadatas, "tab");
	    if (updateDocument != null) {
		writeToDiskIfNecessary("update", updateDocument);
	    }

	    iter.remove();
	}

    }
}
