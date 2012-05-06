/*
 * gvNIX. Spring Roo based RAD tool for Conselleria d'Infraestructures
 * i Transport - Generalitat Valenciana
 * Copyright (C) 2010, 2012 CIT - Generalitat Valenciana
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
package org.gvnix.web.screen.roo.addon;

import java.io.InputStream;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.logging.Logger;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.springframework.roo.addon.propfiles.PropFileOperations;
import org.springframework.roo.addon.web.mvc.controller.details.JavaTypePersistenceMetadataDetails;
import org.springframework.roo.addon.web.mvc.controller.details.WebMetadataService;
import org.springframework.roo.addon.web.mvc.controller.scaffold.mvc.WebScaffoldMetadata;
import org.springframework.roo.addon.web.mvc.jsp.menu.MenuOperations;
import org.springframework.roo.addon.web.selenium.SeleniumOperationsImpl;
import org.springframework.roo.classpath.PhysicalTypeIdentifier;
import org.springframework.roo.classpath.PhysicalTypeMetadata;
import org.springframework.roo.classpath.details.ClassOrInterfaceTypeDetails;
import org.springframework.roo.classpath.details.FieldMetadata;
import org.springframework.roo.classpath.details.FieldMetadataBuilder;
import org.springframework.roo.classpath.details.MemberFindingUtils;
import org.springframework.roo.classpath.details.annotations.AnnotationAttributeValue;
import org.springframework.roo.classpath.details.annotations.AnnotationMetadata;
import org.springframework.roo.classpath.operations.DateTime;
import org.springframework.roo.classpath.scanner.MemberDetails;
import org.springframework.roo.classpath.scanner.MemberDetailsScanner;
import org.springframework.roo.metadata.MetadataService;
import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.process.manager.FileManager;
import org.springframework.roo.project.Path;
import org.springframework.roo.project.PathResolver;
import org.springframework.roo.project.Plugin;
import org.springframework.roo.project.ProjectMetadata;
import org.springframework.roo.project.ProjectOperations;
import org.springframework.roo.support.util.Assert;
import org.springframework.roo.support.util.TemplateUtils;
import org.springframework.roo.support.util.XmlUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Selenium tests generation and configurations for screen patterns.
 *
 * @see SeleniumOperationsImpl
 *
 * @author Mario Martínez Sánchez (mmartinez at disid dot com) at <a
 *         href="http://www.disid.com">DiSiD Technologies S.L.</a> made for <a
 *         href="http://www.cit.gva.es">Conselleria d'Infraestructures i
 *         Transport</a>
 *
 * @since 0.9
 */
@Component
@Service
public class SeleniumServicesImpl implements SeleniumServices {

    /**
     * MetadataService offers access to Roo's metadata model, use it to retrieve
     * any available metadata by its MID
     */
    @Reference private MetadataService metadataService;
    @Reference private ProjectOperations projectOperations;
    @Reference PropFileOperations propFileOperations;
	@Reference protected FileManager fileManager;
	@Reference private MemberDetailsScanner memberDetailsScanner;
	@Reference private WebMetadataService webMetadataService;
	@Reference private MenuOperations menuOperations;

	private static Logger logger = Logger
            .getLogger(SeleniumServicesImpl.class.getName());

