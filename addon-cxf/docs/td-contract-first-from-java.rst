=========================================================================
 gvNIX Defining Contract first web services by generating wsdl from Java
=========================================================================

:Project:   gvNIX. Spring Roo based RAD tool
:Copyright: Conselleria d'Infraestructures i Transport - Generalitat Valenciana
:Author:    DiSiD Technologies, S.L.
:Revision:  $Rev$
:Date:      $Date$

This work is licensed under the Creative Commons Attribution-Share Alike 3.0
Unported License. To view a copy of this license, visit 
http://creativecommons.org/licenses/by-sa/3.0/ or send a letter to 
Creative Commons, 171 Second Street, Suite 300, San Francisco, California, 
94105, USA.

Requirements
============

* Generate local services, withour web service support (Spring @Service).

* Generate web service clients and servers easily.

* Web service servers creation without write wsdl and xsd.
  Contract first model, but contract (wsdl+xsd) generated from Java with a DSL language (Java Annotations).
  
  A change in the source code should not affect the generated service contract (wsdl+xsd). 
  If a code change makes their relationship inconsistent with the information defined in the annotations of the service, would be important to generate a compilation error.
  
* Support web services clients generation compatible with JAX-RPC web service servers.

* Web services clients and servers generation compatible with FUSE ESB / Servicemix.

* Allow web service servers generation from service layer o entity layer.

* Web service framework installation will be automatic when client or server generation is required. 

Additionally, There are some limitations on wsdl generation from Java.
The addon requirements are solve or avoid this limitations too.

Limitations
-----------

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

http://static.springsource.org/spring-ws/sites/2.0/reference/html/why-contract-first.html

Analysis
========

Web Services Framework
----------------------

Spring Web Services and Apache CXF comparisson.

