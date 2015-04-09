==================================
 gvNIX Web Binding add-on
==================================


-----------------
Technical Design
-----------------

:Project:   gvNIX. Spring Roo based RAD tool
:Copyright: DGTI - Generalitat Valenciana
:Author:    DISID Corporation, S.L.
:Revision:  $Rev: 1177 $
:Date:      $Date: 2011-09-20 11:06:13 +0200 (mar, 20 sep 2011) $

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

Requirements
=============

This add-on has been reimplemented due to an issue appeared in Spring 3.0.1GA
(https://jira.springsource.org/browse/SPR-7077). Looks like old way to register property editors
globally doesn't work. Right now the solution is to use methods annotated with @InitBinder in order
to register custom editors.

In the first release of this revisited add-on, it have to register StringTrimmerEditor for all the
Controllers, or one provided.

When the command ``web mvc binding stringTrimmer`` is issued, the add-on will annotate all the classes
annotated with @Controller with @GvNIXStringTrimmerBinder(emptyAsNull=true). This triggers the generation
of an ITD with the method initStringTrimmerBinder annotated with @InitBinder. This method registers
StringTrimmerEditor

Operations
===========

web mvc binding stringTrimmer [--class path_to_controller] [--emptyAsNull true|false]
-----

Performs the needed operations for match the requirements described above.

Proof of concept
================

* http://scmcit.gva.es/svn/gvnix-proof/trunk/petclinic-binding

TODO
====

In future versions of this add-on it would support, at least, Spring Editors listed in
http://static.springsource.org/spring/docs/3.0.x/spring-framework-reference/html/validation.html#beans-beans-conversion
Table 5.2
