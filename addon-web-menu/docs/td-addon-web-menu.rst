=========================================================
 gvNIX - Spring Roo - Addon - Web MVC Menu
=========================================================

-------------------
 Technical design
-------------------

:Project:   gvNIX. Spring Roo based RAD tool
:Copyright: Conselleria d'Infraestructures i Transport - Generalitat Valenciana
:Author:    DiSiD Technologies, S.L.
:Revision:  $Rev$
:Date:      $Date$

This work is licensed under the Creative Commons Attribution-Share Alike 3.0    Unported License. To view a copy of this license, visit
http://creativecommons.org/licenses/by-sa/3.0/ or send a letter to
Creative Commons, 171 Second Street, Suite 300, San Francisco, California,
94105, USA.

.. contents::
   :depth: 2
   :backlinks: none

.. |date| date::

Introduction
===============

This Roo add-on lets you to manage the application structure from Roo shell.

Requirements
=============

This add-on must provide commands to manage the application structure for Roo projects:

* Show menu tree
* Add/remove/update menu entries
* Show/hide menu entries
* Organize menu entries (categories, subcategories, ...)
* support context menus

Additional features could be:

* Synchronize menu roles definition and Spring Security annotations/definitions.
* DB storage for application structure
* Spring EL (expressions) support for menu entries visibility

Analysis
=========

Application structure model
----------------------------

Runtime classes to load the application structure that is stored in an XML file, *menu.xml*.

Java files will be created in target project. Add-on contains the file templates needed to generate them in ``~.web.menu``

Runtime model is composed by:

* `MenuLoader.java`_: Loads application structure into Menu instance and put it in application *ServletContext*.
* `Menu.java`_      : Represents the application structure and information.
* `MenuItem.java`_  : Represents an application page or an application pages group/category.

Context menu
~~~~~~~~~~~~~~

``ContextMenuStrategy`` interface defines an strategy that lets the application to decide which items show in the menu depending on current context.

Currently there are two default implementations: ``URLChildrenContextMenuStrategy`` and ``URLBrothersContextMenuStrategy``. 

* ``URLChildrenContextMenuStrategy``. This strategy decides which menu item (root, subcategory, etc) should act as root by matching current request URL with all menu entries target URLs. If a match item is found the menu will render the children of the match menu entry.
* ``URLBrothersContextMenuStrategy``. This strategy decides which menu item (root, subcategory, etc) should act as root by matching current request URL with all menu entries target URLs. If a match item is found the menu will render the children of the match menu entry parent, that is, it will render its brothers.

To create new strategies, implement ``ContextMenuStrategy`` and annotate as Spring beans ``@Component`` and ``@Configurable``.

Web artifacts
--------------

* *gvnixmenu.tagx* to render menu from `Application structure model`_. This tag renders the menu iterating all menu entries and taking in account the context menu strategy.
* *gvnixitem.tagx* renders a menu entry. Checks if Spring Security is installed to decide what version of tag has to use: with or without security checks.
* */WEB-INF/view/menu.jspx* will be modified to use *gvnixmenu.tagx*. The contained structure info is moved to *menu.xml*.

Roo MenuOperations implementantion
-----------------------------------------

After move the structure info to *menu.xml*, Roo components could manage web pages via *MenuOperations* component causing new info will added to *menu.jspx*. To centralize page info, the add-on has their own *MenuOperations* implementation and disable the *MenuOperationsImpl* that Roo provides by default.

Features roadmap
---------------------

Features below should be implemented in future releases.

Spring Security integration
~~~~~~~~~~~~~~~~~~~~~~~~~~~~

* Automatic sync with *applicationContext-security.xml*:

 * If Spring Security is set, load default roles settings to *menu.xml".
 * If Spring Security is updated, update roles in *menu.xml"
* Detection of *Controller* and *Controller Method* Spring Security configuration and set menu entries visibility based on that info.
 
Menu model in database
~~~~~~~~~~~~~~~~~~~~~~~~~~~~

Load the application structure from a DB.

Currently you can do it manually by setting `MenuLoader`_ ``MENU_CONFIG_FILE`` constant to ``null``. This disable all structure change commands because there is no way to access model data.

Visibility check based on Spring EL (expressions)
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

Another interesting feature could be add support to use Spring Expression Language for items visibility.

TODO
====

* Check now to disable menu Roo service:
 
 * http://jira.springframework.org/browse/ROO-918
 * http://jira.springframework.org/browse/ROO-904
 * http://forum.springsource.org/showthread.php?t=89522
 * http://jira.springframework.org/browse/ROO-950
