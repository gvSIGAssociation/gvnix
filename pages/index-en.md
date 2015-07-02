---
layout: home
title: gvNIX, Spring Roo based RAD tool for Java developers
permalink: en/index.html
lang: en


badges:

  # Customize the documentation links. Delete any entries that do not apply.

  releases:
    - name: 2.0.0.M1
      guide: gvnix-2.0.0.M1-reference-docs
    - name: 1.4.1
      guide: gvnix-1.4.1-reference-docs.pdf
    - name: 1.4.0
      guide: gvnix-1.4.0-reference-docs.pdf
    - name: 1.3.1
      guide: release-1.3.1-index.pdf
    - name: 1.3.0
      guide: release-1.3.0-index.pdf
    - name: 1.2.0
      guide: release-1.2.0-index.pdf
    - name: 1.1.0
      guide: release-1.1.0-index.pdf
    - name: 1.0.0
      guide: release-1.0.0-index.pdf

  samples:
    - name: Petclinic
      url:  http://petclinic-gvnix.rhcloud.com/
      url-code: https://github.com/DISID/gvnix-samples/tree/master/quickstart-app
      icon: globe
    - name: Geo
      url:  http://geo-gvnix.rhcloud.com/
      url-code: https://github.com/DISID/gvnix-samples/tree/master/quickstart-geo-app
      icon: globe

  additional:
    - name: gvNIX install guide (version 1.x.)
      url: https://github.com/DISID/gvnix-samples/blob/master/INSTALL-gvNIX-1.x.adoc
    - name: gvNIX install guide (version 2.x.)
      url: https://github.com/DISID/gvnix-samples/blob/master/INSTALL-gvNIX-2.x.adoc
    - name: gvNIX quick start application
      url: https://github.com/DISID/gvnix-samples/tree/master/quickstart-app
    - name: gvNIX quick start GEO application
      url: https://github.com/DISID/gvnix-samples/tree/master/quickstart-geo-app
    - name: gvNIX styleguide <i>(Spanish)</i>
      url: /styleguide/gvnix-guia-estilo.html
    - name: Installing gvNIX (version 1.x.) <i>(Video)</i>
      url: /es/video-install-gvnix/
    - name: How to work with gvNIX <i>(Video)</i>
      url: http://youtu.be/wS8oLfRZY54
    - name: Geoportal in gvNIX <i>(Video)</i>
      url: /en/video-geoportal-gvnix/
    - name: Video tutorial de gvNIX <i>(Spanish video)</i>
      url: /en/video-taller-gvnix-jornadas-gvsig/


  links:
    - name: Executable code
      url:  http://sourceforge.net/projects/gvnix/files/
      icon: arrow-circle-down

    - name: Source code
      url:  https://github.com/DISID/gvnix
      icon: github

    - name: Support
      url:  http://stackoverflow.com/questions/tagged/gvnix
      icon: stack-overflow
---

gvNIX is an open source tool for rapid application development (RAD) with which you can create Java web applications in minutes.

It is a distribution of <a href="http://projects.spring.io/spring-roo/">Spring Roo</a> that provides the set of Spring Roo tools plus a suite of features that increase development productivity and improve the user experience by integrating frameworks like jQuery, Bootstrap 3, Leaflet, DataTables, Dandelion DataTables, among other.

<p class="lead text-center">gvNIX = productivity</p>
<p class="text-center">Create web applications in minutes.</p>
<br/>

# Features

## Multiplatform and easy to install

gvNIX is easy to install as a standalone tool that works
on Windows, Mac OSX, Linux or as an integrated tool environment
Development: STS or Eclipse.

The only requirements are Java (6 or higher) SDK and Apache Maven 3.

## Non-intrusive code generation

It takes on a different approach to code generation compared to other solutions. Instead of generating additional .java files (through the <a href="http://martinfowler.com/dslCatalog/generationGap.html">Generation Gap Pattern</a>), it generates so-called AspectJ inter-type declaration (ITD) .aj source files.

Each generated inter-type declaration (ITD) type will "weave in" structural changes to its target class in compilation-time; for example to add new methods, attributes, etc.

## Standard JEE projects

Projects created with gvNIX are Java applications that conform to the
JEE standard.

## No runtime dependency

At runtime gvNIX applications are only dependent on frameworks that you used for developing the application, nothing more, nothing less.

## Command Interpreter

gvNIX is designed to use it as an interactive shell-style Rails or Grails.

For ease of use has autocomplete commands and context help.

<div class="text-center">
<figure>
  <img src="{{ site.url }}/images/gvnix-shell-eclipse.png" class="responsive"/>
</figure>
</div>

# Functionalities

## Export/Import Web Services

gvNIX automatically generates web services from Java code and from WSDL files.

