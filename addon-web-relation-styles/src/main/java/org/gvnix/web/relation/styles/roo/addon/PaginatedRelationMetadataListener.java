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

import java.util.List;
import java.util.logging.Logger;

import org.apache.felix.scr.annotations.*;
import org.osgi.service.component.ComponentContext;
import org.springframework.roo.addon.entity.EntityMetadata;
import org.springframework.roo.addon.mvc.jsp.JspMetadata;
import org.springframework.roo.addon.web.mvc.controller.WebScaffoldMetadata;
import org.springframework.roo.classpath.details.*;
import org.springframework.roo.metadata.*;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.project.Path;
import org.w3c.dom.Document;

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

    protected ClassOrInterfaceTypeDetails governorTypeDetails;

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

	    logger.warning("JspMetadata retrieved.");
	    // JavaType javaType = JspMetadata.getJavaType(upstreamDependency);

	    // org.gvnix.test.relation.list.table.web.NuevoController
	    // @RooWebScaffold(path = "cars", formBackingObject = Car.class)
	    // @OneToMany(cascade = CascadeType.ALL, mappedBy = "person")

	    // Work out the MIDs of the other metadata we depend on

	    JavaType javaType = JspMetadata.getJavaType(upstreamDependency);
	    Path webPath = JspMetadata.getPath(upstreamDependency);
	    String webScaffoldMetadataKey = WebScaffoldMetadata
		    .createIdentifier(javaType, webPath);
	    WebScaffoldMetadata webScaffoldMetadata = (WebScaffoldMetadata) metadataService
		    .get(webScaffoldMetadataKey);

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


	    DefaultItdTypeDetails defaultItdTypeDetails = (DefaultItdTypeDetails) entityMetadata
		    .getItdTypeDetails();

	    
	    // Retrieve the fields that are defined as OneToMany relationship.
	    List<FieldMetadata> fieldMetadataList = MemberFindingUtils
		    .getFieldsWithAnnotation(defaultItdTypeDetails
			    .getGovernor(), new JavaType(
			    "javax.persistence.OneToMany"));

	    logger.warning("Entity field anotated with @oneToMany:\n"
		    + fieldMetadataList.size());
	}

    }

    /** return indicates if disk file was changed (ie updated or created) */
    private boolean writeToDiskIfNecessary(String jspFilename, Document proposed) {
	// TODO:
	return true;
    }

}
