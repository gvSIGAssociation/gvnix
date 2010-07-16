
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

Copyright (C) 2010 Conselleria d'Infraestructures i Transport - Generalitat Valenciana

This work is licensed under the Creative Commons Attribution-Share Alike 3.0
Unported License. To view a copy of this license, visit 
http://creativecommons.org/licenses/by-sa/3.0/

Pre-requisites
================

To start with gvNIX you need:

* A *nix machine (Windows users should be OK if they write a .bat)
* JDK 5.0 or above ( http://java.sun.com/javase/downloads/index.jsp )
* Maven 2.0.9 or above ( http://maven.apache.org/download.html )
* Internet access so that Maven can download required dependencies
* Git client, http://en.wikipedia.org/wiki/Git_%28software%29

Setup instructions
===================

Spring Roo 1.1.0.M1
-----------------------------

* Go to gvNIX root directory

* Use your Git client to checkout Spring Roo sources from ``git://git.springsource.org/roo/roo.git`` to gvNIX root directory.

  If your internet connection is defined through a proxy you have to set up the confifuration in `Git`_ (Appendix -> Proxy configuration -> Git).

  Example::

     bash:~/gvnix/trunk/code$ git clone git://git.springsource.org/roo/roo.git

* Change to Spring Roo folder and switch to 1.1.0.M1 version ``3a0b8a399aae14167139c185e4e31355e20d1f25``. Example::

   bash:~/gvnix/trunk/code$ cd roo
   bash:~/gvnix/trunk/code/roo$ git checkout 3a0b8a399aae14167139c185e4e31355e20d1f25

Maven
------------

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

Appendix
=========

Proxy configuration
---------------------

Git
~~~~

Configure Git access through a proxy.

# Set ``http_proxy`` as an environment variable::

    bash:~$ export http_proxy=http://<username>:<password>@<proxy_ip>:<proxy_port>

# Set ``http.proxy`` using Git config command::

    bash:~$ git config --global http.proxy proxy_addr:proxy_port

# To download the project trhough a proxy you have to use the same command changing the protocol.

    Checkout without proxy (git)::

        bash:~$ git clone git://github.com/doctrine/doctrine2.git doctrine

    Checkout *through a proxy* (use *http* instead of git)::

        bash:~$ git clone http://github.com/doctrine/doctrine2.git doctrine

Maven
~~~~~~

To configure Maven access through a proxy you have to set the proxy parameters in Maven configuration file $M2_HOME/config/settings.xml. This is an example of the proxy configuration in the file::

    <?xml version="1.0" encoding="UTF-8"?>
    <settings xmlns="http://maven.apache.org/POM/4.0.0" 
        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
        xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
                            http://maven.apache.org/xsd/settings-1.0.0.xsd">

        ...

        <proxies>
            <proxy>
                <id>optional</id>
                <active>true</active>
                <protocol>http</protocol>
                <host>proxy.somewhere.com</host>
                <port>8080</port>
                <username>proxyuser</username>
                <password>somepassword</password>
                <nonProxyHosts>www.google.com|*.somewhere.com</nonProxyHosts>
            </proxy>
        </proxies>
    </settings>

SVN
~~~~

To configure SVN access through a http proxy, e.g. your web browser requires a http proxy, add the following lines to '~/.subversion/servers'::

   [groups]
   mosuma=svn.mosuma.com
   [mosuma]
   http-proxy-host=proxy.ntu.edu.sg
   http-proxy-port=8080

Groups defines a specific direction to access by svn and then set the proxy values for this group.

Variables::

  * [groups] Set repository address to access inside group ``groups``. Name and address.
  * [Name] Set group proxy connection properties (http-proxy-host y http-proxy-port).