	/**
	 * Creates a new Selenium testcase
	 *
	 * @param controller the JavaType of the controller under test (required)
	 * @param name the name of the test case (optional)
	 */
	public void generateTest(JavaType controller, WebPatternType type, WebPatternHierarchy hierarchy, JavaSymbolName detailField, String name, String serverURL) {
		Assert.notNull(controller, "Controller type required");

		String webScaffoldMetadataIdentifier = WebScaffoldMetadata.createIdentifier(controller, Path.SRC_MAIN_JAVA);
		WebScaffoldMetadata webScaffoldMetadata = (WebScaffoldMetadata) metadataService.get(webScaffoldMetadataIdentifier);
		Assert.notNull(webScaffoldMetadata, "Web controller '" + controller.getFullyQualifiedTypeName() + "' does not appear to be an automatic, scaffolded controller");

		// We abort the creation of a selenium test if the controller does not allow the creation of new instances for the form backing object
		if (!webScaffoldMetadata.getAnnotationValues().isCreate()) {
			logger.warning("The controller you specified does not allow the creation of new instances of the form backing object. No Selenium tests created.");
			return;
		}

		if (!serverURL.endsWith("/")) {
			serverURL = serverURL + "/";
		}

		JavaType formBackingType = webScaffoldMetadata.getAnnotationValues().getFormBackingObject();

		// DiSiD Use pattern name instead of entity name for compatibility with Roo selenium addon
		String relativeTestFilePath = "selenium/test-" + name + "-" + hierarchy + "-" + type;
		if (detailField != null) {
			relativeTestFilePath = relativeTestFilePath + "-" + detailField;
		}
		relativeTestFilePath = relativeTestFilePath + ".xhtml";

		String seleniumPath = projectOperations.getPathResolver().getIdentifier(Path.SRC_MAIN_WEBAPP, relativeTestFilePath);

		Document document;
		try {
			InputStream templateInputStream = TemplateUtils.getTemplate(SeleniumOperationsImpl.class, "selenium-template.xhtml");
			Assert.notNull(templateInputStream, "Could not acquire selenium.xhtml template");
			document = XmlUtils.readXml(templateInputStream);
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}

		Element root = (Element) document.getLastChild();
		if (root == null || !"html".equals(root.getNodeName())) {
			throw new IllegalArgumentException("Could not parse selenium test case template file!");
		}

		// DiSiD Use pattern name instead of entity name for compatibility with Roo selenium addon
		name = (name != null ? name : "Selenium test for " + name);
		XmlUtils.findRequiredElement("/html/head/title", root).setTextContent(name);

		XmlUtils.findRequiredElement("/html/body/table/thead/tr/td", root).setTextContent(name);

		// DiSiD Create pattern test
		Element tbody = XmlUtils.findRequiredElement("/html/body/table/tbody", root);
		tbody.appendChild(openCommand(document, serverURL + projectOperations.getProjectMetadata().getProjectName() + "/" + webScaffoldMetadata.getAnnotationValues().getPath(), type, name, hierarchy));

		MemberDetails memberDetails = getMemberDetails(formBackingType);

		if (hierarchy.equals(WebPatternHierarchy.master) && type.equals(WebPatternType.register)) {

			generateTestMasterRegister(type, formBackingType, document, tbody,
					memberDetails);
		}
		else if (hierarchy.equals(WebPatternHierarchy.master) && type.equals(WebPatternType.tabular)) {

			generateTestMasterTabular(type, formBackingType, document, tbody,
					memberDetails);
		}
		else if (hierarchy.equals(WebPatternHierarchy.detail) && type.equals(WebPatternType.tabular)) {

			generateTestDetailTabular(type, detailField, formBackingType,
					document, tbody, memberDetails);
		}

		fileManager.createOrUpdateTextFileIfRequired(seleniumPath, XmlUtils.nodeToString(document), false);

		manageTestSuite(relativeTestFilePath, name, serverURL);

		installMavenPlugin();
	}

	protected MemberDetails getMemberDetails(JavaType formBackingType) {

		PhysicalTypeMetadata formBackingObjectPhysicalTypeMetadata = (PhysicalTypeMetadata) metadataService.get(PhysicalTypeIdentifier.createIdentifier(formBackingType, Path.SRC_MAIN_JAVA));
		Assert.notNull(formBackingObjectPhysicalTypeMetadata, "Unable to obtain physical type metadata for type " + formBackingType.getFullyQualifiedTypeName());
		ClassOrInterfaceTypeDetails formBackingClassOrInterfaceDetails = (ClassOrInterfaceTypeDetails) formBackingObjectPhysicalTypeMetadata.getMemberHoldingTypeDetails();
		MemberDetails memberDetails = memberDetailsScanner.getMemberDetails(getClass().getName(), formBackingClassOrInterfaceDetails);

		return memberDetails;
	}

	public void generateTestMasterRegister(WebPatternType type,
			JavaType formBackingType, Document document, Element tbody,
			MemberDetails memberDetails) {

		addCompositeIdentifierFields(type, formBackingType, document, tbody,
				memberDetails);

		List<FieldMetadata> fields = webMetadataService.getScaffoldEligibleFieldMetadata(formBackingType, memberDetails, null);

		addFields(fields, type, formBackingType, document, tbody);

		tbody.appendChild(clickAndWaitCommand(document, "//input[@id='proceed']"));

		// Add verifications for all other fields
		addVerificationText(formBackingType, document, tbody, fields);
	}

