---
layout: post
anniversary: false
timeline: false
title: gvNIX en las 11as Jornadas Internacionales de gvSIG
description: Presentación y taller de gvNIX en las 11gvSIG
tags: [gvNIX, gvSIG, Opensource, Formación]
comments: true
featured: true
training: true
type: photo
imagefeature: post/11gvsig/gvnix-11gvsig-es.png
share: true
category:
    - es
lang: es
---

Un año más estaremos en las Jornadas Internacionales de gvSIG,
que se celebrarán del 2 al 4 de diciembre en el Complejo Deportivo-Cultural Petxina (Valencia - España).

La Asociación gvSIG y el equipo de desarrollo de gvNIX de [DISID Corporation](http://www.disid.com)
presentaran la herramienta gvNIX en las jornadas participando activamente con talleres y ponencias.

La presentación **gvNIX:  Nuevas funcionalidades** será el jueves 3 de diciembre a las 11:00h;
el taller sobre **gvNIX: Desarrollo rápido de Geoportales para visualización y gestión de datos**
se impartirá el mismo día 3 de diciembre de 15.10h a 18h.

<div class="row">
  <div class="col-md-offset-3 col-md-6 col-xs-12">
  <figure>
    <img src="{{ site.url }}/images/post/11gvsig/gvnix-11gvsig-es.png" />
  </figure>
  </div>
</div>


## Taller gvNIX: Desarrollo rápido de Geoportales para visualización y gestión de datos

En el taller nos centraremos en esta componente GEO, especialmente orientado a desarrolladores.
En el taller veremos como:

* Configurar un proyecto para trabajar con componentes GEO
* Crear campos GEO
* Transformar capa web de campos GEO para guardar datos
* Demostración de creación, actualización y visualización de entidades con campos GEO
* Generar mapa vacío
* Añadir entidades al mapa
* Añadir nuevas capas al mapa (Tiles y WMS)
* Añadir nuevas herramientas al mapa (Personalizadas e incluídas en gvNIX)
* Añadir nuevos componentes al mapa
* Configuración de capas (filtrable, seleccionable, icono, colores, etc…)
* Demostración de filtrado y selección utilizando componente Datatables

### Requisitos previos

Para los que quieran asistir al taller **los requisitos previos** son:

* Tener instalado STS y el entorno de desarrollo (Maven, distribución de gvNIX)
  * JDK 7: <a href="http://www.oracle.com/technetwork/java/javase/downloads/jdk7-downloads-1880260.html">http://www.oracle.com/technetwork/java/javase/downloads/jdk7-downloads-1880260.html</a>
  * Maven 3.0.5 o superior: <a href="http://maven.apache.org/download.cgi">http://maven.apache.org/download.cgi</a>
  * STS 3.6: <a href="https://spring.io/tools/sts/all">https://spring.io/tools/sts/all</a>
  * Tomcat 7.0.65: [http://ftp.cixug.es/apache/tomcat/tomcat-7/v7.0.65/bin/apache-tomcat-7.0.65.zip](http://ftp.cixug.es/apache/tomcat/tomcat-7/v7.0.65/bin/apache-tomcat-7.0.65.zip)
  * gvNIX 1.5.1: [http://sourceforge.net/projects/gvnix/files/gvNIX-1.5.1.RC4.zip/download](http://sourceforge.net/projects/gvnix/files/gvNIX-1.5.1.RC4.zip/download)
  * Guía instalación gvNIX: <a href="https://github.com/DISID/gvnix-samples/blob/master/INSTALL-gvNIX-1.x.adoc" target="_blank"> https://github.com/DISID/gvnix-samples/blob/master/INSTALL-gvNIX-1.x.adoc</a>

* Tener instalado POSTGRESQL y la extensión espacial POSTGIS
  * PostgreSQL 9.3: <a href="http://www.postgresql.org/download/">http://www.postgresql.org/download/</a>
  * PostGIS 2: <a href="http://postgis.net/install" rel="nofollow">http://postgis.net/install</a>

* Tener creada la base de datos espacial
  * Abrir aplicación pgAdmin.
  * Sobre PostgreSQL 9.x hacer click derecho y seleccionar Connect.

  <div class="col-md-12">
  <figure>
    <img src="{{ site.url }}/images/post/prerequisites/01pgadmin-connect.png">
  </figure>
  </div>

* Crear usuario petclinic. En Login Role hacer click derecho, seleccionar New Login Role… y
crear el usuario petclinic con la siguiente información:
  * Properties > Role Name: petclinic
  * Definition > Password: petclinic

<div class="col-md-12">
<figure>
  <img src="{{ site.url }}/images/post/prerequisites/02pgadmin-new-role.png">
</figure>
</div>

* En Database > New Database crear una base de datos petclinic con la siguiente información:
  * Name: petclinic
  * Login role : petclinic

<div class="col-md-12">
<figure>
  <img src="{{ site.url }}/images/post/prerequisites/03pgadmin-new-database.png">
</figure>
</div>

* To add the spatial extent in the database, select the postgis option in petclinic > Extensions > new Extension.

<div class="col-md-12">
<figure>
  <img src="{{ site.url }}/images/post/prerequisites/04pgadmin-new-extension.png">
</figure>
</div>

* Tener generada la aplicación base
  * Crear proyecto petclinic. Desde el STS, seleccionar “File > New > Other > Spring Roo Project“ y rellenar la siguiente información
    * Package : org.gvnix.petclinic
    * Project : petclinic

    <div class="col-md-12">
    <figure>
      <img src="{{ site.url }}/images/post/prerequisites/05create-new-project.png">
    </figure>
    </div>

  * Automáticamente se abrirá la consola de gvNIX
  * Descargar el script del proyecto desde [https://github.com/DISID/gvnix-samples/blob/master/quickstart-app/quickstart.roo]( https://github.com/DISID/gvnix-samples/blob/master/quickstart-app/quickstart.roo)
  * Ejecutar script en la consola de gvNIX script –file quickstart.roo

* Tener descargado el archivo .m2.zip: [https://drive.google.com/file/d/0B6EMTWvnFZHfZUxQd1JBN25NTEU/view?usp=sharing](https://drive.google.com/file/d/0B6EMTWvnFZHfZUxQd1JBN25NTEU/view?usp=sharing)

La [inscripción](http://www.gvsig.com/es/eventos/jornadas-gvsig/11as-jornadas-gvsig/inscripcion) continua abierta,
y como en todo evento gvSIG es gratuita, siendo el aforo limitado.

En la página web de las Jornadas podéis consultar todo el [programa](http://www.gvsig.com/es/eventos/jornadas-gvsig/11as-jornadas-gvsig).

¡Os esperamos!