---
layout: post
anniversary: false
timeline: false
title: gvNIX in 11th International gvSIG Conference
description: Presentation and workshop of gvNIX at 11gvSIG
tags: [gvNIX, gvSIG, Opensource, Training]
comments: true
featured: true
type: photo
imagefeature: post/11gvsig/gvnix-11gvsig-en.png
share: true
category:
    - en
lang: en
---

Once again this year, gvNIX will be present at the International Conference gvSIG,
that will be held from December 2nd to 4th at La Petxina Sports-Cultural Complex (Valencia – Spain)

The Association gvSIG and the [DISID Corporation](http://www.disid.com) gvNIX development team
will be presented the gvNIX tool to participate actively in the conference with workshops and presentations.

The presentation **gvNIX: New features** will be Thursday December 3rd at 11:00am local time;
**gvNIX workshop: Geoportals for viewing and data management rapid development**
will be held on 3rd December from 15.10h to 18h local time.

<div class="row">
<div class="col-md-offset-3 col-md-6 col-xs-12">
<figure>
  <img src="{{ site.url }}/images/post/11gvsig/gvnix-11gvsig-en.png">
</figure>
</div>
</div>

## gvNIX workshop: Geoportals for viewing and data management rapid development

In this workshop we will focus on GEO component, especially oriented to developers.
In the workshop will see how:

* Set up a project to work with GEO components
* Create GEO fields
* Transform web layer GEO fields to store data
* Demonstration of creating, updating and visualization of entities with GEO fields
* Generate empty map
* Add entities to map
* Add new layers to the map (Tiles and WMS)
* Add new tools to map (Custom and included in gvNIX)
* Add new components to map
* Setting layers (filterable, selectable icon colors, etc ...)
* Demonstration using filtering and selection Datatables component

### Prerequisites

For those who want to participate in the workshop **the prerequisites** are:

* Installed STS and the development environment (Maven, gvNIX distribution)
  * JDK 7: <a href="http://www.oracle.com/technetwork/java/javase/downloads/jdk7-downloads-1880260.html">http://www.oracle.com/technetwork/java/javase/downloads/jdk7-downloads-1880260.html</a>
  * Maven 3.0.5 or higher: <a href="http://maven.apache.org/download.cgi">http://maven.apache.org/download.cgi</a>
  * STS 3.6: <a href="https://spring.io/tools/sts/all">https://spring.io/tools/sts/all</a>
  * Tomcat 7.0.65: [http://ftp.cixug.es/apache/tomcat/tomcat-7/v7.0.65/bin/apache-tomcat-7.0.65.zip](http://ftp.cixug.es/apache/tomcat/tomcat-7/v7.0.65/bin/apache-tomcat-7.0.65.zip)
  * gvNIX 1.5.1: [http://sourceforge.net/projects/gvnix/files/gvNIX-1.5.1.RC1.zip/download](http://sourceforge.net/projects/gvnix/files/gvNIX-1.5.1.RC1.zip/download)
  * Installation guide gvNIX: <a href="https://github.com/DISID/gvnix-samples/blob/master/INSTALL-gvNIX-1.x.adoc" target="_blank"> https://github.com/DISID/gvnix-samples/blob/master/INSTALL-gvNIX-1.x.adoc</a>

* Installed PostgreSQL and PostGIS spatial extension
 * PostgreSQL 9.3: <a href="http://www.postgresql.org/download/">http://www.postgresql.org/download/</a>
 * PostGIS 2: <a href="http://postgis.net/install" rel="nofollow">http://postgis.net/install</a>

* To have created the spatial database
  * Open Application pgAdmin.
  * On PostgreSQL 9.x right click and select Connect
<div class="col-md-12">
<figure>
  <img src="{{ site.url }}/images/post/prerequisites/01pgadmin-connect.png">
</figure>
</div>

* Crear usuario petclinic. En Login Role hacer click derecho, seleccionar New Login Role… y
crear el usuario petclinic la siguiente información:

* Create Petclinic user. On Login Role, right click, select New Login Role... and create the user petclinic with the following information:
  * Properties > Role Name: petclinic
  * Definition > Password: petclinic
<div class="col-md-12">
<figure>
  <img src="{{ site.url }}/images/post/prerequisites/02pgadmin-new-role.png">
</figure>
</div>

* En Database > New Database create a petclinic database with the following information:
  * Name: petclinic
  * Login role : petclinic
<div class="col-md-12">
<figure>
  <img src="{{ site.url }}/images/post/prerequisites/03pgadmin-new-database.png">
</figure>
</div>

* Para añadir la extensión espacial en la base de datos, seleccionar la opción postgis en petclinic > Extensions > new Extension.
<div class="col-md-12">
<figure>
  <img src="{{ site.url }}/images/post/prerequisites/04pgadmin-new-extension.png">
</figure>
</div>

* Be generated the base application
  * Create petclinic project. Since the STS, select "File > New > Other > Spring Roo Project" and fill in the following information
    * Package : org.gvnix.petclinic
    * Project : petclinic
    <div class="col-md-12">
    <figure>
      <img src="{{ site.url }}/images/post/prerequisites/05create-new-project.png">
    </figure>
    </div>
  * gvNIX console automatically opens.
  * Download the script of the project from [https://github.com/DISID/gvnix-samples/blob/master/quickstart-app/quickstart.roo]( https://github.com/DISID/gvnix-samples/blob/master/quickstart-app/quickstart.roo)
  * Run script in the console gvNIX -file script quickstart.roo

* Be downloaded the file .m2.zip: [https://drive.google.com/file/d/0B6EMTWvnFZHfZUxQd1JBN25NTEU/view?usp=sharing](https://drive.google.com/file/d/0B6EMTWvnFZHfZUxQd1JBN25NTEU/view?usp=sharing)

The [Registration](http://www.gvsig.com/en/events/gvsig-conference/11th-gvsig-conference/registration) remains open,
and as in all gvSIG event is free, with places are limited.

The program is available with the paper sessions and workshops on
the [Conference web page](http://www.gvsig.com/en/web/guest/events/gvsig-conference/11th-international-gvsig-conference).

We wait for you!
