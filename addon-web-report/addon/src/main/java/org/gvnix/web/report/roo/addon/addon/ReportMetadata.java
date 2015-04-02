/*
 * gvNIX. Spring Roo based RAD tool for Conselleria d'Infraestructures i
 * Transport - Generalitat Valenciana Copyright (C) 2010, 2011 CIT - Generalitat
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
package org.gvnix.web.report.roo.addon.addon;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.gvnix.web.report.roo.addon.addon.util.ReportValidTypes;
import org.gvnix.web.report.roo.addon.annotations.GvNIXReports;
import org.springframework.roo.addon.propfiles.PropFileOperations;
import org.springframework.roo.addon.web.mvc.controller.addon.details.WebMetadataService;
import org.springframework.roo.addon.web.mvc.controller.addon.scaffold.WebScaffoldAnnotationValues;
import org.springframework.roo.addon.web.mvc.controller.addon.scaffold.WebScaffoldMetadata;
import org.springframework.roo.classpath.PhysicalTypeIdentifier;
import org.springframework.roo.classpath.PhysicalTypeIdentifierNamingUtils;
import org.springframework.roo.classpath.PhysicalTypeMetadata;
import org.springframework.roo.classpath.details.ClassOrInterfaceTypeDetails;
import org.springframework.roo.classpath.details.FieldMetadata;
import org.springframework.roo.classpath.details.ItdTypeDetailsBuilder;
import org.springframework.roo.classpath.details.MethodMetadata;
import org.springframework.roo.classpath.details.MethodMetadataBuilder;
import org.springframework.roo.classpath.details.annotations.AnnotatedJavaType;
import org.springframework.roo.classpath.details.annotations.AnnotationAttributeValue;
import org.springframework.roo.classpath.details.annotations.AnnotationMetadata;
import org.springframework.roo.classpath.details.annotations.AnnotationMetadataBuilder;
import org.springframework.roo.classpath.details.annotations.BooleanAttributeValue;
import org.springframework.roo.classpath.details.annotations.EnumAttributeValue;
import org.springframework.roo.classpath.details.annotations.StringAttributeValue;
import org.springframework.roo.classpath.itd.AbstractItdTypeDetailsProvidingMetadataItem;
import org.springframework.roo.classpath.itd.InvocableMemberBodyBuilder;
import org.springframework.roo.classpath.scanner.MemberDetails;
import org.springframework.roo.classpath.scanner.MemberDetailsScanner;
import org.springframework.roo.metadata.MetadataDependencyRegistry;
import org.springframework.roo.metadata.MetadataIdentificationUtils;
import org.springframework.roo.metadata.MetadataService;
import org.springframework.roo.model.EnumDetails;
import org.springframework.roo.model.ImportRegistrationResolver;
import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.process.manager.FileManager;
import org.springframework.roo.process.manager.MutableFile;
import org.springframework.roo.project.LogicalPath;
import org.springframework.roo.project.Path;
import org.springframework.roo.project.PathResolver;
import org.springframework.roo.project.ProjectOperations;
import org.springframework.roo.support.util.FileUtils;
import org.springframework.roo.support.util.XmlElementBuilder;
import org.springframework.roo.support.util.XmlUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * This type produces metadata for a new ITD. It uses an
 * {@link ItdTypeDetailsBuilder} provided by
 * {@link AbstractItdTypeDetailsProvidingMetadataItem} to register a field in
 * the ITD and a new method.
 * 
 * @author Oscar Rovira (orovira at disid dot com) at <a
 *         href="http://www.disid.com">DiSiD Technologies S.L.</a> made for <a
 *         href="http://www.cit.gva.es">Conselleria d'Infraestructures i
 *         Transport</a>
 * @since 0.6
 */
public class ReportMetadata extends AbstractItdTypeDetailsProvidingMetadataItem {
    private static final JavaSymbolName METHOD_SYMBOL_NAME = new JavaSymbolName(
            "method");

    private static final JavaSymbolName PARAMS_SYMBOL_NAME = new JavaSymbolName(
            "params");

    private static final JavaSymbolName VALUE_SYMBOL_NAME = new JavaSymbolName(
            "value");

    private static final JavaType REQUEST_PARAM_TYPE = new JavaType(
            "org.springframework.web.bind.annotation.RequestParam");

    private static final JavaType MODEL_TYPE = new JavaType(
            "org.springframework.ui.Model");

    private static final JavaType REQUEST_METHOD_TYPE = new JavaType(
            "org.springframework.web.bind.annotation.RequestMethod");
    // private static final Logger logger =
    // HandlerUtils.getLogger(ReportMetadata.class);

