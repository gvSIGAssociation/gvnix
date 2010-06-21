package org.es.gva.cit.web_binder.roo.addon;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.apache.felix.scr.annotations.*;
import org.springframework.roo.classpath.PhysicalTypeCategory;
import org.springframework.roo.classpath.PhysicalTypeIdentifier;
import org.springframework.roo.classpath.PhysicalTypeMetadata;
import org.springframework.roo.classpath.details.ClassOrInterfaceTypeDetails;
import org.springframework.roo.classpath.details.DefaultClassOrInterfaceTypeDetails;
import org.springframework.roo.classpath.details.DefaultMethodMetadata;
import org.springframework.roo.classpath.details.MethodMetadata;
import org.springframework.roo.classpath.details.annotations.AnnotatedJavaType;
import org.springframework.roo.classpath.details.annotations.AnnotationAttributeValue;
import org.springframework.roo.classpath.details.annotations.AnnotationMetadata;
import org.springframework.roo.classpath.details.annotations.DefaultAnnotationMetadata;
import org.springframework.roo.classpath.itd.InvocableMemberBodyBuilder;
import org.springframework.roo.classpath.operations.ClasspathOperations;
import org.springframework.roo.metadata.MetadataService;
import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.process.manager.FileManager;
import org.springframework.roo.process.manager.MutableFile;
import org.springframework.roo.project.Path;
import org.springframework.roo.project.PathResolver;
import org.springframework.roo.project.ProjectOperations;
import org.springframework.roo.support.util.Assert;
import org.springframework.roo.support.util.XmlUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Implementation of commands that are available via the Roo shell.
 *
 * @author jmvivo
 * @since 1.0
 */
@Component
@Service
public class WebBinderOperationsImpl implements WebBinderOperations {

    private static final String ANNOTATION_METHOD_HANDLE_CLASS = "org.springframework.web.servlet.mvc.annotation.AnnotationMethodHandlerAdapter";

    private static final String WEB_BINDIN_INITIALIZER_PROPERTY_NAME = "webBindingInitializer";

    private static Logger logger = Logger
	    .getLogger(WebBinderOperationsImpl.class.getName());

    @Reference
    private FileManager fileManager;
    @Reference
    private MetadataService metadataService;
    @Reference
    private PathResolver pathResolver;
    @Reference
    private ProjectOperations projectOperations;

    @Reference
    private ClasspathOperations classpathOperations;

    private JavaType currentInitializer = null;

    /*
     * (non-Javadoc)
     *
     * @see
     * org.es.gva.cit.web_binder.roo.addon.WebBinderOperations#isSetupAvailable
     * ()
     */
    public boolean isSetupAvailable() {
	return projectOperations.isPerformCommandAllowed() && !isAllredySetup();
    }

    /**
     * Returns <code>true</true> if configuration is already done.
     *
     * @return
     */
    private boolean isAllredySetup() {

	String pathToMvcConfig = getPathToMvcConfig();
	if (!fileManager.exists(pathToMvcConfig)) {
	    return false;
	}

	Document mvcXml;
	try {
	    mvcXml = XmlUtils.getDocumentBuilder().parse(
		    fileManager.getInputStream(pathToMvcConfig));
	} catch (Exception ex) {
	    throw new IllegalStateException(ex);
	}

	return hasMvcWebInitBinderNode(mvcXml);
    }

    /**
     * Returns the current class used to initialize the
     * <code>PropertyEditors</code>
     *
     * @return <code>JavaType</code> of the class
     */
    private JavaType getInitializerClaseRegistered() {
	if (currentInitializer != null) {
	    return currentInitializer;
	}

	String pathToMvcConfig = getPathToMvcConfig();
	if (!fileManager.exists(pathToMvcConfig)) {
	    return null;
	}

	Document mvcXml;
	try {
	    mvcXml = XmlUtils.getDocumentBuilder().parse(
		    fileManager.getInputStream(pathToMvcConfig));
	} catch (Exception ex) {
	    throw new IllegalStateException(ex);
	}

	Element rootElement = (Element) mvcXml.getDocumentElement();

	JavaType webBindingInitializer = null;
	Node beanElement = getMvcWebInitBinderNode(mvcXml);

	if (beanElement.getAttributes().getNamedItem("class") != null) {
	    webBindingInitializer = new JavaType(beanElement.getAttributes()
		    .getNamedItem("class").getTextContent());
	} else if (beanElement.getAttributes().getNamedItem("id") != null) {
	    String webBindingBeanId = beanElement.getAttributes().getNamedItem(
		    "id").getTextContent();
	    Element webBindingBean = XmlUtils.findRequiredElement(
		    "/beans/bean[@id='" + webBindingBeanId + "']", rootElement);
	    Assert.notNull(webBindingBean, "WebBinding initializer with id '"
		    + webBindingBeanId + "' not found.");
	    Assert.notNull(
		    webBindingBean.getAttributes().getNamedItem("class"),
		    "Missing 'class' attribute in bean '" + webBindingBeanId
			    + "'");
	    webBindingInitializer = new JavaType(webBindingBean.getAttributes()
		    .getNamedItem("class").getTextContent());
	}

	return webBindingInitializer;
    }

