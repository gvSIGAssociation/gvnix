======================================
 gvNIX Database generator sequence Id
======================================

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
=============

This Add-on changed the default value of the database sequence assigned to Entity identifier (``@id`` annotated attribute).

Requirements
=============

Creates a new sequence definition to assign to an Entity.

Use Case
=========

Add sequence
-------------

Execute the command to create a new sequence to the selected Entity.
Entity must exist.

Add all sequence
-----------------

Execute the command to create a new sequence for every Entity in the project.

Analysis
=========

Analysis for the development of the Add-on displayed by commands.

Add sequence
-------------

Generates a sequence associated to selected entity.

Creates an AspectJ file with the defined sequence values that overrides the definition of the selected entity sequence values.

Add sequence to all
--------------------

Generates unique sequence for each entity.

Creates an unique AspectJ for each entity with the defined sequence values that overrides the definition of each entity sequence values.

Commands
=========

There are defined two commands in this Add-on:

sequence generation add
------------------------

Creates new sequence identifier to selected Entity.

Parameters:

  * ``--entity`` (mandatory): Selected entity.
  * ``--increment`` (optional): Increment between ids.
  * ``--allocatedSize`` (optional): Allocated space for Entity's identifiers.

sequence generation all
------------------------

Creates new sequence identifier for each entity.

Parameters:

  * ``--increment`` (optional): Increment between ids.

Application versions
=====================

* gvNIX-0.3 version: The Add-on creates a sequence id associated to Entity and for all Entities.
* future versions: Add incremente and allocatedSize functionality to the Add-on, it doesn't work with the annotations.

