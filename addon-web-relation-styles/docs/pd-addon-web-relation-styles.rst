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

This add-on have to install and manage the application visualization for entities.

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
----------------------------------

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
  * Crete AspecJ method for related entities that are annotated as 1-n relationship.
# Use DefaultClassOrInterfaceDetail to retrieve the annotation information for the declared relationships.

Create MetadataProvider that listens RooEntity's changes
..........................................................

Retrieve the metadata to be written. Always checks the Entity to generate the methods.

# Register as RooEntity to launch the generation.

Create MetadataListener
------------------------

Crear un listener que extienda de MetadataListener para escuchar a RooWebSaffold o JspMetadata. INFO.
# Register the MetadataListener (@Service, @Component).
# Check if have been added 1-n LAZY relationships to the entities.
- Use the defined access method to the entity metadata properties to be show in jspx.
# Check **always** the generated code using Strings (the z attribute can't be used).

Jspx files
...........

Acceder a las jspx show y update.

* El atributo ``render`` de las relaciones generadas por rooWebScaffold ponerlo a false para que no muestre lo que genera Roo.
* Crear la llamada al tag que engloba las relaciones fuera del tag de page (page:show o page:update que utiliza Roo).

  * Comprobar si existe en la jspx
  * Si no existe

    * <relations:tab> en el caso que se utilice la visualización por ``tabs``.
    * Dentro de esta etiqueta instancia la propiedad mediante la llamada al tagx <relation:tabview> (en el caso de mostrar los datos dentro de las pestañas) para la visualización con el formato de tabla incluyendo los parámetros necesarios para generar el código.
  * Si existe

    * Dentro de esta etiqueta instancia la propiedad mediante la llamada al tagx <relation:tabview> (en el caso de mostrar los datos dentro de las pestañas) para la visualización con el formato de tabla incluyendo los parámetros necesarios para generar el código.

Roo Shell commands
====================

Comandos asociados al Add-on.

``web relation styles setup``
------------------------------

Use cases
=============

Installation
----------------

Developer wants to use new menu in his Roo application. This are the steeps to get it:

#. Install this add-on if it isn't already installed.

#. Execute command ``web relation styles setup --view tab``.

Future enhancements
====================

Add Parameters to setup command:

* ``--view`` (mandatory): Selects the view to show the relations of an Entity.