    private static final JavaType REQUEST_MAPPING_TYPE = new JavaType(
            "org.springframework.web.bind.annotation.RequestMapping");
    private static final String PROVIDES_TYPE_STRING = ReportMetadata.class
            .getName();
    private static final String PROVIDES_TYPE = MetadataIdentificationUtils
            .create(PROVIDES_TYPE_STRING);
    private List<MethodMetadata> reportMethods;
    private List<MethodMetadata> controllerMethods;
    private List<String> installedReports;
    private final WebScaffoldMetadata webScaffoldMetadata;
    private final FileManager fileManager;
    private final ProjectOperations projectOperations;
    private final WebScaffoldAnnotationValues annotationValues;
    private final PropFileOperations propFileOperations;
    private final MetadataService metadataService;
    private final MemberDetailsScanner memberDetailsScanner;
    private final WebMetadataService webMetadataService;

    public ReportMetadata(String identifier, JavaType aspectName,
            PhysicalTypeMetadata governorPhysicalTypeMetadata,
            List<MethodMetadata> controllerMethods,
            MetadataService metadataService,
            MemberDetailsScanner memberDetailsScanner,
            MetadataDependencyRegistry metadataDependencyRegistry,
            WebScaffoldMetadata webScaffoldMetadata,
            WebMetadataService webMetadataService, FileManager fileManager,
            ProjectOperations projectOperations,
            PropFileOperations propFileOperations,
            List<StringAttributeValue> definedReports) {
        super(identifier, aspectName, governorPhysicalTypeMetadata);
        Validate.notNull(controllerMethods,
                "List of controller methods required");

        this.fileManager = fileManager;
        this.projectOperations = projectOperations;
        this.propFileOperations = propFileOperations;
        this.metadataService = metadataService;
        this.webMetadataService = webMetadataService;
        this.memberDetailsScanner = memberDetailsScanner;

        Validate.isTrue(isValid(identifier), "Metadata identification string '"
                .concat(identifier).concat("' does not appear to be a valid"));

        this.annotationValues = new WebScaffoldAnnotationValues(
                governorPhysicalTypeMetadata);
        JavaType fromBackingObject = annotationValues.getFormBackingObject();

        this.webScaffoldMetadata = webScaffoldMetadata;
        this.controllerMethods = controllerMethods;

        List<MethodMetadata> reportMethods = new ArrayList<MethodMetadata>();
        List<String> installedReports = new ArrayList<String>();
        for (StringAttributeValue definedReport : definedReports) {
            String[] reportNameFormat = stripGvNixReportValue(definedReport
                    .getValue());

            // Add a sample JasperReport
            installJasperReportTemplate(reportNameFormat[0], fromBackingObject);

            // Add config to jasper-views.xml
            addNewJasperReportBean(fromBackingObject, reportNameFormat[0],
                    reportNameFormat[1]);

            // Add methodForm and method in the webScaffold
            MethodMetadata reportFormMethod = addGenerateReportFormMethod(
                    fromBackingObject, reportNameFormat[0], reportNameFormat[1]);
            reportMethods.add(reportFormMethod);
            MethodMetadata reportMethod = addGenerateReportMethod(
                    fromBackingObject, reportNameFormat[0], reportNameFormat[1]);
            reportMethods.add(reportMethod);
            installedReports.add(definedReport.getValue());
        }

        // Create a representation of the desired output ITD
        itdTypeDetails = builder.build();
        this.reportMethods = Collections.unmodifiableList(reportMethods);
        // TODO: Maybe this will disappear in the future
        this.installedReports = Collections.unmodifiableList(installedReports);
    }

