===================================================
 gvNIX add-on Persistence add-on TODO list
=====================================================

:Project:   gvNIX. Spring Roo based RAD tool
:Copyright: Conselleria d'Infraestructures i Transport - Generalitat Valenciana
:Author:    DiSiD Technologies, S.L.
:Revision:  $Rev: 3202 $
:Date:      $Date: 2010-04-15 09:37:50 +0200 (jue 15 de abr de 2010) $

.. contents::
   :depth: 2
   :backlinks: none

.. |date| date::

Introduction
===============

This document is used to have a list of things to review or to do.


OCC Checksum
=======================


AspectJ precedence vs. around
------------------------------------

There is a way to replace Itd definitions of other add-on (like build-in add-on) AspetJ ``declare precedence``:

	Given a potential conflict between inter-type member declarations in different aspects, if one aspect has precedence over the other its declaration will take effect without any conflict notice from compiler. This is true both when the precedence is declared explicitly with declare precedence as well as when when sub-aspects implicitly have precedence over their super-aspect.

We can use this declaration to override ``Roo_Entity`` merge and remove methods instead to use ``around``. We have to study what is the better way to do this.

Related links:

* http://www.eclipse.org/aspectj/doc/next/progguide/semantics-declare.html

* http://www.eclipse.org/aspectj/doc/next/adk15notebook/annotations-decp.html

* http://jira.springframework.org/browse/ROO-795


Set type fields OCC
-----------------------

In a set field modification (adding or removing relation to current entity) has relation whit current OCC because change are made in set element. But in this case we have to assure that this changes are made in same items version.

We need to proof this checks is performed correctly. Instead we need to develop a solution.

Notes
-------

Se crea la plantilla de AspectJ debido a que hay que declarar la precedencia en el ITD, por eso no se crean los métodos mediante código java.

Investigar más adelante si ROO da la posibilidad de definir la precedencia mediante una lista.
