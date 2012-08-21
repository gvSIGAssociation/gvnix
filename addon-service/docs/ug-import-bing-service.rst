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

We will integrate Bing search in our web application using the Bing WS API.

Requirements
--------------

 * JDK 1.6+
 * Maven 2+
 * _gvNIX - Spring Roo - Addon - Services Management_
 * Eclipse Helios 3.6.0 (optional)

Install Addon Services Management
--------------------------------------

Options:

 * To install gvNIX 0.6.0 that contains the add-on installed.
 * OR, to install in Spring Roo 1.1.2 
  * Trust on PGP KEY ID used to sign the add-on. Note that gvNIX project members have their PGP KEYs IDs published at https://code.google.com/p/gvnix/people/list::

      pgp trust --keyId 0xC5FC814B

  * Run the command below::

      addon install bundle --bundleSymbolicName org.gvnix.service.roo.addon

Bing API
----------

To connect to Bing API you have to register at _Bing Developer Center_ and get the Bing _AppID_ and _Version_, both parameters are required to invoke Bing Web Services. 

.. admonition:: DiSiD tests

  AppID:   8DC4F8381BF17A5FE9BF28F7B5AF12F5EA08462F
  Version: 2.2

Go to http://www.bing.com/developers/createapp.aspx for registering.

More info at http://www.bing.com/developers/

The Bing WSDL is published at: http://api.bing.net/search.wsdl

Create your application
=========================

Create a new directory for the project::

  Bash shell:

    mkdir bing-search-app
    cd bing-search-app

Start gvNIX::

  bing-search-app$ gvnix.sh
      ____  ____  ____  
     / __ \/ __ \/ __ \ 
    / /_/ / / / / / / / 
   / _, _/ /_/ / /_/ /   1.1.2.RELEASE [rev fbc33bb]
  /_/ |_|\____/\____/   gvNIX distribution 0.6.0
  
  
  Welcome to Spring Roo. For assistance press TAB or type "hint" then hit ENTER.
  roo>

Create the project::

  roo> project --topLevelPackage org.gvnix.search --projectName bing-search-app

  Created ROOT/pom.xml
  Created SRC_MAIN_JAVA
  Created SRC_MAIN_RESOURCES
  Created SRC_TEST_JAVA
  Created SRC_TEST_RESOURCES
  Created SRC_MAIN_WEBAPP
  Created SRC_MAIN_RESOURCES/META-INF/spring
  Created SRC_MAIN_RESOURCES/META-INF/spring/applicationContext.xml
  Created SRC_MAIN_RESOURCES/log4j.properties

  org.gvnix.search roo> 

Create local service from Bing WSDL that defines the service contract. This local service will act as "service proxy" and will route our local invocations to the remote service::

  org.gvnix.search roo> remote service import ws --wsdl http://api.bing.net/search.wsdl --class ~.service.SearchService

  Created SRC_MAIN_JAVA/org/gvnix/search/service
  Created SRC_MAIN_JAVA/org/gvnix/search/service/SearchService.java
  Updated SRC_MAIN_JAVA/org/gvnix/search/service/SearchService.java
  Updated ROOT/pom.xml [Added repository gvNIX Add-on repository]
  Updated ROOT/pom.xml [Added dependency org.gvnix:org.gvnix.service.roo.addon:0.6.0]
  Updated ROOT/pom.xml [Added property 'gvnix.version' with value '0.6.0']
  Updated ROOT/pom.xml [Added property 'cxf.version' with value '2.2.10']
  Updated ROOT/pom.xml [Added dependency org.apache.cxf:cxf-rt-frontend-jaxws:${cxf.version}]
  Updated ROOT/pom.xml [Added dependency org.apache.cxf:cxf-rt-transports-http:${cxf.version}]
  Updated ROOT/pom.xml [Added dependency org.hibernate.javax.persistence:hibernate-jpa-2.0-api:1.0.0.Final]
  Updated ROOT/pom.xml [Added plugin cxf-codegen-plugin]
  Updated ROOT/pom.xml
  Generating sources ...
  Created SRC_MAIN_JAVA/org/gvnix/search/service/SearchService_Roo_GvNix_WebServiceProxy.aj

  org.gvnix.search roo>

