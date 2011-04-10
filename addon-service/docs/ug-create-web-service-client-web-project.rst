===============================================
 gvNIX Service Layer Add-on - Import Use Case
===============================================

:Project:   gvNIX. Spring Roo based RAD tool
:Copyright: Conselleria d'Infraestructures i Transport - Generalitat Valenciana
:Author:    DiSiD Technologies, S.L.
:Revision:  $Rev: 433 $
:Date:      $Date: 2010-11-23 10:33:14 +0100 (mar, 23 nov 2010) $

.. contents::
   :depth: 2
   :backlinks: none

This work is licensed under the Creative Commons Attribution-Share Alike 3.0
Unported License. To view a copy of this license, visit 
http://creativecommons.org/licenses/by-sa/3.0/ or send a letter to 
Creative Commons, 171 Second Street, Suite 300, San Francisco, California, 
94105, USA.

Introducción
===============

Crear proyecto con GvNIX integrando un cliente de servicio web.

Prerequisitos
===============

Requerimientos
=============== 

Tener instalados los siguientes requerimientos:

* JDK-1.5.07
* Maven 2.0.9
* GvNIX 0.5.0
* Eclipse Galileo 3.5.1

Caso de uso
============

Datos del Servicio Web
------------------------

Dirección del Servicio Web que vamos a utilizar:

* http://api.bing.net/search.wsdl

Parámetros necesarios para ejecutar la operación de búsqueda del servicio web proporcionados por Bing:

* AppID = 8DC4F8381BF17A5FE9BF28F7B5AF12F5EA08462F
* Version = 2.2

Creación del proyecto
=======================

Vamos a crear una aplicación que consulte la operación de búsqueda del Servicio Web de Bing.com y muestre los resultados.

Primera parte
------------------

Crear una aplicación web y generar el cliente del servicio web.

Crear el directorio del proyecto::

    bash> mkdir bing-search-app
    bash> cd bing-search-app

Entrar en la consola de GvNIX::

    bash> gvnix

Crear un proyecto::

    gvnix> project --topLevelPackage org.gvnix.test.bing.search.app --java 5 --projectName bing-search-app

Añadir las dependencias para jdk1.5::

    gvnix> dependency add --groupId javax.xml.bind --artifactId jaxb-api --version 2.1
    gvnix> dependency add --groupId com.sun.xml.bind --artifactId jaxb-impl --version 2.1.3

Generamos el cliente del servicio web que vamos a utilizar::

    gvnix> service import ws --wsdl http://api.bing.net/search.wsdl --class ~.service.SearchService

Vemos que el resultado por pantalla es el siguiente::

    Created SRC_MAIN_JAVA/org/gvnix/test/bing/search/app/service
    Created SRC_MAIN_JAVA/org/gvnix/test/bing/search/app/service/SearchService.java
    Managed SRC_MAIN_JAVA/org/gvnix/test/bing/search/app/service/SearchService.java
    Managed ROOT/pom.xml [Added dependency org.gvnix:org.gvnix.annotations:${gvnix.version}]
    Managed ROOT/pom.xml
    Managed ROOT/pom.xml [Added dependency org.apache.cxf:cxf-rt-frontend-jaxws:${cxf.version}]
    Managed ROOT/pom.xml [Added dependency org.apache.cxf:cxf-rt-transports-http:${cxf.version}]
    Managed ROOT/pom.xml [Added dependency org.hibernate.javax.persistence:hibernate-jpa-2.0-api:1.0.0.Final]
    Managed ROOT/pom.xml