    /*
     * (non-Javadoc)
     *
     * @seeorg.es.gva.cit.web_binder.roo.addon.WebBinderOperations#
     * hasMvcWebInitBinderNode(org.w3c.dom.Document)
     */
    public boolean hasMvcWebInitBinderNode(Document mvcXml) {
	Element rootElement = (Element) mvcXml.getDocumentElement();

	Element annotationMethodHandlerElement = XmlUtils.findFirstElement(
		"/beans/bean[@class='" + ANNOTATION_METHOD_HANDLE_CLASS + "']",
		rootElement);
	Element webBindingInitializerProperty = null;
	if (annotationMethodHandlerElement == null) {
	    return false;

	}

	webBindingInitializerProperty = XmlUtils.findFirstElement(
		"//property[@name='" + WEB_BINDIN_INITIALIZER_PROPERTY_NAME
			+ "']", annotationMethodHandlerElement);

	if (webBindingInitializerProperty == null) {
	    return false;
	}

	return true;
    }

    /**
     * Gets the Elements of the XML MVC config file where is set the class to
     * use as initializer.
     *
     * @param mvcXml
     * @return
     */
    private Element getMvcWebInitBinderNode(Document mvcXml) {
	Element rootElement = (Element) mvcXml.getFirstChild();

	/*
	 *
	 * <beanclass=
	 * "org.springframework.web.servlet.mvc.annotation.AnnotationMethodHandlerAdapter"
	 * > <property name="webBindingInitializer"> <bean
	 * class="es.gva.cit.compsiteid.PropertyEditorsInitializer"/>
	 * </property> </bean>
	 */
	Element annotationMethodHandlerElement = XmlUtils.findFirstElement(
		"/beans/bean[@class='" + ANNOTATION_METHOD_HANDLE_CLASS + "']",
		rootElement);
	Element webBindingInitializerProperty = null;
	if (annotationMethodHandlerElement == null) {
	    annotationMethodHandlerElement = mvcXml.createElement("bean");
	    annotationMethodHandlerElement.setAttribute("class",
		    ANNOTATION_METHOD_HANDLE_CLASS);
	    rootElement.appendChild(annotationMethodHandlerElement);
	    // rootElement.insertBefore(rootElement.getChildNodes().item(1),
	    // annotationMethodHandlerElement);
	} else {

	    webBindingInitializerProperty = XmlUtils.findFirstElement(
		    "//property[@name='" + WEB_BINDIN_INITIALIZER_PROPERTY_NAME
			    + "']", annotationMethodHandlerElement);
	}

	if (webBindingInitializerProperty == null) {
	    webBindingInitializerProperty = mvcXml.createElement("property");
	    webBindingInitializerProperty.setAttribute("name",
		    WEB_BINDIN_INITIALIZER_PROPERTY_NAME);
	    annotationMethodHandlerElement
		    .appendChild(webBindingInitializerProperty);
	}

	Element webBindingInitializerBean;
	if (webBindingInitializerProperty.getChildNodes() == null
		|| webBindingInitializerProperty.getChildNodes().getLength() == 0) {
	    webBindingInitializerBean = mvcXml.createElement("bean");
	    webBindingInitializerProperty
		    .appendChild(webBindingInitializerBean);
	} else {

	    List<Element> webBindingInitializerProperties = XmlUtils
		    .findElements("//property[@name='"
			    + WEB_BINDIN_INITIALIZER_PROPERTY_NAME + "']",
			    annotationMethodHandlerElement);
	    Assert.isTrue(webBindingInitializerProperties.size() == 1,
		    "More than 1 element in 'webBindingInitializer' property");
	    webBindingInitializerBean = XmlUtils.findFirstElement("//bean",
		    webBindingInitializerProperties.get(0));
	}

	return webBindingInitializerBean;

    }

    /**
     * Return the asbolute path to the XML MVC config file.
     *
     * @return
     */
    public String getPathToMvcConfig() {
	return pathResolver.getIdentifier(Path.SRC_MAIN_WEBAPP,
		"WEB-INF/spring/webmvc-config.xml");
    }

    /*
     * (non-Javadoc)
     *
     * @seeorg.es.gva.cit.web_binder.roo.addon.WebBinderOperations#setup(org.
     * springframework.roo.model.JavaType, boolean)
     */
    public void setup(JavaType initializerClass, boolean stringEmptyAsNull) {
	setupMvcConfig(initializerClass);

	generateJavaFile(initializerClass, stringEmptyAsNull);
    }

