==========================================================
  gvNIX. Empaquetado y distribución
==========================================================

:Project:   gvNIX. Spring Roo based RAD tool
:Copyright: Conselleria d'Infraestructures i Transport - Generalitat Valenciana
:Author:    DiSiD Technologies, S.L.
:Revision:  $Rev: 3307 $
:Date:      $Date: 2010-05-04 10:50:45 +0200 (mar 04 de may de 2010) $

This work is licensed under the Creative Commons Attribution-Share Alike 3.0    Unported License. To view a copy of this license, visit 
http://creativecommons.org/licenses/by-sa/3.0/ or send a letter to 
Creative Commons, 171 Second Street, Suite 300, San Francisco, California, 
94105, USA.

.. contents::
   :depth: 2
   :backlinks: none

.. |date| date::

Introducción
============

El proceso descrito en el presente documento permite generar un paquete para la distribución de una versión de gvNIX.
El paquete consiste en un fichero comprimido en formato *ZIP* que contiene todo lo necesario para ejecutar la versión de gvNIX, además de toda la documentación del proyecto.

Adicionalmente, se describe un segundo proceso para la generación de un paquete para la distribución del código fuente de una versión de gvNIX.

Finalmente, se describen las configuraciones que se realizaron sobre el proyecto para facilitar todos los procesos de empaquetado descritos.

Requisitos
==========

Para empaquetar gvNIX se necesita disponer del siguiente software instalado y configurado:

