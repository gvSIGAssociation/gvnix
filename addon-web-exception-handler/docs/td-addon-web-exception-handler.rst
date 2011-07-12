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

Requirements
=============

Add-on to handle Exceptions and show a friendly message to the user through a .jspx view.

* Add a new Exception to be handled by the application.
* Show the Exceptions handled by the application.
* Remove a Exception handled by the application.
* Adds a new translation of the exception message to the application.

Use Case
=========

This Add-on is developed to show a friendly Exception message to the user when is caught an Exception in the application using a .jspx view

Analysis
=========

Analysis for the development of the Add-on displayed by commands::

web mvc exception list - List the Exceptions handled::

  * Shows the handled Exceptions that are listed in the SimpleMappingExceptionResolver bean.

web mvc exception add - Add a new Exception::

  * Creates the new mapping in the SimpleMappingExceptionResover bean in webmvc-config.xml.
  * Creates the jspx and defines it in the views.xml file.
  * Creates the multilanguage tags in the messages**.properties files. Set the title and description in the selected language.

web mvc exception remove - Removes an existing Exception::

  * Removes the definition of the Exception in the SimpleMappingExceptionResolver bean in webmvc-config.xml.
  * Removes the .jspx view and its definition in views.xml file.
  * Remove the multilanguage tags in all the messages**.properties files.

web mvc exception set language - Updates the messages of the Exception in the selected language::

  * Checks if the Exception exists.

    * Updates the tag of the messages**.properties file.

Since version 0.8.0 Exceptions are shown in a Modal Dialog view. This Modal Dialog takes the jspx defined and includes it as part of the view.

Commands
=========

There are defined four commands in this Add-on:

web mvc exception list - List the Exceptions handled::

web mvc exception add - Add a new Exception::

  Parameters: --excepcion Name of the exception e.g. java.lang.Exception,  --title Title of the exception, --description Description of the exception to show in the view and --language The language of the messages [es, en... etc].

web mvc exception remove - Removes an existing Exception::

  Parameters: --excepcion Name of the exception e.g. java.lang.Exception.

web mvc exception set language - Updates the messages of the Exception in the selected language::

  Parameters: --excepcion Name of the exception e.g. java.lang.Exception,  --title Title of the exception, --description Description of the exception to show in the view and --language The language of the messages [es, en... etc].

web mvc exception setup - Creates a Set of defined exceptions for gvNix

Proof of Concept
================

* http://scmcit.gva.es/svn/gvnix-proof/trunk/petclinic-modal-dialogs
* http://scmcit.gva.es/svn/gvnix-proof/trunk/exception-error-handler
* https://svn.disid.com/svn/disid/proof/gvnix/exception-handler-app
* https://svn.disid.com/svn/disid/proof/spring_roo/exceptions-FASE2
* https://svn.disid.com/svn/disid/proof/spring_roo/exceptions-FASE1
* https://svn.disid.com/svn/disid/proof/spring_roo/exceptions

Notes
=======

Try to update views.xml file using ``TilesOperations`` service::

    @Reference
    private TilesOperations tilesOperations;
    .
    .
    .
    tilesOperations.addViewDefinition("", "exception", TilesOperations.DEFAULT_TEMPLATE, jspxPath);

Instead of using the Transformer provided by XmlUtils.


TODO
=====

* Added a new command "web mvc dialog add" that installs message-box.tagx, a new jspx as sample of content of a modal dialog
  and generate an ITD with a helper method to show a new modal dialog in view.
  TODO: Maybe this method will be moved to another add-on
