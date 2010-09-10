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

Publish web service
-------------------------

Commmando to publish a service class as a web service.

service export ws:

* Add *@GvNixWebService* annotation with the command attributes (name, targetNamespace, etc) or if they hadn't been defined set default values.
* Add CXF dependecies into pom.xml.
* Add jax-ws build into the pom.xml to check the correct service contract generated before it will be published in compilation goal.
* Add web service definition to CXF config file. Create the file if doesn't exists.
* Crete AspectJ file. Associated metadata to service class within CXF annotations: 

    * Define @WebService and @SOAPBinding to the published class setting the *@GvNIXWebService* annotation attributes into corresponding CXF annotation properties or default ones.
    * Annotate with *@WebMethod(exclude = true)* all class methods that aren't defined with *@GvNixWebMethod*.

Publicar un método como una operación de un servicio web
---------------------------------------------------------

Comando para publicar un método como operación.

service operation:

* Generar o Regenerar el archivo AspectJ asociado a la clase en la que se encuentra el método que se ha de publicar con la anotación **@GvNixWebService** si contienen algún método anotado con **@GvNixWebMethod** para así generar un método en el archivo AspectJ con las anotaciones necesarias para pubilcarse como operación. Se añade la excepción _java.lang.Exception_ para controlar las excepciones en tiempo de ejecución si contiene ninguna definida.

    * Si se ha de regenerar el AspectJ se mantiene con los mismos datos obtenidos al publicar la clase como servicio web.
* Definición de los parámetros:

    * class: *obligatorio* clase de la que se han de publicar un método como operación de un servicio.
    * method: *obligatorio* nombre del método que se va a publicar como operación del servicio (autocompletado de los métodos publicados como servicio, o si se trata de una entidad los de ésta).
    * operationName: nombre con el que se va a definir la operación.
    * webResultType: tipo de clase que va a devolver el método, void por defecto para comprobar que no va a variar el contrato de servicio.
    * resutlName: nombre asignado a la propiedad _name_ de la anotación @WebResult.
    * resultNamespace: namespace utilizado para @WebResult.
    * responseWrapperName: name para @ResponseWrapper
    * responseWrapperNamespace: namespace utilizado para @ResponseWrapper.
    * requestWrapperName: name para @RequestWrapper
    * requestWrapperNamespace: namespace utilizado para @RequestWrapper.
* Los únicos parámetros obligatorios son method y class ya que a partir de los cuales se ha de seleccionar el método a publicar.
* Esta anotación se asigna al método de la clase del servicio con los parámetros utilizados y los definidos por defecto si no se introducen, siguiendo los estándares para los servicios web.
* Anotar la excepción _Exception_ mediente un fichero AspectJ para que pueda utilizarse en la operación. Si el método utiliza otras excepciones de aplicación, anotarlas para que el monitor del Addon capte los cambios y genere el fichero AspectJ correspondiente.

    * Si la excepción que utiliza el método no se encuentra dentro del proyecto se genera un fichero AspectJ para anotarla como **@WebFault** y no se añade ninguna anotación a la clase.
* Crea el método en la clase AspectJ correspondiente con los mismos parámetros de entrada y salida y la excepción correspondiente.

* Se definen en la anotaciones de GvNix (*@GvNixWebService* y *@GvNixWebMethod*) los parámetros necesarios para regenerar el archivo aspectJ cuando haya que actualizar debido que se publique o elimine algún método como operación.
* Se asigna la anotación *@GvNixXmlElement* a las entidades que se utilicen como parámetros de entrada o salida de la operación.

    * Las entidades anotadas con *@GvNixXmlElement* se les asocia un fichero aj para anotar mediante JAXB, los atributos de relaciones se anotan con *@XmlTransient* y los demás atributos con *@XmlElement*. Se comprueba que estén dentro de +los tipos conocidos de datos+. Una lista que contendrá el Addon para las entidades de la aplicación y los definidos por nosotros, si no se encuentran en ninguna de ambas listas se anotarán como *@XmlTransient*.

Supported data types
--------------------------

Data types: Basic Data
~~~~~~~~~~~~~~~~~~~~~~~~

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

Al añadir un Map o un Set a la entidad y anotarla para hacer la serialización a XML hay que declararlos de la siguiente manera inicializados::

    private Set<String> lista = new java.util.HashSet<String>();
    private Map<String, Integer> mapping = new java.util.HashMap<String, Integer>();

Las colecciones que son listas **Set** si que se pueden definir como @XmlElement.
Aunque se convierten en listas para el cliente en el orden que se han establecido en Set.

* java.util.List::

    <xs:element maxOccurs="unbounded" minOccurs="0" name="lista" nillable="true" type="xs:string"/>

* javautil.Map como lista de elementos compuestos, entonces en el cliente generaría una clase compuesta de dos atributos key y value::

    <xs:element name="mapping">
      <xs:complexType>
        <xs:sequence>
          <xs:element maxOccurs="unbounded" minOccurs="0" name="entry">
            <xs:complexType>
              <xs:sequence>
                <xs:element minOccurs="0" name="key" type="xs:string"/>
                <xs:element minOccurs="0" name="value" type="xs:int"/>
              </xs:sequence>
            </xs:complexType>
          </xs:element>
        </xs:sequence>
      </xs:complexType>
    </xs:element>

**Conclusión:**

**No** habrá que dejar que se utilice Map como colección (Map es la interfaz, es decir, cualquier colección que implemente Map) debido a que **NO** se puede asegurar la funcionalidad de un Map en los servicios web, por lo tanto no se va a permitir que tomen partido en las operaciones de un servicio.

Colecciones excludidas:

* Map<K, V>:  Ya que están ordenadas por un valor determinado.

Data types: Project entities
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

Maneja cualquier tipo de clase entidad que esté definida en nuestro proyecto.
Reestricción de monitorización de Roo del paquete principal del proyecto. 
Si se utilizan Clases con otro paquete que no pertence al principal del proyecto se ha de tener en cuenta que para instanciar las clases se ha de añadir una anotación para que *Spring 3.0.3* lo cargue automáticamente como el ejemplo en el fichero de configuración *webcmvc-config.xml*, pero se debería definir en el *applicationContext.xml* ya que el proyecto no hace falta que sea un proyecto web::

    <!-- The controllers are autodetected POJOs labeled with the @Controller annotation. -->
    <context:component-scan base-package="org.gvnix.test.project" use-default-filters="false">
      <context:include-filter expression="org.springframework.stereotype.Controller" type="annotation"/>
    </context:component-scan>

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

service export ws
------------------

Exports a Class to a Web Service.

Parameters:

  * ``--class`` (mandatory) Class to be exported as a Web Service.
  * ``--name`` Name to define the portType.
  * ``--serviceName`` Name to publish the Web Service.
  * ``--targetNamespace`` Namespace name for the service.

service export operation ws 
----------------------------

Publish a service method as a Web Service operation.

Parameters:

  * ``--class`` (mandatory) Class to export a method.
  * ``--method`` (mandatory) Method to export.
  * ``--operationName`` Name of the method to be showed as a Web Service operation.
  * ``--resutlName`` Method result name.
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


