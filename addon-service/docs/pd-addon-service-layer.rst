==================================
 gvNIX Service Layer Add-on
==================================

:Project:   gvNIX. Spring Roo based RAD tool
:Copyright: Conselleria d'Infraestructures i Transport - Generalitat Valenciana
:Author:    DiSiD Technologies, S.L.
:Revision:  $Rev$
:Date:      $Date$

.. contents::
   :depth: 2
   :backlinks: none

This work is licensed under the Creative Commons Attribution-Share Alike 3.0
Unported License. To view a copy of this license, visit
http://creativecommons.org/licenses/by-sa/3.0/ or send a letter to
Creative Commons, 171 Second Street, Suite 300, San Francisco, California,
94105, USA.

Introducción - Descripción funcional.
TODO: Confirmar la info del doc de CXF (addon cxf).

Introduction
=============

Configuration will be defined in the classes with gvNIX Annotations and generated source will be published in AspectJ files from this annotations.

**DOCUMENTO EN DESARROLLO**

TBC: Objetivo del addon

Prerequisites
=============

See td-contract-first-from-java.rst

Requirements
=============

TBC:

Requirements are in priority order:

* Web service servers creation without write wsdl and xsd.
  Use contract first model, but generate contract (wsdl + xsd) from Java with a DSL language as Java Annotations and/or AspectJ.

  A change in the source code should not affect the generated service contract (wsdl + xsd).
  If a code change makes inconsistent the relationship with the service annotations, would be required to generate a compilation or startup error.

* Using SOAP binding document / literal versus RPC / encoded servers generated, because RPC is obsolete by WS-I Basic Profile.

* Allow web service servers generation on the service layer o entity layer of Roo.

* Not use interfaces related to the implementation on service layer and entitity layer web service servers generation.

* Web service framework installation will be automatic when client or server generation is required.

* Allow the generation of local services, without web service support (Spring @Service).

* Generate web service clients and servers easily.

* Support web services clients generation compatible with JAX-RPC web service servers standard.

* Web services clients and servers generation would be executable on FUSE ESB / Servicemix environments.

Additionally, There are some limitations on wsdl generation from Java.
Another requirements are to solve or avoid this limitations too.

Use Case
=========

TODO:

TRANSLATE:

Add-on use case publishing a Web Service.

Addon Annotations
-------------------

Anontations used by the Add-on to manager Web Services:

* @GvNIXWebService: Identifies published Web Service class.
* @GvNIXWebMethod: Defines method exported as Web Servicre Operation inside @GvNIXWebService class.
* @GvNIXWebFault: Defines Exception classes involved in Web Service Operations.
* @GvNIXWebParam: Defines input method parameters from Web Service Operation.
* @GvNIXXmlElement: Identifies Xml Element involved in Web Service Operation as input or returnType.
* @GvNIXXmlElementField: Field from @GvNIXXlmElement class.
* @GvNIXWebServiceProxy: Defines Web Service Client endpoint.
* @GvNIXWebServiceSecurity: Add request sign to a Web Service Client endpoint implemented by Axis library.

+Publish Web Service class+
-----------------------------

Define **@GvNIXWebSErvice** with all attributes because are mandatory to publish the class as Web Service.

Annotation ``behavior`` to avoid Web Service Contract::

* Change class name or package. Updates configuration file to avoid compiling errors:

  * targetNamespace in annotation to avoid contract updates.
  * portType avoid contract updates.
  * Check avoid parameters in operation using attribute ``exported = true``.
  * CXF xml configuration file. class attribute.
    * Control publishing a service in xml file.

      * Search for the bean that contains this class name. Don't change the contract.
      * Search for the bean that its id matches serviceName attribute. Don't change the contract.
  * pom.xml cxf plugin class reference. Where class is defined: package + class name.

    * Change execution label from java2ws polugin with the new package/class name.
  * Namespace doesn't change to aviod changes in Web Service Contract. If you would to change it uptade it in **@GvNIXWebService** annotation.

+Publish operation.+
-----------------------

Must **@GvNIXWebMethod** attributes if you add manually the annotation.

Annotation ``behavior`` to avoid Web Service Contract::

* Change method name.

  * Controled by operationName attribute. Doesn't change the contract.

* Change input types.

  * Change type: Managed by attribute type in **@GvNIXWebParam**. If there is a change ``only`` in java code, will throw an exception, you have to change it in annotation if you want ot change the WS-Contract.
  * Change name: Managed by attribute name in **@GvNIXWebParam**. Doesn't changes WS-Contract if only change the name in java code.
