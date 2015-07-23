gvNIX - Spring Roo Addon Suite ![alt text](https://jenkins.gvsig.net/buildStatus/icon?job=gvNIX-CI "Build status")
======================================

Welcome to gvNIX, an Spring Roo Addon Suite.

gvNIX is an <a href="http://docs.spring.io/spring-roo/docs/2.0.0.M1/reference/html/#roo-addon-suites" target="_blank">Spring Roo Addon Suite</a> that focuses both enterprise knowledge and enterprise standards to build Java applications.

gvNIX is sponsored by the <a href="http://www.dgti.gva.es/" target="_blank">General
Directorate for Information Technologies</a> (DGTI) at Regional Ministry of Finance
and Public Administration of the Generalitat Valenciana (Valencian Community, Spain),
and led by <a href="http://www.disid.com" target="_blank">DISID</a>.

About this doc
==============

These instructions show you how to get started with gvNIX source tree. Note
that these instructions are for developers looking to develop gvNIX itself.

If you like to try a release that has already been built, tested and
distributed by the core development team, we recommend that you visit gvNIX
download page http://www.gvnix.org and read the documentation.

Copyright (C) 2010 DGTI - Generalitat Valenciana

This work is licensed under the Creative Commons Attribution-Share Alike 3.0
Unported License. To view a copy of this license, visit
http://creativecommons.org/licenses/by-sa/3.0/

Pre-requisites
==============

To start to develop gvNIX and extend its features you need:

* Last Spring Roo distribution (http://spring.io/projects/spring-roo)
* JDK 7 or above ( http://www.oracle.com/technetwork/es/java/javase/downloads/index.html)
* Maven 3.0.5 or above ( http://maven.apache.org/download.html )
* Internet access so that Maven can download required dependencies

Run gvNIX Roo Addon Suite
===========================

To be able to test gvNIX quickly without deploy all components you should following the next steps:

* Add _$ROO_HOME_ variable to your $PATH
* Compile gvNIX project using 'mvn clean install' on your project folder
* Execute ./gvnix-dev

Developing within STS
==========================

STS can be used to develop gvNIX Roo Addon Suite.

Every addon can be imported via *File > Import > Maven > Existing Maven Project*.

Run gvNIX CI test
=======================

To Run Continuous integration test just execute:

  $ deployment-support/build.sh spring-roo-xxx.zip test

Deploy gvNIX artifacts
=======================

All gvNIX artifacts will be deployed on Maven central.

* **RELEASE artifacts**: https://oss.sonatype.org/service/local/staging/deploy/maven2/org/gvnix
* **SNAPSHOT artifacts**: https://oss.sonatype.org/content/repositories/snapshots/org/gvnix

In addition, gvNIX Addon Suite will be deployed in our own gvNIX repository:

* **RELEASE gvNIX Addon Suite**: http://repository.gvnix.org/
* **SNAPSHOT gvNIX Addon Suite**: http://repository.gvnix.org/snapshots

To deploy **RELEASE** artifacts and generate gvNIX Addon Suite you must execute the following command:

	$ deployment-support/build.sh spring-roo-xxx.zip release

To deploy **SNAPSHOT** artifacts and generate gvNIX Addon Suite you must execute the following command:

	$ deployment-support/build.sh spring-roo-xxx.zip deploy

**NOTE**: _You must have the necessary permissions to deploy gvNIX artifacts_

**About gvNIX version number**:

gvNIX versions are series of individual numbers, separated by periods, with a progression such as 1.7.0, 1.8.0, 1.8.1, 1.9.0, 1.10.0, 1.11.0, 1.11.1, 1.11.2, and so on.

The early stages are identified with "ALPHA", "BETA" or "BUILD-SNAPSHOT"
qualifiers.

Documentation
=============

Documentation index
-------------------

* On each gvNIX project module you can find technical and user guides in
the *module/docs/* folder in ASCIIdoc format and Spanish and English:

  * td-module.adoc: Technical design
  * ug-module.adoc: User guide

* gvNIX work guides are placed at _/deployment-support_ in AsciiDoc format and Spanish:

  * reference: Working guide about projects development with gvNIX.
  * developer: Working guide about gvNIX project development.

Need more info ?
----------------

For more information generate and read the *gvNIX Developer Guide* (Spanish).

Run the following command from the root checkout location:

   bash:~/gvnix$  mvn clean install

This will create the guide in the _/deployment-support/target/generated-docs_ directory (in several formats):

    generated-docs
    |-- developer
    |	`-- diagrams (resources)
    | `-- images (resources)
    | `-- index.html (developer guide)
    | `-- index.pdf (developer guide)
    `-- reference
    | `-- images (resources)
    | `-- index.html (reference guide)
    | `-- index.pdf (reference guide)


The *gvNIX Reference Guide* (Spanish) is the documentation for developing
projects with the framework.


Write doc
---------

gvNIX documentation is moving to AsciiDoc. These docs have the suffix .ad,
.adoc or .asciidoc.

To learn more about how to convert AsciiDoc to PDF, HTML5,
etc go to http://asciidoc.org/ or http://asciidoctor.org/


Contribute to gvNIX Project
==============================

Do you want to contribute to gvNIX Project? :D

Create an issue
-----------------
Create a new issue on gitHub using the following link
https://github.com/gvSIGAssociation/gvnix/issues/new

Send your Pull Requests
------------------------

Fork gvNIX project and implement your own features or bug fixes

Send your Pull Requests with your applied changes

gvNIX team will validate your changes and merge your Pull Request


Contact us ?
------------

* http://www.gvnix.org
* http://www.disid.com
* https://github.com/gvSIGAssociation/gvnix
* If you use Twitter, you are encouraged to follow @gvNIX and we appreciate your mentions.