    /**
     * Add to the aspect the MethodMetadata of the generateReportForm method. It
     * uses AbstractMetadataItem.builder for add the new method.
     * 
     * @param reportName
     * @param entity
     * @param reportMethods2
     * @return
     */
    private MethodMetadata addGenerateReportFormMethod(JavaType entity,
            String reportName, String reportFormats) {
        // Specify the desired method name
        JavaSymbolName methodName = new JavaSymbolName(generateMethodName(
                reportName, true));

        // Define method parameter types
        List<AnnotatedJavaType> parameterTypes = new ArrayList<AnnotatedJavaType>();
        parameterTypes.add(new AnnotatedJavaType(MODEL_TYPE,
                new ArrayList<AnnotationMetadata>()));

        // Check if a method with the same signature already exists in the
        // target type
        MethodMetadata reportMethod = reportMethodExists(methodName);
        if (reportMethod != null) {
            // If it already exists, just return the method and omit its
            // generation via the ITD
            return reportMethod;
        }

        // Define method parameter names
        List<JavaSymbolName> parameterNames = new ArrayList<JavaSymbolName>();
        parameterNames.add(new JavaSymbolName("uiModel"));

        // Define method annotations
        List<AnnotationAttributeValue<?>> requestMappingAttributes = new ArrayList<AnnotationAttributeValue<?>>();
        requestMappingAttributes.add(new StringAttributeValue(
                VALUE_SYMBOL_NAME, "/reports/".concat(reportName)));
        requestMappingAttributes.add(new StringAttributeValue(
                PARAMS_SYMBOL_NAME, "form"));
        requestMappingAttributes
                .add(new EnumAttributeValue(METHOD_SYMBOL_NAME,
                        new EnumDetails(REQUEST_METHOD_TYPE,
                                new JavaSymbolName("GET"))));
        AnnotationMetadataBuilder requestMapping = new AnnotationMetadataBuilder(
                REQUEST_MAPPING_TYPE, requestMappingAttributes);
        List<AnnotationMetadataBuilder> annotations = new ArrayList<AnnotationMetadataBuilder>();
        annotations.add(requestMapping);

        // Create the method body
        InvocableMemberBodyBuilder bodyBuilder = new InvocableMemberBodyBuilder();

        // Populate report_formats list for select
        String reportFormatsAsArray = getReportFormatsAsArray(reportFormats);
        bodyBuilder.appendFormalLine("String[] reportFormats =  ".concat(
                reportFormatsAsArray).concat(";"));
        bodyBuilder
                .appendFormalLine("Collection<String> reportFormatsList = Arrays.asList(reportFormats);");
        bodyBuilder
                .appendFormalLine("uiModel.addAttribute(\"report_formats\", reportFormatsList);");

        // return the View
        bodyBuilder.appendFormalLine("return \""
                .concat(annotationValues.getPath()).concat("/")
                .concat(reportName).concat("\";"));

        // ImportRegistrationResolver gives access to imports in the
        // Java/AspectJ source
        ImportRegistrationResolver irr = builder
                .getImportRegistrationResolver();
        irr.addImport(new JavaType("java.util.Arrays"));
        irr.addImport(new JavaType("java.util.Collection"));

        // Use the MethodMetadataBuilder for easy creation of MethodMetadata
        MethodMetadataBuilder methodBuilder = new MethodMetadataBuilder(
                getId(), Modifier.PUBLIC, methodName, JavaType.STRING,
                parameterTypes, parameterNames, bodyBuilder);
        methodBuilder.setAnnotations(annotations);

        reportMethod = methodBuilder.build();
        builder.addMethod(reportMethod);
        controllerMethods.add(reportMethod);
        return reportMethod;
    }

