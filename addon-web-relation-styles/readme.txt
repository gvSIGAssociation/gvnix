==================================
 gvNIX Web relation styles add-on
==================================

:Project:   gvNIX. Spring Roo based RAD tool
:Copyright: Conselleria d'Infraestructures i Transport - Generalitat Valenciana
:Author:    [ ... ]
:Revision:  $Rev$
:Date:      $Date$

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

Add-on update jspx views from entities that have OneToMany mapped relationship to show them inside paginated tables grouped by tabs.

Project contents
=================

This folder contains add-on sources and documents folder ``docs`` with documentation of this project.

Features
===========

This add-ons have this features:

#. Installs new tagx in the project.
#. Generates AspectJ files to get related entities paginated to be show in the jspx table inside a tab.
#. Update the jspx views using featured tagx from the Add-on.

Installation Proof
===================

Install the Add-on.
--------------------

Install the Add-on in gvNIX/Roo shell with the required commands for 1.1.0-M1 version.

Follow instructions of document https://svn.disid.com/svn/gvcit/gvNIX/roo-addon-roo-1.1.rst .

Create a web application
-------------------------

Create a web application with gvNix/Roo Shell script in ``test-roo directory``::

  script --file test-script-roo

Run the application in Bash Shell and see the actual style::

  mvn tomcat:run

Back to the gvNIX/Roo Shell and type::

  relationships setup table

After copying tagx files, generate AspectJ files and new jspx views, run again the applicationwith the maven commmand::

  mvn tomcat:run

Uninstall the Add-on
=====================

To remove the Add-on functionalities from your project you have to delete **gvnixcallfunction.tagx** file located in::

 - src/main/webapp/WEB-INF/tags/util/gvnixcallfunction.tagx

Remove all related entities called with the tagx in jspx show and update.

Future enhancements
====================

Choose between two types of visualization for oneToMany relationships:

# Tabs: Show the relationships 1-n between entities inside tabs.
# Accordion: Show the relationships 1-n between entities inside panels (current default option).

# Remove command: To remove all related entities called with the tagx in jspx (show and update). 