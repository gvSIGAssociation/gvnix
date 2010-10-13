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
package org.gvnix.service.layer.roo.addon;

import japa.parser.JavaParser;
import japa.parser.ParseException;
import japa.parser.ast.CompilationUnit;
import japa.parser.ast.body.BodyDeclaration;
import japa.parser.ast.body.ClassOrInterfaceDeclaration;
import japa.parser.ast.body.MethodDeclaration;
import japa.parser.ast.body.Parameter;
import japa.parser.ast.body.TypeDeclaration;
import japa.parser.ast.expr.NameExpr;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.gvnix.service.layer.roo.addon.annotations.GvNIXWebServiceProxy;
import org.springframework.roo.classpath.PhysicalTypeIdentifierNamingUtils;
import org.springframework.roo.classpath.PhysicalTypeMetadata;
import org.springframework.roo.classpath.details.*;
import org.springframework.roo.classpath.details.annotations.*;
import org.springframework.roo.classpath.details.annotations.populator.AutoPopulate;
import org.springframework.roo.classpath.details.annotations.populator.AutoPopulationUtils;
import org.springframework.roo.classpath.itd.AbstractItdTypeDetailsProvidingMetadataItem;
import org.springframework.roo.classpath.itd.InvocableMemberBodyBuilder;
import org.springframework.roo.metadata.MetadataIdentificationUtils;
import org.springframework.roo.model.*;
import org.springframework.roo.project.Path;
import org.springframework.roo.support.util.Assert;
import org.springframework.roo.support.util.XmlUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

/**
 * gvNix Web Service Java proxy generation.
 * 
 * <p>
 * Compatible address should be SOAP protocol version 1.1 and 1.2.
 * </p>
 * 
 * @author Mario Martínez Sánchez( mmartinez at disid dot com ) at <a
 *         href="http://www.disid.com">DiSiD Technologies S.L.</a> made for <a
 *         href="http://www.cit.gva.es">Conselleria d'Infraestructures i
 *         Transport</a>
 */
