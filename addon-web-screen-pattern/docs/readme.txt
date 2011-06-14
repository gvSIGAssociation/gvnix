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

Add-on updates jspx views from entities that have OneToMany mapped relationship to show them inside paginated tables grouped by
tabs (pattern tabular) or in paginated views of single records (pattern register)

  .. admonition:: What is an screen pattern?

    http://designingwebinterfaces.com/designing-web-interfaces-12-screen-patterns

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

Create a web application
-------------------------

Create a web application with gvNix/Roo Shell script in ``test-roo directory``::

  script --file test-script-roo

Run the application in Bash Shell and see the actual style::

  mvn tomcat:run


Uninstall the Add-on
=====================

Remove all related entities called with the tagx in jspx show and update.

Limitations
===========

* Move the fields annotated with @OneToMany and @ManyToOne (both senses of the relationship) from .aj file to it related .java file.
  This fields are on .aj files in some cases as when there are generated with database reverse engineering.
* Create getId and setId primary key methods for each entity involved, if not exists on related .java or .aj.
  This is important because current version does not support renaming relations table ID field and therefore we have to create the accessor and mutator hand with the name ID.

Future enhancements
====================

Choose between two types of visualization for oneToMany relationships:

# Tabs: Show the relationships 1-n between entities inside tabs.
# Accordion: Show the relationships 1-n between entities inside panels (current default option).
# Remove command: To remove all related entities called with the tagx in jspx (show and update).