* Change return types.

  * Managed with ``webResultType`` attribute in **@GvNIXWebParam**. Has to be the same type as defined in java code.

These behaviors are managed by the MetadataProvider that catches changes in a class annotated with **@GvNIXWebService**.

.. admonition:: Nota:

    If an operation is updated manually updating attributes in **@GvNIXWebMethod** and doesn't complain with defined rules, gvNIX will throw Exception message to complain with defined interoperabily rules.

+Publicar Objetos para la comunicación.+
------------------------------------------
Obligatorios todos los atributos de la anotación de gvNIX.

* Anotados con **@GvNIXXmlElement** para generar el metadato utilizando los atributos de la anotación (name y namespace).
* Si se cambia el paquete o el nombre de la clase, no varía ya que el contrato depende del name y el namespace definido.

+Publicar excepción.+
-----------------------
Obligatorios todos los atributos de la anotación de gvNIX.

* Cambiar el nombre/paquete de la excepción.

  * Comprobar con el valor del atributo faultBean de la anotación **@GvNIXWebFault**, si no coinciden error, es decir no generará el AspectJ asociado para publicar la excepción. Para actualizar se ha de cambiar el valor de faultBean por el nuevo valor del paquete/clase.
  * Si no se comprueba, lanzará un error al compilar el proyecto debido a que el fichero AspectJ no encontrará la clase de la excepción a la que se refiere.
  * Estará controlado por el metadato asociado a la excepción que únicamente comprueba las excepciones definidas del proyecto.

Create a web service defining annotations in classes.
======================================================

Publish Web Service Class
---------------------------

Define *@GvNIXWebService* annotation in class to export as Web Service.

Mandatory ``attributes`` to export a class as Web Service:

  * ``name``: Name for Web Service Port Type definition in WSDL.
  * ``targetNamespace``: Namespace for Web Service in WSDL. i.e.: ``targetNamespace= "http://services.project.layer.service.test.gvnix.org/"``.
  * ``serviceName``: Service name to publish the service in WSDL.
  * ``address``: Address to access to the service in application.
  * ``fullyQualifiedTypeName``: Java fully qualified type name to control if changes the package or class name to avoid updating service contract. i.e.: ``fullyQualifiedTypeName= =org.gvnix.test.service.layer.project.services.Clase"``.
  * ``exported``: Check method input/output parameters when is published as operation if its false. If it's exported this service has been generated from ``wsdl``.

Other *@GvNIXWebService* ``attributes``:

  * ``parameterStyle``: SOAPBinding parameter style for Web Service.

Publish Web Service operation
------------------------------

Define *@GvNIXWebMethod* annotation in method to export as Web Service Operation.

Mandatory ``attributes`` for a method with or without input/output parameters:

  * ``operationName``: Define an operation name to be published.
  * ``webResultType``: Return Java type. i.e.: Return type String: ``webResultType = String.class`` if it's void: ``webResultType = void.class``.

Mandatory ``attributes`` for a method with input parameters:

  * ``requestWrapperName``: Request Wrapper Name in WSDL.
  * ``requestWrapperNamespace``: Request Wrapper Namespace in WSDL.
  * ``requestWrapperClassName``: Fully qualified name for Request Wrapper class. i.e. ``requestWrapperClassName = "org.example.wrapper.RequestWrapper"``.

Other *@GvNIXWebMethod* ``attributes``:

  * ``parameterStyle``: SOAPBinding parameter style for Web Service operation.
  * ``webResultPartName``: Define partName to operation: ``parameters`` or ``body``.
  * ``webResultHeader``: ``true`` or ``false`` depending if the operation uses result header. False by default.

Also you have to define *@GvNIXWebParam* and *@WebParam* annotations for each input parameter:

@GvNIXWebParam mandatory ``attributes``:

  * ``name``: The name of attribute in WSDL.
  * ``type``: Parameter's Java type. i.e.: type String: ``type = String.class``.

@WebParam ``attributes``:

  * ``name``: The same name of attribute name for *@GvNIXWebParam*. The name of attribute in WSDL.
  * ``partName``: Allways set ``partName = "parameters"``.
  * ``mode``: Allways set ``mode = Mode.IN``.
  * ``header``: Allways set ``header = false``.

