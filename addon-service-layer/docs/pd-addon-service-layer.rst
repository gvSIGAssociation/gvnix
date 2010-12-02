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

  * ``--class`` (mandatory) Class in wich will be created the method.
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
  * ``--name`` Name to define the portType.
  * ``--serviceName`` Name to publish the Web Service.
  * ``--targetNamespace`` Namespace name for the service.

service operation list 
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

service entity
----------------

Entity Class to export as a Web Service. 

Parameters:

  * ``--class`` Entity to export.

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