    /**
     * Generates Java file for <code>initializerClass</code>
     *
     * @param initializerClass
     *            class to generate to use as initializer
     * @param stringEmptyAsNull
     *            Adds to the generated class the
     *            <code>StringTrimmerEditor</code> to prevent persisting empty
     *            strings.
     */
    private void generateJavaFile(JavaType initializerClass,
	    boolean stringEmptyAsNull) {

	String ressourceIdentifier = classpathOperations
		.getPhysicalLocationCanonicalPath(initializerClass,
			Path.SRC_MAIN_JAVA);

	String declaredByMetadataId = PhysicalTypeIdentifier.createIdentifier(
		initializerClass, pathResolver.getPath(ressourceIdentifier));

	if (fileManager.exists(ressourceIdentifier)) {
	    PhysicalTypeMetadata ptm = (PhysicalTypeMetadata) metadataService
		    .get(declaredByMetadataId);
	    if (ptm != null
		    && ptm.getPhysicalTypeDetails() != null
		    || !(ptm.getPhysicalTypeDetails() instanceof ClassOrInterfaceTypeDetails)) {
		ClassOrInterfaceTypeDetails cid = (ClassOrInterfaceTypeDetails) ptm
			.getPhysicalTypeDetails();

		if (cid
			.getImplementsTypes()
			.contains(
				new JavaType(
					"org.springframework.web.bind.support.WebBindingInitializer"))) {
		    //it's a valid initializer
		    if (stringEmptyAsNull){
			logger.warning("The class "+ initializerClass + " alredy exist. Check for 'StringTrimmerEditor' manually" );
		    }
		    return;

		} else {
		    throw new IllegalStateException("The class "+ initializerClass + " exists but doesn't implements 'org.springframework.web.bind.support.WebBindingInitializer'.");
		}
	    }

	}

	List<MethodMetadata> declaredMethods = new ArrayList<MethodMetadata>(1);
	if (stringEmptyAsNull) {
	    declaredMethods
		    .add(getInitBinderMethodWithNotEmptyStrings(declaredByMetadataId));
	} else {
	    declaredMethods
		    .add(getInitBinderMethod(declaredByMetadataId, null));
	}

	List<JavaType> implementsTypes = new ArrayList<JavaType>();
	implementsTypes.add(new JavaType(
		"org.springframework.web.bind.support.WebBindingInitializer"));

	ClassOrInterfaceTypeDetails details = new DefaultClassOrInterfaceTypeDetails(
		declaredByMetadataId, initializerClass, Modifier.PUBLIC,
		PhysicalTypeCategory.CLASS, null, null, declaredMethods, null,
		null, implementsTypes, null, null);

	classpathOperations.generateClassFile(details);

    }

