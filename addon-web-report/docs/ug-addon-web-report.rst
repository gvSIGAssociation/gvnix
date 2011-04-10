==================================
 gvNIX Web report add-on
==================================

-----------
User Guide
-----------

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

Introduction
===============

Create a gvNIX project with JasperReports support.

Prerequisites
===============

Requirements
===============

To have installed the following rquirements:

* JDK-1.5.07
* Maven 2.0.9
* GvNIX 0.6.0
* Eclipse Galileo 3.5.1 or higher


Use case
===========

Setup JasperReports support in the project
-------------------------------------------

With gvNIX/Roo shell
~~~~~~~~~~~~~~~~~~~~

::

  web report setup

The command performs the following operations:

* Adds the add-on and JasperReports dependencies to the project pom.xml
* Adds JasperReporViewsResolver in webmv-config.xml
* Installs jasper-views.xml as config file for JasperReportViewResolver.
* Installs jasperreports_extension.properties and the FreeSans font family TTF fonts in the webapp classpath.


Add a new Report for the application
-------------------------------------

With gvNIX/Roo shell
~~~~~~~~~~~~~~~~~~~~

::

  web report add --controller <existing controller> --reportName <report name> --format <pdf,xls,csv,html>

**About command parameters:**

  + *controller*: Is an existing class in your project handling web requests. This controller must be annotated with
    *@RooWebScaffold* and its attribute *fromBackingObject* must be informed. The add-on takes this annotation and its
    attribute value for code generation. The value of *fromBackingObject* informs which is the Entity exposed by the
    controller. The parameter is mandatory and has not default value.
  + *reportName*: Is the name of the report. The value of the parameter will be transformed to lower case. The parameter
    is mandatory and has not default value.
  + *format*: The value can be a single format (pdf (Portable Document Format) | xls (Excel) |
    csv (Comma Separated Values) | html (HyperText Markup Language)) or a comma separated value string given several formats
    (ie: pdf,xls) at once. This parameter is optional and its default value is *pdf*.

**About command itself:**

The command raise several operations that finally add the support to generate a report using JasperReports as report tool.

If the command is launched when *web report setup* is not already launched, it performs the setup operation.

When the command is launched installs a Java file class into *controller-sub-package.servlet.view.jasperreports sub-package*
that handles the render report operation and makes possible a better naming of the generated output file. Its name is
CustomJasperReportsMultiFormatView.java. This class is installed only once, the following invocations checks it the
Java file exists.

Over this class a new bean is defined in **jasper-views.xml** file. This new bean will have
*<fromBackingObject_name>_<reportname>* as id and the class will be our CustomJasperReportsMultiFormatView. The bean defines
a JasperReport view resolver.

The command creates a sample report in JasperReport XML format (jrxml) using three fields of the entity informed by
*fromBackingObject* attribute in annotation @RooWebScaffold as detail of the report. The sample report is in the file
*src/main/webapp/WEB-INF/reports/<fromBackingObject_name>_<reportname>.jrxml*. As the report renders the title in bold,
it needs to have available a bold TTF font. So, it installs some fonts under *src/main/webapp/WEB-INF/classes/jasperfonts/*.

In order to handle the report requests a new view is installed under
*src/main/webapp/WEB-INF/views/<fromBakingObject_name>/<reportname>.jspx*. This view is a simple form where the user can
select the output format of the report and request it using the submit button. This form needs an end-point listening its
requests, so, the command creates two methods in an ITD file called ``<controller>_Roo_GvNIXReport.aj``. One is the method
returning the form view (``generate<Reportname>Form(...)``) and the other is the method collecting the data needed for render
the report and returning the output file (``generate<Reportname>(..)``). This method invokes
``<fromBackingObject>.find<fromBackingObject>Entires(0, 10)`` as example of how to populate the report datasource.

The command can be launched twice whit the the same *controller* and same *reportName* values but given other
*format*. If so, the new formats are added as supported format of the existing report. You can not add the same report with
the same formats twice instead.

Modifying Controller source code
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

Annotate the Controller with::

  @GvNIXReports({ "<reportName>|<formats>"[, "<reportName2>|<formats>"] })

save file changes and let gvNIX/Roo do its magic.

The value of the *@GvNIXReports* annotation is case insensitive, so, something like::

  @GvNIXReports({ "myfirstreport|pdf", "myFirstREPORT|xls,csv" })

is equivalent to::

  @GvNIXReports({ "myfirstreport|pdf,xls,csv" })


