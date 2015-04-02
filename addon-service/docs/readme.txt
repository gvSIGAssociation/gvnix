===================================================================
 gvNIX Service Layer Add-on
===================================================================

:Project:   gvNIX. Spring Roo based RAD tool
:Copyright: Conselleria d'Infraestructures i Transport - Generalitat Valenciana
:Author:    DiSiD TECHNOLOGIES, S.L.
:Revision:  $Rev: 1507 $
:Date:      $Date: 2012-08-21 09:27:47 +0200 (mar, 21 ago 2012) $

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

This project generates the addon to generate and manage service classes and export and import to web services.

Features
===========

TBC

This are features contained:

Development
============

Create two Operation classes, one for service layer management ``ServiceLayerOperationsImpl`` and another for manage the web service layer ``WebServiceLayerOperationsImpl``.

Commands
---------

* remote service class --class:

* remote service operation --class clase --name nombreOperacion --return clase:

* remote service define ws --class clase --name nombreServicio:

* remote service export operation --class clase --method nombreMetodoEntidad --name nombreAPublicar:

* remote service import ws --endPoint urlOPropiedad --wsdl url2wsdl.xml:

* remote service entity --class nombreClase:

* remote service ws list : List all project services (exported/imported) in console.

* remote service security ws --class importedServiceClass --certificate path_to_certificate_file --password password --alias alias

  Adds message signature to a imported Axis web service. Certificate file will be copy into project resource. 

How to install
===============

Important:

You must update **JavaParserMethodMetadata** class in ``gvnix/trunk/code/roo/classpath-javaparser`` ROO Add-on with patch **issue_3879_JavaParserMethodMetadata.patch** in::

http://projects.disid.com/issues/3879

And then, run ``mvn clean install`` in roo sources directory to use this update.


About import WS based on WSDL
==============================

If the WSDL is under a secure server and the access is through HTTPS and the host uses a certificate non reliable by the JVM
the add-on will handle the unreachable source error importing, automatically, the certificates in certificate chain to JVM
cacerts keystore. That is, ``$JAVA_HOME/jre/lib/security/cacerts`` in SUN JDK distribution. Also it creates a local copy of
the needed certificates under ``src/main/resources`` folder of the project, with ``.cer`` extension, so you can distribute
them for its installation in other environments.

Notice that this will be possible only if ``cacerts`` file is writable by the
system user running the add-on and the password of the keystore is ``changeit`` (the default keystore password). If these
two conditions are not met, the add-on only will create the local copy of the certs in certificate chain and you are the
responsible of its instalation in your cacerts keystore (see keytool manual for keystore manipulation).