	public void generateTestMasterTabular(WebPatternType type,
			JavaType formBackingType, Document document, Element tbody,
			MemberDetails memberDetails) {

		String imgId = XmlUtils.convertId("fu:" + formBackingType.getFullyQualifiedTypeName()) + "_create";
		tbody.appendChild(clickCommand(document, "//img[@id='" + imgId + "']"));

		// TODO Composite PK test generation when tabular pattern PK support

		// TODO Check if other fields are editable (storeEditable)

		List<FieldMetadata> fields = webMetadataService.getScaffoldEligibleFieldMetadata(formBackingType, memberDetails, null);

		addFields(fields, type, formBackingType, document, tbody);

		String inputId = "gvnix_control_add_save_" + XmlUtils.convertId("fu:" + formBackingType.getFullyQualifiedTypeName());
		tbody.appendChild(clickAndWaitCommand(document, "//input[@id='" + inputId + "']"));

		addVerificationValue(formBackingType, document, tbody, fields);
	}

	public void generateTestDetailTabular(WebPatternType type,
			JavaSymbolName detailField, JavaType formBackingType,
			Document document, Element tbody, MemberDetails memberDetails) {

		JavaType detailType = null;
		Iterator<FieldMetadata> detailTypes = webMetadataService.getScaffoldEligibleFieldMetadata(formBackingType, memberDetails, null).iterator();
		while (detailTypes.hasNext() && detailType == null) {
			FieldMetadata tmp = detailTypes.next();
			if (tmp.getFieldName().equals(detailField)) {
				detailType = tmp.getFieldType();
			}
		}

		if (detailType != null) {

			JavaType fieldType = detailType.getParameters().get(0);

			MemberDetails memberDetailsField = getMemberDetails(fieldType);

			String imgId = XmlUtils.convertId("fu:" + fieldType.getFullyQualifiedTypeName()) + "_create";
			tbody.appendChild(clickCommand(document, "//img[@id='" + imgId + "']"));

			// TODO Composite PK test generation when tabular pattern PK support

			// TODO Check if other fields are editable (storeEditable)

			List<FieldMetadata> fields = webMetadataService.getScaffoldEligibleFieldMetadata(fieldType, memberDetailsField, null);

			// Add all other fields
			addFields(fields, type, fieldType, document, tbody);

			String inputId = "gvnix_control_add_save_" + XmlUtils.convertId("fu:" + fieldType.getFullyQualifiedTypeName());
			tbody.appendChild(clickAndWaitCommand(document, "//input[@id='" + inputId + "']"));

			addVerificationValue(fieldType, document, tbody, fields);

		}
	}

	protected void addCompositeIdentifierFields(WebPatternType type,
			JavaType formBackingType, Document document, Element tbody,
			MemberDetails memberDetails) {

		// Add composite PK identifier fields if needed
		JavaTypePersistenceMetadataDetails javaTypePersistenceMetadataDetails = webMetadataService.getJavaTypePersistenceMetadataDetails(formBackingType, memberDetails, null);
		if (javaTypePersistenceMetadataDetails != null && !javaTypePersistenceMetadataDetails.getRooIdentifierFields().isEmpty()) {
			for (FieldMetadata field : javaTypePersistenceMetadataDetails.getRooIdentifierFields()) {
				if (!field.getFieldType().isCommonCollectionType() && !isSpecialType(field.getFieldType())) {
					FieldMetadataBuilder fieldBuilder = new FieldMetadataBuilder(field);
					fieldBuilder.setFieldName(new JavaSymbolName(javaTypePersistenceMetadataDetails.getIdentifierField().getFieldName().getSymbolName() + "." + field.getFieldName().getSymbolName()));
					tbody.appendChild(typeCommand(document, fieldBuilder.build(), type, formBackingType));
				}
			}
		}
	}

	protected void addFields(List<FieldMetadata> fields, WebPatternType type,
			JavaType formBackingType, Document document, Element tbody) {

		// Add all other fields
		for (FieldMetadata field : fields) {
			if (!field.getFieldType().isCommonCollectionType() && !isSpecialType(field.getFieldType())) {
				tbody.appendChild(typeCommand(document, field, type, formBackingType));
			}
		}
	}

