
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

First, you need a GitHub account:

 http://forum.springsource.org/showthread.php?114239-Spring-Roo-sources-move-to-GitHub!

And your SSH public key from your account settings (http://help.github.com/linux-set-up-git/).

Be aware that Roo source code is commited on gvNIX project at ``roo`` folder.
However, Roo source code can be updated to a new tag with next commands:

* Update git info:				git pull
* List available tags:			git tag -l
* Store our Roo modifications:	git stash
* Change to new tag:			git checkout {version}
* Apply our Roo modifications:	git stash pop
* Save in SVN:					svn commit

Update gvNIX parent pom roo.version property to new value.
Be careful, git deleted files could be no deleted into SVN ! 

Other userful Git commands:

* Change to master branch:		git checkout master 

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

Developing within Eclipse
==========================

Eclipse can be used to develop gvNIX. Use ``mvn clean eclipse:clean eclipse:eclipse`` to produce Eclipse project files that can be imported via *File > Import > Existing Projects into Workspace*.

It is recommended that you create an Eclipse project for each add-on, in spite of creating a project to contain the entire project.

Package gvNIX
=============

* Update and commit gvNIX version at Roo shell start:

   roo/shell/src/main/java/org/springframework/roo/shell/AbstractShell.java

* Update and commit the appropriate Roo version (tag or head) with git and set this Roo version reference into gvNIX parent pom.xml.

* Update and commit the appropriate Roo and gvNIX versions (GVNIX_VERSION and ROO_VERSION) at build.sh file.

* Update and commit the appropriate gvNIX version in docbook documentacion (releaseinfo property) at src/site/docbook/reference/index.xml

* Create the tag for the gvNIX version we want to build using the following command::

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

* Test uncompress ZIP file, start it a execute some script and check in STS.

* Send to publish on gvPONTIS the gvNIX binary zip, the gvNIX source zip, the html single page reference guide and version changes summary.

* Update (or create if not exists) the wiki page for each add-on from add-on user guide on html format.

* Send to communication department the final release date and version changes summary to publish on social networks:

  * Forum
  * Google Code
  * DiSiD blog
  * gvNIX linkedin group
  * gvNIX twit with Google Code link
  * DiSiD twit with DiSiD blog link

Source code
-----------

* Optional, package the source code release use the following command::

   bash:/tmp$ svn export http://scmcit.gva.es/svn/gvnix/tags/{version}
   
  Rename folder:
  
   bash:/tmp$ mv {version} gvNIX-{version}-src/

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

Proof of concept
================

Roo example projects:

* http://scmcit.gva.es/svn/gvnix-proof/trunk/petclinic

ESB:

* http://scmcit.gva.es/svn/gvnix-proof/trunk/servicemix-service

GWT:

* http://scmcit.gva.es/svn/gvnix-proof/trunk/vaadin
* https://svn.disid.com/svn/disid/proof/gvnix/vaadin-test-project

Real world projects:

* http://scmcit.gva.es/svn/acuses
* http://scmcit.gva.es/svn/sentencias
* http://scmcit.gva.es/svn/regproy

Tests:

* http://scmcit.gva.es/svn/gvnix-proof/trunk/sentencias-upgrade-roo-1.1.2
* https://svn.disid.com/svn/disid/proof/gvnix/gvnix-demo
* https://svn.disid.com/svn/disid/proof/spring_roo/pizza-shop
* https://svn.disid.com/svn/disid/proof/spring_roo/terceros-test

Composite identifiers:

* https://svn.disid.com/svn/disid/proof/spring_roo/reveng_and_compound_id
* https://svn.disid.com/svn/disid/proof/spring_roo/compositeId

TCON:

* https://svn.disid.com/svn/disid/proof/spring_roo/springmvc-tcon

Tomcat5:

* https://svn.disid.com/svn/disid/proof/gvnix/tomcat5
* https://svn.disid.com/svn/disid/proof/spring_roo/gvnix_tomcat5_test

TODO
====

* Remove all @Reference PathResolver declarations from classes and retrieve PathResolver from ProjectMetadata instead

  https://jira.springsource.org/browse/ROO-2097

* Roo refactor at web layer commands:

   web xyz install
   web xyz scaffold --class
   web xyz all --package

  https://jira.springsource.org/browse/ROO-2297
  
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
	
	Foro Roo:
	
	http://forum.springsource.org/showthread.php?109011-Caching-Entities-In-Roo

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

* Hibernate: PostLoad method invoked before collection initialised

  https://issues.jboss.org/browse/JBAS-5474

* Make uniform generated "aj" file names (examples):

  + OCCChecksumMetadataProvider.java:  "gvNIX_occChecksum" 
  + WSServiceSecurityMetadataProvider.java:  "GvNIX_WebSecurity" 
  + WSExportExceptionMetadataProvider.java:  "GvNIX_WebFault"  
  + WSExportMetadataProvider.java: "GvNix_WebService"
  + OCCChecksumMetadataProvider: gvNIX_occChecksum
  + WSServiceSecurityMetadataProvider: GvNIX_WebSecurity
  + WSExportMetadataProvider: GvNix_WebService
  + WSExportXmlElementMetadataProvider: GvNix_XmlElement
  + WSImportMetadataProvider: GvNix_WebServiceProxy
  + ScreenMetadataProvider: gvNIX_related_entries
  
  Decidido utilizar formato GvNIX_${nombre de la anotación sin el prefijo GvNIX}
  
* Some add-ons has duplicated resources section at pom.xml to correct replacing.
  However, mvn eclipse:eclipse not works.

* Los proyectos gvNIX por defecto configuran DBCP, probar que funciona aunque se reinicie la BBDD

* Revisar los pom.xml de los add-ons (ver que tengan la dependencias correctas y hereden las del pom padre

* Analizar el uso de listas de anotaciones.