Mandatory ``attributes`` in *@GvNIXWebMethod* for a method with return type different than void:

  * ``resultName``: Name for result type in WSDL.
  * ``resultNamespace``: Result Namespace in WSDL.
  * ``responseWrapperName``: Response Wrapper Name in WSDL.
  * ``responseWrapperNamespace``:  Namespace for Response Wrapper in WSDL.
  * ``responseWrapperClassName``: Fully qualified name for Response Wrapper class. i.e. ``responseWrapperClassName = "org.example.wrapper.ResponseWrapper"``.

Define Java Object in Web Service Operations.
---------------------------------------------

To define a Java Object which is used in a Web Service Operation as input parameter or return type define *@GvNIXXmlElement* annotation to export the class in XSD into WSDL.

Mandatory ``attributes`` to export a class to XSD schema:

  * ``name``: Name define Object in XSD schema in WSDL.
  * ``namespace``: Object Namespace in XSD schema in WSDL.
  * ``elementList``: Array of field names to be exported as XSD in WSDL schema. i.e.: ``elementList = {"name", "age"}``. The fields that are not defined in array are declared as ``@XmlTransient``.

Other *@GvNIXXmlElement* ``attributes``:

  * ``xmlTypeName``: Name to define in ``@XmlType`` annotation to export into WSDL schema.
  * ``exported``: If object has been created using ``service export ws`` operation value is true. Check if the field are valid defined types to WSDL.
  * ``enumElement``: If class is an ``enumeration`` instead of a class.

You can define *@GvNIXXmlElementField* annotation for each *@GvNIXXmlElement*. Is not a ``mandatory`` annotation.This annotation replicates values from *@XmlElement*:

  * ``defaultValue``: Default value of this element.

  * ``name``: Name of the XML Schema element.

  * ``namespace``: Namespace for XML Schema element. i.e.: ``namespace= "http://services.project.layer.service.test.gvnix.org/"``.

  * ``nillable``: Customize the element declaration to be nillable. Schema element declaration with occurance range of 0..1.

  * ``required``: Customize the element declaration to be required. Schema element declaration must exists.

  * ``type``:  The Java class being referenced.

Publish Web Fault
--------------------

To export and define a Web Fault that is thrown in Web Service Operation you have to define *@GvNIXWebFault* annotation to selected Exception Class.

   .. admonition:: Requirements

       The exception must exist in the project.

Mandatory ``attributes``:

  * ``name``: Name for Web Fault in WSDL.
  * ``targetNamespace``: Namespace for Web Fault in WSDL.
  * ``faultBean``: Fully qualified name for this Exception class. i.e. ``faultBean = "org.example.exception.TestException"``.

This Generates AspectJ file to annotate the exception defined with *@WebFault* values.

Analysis
=========

File Monitoring.

Add-on monitorize java files annotated with **@GvNIX...**, for each one creates its associated AspectJ file where are the jax-ws annotations defined using @GVNIX annotations attributes.

  * Checks correct values aof Annotation attributes before generate ja files.

Este punto será muy útil para la integración con MOSKitt

Analysis for the development of the Add-on displayed by commands.

Create a Service Class
-------------------------

service class:

    Create the class in java package directory that belongs.
    Add **@service** annotation to header's class.

Create an operation into a Class
---------------------------------

service operation:

* Create a method with input name in the selected class. The method is composed by:

    * Return type: The default return type is ``void`` if there is no return type defined.
    * Input parameters: T aren't mandatory to be defined in an operation.

        * Created the class **JavaTypeList** to retrieve the parameter types and manage with the converter *JavaTypeListConverter**.

Export a web service
-------------------------

Command to publish a ¿ service class ? as a web service.

service define ws:

* Add *@GvNixWebService* annotation with the command attributes (name, targetNamespace, etc) or if they hadn't been defined set default values.
* Add CXF dependecies into pom.xml.
* Add jax-ws build into the pom.xml to check the correct service contract generated in compilation before it will be published in execution::

        <plugin>
          <groupId>org.apache.cxf</groupId>
          <artifactId>cxf-java2ws-plugin</artifactId>
          <version>${cxf.version}</version>
          <dependencies>
            <dependency>
              <groupId>org.apache.cxf</groupId>
              <artifactId>cxf-rt-frontend-jaxws</artifactId>
              <version>${cxf.version}</version>
            </dependency>
            <dependency>
              <groupId>org.apache.cxf</groupId>
              <artifactId>cxf-rt-frontend-simple</artifactId>
              <version>${cxf.version}</version>
            </dependency>
          </dependencies>
          <executions>
            <execution>
              <id>generate-car-service-wsdl</id>
              <phase>compile</phase>
              <configuration>
                <className>org.gvnix.test.project.web.services.CarService</className>
                <outputFile>${project.basedir}/src/test/resources/generated/wsdl/CarService.wsdl</outputFile>
                <genWsdl>true</genWsdl>
                <verbose>true</verbose>
              </configuration>
              <goals>
                <goal>java2ws</goal>
              </goals>
            </execution>
          </executions>
        </plugin>

