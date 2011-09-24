Hibernate 3.6.7 wrapper.

It has been tested with FUSE ESB 4.4 (Karaf).

Deploying in Apache ServiceMix
================================

In order to deploy Hibernate bundle, several additional OSGi bundles must be provisioned. 

The easiest and most repeatable way to provision these bundles is to use a Karaf feature descriptor to describe the bundles to be provisioned. The feature descriptor can be found at *src/main/features*. 

Building
-----------

Build the source code by executing ``mvn clean install`` command.

Deploying
----------

#. Start Apache ServiceMix 4.4
#. In the ServiceMix shell, add the feature descriptor for Hibernate by executing the following command::

    features:addurl mvn:org.hibernate/org.gvnix.org.hibernate.features/3.6.7.Final/xml/features

   .. note::

    If you did not build the Hibernate bundle locally, you will need to add the Maven repository for gvNIX project to the ServiceMix configuration.

    Edit *SERVICEMIX_HOME/etc/org.ops4j.pax.url.mvn.cfg* and add http://gvnix.googlecode.com/svn/repo/ to the ``org.ops4j.pax.url.mvn.repositories`` property.

#. Install the bundle by executing one of commands below in the ServiceMix shell::

    features:install gvnix-hibernate
    features:install gvnix-hibernate-spatial

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

