===================================================================
 gvNIX Persistence Add-on
===================================================================

:Company:   DiSiD Technologies, S.L.
:Revision:  $Rev: 2956 $
:Date:      $Date: 2010-01-19 09:08:12 +0100 (mar 19 de ene de 2010) $
:Copyright: Esta obra está bajo la licencia `Reconocimiento-Compartir bajo la misma licencia 3.0 España <http://creativecommons.org/licenses/by-sa/3.0/es/>`_ de Creative Commons. Puede copiarla, distribuirla y comunicarla públicamente siempre que especifique sus autores y comparta cualquier obra derivada bajo la misma licencia. La licencia completa se puede consultar en http://creativecommons.org/licenses/by-sa/3.0/es/

.. contents::
   :depth: 2
   :backlinks: none

.. |date| date::

Introduction
===============

This project generates the Persistence Add-on of gvNIX framework.

This add-on envelope some features relative to entity's persistence.

Features
===========

This are features contained:

* Optimistic Concurrence Control based on state's Checksum: This allow to use OCC on entity haven't any sort of version field an we can't modify it.


Optimistic Concurrence Control based on state's Checksum
=========================================================

This allow to use OCC on entity haven't any sort of version field an we can't modify it. Normally it's when we have to use a legacy DataBase shared between applications.

This implementation computes the checksum of all field values that compounds the entity's state when it's loaded from DB. Then, when application gets an remove or update actions this checksum is compared with stored in database entity's checksum. If there is any difference an exception is raised.

This has a performance problem because this needs to perform an extra read. But this is the best implementation we've found for solve this problem.

There is another problem with Roo's automatic integration test. This generate a java compilation exception because it tries to compare version field using '>' operator.

``GvNIXEntityOCCChecksum`` annotation
---------------------------------------

This is annotation for this functionality.  You can add this manually to any ``@RooEntity`` class or use `occChecksum commands`_. This annotations modify the entity in two ways:

#. Adds a property to store entity's state chechsum. Include getter/setter and some control annotations.

#. Generates a AspetcJ file that contains this solution logical implementation. This file uses the name ``*_Roo_gvNIX_occChecksum.aj``

``occChecksum`` commands
---------------------------

Two new commands are installed into Roo shell:

#. ``occChecksum set``: Apply this occ implementation to a existing ``@RooEntity``.

#. ``occChecksum all``: Apply this occ implementation to all existing ``@RooEntity``. If any of them has already been configure no changes will be applied to this entity.

See shell command help for more information