Internally the add-on uses the Maven *cxf-codegen-plugin* to generate the code needed to invoke the remote service. For easier maintenance the generated code is put in *target/generated-sources/client/*. Don't worry, it will be compiled to *target/classes*.

Now, generate a new web page in which we will include the search form for our application::

  org.gvnix.search roo> controller class --class ~.web.SearchController

  Created SRC_MAIN_JAVA/org/gvnix/search/web
  Created SRC_MAIN_JAVA/org/gvnix/search/web/SearchController.java
  Created SRC_MAIN_WEBAPP/WEB-INF/views/search
  Created SRC_MAIN_WEBAPP/WEB-INF/views/search/index.jspx
  Created SRC_MAIN_WEBAPP/WEB-INF/spring
  Created SRC_MAIN_WEBAPP/WEB-INF/spring/webmvc-config.xml
  Created SRC_MAIN_WEBAPP/WEB-INF/web.xml
  ...

  org.gvnix.search roo>

Create Eclipse specific workspace configuration artifacts::

  org.gvnix.search roo> perform eclipse

  [INFO] Scanning for projects...
  [INFO] Searching repository for plugin with prefix: 'eclipse'.
  [INFO] -------------------------------------------------------------------
  [INFO] Building bing-search-app
  [INFO]    task-segment: [eclipse:clean, eclipse:eclipse]
  [INFO] -------------------------------------------------------------------
   ...

  org.gvnix.search roo>

Open your Eclipse and import the project *File > Import > General > Existing Projects into Workspace*

Add the local service reference to our Controller class::

  @Autowired private SearchService searchService;

Add the handler method that will receive the query string::

    @RequestMapping(params = { "find=ByQuery" }, method = RequestMethod.GET, value = "{query}")
    public String get(@RequestParam("query") String query,
            ModelMap modelMap, HttpServletRequest request,
            HttpServletResponse response) {

        if (query == null || query.length() == 0) {
            throw new IllegalArgumentException("A Query is required.");
        }

        SearchRequest parameters = new SearchRequest();
        SearchRequest2 searchRequestParameters = new SearchRequest2();

        searchRequestParameters.setAppId("YOUR_APPID");
        searchRequestParameters.setVersion("2.2");

        // Query.
        searchRequestParameters.setQuery(query);

        // SourceType type.
        ArrayOfSourceType arrayOfSourceType = new ArrayOfSourceType();
        List<SourceType> sourcesTypeToUpdate = arrayOfSourceType
                .getSourceType();
        sourcesTypeToUpdate.add(SourceType.WEB);
        searchRequestParameters.setSources(arrayOfSourceType);

        // Create search.
        parameters.setParameters(searchRequestParameters);
        // Launch the search.
        SearchResponse searchResponse = searchService.search(parameters);

        List<WebResult> webResult = searchResponse.getParameters().getWeb()
                .getResults().getWebResult();
        modelMap.addAttribute("webResult", webResult);

        return "search/list";
    }

Note that *AppID* and *Version* field are required fields for Bing Service only.

Now open ``src/main/webapp/WEB-INF/views/search/index.jspx`` to add the search form to your application::

  <?xml version="1.0" encoding="UTF-8" standalone="no"?>
  <div xmlns:field="urn:jsptagdir:/WEB-INF/tags/form/fields" xmlns:form="urn:jsptagdir:/WEB-INF/tags/form" xmlns:jsp="http://java.sun.com/JSP/Page" version="2.0">
      <jsp:directive.page contentType="text/html;charset=UTF-8"/>
      <jsp:output omit-xml-declaration="yes"/>
      <form:find finderName="ByQuery" id="ff_bing_search" path="/search/list" z="user-managed">
        <field:input label="Bing" disableFormBinding="true" field="query" 
            id="f_com_microsoft_schemas_livesearch_u2008_u03_search_SearchRequest2_query" 
            required="true" />
      </form:find>
  </div>

Create a web page to show the search results, for example ``src/main/webapp/WEB-INF/views/search/list.jspx``::

<?xml version="1.0" encoding="UTF-8" standalone="no"?>
<div xmlns:c="http://java.sun.com/jsp/jstl/core" xmlns:jsp="http://java.sun.com/JSP/Page" xmlns:page="urn:jsptagdir:/WEB-INF/tags/form" xmlns:spring="http://www.springframework.org/tags" xmlns:util="urn:jsptagdir:/WEB-INF/tags/util" version="2.0">
    <jsp:directive.page contentType="text/html;charset=UTF-8"/>
    <jsp:output omit-xml-declaration="yes"/>
    <page:list label="label.webresult.results" id="pl_com_microsoft_bing_webResult" items="${webResult}">
      <c:forEach items="${webResult}" var="result">
        <a href="${result.url}"><c:out value="${result.title}" /></a>
        <br/>
        <c:out value="${result.description}" />
        <br/>
        <span style="color: green;"><c:out value="${result.url}" /></span>
        <br/>
        <br/>
      </c:forEach>
    </page:list>
</div>

Register the new view at ``src/main/webapp/WEB-INF/views/search/views.xml``::

  <definition extends="default" name="search/list">
    <put-attribute name="body" value="/WEB-INF/views/search/list.jspx"/>
  </definition>

Update labels at ``src/main/webapp/WEB-INF/i18n/application.properties``::

  application_name=Bing Search Demo
  label_search_index=Search at Bing
  
  menu_category_controller_label=Controller
  menu_item_controller__searchindex_id_label=Search at Bing
  
  label_bing_search=Bing Search
  label_com_microsoft_bing_webresult_plural=Bing Results

Finally, run ``mvn tomcat:run`` in the root of your project and the application should be available under the URL http://localhost:8080/bing-search-app/

