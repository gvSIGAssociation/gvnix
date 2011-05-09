==================================
 gvNIX Web report add-on
==================================

:Project:   gvNIX. Spring Roo based RAD tool
:Copyright: Conselleria d'Infraestructures i Transport - Generalitat Valenciana
:Author:    DiSiD Technologies, S.L.
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
===============

Add-on giving some commands for dynamic project configurations.


Project contents
=================

This folder contains add-on sources and documents folder ``docs`` with documentation of this project.


Installation Proof
===================

Install the Add-on.
--------------------

Install the Add-on in gvNIX/Roo shell with the required commands for 1.1.3-RELEASE version.

Create a new gvNIX project
-------------------------

Create a web application with gvNIX/Roo Shell script::

    project --topLevelPackage org.gvnix.test.dynamicconfig
    persistence setup --database POSTGRES --provider HIBERNATE
    configuration save --name development
    configuration save --name preproduction
    configuration property add --name database.password
    configuration property add --name database.username
    configuration property add --name database.url
    configuration property update --configuration development --property database.password  --value 1234
    configuration property update --configuration development --property database.username --value user1
    configuration property update --configuration development --property database.url  \
      --value jdbc\:postgresql\://localhost\:5432/my_db
    configuration property update --configuration preproduction --property database.password  --value pre1234
    configuration property update --configuration preproduction --property database.username --value preuser
    configuration property update --configuration preproduction --property database.url  \
      --value jdbc\:postgresql\://prehost\:5432/my_predb
    configuration activate --name development

With ``configuration activate --name development`` command ``src/main/resources/META-INF/spring/database.properties``
file has been updated with the values of the properties you set before.