    /**
     * Returns a new MethodMetadata based on given parameters. It uses
     * AbstractMetadataItem.builder for add the new method and add needed
     * imports
     * 
     * @param reportName
     * @param entity
     * @param reportNameFormat
     * @return
     */
    private MethodMetadata addGenerateReportMethod(JavaType entity,
            String reportName, String reportFormats) {
        // Specify the desired method name
        Map<String, String> properties = new HashMap<String, String>();
        JavaSymbolName methodName = new JavaSymbolName(generateMethodName(
                reportName, false));

        List<AnnotationAttributeValue<?>> reportAttributes = new ArrayList<AnnotationAttributeValue<?>>();
        reportAttributes.add(new StringAttributeValue(VALUE_SYMBOL_NAME,
                "format"));
        reportAttributes.add(new BooleanAttributeValue(new JavaSymbolName(
                "required"), true));
        List<AnnotationMetadata> reportAttributesAnnotations = new ArrayList<AnnotationMetadata>();
        AnnotationMetadataBuilder reportAttributesAnnotation = new AnnotationMetadataBuilder(
                REQUEST_PARAM_TYPE, reportAttributes);
        reportAttributesAnnotations.add(reportAttributesAnnotation.build());

        // Define method parameter types
        List<AnnotatedJavaType> parameterTypes = new ArrayList<AnnotatedJavaType>();
        parameterTypes.add(new AnnotatedJavaType(new JavaType(String.class
                .getName()), reportAttributesAnnotations));
        parameterTypes.add(new AnnotatedJavaType(MODEL_TYPE,
                new ArrayList<AnnotationMetadata>()));

        // Check if a method with the same signature already exists in the
        // target type
        MethodMetadata reportMethod = reportMethodExists(methodName);
        if (reportMethod != null) {
            // If it already exists, just return the method and omit its
            // generation via the ITD
            return reportMethod;
        }

        // Define method parameter names
        List<JavaSymbolName> parameterNames = new ArrayList<JavaSymbolName>();
        parameterNames.add(new JavaSymbolName("format"));
        parameterNames.add(new JavaSymbolName("uiModel"));

        // Define method annotations
        List<AnnotationAttributeValue<?>> requestMappingAttributes = new ArrayList<AnnotationAttributeValue<?>>();
        requestMappingAttributes.add(new StringAttributeValue(
                VALUE_SYMBOL_NAME, "/reports/".concat(reportName)));
        requestMappingAttributes
                .add(new EnumAttributeValue(METHOD_SYMBOL_NAME,
                        new EnumDetails(REQUEST_METHOD_TYPE,
                                new JavaSymbolName("GET"))));
        AnnotationMetadataBuilder requestMapping = new AnnotationMetadataBuilder(
                REQUEST_MAPPING_TYPE, requestMappingAttributes);
        List<AnnotationMetadataBuilder> annotations = new ArrayList<AnnotationMetadataBuilder>();
        annotations.add(requestMapping);

        // Create the method body
        InvocableMemberBodyBuilder bodyBuilder = new InvocableMemberBodyBuilder();

        // test if format is not informed and return to the report form if so
        bodyBuilder
                .appendFormalLine("if ( null == format || format.length() <= 0 ) {");
        bodyBuilder
                .appendIndent()
                .appendFormalLine(
                        "uiModel.addAttribute(\"error\", \"message_format_required\");");
        bodyBuilder.appendIndent().appendFormalLine(
                "return \"".concat(annotationValues.getPath()).concat("/")
                        .concat(reportName).concat("\";"));
        bodyBuilder.appendFormalLine("}");
        // Add the message error to the application.properties
        properties.put("message_format_required",
                "Format required: (pdf, xls, csv, html)");

        /*
         * This is the trick to make changes in the ITD when a new format is
         * added to the report. This changes triggers ReportJspMetadataListener
         * performing changes in JSPXs
         */
        // test if requested format is supported for this report
        bodyBuilder.appendFormalLine("final String REGEX = \"(".concat(
                reportFormats.replace(",", "|")).concat(")\";"));
        bodyBuilder
                .appendFormalLine("Pattern pattern = Pattern.compile(REGEX, Pattern.CASE_INSENSITIVE);");
        bodyBuilder
                .appendFormalLine("Matcher matcher = pattern.matcher(format);");
        bodyBuilder.appendFormalLine("if ( !matcher.matches() ) {");
        bodyBuilder.appendIndent().appendFormalLine(
                "uiModel.addAttribute(\"error\", \"message_format_invalid\");");
        bodyBuilder.appendIndent().appendFormalLine(
                "return \"".concat(annotationValues.getPath()).concat("/")
                        .concat(reportName).concat("\";"));
        bodyBuilder.appendFormalLine("}");
        // Add the message error to the application.properties
        properties.put("message_format_invalid",
                "The requested format is not supported by this report");

        // ImportRegistrationResolver gives access to imports in the
        // Java/AspectJ source
        ImportRegistrationResolver irr = builder
                .getImportRegistrationResolver();

        // We need import java.util.regex.Matcher and java.util.regex.Pattern
        irr.addImport(new JavaType("java.util.regex.Matcher"));
        irr.addImport(new JavaType("java.util.regex.Pattern"));

        // Code populating the JasperReport DataSource
        bodyBuilder.appendFormalLine("Collection<"
                .concat(entity.getSimpleTypeName()).concat("> dataSource = ")
                .concat(entity.getSimpleTypeName()).concat(".find")
                .concat(entity.getSimpleTypeName()).concat("Entries(0, 10);"));
        // test if DataSource is empty and return to the report form if so
        bodyBuilder.appendFormalLine("if (dataSource.isEmpty()) {");
        bodyBuilder
                .appendIndent()
                .appendFormalLine(
                        "uiModel.addAttribute(\"error\", \"message_emptyresults_noreportgeneration\");");
        bodyBuilder.appendIndent().appendFormalLine(
                "return \"".concat(annotationValues.getPath()).concat("/")
                        .concat(reportName).concat("\";"));
        bodyBuilder.appendFormalLine("}");
        // Add the message error to the application.properties
        properties.put("message_emptyresults_noreportgeneration",
                "No results found to generate report");

        // We need import java.util.Collection and the Entity
        irr.addImport(new JavaType("java.util.Collection"));
        irr.addImport(entity);

        // set required attributes
        bodyBuilder
                .appendFormalLine("uiModel.addAttribute(\"format\", format);");
        bodyBuilder.appendFormalLine("uiModel.addAttribute(\"title\", \""
                .concat(reportName.toUpperCase()).concat("\");"));
        bodyBuilder.appendFormalLine("uiModel.addAttribute(\"".concat(
                reportName).concat("List\", dataSource);"));

        // return the JasperReport View
        bodyBuilder.appendFormalLine("return \""
                .concat(entity.getSimpleTypeName().toLowerCase()).concat("_")
                .concat(reportName.toLowerCase()).concat("\";"));

        // Use the MethodMetadataBuilder for easy creation of MethodMetadata
        MethodMetadataBuilder methodBuilder = new MethodMetadataBuilder(
                getId(), Modifier.PUBLIC, methodName, JavaType.STRING,
                parameterTypes, parameterNames, bodyBuilder);
        methodBuilder.setAnnotations(annotations);

        reportMethod = methodBuilder.build();
        builder.addMethod(reportMethod);
        controllerMethods.add(reportMethod);

        propFileOperations
                .addProperties(
                        LogicalPath.getInstance(Path.SRC_MAIN_WEBAPP, ""),
                        "/WEB-INF/i18n/application.properties", properties,
                        true, false);
        return reportMethod;
    }

