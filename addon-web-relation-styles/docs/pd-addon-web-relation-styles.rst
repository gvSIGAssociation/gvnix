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

# Preparar los archivos que se van a copiar al proyecto:
- Conjunto de tagx para la visualización.
# Crear MetadataProvider y Metadata para la generación de:
- Archivos AspectJ con el método para la búsqueda de elementos paginados. Regenerar completamente la clase AspectJ con los métodos.
# Crear un MetadataListener para monitorizar los cambios cuando se haya ejecutado el @RooWebScaffold:
- Regenerar los ficheros jspx y comprobar si han cambiado para sustituirlos por los nuevos o mantener los anteriores. 
- Se genera a partir de los datos que se recogen de la entidad relacionada a la vista.
# Activar el Add-on al inicializar la consola de roo.

Estructura para la creación del Add-on
========================================

Add-on functionality.

Group of tagx for visualization.
--------------------------------------

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

Retrieve the metadata to be written. Always generates the whole file to disk.

# Register as RooEntity to launch the generation.

Create MetadataListener
------------------------

Crear un listener que extienda de MetadataListener para escuchar a RooWebSaffold o JspMetadata. INFO.
# Registrar el MetadataListener (@Service, @Component).
# Comprobar si se han añadido relaciones del tipo 1-n LAZY a las entidades con los métodos de acceso a las propiedades de la entidad que se visualiza en la jspx.
# **Siempre** comprobar lo que se ha generado usando Strings (no se pueden utilizar los atributos z).

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
