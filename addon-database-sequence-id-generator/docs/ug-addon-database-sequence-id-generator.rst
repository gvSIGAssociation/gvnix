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

Requirements
=============

Have a project with database connection.

Installation
============


Install the Add-on.
--------------------

Install gvNIX or the Add-on in Roo shell.

Create web application
-----------------------

Create a web application with database connection (previous existing database) using gvNix/Roo Shell script::

  // Spring Roo 1.1.0.M1 log opened at 2010-05-10 11:08:03
  project --topLevelPackage org.gvnix.test.generated.id.sequence--projectName generatedIdSequence
  jpa setup --provider HIBERNATE --database POSTGRES --databaseName generated-id-test
  jpa setup --provider HIBERNATE --database POSTGRES --databaseName generated-id-test --userName user --password user
  entity jpa --class ~.domain.Person --testAutomatically 
  field string --fieldName name --class org.gvnix.test.generated.id.sequence.domain.Person
  entity jpa --class ~.domain.RoundTable
  field string --fieldName name --class org.gvnix.test.generated.id.sequence.domain.RoundTable
  web mvc setup
  web mvc all --package org.gvnix.test.generated.id.sequence.web

Use the command to add a sequence id to Person Entity::

  sequence generation add --entity org.gvnix.test.generated.id.sequence.domain.Person
