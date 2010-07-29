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

* Generate web service clients and servers easily.
* Web service servers creation without write wsdl and xsd.
  Contract first model, but contract (wsdl+xsd) generated from Java with a DSL language (annotations).
* Support web services clients generation compatible with JAX-RPC web service servers.
* Web services clients and servers generation compatible with FUSE ESB / Servicemix.
* Allow web service servers generation from service layer o entity layer. 

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
   
   * http://cxf.apache.org/docs/developing-a-service.html
   * http://www.devx.com/Java/Article/34069/1954?pf=true  

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
   
   JAX-WS specification: http://www.jcp.org/en/jsr/detail?id=224
   
   JAXB Architecture: https://jaxb-architecture-document.dev.java.net/nonav/doc/?jaxb/package-summary.html
   
   JAXB user guide: https://jaxb.dev.java.net/guide/
   
   JAXB Tutorial: http://java.sun.com/webservices/docs/2.0/tutorial/doc/JAXBWorks.html#wp100322

#. Unportable types
 	
   Only allow a set of specific types that have no conversion problems. For example, let String, but not allow TreeMap.

   We may also need to define the mapping of certain types of data that is not completely accurate, for example, the Date in Java provides the time and XML.

#. Cyclic graphs

   Related entities shall not be processed in the conversion to XML with the @XmlTransient JAX-B annotation. 

#. Performance

   As previous explanation, related entities shall not be processed in the conversion to XML with the @XmlTransient JAX-B annotation. 

#. Versioning

   TODO

#. XSD extensions

   Not be allowed XSD extensions on web service servers generated.

   To add a restriction on any of the input parameters of the web service server, validate the retricción in your method code and return a exception if not satisfied.
   This will generate a fault on the web service server when restriction is not respected. 

#. Reusability

   TODO

http://www.liquid-reality.de:8080/display/liquid/2008/08/20/Defining+Contract+first+webservices+by+generating+wsdl+from+java

Design
======

TODO
====

* Publish an operation as web service with AJs or with Annotations ? 
* Validate the generated contract with the WS-I Basic Profile standar (http://www.ws-i.org).
* Use interfaces or only implementations on web service servers generation ?
* WSDL and XSD documentation generation on the contract.
* Define the list compatible types list allowed on web service server generation on the properties objects.
* Can be XML schemas generated in a separate file ?
* Can be the contract generated with versioning structure ?
* To use annotations as bind validation (jsr303) to simulate XSD extensions.
* Web services unit testing.
