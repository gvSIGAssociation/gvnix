==================================
 gvNIX Web screen patterns add-on
==================================

:Project:   gvNIX. Spring Roo based RAD tool
:Copyright: Conselleria d'Infraestructures i Transport - Generalitat Valenciana
:Author:    DiSiD Technologies, S.L.
:Revision:  $Rev: 1010 $
:Date:      $Date: 2011-06-13 16:54:27 +0200 (lun 13 de jun de 2011) $

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

TBC: How form fields will be layout, how to change form field layouts, applied to relation chosen by developer, ....

Tech design
============

#. Create the needed metadata to handle the defined pattern. That is, for a given Entity how this must be shown in CRUD operations, and how its related entities (@OneToMany annotated fields) must be shown in the same context.

#. JSPx, TAGx to work with an entity and its related entities. The layout must follow the defined pattern (master register, mater tabular, detail register, detail tabular).

Command
---------

web mvc pattern install
```````````````````````

::

  web mvc pattern install
  
This method adds tagx, iamges, css, javascript, jspx and modify i18 property files.

This doesn't need any parameters.

Copy tagx files from ``src/main/resources/org/gvnix/web/screen/roo/addon`` addon folder into ``src/main/webapp/WEB-INF/`` project folder:

* tags/pattern::

	├── cancelbutton.tagx
	├── contentpane.tagx
	├── form
	│   ├── fields
	│   │   ├── checkbox.tagx
	│   │   ├── contentpane.tagx
	│   │   ├── datetime.tagx
	│   │   ├── input.tagx
	│   │   ├── selectRelated.tagx
	│   │   ├── select.tagx
	│   │   ├── select-withempty.tagx
	│   │   ├── simple.tagx
	│   │   └── textarea.tagx
	│   ├── show.tagx
	│   └── update.tagx
	├── hiddengvnixpattern.tagx
	├── paginationadd.tagx
	├── paginationview.tagx
	├── relations.tagx
	├── relation.tagx
	└── tabcontainer.tagx

* tags/util::

	├── changes-control.tagx
	├── panel-tabs.tagx
	├── panel-tab.tagx
	├── quicklinks.tagx
	├── quicklink.tagx
	└── slider.tagx

* tags/dialog::

	└── message
	    └── message-box.tagx

web mvc pattern master
``````````````````````

This command creates AspectJ files and creates the jspx view pattern of a entity.

::

  web mvc pattern master --class ~.web.PetController --type table --name MyPattern

Options:

* class: The path and name of the controller object to be used. *Required*. Extends: JavaType

  This option must be the name or path of a JavaType annotated with RooWebScaffold in order to allow Metadata generation.

* type: The pattern type to include on the controller object. *Required*

  Enum type in WebPattern will define the available pattern types. Nowadays: tabular and register.

* name: Unique name for the pattern in whole project. *Required*. Type: JavaSymbolName, it avoids non valid characters.

The command has to add the GvNIXPattern annotation in the class requested by the option "class" and with the following value::

  @GvNIXPattern({"MyPattern=tabular"})

In the same operation, if requested type is "tabular", the Entity exposed by the given class has to be annotated with
GvNIXEntityBatch also.

web mvc pattern detail
``````````````````````

This command creates AspectJ files and creates the jspx view pattern of a entity relation.

::

  web mvc pattern detail --class ~.web.PetController --name MyPattern --field field1 --type table

Options:

* class: The path and name of the controller object to be used. *Required*. Extends: JavaType

  Desired class must be annotated with GvNIXPattern before apply the new annotation.

* name: name for the pattern in whole project to set relation pattern to. *Required*. Type: JavaSymbolName, it avoids non valid characters.

  The name of the pattern must be already defined as value of GvNIXPattern.

* field: One-to-many property name to apply pattern to. *Required*

  The name of the field we want to show as relationship of the master entity (the formBackingObject of controller). By now,
  this field must be annotated with OneToMany.

* type: The pattern type to apply pattern to. *Required*

  Enum type in WebPattern will define the available pattern types. Nowadays: tabular and register.

The command has to add the GvNIXRelationsPattern in the class requested by the option "class" and with the following value::

  @GvNIXRelationsPattern({"MyPattern: field1=table"})``

If there is any previous GvNIXRelationsPattern defined, the field specified in the command will be added as comma separated value::

  @GvNIXRelationsPattern({"MyPattern: otherField=register, field1=tabular", "OtherPattern: otherField=tabular"})``

Since the command applies a pattern definition over a new Entity (denoted by the field in the exposed Entity), the command has to
annotate this new Entity with GvNIXRelatedPattern. This annotation takes the same value defined in GvNIXRelationsPattern for the
given field and the given pattern identifier.

As we described before for GvNIXPattern, if requested type is "tabular", the entity exposed by the field has to be annotated
with GvNIXEntityBatch also.

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
      e.g: ``@GvNIXPattern({"pattern_id1=register", "pattern_id2=tabular"})``. (the format may change)

    * **@GvNIXRelationsPattern**: Defines patterns over the entities related with formBackingObject
      entity.
      e.g: ``@GvNIXRelationsPattern({"pattern_id1: field1=table, field2=tabular",
      "pattern_id2: field2=tabular"})`` (the format may change)

    ``@GvNIXPattern`` annotation triggers the generation of the AspectJ files with the methods in the
    controller accepting request of operations over the entity. Also, they trigger the generation of
    the MVC artifacts (JSPx files) rendering views of the formBackingObject.

    Both, ``@GvNIXPattern`` and ``@GvNIXRelationsPattern``, trigger the modification of the JAVA files
    defining the related entities adding the annotation ``GvNIXRelatedPattern`` in the case of
    ``GvNIXRelationsPattern`` and ``GvNIXEntityBatch`` (described below) when the pattern selected is
    of type "tabular".

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
    modifying their own ``@GvNIXRelatedPattern``.

    TODO: Use case of distinct annotation on related entities instead of ``@GvNIXPattern``.

* Metadata for Entity

  **That only has sense in table pattern case where the pattern requires to work with list of entities,
  that is, batch operations.**

  The metadata in entities defines the methods accepting list of entities as parameter and performs
  write operations againts DB (create, update, delete) inside the same transaction.

  The annotation defining these metadata will be **@GvNIXEntityBatch**.

Metadata Listeners
...................

The add-on will have the needed metadata listeners registered to GvNIXPattern and GvNIXRelatedPattern changes, so it will
know when a pattern definition has been modified or removed and act handling the changes.


JSPx files
-----------

The add-on will create it's own JSPx files for each defined pattern using the set of TAGx files
available in the framework.

For "tabular" type, created JSPx will be based on Roo's show.jspx but using gvNIX version of show.tagx. In the other hand,
"register" JSPx are based on update.jspx, using gvNIX version of update.tagx.


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

Proof of concept
================

* Tabular pattern:

 http://scmcit.gva.es/svn/gvnix-proof/trunk/petclinic-table
 http://scmcit.gva.es/svn/gvnix-proof/trunk/petclinic-screen-pattern-table

* Register pattern:

 http://scmcit.gva.es/svn/gvnix-proof/trunk/petclinic-register
 http://scmcit.gva.es/svn/gvnix-proof/trunk/petclinic-screen-pattern-register

* https://svn.disid.com/svn/disid/proof/gvnix/display-relation-table
* https://svn.disid.com/svn/disid/proof/gvnix/relationship-table-app
* https://svn.disid.com/svn/disid/proof/gvnix/display-tag
* https://svn.disid.com/svn/disid/proof/spring_roo/gvnix-display-relation-table
* https://svn.disid.com/svn/disid/proof/spring_roo/gvnix-display-tag

TODO
====

* Forzar altura de la capa contenedora de tabs para que no quede oculta la tabla por debajo.
  El tamaño no puede ser fijo, pq la tabla puede crecer más aún del tamaño máximo fijado.
  r148

* Respecto al punto anterior: en Sentencias r17 se ha corregido el problema de manera demasiado
  add-hoc. Al height de las capas se le suman 137px que es lo que ocupan las 5 nuevas filas.
  Revisar lo que se comenta en http://anaturb.net/dojo/my/dojoTabContainer.htm por si puede
  servir como mejor solución.

* Rename 'screen' with 'pattern'.

* Los patrones de las entidades relacionadas actualmente funcionan para @OneToMany y deberían
  funcionar también para los tipos @ManyToMany sin cambios y para ManyToOne con algún pequeño cambio.

* Ampliar la ejecución del comando de "pattern relation" para que se pueda ejecutar recursivamente.

* La generación del ITD debe considerar los atributos mergeMethod, persistMethod y removeMethod de la
  anotación @Entity ya que si se especifica un nombre distinto de método deberá utilizarse dicho nombre
  en la invocación que se hace desde este ITD.

* Escribir en el Java de la entidad la anotación GvNIXEntityBatch desde el Metadata de GvNIXPattern y GvNIXRelatedPattern,
  en lugar de hacerlo desde el command.

* Se desactiva el soporte de los patrones Maestro tabular - Detalle * y Maestro registro - Detalle registro. En
  WebScreenOperationsImpl.addRelationPattern(JavaType, JavaSymbolName, JavaSymbolName, WebPattern) se comprueba si
  se están definiendo estos patrones y se aborta la ejecución en caso afirmativo.

* ¿ Hay un problema con este add-on probocado por el método
  org.gvnix.web.screen.roo.addon.ScreenMetadataListener.getAnnotatedFields(String, String). Falla
  porque no está controllado el caso de que WebScaffoldMetada sea null. ?
  
* TODO Set ``render`` attribute in OneToMany relationships to false.
  