    /**
     * Checks if Method exists in the ReportMetadata.
     * 
     * @param methodName
     * @param paramTypes
     * @return
     */
    private MethodMetadata reportMethodExists(JavaSymbolName methodName) {
        for (MethodMetadata methodMetadata : controllerMethods) {
            if (methodMetadata.getMethodName().equals(methodName)) {
                return methodMetadata; // The method already exists. Just return
                                       // it
            }
        }
        return null;
    }

    /**
     * Add a new <bean/> to jasper-views.xml file with the name of the new
     * report
     * 
     * @param reportName the name of the report
     * @return jasperReportBeanId
     */
    private void addNewJasperReportBean(JavaType entity, String reportName,
            String format) {
        PathResolver pathResolver = projectOperations.getPathResolver();

        // Install CustomJasperReportsMultiFormatView into
        // top.level.package.<web controllers
        // sub-package>.servlet.view.jasperreports
        // if it is not already installed
        String classMultiFormatView = installCustomJasperReportMultiFormatView();

        // The bean id will be entity_reportName
        String reportBeanId = entity.getSimpleTypeName().toLowerCase()
                .concat("_").concat(reportName.toLowerCase());

        // Add config to jasper-views.xml
        String jasperReportsConfig = pathResolver.getIdentifier(
                LogicalPath.getInstance(Path.SRC_MAIN_WEBAPP, ""),
                "WEB-INF/spring/jasper-views.xml");
        MutableFile mutableJasperViewsConfigFile = fileManager
                .updateFile(jasperReportsConfig);
        Document jasperViewsConfigDocument;

        try {
            jasperViewsConfigDocument = XmlUtils.getDocumentBuilder().parse(
                    mutableJasperViewsConfigFile.getInputStream());
        }
        catch (Exception ex) {
            throw new IllegalStateException(
                    "Could not open jasper-views.xml config file '".concat(
                            jasperReportsConfig).concat("'"), ex);
        }

        Element beans = jasperViewsConfigDocument.getDocumentElement();

        if (null != XmlUtils.findFirstElement(
                "/beans/bean[@id='".concat(reportBeanId).concat("']"), beans)) {
            // logger.warning("A report with the name " + reportBeanId +
            // " is already defined");
            return; // There is a bean with the reportName already
                    // defined, nothing to do
        }

        // Create a DOM element defining the new bean for the JasperReport view
        InputStream templateInputStream = null;
        OutputStream jasperViewsConfigOutStream = null;
        try {
            templateInputStream = FileUtils.getInputStream(getClass(),
                    "jasperreports-bean-config-template.xml");
            Document reportBeanConfigDocument;
            try {
                reportBeanConfigDocument = XmlUtils.getDocumentBuilder().parse(
                        templateInputStream);
            }
            catch (Exception ex) {
                throw new IllegalStateException(
                        "Could not open jasperreports-bean-config-template.xml file",
                        ex);
            }

            Element configElement = (Element) reportBeanConfigDocument
                    .getDocumentElement();
            Element bean = XmlUtils.findFirstElement("/config/bean",
                    configElement);

            // Set the right attributes dynamically
            bean.setAttribute("id", reportBeanId);
            bean.setAttribute("class", classMultiFormatView);
            bean.setAttribute("p:url", "/WEB-INF/reports/".concat(reportBeanId)
                    .concat(".jrxml"));
            bean.setAttribute("p:reportDataKey", reportName.concat("List"));

            // Add the new bean handling the new report view
            Element rootElement = (Element) jasperViewsConfigDocument
                    .getFirstChild();
            Node importedBean = jasperViewsConfigDocument
                    .importNode(bean, true);
            rootElement.appendChild(importedBean);
            jasperViewsConfigOutStream = mutableJasperViewsConfigFile
                    .getOutputStream();
            XmlUtils.writeXml(jasperViewsConfigOutStream,
                    jasperViewsConfigDocument);

        }
        finally {
            IOUtils.closeQuietly(templateInputStream);
            IOUtils.closeQuietly(jasperViewsConfigOutStream);
        }

        fileManager.scan();
    }

