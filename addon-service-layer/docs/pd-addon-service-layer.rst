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

Introducción
=============

**DOCUMENTO EN DESARROLLO**

TBC: Objetivo del addon

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

Limitations
-----------

TBC: 

These limitations would be resolved. There are sorted by relevance:

#. Fragility

   Each time you change your Java contract and redeploy it, there might be subsequent changes to the web service contract. 

#. Unportable types

   You must use some common and interlingual class libraries format to communicate between muliple platforms.
   This problem is also present when working on the client side.

#. Cyclic graphs

   Cyclic graphs, like Flight refers to the Passengers which refers to the Flight again, are quite common in Java.
   One way to solve this problem is to use references to objects, but the standard way to use these references in SOAP (RPC/encoded) has been deprecated in favor of document/literal (see WS-I Basic Profile). 

#. Performance

   When Java is automatically transformed into XML, there is no way to be sure as to what is sent across the wire.
   An object might reference another object, etc. which will result in slow response times. 

#. Versioning

   Even though a contract must remain constant for as long as possible, they do need to be changed sometimes.

#. XSD extensions

   In XSD, you can extend a data type by restricting it.
   The regular expression restriction is lost in the conversion process to Java, because Java does not allow for these sorts of extensions.

#. Reusability

   Defining your schema in a separate file allows you to reuse that file in different scenarios.
   
More information:

* http://static.springsource.org/spring-ws/sites/2.0/reference/html/why-contract-first.html

Use Case
=========

TODO:

TRANSLATE:

+Publicar clase como servicio web.+
----------------------------------------

Obligatorios todos los atributos de la anotación de gvNIX.

* Cambiar el paquete o el nombre de la clase. Si se hace un refactor en Eclipse actualiza automáticamente todo lo referenciado con el nombre de la clase **menos** el pom.xml. Hay que tener en cuenta que cambia en la anotación **@GvNIXWebService** el serviceName y Address por el nuevo si coinciden los nombres igual que el archivo de configuración.

  * Controlado por el targetNamespace definido en la anotación **@GvNIXWebSErvice** para que no cambie el contrato.
  * Controlado por portType. No cambia el contrato.
* Actualizar si ha variado el nombre de la clase o del paquete. Cambiar para que no cambie el contrato y no existan errores de compilación:

  * Archivo de configuración xml de cxf. Atributo class donde está instanciado el servicio.

    * Control de la publicación de un servicio en el archivo xml.

      * Buscar el bean que contenga la clase. No cambia el contrato.
      * Buscar el bean que contenga el id de publicación con el nombre definido por el serviceName. No cambia el contrato.
  * pom.xml referencia a la clase. Dirección donde se encuentra la clase.

    * Cambiar la ejecución del plugin de java2ws por el nuevo paquete/nombre de la clase.
  * Namespace **NO** se cambia para no cambiar el contrato de servicio. Si se cambia es conscientemente en la anotación **@GvNIXWebService**
* En la publicación controlar en el comando si el archivo de configuración existe, para así cuando se genera el metadato definir/comprobar el bean.

+Publicar operación.+
-----------------------

Obligatorios todos los atributos de la anotación de gvNIX si se añade manualmente la anotación a la clase.

* Cambiar nombre del método

  * Controlado con el atributo operationName. No cambia el contrato.
* Cambiar parámetro/s de entrada.

  * Cambiar tipo: Debe controlar el cambio la anotación con el tipo de parametro para no regenerar el Aj con las anotaciones asociadas a los parámetros de entrada **@GvNIXWebParam**. No cambia el contrato.
  * Cambiar nombre: Controlado por el atributo name de la anotación **@WebParam**, no varía el contrato de servicio si se cambia el nombre en java.
* Cambiar parámetros de salida.

  * Debe controlar el cambio la anotación con el tipo de returnType para **no regenerar** el Aj si no coincide. No cambia el contrato.

Estas comprobaciones se han de hacer al generar el metadato. El metadato relacionado con la publicación del servicio y de las operaciones de éste.

Nota:
  Si a alguna operación se actualiza o se publica manualmente usando la anotación **@GvNIXWebMethod** y no cumple algún requisito, se muestra un mensaje donde apunta que no va a ser publicada debido a que no cumple el estándar y no se genera le AspectJ asociado, por lo tanto no existe el servicio web hasta que no esté correctamente formado.

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

Create a web service defining annotations in clases.
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

Also you have to define *@GvNIXWebParam* and *@WebParam* annotations for each input parameter:

@GvNIXWebParam mandatory ``attributes``:

  * ``name``: The name of attribute in WSDL.
  * ``type``: Parameter's Java type. i.e.: type String: ``type = String.class``.

@WebParam ``attributes``:

  * ``name``: The same name of attribute name for *@GvNIXWebParam*. The name of attribute in WSDL.
  * ``partName``: Allways set ``partName = "parameters".
  * ``mode``: Allways set ``mode = Mode.IN``.
  * ``header``: Allways set ``header = false``.

Mandatory ``attributes`` for a method with return type different than void:

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

Monitorizaciones de archivos y procesos internos

TBC: Indicar qué se monitoriza, por ejemplo, crear una clase anotada con *tal* anotación y el proceso asociado, por ejemplo, crea un .aj con *tal cosa*. Este punto será muy útil para la integración con MOSKitt

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

Command to publish a service class as a web service.

service export ws:

* Add *@GvNixWebService* annotation with the command attributes (name, targetNamespace, etc) or if they hadn't been defined set default values.
* Add CXF dependecies into pom.xml.
* Add jax-ws build into the pom.xml to check the correct service contract generated before it will be published in compilation goal.
* Add web service definition to CXF config file. Create the file if doesn't exists.
* Create AspectJ file. Associated metadata to service class within CXF annotations: 

    * Define @WebService and @SOAPBinding to the published class setting the *@GvNIXWebService* annotation attributes into corresponding CXF annotation properties or default ones.
    * Annotate with *@WebMethod(exclude = true)* all class methods that aren't defined with *@GvNixWebMethod*.

Export a method as web service operation
---------------------------------------------------------

Command to publish a method as web service operation.

service operation:

* Modify method in Class where is defined with **@GvNIXWebMethod** annotation with its mandatory parameters or defined in inter-operability web service standards by default. 

  * Create or Modify associated AspectJ file with the new published method. Rebuild with defined values in gVNIX annotations. Associated Metadata generates AspectJ file.
  * Checks if GvNIX annotation is well formed before generate Metadata, if is incorrect shows a message and deletes metadata.
* Add @GvNIXWebFault annotation to method _Exceptions_ if are defined in the project. If Exception are imported create a declaration in an AspectJ file. Associated Metadata generates AspectJ file. 

  * Checks if GvNIX annotation is well formed before generate Metadata, if is incorrect shows a message and deletes metadata.
* Checks **Allowed Parameters** involved in operation.

  * If there is a not allowed parameters (input/output) doesn't publish the operation. See: supported data types.

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

  * ``--endPoint`` Class to act as a proxy.
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

TBC: The location of the project will be updated when the shell is built

Notes
=======

TBC