	protected void addVerificationValue(JavaType formBackingType,
			Document document, Element tbody, List<FieldMetadata> fields) {

		// Add verifications for all other fields
		for (FieldMetadata field : fields) {
			if (!field.getFieldType().isCommonCollectionType() && !isSpecialType(field.getFieldType())) {
				tbody.appendChild(verifyValueCommand(document, formBackingType, field));
			}
		}
	}

	protected void addVerificationText(JavaType formBackingType,
			Document document, Element tbody, List<FieldMetadata> fields) {

		// Add verifications for all other fields
		for (FieldMetadata field : fields) {
			if (!field.getFieldType().isCommonCollectionType() && !isSpecialType(field.getFieldType())) {
				tbody.appendChild(verifyTextCommand(document, formBackingType, field));
			}
		}
	}

	private Node openCommand(Document document, String linkTarget, WebPatternType type, String name, WebPatternHierarchy hierarchy) {
		Node tr = document.createElement("tr");

		Node td1 = tr.appendChild(document.createElement("td"));
		td1.setTextContent("open");

		// DiSiD Add pattern request attributes
		Node td2 = tr.appendChild(document.createElement("td"));

		if (hierarchy.equals(WebPatternHierarchy.detail)) {
			td2.setTextContent(linkTarget + (linkTarget.contains("?") ? "&" : "?")
					+ "gvnixform"
					+ "&gvnixpattern=" + name
					+ "&index=1"
					+ "&lang=" + Locale.getDefault());
		}
		else if (type.equals(WebPatternType.register)) {
			td2.setTextContent(linkTarget + (linkTarget.contains("?") ? "&" : "?")
					+ "form"
					+ "&gvnixpattern=" + name
					+ "&index=1"
					+ "&lang=" + Locale.getDefault());
		}
		else if (type.equals(WebPatternType.tabular)) {
			td2.setTextContent(linkTarget + (linkTarget.contains("?") ? "&" : "?")
					+ "gvnixpattern=" + name
					+ "&lang=" + Locale.getDefault());
		}

		Node td3 = tr.appendChild(document.createElement("td"));
		td3.setTextContent(" ");

		return tr;
	}

	private boolean isSpecialType(JavaType javaType) {

		String physicalTypeIdentifier = PhysicalTypeIdentifier.createIdentifier(javaType, Path.SRC_MAIN_JAVA);

		// We are only interested if the type is part of our application and if no editor exists for it already
		if (metadataService.get(physicalTypeIdentifier) != null) {
			return true;
		}
		return false;
	}

	private Node typeCommand(Document document, FieldMetadata field, WebPatternType type, JavaType formBackingType) {
		Node tr = document.createElement("tr");

		Node td1 = tr.appendChild(document.createElement("td"));
		td1.setTextContent("type");

		Node td2 = tr.appendChild(document.createElement("td"));

		if (type.equals(WebPatternType.register)) {

			td2.setTextContent("_" + field.getFieldName().getSymbolName() + "_id");
		}
		else if (type.equals(WebPatternType.tabular)) {

			String id = "_" + XmlUtils.convertId("fu:" + formBackingType.getFullyQualifiedTypeName()) + "[0]_" + field.getFieldName() + "_id_create";
			td2.setTextContent(id);
		}

		Node td3 = tr.appendChild(document.createElement("td"));
		td3.setTextContent(convertToInitializer(field));

		return tr;
	}

	private Node clickAndWaitCommand(Document document, String linkTarget) {
		Node tr = document.createElement("tr");

		Node td1 = tr.appendChild(document.createElement("td"));
		td1.setTextContent("clickAndWait");

		Node td2 = tr.appendChild(document.createElement("td"));
		td2.setTextContent(linkTarget);

		Node td3 = tr.appendChild(document.createElement("td"));
		td3.setTextContent(" ");

		return tr;
	}

	private Node clickCommand(Document document, String linkTarget) {
		Node tr = document.createElement("tr");

		Node td1 = tr.appendChild(document.createElement("td"));
		td1.setTextContent("click");

		Node td2 = tr.appendChild(document.createElement("td"));
		td2.setTextContent(linkTarget);

		Node td3 = tr.appendChild(document.createElement("td"));
		td3.setTextContent(" ");

		return tr;
	}

