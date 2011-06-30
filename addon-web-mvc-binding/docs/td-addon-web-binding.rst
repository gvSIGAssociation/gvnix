==================================
 gvNIX Web Binding add-on
==================================


-----------------
Technical Design
-----------------

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

Requirements
=============

In the first release of this add-on, it have to setup configuration for Spring MVC default editors.

When the command ``web binding setup`` is issued, the add-on will create a new class implementing
``WeBinidingInitializer`` interface and will modify ``webmvc-config.xml`` registering the bean of the
new class.

Operations
===========

setup
-----

Performs the needed operations for match the requirements described above.

drop
-------

Remove the bean registration from ``webmvc-config.xml``.

Proof of concept
================

* http://scmcit.gva.es/svn/gvnix-proof/trunk/petclinic-binding

TODO
====

Catch the file RENAMED event on file change and modify the bean registration in ``webmvc-config.xml``. Now when
file rename operation (mv file new_file) is performed two events are raised: CREATED and DELETED.