* Una máquina Unix (usuarios de Windows deberían poder usar gvNIX creando sus propios scripts .bat)
* JDK 6.0 o superior ( http://java.sun.com/javase/downloads/index.jsp )
* Maven 3.0.1 o superior ( http://maven.apache.org/download.html )
* Acceso a Internet para que Maven pueda descargar las dependencias

Ante cualquier duda, consultar el fichero ``leeme.txt`` en el directorio ``code``.
Es especialmente importante leer este fichero si se está accediendo a internet a través de un proxy, ya que habrá que configurar las aplicaciones GIT, Maven y SVN para que funcionen a través de él. 

Generar la distribución de una versión
======================================

A continuación, se describen los pasos que se deben seguir para obtener el paquete de distribución de una determinada versión de gvNIX.

Esto implica obtener la versión base de Roo a partir de la cual vamos a construir la distribución de gvNIX, congelar el código fuente de la versión de gvNIX en el repositorio y generar el paquete binario de la versión a partir del código fuente anteriormente congelado.
Opcionalmente, puede generarse el paquete de código fuente a partir de la misma versión de código fuente congelada.  

Obtener la versión base de Roo
------------------------------

La distribución de gvNIX utiliza como base la distribución de Spring Roo.
Por este motivo, para generar la distribución de una versión de gvNIX necesitamos el código fuente de una versión de Roo concreta.
Este código fuente ya se encontrará descargado en el subdirectorio roo/ del proyecto gvNIX.

Congelar el código fuente de gvNIX
----------------------------------

En esta sección se genera un tag en el repositorio de gvNIX que congela el código fuente de la rama raíz (trunk) del proyecto con un determinado número de versión.
El paquete será generado a partir del código fuente congelado en dicho tag.

#. Los pasos descritos en esta sección no serán necesarios si el tag ya está creado, ya que en dicho caso el código fuente de la versión ya fue congelado con anterioridad.
   Puede comprobarse si ya fue creado el tag a partir del cual queremos generar el paquete de la versión mediante los siguientes comandos::

       bash> cd gvnix/tags
       bash> svn update
       bash> ls -la

   Comprobar si en el listado de directorios mostrado ya existe la versión de código fuente a partir de la cual deseamos generar el paquete.
   En caso de existir, pasar a la siguiente sección `Generar el paquete binario de gvNIX`_.

#. Desde que el código fuente se considere terminado hasta que se congele la versión del código fuente realizando un tag, es importante que ningún desarrollador pueda añadir nuevos cambios al repositorio.
   Con esto se pretende evitar que puedan incluirse modificaciones no deseadas en la versión del código fuente que estamos congelando. 
   Para ello, se recomienda denegar el acceso en modo escritura al repositorio del proyecto a todos los programadores desde el fin del desarrollo hasta que esté realizado el tag.

#. Actualizar la versión de código fuente de la rama principal a la última versión::

       bash> cd gvnix/trunk
       bash> svn update

#. Crear el tag de la versión para la cual queremos generar el paquete mediante los siguientes comandos::

       bash> cd gvnix/trunk/code
       bash> mvn release:prepare -Dtag={nombre de tag a crear}
       
   El formato del {nombre de tag a crear} seguirá la nomenclatura X.Y.Z (por ejemplo, 0.3.0).

   Este comando no funcionará si tenemos ficheros modificados en nuestra copia local del repositorio.

   El comando nos solicitará la siguiente información:
   
    * Número de versión con el que quedará congelado el código fuente del proyecto en el tag (release version).
      El número de versión que daremos al tag seguirá el formato X.Y.Z (por ejemplo, 0.3.0).
      Esta información será solicitada para cada módulo del proyecto, especificar la misma versión para todos ellos.
      
    * Número de versión que se va a dar al código fuente que va a seguir en desarrollo en la rama raíz (development version).
      El número de versión que daremos a la rama raíz seguirá la nomenclatura X.Y.Z-SNAPSHOT (por ejemplo, 0.4.0-SNAPSHOT).
      Esta información será solicitada para cada módulo del proyecto, especificar la misma versión para todos ellos.
      Si posteriormente vamos a crear una nueva rama (branch), dicha rama tomará como número de versión el que hemos asignado en este momento a la rama raíz y se nos solicitará una nueva versión para la rama raíz.   

   Por defecto, la generación del tag se realiza directamente en el servidor por lo que el tag no habrá sido descargado a nuestra máquina local.

Este proceso habrá realizado automáticamente las siguientes acciones:

    * Se habrá creado un nuevo *tag* en el repositorio que contendrá el código fuente congelado para poder generar el paquete.
    * El tag creado contendrá en los ficheros de control ``pom.xml`` el número de versión que hemos asignado al tag. 
    * Se habrán actualizado en la raíz (trunk) del proyecto los ficheros de control ``pom.xml`` con la nueva versión hacia la que va a evolucionar el desarrollo.
    * Se habrán creado unos ficheros xml para almacenar información sobre el estado de la generación de la *release*.

Generar el paquete binario de gvNIX
-----------------------------------

En esta sección se crea un paquete de distribución de una versión de gvNIX a partir del tag asociado a dicho número de versión.

La compilación y generación del paquete se hará a partir de una copia nueva de los fuentes obtenida desde el tag.
De esa forma estamos seguros de que lo generado será un reflejo fiel de lo que hay en ese tag.

#. Actualizar el directorio ``gvnix/tags`` para que descargue todos los tags existentes en el proyecto::

       bash> cd gvnix/tags
       bash> svn update

#. Entrar en el directorio del tag de la {versión} deseada::

       bash> cd {versión}

#. Empaquetar la versión de Roo parcheada::

       bash> mvn clean install assembly:assembly

#. Ejecutar el empaquetado del código de gvNIX::

       bash> cd ..
       bash> mvn clean install site assembly:assembly

   Esto nos habrá generado el fichero ``target/gvNIX-{version}.zip``

#. Subir el fichero a un repositorio FTP.
   Si no se desea subir automáticamente el fichero a un FTP remoto, copiar el fichero ``target/gvNIX-{version}.zip`` a un lugar seguro y saltar este paso.
   Para subir el fichero a un repositorio FTP debe configurarse previamente la dirección y los datos de acceso del FTP remoto tal como se indica en los anexos y a continuación ejecutar el siguiente comando::

       bash> mvn deploy:deploy-file

#. Si todo ha ido bien, limpiar la información sobre el empaquetado del directorio de la rama raíz::

       bash> cd gvnix/trunk/code
       bash> mvn release:clean

Generar el paquete de código fuente de gvNIX
--------------------------------------------

El proyecto, al tener licencia GPL requiere que, junto a los binarios, se publique el código fuentes de alguna forma, ya sea dando acceso al repositorio o empaquetado.

Como de momento no se va conceder acceso anónimo al repositorio, es necesario generar un fichero con los fuentes comprimidos.

El proceso definido en esta sección no es obligatorio, pero si que es necesario para cumplir con la licencia GPL del proyecto.

#. Crear un directorio temporal de trabajo. Por ejempo en el directorio temporal del sistema::
   
       bash> mkdir /tmp/gvnix-src
       bash> cd /tmp/gvnix-src

#. Exportar el tag de la distribución en el directorio temporal. Esto generará una copia de los fuentes *desvinculada* del repositorio::

       bash> svn export http://webdav.cop.gva.es/svn/gvnix/tags/{NOMBRE_DEL_TAG_DE_LA_RELEASE}

   Reemplazar ``{NOMBRE_DEL_TAG_DE_LA_RELEASE}`` por el nombre del tag de la release. Este sería un ejemplo para la versión 0.3.0::

       bash> svn export http://webdav.cop.gva.es/svn/gvnix/tags/0.3.0

   Al finalizar, nos habrá creado un directorio del mismo nombre que el tag::

       bash> ls 0.3.0
       bash>

#. Ajustar el nombre del directorio, añadiéndole el sufijo ``-src``::

       bash> mv 0.3.0 gvNIX-0.3.0-src

#. Empaquetar en un fichero ``.zip``::

       bash> zip -r gvNIX-0.3.0-src.zip gvNIX-0.3.0-src/

#. Copiar/enviar el fichero ``.zip`` a su destino.

#. Limpiar el directorio temporal de trabajo::

       bash> cd
       bash> rm -r /tmp/gvnix-src

Deshacer la generación de la distribución
-----------------------------------------

El comando ``mvn release:rollback`` deshace los cambios que se hubiesen hecho ante un error en cualquier punto del proceso excepto eliminar el *tag* creado del repositorio de fuentes si se hubiese llegado a generar, que habrá que hacer a mano.
Esto último está en una tarea que tienen marcada como pendiente en el plugin de maven release.
La condición para que funcione esto es que existan los ficheros xml de información que se generan durante el proceso.

Crear una rama para un desarrollo independiente
-----------------------------------------------

El comando utilizado para crear una rama (branch) para realizar un desarrollo independiente de la rama principal es el siguiente::

       bash> cd gvnix/trunk/code
       bash> mvn release:branch -DbranchName={nombre branch}
       
El formato del {nombre branch} seguirá la nomenclatura X.Y.Z (por ejemplo, 0.4.0).

Este comando no funcionará si tenemos ficheros modificados en nuestra copia local del repositorio.

Nos preguntará el número de versión que se va a dar al código fuente que va a seguir en desarrollo en la rama raíz (working copy version).
El número de versión que daremos a la rama raíz seguirá el formato X.Y.Z-SNAPSHOT (por ejemplo, 0.5.0-SNAPSHOT).
Tener precaución ya que nos está preguntando el nombre de la rama raíz y no el de la nueva rama.
La nueva rama tomará el número de versión que hubiese anteriormente en la rama raíz. 

Si queremos hacer un empaquetado a partir de esta rama, tendremos que descargarlo del repositorio y ejecutar todo el proceso de `Generar la distribución de una versión`_ desde él, en lugar de desde el trunk.

Anexo
=====

Esta sección describe las configuraciones que se han realizado en el proyecto gvNIX para facilitar el proceso de empaquetado y distribución descrito en este documento.

Toda la información aquí incluida ya ha sido realizada por el equipo de desarrollo de gvNIX, sin embargo, es interesante estar familiarizado con ella. 

Dentro del fichero ``code/pom.xml`` habrá que realizar varias configuracione tal como se detalla a continuación.

Configuración del empaquetado
-----------------------------

Repositorio de fuentes
'''''''''''''''''''''''''

Este es el repositorio de los fuentes actual. Es necesario para los procesos de creación de *tag* y *branch* ya que la copia se realiza en el propio servidor.

Este es un ejemplo de configuración::

    <scm>
        <connection>scm:svn:http://webdav.cop.gva.es/svn/gvnix/trunk/code</connection>
        <developerConnection>scm:svn:http://webdav.cop.gva.es/svn/gvnix/trunk/code</developerConnection>
    </scm>

El importante para el proceso es ``developerConnection`` y debe tener (para repositorios de tipo *SVN*) el prefijo ``scm:svn:`` más la URL de los fuentes.

URL base de los *tags* y *branches*
'''''''''''''''''''''''''''''''''''''

Esta es la URL donde el proceso creará el *tag* o el *branch*. Hay que utilizar las etiquetas ``tagBase`` y ``branchBase`` respectivamente. Un ejemplo es este::

          <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-release-plugin</artifactId>
                <version>2.0</version>
                <configuration>
                    <autoVersionSubmodules>true</autoVersionSubmodules>
                    <!-- this declaration is optional because we're using standart svn layout -->
                    <tagBase>
                        http://webdav.cop.gva.es/svn/gvnix/tags
                    </tagBase>
                    <!-- this declaration is optional because we're using standart svn layout -->
                    <branchBase>
                        http://webdav.cop.gva.es/svn/gvnix/branches
                    </branchBase>
                </configuration>
          </plugin>

Repositorio de *deploy*
''''''''''''''''''''''''''

Aquí es donde se configura donde se subirá el paquete de instalación una vez terminado. Un ejemplo de configuración::

    <distributionManagement>
        <repository>
            <id>gvNIX-releases</id>
            <name>gvNIX Releases</name>
            <url>ftp://ftp.releases.es</url>
        </repository>
    </distributionManagement>

Para configurar el resto de parámetro de la subida hay que ajustar la configuración del plugin *release*. Este es un ejemplo::

            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-deploy-plugin</artifactId>
                <version>2.4</version>
                <configuration>
                       <file>${project.build.directory}/${project.name}-${project.version}.zip</file>
                        <repositoryId>gvNIX-releases</repositoryId>
                        <groupId>gvNIX-install</groupId>
                        <artifactId>gvNIX</artifactId>
                        <version>${project.version}</version>
                        <url>ftp://ftp.releases.es/releases</url>
                        <packaging>zip</packaging>
                 </configuration>
            </plugin>

Hay que tener en cuenta los siguientes parámetros:

* El ``repositoryId`` debe coincidir con el ``repository/id`` especificado en el parrafo anterior.

* La ``url`` debe ser el directorio raíz donde dejar le paquete.

Con el ejemplo anterior en el servidor se creará para cada realease::

     ftp://ftp.releases.es/releases/gvNIX/gvNIX-install/{version}/gvNIX-{version}.zip

Para poder subir los ficheros hay que configurar el usuario y password en el fichero ``$HOME/.m2/settings.xml``. Un ejemplo de este fichero es::

        <settings>
          <servers>
            <server>
              <id>gvNIX-releases</id>
              <username>user</username>
              <password>pass</password>
            </server>
          </servers>
        </settings>

Hay que hacer coincidir el ``id`` con el ``id`` del repositorio para el *deploy*.

El password debe estar en texto plano (limitación de plugin de maven). Para mas seguridad podemos configurar los permisos del fichero para solo lectura del propietario.
