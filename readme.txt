=============================================
 gvNIX - Spring Roo based RAD tool
=============================================

Welcome to gvNIX.

In this you will find intructions to start develop whit this tool.

Pre-requisites
================

JDK 1.5
--------

You must have installed Java Development Kit 1.5 or higher.

Maven 2.x
------------

You must have installed Maven 2.x in your system

Internet connection
--------------------

To download dependencies and sorces.

Git
--------

You must have installed Git client.

Spring Roo 1.1.0-M1 sources
-----------------------------

You must compile Spring Roo 1.1.0-M1 in your system. To do that you can follow this steps:

1. Creare a folder for checkout Spring Roo sources.

2. Open a shell and go into this folder.

3. Checkout Spring Roo sources with this instruction:

  git clone git://git.springsource.org/roo/roo.git

4. Switch to las 1.1.0M1 version

  git checkout 3a0b8a399aae14167139c185e4e31355e20d1f25

5. Compile and Install Spring Roo projects: follow steps in readme.txt

Compile gvNIX
=================

For compile gvNIX you have to use maven command:

  mvn clean install

Installl gvNIX add-ons into roo-dev
=======================================

1. run roo-dev.

2. install OSGi bundle.

   osgi install --url file:path-to-add-on-jar
   
   The output will be osgi component ID.

3. start bundel.

   felix shell start ID

Uninstall gvNIX add-on from roo-dev
============================================

1. run roo-dev

2. localte OSGi bundle add-on's name.

   osgi ps

3. uninstalll OSGi bundle.

   osgi uninstall --bundleSymbolicName org.gnix.NAME-OF-BUNDLE

Reinstall gvNIX add-on in roo-dev
============================================

1. Compile and install the add-on

1. run roo-dev

2. localte OSGi bundle add-on's name.

   osgi ps

3. update OSGi bundle.

   osgi update --bundleSymbolicName org.gnix.NAME-OF-BUNDLE

Generate reference document
==============================

From this folder execute:

  mvn site

That generates this files:

* target/site/reference/html/index.html: Reference document in html

* target/site/reference/pdf/gvNIX-referencia.pdf: Reference document in pdf

* target/site/reference/html-single/index.html: Reference document in a single html file


Generate gvNIX package
=============================

From this folder execute:

  mvn package site assembly:assembly

This generate a file:

  target/gvNIX-{version}.zip