public class ServiceLayerWSImportMetadata extends
	AbstractItdTypeDetailsProvidingMetadataItem {

    private static Logger logger = Logger
	    .getLogger(ServiceLayerWSImportMetadata.class
		    .getName());

    private static final String WEB_SERVICE_TYPE_STRING = ServiceLayerWSImportMetadata.class
	    .getName();
    private static final String WEB_SERVICE_TYPE = MetadataIdentificationUtils
	    .create(WEB_SERVICE_TYPE_STRING);

    // From annotation
    @AutoPopulate
    private String wsdlLocation;

    public ServiceLayerWSImportMetadata(String identifier, JavaType aspectName,
	    PhysicalTypeMetadata governorPhysicalTypeMetadata) {

	super(identifier, aspectName, governorPhysicalTypeMetadata);

	Assert.isTrue(isValid(identifier), "Metadata identification string '"
		+ identifier + "' does not appear to be valid");

	if (!isValid()) {
	    return;
	}

	// Create the metadata.
	AnnotationMetadata annotationMetadata = MemberFindingUtils
		.getTypeAnnotation(governorTypeDetails, new JavaType(
			GvNIXWebServiceProxy.class.getName()));

	if (annotationMetadata != null) {

	    // Populate wsdlLocation property class from annotation attribute
	    AutoPopulationUtils.populate(this, annotationMetadata);
	    logger.log(Level.FINE, "Wsdl location = " + wsdlLocation);

	    try {

		// Parse the wsdl location to a DOM document
		Document wsdl = XmlUtils.getDocumentBuilder().parse(
			wsdlLocation);
		Element root = wsdl.getDocumentElement();
		Assert.notNull(root, "No valid document format");

		// Generate source code client clases with CXF
		if (generateSources() == 0) {

		    // Create Aspect methods related to this wsdl location
		    createAspectMethods(root);
		}
		else {
		    
		    // The wsdl is RPC/encoded
		    // TODO Use Axis library to be compatible with Rpc/encoded
		    Assert.state(false,
			    "TODO Currently rpc/encoded wsdls not supported");
		}

		// TODO Add wsdl location to pom.xml here instead on command
		// serviceLayerWsConfigService.addImportLocation(wsdlLocation);

	    } catch (SAXException e) {

		Assert.state(false,
			"The format of the web service to import has errors");

	    } catch (IOException e) {

		Assert.state(false,
			"There is no connection to the web service to import");
		
	    } catch (ParseException e) {

		Assert.state(false,
			"Generated web service client has errors");
	    }

	    logger.log(Level.INFO, "Web service has been imported");
	}

	// Create a representation of the desired output ITD
	itdTypeDetails = builder.build();
    }

    /**
     * Maven generate sources.
     * 
     * <p>
     * If return value is not 0, the service can be Rpc/encoded.
     * </p>
     * 
     * TODO Check on windows environment.
     * 
     * @return exit value, the value 0 indicates normal termination
     * @throws IOException
     *             Error on maven generate sources execution
     */
    public int generateSources() throws IOException {

	// Create windows or linux command
	String cmd = null;
	if (File.separatorChar == '\\') {
	    
	    cmd = "mvn.bat " + "generate-sources";
	    
	} else {
	    
	    cmd = "mvn " + "generate-sources";
	}

	// Execute command
	Process p = Runtime.getRuntime().exec(cmd, null, new File("." + File.separator));

	try {

	    // Wait command to end
	    p.waitFor();
	    
	} catch (InterruptedException e) {
	    
	    throw new IllegalStateException(e);
	}
	
	return p.exitValue();
    }

    /**
     * Create methods on Aspect file related to this wsdl location.
     * 
     * @param root
     *            Root element of the wsdl document
     * @throws IOException
     *             No connection to the wsdl location
     * @throws SAXException
     *             Invalid wsdl format
     * @throws ParseException
     *             Generated Java client parse error
     */
    private void createAspectMethods(Element root) throws SAXException, IOException,
	    ParseException {

	// Get the path to the generated service class
	String servicePath = WsdlParserUtils.getServiceClassPath(root);

	// Get the path to the generated port type class
	String portTypePath = WsdlParserUtils.getPortTypeClassPath(root);

	// Get the the port element class name
	String portName = WsdlParserUtils.findFirstCompatiblePortClassName(root);

	// Get the port type Java file
	File file = WsdlParserUtils.getPortTypeJavaFile(root);

	// Parse the port type Java file
	CompilationUnit unit = JavaParser.parse(file);

	// Get the first class or interface Java type
	List<TypeDeclaration> types = unit.getTypes();
	if (types != null) {
	    TypeDeclaration type = types.get(0);
	    if (type instanceof ClassOrInterfaceDeclaration) {
		
		// Get all methods
		List<BodyDeclaration> members = type.getMembers();
		if (members != null) {
		    for (BodyDeclaration member : members) {
			if (member instanceof MethodDeclaration) {

			    createAspectMethod(servicePath, portTypePath,
				    portName, (MethodDeclaration) member);
			}
		    }
		}
	    }
	}
    }
    
    /**
     * Create method on Aspect file related to method object.
     * 
     * @param servicePath Path to the service type
     * @param portTypePath Path to the port type type
     * @param portName Name of port name
     * @param method Method to create on AspectJ
     */
    private void createAspectMethod(String servicePath, String portTypePath,
	    String portName, MethodDeclaration method) {

	// List to store method parameters types and names
	List<AnnotatedJavaType> javaTypes = new ArrayList<AnnotatedJavaType>();
	List<JavaSymbolName> javaNames = new ArrayList<JavaSymbolName>();
	
	// Get method parameters and store it on types and names list
	List<Parameter> parameters = method.getParameters();
	if (parameters != null) {
	    for (Parameter parameter : parameters) {

		javaTypes.add(new AnnotatedJavaType(getJavaTypeByName(parameter
			.getType().toString()), null));
		javaNames.add(new JavaSymbolName(parameter.getId().toString()));
	    }
	}

	// List to store throws
	List<JavaType> throwsTypes = new ArrayList<JavaType>();
	
	// Get throws and store it on throws list
	List<NameExpr> throwsList = method.getThrows();
	if (throwsList != null) {
	    for (NameExpr nameExpr : throwsList) {

		throwsTypes.add(new JavaType(nameExpr.toString()));
	    }
	}

	// Create the method body
	InvocableMemberBodyBuilder body = createAspectMethodBody(servicePath,
		portTypePath, portName, method, parameters);

	// Get the method return type
	String methodType = method.getType().toString();
	JavaType returnType = getJavaTypeByName(methodType);
	    
	// Create the method metadata with previous information
	MethodMetadata result = new DefaultMethodMetadata(getId(), method
		.getModifiers(), new JavaSymbolName(method.getName()),
		returnType, javaTypes, javaNames,
		new ArrayList<AnnotationMetadata>(), throwsTypes, body
			.getOutput());
	
	// Build the method
	builder.addMethod(result);
    }

    /**
     * Create method on Aspect file related to method object.
     * 
     * @param servicePath Path to the service type
     * @param portTypePath Path to the port type type
     * @param portName Name of port name
     * @param method Method to create on AspectJ
     */
    private InvocableMemberBodyBuilder createAspectMethodBody(
	    String servicePath, String portTypePath, String portName,
	    MethodDeclaration method, List<Parameter> parameters) {
	
	InvocableMemberBodyBuilder body = new InvocableMemberBodyBuilder();
	
	// Create the service
	body.appendFormalLine(servicePath + " s = new " + servicePath + "();");
	
	// Get the port type from service
	body.appendFormalLine(portTypePath + " p = s.get" + portName + "();");
	
	// Invoke the port type method with params
	StringBuilder invocation = new StringBuilder();
	invocation.append("p." + method.getName() + "(");
	if (parameters != null) {

	    boolean first = true;
	    for (Parameter parameter : parameters) {
		if (!first) {

		    invocation.append(", ");
		}
		invocation.append(parameter.getId());
		first = false;
	    }
	}
	invocation.append(")");
	
	// Return the method invocation
	body.appendFormalLine("return " + invocation + ";");
	
	return body;
    }

    /**
     * Get object type or primitive java type related to primitive type name.
     * 
     * <p>
     * TODO ¿ What happends if method returns null or object ?
     * </p>
     * 
     * @param name
     *            Type name
     * @return Java type
     */
    private JavaType getJavaTypeByName(String name) {

	JavaType type;

	try {

	    // Types
	    type = new JavaType(name);

	} catch (IllegalArgumentException e) {

	    // Primitives
	    
	    if ("boolean".equals(name)) {

		type = new JavaType(Boolean.class.getName(), 0,
			DataType.PRIMITIVE, null, null);

	    } else if ("char".equals(name)) {

		type = new JavaType(Character.class.getName(), 0,
			DataType.PRIMITIVE, null, null);

	    } else if ("byte".equals(name)) {

		type = new JavaType(Byte.class.getName(), 0,
			DataType.PRIMITIVE, null, null);

	    } else if ("short".equals(name)) {

		type = new JavaType(Short.class.getName(), 0,
			DataType.PRIMITIVE, null, null);

	    } else if ("int".equals(name)) {

		type = new JavaType(Integer.class.getName(), 0,
			DataType.PRIMITIVE, null, null);

	    } else if ("long".equals(name)) {

		type = new JavaType(Long.class.getName(), 0,
			DataType.PRIMITIVE, null, null);

	    } else if ("float".equals(name)) {

		type = new JavaType(Float.class.getName(), 0,
			DataType.PRIMITIVE, null, null);

	    } else if ("double".equals(name)) {

		type = new JavaType(Double.class.getName(), 0,
			DataType.PRIMITIVE, null, null);

	    } else {

		throw new IllegalStateException("Unsupported primitive " + name);
	    }
	}

	return type;
    }
    
    public static String getMetadataIdentiferType() {
	return WEB_SERVICE_TYPE;
    }

    public static boolean isValid(String metadataIdentificationString) {
	return PhysicalTypeIdentifierNamingUtils.isValid(
		WEB_SERVICE_TYPE_STRING, metadataIdentificationString);
    }

    public static final JavaType getJavaType(String metadataIdentificationString) {
	return PhysicalTypeIdentifierNamingUtils.getJavaType(
		WEB_SERVICE_TYPE_STRING, metadataIdentificationString);
    }

    public static final Path getPath(String metadataIdentificationString) {
	return PhysicalTypeIdentifierNamingUtils.getPath(
		WEB_SERVICE_TYPE_STRING, metadataIdentificationString);
    }

    public static final String createIdentifier(JavaType javaType, Path path) {
	return PhysicalTypeIdentifierNamingUtils.createIdentifier(
		WEB_SERVICE_TYPE_STRING, javaType, path);
    }

}