* Add web service definition to CXF config file. Create the file if doesn't exists.
* Create AspectJ file. Associated metadata to service class within CXF annotations:

    * Define @WebService and @SOAPBinding to the published class setting the *@GvNIXWebService* annotation attributes into corresponding CXF annotation properties or default ones.
    * Annotate with *@WebMethod(exclude = true)* all class methods that aren't defined with *@GvNixWebMethod*.

Export a method as web service operation
---------------------------------------------------------

Command to publish a method as web service operation.

service operation:

* Modify method in Class where is defined with **@GvNIXWebMethod** annotation with its mandatory parameters or ¿ defined in inter-operability web service standards by default ?.

  * Create or Modify associated AspectJ file with the new published method. Rebuild with defined values in gVNIX annotations. Associated Metadata generates AspectJ file.
  * Checks if GvNIX annotation is well formed before generate Metadata, if is incorrect shows a message and deletes metadata.
* Add @GvNIXWebFault annotation to method _Exceptions_ if are defined in the project. If Exception are imported create a declaration in an AspectJ file. Associated Metadata generates AspectJ file.

  * Checks if GvNIX annotation is well formed before generate Metadata, if is incorrect shows a message and deletes metadata.
* Checks **Allowed Parameters** involved in operation.

  * If there is a not allowed parameters (input/output) doesn't publish the operation. See: supported data types.
  * Adds **GvNIXXmlElement** annotation to object parameters, if not exists already

Import a web service from a WSDL
----------------------------------

service import ws:

* Create the class defined by the command parameter ``--class`` and annotate it with **@GvNIXWebServiceProxy(wsdlLocation = "<url_of_the_wsld>")**.

If the WSDL is under a secure server and the access is through HTTPS we are facing two possible scenarios:

1. Server with reliable certificate authority:

  In this case the WSDL will be accessible and the add-on will perform all the operations needed in order to create the
  local service.

2. Server with non reliable certificate authority:

  There we need to import the certificates in authentication chain to our keystore, and retry to access the WSDL. In this
  case the add-on will do this operations for us.

  The add-on, in this version, try to use the cacerts keystore under $JAVA_HOME/jre/lib/security/cacerts (this path is
  for Sun JVM) with default ``changeit`` password.

  When the handsake process fails due to the reability on the server certificate, the add-on creates a copy of the cacerts
  keystore under ``src/main/resources/gvnix-cacerts`` and automatically import the certificates in certificate chain to it.
  Also a local copy of the certificates is created under ``src/main/resources/<host>-<cert-index>.cer`` so you can
  distribute them in order to install it to other environments (see keytool command manual). Finally the same certificates
  are installed to the JVM cacerts keystore in ordert to enable the WSDL access. Maybe in a future version this last
  operation will be enabled by a command parameter.

  Now the add-on retries to access the secured WSDL and now it gets the file and performs all the operations needed in
  order to create the local service.

Supported data types
--------------------------

Data types: Basic Data
~~~~~~~~~~~~~~~~~~~~~~~~

Checks which are supported data types that accomplish web service interoperability defined by the Add-on.

All basic data are supported in web services:

*  http://download.oracle.com/docs/cd/E12840_01/wls/docs103/webserv/data_types.html#wp231439

And Basic Objects:

* Long
* String
* Integer
* Boolean
* Short
* Character
* Double

Data types: Collections
~~~~~~~~~~~~~~~~~~~~~~~~

Collections that don't accomplish with web service interoperability::

* Map
* HashMap
* TreeMap

Data types: Project entities
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

Entities defined in the project.
Where a method uses an Entity in operation, the Add-on adds **@GvNIXXmlElement** annotation to the entity to generate a metadata that builds a correct xml format to be sent in web service operation as input/output parameter.
* The fields are checked if they accomplish with web service interoperability.

  * Fields with a database relationship annotation are defined as **@XmlTransient** elements which are not sent in the operation as part of the Entity.
  * The other ones are defined as **@XmlElement** with ``name`` attribute.

Commands
=========

There are defined eight commands in this Add-on:

service class
--------------

Create new Service Class.

Parameters:

  * ``--class`` (mandatory) New Service Class name

service operation
------------------

Creates new operation in the selected class.

