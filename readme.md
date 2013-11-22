
gvNIX - RAD tool for Java developers
======================================

Welcome to gvNIX, Spring Roo based RAD tool for Java developers.

About this doc
==============

These instructions show you how to get started with gvNIX source tree. Note
that these instructions are for developers looking to develop gvNIX itself.

If you like to try a release that has already been built, tested and
distributed by the core development team, we recommend that you visit gvNIX
download page http://www.gvnix.org in Spanish or http://gvnix.googlecode.com
in English and read the documentation.

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

gvNIX provides its own distribution of the Spring Roo source code. This is needed because gvNIX has some patches still not included in a Roo published distribution, and the process to generate a distribution of gvNIX needs all the sources.

Then, first of all you must configure and compile the roo projects, by following the steps provided in the document::

 roo/readme.txt

It provides more information of projects requirements and setup, like MAVEN, GIT, GPG or ECLIPSE.

If you don't want to perform the GPG configuration needed to sign build outputs, just add the following parameter each time you call mvn to build the project::

  -Dgpg.skip=true

Git
---

Git is used for download Roo source code, but Roo is stored into gvNIX SVN too.
To compile the gvNIX project source code you don't need Git installed, it is only required to upgrade the gvNIX based Roo source code.

First, you need a GitHub account:

 http://forum.springsource.org/showthread.php?114239-Spring-Roo-sources-move-to-GitHub!

And your SSH public key from your account settings (http://help.github.com/linux-set-up-git/).

Roo source code is commited on gvNIX project in the ``roo`` folder.
However, Roo source code can be updated to a new tag with the following commands:

* Ask which branch is current:	git status
* Revert working copy changes:  git checkout .
* Add a file/resolve conflict:  git add
* Revert index (adedd) changes: git reset
* Update git info:				git pull
* Update master git info:		git pull git@github.com:SpringSource/spring-roo.git master
* List available tags:			git tag -l
* Store our Roo modifications:	git stash
* Change to new tag:			git checkout {version}
* Apply our Roo modifications:	git stash pop
* Merge changes:				git mergetool
* Remove all stashed states:	git stash clear
* Save in SVN:					svn commit

Update gvNIX parent pom roo.version property to the new value.
Be careful, git deleted files could be not deleted from SVN !

Other userful Git commands:

* Change to master branch:		git checkout master

Run gvNIX dev
=============

#. Build Roo::

    bash:~/gvnix/trunk/code/roo$ mvn clean install

   Roo is only necessary to be installed the first time.
   Only reinstall it if Roo there are changes in the source code.

#. Build gvNIX::

    bash:~/gvnix/trunk/code$ mvn clean install

   From now on, you will need to reinstall only each modified module instead of reinstalling all the gvNIX projects again::

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

* Modify and commit gvNIX version at Roo shell start:

   roo/shell/src/main/java/org/springframework/roo/shell/AbstractShell.java

* Update if necessary the Roo source code version (tag or head) into 'roo' folder with git

* Update Roo version reference into gvNIX parent pom.xml (roo.version)

* Modify and commit the appropriate Roo and gvNIX versions (GVNIX_VERSION and ROO_VERSION) at build.sh file.

* Modify and commit the appropriate gvNIX version in docbook documentacion (releaseinfo property) at src/site/docbook/reference/index.xml and at src/site/docbook/developer/index.xml

* Create the tag for the gvNIX version we want to build using the following command::

   bash:~/gvnix/trunk/code$ svn update
   bash:~/gvnix/trunk/code$ mvn release:prepare -Dtag={version}

  Version formats:

   * Snapshot: X.Y.Z-SNAPSHOT
   * Release: X.Y.Z-RELEASE

* If all ok, clean packaging information on the trunk code directory::

   bash:~/gvnix/trunk/code$ mvn release:perform
   bash:~/gvnix/trunk/code$ mvn release:clean

  If some errors, revert the changes::

   bash:~/gvnix/trunk/code$ mvn release:rollback
  
  If release don't work whit this method, do manually replace old version with new one searching in text mode into all project.
  Then do tag manually and iterate to new SNAPSHOT version manually replacing too.

* To package the binary release use the following commands::

   bash:~/gvnix/tags/$ svn update
   bash:~/gvnix/tags/{version}$ ./build.sh -d

  The ``-d`` option deploy to google code, can be used only by commiters.
  This requires next configuration in maven configuration file at conf/settings.xml::

    <server>
      <id>Google Code</id>
      <username>gvnixscm@gmail.com</username>
      <password>XXXXXXXXXXX</password>
    </server>

  Get password from redmine project wiki.

  Check if all add-ons are published correctly at http://gvnix.googlecode.com/svn/repo/repository.xml for RooBoot.
  This will create the ZIP file ``target/gvnix-dist/gvNIX-{version}.zip``.

* Deploy wrappings to google code.

* Test uncompress ZIP file, start it, execute some script and check in STS.

* Publish into gvnix.googlecode.com the gvNIX binary zip.

* Notify to the communication department the new version. 

Branch
------

* Optional, create a branch for a new development version::

   bash:~/gvnix/trunk/code$ mvn release:branch -DbranchName={version}

Documentation
=============

Documentation index
-------------------

* On each gvNIX project module you can find technical and user guides in the *module/docs/* folder in reStructuredText format and English:

  * td-module.rst: Technical design
  * ug-module.rst: User guide

* gvNIX work guides are placed at src/site/docbook/ in docbook format and Spanish:

  * reference: Working guide about projects development with gvNIX.
  * developer: Working guide about gvNIX project development.

* gvNIX project methodology documentation can be found at ../doc in gvMetrica format and Spanish.

Contact us ?
------------

* http://www.gvnix.org
* https://gvnix.googlecode.com
* http://listserv.gva.es/cgi-bin/mailman/listinfo/gvNIX_soporte
* http://www.gvpontis.gva.es/cast/gvnix
* If you use Twitter, you're encouraged to follow @gvnix and we appreciate youur mentions.

Need more info ?
----------------

For more information generate and read the *gvNIX Developer Guide* (Spanish).

# Run the following command from the root checkout location::

   bash:~/gvnix/trunk/code$ mvn site

# This will create the guide in the "target/site/developer" directory (in several formats)::

    target
    |-- docbkx
    |	`-- pdf
    |		`-- index.pdf (reference guide)
    `-- site
    	|-- index.html
        |-- developer
        |   |-- html
        |   |   |-- index.html
        |   |   `-- ...
        |   `-- html-single
        |       `-- index.html
        `-- reference
            |-- html
            |   |-- index.html
            |   `-- ...
            `-- html-single
                `-- index.html

The *gvNIX Reference Guide* (Spanish) is the documentation for developing projects with the framework.

# And will create a site with the project summary (target/site/index.html).

Write doc
---------

* Download and install XMLmind XML Editor Personal Edition ( http://www.xmlmind.com/xmleditor/download.shtml )
* Use the previous editor to open ``src/site/docbook/developer/index.xml`` and contribute with your knowledge.
