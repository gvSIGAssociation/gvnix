=========================================================
 gvNIX add-on CIT security
=========================================================

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

This add-on must install Gv CIT Authentication Web Service client and the user model needed to used it with Spring Security library.


Analysis
=========

We can split this sections in four parts:

* `Install web service client`_
* `User model`_
* `Install Spring Security Authentication Provider`_
* `Configuration files`_

Install web service client
----------------------------

Gv CIT has a Web service for authenticate user. The return values of this service includes information about user and its permission's list. This service required an application name and a service password. This data must be configured in each application that uses the service.

The classes needed to perform service call will be copied to target application on setup. Apart of it, a property file will be created to configure the application name and service password for make service's call.

The artifact installed in this step will be:

* ``es/gva/cit/WS_CIT_Credencial/xsd/**`` : XSD definiton used by service client.
* ``ServerWSAuth/*``: Service client

We use 'ServerWSAuth' package for service client to make easier the WS client regeneration if it's needed.

User model
----------------------------

To use Spring Security library we need a User model that extends the Spring Security model. This model must contain all data returned by Web Service.

The model is compound by this classes:

* ``org.gvnix.security.authentication.wscit.WscitUser``: User model class.
* ``org.gvnix.security.authentication.wscit.WscitUserAuthority``: UserAuthority model class.

Install Spring Security Authentication Provider
-------------------------------------------------

This Bean will be the join point between Spring Security and the Authentication Web Service. Spring Security will use it to retrieve the used and validate inserted user password. As it use Spring Security the add-on will install this dependency in project if it's not done before.

The model is compound by the class ``org.gvnix.security.authentication.wscit.WscitAuthenticationProvider``.


Configuration files
---------------------------

The Spring Security configuration must be changed to use the new Authentication Provider.

This configuration is compound by this files:

* ``src/main/resources/META-INF/spring/applicationContext-security.xml``: Spring Security configuration and Bean declaration.
* ``src/main/resources/META-INF/spring/CITWSAuth.properties``: CIT Web Service parameters.

In applicationContext we can find URL authorization configuration and other configuration as Salt type and Password for the Authentication Provider.

Roo Shell commands
====================

Command list to implement in this add-on:


cit security setup
--------------------------

Install Gv CIT authentication.

Parameters:

* ``--url``: CIT WS url

* ``--appName``: Name of application for CIT WS.

* ``--password``: CIT WS password.


Examples::

    cit security setup --url [URL] --appName [APP_NAME] --login [LOGIN] --password [PASSWORD]

Dynamic configuration
=====================

See technical design documentation at addon-dynamic-configuration.

TODO
====

* Review the packages of the WS client and Authentication artifacts