    /**
     * Creates a sample jrxml report file based in the template
     * report/JasperReport-template.jrxml with:
     * <ul>
     * <li>3 field elements or less if the entity in fromBackingObject doesn't
     * has more than 3 member fields</li>
     * <li>As many staticText/textField element pairs as fields have been
     * created</li>
     * </ul>
     * The jrxml is copied to the WEB-INF/reports foder with the name
     * entity_report_name.jrxml
     * 
     * @param installedReport
     * @param fromBackingObject
     */
    private void installJasperReportTemplate(String installedReport,
            JavaType fromBackingObject) {
        // Check if a jrxml file exists
        PathResolver pathResolver = projectOperations.getPathResolver();
        String reportJrXml = pathResolver
                .getIdentifier(
                        LogicalPath.getInstance(Path.SRC_MAIN_WEBAPP, ""),
                        "WEB-INF/reports/"
                                .concat(fromBackingObject.getSimpleTypeName()
                                        .toLowerCase()).concat("_")
                                .concat(installedReport.toLowerCase())
                                .concat(".jrxml"));

        if (fileManager.exists(reportJrXml)) {
            // We can't modify the existing jrxml file just in case the user has
            // modified it
            return;
        }

        // Get member details of fromBackingObject type
        MemberDetails memberDetails = getMemberDetails(fromBackingObject);
        List<FieldMetadata> elegibleFields = webMetadataService
                .getScaffoldEligibleFieldMetadata(fromBackingObject,
                        memberDetails, governorPhysicalTypeMetadata.getId());
        /*
         * We only use 3 fields in the sample report. By now we only use field
         * types accepted by JasperReports XMLSchema
         */
        List<FieldMetadata> usableFields = new ArrayList<FieldMetadata>();
        Iterator<FieldMetadata> it = elegibleFields.iterator();
        FieldMetadata field = null;
        while (it.hasNext() && usableFields.size() < 3) {
            field = it.next();
            if (ReportValidTypes.VALID_TYPES.contains(field.getFieldType()
                    .getFullyQualifiedTypeName())) {
                usableFields.add(field);
            }
        }

        InputStream templateInputStream = FileUtils.getInputStream(getClass(),
                "report/JasperReport-template.jrxml");

        Document jrxml;
        try {
            jrxml = XmlUtils.getDocumentBuilder().parse(templateInputStream);
        }
        catch (Exception ex) {
            throw new IllegalStateException(
                    "Could not open JasperReport-template.jrxml file", ex);
        }

        Element jasperReport = jrxml.getDocumentElement();

        // Add a field definition and its use in the detail band per
        // usableFields
        String textHeight = "20";
        String sTextW = "64";
        String fTextW = "119";
        String yPos = "0";
        int xPos = 0; // we need xPos as int in order to modify its value.
        Element staticText;
        Element textField;
        Element detailBand = XmlUtils.findFirstElement(
                "/jasperReport/detail/band", jasperReport);
        Node backgroundNode = XmlUtils.findNode("/jasperReport/background",
                jasperReport);
        for (FieldMetadata fieldMetadata : usableFields) {
            jasperReport.insertBefore(
                    new XmlElementBuilder("field", jrxml)
                            .addAttribute(
                                    "name",
                                    fieldMetadata.getFieldName()
                                            .getSymbolName())
                            .addAttribute(
                                    "class",
                                    fieldMetadata.getFieldType()
                                            .getFullyQualifiedTypeName())
                            .build(), backgroundNode);
            staticText = (Element) new XmlElementBuilder("staticText", jrxml)
                    .build();
            staticText
                    .appendChild(new XmlElementBuilder("reportElement", jrxml)
                            .addAttribute("x", String.valueOf(xPos))
                            .addAttribute("y", yPos)
                            .addAttribute("width", sTextW)
                            .addAttribute("height", textHeight).build());
            staticText.appendChild(new XmlElementBuilder("textElement", jrxml)
                    .build());
            staticText.appendChild(new XmlElementBuilder("text", jrxml)
                    .addChild(
                            jrxml.createCDATASection(fieldMetadata
                                    .getFieldName().getReadableSymbolName()))
                    .build());
            detailBand.appendChild(staticText);
            // Increment xPos for the next text box
            xPos += (Integer.parseInt(sTextW) + 1);
            textField = (Element) new XmlElementBuilder("textField", jrxml)
                    .build();
            textField.appendChild(new XmlElementBuilder("reportElement", jrxml)
                    .addAttribute("x", String.valueOf(xPos))
                    .addAttribute("y", yPos).addAttribute("width", fTextW)
                    .addAttribute("height", textHeight).build());
            textField.appendChild(new XmlElementBuilder("textElement", jrxml)
                    .build());
            textField.appendChild(new XmlElementBuilder("textFieldExpression",
                    jrxml)
                    .addAttribute(
                            "class",
                            fieldMetadata.getFieldType()
                                    .getFullyQualifiedTypeName())
                    .addChild(
                            jrxml.createCDATASection("$F{".concat(
                                    fieldMetadata.getFieldName()
                                            .getSymbolName()).concat("}")))
                    .build());
            detailBand.appendChild(textField);
            // Increment xPos for the next text box
            xPos += (Integer.parseInt(fTextW) + 1);
        }

        // We are sure that reportJrXml file doesn't exist so we can write it
        // with the jrxml document content
        MutableFile mutableFile = fileManager.createFile(reportJrXml);
        Validate.notNull(mutableFile,
                "Could not create jrxml file '".concat(reportJrXml).concat("'"));

        try {
            // Build a string representation of the jrxml
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            XmlUtils.writeXml(XmlUtils.createIndentingTransformer(),
                    byteArrayOutputStream, jrxml);
            String jrxmlContent = byteArrayOutputStream.toString();

            // We need to write the file out (it's a new file, or the existing
            // file has different contents)
            InputStream inputStream = null;
            OutputStream outputStream = null;
            try {
                inputStream = IOUtils.toInputStream(jrxmlContent);
                outputStream = mutableFile.getOutputStream();
                IOUtils.copy(inputStream, outputStream);
            }
            finally {
                IOUtils.closeQuietly(inputStream);
                IOUtils.closeQuietly(outputStream);
            }
        }
        catch (IOException ioe) {
            throw new IllegalStateException("Could not output '".concat(
                    mutableFile.getCanonicalPath()).concat("'"), ioe);
        }

    }

