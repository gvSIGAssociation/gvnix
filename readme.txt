
======================================
 gvNIX - RAD tool for Java developers
======================================

Welcome to gvNIX, Spring Roo based RAD tool for Java developers.

About this doc
==============

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

Pre-requisites
==============

To start with gvNIX you need:

* A nix machine (Windows users should be OK if they write a .bat)
* JDK 6.0 or above ( http://java.sun.com/javase/downloads/index.jsp )
* Maven 3.0.1 or above ( http://maven.apache.org/download.html )
* Internet access so that Maven can download required dependencies

Setup instructions
==================

Roo
---

If you have any questions about MAVEN, GIT, GPG or ECLIPSE setup see the following document::

 roo/readme.txt

Git
---

Be aware that Roo source code is commited on gvNIX project at ``roo`` folder.
However, Roo source code can be managed with Git commands like:

* Update:        git pull 
* List tags:     git tag -l
* Change to tag: git checkout {version}

Run gvNIX dev
=============

#. Build Roo::

    bash:~/gvnix/trunk/code/roo$ mvn clean install
    
   Roo is only necessary to be installed the first time.
   Only reinstall it if Roo source code change.

#. Build gvNIX::

    bash:~/gvnix/trunk/code$ mvn clean install
    
   From now, you will need to reinstall only each modified module instead of reinstall all gvNIX again::
   
    bash:~/gvnix/trunk/code/module$ mvn clean install

#. Add gvNIX ``bin`` directory to PATH::

    bash:~/gvnix/trunk/code$ PATH=$PWD/bin:$PATH
    
   It is recommended that you add this information to your .bashrc script.

#. Execute gvNIX shell in your java project::

    bash:~/project$ gvnix-dev
      
   Or execute gvNIX shell on debug mode in your java project::
     
    bash:~/project$ gvnix-dev-debug

.. admonition:: Important

   After each change and gvNIX compilation you *must* delete the contents of the *cache*, for it to clear the OSGi container located in *roo/bootstrap/target/osgi*.
   It is necessary for the container load new OSGi bundles and bundles not in cache::
   
    rm -rf roo/bootstrap/target/osgi

Package gvNIX
=============

* Create the tag for the version we want to build using the following command::

   bash:~/gvnix/trunk/code$ svn update
   bash:~/gvnix/trunk/code$ mvn release:prepare -Dtag={version}
   
  Version formats:
  
   * Snapshot: X.Y.Z-SNAPSHOT
   * Release: X.Y.Z

* If all ok, clean packaging information on the trunk code directory::

   bash:~/gvnix/trunk/code$ mvn release:clean
   
  If some errors, revert the changes::
  
   bash:~/gvnix/trunk/code$ mvn release:rollback

* To package the binary release use the following commands::

   bash:~/gvnix/tags/$ svn update
   bash:~/gvnix/tags/{version}$ ./build.sh -d

  The ``-d`` option deploy to google code, can be used only by commiters.
  Check if all add-ons are published correctly at http://gvnix.googlecode.com/svn/repo/repository.xml for RooBoot.
  This will create the ZIP file ``target/gvnix-dist/gvNIX-{version}.zip``.

* Send to publish on gvPONTIS the gvNIX binary zip, the gvNIX source zip and the html single page reference guide.

* Update (or create if not exists) the wiki page for each add-on from add-on user guide on html format.  

Source code
-----------

* Optional, package the source code release use the following command::

   bash:/tmp$ svn export http://webdav.cop.gva.es/svn/gvnix/tags/{version}

  ZIP created folder with name ``gvNIX-{version}-src.zip``::
  
   bash:/tmp$ zip -r -9 gvNIX-{version}-src.zip gvNIX-{version}-src/

Branch
------

* Optional, create a branch for a new development version::

   bash:~/gvnix/trunk/code$ mvn release:branch -DbranchName={version}

Documentation
=============

Documentation index
-------------------

* On each gvNIX project module you can find technical and user guides at module/docs/ in reStructuredText format and English:

  * td-module.rst: Technical design
  * ug-module.rst: User guide

* gvNIX work guides are placed at src/site/docbook/ in docbook format and Spanish:

  * reference: Working guide about projects development with gvNIX.
  * developer: Working guide about gvNIX project development.

* gvNIX project methodology documentation can be found at ../doc in gvMetrica format and Spanish.

Contact us ?
------------

* https://gvnix.googlecode.com
* http://listserv.gva.es/cgi-bin/mailman/listinfo/gvNIX_soporte
* http://www.gvpontis.gva.es/cast/gvnix

Need more info ?
----------------

For more information generate and read the *gvNIX Developer Guide* (Spanish).

# Run the following command from the root checkout location::

   bash:~/gvnix/trunk/code$ mvn site

# This will create the guide in the "target/site/developer" directory (in several formats)::

    target
    `-- site
        `-- developer
            |-- html
            |   |-- index.html
            |   `-- ...
            |-- html-single
            |   `-- index.html
            `-- pdf
                `-- gvNIX-referencia.pdf

Write doc
---------

* Download and install XMLmind XML Editor Personal Edition ( http://www.xmlmind.com/xmleditor/download.shtml )
* Use the previous editor to open ``src/site/docbook/developer/index.xml`` and contribute with your knowledge.

TODO
====

* Remove all @Reference PathResolver declarations from classes and retrieve PathResolver from ProjectMetadata instead

  https://jira.springsource.org/browse/ROO-2097

* Roo refactor at web layer commands:

   web xyz install
   web xyz scaffold --class
   web xyz all --package

  https://jira.springsource.org/browse/ROO-2297
  
* Composite primary key support in MVC scaffolding:
  
   https://jira.springsource.org/browse/ROO-1999

  Support for embedded in Web scaffolding:

   http://projects.disid.com/issues/3920  
   https://jira.springsource.org/browse/ROO-345
  
  Data-on-demand and integration tests to support composite primary keys
  
   https://jira.springsource.org/browse/ROO-2070

* Declare annotation to a method param.
  Declare annotation - augmentation/overriding and precedence.
  
   https://bugs.eclipse.org/bugs/show_bug.cgi?id=313026
   https://bugs.eclipse.org/bugs/show_bug.cgi?id=321820

* Cache add-on:

	http://blog.springsource.com/2011/02/23/spring-3-1-m1-caching/
	http://ehcache.org/documentation/overview.html
	
	http://ehcache.org/features.html
	
	y además se puede integrar con Spring mediante anotaciones
	
	http://ehcache.org/recipes/spring-annotations.html
	
	aunque el propio hibernate ya lo tienen más que usado.
	
	http://ehcache.org/documentation/hibernate.html

* Criteria API:
	
	A raíz de este mensaje del foro: http://forum.springsource.org/showpost.php?p=351029&postcount=1
	
	http://docs.jboss.org/hibernate/jpamodelgen/1.0/reference/en-US/html_single/
	
	JPA 2 defines a new typesafe Criteria API which allows criteria queries to be constructed in a strongly-typed manner, using metamodel objects to provide type safety.
	
	http://stackoverflow.com/questions/3037593/how-to-generate-jpa-2-0-metamodel
	
	Sip, Sentencias ya utiliza el Criteria API aunque no he llegado a profundizar en el tema del typesafe.
	
	Ya q estamos https://jira.springsource.org/browse/ROO-2112 ... votad plis 

* Sería interesante documentar cómo propone Stefan el cambio de JS, no lo vamos a hacer pero es una info que estaría bien conservar. ¿Alguna sugerencia sobre cómo añadir esta pequeña reseña de Stefan?

   https://jira.springsource.org/browse/ROO-2216

* Capa web con sitemesh

   https://jira.springsource.org/browse/ROO-41, 

* Info importante sobre atributos que empiezan por '_', no se incluyen en la generación del hash de 'z'

   https://jira.springsource.org/browse/ROO-2226, 

*  Soporte multipart para forms, welcome file upload.

   https://jira.springsource.org/browse/ROO-2231

* 1.1.3 introduced a 'web mvc update tags' command which allows the replacement of existing tagx files with an updated version if there is a difference.
  Obviously, this means Roo will not automatically update tagx files upon version upgrades.

* Currently, clients are required to create a MutableFile instance to read an XML file to convert to a DOM Document.
  FileManager and XmlUtils will be enhanced to do the work itself reducing the need for so much code in callers.

* Replace hard coded gvNIX version with var:

   roo/shell/src/main/java/org/springframework/roo/shell/AbstractShell.java

* Validate bin/ scripts.
  ¿ Replace GVNIX_HOME and ROO_HOME vars with GVNIX_DEV and ROO_DEV ?