	private Node verifyTextCommand(Document document, JavaType formBackingType, FieldMetadata field) {
		Node tr = document.createElement("tr");

		Node td1 = tr.appendChild(document.createElement("td"));
		td1.setTextContent("verifyText");

		Node td2 = tr.appendChild(document.createElement("td"));
		td2.setTextContent(XmlUtils.convertId("_s_" + formBackingType.getFullyQualifiedTypeName() + "_" + field.getFieldName().getSymbolName() + "_" + field.getFieldName().getSymbolName() + "_id"));

		Node td3 = tr.appendChild(document.createElement("td"));
		td3.setTextContent(convertToInitializer(field));

		return tr;
	}

	private Node verifyValueCommand(Document document, JavaType formBackingType, FieldMetadata field) {
		Node tr = document.createElement("tr");

		Node td1 = tr.appendChild(document.createElement("td"));
		td1.setTextContent("verifyValue");

		Node td2 = tr.appendChild(document.createElement("td"));
		String id = "_" + XmlUtils.convertId("fu:" + formBackingType.getFullyQualifiedTypeName()) + "[0]_" + field.getFieldName() + "_id_update";
		td2.setTextContent(XmlUtils.convertId(id));

		Node td3 = tr.appendChild(document.createElement("td"));
		td3.setTextContent(convertToInitializer(field));

		return tr;
	}

	private void manageTestSuite(String testPath, String name, String serverURL) {
		String relativeTestFilePath = "selenium/test-suite.xhtml";
		String seleniumPath = projectOperations.getPathResolver().getIdentifier(Path.SRC_MAIN_WEBAPP, relativeTestFilePath);

		Document suite;
		try {
			if (fileManager.exists(seleniumPath)) {
				suite = XmlUtils.readXml(fileManager.getInputStream(seleniumPath));
			} else {
				InputStream templateInputStream = TemplateUtils.getTemplate(SeleniumOperationsImpl.class, "selenium-test-suite-template.xhtml");
				Assert.notNull(templateInputStream, "Could not acquire selenium test suite template");
				suite = XmlUtils.readXml(templateInputStream);
			}
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}

		ProjectMetadata projectMetadata = projectOperations.getProjectMetadata();
		Assert.notNull(projectMetadata, "Unable to obtain project metadata");

		Element root = (Element) suite.getLastChild();

		XmlUtils.findRequiredElement("/html/head/title", root).setTextContent("Test suite for " + projectMetadata.getProjectName() + "project");

		Element tr = suite.createElement("tr");
		Element td = suite.createElement("td");
		tr.appendChild(td);
		Element a = suite.createElement("a");
		a.setAttribute("href", serverURL + projectMetadata.getProjectName() + "/resources/" + testPath);
		a.setTextContent(name);
		td.appendChild(a);

		XmlUtils.findRequiredElement("/html/body/table", root).appendChild(tr);

		fileManager.createOrUpdateTextFileIfRequired(seleniumPath, XmlUtils.nodeToString(suite), false);

		menuOperations.addMenuItem(new JavaSymbolName("SeleniumTests"), new JavaSymbolName("Test"), "Test", "selenium_menu_test_suite", "/resources/" + relativeTestFilePath, "si_");
	}

	private void installMavenPlugin() {
		PathResolver pathResolver = projectOperations.getPathResolver();
		String pom = pathResolver.getIdentifier(Path.ROOT, "/pom.xml");
		Document document = XmlUtils.readXml(fileManager.getInputStream(pom));
		Element root = (Element) document.getLastChild();

		// Stop if the plugin is already installed
		if (XmlUtils.findFirstElement("/project/build/plugins/plugin[artifactId = 'selenium-maven-plugin']", root) != null) {
			return;
		}

		Element configuration = XmlUtils.getConfiguration(SeleniumOperationsImpl.class);
		Element plugin = XmlUtils.findFirstElement("/configuration/selenium/plugin", configuration);

		// Now install the plugin itself
		if (plugin != null) {
			projectOperations.addBuildPlugin(new Plugin(plugin));
		}
	}

