=============================
 gvNIX - ServiceMix Features
=============================

:Revision:  $Rev: 322 $
:Date:      $Date: 2011-09-12 06:59:38 +0200 (lun 12 de sep de 2011) $
:Copyright: 2011 (C) CITM

Introduction
=============

gvNIX features, the easiest and most repeatable way to install bundles in ServiceMix.

The feature descriptor describes the bundles to be provisioned and installed in ServiceMix to deploy and run gvNIX bundles.

It has been tested with FUSE ESB 4.4 (Karaf).

Build
=======

Build the source code by executing ``mvn clean install`` command.

Deploy
==========

#. Start Apache ServiceMix 4.4
#. In the ServiceMix shell, add the feature descriptor for gvNIX OSGi wrappers by executing the following command::

    features:addurl mvn:org.gvnix/org.gvnix.servicemix.features/0.8.0/xml/features

   .. note::

    If you did not build the gvNIX wrapping bundles locally, you will need to add the Maven repository for gvNIX project to the ServiceMix configuration.

    Edit *SERVICEMIX_HOME/etc/org.ops4j.pax.url.mvn.cfg* and add http://gvnix.googlecode.com/svn/repo/ to the ``org.ops4j.pax.url.mvn.repositories`` property.

#. Install the bundle by executing one of commands below in the ServiceMix shell::

    features:install gvnix-postgis/1.5.3.jdbc4
    features:install gvnix-hibernate/3.6.7.Final
    features:install gvnix-hibernatespatial-postgis/1.1.jdbc4

#. Confirm the installation by executing the following command in the ServiceMix shell::

    osgi:list

The output should be similar to:: 

  [ 230] [Active     ] [            ] [       ] [   60] J2EE JACC 1.1 (1.0.2)
  [ 231] [Active     ] [            ] [       ] [   60] Apache ServiceMix :: Specs :: JSR-303 API 1.0.0 (1.8.0)
  [ 232] [Active     ] [            ] [       ] [   60] gvNIX - OSGi - Hibernate Validator (4.2.0.Final)
  [ 233] [Active     ] [            ] [       ] [   60] gvNIX - OSGi - Hibernate (3.6.7.Final)

TODO
======

* Probar en un OSGi que no sea FUSE porque FUSE incluye muchos componentes como JAXB y hay que probar que el wrap funciona bien en todos los entornos OSGi

* Encontrar una forma de distribuir el features.xml en el repositorio de gvNIX http://gvnix.googlecode.com/svn/repo/