It also automatically generates web service clients stating
the URL where the WSDL file is published. This generates *Stub* classes in our
application to make possible that the other classes invoke these remote services
as local services.

## Reports

gvNIX installs JasperReports to generate reports.

Each report is accessible from application menu and generates a previous form to specify the filter parameters.

All reports are fully functional since its inception, including the generation of .jrxml template for comfortable and easy customization.

## Database Reverse Engineering

gvNIX allows to create a complete Java entity model using database introspection. In addition, gvNIX maintains the Java entity model synchronized with all changes in the data model.

## Non-intrusive Optimistic Concurrency Control

In multi user environments, such as web applications, it is common that two users access to the same record at the same time to edit it. Concurrency control can prevent that some action interfere with other.

Most accepted concurrency control pattern in web environments is known
as optimistic concurrency control. Most common way to implement it is to use a version field to be included in all tables in the data model.

In large organizations the data model follows very strict security policies and is frequent that you can't add new version fields.

gvNIX provides an implementation of optimistic concurrency control *based on the objects state*, equally effective but non-intrusive.

## Audit changes on Database

gvNIX adds support on applications to make audit of data changes of the
domain entities: saves who and when it creates or modifies an instance,
or in terms of database who and when modifying a record.

## Historical changes on Database

This functionality lets you to store the history of changes made to the entities. You can retrieve and query historical data without much effort: what, whom and when a change was done, including record deletion.

## Responsive UI

gvNIX integrate web development frameworks, including [Bootstrap](http://getbootstrap.com/), in the application to generate responsive UIs with HTML5 and CSS3 structure. Applications are automatically adapted by itself for displaying multiple devices: tablets, mobile, laptops, PCs...

In addition, these development frameworks use web standards. That makes all web applications generated using gvNIX  are accessible to people who use assistive technologies to navigate.

<div class="text-center">
<figure>
  <img src="{{ site.url }}/images/responsive.png" class="responsive"/>
</figure>
</div>

## jQuery integration

gvNIX can migrate projects based on *Dojo* to projects based on *jQuery UI*.

It adds support for form validation and optimize JavaScript code
included in the HTML document.

## Advanced Visual Components

### Loupe Component

gvNIX allows you to use loupe components in applications. Thanks to these
components, we can search records in a simple way of related fields
without having to display all the data in a drop-down.

### Dandelion Datatables

gvNIX integrates more dynamic and more functional tables: paging,
overall, filter by column, sorting, various sources of
data: AJAX, DOM, etc;

This component allows register mode viewing, Online Edition, multiple edition and
deletion, predefined set of operations,
recent created record on first row, always visible selection...

Create patterns and details with
[Dandelion Datatables](http://dandelion.github.io/datatables/)

## Multilevel Master-Detail

gvNIX allows you to define viewing patterns on entities and their relationships:
allowing any combination [master-list | master-registry] with
[detail-list | detail-registry], no limit on the number of relations
both direct and indirect.

## Monitoring performance in production

It integrates a monitoring system for web applications in production.

gvNIX creates the necessary infrastructure to record execution times of
application elements: view generation time, SQL queries,
implementation of methods, HTTP request...

Also, it creates a page from where you can see statistics of collected data.

## Filter Wizard

Table filtering data systems not only allow to compare text, gvNIX allows you to define filtering operations such as *CONTAINS()*,*&gt;=*, *DATE()*, etc.

gvNIX integrates a column filter wizard that allows you to select some of the previous functions and helps you to implement it.

## Geographic component

gvNIX automatically generates pages to display, list,
Search, creation and editing of alphanumeric data and geospatial data.

It allows you to integrate on a same application alphanumeric and geographic information that users can exploit such information without using other specific applications.

gvNIX automatilcally generates independent geoportals and/or application integrated geoportals.

<div class="text-center">
<figure>
  <img src="{{ site.url }}/images/geo-desktop.png" class="responsive"/>
</figure>
</div>

# Technology

<div class="text-center">
<figure>
  <img src="{{ site.url }}/images/tecno.png" class="responsive"/>
</figure>
</div>

<br/>

<div class="pull-left">
<span class="fa-stack fa-3x">
<i class="fa fa-circle fa-stack-2x text-primary"></i>
<i class="fa fa-code-fork fa-stack-1x fa-inverse"></i>
</span>
</div>

# gvNIX Community

Known, modify and redistribute the source code of gvNIX.

Feel free to help us to evolve, develop and improve gvNIX.

You can send your support requests related to gvNIX, as well as the suggestions that you consider appropiate for its improvement to [Stackoverflow](http://stackoverflow.com/questions/tagged/gvnix). Remember to use the tag #gvnix or #gvnix-es.

Of course you will find us on the social networks either according to '@gvNIX' or including the `#gvnix` tag in your tweets.

