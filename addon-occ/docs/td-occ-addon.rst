===================================
 gvNIX OCC Add-on Technical Design
===================================

:Project:   gvNIX. Spring Roo based RAD tool
:Copyright: DGTI - Generalitat Valenciana
:Author:    DISID Corporation, S.L.
:Revision:  $Rev: 3202 $
:Date:      $Date: 2010-04-15 09:37:50 +0200 (jue 15 de abr de 2010) $

.. contents::
   :depth: 2
   :backlinks: none

.. |date| date::

Introduction
===============

This project generates the optimistic concurrency control (OCC) persistence Add-on of gvNIX framework.

This document contains the technical design of the OCC add-on.

Optimistic Concurrence Control based on state's Checksum
========================================================

This implementation computes the checksum of all field values that compounds the entity's state when it's loaded from DB.
Then, when application gets an remove or update actions this checksum is compared with stored in database entity's checksum.
If there is any difference an exception is raised.

This has a performance problem because this needs to perform an extra read.
But this is the best implementation we've found for solve this problem.

There is another problem with Roo's automatic integration test.
This generate a java compilation exception because it tries to compare version field using '>' operator.

``GvNIXEntityOCCChecksum`` annotation
=====================================

This is the annotation for this functionality.
You can add this manually to any ``@RooJpaActiveRecord`` class or use `occChecksum commands` described at user guide.
This annotations modify the entity in two ways:

#. Adds a property to store entity's state chechsum. Include getter/setter and some control annotations.

#. Generates a AspetcJ file that contains this solution logical implementation.
   This file uses the name ``*_Roo_gvNIX_occChecksum.aj``

Proof of Concept
================

* http://scmcit.gva.es/svn/gvnix-proof/trunk/petclinic-occ
* https://svn.disid.com/svn/disid/proof/gvnix/concurrency
* https://svn.disid.com/svn/disid/proof/gvnix/occ-checksum-app
* https://svn.disid.com/svn/disid/proof/spring_roo/gvnix_concurrency_test

TODO List
=========

AspectJ precedence vs. around
-----------------------------

There is a way to replace Itd definitions of other add-on (like build-in add-on) AspetJ ``declare precedence``:

	Given a potential conflict between inter-type member declarations in different aspects, if one aspect has precedence over the other its declaration will take effect without any conflict notice from compiler.
	This is true both when the precedence is declared explicitly with declare precedence as well as when when sub-aspects implicitly have precedence over their super-aspect.

We can use this declaration to override ``Roo_Jpa_ActiveRecord`` merge and remove methods instead to use ``around``.
We have to study what is the better way to do this.

Related links:

* http://www.eclipse.org/aspectj/doc/next/progguide/semantics-declare.html

* http://www.eclipse.org/aspectj/doc/next/adk15notebook/annotations-decp.html

* http://jira.springframework.org/browse/ROO-795


Set type fields OCC
-------------------

In a set field modification (adding or removing relation to current entity) has relation whit current OCC because change are made in set element. But in this case we have to assure that this changes are made in same items version.

We need to proof this checks is performed correctly. Instead we need to develop a solution.

Notes
-----

Se crea la plantilla de AspectJ debido a que hay que declarar la precedencia en el ITD, por eso no se crean los métodos mediante código java.

Investigar más adelante si ROO da la posibilidad de definir la precedencia mediante una lista.

TODO
-----

* We've noticed a compilation error when perform ``mvn clean assembly:assebly`` and the project has unit test over entitie defined. See http://projects.disid.com/issues/5157 for further details.
