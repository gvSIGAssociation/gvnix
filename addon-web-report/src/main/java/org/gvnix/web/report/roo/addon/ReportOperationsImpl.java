/*
 * gvNIX. Spring Roo based RAD tool for Conselleria d'Infraestructures
 * i Transport - Generalitat Valenciana
 * Copyright (C) 2010, 2011 CIT - Generalitat Valenciana
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
package org.gvnix.web.report.roo.addon;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.logging.Logger;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.springframework.roo.addon.web.mvc.controller.RooWebScaffold;
import org.springframework.roo.classpath.PhysicalTypeDetails;
import org.springframework.roo.classpath.PhysicalTypeIdentifier;
import org.springframework.roo.classpath.PhysicalTypeMetadata;
import org.springframework.roo.classpath.PhysicalTypeMetadataProvider;
import org.springframework.roo.classpath.details.MemberFindingUtils;
import org.springframework.roo.classpath.details.MutableClassOrInterfaceTypeDetails;
import org.springframework.roo.classpath.details.annotations.AnnotationAttributeValue;
import org.springframework.roo.classpath.details.annotations.AnnotationMetadata;
import org.springframework.roo.classpath.details.annotations.AnnotationMetadataBuilder;
import org.springframework.roo.classpath.details.annotations.ArrayAttributeValue;
import org.springframework.roo.classpath.details.annotations.StringAttributeValue;
import org.springframework.roo.metadata.MetadataService;
import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.project.ProjectOperations;
import org.springframework.roo.support.logging.HandlerUtils;
import org.springframework.roo.support.util.Assert;
import org.springframework.roo.support.util.StringUtils;

/**
 * Implementation of operations this add-on offers.
 *
 * @author Oscar Rovira (orovira at disid dot com) at <a
 *         href="http://www.disid.com">DiSiD Technologies S.L.</a> made for <a
 *         href="http://www.cit.gva.es">Conselleria d'Infraestructures i
 *         Transport</a>
 * @since 0.6
 */
@Component // Use these Apache Felix annotations to register your commands class in the Roo container
@Service
public class ReportOperationsImpl implements ReportOperations {
    private static final Logger logger = HandlerUtils
            .getLogger(ReportOperationsImpl.class);

    private static final JavaType GVNIX_REPORTS = new JavaType(
            GvNIXReports.class.getName());

    /**
     * MetadataService offers access to Roo's metadata model, use it to retrieve
     * any available metadata by its MID
     */
    @Reference
    private MetadataService metadataService;

    /**
     * Use the PhysicalTypeMetadataProvider to access information about a
     * physical type in the project
     */
    @Reference
    private PhysicalTypeMetadataProvider physicalTypeMetadataProvider;

    /**
     * Use ProjectOperations to install new dependencies, plugins, properties,
     * etc into the project configuration
     */
    @Reference
    private ProjectOperations projectOperations;

    /**
     * ReportConfigService offers some methods for configuration purposes
     */
    @Reference
    ReportConfigService reportConfigService;


    /** {@inheritDoc} */
    public boolean isCommandAvailable() {
        // Check if a project has been created
        return projectOperations.isProjectAvailable();
    }

    /** {@inheritDoc} */
    public boolean isProjectAvailable() {
        // Check if a project has been created
        return projectOperations.isProjectAvailable();
    }