    /**
     * Install the Java file CustomJasperReportsMultiFormatView.java as part of
     * the web package. The sub-package will be
     * <b>controller_package.servlet.view.jasperreports</b>
     * 
     * @return Qualified name of the installed Java file without extension
     */
    private String installCustomJasperReportMultiFormatView() {
        String controllerPackage = this.aspectName.getPackage()
                .getFullyQualifiedPackageName();
        String customMultiFormatViewPackage = controllerPackage
                .concat(".servlet.view.jasperreports");
        String customMultiFormatViewPath = projectOperations.getPathResolver()
                .getIdentifier(
                        LogicalPath.getInstance(Path.SRC_MAIN_JAVA, ""),
                        customMultiFormatViewPackage.replace(".",
                                File.separator).concat(
                                "/CustomJasperReportsMultiFormatView.java"));

        MutableFile mutCMultFormVCl = null;
        if (!fileManager.exists(customMultiFormatViewPath)) {
            mutCMultFormVCl = fileManager.createFile(customMultiFormatViewPath);
            InputStream template = FileUtils
                    .getInputStream(
                            getClass(),
                            "web/servlet/view/jasperreports/CustomJasperReportsMultiFormatView-template.java");
            String javaTemplate;
            try {
                javaTemplate = IOUtils
                        .toString(new InputStreamReader(template));

                // Replace package definition
                javaTemplate = StringUtils.replace(javaTemplate, "${PACKAGE}",
                        customMultiFormatViewPackage);

                // Write final java file
                InputStream inputStream = null;
                OutputStream outputStream = null;
                try {
                    inputStream = IOUtils.toInputStream(javaTemplate);
                    outputStream = mutCMultFormVCl.getOutputStream();
                    IOUtils.copy(inputStream, outputStream);
                }
                finally {
                    IOUtils.closeQuietly(inputStream);
                    IOUtils.closeQuietly(outputStream);
                }
            }
            catch (IOException ioe) {
                throw new IllegalStateException(
                        "Unable load CustomJasperReportsMultiFormatView-template.java template",
                        ioe);
            }
            finally {
                IOUtils.closeQuietly(template);
            }
        }

        return customMultiFormatViewPackage
                .concat(".CustomJasperReportsMultiFormatView");
    }

