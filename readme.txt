
=============================================
 gvNIX - RAD tool for Java developers
=============================================

Welcome to gvNIX, Spring Roo based RAD tool for Java developers.

TODO: This doc isn't updated

About this doc
===============

These instructions show you how to get started with gvNIX source tree. Note 
that these instructions are for developers looking to develop gvNIX itself.

If you like to try a release that has already been built, tested and 
distributed by the core development team, we recommend that you visit gvNIX 
download page http://www.gvpontis.gva.es/proyectos-integra/proy-desarrollo/gvnix/gvnix-documentacion/
and read the *gvNIX Reference Guide* documentation.

This document is written using the reStructuredText markup and UTF-8 charset encoding.

Copyright (C) 2010 Conselleria d'Infraestructures i Transport - Generalitat Valenciana

This work is licensed under the Creative Commons Attribution-Share Alike 3.0
Unported License. To view a copy of this license, visit 
http://creativecommons.org/licenses/by-sa/3.0/

Documentation index
===================

* On each gvNIX project module you can find technical and user guides at module/docs/ in reStructuredText format and English:

  * td-module.rst: Technical design
  * ug-module.rst: User guide

* gvNIX work guides are placed at src/site/docbook/ in docbook format and Spanish:

  * reference: Working guide about projects development with gvNIX.
  * developer: Working guide about gvNIX project development.

* gvNIX project methodology documentation can be found at ../doc in gvMetrica format and Spanish.

Pre-requisites
================

To start with gvNIX you need:

* A *nix machine (Windows users should be OK if they write a .bat)
* JDK 6.0 or above ( http://java.sun.com/javase/downloads/index.jsp )
* Maven 3.0.1 or above ( http://maven.apache.org/download.html )
* Internet access so that Maven can download required dependencies

Setup instructions
===================

Maven
-----

* Setup environment variable called MAVEN_OPTS::

    bash:~/gvnix/trunk/code$ export MAVEN_OPTS="-Xmx1024m -XX:MaxPermSize=512m"

  If you already have a MAVEN_OPTS, just check it has the memory sizes
  shown above (or greater).

  On a *nix machines, we recommed to add the setup to your ``.bashrc``::

   bash:~$ echo export MAVEN_OPTS=\"-Xmx1024m -XX:MaxPermSize=512m\" >> ~/.bashrc
   bash:~$ source ~/.bashrc

Run gvNIX dev
=================

If you have internet access through a proxy you have to enable in `Maven`_ configuration (Appendix -> Proxy configuration -> Maven).

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
* Use the previous editor to open ``src/site/docbook/reference/index.xml`` and contribute with your knowledge.

Package gvNIX
================

To pack a ready-to-install release use the following command from the gvNIX 
home directory::

  bash:~/gvnix/trunk/code$ mvn clean install site assembly:assembly

This will create the ZIP file ``target/gvNIX-{version}.zip``. To install it read the *gvNIX Reference Guide*.

Need more info?
===============

For more information generate and read the *gvNIX Reference Guide* (spanish).

# Run the following command from the root checkout location::

   bash:~/gvnix/trunk/code$ mvn site

# This will create the guide in the "target/site/reference" directory (in several formats)::

    target
    `-- site
        |-- developer
        |   |-- html
        |   |   |-- index.html
        |   |   `-- ...
        |   |-- html-single
        |   |   `-- index.html
        |   `-- pdf
        |       `-- gvNIX-referencia.pdf
        `-- reference
            |-- html
            |   |-- index.html
            |   `-- ...
            |-- html-single
            |   `-- index.html
            `-- pdf
                `-- gvNIX-referencia.pdf

TODO
====

* https://jira.springsource.org/browse/ROO-2097

* Roo refactor at web layer commands:

   web xyz install
   web xyz scaffold --class
   web xyz all --package

  https://jira.springsource.org/browse/ROO-2297
