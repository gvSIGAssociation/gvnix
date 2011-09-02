
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

* JAR original: ojdbc14-10.2.0.4.0.jar
* Bundle de gvNIX: org.gvnix.com.oracle.ojdbc14-10.2.0.4.0.jar

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


