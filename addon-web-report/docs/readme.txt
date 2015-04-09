==================================
 gvNIX Web report add-on
==================================

:Project:   gvNIX. Spring Roo based RAD tool
:Copyright: DGTI - Generalitat Valenciana
:Author:    DISID Corporation, S.L.
:Revision:  $Rev: 1535 $
:Date:      $Date: 2012-09-01 10:55:06 +0200 (sáb, 01 sep 2012) $

This work is licensed under the Creative Commons Attribution-Share Alike 3.0
Unported License. To view a copy of this license, visit
http://creativecommons.org/licenses/by-sa/3.0/ or send a letter to
Creative Commons, 171 Second Street, Suite 300, San Francisco, California,
94105, USA.

.. contents::
   :depth: 2
   :backlinks: none

.. |date| date::

Introduction
===============

Add-on giving reports support.

Supported report tools:

# JasperReports

Project contents
=================

This folder contains add-on sources and documents folder ``docs`` with documentation of this project.


Installation
============

Install the Add-on.
--------------------

Install gvNIX or the Add-on in Roo shell.

Create a web application
-------------------------

Create a web application with gvNIX/Roo Shell script::

    project --topLevelPackage org.gvnix.test.report
    jpa setup --provider HIBERNATE --database HYPERSONIC_IN_MEMORY
    entity jpa --class ~.domain.Person --testAutomatically
    field string --fieldName name --notNull
    web mvc setup
    web mvc all --package ~.web

Run the application and take a look to the menu entries::

  mvn tomcat:run

Stop tomcat and apply gvNIX report (Install and set gvNIX commands) to the project::

    web report add --controller ~.web.PersonController --reportName samplereport

Run the application and a new menu entry has been added (Person samplereport Report)::

  mvn tomcat:run

The new menu entry gives access to a new form where you can request for a report generation over Person entity. By now,
only PDF is the output format available for your report.

Stop tomcat and add a new supported format to your existing report through gvNIX/Roo shell::

  web report add --controller ~.web.PersonController --reportName samplereport --format xls,csv

Run the appliation::

  mvn tomcat:run

In the same form page as before the drop-down select has PDF, XLS and CSV as available formats.
