==================================
 gvNIX Web relation styles add-on
==================================

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

Definitions
=================

Requirements
=============

This add-on have to install and manage the application visualization for entities paginated inside a table grouped by tabs.

# Tagx files to show the related entities in forms show and update.

# Create MetadataProvider and Metadata for generate:
- AspectJ file and method to retrieve paginated results for related entities. 
- Regenerate the AspectJ class when perform any change in the one_to_many relations of the entity.
# Create a MetadataListener to monitoring changes after the execution @RooWebScaffold:
- Regenerate jspx files (show and update) if there any changes in the entity adding o removing a 1-n relationship.
- It depends on the entity data class and its relationships.
# Activate the Add-on at Roo console startup to monitoring. After its installation.

Functionality
===============

Add-on functionality.

Group of tagx for visualization structure.
------------------------------------------

Copy the tagx included in resources folder of the Add-on inside ``WEB-INF/`` project folder::

    src/main/webapp/WEB-INF/tags/relations/
    |-- decorators
    |   |-- accordiontableview.tagx
    |   `-- tabtableview.tagx
    |-- relationspagination.tagx
    |-- relationstable.tagx
    |-- accordion.tagx
    `-- tab.tagx
    src/main/webapp/WEB-INF/tags/util
    `-- gvnixcallfunction.tagx

ITD
----

Create a Metadata
...................

Metadata for create AspecJ method using entity's properties (1-n relations).

# The metadata creates the search method for paginated related entities.

  * Crete AspecJ method for retrieve related entities that are annotated as 1-n relationship.
  
# Use DefaultClassOrInterfaceDetail to retrieve the annotation information for the declared relationships.

Create MetadataProvider that listens RooEntity's changes
..........................................................

Retrieve the metadata to be written. Always checks the Entity to generate the methods.

# Register as RooEntity to launch the generation.

Create MetadataListener
------------------------

Create a listener that implements ``MetadataNotificationListener`` to listen JspMetadata notifications.
# Register the MetadataListener (@Service, @Component(immediate = true)).
# Check if have been added 1-n LAZY relationships to the entities.
- Use the defined access method to the entity metadata properties to be show in jspx.
# Check **always** the generated code using Strings (the z attribute can't be used).

Jspx files
...........

Acceder a las jspx show y update.
Access to show and update jspx files::

* Set ``render`` attribute in OneToMany relationships to false.
* Create the new tagx call to show related entities outside tagx page (page:show or page:update depending what jspx you are checking).

  * Check if exists
  * Not Exists

    * <relations:tab> using tabs visualization: ``tabs``.
    * This tag contains each relationship definided inside using tagx from the Add-on ``<relation:tabview>``.
      
      * For each property of the related entity have to create a column tag to show the element properties inside table columns.
  * Exists

    * This tag contains each relationship definided inside using tagx from the Add-on ``<relation:tabview>``.
      
      * For each property of the related entity have to create a column tag to show the element properties inside table columns.

Roo Shell commands
====================

Comandos asociados al Add-on.

``relationships setup table``
------------------------------

This method install tagx, creates AspectJ files and updates the jspx views.

This doesn't need any parameters.

Use cases
=============

Installation
----------------

Developer wants to use new menu in his Roo application. This are the steeps to get it:

#. Install this add-on if it isn't already installed.

#. Create a web application with gvNix/Roo Shell script in ``test-roo`` directory::

      script --file test-script-roo

Future enhancements
====================

* Add Parameters to setup command:

 * ``--view`` (mandatory): Selects the view to show the relations of an Entity.

* Support database reverse engineering OneToMany relations generated on .aj file.

* Support primary key with name different of "id" (no getId and setId). 

New commands
--------------

``relationships remove table``
------------------------------

Remove the tagx, aspectJ files and the tagx references in jspx from the project.

TODO
====

* El import de la clase BeanInfoUtils debe referenciar al add-on de classpath.

* Forzar altura de la capa contenedora de tabs para que no quede oculta la tabla por debajo.
  El tamaño no puede ser fijo, pq la tabla puede crecer más aún del tamaño máximo fijado.
  r148