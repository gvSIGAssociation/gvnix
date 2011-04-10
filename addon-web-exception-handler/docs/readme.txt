===================================================================
 gvNIX Uncaught Exception Handler Add-on
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

This project generates the addon to catch unhandled exceptions in controllers and show an intelligible message to the browser.

Features
===========

This are features contained:

  - Creates an Exception to handle and the depending view of the exception.
  - Removes a selected Exception from the application.
  - Lists all the handled Exception of the application.
  - Adds a new translation in a determinate language for an Exception.

Proof of Concept
=================


How to install
===============

#. Download and setup `Spring Roo <http://www.springsource.com/download/community?project=Spring%20Roo>`_ .

#. Change to cxf addon directory

#. Run the Roo console

#. Execute the command ``perform assembly`` to build the addon

#. Execute the command ``addon install --url file:{absolute_path_to_zip_generated_from_assembly}`` to install the addon

That's all.

To remove the addon execute ``addon uninstall --pattern {name_of_addon_zip_file_without_path}`` in any Roo console

