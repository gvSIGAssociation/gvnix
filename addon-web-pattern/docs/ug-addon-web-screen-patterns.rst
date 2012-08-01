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

Add-on allow jspx views creation for entities showed with different patterns:

  * tabular: Paginated and modifiable table of records
  * register: Paginated views of single records


Also, allow jspx views creation for OneToMany and ManyToMany mapped relationship to show them with tabular pattern or register pattern.

  .. admonition:: What is an screen pattern?

    http://designingwebinterfaces.com/designing-web-interfaces-12-screen-patterns

Features
===========

This add-ons have this features:

#. Installs new JSPXs, TAGXs, images, CSS, scripts (js) and modify i18n properties in the project.
#. Create jspx views using featured tagx from the Add-on.
#. Create AspectJ files related to entities new applied patterns.

Use cases
=============

Create a web application
-------------------------

Developer wants to use new patterns in his Roo application. This are the steeps to get it:

#. Install this add-on if it isn't already installed.

#. Create a web application with gvNix/Roo Shell script in ``src/test/resources`` directory::

      script --file test-script-roo

#. Run the application in Eclipse or in Bash Shell with::

  mvn tomcat:run

Uninstall a pattern
===================

Remove new pattern jspx view, menu entry and this pattern related annotations.

Limitations
===========

* Move the fields annotated with @OneToMany and @ManyToOne (both senses of the relationship) from .aj file to it related .java file.
  This fields are on .aj files in some cases as when there are generated with database reverse engineering.
* Create getId and setId primary key methods for each entity involved, if not exists on related .java or .aj.
  This is important because current version does not support renaming relations table ID field and therefore we have to create the accessor and mutator hand with the name ID.

Future enhancements
====================

Choose between two types of visualization for oneToMany and manyToMany relationships:

* Tabs: Show the relationships 1-n between entities inside tabs.
* Accordion: Show the relationships 1-n between entities inside panels (current default option).
* Remove command: To remove all related entities called with the tagx in jspx (show and update).
