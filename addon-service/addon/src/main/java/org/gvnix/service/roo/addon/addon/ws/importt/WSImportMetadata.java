/*
 * gvNIX. Spring Roo based RAD tool for Conselleria d'Infraestructures i
 * Transport - Generalitat Valenciana Copyright (C) 2010 CIT - Generalitat
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
package org.gvnix.service.roo.addon.addon.ws.importt;

import com.github.antlrjavaparser.JavaParser;
import com.github.antlrjavaparser.ParseException;
import com.github.antlrjavaparser.api.CompilationUnit;
import com.github.antlrjavaparser.api.body.BodyDeclaration;
import com.github.antlrjavaparser.api.body.ClassOrInterfaceDeclaration;
import com.github.antlrjavaparser.api.body.MethodDeclaration;
import com.github.antlrjavaparser.api.body.Parameter;
import com.github.antlrjavaparser.api.body.TypeDeclaration;
import com.github.antlrjavaparser.api.expr.NameExpr;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang3.Validate;
import org.gvnix.service.roo.addon.addon.security.SecurityService;
import org.gvnix.service.roo.addon.addon.util.WsdlParserUtils;
import org.gvnix.service.roo.addon.addon.ws.WSConfigService.WsType;
import org.gvnix.service.roo.addon.annotations.GvNIXWebServiceProxy;
import org.springframework.roo.classpath.PhysicalTypeIdentifierNamingUtils;
import org.springframework.roo.classpath.PhysicalTypeMetadata;
import org.springframework.roo.classpath.details.MethodMetadata;
import org.springframework.roo.classpath.details.MethodMetadataBuilder;
import org.springframework.roo.classpath.details.annotations.AnnotatedJavaType;
import org.springframework.roo.classpath.details.annotations.AnnotationMetadata;
import org.springframework.roo.classpath.details.annotations.populator.AutoPopulate;
import org.springframework.roo.classpath.details.annotations.populator.AutoPopulationUtils;
import org.springframework.roo.classpath.itd.AbstractItdTypeDetailsProvidingMetadataItem;
import org.springframework.roo.classpath.itd.InvocableMemberBodyBuilder;
import org.springframework.roo.metadata.MetadataIdentificationUtils;
import org.springframework.roo.model.DataType;
import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.project.LogicalPath;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

/**
 * gvNix Web Service Java proxy generation.
 * <p>
 * Compatible address should be SOAP protocol version 1.1 and 1.2.
 * </p>
 * 
 * @author Mario Martínez Sánchez( mmartinez at disid dot com ) at <a
 *         href="http://www.disid.com">DiSiD Technologies S.L.</a> made for <a
 *         href="http://www.cit.gva.es">Conselleria d'Infraestructures i
 *         Transport</a>
 */
