/*
 * gvNIX is an open source tool for rapid application development (RAD).
 * Copyright (C) 2010 Generalitat Valenciana
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

package org.gvnix.web.report.roo.addon.addon;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;

import org.apache.commons.lang3.Validate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;
import org.gvnix.web.report.roo.addon.annotations.GvNIXReports;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.ComponentContext;
import org.springframework.roo.addon.propfiles.PropFileOperations;
import org.springframework.roo.addon.web.mvc.controller.addon.details.WebMetadataService;
import org.springframework.roo.addon.web.mvc.controller.addon.scaffold.WebScaffoldMetadata;
import org.springframework.roo.addon.web.mvc.controller.addon.scaffold.WebScaffoldMetadataProvider;
import org.springframework.roo.classpath.PhysicalTypeIdentifier;
import org.springframework.roo.classpath.PhysicalTypeMetadata;
import org.springframework.roo.classpath.TypeLocationService;
import org.springframework.roo.classpath.details.ClassOrInterfaceTypeDetails;
import org.springframework.roo.classpath.details.MemberFindingUtils;
import org.springframework.roo.classpath.details.annotations.AnnotationAttributeValue;
import org.springframework.roo.classpath.details.annotations.AnnotationMetadata;
import org.springframework.roo.classpath.details.annotations.ArrayAttributeValue;
import org.springframework.roo.classpath.details.annotations.StringAttributeValue;
import org.springframework.roo.classpath.itd.AbstractItdMetadataProvider;
import org.springframework.roo.classpath.itd.ItdTypeDetailsProvidingMetadataItem;
import org.springframework.roo.classpath.scanner.MemberDetails;
import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.project.LogicalPath;
import org.springframework.roo.project.ProjectOperations;
import org.springframework.roo.support.logging.HandlerUtils;

/**
 * Provides {@link ReportMetadata}. This type is called by Roo to retrieve the
 * metadata for this add-on. Use this type to reference external types and
 * services needed by the metadata type. Register metadata triggers and
 * dependencies here. Also define the unique add-on ITD identifier.
 * 
 * @author <a href="http://www.disid.com">DISID Corporation S.L.</a> made for <a
 *         href="http://www.dgti.gva.es">General Directorate for Information
 *         Technologies (DGTI)</a>
 * @since 1.1
 */
@Component
@Service
public final class ReportMetadataProvider extends AbstractItdMetadataProvider {
    private static final Logger logger = HandlerUtils
            .getLogger(ReportMetadataProvider.class);

    protected TypeLocationService typeLocationService;

    protected WebScaffoldMetadataProvider webScaffoldMetadataProvider;

    protected ProjectOperations projectOperations;

    protected PropFileOperations propFileOperations;

    protected WebMetadataService webMetadataService;

    protected ReportConfigService reportConfigService;

    /**
     * The activate method for this OSGi component, this will be called by the
     * OSGi container upon bundle activation (result of the 'addon install'
     * command)
     * 
     * @param context the component context can be used to get access to the
     *        OSGi container (ie find out if certain bundles are active)
     */
    protected void activate(ComponentContext cContext) {
        context = cContext.getBundleContext();
        getMetadataDependencyRegistry().registerDependency(
                PhysicalTypeIdentifier.getMetadataIdentiferType(),
                getProvidesType());
        getWebScaffoldMetadataProvider().addMetadataTrigger(
                new JavaType(GvNIXReports.class.getName()));
        addMetadataTrigger(new JavaType(GvNIXReports.class.getName()));
    }

    /**
     * The deactivate method for this OSGi component, this will be called by the
     * OSGi container upon bundle deactivation (result of the 'addon uninstall'
     * command)
     * 
     * @param context the component context can be used to get access to the
     *        OSGi container (ie find out if certain bundles are active)
     */
    protected void deactivate(ComponentContext context) {
        getMetadataDependencyRegistry().deregisterDependency(
                PhysicalTypeIdentifier.getMetadataIdentiferType(),
                getProvidesType());
        getWebScaffoldMetadataProvider().removeMetadataTrigger(
                new JavaType(GvNIXReports.class.getName()));
        removeMetadataTrigger(new JavaType(GvNIXReports.class.getName()));
    }