    /**
     * Prepares the XML MVC config file to use <code>initializerClass</code> as
     * the default binding initializer
     *
     * @param initializerClass
     */
    private void setupMvcConfig(JavaType initializerClass) {
	String pathToMvcConfig = getPathToMvcConfig();
	Assert.isTrue(fileManager.exists(pathToMvcConfig),
		"MVC config file not found");

	MutableFile mvcXmlMutableFile = null;
	Document mvcXml;
	try {
	    mvcXmlMutableFile = fileManager.updateFile(pathToMvcConfig);
	    mvcXml = XmlUtils.getDocumentBuilder().parse(
		    mvcXmlMutableFile.getInputStream());
	} catch (Exception ex) {
	    throw new IllegalStateException(ex);
	}

	Element webBindingInitializerBean = getMvcWebInitBinderNode(mvcXml);
	webBindingInitializerBean.setAttribute("class", initializerClass
		.getFullyQualifiedTypeName());

	XmlUtils.writeXml(mvcXmlMutableFile.getOutputStream(), mvcXml);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.es.gva.cit.web_binder.roo.addon.WebBinderOperations#isDropAvailable()
     */
    public boolean isDropAvailable() {
	return isAllredySetup();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.es.gva.cit.web_binder.roo.addon.WebBinderOperations#drop()
     */
    public void drop() {
	JavaType classInitialize = getInitializerClaseRegistered();
	if (classInitialize == null) {
	    return;
	}
	dropMvcConfig();

	// XXX: Remove class???
    }

    /**
     * Removes form XML MVC config file the current binding initialization
     * configuration
     */
    private void dropMvcConfig() {
	String pathToMvcConfig = getPathToMvcConfig();
	Assert.isTrue(fileManager.exists(pathToMvcConfig),
		"MVC config file not found");

	MutableFile mvcXmlMutableFile = null;
	Document mvcXml;
	try {
	    mvcXmlMutableFile = fileManager.updateFile(pathToMvcConfig);
	    mvcXml = XmlUtils.getDocumentBuilder().parse(
		    mvcXmlMutableFile.getInputStream());
	} catch (Exception ex) {
	    throw new IllegalStateException(ex);
	}

	Element rootElement = (Element) mvcXml.getDocumentElement();

	Element annotationMethodHandlerElement = XmlUtils.findFirstElement(
		"/beans/bean[@class='" + ANNOTATION_METHOD_HANDLE_CLASS + "']",
		rootElement);
	if (annotationMethodHandlerElement == null) {
	    // Nothing to do
	    return;
	}
	Element webBindingInitializerProperty = XmlUtils.findFirstElement(
		"//property[@name='" + WEB_BINDIN_INITIALIZER_PROPERTY_NAME
			+ "']", annotationMethodHandlerElement);
	if (webBindingInitializerProperty == null) {
	    // Nothing to do
	    return;
	}

	annotationMethodHandlerElement
		.removeChild(webBindingInitializerProperty);

	XmlUtils.writeXml(XmlUtils.createIndentingTransformer(),mvcXmlMutableFile.getOutputStream(), mvcXml);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.es.gva.cit.web_binder.roo.addon.WebBinderOperations#isAddAvailable()
     */
    public boolean isAddAvailable() {
	return isAllredySetup();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.es.gva.cit.web_binder.roo.addon.WebBinderOperations#add(org.
     * springframework.roo.model.JavaType,
     * org.springframework.roo.model.JavaType)
     */
    public void add(JavaType target, JavaType editor) {
	// TODO Auto-generated method stub

    }

    /**
     * Generates the metadata for the method <code>initBinder</code><br/>
     * This will use to generate the class.
     *
     * @param declaredByMetadataId
     *            container-class's identify
     * @param body
     *            String of the method body
     * @return Method's metadata
     */
    private MethodMetadata getInitBinderMethod(String declaredByMetadataId,
	    String body) {
	if (body == null) {
	    body = "";
	}

	// Params
	List<JavaType> params = new ArrayList<JavaType>(2);
	params.add(new JavaType("org.springframework.web.bind.WebDataBinder"));
	params.add(new JavaType(
		"org.springframework.web.context.request.WebRequest"));

	// Params Names
	List<JavaSymbolName> paramNames = new ArrayList<JavaSymbolName>(2);
	paramNames.add(new JavaSymbolName("binder"));
	paramNames.add(new JavaSymbolName("request"));

	// Annotations
	List<AnnotationMetadata> annotations = new ArrayList<AnnotationMetadata>(
		1);
	annotations.add(new DefaultAnnotationMetadata(new JavaType(
		Override.class.getName()),
		new ArrayList<AnnotationAttributeValue<?>>()));

	return new DefaultMethodMetadata(declaredByMetadataId, Modifier.PUBLIC,
		new JavaSymbolName("initBinder"), JavaType.VOID_PRIMITIVE,
		AnnotatedJavaType.convertFromJavaTypes(params), paramNames,
		annotations, null, body);

    }

    /**
     * Generates the metadata for the method <code>initBinder</code> including
     * initialization of <code>StringTrimmerEditor</code>
     *
     * @param declaredByMetadataId
     *            container-class's identify
     * @return Method's metadata
     */
    private MethodMetadata getInitBinderMethodWithNotEmptyStrings(
	    String declaredByMetadataId) {

	InvocableMemberBodyBuilder bodyBuilder = new InvocableMemberBodyBuilder();
	bodyBuilder
		.appendFormalLine("binder.registerCustomEditor(java.lang.String.class, new org.springframework.beans.propertyeditors.StringTrimmerEditor(true));");

	return getInitBinderMethod(declaredByMetadataId, bodyBuilder
		.getOutput());
    }

    /**
     * Generate a new version of <code>initiBinding</code> method that include
     * the new editor's config.
     *
     * @param declaredByMetadataId
     * @param original
     * @param target
     * @param editor
     * @return
     */
    private MethodMetadata getUpdatedMethod(String declaredByMetadataId,
	    MethodMetadata original, JavaType target, JavaType editor) {
	// TODO
	return null;
    }

    /*
     * (non-Javadoc)
     *
     * @seeorg.es.gva.cit.web_binder.roo.addon.WebBinderOperations#
     * clearCurrentIntializer()
     */
    public void clearCurrentIntializer() {
	this.currentInitializer = null;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.es.gva.cit.web_binder.roo.addon.WebBinderOperations#getCurrentInitializer
     * ()
     */
    public JavaType getCurrentInitializer() {
	if (this.currentInitializer == null) {
	    return getInitializerClaseRegistered();
	}
	return this.currentInitializer;
    }

    public void renameInitializer(JavaType newType) {
	setupMvcConfig(newType);
    }

}