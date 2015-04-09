==================================
 gvNIX Web Binding add-on
==================================

-----------
User Guide
-----------

:Project:   gvNIX. Spring Roo based RAD tool
:Copyright: 2010 (C) Dirección General de Tecnologías de la Información - Conselleria d'Hisenda i Administració Pública
:Author:    DISID Corporation, S.L.
:Revision:  $Rev: 1177 $
:Date:      $Date: 2011-09-20 11:06:13 +0200 (mar, 20 sep 2011) $

.. contents::
   :depth: 2
   :backlinks: none

This work is licensed under the Creative Commons Attribution-Share Alike 3.0
Unported License. To view a copy of this license, visit
http://creativecommons.org/licenses/by-sa/3.0/ or send a letter to
Creative Commons, 171 Second Street, Suite 300, San Francisco, California,
94105, USA.

Introduction
===============

Setup Spring MVC default editors to Roo-based project.

Requirements
--------------

* JDK 1.6+
* Maven 2+
* gvNIX - Spring Roo - Addon - Web Binding
* Eclipse Helios 3.6.0 (optional)

Install Addon Services Management
------------------------------------

Options:

* To install gvNIX 0.8.0 that contains the add-on installed.
* OR, to install in Spring Roo 1.1.5

  - Trust on PGP KEY ID used to sign the add-on. Note that gvNIX project members have their PGP KEYs IDs published at https://code.google.com/p/gvnix/people/list::

      pgp trust --keyId 0xC5FC814B

  - Run the command below::

      addon install bundle --bundleSymbolicName org.gvnix.web.binding.roo.addon

Use case: PropertyEditors Binding in Pet clinic
================================================

Create a new directory for the project::

  Bash shell:

    mkdir clinic
    cd clinic

Start gvNIX::

  clinic$ gvnix.sh
      ____  ____  ____
     / __ \/ __ \/ __ \
    / /_/ / / / / / / /
   / _, _/ /_/ / /_/ /   1.1.5.RELEASE [rev xxxxxx]
  /_/ |_|\____/\____/   gvNIX distribution 0.8.0


  Welcome to Spring Roo. For assistance press TAB or type "hint" then hit ENTER.
  roo-gvNIX>

Create the project::

  roo-gvNIX> script clinic.roo

Add a new report::

  roo-gvNIX> web binding stringTrimmer


About the command
-------------------

The command performs the following operations:

* Annotates all the classes annotated with @Controller with @GvNIXStringTrimmerBinder(emptyAsNull=true).
* Generation of an ITD with the method initStringTrimmerBinder annotated with @InitBinder.
  This method registers StringTrimmerEditor.

The command accepts the parameters below:

* *class*: Is the class of the Controller we want to register the StringTrimmerEditor.
* *emptyAsNull*: Based on this option the StringTrimmerEditor will convert empty Strings to null (emptyAsNull=true)
  or not.