    /**
     * Return an instance of the Metadata offered by this add-on
     */
    @Override
    protected ItdTypeDetailsProvidingMetadataItem getMetadata(
            String metadataIdentificationString, JavaType aspectName,
            PhysicalTypeMetadata governorPhysicalTypeMetadata,
            String itdFilename) {
        // Setup JasperReports support
        getReportConfigService().setup();

        JavaType javaType = ReportMetadata
                .getJavaType(metadataIdentificationString);

        // We know governor type details are non-null and can be safely cast
        ClassOrInterfaceTypeDetails controllerCoID = (ClassOrInterfaceTypeDetails) governorPhysicalTypeMetadata
                .getMemberHoldingTypeDetails();
        Validate.notNull(
                controllerCoID,
                "Governor failed to provide class type details, in violation of superclass contract");
        MemberDetails controllerMemberDetails = getMemberDetailsScanner()
                .getMemberDetails(getClass().getName(), controllerCoID);

        List<StringAttributeValue> definedReports = new ArrayList<StringAttributeValue>();

        AnnotationMetadata gvNixReportsAnnotation = MemberFindingUtils
                .getAnnotationOfType(controllerCoID.getAnnotations(),
                        new JavaType(GvNIXReports.class.getName()));
        if (gvNixReportsAnnotation != null) {
            AnnotationAttributeValue<?> val = gvNixReportsAnnotation
                    .getAttribute(new JavaSymbolName("value"));

            if (val != null) {
                // Ensure we have an array of strings
                if (!(val instanceof ArrayAttributeValue<?>)) {
                    // logger.warning(getErrorMsg());
                    return null;
                }

                ArrayAttributeValue<?> arrayVal = (ArrayAttributeValue<?>) val;
                Map<String, String> validReportNameFormats = getValidReportNameFormats(arrayVal);
                /*
                 * Dealing with reportNames defined several times in the gvNIX
                 * annotation. With the validReportNameFormats HashMap we can
                 * know if a report is defined more than once and handle it as
                 * just one definition aggregating the formats defined in each
                 * report definition. TODO: would be a great improvement to
                 * advise user about duplicity of report definitions
                 */
                for (Entry<String, String> reportEntry : validReportNameFormats
                        .entrySet()) {
                    String reportName = reportEntry.getKey().toLowerCase();
                    StringAttributeValue newSV = new StringAttributeValue(
                            new JavaSymbolName("ignored"), reportName.concat(
                                    "|").concat(
                                    validReportNameFormats.get(reportName)));
                    definedReports.add(newSV);
                }
            }
        }

        LogicalPath path = ReportMetadata.getPath(metadataIdentificationString);
        String webScaffoldMetadataKey = WebScaffoldMetadata.createIdentifier(
                javaType, path);
        getMetadataDependencyRegistry().registerDependency(
                webScaffoldMetadataKey, metadataIdentificationString);
        WebScaffoldMetadata webScaffoldMetadata = (WebScaffoldMetadata) getWebScaffoldMetadataProvider()
                .get(webScaffoldMetadataKey);
        if (webScaffoldMetadata == null) {
            logger.warning("The report can not be created over a Controlloer without "
                    + "@RooWebScaffold annotation and its 'fromBackingObject' attribute "
                    + "set. Check " + javaType.getFullyQualifiedTypeName());
            return null;
        }

        // Pass dependencies required by the metadata in through its constructor
        return new ReportMetadata(metadataIdentificationString, aspectName,
                governorPhysicalTypeMetadata,
                controllerMemberDetails.getMethods(), getMetadataService(),
                getMemberDetailsScanner(), getMetadataDependencyRegistry(),
                webScaffoldMetadata, getWebMetadataService(), getFileManager(),
                getProjectOperations(), getPropFileOperations(), definedReports);
    }

    /**
     * Returns a HashMap<String, String>. Key is reportName; Value is the csv of
     * formats.
     * 
     * @param arrayVal
     * @return
     */
    private Map<String, String> getValidReportNameFormats(
            ArrayAttributeValue<?> arrayVal) {
        HashMap<String, String> validReportNameFormats = new HashMap<String, String>();
        StringAttributeValue sAttrValue = null;
        String[] reportNameFormat;
        for (Object o : arrayVal.getValue()) {
            if (!(o instanceof StringAttributeValue)) {
                return null;
            }
            sAttrValue = (StringAttributeValue) o;
            reportNameFormat = ReportMetadata.stripGvNixReportValue(sAttrValue
                    .getValue());
            if (ReportMetadata.isValidFormat(reportNameFormat[1])) {
                if (validReportNameFormats.containsKey(reportNameFormat[0])) {
                    // logger.warning("A report with the name ** " +
                    // reportNameFormat[0] +
                    // " ** is already defined. Please check it");
                    validReportNameFormats.put(reportNameFormat[0],
                            ReportMetadata.updateFormat(validReportNameFormats
                                    .get(reportNameFormat[0]),
                                    reportNameFormat[1]));
                }
                else {
                    validReportNameFormats.put(reportNameFormat[0],
                            reportNameFormat[1]);
                }
            }
        }
        return validReportNameFormats;
    }