    /**
     * Return the member details of the given Type
     * 
     * @param type
     * @return
     */
    private MemberDetails getMemberDetails(JavaType type) {
        PhysicalTypeMetadata fBackObjPhTypeMD = (PhysicalTypeMetadata) metadataService
                .get(PhysicalTypeIdentifier.createIdentifier(type,
                        LogicalPath.getInstance(Path.SRC_MAIN_JAVA, "")));
        Validate.notNull(
                fBackObjPhTypeMD,
                "Unable to obtain physical type metdata for type "
                        + type.getFullyQualifiedTypeName());
        return memberDetailsScanner.getMemberDetails(getClass().getName(),
                (ClassOrInterfaceTypeDetails) fBackObjPhTypeMD
                        .getMemberHoldingTypeDetails());
    }

    /**
     * Return an string with the following content: <b>{"format1","format2"}</b>
     * as many fromatX as reportFromats values.
     * 
     * @param reportFormats
     * @return
     */
    private String getReportFormatsAsArray(String reportFormats) {
        StringBuilder sb = new StringBuilder("{");
        for (String format : reportFormats.split(",")) {
            sb.append("\"").append(format).append("\",");
        }
        sb.deleteCharAt(sb.length() - 1);
        sb.append("}");
        return sb.toString();
    }

    /**
     * Given a reportName returns the name for the controller method
     * "generate<reportName>". If isForm = true, it will add "Form" to the
     * returned method name.
     * 
     * @param reportName
     * @param isForm
     */
    public static String generateMethodName(String reportName, boolean isForm) {
        String methodName = "generate" + StringUtils.capitalize(reportName);

        if (isForm) {
            methodName = methodName.concat("Form");
        }
        return methodName;
    }

    /**
     * Strip a report|format string in 2 parts
     * 
     * @param definedReport
     * @return string[0]=reportname, string[1]=format
     */
    public static String[] stripGvNixReportValue(String definedReport) {
        Validate.isTrue(definedReport.contains("|"),
                "GvNixReport Annotation value must be reportName|format");
        return definedReport.toLowerCase().split("\\|");
    }

    /**
     * Check if formats are a comma separated value matching supported report
     * formats.
     * 
     * @param formats
     * @return
     */
    public static boolean isValidFormat(String formats) {
        final String REGEX = "(pdf|xls|csv|html)";
        Pattern pattern = Pattern.compile(REGEX, Pattern.CASE_INSENSITIVE);

        Matcher matcher = null;
        boolean isValid = false;
        for (String string : formats.split(",")) {
            matcher = pattern.matcher(string);
            isValid = matcher.matches();
            if (!isValid)
                break;
        }

        return isValid;
    }

    /**
     * Update the format part in the value of a report definition in the
     * {@link GvNIXReports}
     * 
     * @param oldFormats
     * @param formats
     * @return
     */
    public static String updateFormat(String oldFormats, String formats) {
        StringBuilder sb = new StringBuilder(oldFormats);
        String[] splitedFormats = formats.split(",");
        for (String format : splitedFormats) {
            if (!oldFormats.contains(format)) {
                sb.append(",").append(format);
            }
        }
        return sb.toString().toLowerCase();
    }

    public List<MethodMetadata> getReportMethods() {
        return reportMethods;
    }

    public List<MethodMetadata> getControllerMethods() {
        return controllerMethods;
    }

    public List<String> getInstalledReports() {
        return installedReports;
    }

    public WebScaffoldMetadata getWebScaffoldMetadata() {
        return webScaffoldMetadata;
    }

    // Typically, no changes are required beyond this point

    public String toString() {
        ToStringBuilder tsc = new ToStringBuilder(this);
        tsc.append("identifier", getId());
        tsc.append("valid", valid);
        tsc.append("aspectName", aspectName);
        tsc.append("destinationType", destination);
        tsc.append("governor", governorPhysicalTypeMetadata.getId());
        tsc.append("reportMethods", reportMethods);
        tsc.append("itdTypeDetails", itdTypeDetails);
        return tsc.toString();
    }

    public static final String getMetadataIdentiferType() {
        return PROVIDES_TYPE;
    }

    public static final String createIdentifier(JavaType javaType,
            LogicalPath path) {
        return PhysicalTypeIdentifierNamingUtils.createIdentifier(
                PROVIDES_TYPE_STRING, javaType, path);
    }

    public static final JavaType getJavaType(String metadataIdentificationString) {
        return PhysicalTypeIdentifierNamingUtils.getJavaType(
                PROVIDES_TYPE_STRING, metadataIdentificationString);
    }

    public static final LogicalPath getPath(String metadataIdentificationString) {
        return PhysicalTypeIdentifierNamingUtils.getPath(PROVIDES_TYPE_STRING,
                metadataIdentificationString);
    }

    public static boolean isValid(String metadataIdentificationString) {
        return PhysicalTypeIdentifierNamingUtils.isValid(PROVIDES_TYPE_STRING,
                metadataIdentificationString);
    }
}
