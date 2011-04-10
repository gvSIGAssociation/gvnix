=============================
 gvNIX OCC Add-on User Guide
=============================

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

This add-on envelope some features relative to entity's persistence.

Feature
=======

Optimistic Concurrence Control based on state's Checksum:

 This allow to use OCC on entity haven't any sort of version field an we can't modify it.

Description
===========

This allow to use OCC on entity haven't any sort of version field an we can't modify it.
Normally it's when we have to use a legacy DataBase shared between applications.

``occChecksum`` commands
---------------------------

Two new commands are installed into Roo shell:

#. ``occChecksum set``: Apply this occ implementation to a existing ``@RooEntity``.
   Add the gvNIX Optimistic Concurrecy Control Checksum based behaivor to a Entity.

   Command options:
   
   * entity (optional): The name of the entity object to add OCC
   * fieldName (optional): The name of the field to use to store de checksum value
   * digestMethod (optional): The name of the type of digest method to compute the checksum

#. ``occChecksum all``: Apply this occ implementation to all existing ``@RooEntity``.
   Add the gvNIX Optimistic Concurrency Control Checksum based behaivor to all entities in project.
   If any of them has already been configure no changes will be applied to this entity.

   Command options:

   * fieldName (optional): The name of the field to use to store de checksum value
   * digestMethod (optional): The name of the type of digest method to compute the checksum

See shell command help for more information
