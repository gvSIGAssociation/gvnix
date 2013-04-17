
======================================
 gvNIX - RAD tool for Java developers
======================================

Introduction
===============

gvNIX wrapping subproject contains OSGi-ready versions of open source enterprise libraries that are commonly used by OSGi applications.

Ben Alex
--------

For context, the wrapper will turn a non-OSGi JAR into an OSGi-enabled JAR. Specifically, an automatic static analysis is undertaken of the classes in the input JAR and a manfiest is created in the resulting output JAR. The output JAR is then usable in Roo or other OSGi containers. Occasionally you'll need to tweak the maven-bundle-plugin instructions in pom.xml. For non-trivial examples of this, check Roo out of Git and review the items we're wrapping in the /wrapping directory. Most of the time, though, the automatic static analysis is sufficient and it will simply work without maven-bundle-plugin tweaks.

Note the --artifactId, --groupId and --version given in the command represents the "input" non-OSGi JAR and should identify that JAR in your local Maven repository. When you "mvn deploy" the standard Maven behavior of attempting an automatic downloaded from a remote Maven repository (such as Maven Central) if not already local will occur. Obviously only items in remote Maven repositories will automatically download.

If you "mvn deploy", by default the wrapped JAR will be placed on Google Code. You can email the OBR repository.xml URL of your Google Code project to s2-roobot@vmware.com and it will be automatically indexed and subsequently appear in the "addon search", "addon install" etc commands. This is the easiest way to make a wrapped JAR available to all Roo users. See the Spring Roo Reference Guide for more details on add-on distribution and RooBot.

Please check the license carefully of any items you wrap. Do not "mvn deploy" items to Google Code or other public locations where doing so would be violating their licenses. If you do not "mvn deploy", you may use "mvn install" to create a local OSGi-enabled JAR on your machine (which is installed into your local Maven repository), and from there you can load that JAR in your Roo installation using the "osgi start" command. A further alternative is to edit the pom.xml of the created project and configure it to deploy to a web server within your organisation (if permitted by the license). If you do this, you can then share the organisation's web server OBR repository.xml URL with your colleagues and then use Roo's "osgi obr url add" command to add that URL to their environments. Once this is completed, the "osgi obr start" command will work and enable you to start the bundles defined in that repository.xml. This is the generally suggested approach if you have multiple libraries to wrap inside your organisation that you cannot share publicly for some reason (licensing, confidentiality, liability etc). Of course another alternative is to copy the JAR you produced using "mvn install" to an organisational web server and simply give internal people fully-qualified "osgi start" commands containing that URL. These techniques mean you need not have everyone in your organisation wrapping the same libraries yet you can share the results of someone having performed the wrapping.

Naturally if a wrapped library is open source, please wrap it and use "mvn deploy" to deploy it to the default Google Code repository to share it with the wider community via the RooBot mechanism mentioned earlier. This is far easier as the "addon" commands automate searching and installation, plus inbuilt features such as Roo shell's unknown command resolver and Roo's JDBC acquisition module will be able to automate the installation of such add-ons.

Sistema de nominación de wrappers
===========================================

Version
--------
Springsource y Apache Servicemix mantienen la versión del JAR original, es interesante para identificar fácilmente la versión contenida en el bundle. 

La versión gvNIX no aporta ninguna info, da igual en qué versión de gvNIX se ha empaquetado un wrapper, lo importante es la versión de la librería empaquetada. 

En gvNIX se mantiene la versión del JAR original.

GroupId y ArtifactId
---------------------

Springsource mantiene toda la información del artifact original y le prefija su propio grupo, de esta manera en el JAR se identifica fácilmente que se ha generado por el proyecto Spring y qué contiene.

En gvNIX el artifactId se genera:

#. *groupId* y *artifactId* del proyecto original son distintos::

    groupId raíz de gvNIZ 
      + groupId proyecto original 
      + artifactId del proyecto original

#. Cuando *groupId* y *artifactId* del proyecto original son iguales::

    groupId raíz de gvNIX 
      + groupId proyecto original

#. *artifactId* del proyecto original no aporta información extra al *groupId*::

    groupId raíz de gvNIX 
      + groupId proyeto original

Ejemplos
----------

Apache Commons Collections 3.2.1 
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

*ArtifactId igual al groupId*

* JAR original: commons-collections-3.2.1.jar
* Bundle de Springsource: com.springsource.org.apache.commons.collections-3.2.1.jar
* Bundle de gvNIX: org.gvnix.commons-collections-3.2.1.jar

Oracle JDBC Driver
~~~~~~~~~~~~~~~~~~~~~

*ArtifactId distinto a groupId*

* JAR original: ojdbc14-10.2.0.5.jar
* Bundle de gvNIX: org.gvnix.com.oracle.ojdbc14-10.2.0.5.jar

JavaBeans(TM) Activation Framework
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

*ArtifactId no aporta más info a groupId*

* JAR original: activation-1.1.1.jar
* Bundle de gvNIX: org.gvnix.javax.activation-1.1.1.jar

.. note::

  El formato 'groupId + artifactId' sería muy repetitivo: org.gvnix.javax.activation.activation-1.1.1.jar

Conclusión
------------

Este sistema de nominación permite una identificación visual del proyecto que genera el bundle y de la librería empaquetada.

El bundle generado se instala en el repositorio Maven dentro del grupo de la librería original, de forma que permite un acceso sencillo ya que los bundles se buscan por la librería que contiene y no por el proyecto que lo empaqueta.

Wrappers analysis
==================

Use Eclipse to analyze bundle relations (imports and exports), just open generated MANIFEST.MF with Eclipse and you will get an easy way.

TODO
=====

* Todas los bundles incluidos en las *features* estan con una versión concreta. Los archivos *features.xml* soportan el uso de variables ${XYZ}, por lo que se pueden utilizar versiones definidas en los pom.xml. Es lo más adecuado.