Parameters:

  * ``--service`` (mandatory) Class in wich will be created the method.
  * ``--name`` (mandatory) Name of the method to be created.
  * ``--return`` Type of the returning method object. Default void.
  * ``--paramNames`` Method parameter input names.
  * ``--paramTypes`` Method parameter input types.
  * ``--exceptions`` Method exceptions that can be thrown.

service define ws
------------------

Exports a Class to a Web Service.

Parameters:

  * ``--class`` (mandatory) Class to be exported as a Web Service.
  * ``--serviceName`` Name to publish the Web Service.
  * ``--portTypeName`` Name to define the portType.
  * ``--addressName`` Address to publish the Web Service in server. Default class name value.
  * ``--targetNamespace`` Namespace name for the service.

service list operation
----------------------------

List all method from Web Service class that are not exported as Web Service Operation.

Parameters:

  * ``--class`` (mandatory) Class to search methods that are not exported.

service export operation ws
----------------------------

Publish a service method as a Web Service operation.

Parameters:

  * ``--class`` (mandatory) Class to export a method.
  * ``--method`` (mandatory) Method to export.
  * ``--operationName`` Name of the method to be showed as a Web Service operation.
  * ``--resultName`` Method result name.
  * ``--resultNamespace`` Namespace of the result type.
  * ``--responseWrapperName`` Name to define the Response Wrapper Object.
  * ``--responseWrapperNamespace``: Namespace of the Response Wrapper Object.
  * ``--requestWrapperName``: Name to define the Request Wrapper Object.
  * ``--requestWrapperNamespace``: Namespace of the Request Wrapper Object.

service export ws
-------------------

Generates a Service Class using a wsdl definition.

Parameters:

  * ``--wsdl`` (mandatory) Wsdl file location.

service import ws
-------------------

Creates a service class to act as a proxy for the Web Service defined in wsdl.

Parameters:

  * ``--class`` (mandatory) Class to act as a proxy.
  * ``--wsdl`` (mandatory) Location of the remote Web Service.

service ws list
-------------------
Shows a class list with imports and/or exported services.

service security ws
---------------------
Configures request signing of a imported web services. Only supported for a Axis library.

This command is a fist version. In future will be upgraded for support CXF and others security operations (like user validations, etc).

For more information see `WSS4J for Axis`_

Parameters:

  * ``--class`` (mandatory) Class of imported service.
  * ``--certificate`` (mandatory) pkcs12 to use for signing request. This file will be copied to project resources forlder.
  * ``--password`` (mandatory) password for certificate file.
  * ``--alias`` (mandatory) alias to use for signing.
  
Certificate file will be copied to ``src/main/resources/${path_of_class_package}/${certificate_file_name}. I file already exist, the file will be copied with another name (base on a counter).
 


Commands Availability
---------------------

* Local Service Layer commands as class or operation creation only requires a Roo project to be available.
* Import Service Layer commands only requires a Roo project to be available, too.
* Export Service Layer Commannds requires a Roo web project to be available, because web services are published by the web tier.

Proof of Concept
=================

Proof of concept repository location:

Web Service export and export wsdl:

* https://svn.disid.com/svn/disid/proof/gvnix/web-service-server-app

Web Service Client:

* https://svn.disid.com/svn/disid/proof/gvnix/bing-search-app

TBC: The location of the project will be updated when the shell is built

Notes
=======

Referentes a las tareas.

Service Export
===============

Restricciones comando ``service export ws`` para un WSDL:

  * WSDL 1.0
  * Soap 1.1 ó Soap 1.2
  * Document/Literal

Generación XmlElement
-----------------------

El elemento generado *no* tiene la etiqueta XmlElement ya que no está definido en el contrato del servicio WSDL.

Este tipo de casos, los ha de tratar el Add-on replicando código, o son parte del estándar de la definición de un contrato WSDL contract First ?
* Buscar info sobre la definición de un objeto en XSD.

Gestionar XmlElement
-----------------------

Futuras versiones:

* Comnado para gestionar los XmlElement.

Ahora existe para cada **@GvNIXXmlElement** una anotación **@GvNIXXmlElementField** para cada uno de sus campos definidos donde se definen las restricciones del campo.


**service define ws**

Tener en cuenta el atributo **exclude** ya que evita que se comprueben los tipos permitidos en las operaciones de un servicio que se quiere publicar, se puede cambiar a mano pero **NO** garantiza la interoperabilidad del Servicio Web.

ROO 1.1.0-RELEASE
====================

Comprobar la creación de clases y actualización de las mismas.
Definición de Metadatos y Providers.

