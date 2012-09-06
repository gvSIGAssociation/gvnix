==========================================
 gvNIX Oracle JDBC Driver Wrapper
==========================================

:Project:   gvNIX. Spring Roo based RAD tool
:Copyright: Conselleria d'Infraestructures i Transport - Generalitat Valenciana
:Author:    DiSiD Technologies, S.L.
:Revision:  $Rev$
:Date:      $Date$

This work is licensed under the Creative Commons Attribution-Share Alike 3.0
Unported License. To view a copy of this license, visit
http://creativecommons.org/licenses/by-sa/3.0/ or send a letter to
Creative Commons, 171 Second Street, Suite 300, San Francisco, California,
94105, USA.

.. contents::
   :depth: 2
   :backlinks: none

.. |date| date::

Description
===============

Oracle JDBC driver classes for use with JDK1.4+, driverclass:oracle.jdbc.OracleDriver.

How to install
================

#. Turn Oracle JDBC JAR into an OSGi-enabled JAR

   #. Oracle JDBC isn't in public Maven repositories, so you have to install
      in your local Maven repository by hand::
      
        bash shell:
        
          mvn install:install-file \
           -Dfile=$GVNIX_HOME/wrapping/oracle/lib/ojdbc14-10.2.0.5.jar \
           -DgroupId=com.oracle \
           -DartifactId=ojdbc14 -Dversion=10.2.0.5 -Dpackaging=jar

   #. Run ...

#. Setup proxy (optional)

#. Install required dependencies::

    roo shell:

      osgi start --url http://repository.springsource.com/ivy/bundles/external/javax.resource/com.springsource.javax.resource/1.5.0/com.springsource.javax.resource-1.5.0.jar

      osgi start --url http://repository.springsource.com/ivy/bundles/external/javax.transaction/com.springsource.javax.transaction/1.1.0/com.springsource.javax.transaction-1.1.0.jar


# Ben Alex commented on ROO-1937 (Add new option to add-on creator to facilitate creation of wrapped bundles (such as JDBC drivers)) saying:

For context, the wrapper will turn a non-OSGi JAR into an OSGi-enabled JAR. Specifically, an automatic static analysis is undertaken of the classes in the input JAR and a manfiest is created in the resulting output JAR. The output JAR is then usable in Roo or other OSGi containers. Occasionally you'll need to tweak the maven-bundle-plugin instructions in pom.xml. For non-trivial examples of this, check Roo out of Git and review the items we're wrapping in the /wrapping directory. Most of the time, though, the automatic static analysis is sufficient and it will simply work without maven-bundle-plugin tweaks.

Note the --artifactId, --groupId and --version given in the command represents the "input" non-OSGi JAR and should identify that JAR in your local Maven repository. When you "mvn deploy" the standard Maven behavior of attempting an automatic downloaded from a remote Maven repository (such as Maven Central) if not already local will occur. Obviously only items in remote Maven repositories will automatically download.

If you "mvn deploy", by default the wrapped JAR will be placed on Google Code. You can email the OBR repository.xml URL of your Google Code project to s2-roobot@vmware.com and it will be automatically indexed and subsequently appear in the "addon search", "addon install" etc commands. This is the easiest way to make a wrapped JAR available to all Roo users. See the Spring Roo Reference Guide for more details on add-on distribution and RooBot.

Please check the license carefully of any items you wrap. Do not "mvn deploy" items to Google Code or other public locations where doing so would be violating their licenses. If you do not "mvn deploy", you may use "mvn install" to create a local OSGi-enabled JAR on your machine (which is installed into your local Maven repository), and from there you can load that JAR in your Roo installation using the "osgi start" command. A further alternative is to edit the pom.xml of the created project and configure it to deploy to a web server within your organisation (if permitted by the license). If you do this, you can then share the organisation's web server OBR repository.xml URL with your colleagues and then use Roo's "osgi obr url add" command to add that URL to their environments. Once this is completed, the "osgi obr start" command will work and enable you to start the bundles defined in that repository.xml. This is the generally suggested approach if you have multiple libraries to wrap inside your organisation that you cannot share publicly for some reason (licensing, confidentiality, liability etc). Of course another alternative is to copy the JAR you produced using "mvn install" to an organisational web server and simply give internal people fully-qualified "osgi start" commands containing that URL. These techniques mean you need not have everyone in your organisation wrapping the same libraries yet you can share the results of someone having performed the wrapping.

Naturally if a wrapped library is open source, please wrap it and use "mvn deploy" to deploy it to the default Google Code repository to share it with the wider community via the RooBot mechanism mentioned earlier. This is far easier as the "addon" commands automate searching and installation, plus inbuilt features such as Roo shell's unknown command resolver and Roo's JDBC acquisition module will be able to automate the installation of such add-ons.

How to test
=============

* Install Oracle XE 10g Release 2

  Port: 5050
  SYS passwd: sysadm
  http://127.0.0.1:5050/apex

