
=============================================
 gvNIX - RAD tool for Java developers
=============================================

Welcome to gvNIX, Spring Roo based RAD tool for Java developers.

About this doc
===============

These instructions show you how to get started with gvNIX source tree. Note 
that these instructions are for developers looking to develop gvNIX itself.

If you like to try a release that has already been built, tested and 
distributed by the core development team, we recommend that you visit gvNIX 
download page http://www.gvpontis.gva.es/proyectos-integra/proy-desarrollo and
read the *gvNIX Reference Guide* included in package.

This document is written using the reStructuredText markup and UTF-8 charset encoding.

Copyright (C) 2010 Conselleria de Infraestructuras y Transporte - Generalitat Valenciana

This work is licensed under the Creative Commons Attribution-Share Alike 3.0
Unported License. To view a copy of this license, visit 
http://creativecommons.org/licenses/by-sa/3.0/ or send a letter to 
Creative Commons, 171 Second Street, Suite 300, San Francisco, California,
94105, USA.

Pre-requisites
================

To start with gvNIX you need:

* A *nix machine (Windows users should be OK if they write a .bat)
* JDK 5.0 or above ( http://java.sun.com/javase/downloads/index.jsp )
* Maven 2.0.9 or above ( http://maven.apache.org/download.html )
* Internet access so that Maven can download required dependencies
* Git client, http://en.wikipedia.org/wiki/Git_%28software%29
* Checkout Spring Roo 1.1.0.M1 to gvNIX folder:

  * Use your Git client to checkout Spring Roo sources from ``git://git.springsource.org/roo/roo.git``. Example::

     bash:~/gvnix/trunk/code$ git clone git://git.springsource.org/roo/roo.git

  * Change to Spring Roo folder and switch to 1.1.0.M1 version ``3a0b8a399aae14167139c185e4e31355e20d1f25``. Example::

     bash:~/gvnix/trunk/code$ cd roo
     bash:~/gvnix/trunk/code/roo$ git checkout 3a0b8a399aae14167139c185e4e31355e20d1f25

Run gvNIX dev
=================

#. Build gvNIX::

    bash:~/gvnix/trunk/code$ mvn clean install

#. Get started with gvNIX

   * Add gvNIX ``bin`` directory to PATH::

      bash:~/gvnix/trunk/code$ PATH=$PWD/bin:$PATH

   * Change to Java project directory::

      bash:~/gvnix/trunk/code$ cd ~/project-directory

   * Execute gvNIX shell::

      bash:~/project-directory$ gvnix-dev

Write doc
==========

* Download and install XMLmind XML Editor Personal Edition ( http://www.xmlmind.com/xmleditor/download.shtml )
* Use your preferred DocBook editor (i.e. XMLmind) to open ``src/site/docbook/reference/index.xml`` and contribute your knowledge

Package gvNIX
================

To pack a ready-to-install release use the following command from the gvNIX 
home directory::

  bash:~/gvnix/trunk/code$ mvn clean install site assembly:assembly

This will create the ZIP file ``target/gvNIX-{version}.zip``. To install it read the *gvNIX Reference Guide*.

Need more info?
================

For more information generate and read the *gvNIX Reference Guide* (spanish).

# Run the following command from the root checkout location::

   bash:~/gvnix/trunk/code$ mvn site

# This will create the guide in the "target/site/reference" directory (in several formats)::

    target
    `-- site
        `-- reference
            |-- html
            |   |-- index.html
            |   `-- ...
            |-- html-single
            |   `-- index.html
            `-- pdf
                `-- gvNIX-referencia.pdf

This reference guide will help you to get started. Useful sections are:

* Instalación de gvNIX
* Primeros pasos con gvNIX
* Desarrollo de aplicaciones con gvNIX