Import
======

Tipos de servicios:

   1. RPC/encoded
   2. RPC/literal
   3. Document/encoded (Nobody follows this style. It is not WS-I compliant)
   4. Document/literal
   5. Document/literal wrapped

Algoritmo que indica cuando es RPC/Encoded, lo pongo como info y lo pasaremos a la doc.

Analizando el elemento binding, será RPC/Encoded si para alguna de las operaciones de dicho binding se cumple la siguiente condición:

 (en soap:binding el style="rpc" o en operation el style="rpc") y (en operation/input/soap:body el use="encoded" o en operation/output/soap:body el use="encoded")

Solo soportaremos SOAP en esta primera versión. Si hay soap y soap12, tomaremos soap12.

CXF
---

Plugin Maven CXF
~~~~~~~~~~~~~~~~

Plugin Maven creación cliente WS para CXF

Necesita las dependencias cxf-rt-frontend-jaxws, cxf-rt-transports-http y cxf-rt-transports-http-jetty (esta última si no se utiliza CXFServlet).
No se añade la dependencia cxf-rt-transports-http-jetty porque estamos usando el CXFServlet. No se ha necesitado añadir ninguna dependencia adicional.

En la configuración del plugin no se especifica ningún valor para la propiedad <sourceRoot>, se acepta el valor por defecto target/generated-sources/cxf.

<plugin>
    <groupId>org.apache.cxf</groupId>
    <artifactId>cxf-codegen-plugin</artifactId>
    <version>${cxf.version}</version>
    <executions>
        <execution>
            <id>generate-sources</id>
                        <phase>generate-sources</phase>
            <configuration>
                ...
            </configuration>
            <goals>
                <goal>wsdl2java</goal>
            </goals>
        </execution>
    </executions>
</plugin>

    * http://cxf.apache.org/docs/using-cxf-with-maven.html
    * https://cwiki.apache.org/CXF20DOC/maven-cxf-codegen-plugin-wsdl-to-java.html

Crear consumidores

Los consumidores de dos servicios web se crean configurando la ruta a los WSDLs en el plugin cxf-codegen-plugin, dentro de la sección configuration.

                <wsdlOptions>
                    <wsdlOption>
                        <wsdl>wsdl1</wsdl>
                    </wsdlOption>
                    <wsdlOption>
                        <wsdl>wsdl2</wsdl>
                    </wsdlOption>
                </wsdlOptions>

La ruta al wsdl puede ser local o remota, por ejemplo:

    * src/main/resources/HelloWorld.wsdl
    * http://www.w3schools.com/webservices/tempconvert.asmx?WSDL

Entonces al ejecutar la fase mvn generate-sources se generará todo el código Java asociado al cliente de los servicios web en la ruta target/generated-sources/cxf. mvn install también realiza la generación de este código.

    * http://cxf.apache.org/docs/developing-a-consumer.html
    * https://cwiki.apache.org/CXF20DOC/wsdl-to-java.html

Código generado
~~~~~~~~~~~~~~~

    public String SomeService.someOperation() {

    SOAPService service = new GeneratedService();
    Greeter port = ss.getGeneratedPort();

    return port.someOperation();
    }

Este fichero AspectJ será administrado por el addon de service-layer mediante la monitorización de las clases Java que contengan la anotación @GvNIXWebServiceProxy.

Axis
----

Plugin Maven Axis
~~~~~~~~~~~~~~~~~

Plugin Maven creación cliente WS para Axis (compatibles con RPC/Encoded):

Añadir la dependencia a la librería:

          <dependency>
            <groupId>axis</groupId>
            <artifactId>axis</artifactId>
            <version>1.4</version>
          </dependency>

Y configurar el plugin en el pom.xml de Maven:

      <plugin>
        <groupId>org.codehaus.mojo</groupId>
        <artifactId>axistools-maven-plugin</artifactId>
        <version>1.4</version>
        <configuration>
          <urls>
            ...
          </urls>
        </configuration>
        <executions>
          <execution>
            <goals>
              <goal>wsdl2java</goal>
            </goals>
          </execution>
        </executions>
      </plugin>

Más info:

http://mojo.codehaus.org/axistools-maven-plugin/examples/simple.html
http://mojo.codehaus.org/axistools-maven-plugin/usage.html

Crear consumidores

Los consumidores de los servicios web se crean configurando la ruta a los WSDLs en el plugin axistools-maven-plugin, dentro de la sección urls.

            <url>http://pruebas.ha.gva.es/WS_BDC/WSBDC.WebServicios?WSDL</url>

