==================================
 gvNIX Web report add-on
==================================


-----------------
Technical Design
-----------------

:Project:   gvNIX. Spring Roo based RAD tool
:Copyright: Conselleria d'Infraestructures i Transport - Generalitat Valenciana
:Author:    DiSiD Technologies, S.L.
:Revision:  $Rev$
:Date:      $Date$

This work is licensed under the Creative Commons Attribution-Share Alike 3.0    Unported License. To view a copy of this license, visit
http://creativecommons.org/licenses/by-sa/3.0/ or send a letter to
Creative Commons, 171 Second Street, Suite 300, San Francisco, California,
94105, USA.

.. contents::
   :depth: 2
   :backlinks: none

.. |date| date::

Introduction
===============

This document contents relative to this add-on.

Requirements
=============

In the first release of this add-on, it have to install JasperReports support and create a sample report of Roo projects.

The desired features of the add-on could be:

* setup of several Java Report tools.
* create reports of the set Java Report tool for all the finder methods of the project Entities.
* create a report for a desired finder method of an Entity.
* remove a report.
* list all the reports in the project.

Analysis
=========

(First version)

* gvNIX command:  `web report add --controller <controller> --reportName <report_name> --format <format>`

When the command is issued the following operations must be performed:

* Add Maven dependencies for JasperReports to the pom.xml of the project.
* Set configuration for the JasperReport "views" in:
  - WEB-INF/spring/webmvc-config.xml
  - WEB-INF/spring/jasper-views.xml (this one will be created the first time)
* Annotate the <controller> with the annotation @GvNixReports({"<report_name>|<format>"}). Add to the <controller>Controler.aj a new controller method with the
  *@RequestMapping("/reports/<report_name>")* annotation and accepting a request paramater *format* (String). In this controller the report DataSource will be
  populated with the result of the *<entity>.findEntries(0, 10)*. The entity will be given by the fromBackingOject attribute in the RooWebScaffold annotation.
* Add a new file under WEB-INF/reports with the name <report_name>.jrxml. This report will be the sample report showing <report_name> in the title of the
  report and some fields of the entity in the detail band, so, the *<field/>* and *<textField/>* nodes must be created in the jrxml file.
* Create a new JSPX under the <entity> views folder with <report_name> as file name. The new JSP will have a simple form with a drop-down select field
  with formats defined for this report and a submit button requesting the report generation. Add a new menu entry under the <entity> section pointing to the
  new controller action.

Of course, the command must check if it was issued before, so the configuration of JasperReports dependencies and "views" must be skipped.

We decided annotate the Controller instead of the Entity because the access to the report generation depends directly of the web tier. The Controller informs,
through the fromBackingObject attribute in the @RooWebScaffold, the related Entity.

Addon Annotations
==================

* @GvNixReports: Defines the list of supported reports handled by the annotated Controller.

Add a new report
-------------------

Define **@GvNIXReports** setting *reportName|format* as value. The annotation support as many *reportName|format* pairs as reports you must handle with the annotated
Controller.

The format part of the annotation value can be a String with the formats you want available for this report. Supported formats are: *pdf, xls, csv, html*. So, the
annotation will have the following look @GvNIXReports({"<reportName>|<formats as csv>"[,"<reportName>|<formats as csv>"]}). Once a report has been defined you can
add or remove supported formats.

Operations
===========

setup
-----

Performs the needed operations for give JasperReports support in the project.

* Add a repository definition, some properties, add-on library dependency and JasperReports dependencies to porm.xml.
* Modify Spring MVC config file (*webmvc-config.xml*) adding JasperReportsXmlViewResolver as views resolver.
* Install the JasperReports views config file **jasper-views.xml**.
* Install JasperReports fonts. With them it install a file defining a JasperReport extension where the fonts are set for
  the report render.

annotateType
------------

Add the *GvNIXReports* annotation to the Controller class or update its value.

Annotate Controller triggers all the Metadata operations ending with a fully functional report in the project. These operations
are:

* If *setup* operation has not yet been performed, *annotateType* launches it.
* Creates and installs the sample report design file.
* Adds a JasperReport view definition in *jasper-views.xml*.
* Creates / Updates the Controller ITD with the methods handling report requests.
* Installs a JSP with the form for report requests.

Metadata
=========

ReportMetadaProvider
---------------------

Records dependencies between WebScaffoldMetadata and ReportMetada so, any change in *@GvNIXReports* values triggers operations
over ReportMetadata.

Also it provides components and data needed by RepotMetadata.

ReportMetada
-------------

Represents the reports installed in a Controller. It's responsible of ITD creation. Here we create the methods of the Controller
handling requests related with reports, that is, a method returning the form to request the report and the method returning
the generated report. Other important operation performed by ReportMetadata is the creation of the sample report design file,
a JRXML file as a starting point of the report development.

Interesting member fields
~~~~~~~~~~~~~~~~~~~~~~~~~
 * *reportMethods*: A list of *MethodMetada* with the metadata of methods in Report ITD.
 * *controllerMethods*: A list of *MethodMetada* with metadata of all the methods in the WebScaffold. This is needed in
   in order to check if methods that ReportMetadata created in the ITD already exist in WebScaffold.
 * *installedReports*: A list of Strings representing the values in *@GvNIXReports*.
 * *webScaffoldMetada*: Metadata about the Controller. We need it for some operations done in *ReportJspMetadataListener*.

ReportJspMetadata
------------------

Represents the JSP of the form in the web tier that allow users to request a report.

Just stores a reference to its ReportMetadata.

ReportJspMetadataListener
--------------------------

Handles the changes in the ReportMetadata ITD, so when they happen, it triggers changes in the JSP. Actually, just the creation
of the JSP. In the future other changes as adapt the form to the fields finder form will be performed.

So, its main purpose is to create the JSP with the form requesting the report.

Proof of Concept
================

* http://scmcit.gva.es/svn/gvnix-proof/trunk/jasperreports-app
* https://svn.disid.com/svn/disid/proof/gvnix/jasperreports-app


TODO
====

* https://jira.springsource.org/browse/ROO-228?focusedCommentId=64509&page=com.atlassian.jira.plugin.system.issuetabpanels%3Acomment-tabpanel#action_64509

* Revisit ReportMetadataProvider in order to modify the method used to retrieve WebScaffoldMetadata via MetadataService instead of
  WebScaffoldMetadataProvider.