Y ejecuta el plugin de maven **cxf-codegen-plugin** para generar el cliente y añade las operaciones a la clase **SearchService** del proyecto::

    [Thread-4] Listening for transport dt_socket at address: 4001
    [Thread-4] [INFO] Scanning for projects...
    [Thread-4] [INFO] ------------------------------------------------------------------------
    [Thread-4] [INFO] Building bing-search-app
    [Thread-4] [INFO]    task-segment: [clean, generate-sources]
    [Thread-4] [INFO] ------------------------------------------------------------------------
    [Thread-4] [INFO] [clean:clean]
    [Thread-4] [INFO] [cxf-codegen:wsdl2java {execution: generate-sources}]
    [Thread-5] log4j:WARN No appenders could be found for logger (org.apache.cxf.bus.spring.BusApplicationContext).
    [Thread-5] log4j:WARN Please initialize the log4j system properly.
    [Thread-5] 24-nov-2010 13:25:45 org.apache.cxf.tools.wsdlto.frontend.jaxws.processor.internal.ParameterProcessor processInput
    [Thread-5] ADVERTENCIA: Can not find the soap binding in your classpath
    [Thread-5]   Will not generate the extra parameter.
    [Thread-5] 24-nov-2010 13:25:45 org.apache.cxf.tools.wsdlto.frontend.jaxws.processor.internal.ParameterProcessor processInput
    [Thread-5] ADVERTENCIA: Can not find the soap binding in your classpath
    [Thread-5]   Will not generate the extra parameter.
    [Thread-4] [INFO] ------------------------------------------------------------------------
    [Thread-4] [INFO] BUILD SUCCESSFUL
    [Thread-4] [INFO] ------------------------------------------------------------------------
    [Thread-4] [INFO] Total time: 14 seconds
    [Thread-4] [INFO] Finished at: Wed Nov 24 13:25:45 CET 2010
    [Thread-4] [INFO] Final Memory: 44M/105M
    [Thread-4] [INFO] ------------------------------------------------------------------------
    Created SRC_MAIN_JAVA/org/gvnix/test/bing/search/app/service/SearchService_Roo_GvNix_WebServiceProxy.aj

Creamos un **Controller** para gestionar el cliente desde la aplicación web y añadimos la compatibilidad para ``jsp 2.0``::

    gvnix> controller class --class ~.controller.SearchController
    gvnix> controller jsp2.0 support 

Vemos que se ha creado la estructura web del proyecto (el directorio ``src/main/webapp``) y las vistas básicas para el ``Controller`` sin funcionalidad que ha creado GvNIX::

    src/main/webapp/WEB-INF/views/search/
    ├── index.jspx
    └── views.xml

Preparamos el proyecto para trabajar en eclipse::

    gvnix> perform eclipse

La aplicación ya está preparada para arrancar, se puede hacer la prueba volviendo al ``bash`` y arrancándola con el plugin de *tomcat*::

    bash> mvn clean tomcat:run-war

Accedemos a la dirección http://localhost:8080/bing-search-app/ .

Segunda Parte
------------------

Integrar el cliente generado con la aplicación web.

Dotar de lógica al ``Controller`` *SearchService*.

    * Inyectamos el cliente con la anotación **@Autowired**::
    
        @Autowired
        private SearchService searchService;

    * Definir un método ``GET`` que obtenga el parámetro de búsqueda **stringQuery** y ejecute el cliente con los parámetros necearios::

        @RequestMapping(params = { "find=ByQuery"}, method = RequestMethod.GET, value = "{stringQuery}")
        public String get(@RequestParam("stringQuery") String stringQuery, ModelMap modelMap, HttpServletRequest request, HttpServletResponse response) {
    
            if (stringQuery == null || stringQuery.length() == 0) throw new IllegalArgumentException("A Query is required.");
    
            SearchRequest parameters = new SearchRequest();
            SearchRequest2 searchRequestParameters = new SearchRequest2();
     
            // AppID=8DC4F8381BF17A5FE9BF28F7B5AF12F5EA08462F&Version=2.2
            searchRequestParameters.setAppId("8DC4F8381BF17A5FE9BF28F7B5AF12F5EA08462F");
            searchRequestParameters.setVersion("2.2");
    
            // Query.
            searchRequestParameters.setQuery(stringQuery);
    
            // SourceType type.
            ArrayOfSourceType arrayOfSourceType = new ArrayOfSourceType();
            List<SourceType> sourcesTypeToUpdate = arrayOfSourceType.getSourceType();
            sourcesTypeToUpdate.add(SourceType.WEB);
            searchRequestParameters.setSources(arrayOfSourceType);
    
            // Create search.
            parameters.setParameters(searchRequestParameters);
            // Launch the search.
            SearchResponse searchResponse = searchService.search(parameters);
    
            List<WebResult> webResult = searchResponse.getParameters().getWeb().getResults().getWebResult();
            modelMap.addAttribute("webResult", webResult);
    
            return "search/list";
        }

    * Necesita los valores ``AppID=8DC4F8381BF17A5FE9BF28F7B5AF12F5EA08462F`` y ``Version=2.2`` para certificar la consulta. Esta es una particularidad de esta operación del Servicio Web.  

