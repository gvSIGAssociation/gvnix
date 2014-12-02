---
layout: home
title: gvNIX, Spring Roo based RAD tool for Java developers
permalink: en/index.html
lang: en


badges:

  # Customize the documentation links. Delete any entries that do not apply.

  releases:
    - name: 1.4.0
      guide: gvnix-1.4-0-reference-docs.pdf
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
    - name: gvNIX quick start application
      url: https://github.com/DISID/gvnix-samples/tree/master/quickstart-app
    - name: gvNIX quick start GEO application
      url: https://github.com/DISID/gvnix-samples/tree/master/quickstart-geo-app
    - name: Installing gvNIX <i>(Video)</i>
      url: http://www.youtube.com/watch?v=3SxfUcIy2sM


  links:
    - name: Executable code
      url:  http://sourceforge.net/projects/gvnix/files/gvNIX-1.4.0.RELEASE.zip/download
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

TBC

## No runtime dependency

If you develop applications using Spring Roo, 

At runtime gvNIX applications are only dependent on frameworks that you used for developing the application, nothing more, nothing less.

## Command Interpreter

From the point of view of its use, gvNIX is designed as an interactive
shell-style Rails or Grails.

For ease of use has autocomplete commands and context help.

## User interface responsive

Integrates web development frameworks, including [Bootstrap](http://getbootstrap.com/),
in the application to generate the responsive view with HTML5 structure and CSS3
that is automatically adapted for display in multiple devices: tablets,
phones, laptops, PCs...

In addition, these development frameworks using web standards in such a way that
set accessible websites bases for people who use assistive technologies
to navigate.

## Spring MVC - Integration of jQuery

Converts projects based on Dojo in projects on jQuery UI.

Adds support for form validation and optimizes the JavaScript code included in
the HTML document.

## Integration of Dandelion DataTables

Integrates more dynamic and functional components: pagination,
global search, column filtering, sorting, various sources of
Data: AJAX, DOM, etc;

Allows the register mode view, inline edit, editing and
deleting multiple,  set predefined of operations,
record created in first position, selection always visible...

Create patterns and details
[Dandelion DataTables] (http://dandelion.github.io/datatables/)

## Loupe component

Allows to use loupe type components in applications. Thanks to these
components, we can easily find records of fields related
without viewing all data in a dropdown.

## Master detail multilevel

Allows you to define display patterns on entities and their relationships:
allowing any combination [Master-tabular | master-registration] with
[Detail-tabular | detail-registration], with no limit on the number of relationships
both direct and indirect.

## Production performance monitoring

Integrates a monitoring system for Web applications in production.

The necessary infrastructure is created to record runtimes for
the different elements of the application: generation of view, SQL queries,
execution methods, HTTP request.

Also is created a page where we can see statistics data collected.

## Filter Wizard

Filtering systems table data allow not only compare text
plane but to define filtering operations with * CONTAINS () *,
*> = *, * DATE () *, etc.

To enable filtering by column automatically integrates an assistant
which allows the user to choose the filtering operation per column.

## Geographic Component

gvNIX automatically generates pages for display, listing,
search, create and edit alphanumeric data and also incorporates the
view on a map of the same data.

Allows editing of the location of elements, the location of data that
being edited by selecting a point on a map.

Allows the generation of geoportals. Any gvNIX application may incorporate a
geoportal in which all data are managed from the display
application as different layers as well as integration with the rest of
application pages.

<br>

<div class="pull-left">
<span class="fa-stack fa-3x">
<i class="fa fa-circle fa-stack-2x text-primary"></i>
<i class="fa fa-desktop fa-stack-1x fa-inverse"></i>
</span>
</div>

# Technology

gvNIX lets you build the data model of an application using forward and reverse
engineer and generate a management application (web or not) in multiple
display technologies (Spring MVC, Flex, JSF, Vaddin ..)

gvNIX has been implemented with Java language and follow a model of
component-oriented architecture of OSGi platform in which each functionality
that provides the framework is implemented as a component or add-on that works
with the rest in different generation tasks.

It consists of a set of open source tools in particular its core, Spring Roo.
The technology includes gvNIX is most often used today with 40% of the study
population uses as Spring MVC framework technology.

<br>

<div class="pull-left">
<span class="fa-stack fa-3x">
<i class="fa fa-circle fa-stack-2x text-primary"></i>
<i class="fa fa-code fa-stack-1x fa-inverse"></i>
</span>
</div>

# Open Source Project

Tool free and open source software based on Spring Roo.
This gives you a wide support from important organizations like
Spring Source and VMWare.

## Benefits

* The evolution of applications is not blocked by the evolution of gvNIX.
Integrating new functionality in an application does not require previously
be added to gvNIX.

* Fix incidences in the application does not depend on gvNIX,
can be solved in the application itself and then reports to
gvNIX project to include as improvement in future versions.

<br>

<div class="pull-left">
<span class="fa-stack fa-3x">
<i class="fa fa-circle fa-stack-2x text-primary"></i>
<i class="fa fa-code-fork fa-stack-1x fa-inverse"></i>
</span>
</div>

# Community gvNX

Know, modify and redistribute the gvNIX source code.
The tool evolves, develops and improves continuously thanks
to the contributions of our developer community.

You can do get your requests, request support and suggestions that you
 consider necessary for improvements through multiple communication methods:

* Issues / Bugs in [Stackoverflow] (http://stackoverflow.com/questions/tagged/gvnix).
   Remember to use the label #gvnix or # gvnix-es

* [GVA Mailing List] (http://listserv.gva.es/cgi-bin/mailman/listinfo/gvNIX_soporte)

Of course you will find us on social networks either following a '@gvNIX'
or including the `#gvnix` in your tweets.

