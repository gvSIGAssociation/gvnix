===================================================================
 gvNIX Service Layer Add-on
===================================================================

:Project:   gvNIX. Spring Roo based RAD tool
:Copyright: Conselleria d'Infraestructures i Transport - Generalitat Valenciana
:Author:    [ ... ]
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

Introduction
===============

This project generates the addon to generate and manage service classes and export and import to web services.

Features
===========

TBC

This are features contained:

Development
============

Create two Operation classes, one for service layer management ``ServiceLayerOperationsImpl`` and another for manage the web service layer ``WebServiceLayerOperationsImpl``.

Commands
---------

* service class --class:

* service operation --class clase --name nombreOperacion --return clase:

* service parameter --class clase --method nombreOperacion --name nombreParametro --type clase:

* service export ws --class clase --name nombreServicio:

* service export operation ws --class clase --method nombreMetodoEntidad --name nombreAPublicar: 

* service import ws --endPoint urlOPropiedad --wsdl url2wsdl.xml:

* service entity --class nombreClase:

Proof of Concept
=================

TBC

How to install
===============

TBC