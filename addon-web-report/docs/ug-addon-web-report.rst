==================================
 gvNIX Web report add-on
==================================

-----------
User Guide
-----------

:Project:   gvNIX. Spring Roo based RAD tool
:Copyright: DGTI - Generalitat Valenciana
:Author:    DISID Corporation, S.L.
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

Adds JasperReports support to Roo-based project.

Requirements
--------------

* JDK 1.6+
* Maven 2+
* gvNIX - Spring Roo - Addon - Web Report
* Eclipse Helios 3.6.0 (optional)

Install Addon Services Management
------------------------------------

Options:

* To install gvNIX 0.6.0 that contains the add-on installed.
* OR, to install in Spring Roo 1.1.2 

  - Trust on PGP KEY ID used to sign the add-on. Note that gvNIX project members have their PGP KEYs IDs published at https://code.google.com/p/gvnix/people/list::

      pgp trust --keyId 0xC5FC814B

  - Run the command below::

      addon install bundle --bundleSymbolicName org.gvnix.web.report.roo.addon

Use case: Reports support in Pet clinic 
============================================

Create a new directory for the project::

  Bash shell:

    mkdir clinic
    cd clinic

Start gvNIX::

  clinic$ gvnix.sh
      ____  ____  ____  
     / __ \/ __ \/ __ \ 
    / /_/ / / / / / / / 
   / _, _/ /_/ / /_/ /   1.1.2.RELEASE [rev fbc33bb]
  /_/ |_|\____/\____/   gvNIX distribution 0.6.0
  
  
  Welcome to Spring Roo. For assistance press TAB or type "hint" then hit ENTER.
  roo>

Create the project::

  roo> script clinic.roo

Add a new report::

  roo> web report add --controller ~.web.PetController --reportName PetReport --format pdf,xls

Run `mvn tomcat:run` in the root of your project and the report should be available under the URL http://localhost:8080/petclinic/pets/reports/petreport?form

About the command
-------------------

The command performs the following operations:

* Adds the add-on and JasperReports dependencies to the project *pom.xml*
* Adds JasperReporViewsResolver in *webmv-config.xml*
* Installs *jasper-views.xml* as config file for ``CustomJasperReportsMultiFormatView.java``.
* Installs *jasperreports_extension.properties* and the FreeSans font family TTF fonts in the webapp classpath.

The command accepts the parameters below:

* *controller*: Is an existing class in your project handling web requests. This controller must be annotated with
* *@RooWebScaffold* and its attribute *formBackingObject* must be informed. The add-on takes this annotation and its attribute value for code generation. The value of *formBackingObject* informs which is the Entity exposed by the controller. The parameter is mandatory and has not default value.
* *reportName*: Is the name of the report. The value of the parameter will be transformed to lower case. The parameter is mandatory and has not default value.
* *format*: The value can be a single format (pdf (Portable Document Format) | xls (Excel) | csv (Comma Separated Values) | html (HyperText Markup Language)) or a comma separated value string given several formats (ie: pdf,xls) at once. This parameter is optional and its default value is *pdf*.

Command unleashed:

When the command is launched installs a Java file class into *controller-sub-package.servlet.view.jasperreports sub-package* that handles the render report operation and makes possible a better naming of the generated output file. Its name is ``CustomJasperReportsMultiFormatView.java``. This class is installed only once, the following invocations checks it the Java file exists.

Over this class a new bean is defined in **jasper-views.xml** file. This new bean will have *<formBackingObject_name>_<reportname>* as id and the class will be our CustomJasperReportsMultiFormatView. The bean defines a JasperReport view resolver.

The command creates a sample report in JasperReport XML format (jrxml) using three fields of the entity informed by *formBackingObject* attribute in annotation @RooWebScaffold as detail of the report. The sample report is in the file *src/main/webapp/WEB-INF/reports/<formBackingObject_name>_<reportname>.jrxml*. As the report renders the title in bold, it needs to have available a bold TTF font. So, it installs some fonts under *src/main/webapp/WEB-INF/classes/jasperfonts/*.

In order to handle the report requests a new view is installed under *src/main/webapp/WEB-INF/views/<fromBakingObject_name>/<reportname>.jspx*. This view is a simple form where the user can select the output format of the report and request it using the submit button. This form needs an end-point listening its requests, so, the command creates two methods in an ITD file called ``<controller>_Roo_GvNIXReport.aj``. One is the method returning the form view (``generate<Reportname>Form(...)``) and the other is the method collecting the data needed for render the report and returning the output file (``generate<Reportname>(..)``). This method invokes ``<formBackingObject>.find<formBackingObject>Entires(0, 10)`` as example of how to populate the report datasource.

The command can be launched twice with the the same *controller* and same *reportName* values but given other *format*. If so, the new formats are added as supported format of the existing report. You can not add the same report with the same formats twice instead.

Modifying source code
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

Annotate the Controller with::

  @GvNIXReports({ "<reportName>|<formats>"[, "<reportName2>|<formats>"] })

save file changes and let gvNIX/Roo do its magic.

The value of the *@GvNIXReports* annotation is case insensitive, so, something like::

  @GvNIXReports({ "myfirstreport|pdf", "myFirstREPORT|xls,csv" })

is equivalent to::

  @GvNIXReports({ "myfirstreport|pdf,xls,csv" })


