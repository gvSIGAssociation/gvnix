package org.gvnix.web.report.roo.addon;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.osgi.service.component.ComponentContext;
import org.springframework.roo.addon.propfiles.PropFileOperations;
import org.springframework.roo.addon.web.mvc.controller.details.WebMetadataService;
import org.springframework.roo.addon.web.mvc.controller.scaffold.mvc.WebScaffoldMetadata;
import org.springframework.roo.addon.web.mvc.controller.scaffold.mvc.WebScaffoldMetadataProvider;
import org.springframework.roo.classpath.PhysicalTypeIdentifier;
import org.springframework.roo.classpath.PhysicalTypeMetadata;
import org.springframework.roo.classpath.PhysicalTypeMetadataProvider;
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
import org.springframework.roo.project.Path;
import org.springframework.roo.project.ProjectOperations;
import org.springframework.roo.support.logging.HandlerUtils;
import org.springframework.roo.support.util.Assert;

/**
 * Provides {@link ReportMetadata}. This type is called by Roo to retrieve the
 * metadata for this add-on. Use this type to reference external types and
 * services needed by the metadata type. Register metadata triggers and
 * dependencies here. Also define the unique add-on ITD identifier.
 *
 * @since 1.1
 */
@Component(immediate = true)
@Service
public final class ReportMetadataProvider extends AbstractItdMetadataProvider {
    private static final Logger logger = HandlerUtils.getLogger(ReportMetadataProvider.class);

    @Reference
    TypeLocationService typeLocationService;

    @Reference
    WebScaffoldMetadataProvider webScaffoldMetadataProvider;

    @Reference
    ProjectOperations projectOperations;

    @Reference
    PhysicalTypeMetadataProvider physicalTypeMetadataProvider;

    @Reference
    PropFileOperations propFileOperations;

    @Reference
    WebMetadataService webMetadataService;

    @Reference
    ReportConfigService reportConfigService;

    /**
     * The activate method for this OSGi component, this will be called by the
     * OSGi container upon bundle activation (result of the 'addon install'
     * command)
     *
     * @param context
     *            the component context can be used to get access to the OSGi
     *            container (ie find out if certain bundles are active)
     */
    protected void activate(ComponentContext context) {
        metadataDependencyRegistry.registerDependency(
                PhysicalTypeIdentifier.getMetadataIdentiferType(),
                getProvidesType());
        webScaffoldMetadataProvider.addMetadataTrigger(new JavaType(
                GvNIXReports.class.getName()));
        addMetadataTrigger(new JavaType(GvNIXReports.class.getName()));
    }

    /**
     * The deactivate method for this OSGi component, this will be called by the
     * OSGi container upon bundle deactivation (result of the 'addon uninstall'
     * command)
     *
     * @param context
     *            the component context can be used to get access to the OSGi
     *            container (ie find out if certain bundles are active)
     */
    protected void deactivate(ComponentContext context) {
        metadataDependencyRegistry.deregisterDependency(
                PhysicalTypeIdentifier.getMetadataIdentiferType(),
                getProvidesType());
        webScaffoldMetadataProvider.removeMetadataTrigger(new JavaType(
                GvNIXReports.class.getName()));
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
        reportConfigService.setup();

        JavaType javaType = ReportMetadata
                .getJavaType(metadataIdentificationString);

        // We know governor type details are non-null and can be safely cast
        ClassOrInterfaceTypeDetails controllerClassOrInterfaceDetails = (ClassOrInterfaceTypeDetails) governorPhysicalTypeMetadata
                .getMemberHoldingTypeDetails();
        Assert.notNull(
                controllerClassOrInterfaceDetails,
                "Governor failed to provide class type details, in violation of superclass contract");
        MemberDetails controllerMemberDetails = memberDetailsScanner
                .getMemberDetails(getClass().getName(),
                        controllerClassOrInterfaceDetails);

        List<StringAttributeValue> definedReports = new ArrayList<StringAttributeValue>();

        AnnotationMetadata gvNixReportsAnnotation = MemberFindingUtils
                .getAnnotationOfType(
                        controllerClassOrInterfaceDetails.getAnnotations(),
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
                HashMap<String, String> validReportNameFormats = getValidReportNameFormats(arrayVal);
                /*
                 * Dealing with reportNames defined several times in the GvNIX
                 * annotation. With the validReportNameFormats HashMap we can
                 * know if a report is defined more than once and handle it as
                 * just one definition aggregating the formats defined in each
                 * report definition.
                 * TODO: would be a great improvement to advise user about duplicity of report definitions
                 */
                for (String reportName : validReportNameFormats.keySet()) {
                    StringAttributeValue newSV = new StringAttributeValue(
                            new JavaSymbolName("ignored"),
                            reportName.toLowerCase()
                                    + "|"
                                    + validReportNameFormats.get(reportName
                                            .toLowerCase()));
                    definedReports.add(newSV);
                }
            }
        }

        Path path = ReportMetadata.getPath(metadataIdentificationString);
        String webScaffoldMetadataKey = WebScaffoldMetadata.createIdentifier(
                javaType, path);
        metadataDependencyRegistry.registerDependency(webScaffoldMetadataKey,
                metadataIdentificationString);
        WebScaffoldMetadata webScaffoldMetadata = (WebScaffoldMetadata) webScaffoldMetadataProvider
                .get(webScaffoldMetadataKey);
        if (webScaffoldMetadata == null) {
            logger.warning("The report can not be created over a Controlloer without " +
                            "@RooWebScaffold annotation and its 'fromBackingObject' attribute " +
                            "set. Check " + javaType.getFullyQualifiedTypeName());
            return null;
        }

        // Pass dependencies required by the metadata in through its constructor
        return new ReportMetadata(metadataIdentificationString, aspectName,
                governorPhysicalTypeMetadata,
                MemberFindingUtils.getMethods(controllerMemberDetails),
                metadataService, memberDetailsScanner,
                metadataDependencyRegistry, webScaffoldMetadata,
                webMetadataService, fileManager,
                projectOperations, propFileOperations, definedReports);
    }

    /**
     * Returns a HashMap<String, String>. Key is reportName; Value is the csv of
     * formats.
     *
     * @param arrayVal
     * @return
     */
    private HashMap<String, String> getValidReportNameFormats(
            ArrayAttributeValue<?> arrayVal) {
        HashMap<String, String> validReportNameFormats = new HashMap<String, String>();
        StringAttributeValue sv = null;
        String[] reportNameFormat = {};
        for (Object o : arrayVal.getValue()) {
            if (!(o instanceof StringAttributeValue)) {
                return null;
            }
            sv = (StringAttributeValue) o;
            reportNameFormat = ReportMetadata.stripGvNixReportValue(sv
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
                } else {
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
        Path path = ReportMetadata.getPath(metadataIdentificationString);
        return PhysicalTypeIdentifier.createIdentifier(javaType, path);
    }

    @Override
    protected String createLocalIdentifier(JavaType javaType, Path path) {
        return ReportMetadata.createIdentifier(javaType, path);
    }

    public String getProvidesType() {
        return ReportMetadata.getMetadataIdentiferType();
    }

}