WSS4J for Axis
~~~~~~~~~~~~~~~~~~~~

This library is used to configure request signing.

Related links:

* Main page: http://ws.apache.org/wss4j/

* Documentation: http://ws.apache.org/wss4j/using.html

* Configuration for Axis 1.x: http://ws.apache.org/wss4j/axis.html

We need to use 1.5.x beause 1.6 doesn't support Axis 1.x (only Axix 2.x) [http://ws.apache.org/wss4j/wss4j16.html].


The command performs this actions:

* Add dependecy to WSS4J in pom (if it's needed)::

 	<dependency>
      <groupId>org.apache.ws.security</groupId>
      <artifactId>wss4j</artifactId>
      <version>1.5.11</version>
    </dependency>


* Creates ``src/main/resources/client-config.wsdd`` with the basical content::
	
	<?xml version="1.0" encoding="UTF-8"?>
	<deployment xmlns="http://xml.apache.org/axis/wsdd/" xmlns:java="http://xml.apache.org/axis/wsdd/providers/java">
	 <transport name="http" pivot="java:org.apache.axis.transport.http.HTTPSender"/>
	 <!-- Service signature template
	  <service name="ServiciosMap" >
	   <requestFlow >
	    <handler type="java:org.apache.ws.axis.security.WSDoAllSender" >
	     <parameter name="action" value="Signature"/>
	     <parameter name="user" value="aplicacion_profile"/>
	     <parameter name="passwordCallbackClass" value="es.gva.pki.sleipnir2.accvumapugateway.services.serviciosmap.PasswordHandler"/>
	     <parameter name="signaturePropFile" value="ServiciosMap_outsecurity_sign.properties"/>
	     <parameter name="signatureKeyIdentifier" value="DirectReference" />
	    </handler>
	   </requestFlow >
	  </service >
	   -->
	</deployment>

* Copies the certificate file into the same package path into project resorces folder.
	
* Adds ``GvNIXWebServiceSecurity`` anntation to target class


The metadata provider performs this actions:

* Generates ``.aj`` file that adds to class the ``javax.security.auth.callback.CallbackHandler`` implementation (like this code)::

	public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
	    WSPasswordCallback pc = (WSPasswordCallback) callbacks[0];
	    pc.setPassword(${Password});
	}
  
* Generates ``${target_class_name}-security.properties`` in the same target class package inside project resources folder::

	org.apache.ws.security.crypto.provider=org.apache.ws.security.components.crypto.Merlin
	org.apache.ws.security.crypto.merlin.keystore.type=pkcs12
	org.apache.ws.security.crypto.merlin.keystore.password=${Password}
	org.apache.ws.security.crypto.merlin.alias.password=${Password}
	org.apache.ws.security.crypto.merlin.keystore.alias=${Alias}
	org.apache.ws.security.crypto.merlin.file=${Certificate}

* Adds an entry in ``src/main/resources/client-config.wsdd``::

	<service name="${Servicio}">
	   <requestFlow >
	    <handler type="java:org.apache.ws.axis.security.WSDoAllSender" >
	     <parameter name="action" value="Signature"/>
	     <parameter name="user" value="${Alias}"/>
	     <parameter name="passwordCallbackClass" value="${Proxy}"/>
	     <parameter name="signaturePropFile" value="${Propiedades}"/>
	     <parameter name="signatureKeyIdentifier" value="DirectReference" />
	    </handler>
	   </requestFlow >
	</service >
	
	
  * ``${Servicio}`` must be get from ``name`` atribute of wsdl ``port`` tag.
  * ``${Proxy}`` will be the same target class.

 

gvNIX
-----

Anotación de gvNIX
~~~~~~~~~~~~~~~~~~

Se ha creado una anotación @GvNIXWebServiceProxy para marcar una clase como cliente proxy que da acceso a las operaciones de un servicio web. Inicialmente, contiene un único atributo wsdlLocation que define de forma obligatoria la ruta en la que se encuentra emplazado el WSDL.

    @GvNIXWebServiceProxy(wsdlLocation = "...")

Integración con gvNIX
~~~~~~~~~~~~~~~~~~~~~

Se crea una clase vacía a la que se le añade la anotación @GvNIXWebServiceProxy con su atributo obligatorio wsdlLocation.

Además, a la misma clase se le añade la anotación @Service de Spring para seguir la misma estructura que hemos propuesto para todas las clases de servicio.

