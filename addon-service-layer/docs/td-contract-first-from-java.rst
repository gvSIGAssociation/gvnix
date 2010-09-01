=========================================================================
 gvNIX Defining Contract first web services by generating wsdl from Java
=========================================================================

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

Requirements
============

Requirements are in priority order:

* Web service servers creation without write wsdl and xsd.
  Use contract first model, but generate contract (wsdl + xsd) from Java with a DSL language as Java Annotations and/or AspectJ.
  
  A change in the source code should not affect the generated service contract (wsdl + xsd). 
  If a code change makes inconsistent the relationship with the service annotations, would be required to generate a compilation or startup error.

* Using SOAP binding document / literal versus RPC / encoded servers generated, because RPC is obsolete by WS-I Basic Profile.

* Allow web service servers generation on the service layer o entity layer of Roo.

* Not use interfaces related to the implementation on service layer and entitity layer web service servers generation.

* Web service framework installation will be automatic when client or server generation is required. 

* Allow the generation of local services, withour web service support (Spring @Service).

* Generate web service clients and servers easily.

* Support web services clients generation compatible with JAX-RPC web service servers standar.

* Web services clients and servers generation would be executable on FUSE ESB / Servicemix environments.

Additionally, There are some limitations on wsdl generation from Java.
Another requirements are to solve or avoid this limitations too.

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
   
More information:

* http://static.springsource.org/spring-ws/sites/2.0/reference/html/why-contract-first.html

Analysis
========

Web Services Framework
----------------------

Spring Web Services and Apache CXF comparisson:

.. list-table:: 
   :widths: 50 50 50
   :header-rows: 1

   * -
     - Spring Web Services
     - Apache CXF
   * - Development styles
     - Contract first
     - Contract first and code first
   * - JAX-RPC support
     - Yes
     - No, because JAX-RPC is deprecated (http://stackoverflow.com/questions/412772/java-rpc-encoded-wsdls-are-not-supported-in-jaxws-2-0)
   * - JAX-WS support
     - No, use their own implementation
     - Yes
   * - JAX-B support
     - Yes
     - Yes, and more frameworks
   * - REST support
     - Yes, on lastest versions
     - Yes
   * - Marshall (serialize) / unmarshall (deserialize) utilities
     - Yes (http://static.springsource.org/spring-ws/sites/2.0/reference/html/oxm.html)
     - Yes
   * - WS-I basic profile compatibility (http://www.ws-i.org) 
     - Yes
     - Yes

Pros and cons:

* Apache CXF

 * Pros:
 
  * Is already integrated by us on Roo.
  * We have already some web service servers generated with CXF on the gvNIX sponsor organization.
  * FUSE ESB / Servicemix support.
  
 * Cons:
 
  * CXF has no support JAX-RPC client generation, Axis could be used instead on this clients.
  
* Spring Web Services

 * Pros
 
  * Roo is a Spring product as Spring Web Services: better integration expected.
  
 * Cons
 
  * No FUSE ESB / Servicemix support.
  * The Spring annotations provides less control of the contract wsdl generated, because its orientation appears first contract focused on prewriting of wsdl.
 
**CXF has been selected as web services framework because has ESB / Servicemix support and better contract generation annotations.**

More information:

* http://static.springsource.org/spring-ws/sites/2.0/reference/html/tutorial.html
* http://cxf.apache.org/docs/defining-contract-first-webservices-with-wsdl-generation-from-java.html   
* http://cxf.apache.org/
* http://static.springsource.org/spring/docs/2.5.x/reference/remoting.html
* http://www.theserverside.com/news/thread.tss?thread_id=46635

Limitations solution
--------------------

#. Fragility

   Use JAX-B and JAX-WS annotations to avoid source code modifications makes web service contract change.

   **Source code elements related to contract operations generation:**
   
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
   
   Use JAX-WS annotations.
    
   **Source code elements related to contract entities generation:**	
   
   * Class package
   * Class name
   * Properties quantity
   * Properties order
   * Each property name
   * Each property type
   * Each property type compatibility
   
   Use JAX-B annotations.
   
#. Unportable types
 	
   Allow only a list of specific types that has no conversion problems. For example, let String, but not allow TreeMap.
   
   We may also need to define the mapping of certain types of data that is not completely accurate, for example, the Date in Java provides the time and XML (https://jaxb.dev.java.net/guide/Using_different_datatypes.html).

#. Cyclic graphs

   Related entities shall not be processed on the conversion to XML with the @XmlTransient JAX-B annotation. 
   
   Another option in the newest versions of JAX-B is to implement an interface that forces us to define operations to be performed to avoid cycles.
   
   More information:
   
   * https://jaxb.dev.java.net/guide/Mapping_cyclic_references_to_XML.html

#. Performance

   As previous explanation, some related entities shall not be processed in the conversion to XML. 

#. Versioning

   Different operation versions could be defined as different operations or different endpoints.

#. XSD extensions

   We will not allow XSD extensions on the generated web service servers.

   To add a restriction on any of the input parameters of the web service server, validate the retricción in your method code and return a exception if not satisfied.
   This will generate a fault on the web service server when restriction is not respected. 

#. Reusability

   Generate the XML Schema (XSD) in a separate file from the WSDL file.
   The WSDL file will include (use) the XSD file, and other services could do the same. 

More information:

* http://www.liquid-reality.de:8080/display/liquid/2008/08/20/Defining+Contract+first+webservices+by+generating+wsdl+from+java

Design
======

Proof of concept repository location:

* https://svn.disid.com/svn/disid/proof/spring_roo/gvnix-cxf-web-service

Developing a service
--------------------

The service endpoint interface (SEI) is the piece of Java code that is shared between a service and the consumers that make requests on it. When starting from Java, it is the up to a developer to create the SEI. There are two basic patterns for creating an SEI:

#. Green field development: You are developing a new service from the ground up. When starting fresh, it is best to start by creating the SEI first. You can then distribute the SEI to any developers that are responsible for implementing the services and consumers that use the SEI.
#. Service enablement: In this pattern, you typically have an existing set of functionality that is implemented as a Java class and you want to service enable it.

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

    * name: Specifies the local name of the fault element.
    * targetNamespace: Specifies the namespace under which the fault element is defined. The default value is the target namespace of the SEI.
    * faultBean: Specifies the full name of the Java class that implements the exception.
    
  The name property is required.

* @WebService: Marca una clase como servicio

	* name: Specifies the name of the service interface. This property is mapped to the name attribute of the wsdl:portType element that defines the service's interface in a WSDL contract. The default is to append PortType to the name of the implementation class.
	* targetNamespace: Specifies the target namespace under which the service is defined. If this property is not specified, the target namespace is derived from the package name.
	* serviceName: Specifies the name of the published service. This property is mapped to the name attribute of the wsdl:service element that defines the published service. The default is to use the name of the service's implementation class. Note: Not allowed on the SEI
	* wsdlLocation: Specifies the URI at which the service's WSDL contract is stored. The default is the URI at which the service is deployed. The location of a predefined WSDL file describing the service.
	* endpointInterface: Specifies the full name of the SEI that the implementation class implements. This property is only used when the attribute is used on a service implementation class. Note: Not allowed on the SEI
	* portName: Specifies the name of the endpoint at which the service is published. This property is mapped to the name attribute of the wsdl:port element that specifies the endpoint details for a published service. The default is the append Port to the name of the service's implementation class. Note: Not allowed on the SEI

* @WebParam ( name="name" ): Necesario para que Java no pierda el nombre de un parámetro web y así evitar que en el wsdl contenda arg0 en lugar del nombre deseado.

    * name: Specifies the name of the parameter as it appears in the WSDL. For RPC bindings, this is name of the wsdl:part representing the parameter. For document bindings, this is the local name of the XML element representing the parameter. Per the JAX-WS specification, the default is argN, where N is replaced with the zero-based argument index (i.e., arg0, arg1, etc.)
    * targetNamespace: Specifies the namespace for the parameter. It is only used with document bindings where the parameter maps to an XML element. The defaults is to use the service's namespace.
    * mode: Mode.IN, Mode,OUT, Mode.INOUT
    
      Specifies the direction of the parameter.
    
    * header: false, true
    
      Specifies if the parameter is passed as part of the SOAP header.
    
    * partName: Specifies the value of the name attribute of the wsdl:part element for the parameter when the binding is document. Default parametes.

  Los primeros son los valores por defecto.
  
* @WebResult del paquete the javax.jws: Allows you to specify the properties of the generated wsdl:part that is generated for the method's return value.

    * name: Specifies the name of the return value as it appears in the WSDL. For RPC bindings, this is name of the wsdl:part representing the return value. For document bindings, this is the local name of the XML element representing the return value. The default value is return.
    * targetNamespace: Specifies the namespace for the return value. It is only used with document bindings where the return value maps to an XML element. The defaults is to use the service's namespace.
    * header: Specifies if the return value is passed as part of the SOAP header.
    * partName: Specifies the value of the name attribute of the wsdl:part element for the return value when the binding is document. Default parametes.

* @WebMethod del paquete javax.jws: Provides the information that is normally represented in the wsdl:operation element describing the operation to which the method is associated. Sus propiedades son:

    * operationName: Specifies the value of the associated wsdl:operation element's name. The default value is the name of the method.
    * action: Specifies the value of the soapAction attribute of the soap:operation element generated for the method. The default value is an empty string.
    * exclude: Specifies if the method should be excluded from the service interface. The default is false.

* @SOAPBinding del paquete javax.jws.soap: Provee información sobre como se relaciona el servicio con SOAP. Si no se especifica se toma document/literal. Pueden definirse las siguientes propiedades:

    * style: Style.DOCUMENT, Style.RPC
    
      Specifies the style of the SOAP message. If RPC style is specified, each message part within the SOAP body is a parameter or return value and will appear inside a wrapper element within the soap:body element. The message parts within the wrapper element correspond to operation parameters and must appear in the same order as the parameters in the operation. If DOCUMENT style is specified, the contents of the SOAP body must be a valid XML document, but its form is not as tightly constrained.
    
    * use: Use.LITERAL, Use.ENCODED
    
      Specifies how the data of the SOAP message is streamed.
    
    * parameterStyle: ParameterStyle.WRAPPED, ParameterStyle.BARE
    
      Specifies how the method parameters, which correspond to message parts in a WSDL contract, are placed into the SOAP message body. A parameter style of BARE means that each parameter is placed into the message body as a child element of the message root. A parameter style of WRAPPED means that all of the input parameters are wrapped into a single element on a request message and that all of the output parameters are wrapped into a single element in the response message. If you set the style to RPC you must use the WRAPPED parameter style.

  Los primeros son los valores por defecto.

*  @RequestWrapper y @ResponseWrapper del paquete javax.xml.ws: Java class that implements the wrapper bean for the method parameters that are included in the request or response message in a remote invocation. It is also used to specify the element names, and namespaces, used by the runtime when marshalling and unmarshalling the messages. Propiedades:

      o localName: Specifies the local name of the wrapper element in the XML representation of the message. The default value is the name of the method or the value of the @WebMethod annotation's operationName property.
      o targetNamespace: Specifies the namespace under which the XML wrapper element is defined. The default value is the target namespace of the SEI.
      o className: Specifies the full name of the Java class that implements the wrapper element.
      
   Tip: Only the className property is required.
   
   Se define en la variable className una clase a crear automáticamente que será el objeto contenedor donde se guardan los parámetros que se envían o se devuelven.

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
 
 The @XmlRootElement annotation notifies JAXB that the annotated class is the root element of the XML document. If this annotation is missing, JAXB will throw an exception.

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

* http://www.devx.com/Java/Article/34069/1954?pf=true

* http://download-llnw.oracle.com/javaee/5/api/index.html?javax/xml/bind/annotation/XmlType.html

Service annotation
------------------

* Definir en la interfaz los parámetros relativos a @WebService::

    package org.gvnix.test.project.web.services.impl;

	@WebService(name = "PersonServicePortType", 
	    targetNamespace = "http://impl.services.web.project.test.gvnix.org/")
	public interface PersonService

* Definir el la implementación del servicio los parámetros de @WebService::

    package org.gvnix.test.project.web.services.impl;

	@WebService(endpointInterface = "org.gvnix.test.project.web.services.impl.PersonService",
	    serviceName = "PersonService",
	    targetNamespace = "http://impl.services.web.project.test.gvnix.org/", 
	    portName = "PersonServiceImplPort")
	public class PersonServiceImpl implements PersonService
	
* Definido el servicio mediante la anotación @SOAPBinding con los valores de los parámetros asociados. No hay variación por Código Java::

	@SOAPBinding(style = Style.DOCUMENT, use = Use.LITERAL, parameterStyle = ParameterStyle.WRAPPED)
	
* Definida la anotación @WebMethod para la operación del servicio en la interfaz::

	@WebMethod(operationName = "getPersonName", action = "", exclude = false)
	
* Definición de la anotación en la interfaz del servicio en la operación::

	@RequestWrapper(localName = "getPersonName", targetNamespace = "http://services.web.project.test.gvnix.org/types", className = "java.lang.Long")
	abstract Person getPersonName(@WebParam(name = "id") Long id);
	
  Si cambiamos el parámetro de entrada al método por List<Integer> id en la intefaz y la implementación: El wsdl generado sigue siendo el mismo.
  
  Envía dentro de RequestWrapper el parámetro que no está está anotado como @WebParam. No se puede controlar que no varíe el contrato del servicio si se altera la signatura del método.

* Definición de la anotación en la interfaz del servicio en la operación::

	@ResponseWrapper(localName = "getPersonNameResponse", targetNamespace = "http://services.web.project.test.gvnix.org/types", className = "org.gvnix.test.project.web.services.domain.Person")
	abstract Person getPersonName(@WebParam(name = "id") Long id);

  Crea un objeto Person en el wsdl que le envía como respuesta de la operación del servicio.
  
  Si cambiamos el parámetro de salida al método por Long en la intefaz y la implementación: El wsdl generado sigue siendo el mismo que devuelve un objeto Person como resultado, pero como ahora devuelve un objeto distinto es como si devolviera un null.

* Definición de la anotación en la cabecera de la excepción que va a utilizar la operación del servicio web::

	@WebFault(name = "FaultException", targetNamespace = "http://services.web.project.test.gvnix.org/types", faultBean = "org.gvnix.test.project.web.services.exceptions.FaultException")
	public class FaultException extends Exception

  Se añade al método del servicio definido en la interfaz y en su implementación::

	abstract Person getPersonName(@WebParam(name = "id") Long id) throws FaultException;
	public Person getPersonName(Long id) throws FaultException {...}

  Crear una exception nueva que tenga el mismo name, namespace y faultBean: Falla al compilar ya que el faultBean debe ser la clase de la excepción que se está definiendo.
  
  Si hay un cambio de excepción en el wsdl se ha de cambiar el contrato del servicio, no se puede cambiar la excepción en java para que el servicio publique otra definida por el parámetro faultBean ya que aparecería un warning al generar el contrato del servicio.
  
  Si se define una segunda excepción y se mantienen los mismos parámetros en la anotación, no cambia el contrato de servicio. La definición en la anotación de la excepción creada tiene preferencia sobre los atributos definidos en su clase.
  
* @WebParam: Si se cambia el Tipo de parámetro de entrada (en la interfaz y la implementación) cambia el contrato de servicio pero no cambia el nombre del parámetro que se ha definido en la variable name.
  No controla el tipo del parámetro que utiliza la operación del servicio (método de la clase) con anotaciones.
  Si se añade un atributo nuevo al objeto de entrada en la operación se genera un nuevo contrato para el servicio. Esto se debería evitar creando los XSD por separado e importándolos como esquemas ya que el wsdl generado incluye la definición del Objeto en XML.
  
  TODO Probar si incluir un parámetro que no está anotado con @WebParam.
  
* @WebResult: Si se cambia el Tipo de parámetro de que devuelve (en la interfaz y la implementación) cambia el contrato de servicio pero no cambia el nombre del parámetro que se ha definido en la variable name.
  No controla el tipo del parámetro que devuelve como resultado la operación del servicio (método de la clase) con anotaciones.
  Si se añade un atributo nuevo al objeto que devuelve la operación se genera un nuevo contrato para el servicio. Esto se debería evitar creando los XSD por separado e importándolos como esquemas ya que el wsdl generado incluye la definición del Objeto en XML.
  
* Si se añade la etiqueta @OneWay en la interfaz (SEI) de un método de la clase del servicio, la operación del servicio no devolverá nada, ejemplo::

	@WebMethod(operationName = "returnString", action = "", exclude = false)
	@Oneway
	abstract String returnString();

  El resultado al consultar el servicio está vacío, no devuelve nada aunque en la implementación del método devuelva el string. Cualquier tipo de resultado definido en el método no hará que se regenere el contrato y no devolverá ningún objeto (XML).

* CXF: http://cxf.apache.org/docs/configuration.html
* Jaxb2: http://java.sun.com/developer/technicalArticles/J2SE/jax_ws_2/
* https://svn.disid.com/svn/gvcit/JavaESB/docs/soa-analisis-contrato-servicios.rst
* https://svn.disid.com/svn/gvcit/JavaESB/docs/soa-analisis-guia-XSD.rst

Entities annotation
-------------------

* Cabera de la clase::

	@XmlRootElement(name = "horse", namespace = "http://services.web.project.test.gvnix.org/horse")
	@XmlType(propOrder = { "name", "person" }, name = "horse", namespace = "http://services.web.project.test.gvnix.org/horse")
	@XmlAccessorType(XmlAccessType.FIELD)

  Para controlar que los cambios en los atributos de la entidad no afecten al contrato de servicio se han de definir los atributos en la anotación @XmlType con el parámetro 'propOrder = { "name", "person" }' para que así si se añade un atributo nuevo a la entidad de un warning al intentar publicar el servicio.
  Si se utiliza propOrder se han de ordenar/definir todas las propiedades del objeto que no estén anotadas con @XmlTransient, da igual que no estén anotadas con @XmlElement (Esta anotación sirve para convertir la propiedad a una etiqueta xml con un nombre específico) falla.

* En cada campo que se quiere crear como elemento se ha definir la anotación con el nombre que se quiere mostrar en xml para no alterar el contrato del servicio::
  
	@XmlElement(name = "persona")
	
* Si no se quiere convertir una propiedad de la clase se ha de añadir la anotación @XmlTransient en la declaración de la propiedad.
  Se utilizará para evitar Grafos cíclicos.

  Después de unas pruebas con entidades relacionadas ('1 a n' y 'n a 1') la configuración correcta es asignar @XmlTransient a la entidad que contiene la lista de entidades (1-n) que no serán mostradas en una consulta por entidades ya que son gestionadas en la otra parte de la relación.
  Podríamos rumiar un poco más la idea ya que en algunos casos puede ser información muy interesante. Por ejemplo, en los terceros ver la lista de sus domicilios o cuentas puede ser interesante, pero ¿ hasta donde puede llegar el grafo de objetos a transformar ?. Esto colisiona con el requerimiento de rendimiento.

Anotar todas las entidades de la aplicación al "instalar" el Add-on de servicios, es decir al publicar un servicio como servicio web.

* Crear el fichero aj para que anote cada uno de los campos de la entidad con @XmlElement y las relaciones, definidas por @OneToMany, @ManyToOne, etc como transient.

Addon commands
---------------

* service class --class:

  - ``class``: Nombre de la nueva clase servicio.

    Crear una clase para gestionar servicios. Añadiría las anotaciones de Spring que necesitase ``@Service``.
    
    Hay que pensar si alguna más (puede que del propio add-on). Ninguna ya que es una clase de Servicio no Servicio Web.

* service operation --class clase --name nombreOperacion --return clase:

  - ``class``: Clase @Service a la que se va a añadir el método nuevo.
  - ``name``: nombre del método.
  - ``return``: Objeto que devuelve el método.

    Sólo está activo para las clases anotadas con ``@Service`` (Autocompletado).
    
    Si la clase viene de una entidad se mostrarán los nombres de los métodos que se pueden publicar. La clase estará anotada con @GvNixEntityService y no hará falta definir los parámetros de entrada ni los de salida, toma como plantilla el método de la clase definido en el fichero aj de la entidad.
    
    Un método, que devolverá (o no) un tipo en concreto. Habría que ver como concretar la especificación del tipo devuelto cuando es Map, Collection, Set, etc...).

* service parameter --class clase --method nombreOperacion --name nombreParametro --type clase:

  - ``class``: Clase ``@Service`` que no sea ``@GvNixEntityService`` que se va a añadir el parámetro al método.
  - ``method``: nombre del método que se va a editar.
  - ``name``: parámetro que se va a añadir al método.
  - ``type``: Tipo del parámetro.

    Añade un parámetro de entrada a un método de una clase servicio (o de entidad). 
    
    Habría que ver como concretar la especificación del parámetro cuando es Map, Collection, Set, etc...).

* service export ws --class clase --name nombreServicio:

  - ``class``: clase que se va a publicar como servicio web. Anotar la clase con ``@GvNixWebService``.
  - ``name``: nombre que se le va dar al servicio a definir.

    Generará lo necesario para que esta clase (dependiendo si --name se define) sea accesible externamente. 
    
    Definirá en el pom.xml la generación en la fase de compilación del fichero wsdl para comprobar el correcto funcionamiento antes de ser publicado. Ver último punto del apartado `TODO`_.
    
    La clase debería poder ser una clase de servicio o una entidad (habría que ver opciones u otro comando para publicar CRUD). Publicar el CRUD como servicio ?

    **Duda:** 
      Se deberían añadir las anotaciones JAXB al crear cualquier servicio web a todas las clases que se encuentren en el paquete ...domain al crear el primer servicio.

    **TODO:**
      Tendríamos que ver como implementar esto para que permitiese exportar de distintas formas (por ejemplo si es un proyecto ESB o no, etc). 
      Este comando requerirá mucho más análisis.

* service export operation ws --class clase --method nombreMetodoEntidad --name nombreAPublicar: 

  - ``clase``: Clase anotada con ``@GvNixEntityService``.
  - ``method``: nombre del método de la clase que se quiere publicar.
  - ``name``: nombre con el que se quiere publicar el método de la clase.

    Publicar como operación de un servicio web un método definido en la clase de servicio concreta.
    
    Anota un método definido en la clase del Servicio como operación del servicio web.
    
    Sólo está activo para clases que se han publicado como servicios ``@GvNixWebService`` en el paquete service (Autocompletado).
    
    Asigna la anotación @GvNixWebMethod al método que se va a publicar.
    
    **Parámetros:**
      Los parámetros del método si los tiene se anotan con ``@WebParam`` y los valores por defecto, es decir los que se han declarado en el método.
    
    **Importante:** 
      Si no se define ni method ni name se aplica a todos los métodos con los valores por defecto.

* service export ws --wsdl url2wsdl:

  - ``wsdl``: localización del archivo wsdl.

    Generará generará una clase de servicio a partir de su definición en wsdl. 
    
    Los métodos serán generados en blanco para que el desarrollador pueda realizar su implementación. 
    
    Este comando es el mismo que el anterior pero con sólo el parámetro de la descripción del contrato. 
    
    Como paquete y clase se usará el namespace que haya definido en el contrato. Este comando requerirá mucho más análisis.

* service entity --class nombreClase:

  - ``class``: Entidad que a partir de la que se va a crear el servicio.

    Crear una clase a partir de una entidad para gestionar servicios.
    
    Añadiría las anotaciones de Spring que necesitase ``@Service`` y ``@GvNixEntityService``.

* service import ws --endPoint urlOPropiedad --wsdl url2wsdl.xml:

  - ``endPoint`` urlOPropiedad
  - ``wsdl`` url

    Creará a una clase de servicio que hará de proxy de las operaciones que publica un Web Service remoto. 
    
    El parámetro endPoint sería opcional y debería poder ser una propiedad configurable desde los profiles (esto será útil para configura accesos a los servicios de desarrollo/pre-producción/producción). 
    
    La clase y el paquete a generar se usará el namespace del contrato del servicio.

Posibles mejoras el add-on cd CXF:

  Creación de una operación en un servicio.

      * Siempre está disponible el comando ``service operation`` si existe alguna clase anotada con ``@Service``.
      * Los parámetros que pide el add-on para la creación de la operación en el servicio no son obligatorios, pero cuando creas una operación de servicio (método) sin parámetros hace la comprobación de que no tienen que ser nulos.
            * Si es sin parámetros, ¿ que va a comprobar ?
      * Mejorar la forma de Buscar la implementación del servicio para añadirle la operación, ya que se podría añadir una operación a cualquier servicio existente.
            * Comandos ``service operation`` y ``service parameter``.

Addon Annotations
-------------------

Anotaciones definidas por el Addon para la gestión de servicios web:

* @GvNIXWebFault: Anotación para definir las anotaciones utilizadas como excepciones en las operaciones de un servicio web.
* @GvNIXWebMethod: Anotación para identificar el método a publicar como operación de un servicio web.
* @GvNIXWebService: Anotación para identificar la clase publicada como servicio web.
* @GvNIXWebParam: Anotación asociada la los parámetros del método que se publica como operación de un servicio.

TODO
====

* Publish an operation as web service with AJs or with Annotations
    * Publish with AJs and Annotations.
* Validate the generated contract with the WS-I Basic Profile standar (http://www.ws-i.org). Parece que, en general, se sigue la versión 1.1 de este estándar.
    * No usar interfaces ya que se crea el servicio como tal y la clase AspectJ se encarga de publicarlo como servicio web.
* WSDL and XSD documentation generation on the contract.
    * No genera documentación a partir de javadoc automáticamente.
* Define the list compatible types list allowed on web service server generation on the properties objects: https://jaxb.dev.java.net/guide/Using_different_datatypes.html
    * Tipos compatibles.
* Can be XML schemas generated in a separate file.
    * Por lo que he visto no hay manera, genera dentro del contrato y no nos debe afectar al desarrollo.
* Can be the contract generated with versioning structure ?
* To use annotations as bind validation (jsr303) to simulate XSD extensions.
* Web services unit testing.
* Para el tema del namespace es posible que sea necesario añadir monitorizaciones adicionales al NotifiableFileMonitorService, ya que seguramente las clases de los servicios no estén dentro de directorio del paquete base de la aplicación.
    * Como que no estén dentro del paquete base? es para crear la clase, se puede crear en cualquier paquete, puede que no haya entendido este punto.
* Una opción muy interesante sería poder hacer una prueba de generación del servicio utilizando el plugin para maven **java2ws** ya que por defecto se ejecuta en el arranque o primera petición del servicio.
    * He estado haciendo las pruebas arrancando la aplicación, debido a que de esta manera también podía probar el servicio mediante web-services-explorer de eclipse.
    * Se ha añadido el plugin para generar el wsdl en la fase de compilación.

    * Configuración en el pom.xml del proyecto::
    
        <!-- CXF java2Ws plugin to generate wsdl  -->
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