public class WSImportMetadata extends
        AbstractItdTypeDetailsProvidingMetadataItem {

    private static Logger LOGGER = Logger.getLogger(WSImportMetadata.class
            .getName());

    private static final String WEB_SERVICE_TYPE_STRING = WSImportMetadata.class
            .getName();

    private static final String WEB_SERVICE_TYPE = MetadataIdentificationUtils
            .create(WEB_SERVICE_TYPE_STRING);

    private final SecurityService securityService;

    // From annotation
    @AutoPopulate
    private String wsdlLocation;

    public WSImportMetadata(String identifier, JavaType aspectName,
            PhysicalTypeMetadata governorPhysicalTypeMetadata,
            SecurityService secuirityService) {

        super(identifier, aspectName, governorPhysicalTypeMetadata);

        this.securityService = secuirityService;

        Validate.isTrue(isValid(identifier), "Metadata identification string '"
                + identifier + "' does not appear to be valid");

        if (!isValid()) {
            return;
        }

        // Create the metadata.
        AnnotationMetadata annotationMetadata = governorTypeDetails
                .getTypeAnnotation(new JavaType(GvNIXWebServiceProxy.class
                        .getName()));

        if (annotationMetadata != null) {

            // Populate wsdlLocation property class from annotation attribute
            AutoPopulationUtils.populate(this, annotationMetadata);
            LOGGER.log(Level.FINE, "Wsdl location = " + wsdlLocation);

            try {

                // Check URL connection and WSDL format
                Element root = securityService.getWsdl(wsdlLocation)
                        .getDocumentElement();

                // Create Aspect methods related to this wsdl location
                if (WsdlParserUtils.isRpcEncoded(root)) {

                    createAspectMethods(root, WsType.IMPORT_RPC_ENCODED);
                }
                else {

                    createAspectMethods(root, WsType.IMPORT);
                }

            }
            catch (IOException e) {
                LOGGER.log(Level.SEVERE, "Web service has been imported", e);
                throw new IllegalStateException(
                        "Error accessing generated web service sources");

            }
            catch (ParseException e) {
                LOGGER.log(Level.SEVERE, "Web service has been imported", e);
                throw new IllegalStateException(
                        "Error parsing generated web service sources");
            }

            LOGGER.log(Level.FINE, "Web service has been imported");
        }

        // Create a representation of the desired output ITD
        itdTypeDetails = builder.build();
    }

    /**
     * @return WSDL location
     */
    public String getWsdlLocation() {
        return wsdlLocation;
    }

    /**
     * Create methods on Aspect file related to this wsdl location.
     * 
     * @param root Root element of the wsdl document
     * @param sense Communication sense type
     * @throws IOException No connection to the wsdl location
     * @throws SAXException Invalid wsdl format
     * @throws ParseException Generated Java client parse error
     */
    private void createAspectMethods(Element root, WsType sense)
            throws IOException, ParseException {

        // Get the path to the generated service class
        String servicePath = WsdlParserUtils.getServiceClassPath(root, sense);

        // Get the path to the generated port type class
        String portTypePath = WsdlParserUtils.getPortTypeClassPath(root, sense);

        // Get the the port element class name
        String portName = WsdlParserUtils.findFirstCompatiblePortClassName(
                root, sense);

        // Get the port type Java file
        File file = WsdlParserUtils.getPortTypeJavaFile(root, sense);

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

                            createAspectMethod(root, servicePath, portTypePath,
                                    portName, (MethodDeclaration) member, sense);
                        }
                    }
                }
            }
        }
    }

    /**
     * Create method on Aspect file related to method object.
     * 
     * @param root Root element of the wsdl document
     * @param servicePath Path to the service type
     * @param portTypePath Path to the port type type
     * @param portName Name of port name
     * @param method Method to create on AspectJ
     * @param type Communication sense type
     */
    private void createAspectMethod(Element root, String servicePath,
            String portTypePath, String portName, MethodDeclaration method,
            WsType sense) {

        // List to store method parameters types and names
        List<AnnotatedJavaType> javaTypes = new ArrayList<AnnotatedJavaType>();
        List<JavaSymbolName> javaNames = new ArrayList<JavaSymbolName>();

        // Get method parameters and store it on types and names list
        List<Parameter> parameters = method.getParameters();
        if (parameters != null) {
            for (Parameter parameter : parameters) {

                javaTypes.add(new AnnotatedJavaType(getJavaTypeByName(root,
                        parameter.getType().toString()),
                        new ArrayList<AnnotationMetadata>()));
                javaNames.add(new JavaSymbolName(parameter.getId().toString()));
            }
        }

        // List to store throws
        List<JavaType> throwsTypes = new ArrayList<JavaType>();

        // Get throws and store it on throws list
        List<NameExpr> throwsList = method.getThrows();
        if (throwsList != null) {
            for (NameExpr nameExpr : throwsList) {

                throwsTypes.add(getJavaTypeByName(root, nameExpr.toString()));
            }
        }

        // Get the method return type
        String methodType = method.getType().toString();
        JavaType returnType = getJavaTypeByName(root, methodType);

        // Rpc Encoded generated clients includes this Exception by default
        if (sense.equals(WsType.IMPORT_RPC_ENCODED)) {

            throwsTypes.add(new JavaType("javax.xml.rpc.ServiceException"));
        }

        // Create the method body
        InvocableMemberBodyBuilder body = createAspectMethodBody(servicePath,
                portTypePath, portName, method, parameters, returnType);

        // Create the method metadata with previous information
        MethodMetadataBuilder methodMetadataBuilder = new MethodMetadataBuilder(
                getId(), method.getModifiers(), new JavaSymbolName(
                        method.getName()), returnType, javaTypes, javaNames,
                new InvocableMemberBodyBuilder().appendFormalLine(body
                        .getOutput()));
        for (JavaType javaType : throwsTypes) {
            methodMetadataBuilder.addThrowsType(javaType);
        }
        MethodMetadata result = methodMetadataBuilder.build();

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
            MethodDeclaration method, List<Parameter> parameters,
            JavaType returnType) {

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

        // Method invocation
        String returnLine = "";
        if (!returnType.equals(JavaType.VOID_PRIMITIVE)) {

            // Add return directive if not void
            returnLine = returnLine.concat("return ");
        }
        returnLine = returnLine.concat(invocation + ";");
        body.appendFormalLine(returnLine);

        return body;
    }

    /**
     * Get object type or primitive java type related to primitive type name.
     * 
     * @param root Root element of the wsdl document
     * @param name Type name
     * @return Java type
     */
    private JavaType getJavaTypeByName(Element root, String name) {

        JavaType type;
        int index;

        if (getJavaClassPrimitiveByName(name) != null) {

            // Primitive types
            type = new JavaType(getJavaClassPrimitiveByName(name), 0,
                    DataType.PRIMITIVE, null, null);

        }
        else if ("void".equals(name)) {

            // Void type
            type = JavaType.VOID_PRIMITIVE;

        }
        else if (name.trim().endsWith("[]")) {

            // If array, analyze dimensions quantity
            int i = 0;
            while (name.trim().endsWith("[]")) {

                name = name.trim().substring(0, name.trim().length() - 2);
                i++;
            }

            String primitive = getJavaClassPrimitiveByName(name);
            if (primitive == null) {

                // Array of object types
                type = new JavaType(name, i, DataType.TYPE, null, null);
            }
            else {

                // Array of primitive types
                type = new JavaType(primitive, i, DataType.PRIMITIVE, null,
                        null);
            }
        }
        // Object types without package
        else if (name.indexOf('.') == -1) {

            // If not package separator, add generated client package
            type = new JavaType(
                    WsdlParserUtils.getTargetNamespaceRelatedPackage(root)
                            + name);
        }
        // Collection types
        else if ((index = name.indexOf('<')) != -1) {

            // Collection type name an inner type params
            String typeName = name.substring(0, index);
            String typeParam = name.substring(index + 1, name.indexOf('>'));
            LOGGER.log(Level.FINE, "Collection: class=" + typeName + ", type="
                    + typeParam);

            // Include param into List, required by JavaType
            List<JavaType> array = new ArrayList<JavaType>();
            array.add(new JavaType(typeParam));

            type = new JavaType(typeName, 0, DataType.TYPE, null, array);
        }
        // Object types with package
        else {

            type = new JavaType(name);
        }

        return type;
    }

    /**
     * Get the primitive object type name related to a primitive type name.
     * 
     * @param name Type name
     * @return Java object type or null
     */
    private String getJavaClassPrimitiveByName(String name) {

        String type = null;

        if ("boolean".equals(name)) {

            type = Boolean.class.getName();

        }
        else if ("char".equals(name)) {

            type = Character.class.getName();

        }
        else if ("byte".equals(name)) {

            type = Byte.class.getName();

        }
        else if ("short".equals(name)) {

            type = Short.class.getName();

        }
        else if ("int".equals(name)) {

            type = Integer.class.getName();

        }
        else if ("long".equals(name)) {

            type = Long.class.getName();

        }
        else if ("float".equals(name)) {

            type = Float.class.getName();

        }
        else if ("double".equals(name)) {

            type = Double.class.getName();
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

    public static final LogicalPath getPath(String metadataIdentificationString) {
        return PhysicalTypeIdentifierNamingUtils.getPath(
                WEB_SERVICE_TYPE_STRING, metadataIdentificationString);
    }

    public static final String createIdentifier(JavaType javaType,
            LogicalPath path) {
        return PhysicalTypeIdentifierNamingUtils.createIdentifier(
                WEB_SERVICE_TYPE_STRING, javaType, path);
    }

}
