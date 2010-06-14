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

Requirements
=============

Development mode
-----------------

Runtime mode
-------------

Make gvNIX installation
------------------------

Installation Proof
===================

Install the Add-on.
--------------------

Install the Add-on in gvNIX/Roo shell with the required commands for 1.1.0-M1 version.

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

To remove the Add-on you have to delete **gvnixcallfunction.tagx** file located in::

 - src/main/webapp/WEB-INF/tags/util/gvnixcallfunction.tagx

Future enhancements
====================

Choose between two types of visualization for oneToMany relationships:
1
# Tabs: Show the relationships 1-n between entities inside tabs.
# Accordion: Show the relationships 1-n between entities inside panels (current default option).