	private String convertToInitializer(FieldMetadata field) {
		String initializer = " ";
		short index = 1;
		AnnotationMetadata min = MemberFindingUtils.getAnnotationOfType(field.getAnnotations(), new JavaType("javax.validation.constraints.Min"));
		if (min != null) {
			AnnotationAttributeValue<?> value = min.getAttribute(new JavaSymbolName("value"));
			if (value != null) {
				index = new Short(value.getValue().toString());
			}
		}
		if (field.getFieldName().getSymbolName().contains("email") || field.getFieldName().getSymbolName().contains("Email")) {
			initializer = "some@email.com";
		} else if (field.getFieldType().equals(JavaType.STRING_OBJECT)) {
			initializer = "some" + field.getFieldName().getSymbolNameCapitalisedFirstLetter() + index;
		} else if (field.getFieldType().equals(new JavaType(Date.class.getName())) || field.getFieldType().equals(new JavaType(Calendar.class.getName()))) {
			Calendar cal = Calendar.getInstance();
			AnnotationMetadata dateTimeFormat = null;
			String style = null;
			if (null != (dateTimeFormat = MemberFindingUtils.getAnnotationOfType(field.getAnnotations(), new JavaType("org.springframework.format.annotation.DateTimeFormat")))) {
				AnnotationAttributeValue<?> value = dateTimeFormat.getAttribute(new JavaSymbolName("style"));
				if (value != null) {
					style = value.getValue().toString();
				}
			}
			if (null != MemberFindingUtils.getAnnotationOfType(field.getAnnotations(), new JavaType("javax.validation.constraints.Past"))) {
				cal.add(Calendar.YEAR, -1);
				cal.add(Calendar.MONTH, -1);
				cal.add(Calendar.DAY_OF_MONTH, -1);
			} else if (null != MemberFindingUtils.getAnnotationOfType(field.getAnnotations(), new JavaType("javax.validation.constraints.Future"))) {
				cal.add(Calendar.YEAR, +1);
				cal.add(Calendar.MONTH, +1);
				cal.add(Calendar.DAY_OF_MONTH, +1);
			}
			if (style != null) {
				if (style.startsWith("-")) {
					initializer = ((SimpleDateFormat) DateFormat.getTimeInstance(DateTime.parseDateFormat(style.charAt(1)), Locale.getDefault())).format(cal.getTime());
				} else if (style.endsWith("-")) {
					initializer = ((SimpleDateFormat) DateFormat.getDateInstance(DateTime.parseDateFormat(style.charAt(0)), Locale.getDefault())).format(cal.getTime());
				} else {
					initializer = ((SimpleDateFormat) DateFormat.getDateTimeInstance(DateTime.parseDateFormat(style.charAt(0)), DateTime.parseDateFormat(style.charAt(1)), Locale.getDefault())).format(cal.getTime());
				}
			} else {
				initializer = ((SimpleDateFormat) DateFormat.getDateInstance(DateFormat.SHORT, Locale.getDefault())).format(cal.getTime());
			}

		} else if (field.getFieldType().equals(JavaType.BOOLEAN_OBJECT) || field.getFieldType().equals(JavaType.BOOLEAN_PRIMITIVE)) {
			initializer = new Boolean(false).toString();
		} else if (field.getFieldType().equals(JavaType.INT_OBJECT) || field.getFieldType().equals(JavaType.INT_PRIMITIVE)) {
			initializer = new Integer(index).toString();
		} else if (field.getFieldType().equals(JavaType.DOUBLE_OBJECT) || field.getFieldType().equals(JavaType.DOUBLE_PRIMITIVE)) {
			initializer = new Double(index).toString();
		} else if (field.getFieldType().equals(JavaType.FLOAT_OBJECT) || field.getFieldType().equals(JavaType.FLOAT_PRIMITIVE)) {
			initializer = new Float(index).toString();
		} else if (field.getFieldType().equals(JavaType.LONG_OBJECT) || field.getFieldType().equals(JavaType.LONG_PRIMITIVE)) {
			initializer = new Long(index).toString();
		} else if (field.getFieldType().equals(JavaType.SHORT_OBJECT) || field.getFieldType().equals(JavaType.SHORT_PRIMITIVE)) {
			initializer = new Short(index).toString();
		} else if (field.getFieldType().equals(new JavaType("java.math.BigDecimal"))) {
			initializer = new BigDecimal(index).toString();
		}
		return initializer;
	}

}