.. list-table:: 
   :widths: 50 50 50
   :header-rows: 1

   * -
     - Spring Web Services
     - Apache CXF
   * - Development styles
     - Contract first
     - Contract first and code first
   * - JAX-RPC and JAX-WS APIs support
     - Yes
     - Only JAX-WS, because JAX-RPC is deprecated (http://stackoverflow.com/questions/412772/java-rpc-encoded-wsdls-are-not-supported-in-jaxws-2-0)
   * - JAX-WS support
     - No, they use their own implementation
     - Yes
   * - JAX-B support
     - Yes
     - Yes, and more framewworks
   * - REST support
     - Yes, on lastest versions
     - Yes
   * - Marshall (serialize) / unmarshall (deserialize) utilities
     - Yes (http://static.springsource.org/spring-ws/sites/2.0/reference/html/oxm.html)
     - Yes
   * - WS-I basic profile compatibility (http://www.ws-i.org) 
     - Yes
     - Yes

CXF has no support JAX-RPC client generation, Axis will be used instead on this clients.
CXF is already integrated with Roo.
We have already some web service servers generated with CXF on the gvNIX sponsor organization.

FUSE ESB / Servicemix has no support Spring WS.
The Spring annotations provides less control of the contract wsdl generated, because its orientation appears first contract focused on prewriting of wsdl.
 
**CXF has been selected as web services framework.**

* http://static.springsource.org/spring-ws/sites/2.0/reference/html/tutorial.html
* http://cxf.apache.org/docs/defining-contract-first-webservices-with-wsdl-generation-from-java.html   
* http://cxf.apache.org/
* http://static.springsource.org/spring/docs/2.5.x/reference/remoting.html
* http://www.theserverside.com/news/thread.tss?thread_id=46635

Limitations solution
--------------------

#. Fragility

   Use JAX-B and JAX-WS annotations to avoid source code modifications makes web service contract change.

   **Source code elements related on operations contract generation with JAX-WS:**
   
   * Class package
   * Class name
   * Method name
   * Input method parameters:
   
    * Quantity
    * Parameters order
    * Each parameter name
    * Each parameter type
    
   * Output method parameter:
   
    * Parameter type
    
   * Throwed exceptions:
   
    * Exception type
    
   **Source code elements related on entities contract generation with JAX-B:**	
   
   * Class package
   * Class name
   * Properties quantity
   * Properties order
   * Each property name
   * Each property type
   * Each property type compatibility
   
#. Unportable types
 	
   Only allow a set of specific types that have no conversion problems. For example, let String, but not allow TreeMap.

   TODO We may also need to define the mapping of certain types of data that is not completely accurate, for example, the Date in Java provides the time and XML:
   
    https://jaxb.dev.java.net/guide/Using_different_datatypes.html.

#. Cyclic graphs

   Related entities shall not be processed in the conversion to XML with the @XmlTransient JAX-B annotation. 
   
   Otra opción en las versiones más modernas de JAX-B es implementar una interfaz que nos obliga a definir las operaciones a realizar para evitar los ciclos.
   
   https://jaxb.dev.java.net/guide/Mapping_cyclic_references_to_XML.html 

#. Performance

   As previous explanation, related entities shall not be processed in the conversion to XML with the @XmlTransient JAX-B annotation. 

#. Versioning

   En un principio, podrían definirse como distintas operaciones o distintos endpoints.
   Aún no está claro como lo haríamos porque aún no hemos analizado la documentación sobre verdionado de servicios web.

#. XSD extensions

   Not be allowed XSD extensions on web service servers generated.

   To add a restriction on any of the input parameters of the web service server, validate the retricción in your method code and return a exception if not satisfied.
   This will generate a fault on the web service server when restriction is not respected. 

#. Reusability

   Generate the XML Schema (XSD) in a separate file from the WSDL file.
   The WSDL file will include (use) the XSD file, and other services could do the same. 

http://www.liquid-reality.de:8080/display/liquid/2008/08/20/Defining+Contract+first+webservices+by+generating+wsdl+from+java

Design
======

Proof of concept repository location:

 http://webdav.cop.gva.es/svn/gvnix/trunk/code/addon-cxf/docs/td-contract-first-from-java.rst

Developing a service
--------------------

The service endpoint interface (SEI) is the piece of Java code that is shared between a service and the consumers that make requests on it. When starting from Java, it is the up to a developer to create the SEI. There are two basic patterns for creating an SEI:

   1. Green field development: You are developing a new service from the ground up. When starting fresh, it is best to start by creating the SEI first. You can then distribute the SEI to any developers that are responsible for implementing the services and consumers that use the SEI.
   2. Service enablement: In this pattern, you typically have an existing set of functionality that is implemented as a Java class and you want to service enable it.

The SEI corresponds to a wsdl:portType element. The methods defined by the SEI correspond to wsdl:operation elements in the wsdl:portType element.

JAX-WS defines an annotation that allows you to specify methods that are not exposed as part of a service. However, the best practice is to leave such methods out of the SEI.

JAX-WS relies on the annotation feature of Java 5. The JAX-WS annotations are used to specify the metadata used to map the SEI to a fully specified service definition. Among the information provided in the annotations are the following:

    * The target namespace for the service.
    * The name of the class used to hold the request message.
    * The name of the class used to hold the response message.
    * If an operation is a one way operation.
    * The binding style the service uses.
    * The name of the class used for any custom exceptions.
    * The namespaces under which the types used by the service are defined.
   
* http://cxf.apache.org/docs/developing-a-service.html
* http://www.devx.com/Java/Article/34069/1954?pf=true  

JAX-WS annotations
------------------

Paquetes base javax.xml.ws, javax.jws.

* @WebFault ( name="NoSuchCustomer" ): Nos permite independizar el nombre de la clase de excepción del nombre del dato a transmitir.

    * name
    * targetNamespace
    * faultName

* @WebService: Marca una clase como servicio

    * name
    * targetNamespace
    * serviceName
    * wsdlLocation
    * endPointInterface
    * portName

* @WebParam ( name="name" ): Necesario para que Java no pierda el nombre de un parámetro web y así evitar que en el wsdl contenda arg0 en lugar del nombre deseado.

    * name
    * targetNamespace
    * mode: Mode.IN, Mode,OUT, Mode.INOUT
    * header: false, true
    * partName

  Los primeros son los valores por defecto.
  
* @WebResult del paquete the javax.jws: Allows you to specify the properties of the generated wsdl:part that is generated for the method's return value.

    * name
    * targetNamespace
    * header
    * partName

* @WebMethod del paquete javax.jws: Provides the information that is normally represented in the wsdl:operation element describing the operation to which the method is associated. Sus propiedades son:

    * operationName
    * action
    * exclude

* @SOAPBinding del paquete javax.jws.soap: Provee información sobre como se relaciona el servicio con SOAP. Si no se especifica se toma document/literal. Pueden definirse las siguientes propiedades:

    * style: Style.DOCUMENT, Style.RPC
    * use: Use.LITERAL, Use.ENCODED
    * parameterStyle: ParameterStyle.WRAPPED, ParameterStyle.BARE

  Los primeros son los valores por defecto.

*  @RequestWrapper y @ResponseWrapper del paquete javax.xml.ws: Java class that implements the wrapper bean for the method parameters that are included in the request or response message in a remote invocation. It is also used to specify the element names, and namespaces, used by the runtime when marshalling and unmarshalling the messages. Propiedades:

      o localName
      o targetNamespace
      o className

   Ejemplo::

    @ResponseWrapper(targetNamespace="http://demo.iona.com/types",
                   className="org.eric.demo.Quote")
                   
* @Oneway del paquete javax.jws: Methods in the SEI that will not require a response from the service. It can optimize the execution of the method by not waiting for a response

* JAX-WS tools:

 * Utiliza el plugin de maven cxf-java2ws-plugin para generar el wsdl.

* JAX-WS specification: http://www.jcp.org/en/jsr/detail?id=224

* https://jax-ws.dev.java.net/jax-ws-ea3/docs/annotations.html

* https://jaxb.dev.java.net/guide/Evolving_annotated_classes.html
   
JAX-B annotations
-----------------

Paquete base javax.xml.bind.annotation.

* The @XmlRootElement annotation notifies JAXB that the annotated class is the root element of the XML document. If this annotation is missing, JAXB will throw an exception.

 * name

* @XmlTransient: You can use this annotation on a class or an attribute to exclude this element of the XML conversion.

* @XmlElement and @XmlAttribute tag allows a class property to appear in the XML as an attribute::

   <element attribute="value"/>
    
  or as an element::
  
   <element>value</element>
   
  * name
  * required=true: Evita la opcionalidad de los elementos que se aplica por defecto.

API de la anotaciones de JAXB: http://download.oracle.com/javaee/5/api/javax/xml/bind/annotation/package-summary.html

* It generates a wrapper element around the collections of delivery addresses. Without them you could see various <deliveryAddresses> elements.
  With the code above, you get one <delivery> element that wraps various <address> elements::

   @XmlElementWrapper(name = "delivery")
   @XmlElement(name = "address")
   protected List<Address> deliveryAddresses = new ArrayList<Address>();

* You want to get rid of the identifier and the tags from the XML document. For that, use the @XmlTransient annotation::

   @XmlTransient
   private Long id;
   
  TODO Where to use this annotation: on the source property, on the destination poperty or both ?

* To rename an element, just use the name property of the @XmlElement annotation::

   @XmlElement(name = "zip")
   private String zipcode;

* @XmlType annotation on the top of the class. It allows JAXB to map a class or an enum to a XML schema type.
  You can use it to specify a namespace or to order attributes using the propOrder property, which takes a list of names of attributes and generates the XML document following this order::
  
   @XmlType(propOrder = {"street", "zipcode", "city", "country"})
   
  * name
   
  TODO Is it required to define all properties on propOrder ? @XmlTransient properties are not required on propOrder.

* The Individual class uses a @XmlJavaTypeAdapter annotation. @XmlJavaTypeAdapter(DateAdapter.class) notifies JAXB to use the custom adapter called DateAdapter when marshalling/unmarshalling the dateOfBirth attribute.
  Adapters are used when Java types do not map naturally to a XML representation. You can then adapt a bound type to a value type or vice versa::

   @XmlJavaTypeAdapter(DateAdapter.class)
   private Date dateOfBirth;

* @XmlAccessorType(XmlAccessType.FIELD): De esta forma pueden crearse tipos de datos primarios, arrays de primitivas o clases.

* @XmlSchema

* JAXB tools:

 * schemaGen allows to generate an XML schema from Java classes.
 * xjc does the opposite: from an XML schema, it creates annotated Java files.

* JAXB Architecture: https://jaxb-architecture-document.dev.java.net/nonav/doc/?jaxb/package-summary.html
   
* JAXB user guide: https://jaxb.dev.java.net/guide/
   
* JAXB Tutorial: http://java.sun.com/webservices/docs/2.0/tutorial/doc/JAXBWorks.html#wp100322

Addon commands
--------------

* service class: Crear una clase para gestionar servicios. Añadiría las anotaciones de Spring que necesitase (@Service?). Hay que pensar si alguna más (puede que del própio add-on).

* service operation --class clase --name nombreOperacion --return clase: Añadiría a una clase de servicio (o a una entidad, una entidad también podrá tener servicios) un método de operación, que devolverá (o no) un tipo en concreto. Habría que ver como concretar la especificación del tipo devuelto cuando es Map, Collection, Set, etc...).

* service parameter --class clase --operation nombreOperacion --name nombreParametro --type clase: Añade un parámetro de entrada a una operación de una clase servicio (o de entidad). Habría que ver como concretar la especificación del parámetro cuando es Map, Collection, Set, etc...).

* service import ws --endPoint urlOPropiedad --wsdl url2wsdl.xml: Creará a una clase de servicio que hará de proxy de las operaciones que publica un Web Service remoto. El parámetro endPoint sería opcional y debería poder ser una propiedad configurable desde los profiles (esto será útil para configura accesos a los servicios de desarrollo/pre-producción/producción). La clase y el paquete a generar se usará el namespace del contrato del servicio.

* service export ws --class clase --name nombreServicio : Generará lo necesario para que este método o la clase (dependiendo si --name se define) sea accesible externamente. La clase debería poder ser una clase de servicio o una entidad (habría que ver opciones u otro comando para publicar CRUD). Tendríamos que ver como implementar esto para que permitiese exportar de distintas formas (por ejemplo si es un proyecto ESB o no, etc). Este comando requerirá mucho más análisis.

* service export ws --wsdl url2wsdl: Generará generará una clase de servicio a partir de su definición en wsdl. Los métodos serán generados en blanco para que el desarrollador pueda realizar su implementación. Este comando es el mismo que el anterior pero con sólo el parámetro de la descripción del contrato. Como paquete y clase se usará el namespace que haya definido en el contrato. Este comando requerirá mucho más análisis.

TODO
====

* Publish an operation as web service with AJs or with Annotations ? 
* Validate the generated contract with the WS-I Basic Profile standar (http://www.ws-i.org).
  Parece que, en general, se sigue la versión 1.1 de este estándar.
* Use interfaces or only implementations on web service servers generation ?
* WSDL and XSD documentation generation on the contract.
* Define the list compatible types list allowed on web service server generation on the properties objects: https://jaxb.dev.java.net/guide/Using_different_datatypes.html
* Can be XML schemas generated in a separate file ?
* Can be the contract generated with versioning structure ?
* To use annotations as bind validation (jsr303) to simulate XSD extensions.
* Web services unit testing.
* Para el tema del namespace es posible que sea necesario añadir monitorizaciones adicionales al NotifiableFileMonitorService, ya que seguramente las clases de los servicios no estén dentro de directorio del paquete base de la aplicación.
* Una opción muy interesante sería poder hacer una prueba de generación del servicio utilizando el plugin para maven wsdl2java ya que por defecto se ejecuta en el arranque o primera petición del servicio.
