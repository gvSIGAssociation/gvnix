==================================
 gvNIX Web screen patterns add-on
==================================

:Project:   gvNIX. Spring Roo based RAD tool
:Copyright: Conselleria d'Infraestructures i Transport - Generalitat Valenciana
:Author:    DiSiD Technologies, S.L.
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

This document contents relative to this add-on.

Definitions
=================

TBC

Requirements
=============

#. This add-on have to install and manage the application visualization for entities by applying screen patterns.

   .. admonition:: What is an screen pattern?

    http://designingwebinterfaces.com/designing-web-interfaces-12-screen-patterns

   So, it should to create/maintain JSPx/TAGx/JS/CSS/Images files, the code in Controllers and optionally the code in Entities.
   Currently screen patterns will be applied to Spring MVC projects.
   
#. A pattern layout must be defined by any developer, that is, he will be able to set patterns as master-register/detail-table, master-table/detail-register, master-table/detail-table/detail-register, etc.

#. The same entity could be set with several patterns based on which context is defining the pattern (Entit1 <related with> Entity2 and Entity3; Entity2 <related with> Entity3).

TBC: How form fields will be layout, how to change form field layouts, applied to relation choosen by developer, ....

Tech design
============

#. Create the needed metadata to handle the defined pattern. That is, for a given Entity how this must be shown in CRUD operations, and how its related entities (@OneToMany annotated fields) must be shown in the same context.

#. JSPx, TAGx to work with an entity and its related entities. The layout must follow the defined pattern (master register, mater tabular, detail register, detail tabular).

Command
---------

::

  web mvc pattern --class ~.web.PetController --type table --name MyPattern

Options:

* class: The path and name of the controller object to be used. *Required*. Extends: JavaType

  Definir nuestro RooWebScaffoldJavaType que extiende JavaType y que adicionalmente comprobará que la anotación @RooWebScaffold se encuentra definida en el tipo principal de la clase Java.
  Comprobar que la anotación @RooWebScaffold se encuentra definida en el tipo principal de la clase Java

* type: The pattern type to include on the controller object. *Required*

  Debe ser un tipo enumerado que permita elegir autocompletando de entre ciertos posibles valores.

  En una primera versión, la única posibilidad de este tipo enumerado será el valor "table".

* name: Unique name for the pattern in whole project. *Required*. Type: JavaSymbolName, it avoids non valid characters.

El comando tendrá que incluir la anotación @GvNIXPattern en la clase indicada por la opción "class" del comando y con el siguiente value::

  @GvNIXPattern({"MyPattern=table"})

::

  web mvc relation pattern --class ~.web.PetController --name MyPattern --field field1 --type table 

Options:

* class: The path and name of the controller object to be used. *Required*. Extends: JavaType

  La clase debe estar ya anotada con @GvNIXPattern. 

* name: name for the pattern in whole project to set relation pattern to. *Required*. Type: JavaSymbolName, it avoids non valid characters.

  Debe estar un nombre definido en la anotación @GvNIXPattern

* field: One-to-many property name to apply pattern to. *Required*

  Ver la posibilidad de autocompletar.

* type: The pattern type to apply pattern to. *Required*

  Debe ser un tipo enumerado que permita elegir autocompletando de entre ciertos posibles valores.

  En una primera versión, la única posibilidad de este tipo enumerado será el valor "table".

El comando tendrá que incluir la anotación @GvNIXRelationsPattern en la clase indicada por la opción "class" del comando y con el siguiente value::

  @GvNIXRelationsPattern({"MyPattern: field1=table"})``

Si la anotación ya tubuiese un elemento para el patron añadido, el campo se añadira separado por comas::

  @GvNIXRelationsPattern({"MyPattern: otherField=register, field1=table", "OtherPattern: otherField=talbe"})``

ITD
----

Metadata
..........

* Metadata for Controller:

    It defines as many layouts as contexts involves **formBackingObject** entity. In some cases, the
    **formBackingObject** entity can act as main entity in the view and its related entities act as
    details of the first one, but in other cases this entity would act as detail of an other entity.
    That is what we call context in this scenario.

    So, the metadata in controller holds information about how to render **formBackingObject** in
    each context.

    The annotations defining these metadata will be:

    * **@GvNIXPattern**: Defines patterns over formBackingObject entity.
      e.g: ``@GvNIXPattern({"pattern_id1=register", "pattern_id2=table"})``. (the format may change)

    * **@GvNIXRelationsPattern**: Defines patterns over the entities related with formBackingObject
      entity.
      e.g: ``@GvNIXRelationsPattern({"pattern_id1: field1=table, field2=table",
      "pattern_id2: field2=table"})`` (the format may change)

    ``@GvNIXPattern`` annotation triggers the generation of the AspectJ files with the methods in the
    controller accepting request of operations over the entity. Also, they trigger the generation of
    the MVC artifacts (JSPx files) rendering views of the formBackingObject.

    Both, ``@GvNIXPattern`` and ``@GvNIXRelationsPattern``, trigger the modification of the JAVA files
    defining the related entities adding the annotation ``GvNIXEntityBatch`` (described below) when the
    pattern selected is of type "table".

    The JSPx files will keept the structure of the MVC artifacts created by Roo::

     src/main/webapp/WEB-INF/views
     |-- /entity1
     |   |-- pattern_id1.jspx
     |   |-- update.jspx
     |   `-- ...
     |-- /entity2
     |   |-- pattern_id1.jspx
     |   |-- pattern_id2.jspx
     |   `-- ...

    ``@GvNIXRelationsPattern`` triggers the update of annotations in related entities adding or
    modifying their own ``@GvNIXPattern``.
    
    TODO: Use case of distinct annotation on related entities instead of ``@GvNIXPattern``.

* Metadata for Entity

  **That only has sense in table pattern case where the pattern requires to work with list of entities,
  that is, batch operations.**

  The metadata in entities defines the methods accepting list of entities as parameter and performs
  write operations againts DB (create, update, delete) inside the same transaction.

  The annotation defining these metadata will be **@GvNIXEntityBatch**.

Metadata Listeners
...................

The add-on will have the needed metadata listeners registered to GvNIXPattern's changes, so it will
know when a pattern definition has been modified or removed and act handling the changes.


JSPx files
-----------

The add-on will create it's own JSPx files for each defined pattern using the set of TAGx files
available in the framework.


Roo Shell commands
====================

Comandos asociados al Add-on.

Use cases
=============

Installation
-------------

References
==========

* http://pragmatikroo.blogspot.com/2011/04/springt-roo-standard-screen-patterns.html

* http://viralpatel.net/blogs/2011/01/spring-roo-implement-masterdetail-forms.html

* http://viralpatel.net/blogs/download/spring/springroo-masterdetail.zip

Future enhancements
====================


TODO
====

* El import de la clase BeanInfoUtils debe referenciar al add-on de classpath.

* Forzar altura de la capa contenedora de tabs para que no quede oculta la tabla por debajo.
  El tamaño no puede ser fijo, pq la tabla puede crecer más aún del tamaño máximo fijado.
  r148

* Respecto al punto anterior: en Sentencias r17 se ha corregido el problema de manera demasiado
  add-hoc. Al height de las capas se le suman 137px que es lo que ocupan las 5 nuevas filas.
  Revisar lo que se comenta en http://anaturb.net/dojo/my/dojoTabContainer.htm por si puede
  servir como mejor solución.

* Hay un problema con este add-on probocado por el método
  org.gvnix.web.screen.roo.addon.ScreenMetadataListener.getAnnotatedFields(String, String). Falla
  porque no está controllado el caso de que WebScaffoldMetada sea null.