    /**
     * Define the unique ITD file name extension, here the resulting file name
     * will be **_ROO_GvNixReport.aj
     */
    public String getItdUniquenessFilenameSuffix() {
        return "GvNIXReport";
    }

    @Override
    protected String getGovernorPhysicalTypeIdentifier(
            String metadataIdentificationString) {
        JavaType javaType = ReportMetadata
                .getJavaType(metadataIdentificationString);
        LogicalPath path = ReportMetadata.getPath(metadataIdentificationString);
        return PhysicalTypeIdentifier.createIdentifier(javaType, path);
    }

    @Override
    protected String createLocalIdentifier(JavaType javaType, LogicalPath path) {
        return ReportMetadata.createIdentifier(javaType, path);
    }

    public String getProvidesType() {
        return ReportMetadata.getMetadataIdentiferType();
    }

    public WebScaffoldMetadataProvider getWebScaffoldMetadataProvider() {
        if (webScaffoldMetadataProvider == null) {
            // Get all Services implement WebScaffoldMetadataProvider interface
            try {
                ServiceReference<?>[] references = this.context
                        .getAllServiceReferences(
                                WebScaffoldMetadataProvider.class.getName(),
                                null);

                for (ServiceReference<?> ref : references) {
                    return (WebScaffoldMetadataProvider) this.context
                            .getService(ref);
                }

                return null;

            }
            catch (InvalidSyntaxException e) {
                logger.warning("Cannot load WebScaffoldMetadataProvider on ReportMetadataProvider.");
                return null;
            }
        }
        else {
            return webScaffoldMetadataProvider;
        }

    }

    public ProjectOperations getProjectOperations() {
        if (projectOperations == null) {
            // Get all Services implement ProjectOperations interface
            try {
                ServiceReference<?>[] references = this.context
                        .getAllServiceReferences(
                                ProjectOperations.class.getName(), null);

                for (ServiceReference<?> ref : references) {
                    return (ProjectOperations) this.context.getService(ref);
                }

                return null;

            }
            catch (InvalidSyntaxException e) {
                logger.warning("Cannot load ProjectOperations on ReportMetadataProvider.");
                return null;
            }
        }
        else {
            return projectOperations;
        }

    }

    public PropFileOperations getPropFileOperations() {
        if (propFileOperations == null) {
            // Get all Services implement PropFileOperations interface
            try {
                ServiceReference<?>[] references = this.context
                        .getAllServiceReferences(
                                PropFileOperations.class.getName(), null);

                for (ServiceReference<?> ref : references) {
                    return (PropFileOperations) this.context.getService(ref);
                }

                return null;

            }
            catch (InvalidSyntaxException e) {
                logger.warning("Cannot load PropFileOperations on ReportMetadataProvider.");
                return null;
            }
        }
        else {
            return propFileOperations;
        }

    }

    public WebMetadataService getWebMetadataService() {
        if (webMetadataService == null) {
            // Get all Services implement WebMetadataService interface
            try {
                ServiceReference<?>[] references = this.context
                        .getAllServiceReferences(
                                WebMetadataService.class.getName(), null);

                for (ServiceReference<?> ref : references) {
                    return (WebMetadataService) this.context.getService(ref);
                }

                return null;

            }
            catch (InvalidSyntaxException e) {
                logger.warning("Cannot load WebMetadataService on ReportMetadataProvider.");
                return null;
            }
        }
        else {
            return webMetadataService;
        }

    }

    public ReportConfigService getReportConfigService() {
        if (reportConfigService == null) {
            // Get all Services implement ReportConfigService interface
            try {
                ServiceReference<?>[] references = this.context
                        .getAllServiceReferences(
                                ReportConfigService.class.getName(), null);

                for (ServiceReference<?> ref : references) {
                    return (ReportConfigService) this.context.getService(ref);
                }

                return null;

            }
            catch (InvalidSyntaxException e) {
                logger.warning("Cannot load ReportConfigService on ReportMetadataProvider.");
                return null;
            }
        }
        else {
            return reportConfigService;
        }

    }

}