Ejemplo:

    @GvNIXWebServiceProxy(wsdlLocation = "...")
    @Service
    public class SomeService {

Asociado a la anotación @GvNIXWebServiceProxy existe un fichero AspectJ que contiene un método por cada operación del servicio web. Cada uno de estos métodos invoca a las clases Java del cliente del servicio web generadas con anterioridad mediante la aplicación WSDL2Java.

Axis
----

El aspecto creado tiene la siguiente estructura, originalmente:

    public WSBDC.IWs_bdc_xsd.WSBDC_Wrcterglobal BdcService.wcterglobal(
        String pUsuario, String pPasword, String pCif, String pNombre,
        String pBajas, String pSustitutos, String pDocumentales,
        String pOtros) throws RemoteException, ServiceException  {

    // TODO Deberíamos proporcionar en el constructor, al menos, la URL del WSDL

    WSBDCWebServiciosLocator locator = new WSBDCWebServiciosLocator();
    Ws_bdcPortType portType = locator.getWs_bdcPort();

    return portType.wcterglobal(pUsuario, pPasword, pCif, pNombre, pBajas,
        pSustitutos, pDocumentales, pOtros);
    }

Test
====

Execute next command on a empty folder to validate add-on:

  bash:~/project$ gvnix-dev script --file src/test/resources/gvnix-test.roo

Check roo exited with code 0, else error.

TODO
====

* Service Layer Import:

 * Deberíamos proporcionar al invocar al constructor de la clase de servicio del cliente generado en los métodos del AspectJ, al menos, la URL del WSDL.
 * ¿ hay más versiones de SOAP soportadas ?
 * Una posibilidad de futuro sería poder elegir que operaciones del WS se desean generar definiendo los métodos directamente en la clase Java (con una anotación) y en el AspectJ toda la infraestructura de acceso a las clases generadas del cliente.
 * Utilizar como wrapper OSGi las librerías de CXF y Axis que actualmente se invocan desde maven.
 * Analizar el modo en el que podríamos incluir una librería (JAR) en el proyecto ESB, de modo que sería viable JARear las clases del cliente generadas en target e incluirlas como librería tanto en las aplicaciones web (WEB-INF/lib) como en las aplicaciones del ESB.
 * De cara a futuro, es muy interesante la posibilidad de para una clase generada que representa a una entidad de datos que se transmite a través de un servicio pueda añadirse la anotación "RooEntity" (y posiblemente alguna más) para que puedan persistirse facilmente. Esto sería muy interesante también para la importación de servicios, sin embargo ahora mismo no es posible porque estas clases se regeneran en target y por lo tanto si se realizaran cambios estos desaparecerían al recompilar.
 * Ver que hacemos en el caso de que al generar clases (del cliente, servidor, etc.), coincidan varias clases con el mismo nombre en el mismo paquete.
 * Unportable types: You must use some common and interlingual class libraries format to communicate between muliple platforms. This problem is also present when working on the client side.
 * XSD extensions: In XSD, you can extend a data type by restricting it. The regular expression restriction is lost in the conversion process to Java, because Java does not allow for these sorts of extensions.
 * ¿ El cliente debe ser regenerado automáticamente cuando cambie el wsdl o debe permanecer invariable ?
 * Hacer esquema UML con la estructura de clases.
 * Investigar la posibilidad de que una clase no sea definida como un servicio-componente OSGi y sin embargo pueda utilizar otros servicios-componentes OSGi. Se quiere utilizar para la clase ServiceLayerWsConfigService.
 * Sería una buena idea generar una clase de Test para el cliente en el proyecto para así asegurarnos de que funciona siguiendo la metodología de ROO para generar clases de Test con respecto a la BBDD.

* Quizás sería interesante definir un nombre (o identificador o descripción) único para cada servicio, por lo menos para los importados. Esta información sería muy útil para conocer el servicio que tiene asociado dicha clase, ya que actualmente solo podemos conocer la URL del WSDL que a veces es muy poco representativa de lo que proporciona el servicio.

* Service ws security:
  
  * Add support to CXF services
  * Add support for others actions
  * Use converters for service class to allow autocomplete
  * Support more Certificate types.

* Replace "exported" with "exposed".

* Si se ejecuta en la clase Visi de petclinic un 'service export' de un método añadido se produce un problema parece que devido a que la clase Owner hereda de AbstractPerson y eso no se ha tendido en cuenta.
  Otra cosa importante es que se pierden algunos import (por ejemplo, java.util.List que utiliza el método creado) al regenerar el Visi.java para añadirle la anotación @GvNIXWebMethod dejando el fichero con errores de compilación.
  