    /** {@inheritDoc} */
    public void annotateType(JavaType javaType, String reportName, String format) {
        Assert.isTrue(reportConfigService.isSpringMvcProject(),
                "Project must be Spring MVC project");
        reportConfigService.addJasperReportsViewResolver();
        Assert.isTrue(reportConfigService.isJasperViewsProject(),
                "WEB-INF/spring/jasper-views.xml must exists");

        // Use Roo's Assert type for null checks
        Assert.notNull(javaType, "Java type required");
        Assert.isTrue(StringUtils.hasText(reportName), "Report Name required");
        Assert.isTrue(StringUtils.hasText(format), "Report Name required");
        reportName = reportName.toLowerCase();
        format = format.replaceAll(" ", "").toLowerCase();
        Assert.isTrue(ReportMetadata.isValidFormat(format),
                "Format must be pdf,xls,csv,html");

        // Retrieve metadata for the Java source type the annotation is being
        // added to
        String id = physicalTypeMetadataProvider.findIdentifier(javaType);
        if (id == null) {
            throw new IllegalArgumentException("Cannot locate source for '"
                    + javaType.getFullyQualifiedTypeName() + "'");
        }

        // Obtain the physical type and itd mutable details
        PhysicalTypeMetadata physicalTypeMetadata = (PhysicalTypeMetadata) metadataService
                .get(id);
        Assert.notNull(physicalTypeMetadata,
                "Java source code unavailable for type "
                        + PhysicalTypeIdentifier.getFriendlyName(id));

        // Obtain physical type details for the target type
        PhysicalTypeDetails physicalTypeDetails = physicalTypeMetadata
                .getMemberHoldingTypeDetails();
        Assert.notNull(physicalTypeDetails,
                "Java source code details unavailable for type "
                        + PhysicalTypeIdentifier.getFriendlyName(id));

        // Test if the type is an MutableClassOrInterfaceTypeDetails instance so
        // the annotation can be added
        Assert.isInstanceOf(MutableClassOrInterfaceTypeDetails.class,
                physicalTypeDetails, "Java source code is immutable for type "
                        + PhysicalTypeIdentifier.getFriendlyName(id));
        MutableClassOrInterfaceTypeDetails mutableTypeDetails = (MutableClassOrInterfaceTypeDetails) physicalTypeDetails;

        AnnotationMetadata rooWebScaffoldAnnotation = MemberFindingUtils
                .getAnnotationOfType(mutableTypeDetails.getAnnotations(),
                        new JavaType(RooWebScaffold.class.getName()));
        if (rooWebScaffoldAnnotation == null) {
            logger.warning("The report can not be created over a Controlloer without "
                    + "@RooWebScaffold annotation and its 'fromBackingObject' attribute "
                    + "set.");
            return;
        }

        // Make a destination list to store our final attributes
        List<AnnotationAttributeValue<?>> attributes = new ArrayList<AnnotationAttributeValue<?>>();
        List<StringAttributeValue> desiredReports = new ArrayList<StringAttributeValue>();

        // Test if the annotation arlready exists on the target type and update
        // reports attribute if the new reportName is not defined
        AnnotationMetadata gvNixReportsAnnotation = MemberFindingUtils
                .getAnnotationOfType(mutableTypeDetails.getAnnotations(),
                        new JavaType(GvNIXReports.class.getName()));
        boolean alreadyAdded = false;
        if (gvNixReportsAnnotation != null) {
            AnnotationAttributeValue<?> val = gvNixReportsAnnotation
                    .getAttribute(new JavaSymbolName("value"));

            if (val != null) {
                // Ensure we have an array of strings
                if (!(val instanceof ArrayAttributeValue<?>)) {
                    logger.warning(getErrorMsg());
                    return;
                }
                ArrayAttributeValue<?> arrayVal = (ArrayAttributeValue<?>) val;
                for (Object o : arrayVal.getValue()) {
                    if (!(o instanceof StringAttributeValue)) {
                        logger.warning(getErrorMsg());
                        return;
                    }
                    StringAttributeValue sv = (StringAttributeValue) o;
                    if (sv.getValue().equals(reportName + "|" + format)) {
                        logger.warning("Report " + reportName + " with format "
                                + format + " is already defined in "
                                + javaType.getSimpleTypeName());
                        return;
                    }
                    if (sv.getValue().contains(reportName + "|")) {
                        String oldFormat = sv.getValue().split("\\|")[1];
                        sv = new StringAttributeValue(new JavaSymbolName(
                                "ignored"), sv.getValue().replace(oldFormat,
                                ReportMetadata.updateFormat(oldFormat, format)));
                        alreadyAdded = true;
                    }
                    desiredReports.add(sv);
                }
            }
        }

        if (!alreadyAdded) {
            desiredReports.add(new StringAttributeValue(new JavaSymbolName(
                    "ignored"), reportName + "|" + format));
        }

        attributes.add(new ArrayAttributeValue<StringAttributeValue>(
                new JavaSymbolName("value"), desiredReports));
        AnnotationMetadataBuilder annotationBuilder = new AnnotationMetadataBuilder(
                GVNIX_REPORTS, attributes);
        mutableTypeDetails.updateTypeAnnotation(annotationBuilder.build(),
                new HashSet<JavaSymbolName>());

    }

    private String getErrorMsg() {
        return "Annotation " + GVNIX_REPORTS.getSimpleTypeName()
                + " attribute 'value' must be an array of strings";
    }
}