Actualizar la página ``src/main/webapp/WEB-INF/views/search/index.jspx`` generada por GvNIX para que contenga el formulario de búsquedas::

    <?xml version="1.0" encoding="UTF-8"?>
    <div xmlns:field="urn:jsptagdir:/WEB-INF/tags/form/fields" xmlns:form="urn:jsptagdir:/WEB-INF/tags/form" xmlns:jsp="http://java.sun.com/JSP/Page" version="2.0">
        <jsp:output omit-xml-declaration="yes"/>
        <form:find finderName="ByQuery" id="ff:formulariobing" path="/search/list">
            <field:input label="Consulta" disableFormBinding="true" field="stringQuery" id="f:com.microsoft.schemas.livesearch.u2008.u03.search.SearchRequest2.query" required="true" />
        </form:find>
    </div>

Crear una página jspx para mostrar los resultados. 

    * Utilizaremos los tags que proporciona GvNIX para la nueva jspx ``src/main/webapp/WEB-INF/views/search/list.jspx``::

        <?xml version="1.0" encoding="UTF-8"?>
        <div xmlns:jsp="http://java.sun.com/JSP/Page" xmlns:page="urn:jsptagdir:/WEB-INF/tags/form" xmlns:table="urn:jsptagdir:/WEB-INF/tags/form/fields" version="2.0">
            <jsp:output omit-xml-declaration="yes"/>
            <page:list label="label.webresult.results" id="pl:com.microsoft.schemas.livesearch.u2008.u03.search.SearchResponse.parameters.web.results.webResult" items="${webResult}">
                <table:table data="${webResult}" typeIdFieldName="title" id="l:com.microsoft.schemas.livesearch.u2008.u03.search.WebResult" path="/cars" z="fNYN/4QCBEFvwKAFzL0J/hI1T4M=">
        
                    <table:column id="c:com.microsoft.schemas.livesearch.u2008.u03.search.WebResult.title" property="title" z="/FxqUpZD9TluFywrVRPOLZ94zcA="/>
        
                    <table:column id="c:com.microsoft.schemas.livesearch.u2008.u03.search.WebResult.description" property="description" z="/FxqUpZD9TluFywrVRPOLZ94zcA="/>
        
                    <table:column id="c:com.microsoft.schemas.livesearch.u2008.u03.search.WebResult.url" property="url" z="/FxqUpZD9TluFywrVRPOLZ94zcA="/>
        
                </table:table>
            </page:list>
        </div>
    
    * Instanciar la nueva vista en ``src/main/webapp/WEB-INF/views/search/views.xml``::

        <definition extends="default" name="search/list">
          <put-attribute name="body" value="/WEB-INF/views/search/list.jspx"/>
        </definition>

    * Actualizar el archivo ``src/main/webapp/WEB-INF/i18n/application.properties`` con las etiquetas asociadas a la búsqueda::
    
        #WebResult
        label.formulariobing=Búsqueda en bing
        label.webresult.results=Resultados de la búsqueda
        label.com.microsoft.schemas.livesearch.u2008.u03.search.webresult.title=Título
        label.com.microsoft.schemas.livesearch.u2008.u03.search.webresult.description=Descripción
        label.com.microsoft.schemas.livesearch.u2008.u03.search.webresult.url=URL
        label.com.microsoft.schemas.livesearch.u2008.u03.search.searchresponse.parameters.web.results.webresult.plural=Resultados

Tercera Parte
------------------

Comprobar el funcionamiento de la aplicación

Salimos de la consola de GvNIX y lanzamos la aplicación con el plugin de tomcat::

    bash> mvn clean tomcat:run-war

Probar el cliente introduciendo la consulta para mostrar los resultados en la lista.

