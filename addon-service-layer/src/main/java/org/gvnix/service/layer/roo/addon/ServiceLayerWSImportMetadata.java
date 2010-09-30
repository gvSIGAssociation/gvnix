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
import org.springframework.roo.support.util.StringUtils;
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
	    .getLogger(ServiceLayerWSImportMetadataNotificationListener.class
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
		
		// TODO Check
		mvn();

		// Create methods on Aspect file related to this wsdl location
		createAspectMethods();
		
		// TODO Add wsdl location to pom.xml here instead of on command
//		serviceLayerWsConfigService.addImportLocation(wsdlLocation);

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
	 * 
	 * @throws IOException
	 */
	public void mvn() throws IOException {

		String cmd = null;
		if (File.separatorChar == '\\') {
			cmd = "mvn.bat " + "generate-sources";
		} else {
			cmd = "mvn " + "generate-sources";
		}
		
		// TODO "./" works in Windows ?
		Process p = Runtime.getRuntime().exec(cmd, null, new File("./"));

		try {
			p.waitFor();
		} catch (InterruptedException e) {
			throw new IllegalStateException(e);
		}
		
	}
    /**
     * Create methods on Aspect file related to this wsdl location.
     * 
     * <p>
     * Compatible address should be SOAP protocol version 1.1 and 1.2.
     * </p>
     * 
     * @throws IOException
     *             No connection to the wsdl location
     * @throws SAXException
     *             Invalid wsdl format
     * @throws ParseException
     *             Generated Java client parse error
     */
    private void createAspectMethods() throws SAXException, IOException,
	    ParseException {

	// Parse the wsdl location to a DOM document
	Document wsdl = XmlUtils.getDocumentBuilder().parse(wsdlLocation);
	Element root = wsdl.getDocumentElement();
	Assert.notNull(root, "No valid document format");

	// Get the path to the generated service class
	String servicePath = WsdlParserUtils.getServiceClassPath(root);

	// Get the path to the generated port type class
	String portTypePath = WsdlParserUtils.getPortTypeClassPath(root);

	// Get the the port element name
	String portName = WsdlParserUtils.findFirstCompatiblePortName(root);

	// Get
	File file = new File("./target/generated-sources/cxf", portTypePath
		.replace(".", "/").concat(".java"));
	CompilationUnit unit = JavaParser.parse(file);
	List<TypeDeclaration> types = unit.getTypes();
	if (types != null && !types.isEmpty()) {
	    TypeDeclaration type = types.get(0);
	    if (type instanceof ClassOrInterfaceDeclaration) {
		List<BodyDeclaration> members = type.getMembers();
		if (members != null) {
		    for (BodyDeclaration member : members) {
			if (member instanceof MethodDeclaration) {
			    
			    

			    MethodDeclaration method = (MethodDeclaration) member;

			    logger.log(Level.INFO, "Name: " + method.getName());
			    List<Parameter> parameters = method.getParameters();
			    List<AnnotatedJavaType> javaTypes = new ArrayList<AnnotatedJavaType>();
			    List<JavaSymbolName> javaNames = new ArrayList<JavaSymbolName>();
			    if (parameters != null) {
				for (Parameter parameter : parameters) {
				    logger.log(Level.INFO, "Parameter: "
					    + parameter.getType() + " "
					    + parameter.getId());
				    javaTypes.add(new AnnotatedJavaType(
					    new JavaType(parameter.getType()
						    .toString()), null));
				    javaNames.add(new JavaSymbolName(parameter
					    .getId().toString()));
				}
			    }
			    logger.log(Level.INFO, "Type: " + method.getType());

			    List<NameExpr> throwsList = method.getThrows();
			    List<JavaType> throwsTypes = new ArrayList<JavaType>();
			    if (throwsList != null) {

				for (NameExpr nameExpr : throwsList) {

				    throwsTypes.add(new JavaType(nameExpr
					    .toString()));
				}
			    }

			    // TODO Completar
			    InvocableMemberBodyBuilder body = new InvocableMemberBodyBuilder();
			    body.appendFormalLine(servicePath + " s = new "
				    + servicePath + "();");
			    body.appendFormalLine(portTypePath + " p = s.get"
				    + StringUtils.capitalize(portName) + "();");
			    StringBuilder invocation = new StringBuilder();
			    invocation.append("p." + method.getName() + "(");
			    boolean first = true;
			    if (parameters != null) {
				if (!first) {
				    invocation.append(" ,");
				}
				for (Parameter parameter : parameters) {
				    invocation.append(parameter.getId());
				}
				first = false;
			    }
			    invocation.append(")");
			    body.appendFormalLine("return " + invocation + ";");
			    
			    JavaType returnType = null; 
			    try {
				
				// TODO ¿ What happends if method return null or object ?
				returnType = new JavaType(method.getType().toString());
			    }
			    catch (IllegalArgumentException e) {
				
				if (method.getType().toString().equals("boolean")) {
				    returnType = new JavaType(Boolean.class.getName(), 0, DataType.PRIMITIVE, null, null);
				}
				else if (method.getType().toString().equals("char")) {
				    returnType = new JavaType(Character.class.getName(), 0, DataType.PRIMITIVE, null, null);
				}
				else if (method.getType().toString().equals("byte")) {
				    returnType = new JavaType(Byte.class.getName(), 0, DataType.PRIMITIVE, null, null);
				}
				else if (method.getType().toString().equals("short")) {
				    returnType = new JavaType(Short.class.getName(), 0, DataType.PRIMITIVE, null, null);
				}
				else if (method.getType().toString().equals("int")) {
				    returnType = new JavaType(Integer.class.getName(), 0, DataType.PRIMITIVE, null, null);
				}
				else if (method.getType().toString().equals("long")) {
				    returnType = new JavaType(Long.class.getName(), 0, DataType.PRIMITIVE, null, null);
				}
				else if (method.getType().toString().equals("float")) {
				    returnType = new JavaType(Float.class.getName(), 0, DataType.PRIMITIVE, null, null);
				}
				else if (method.getType().toString().equals("double")) {
				    returnType = new JavaType(Double.class.getName(), 0, DataType.PRIMITIVE, null, null);
				}
				else {
				    throw new IllegalStateException("Unsupported primitive '" + method.getType().toString() + "'");    
				}
			    }
			    
			    MethodMetadata result = new DefaultMethodMetadata(
				    getId(), method.getModifiers(),
				    new JavaSymbolName(method.getName()),
				    returnType,
				    javaTypes, javaNames,
				    new ArrayList<AnnotationMetadata>(),
				    throwsTypes, body.getOutput());
			    builder.addMethod(result);
			}
		    }
		}
	    }
	}
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
