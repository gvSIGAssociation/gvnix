============================
 gvNIX Theme Manager add-on
============================

:Project:   gvNIX. Spring Roo based RAD tool
:Copyright: Conselleria d'Infraestructures i Transport - Generalitat Valenciana
:Author:    [ ... ]
:Revision:  $Rev: 3349 $
:Date:      $Date: 2010-05-20 09:25:20 +0200 (jue 20 de may de 2010) $

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
=============

This Add-on manages the themes defined in gvNIX to install in a project.

Requirements
=============

Manage application themes:

  * Install a theme that is defined in gvNIX.
  * Manage active theme.
  * List installed, available and active themes.

Future enhancements:

  * Save a new theme using the edited files of a project (css, images, jspx, tagx, xml).

Definitions
------------

theme - The application theme is composed by css, image, jspx, tagx and xml files to define the user interface for the application.
distribution theme - Theme created with the defined folder structure in gvNIX distribution directory.
installed theme - Theme that has been copied to project themes folder in the root of the working project directory.
active theme - Theme that has been included inside the project structure overwriting the old theme's files in ``project/src/main/webapp`` directory.

.. admonition:: Note

     active theme doesn't delete the old theme files, only override ones that have the same name.

Theme folder structure
-----------------------

Folder with the **name** of the theme inside the gvNIX repository folder ``themes``::

    theme-cit/
    |-- styles
    |   |-- cit.css
    |   `-- themes
    |       `--cit
    |          |-- dijit.css
    |          |-- dijit_rtl.css
    |          `-- final
    |-- images
    |   `-- cit
    |       `-- theme
    `-- WEB-INF
        `-- layouts
        |   |-- default.jspx
        |   |-- home-default.jspx
        |   `-- layouts.xml
        |-- views
        |   |-- home-menu.jspx
        |   `-- menu.jspx
        |-- tags
        |   |-- form
        |   |-- menu
        |   |-- util
        |   `-- view
         `-- theme.xml

Content specification::

    theme-cit/
    |-- styles - Css defined for the application visualization.
    |   `-- themes
    |       `--cit - Group of css defined to use with DOJO javascript.
    |-- images
    |   `-- cit - Images for the application templates.
    `-- WEB-INF
        `-- layouts - default .jspx templates for the application pages. Defined in layouts.xml.
        |-- views - Menu views for the application template and web.xml defined layouts.
        |-- tags - A collection of tags defined for the application (menu, pagination, tables, language...).
         `-- theme.xml - Theme properties. Xml composed by <theme> and <description> labels.

theme.xml
----------

This file contents theme's description with this structure::

    <theme>
      <description>
      </description>
    </theme>

TODO: If ``WEB-INF/views/views.xml`` file exists in the project the Add-on should check if has to override it or merge it with the project file.

Use Case
=========

TODO Validate this is valid already. 

Set the environment variable ROO_THEMES = "gvNIX_HOME/themes" to retrieve the default theme location.
Start Roo shell and create a web project.

Install
--------

Execute the command to install a new theme into the project.
  * This will copy the files into project folder ``themes``. Creates the folder ``themes`` if not exists.

List
-----

Execute the list command.
  * This shows the list of all themes and categories: distribution, installed, active available for the Add-on.

You will get an output similar to::

  gvNIX           Installed       Active        Name        Description
    X                X                          CIT         CIT theme for Roo projects
    X                X              X           gvNIX       gvNIX base theme
    X                                           blue        Blue color theme


Activation
-----------

Execute the activation command.
  * Copy the files into the project from the selected installed theme.
  * Create an <id> tag with the selected theme name (folder's theme name) into its ``theme.xml`` file.

Project Organization
======================

TODO Validate this is valid already. 

To assign more versatility to the Add-on there is not included any theme. The themes are stored in a different directory inside the gvNIX distribution: ``themes``. Each theme has a folder structure named itself.
The directory must be defined to be packaged with the gvNIX distribution and controlled by the Add-on to get the sources of the themes to install.

Analysis
=========

Analysis for the development of the Add-on displayed by commands.

Installation
-------------

Installs selected theme into project.

Copy the selected theme into the folder ``themes`` in root directory of the project using the Roo components to create files.
If theme exists in themes project folder: override it.


List
-----

List themes.

List the gvNIX themes grouped by three categories::

  * The available themes installed in the gvNIX distribution. List ``gvNIX/themes`` directory.
  * The available themes installed in the root of the working project. List ``project_home/themes`` directory.
  * Show the activated theme of the application. This theme is defined in the theme.xml file inside the ``WEB-INF`` directory in the project.

Activation
-----------

Activate an installed theme in application.

Copy and override the default files in the project folder ``webapp`` with the selected ones that are placed in theme's folder inside ``project_home/themes``.
Adds an ``<id>`` label to selected theme ``theme.xml`` file with the theme folder name to set this theme activated in the application.

.. admonition:: Note

     Activation a theme will copy the selected theme files into webapp application directory. This action **will not** delete files from another installed theme.

Commands
=========

There are defined three commands in this Add-on:

theme manager install theme
----------------------------

Installs selected theme into project.

Parameters:

  * ``--name`` (mandatory): Theme's name available in gvNIX.

theme manager list
-------------------

Shows themes available, installed and activated.

This doesn't need any parameters.

theme manager activate theme
------------------------------

Activate a theme in the project.

Parameters:

  * ``--name`` (mandatory): Theme's name set active in the project.

Project Folders
================

Theme folder: ``theme-cit`` inside gvNIX theme installation folder.
Add-on folder: addon.gvnix.theme.manager.roo.addon
Add-on name: addon-theme-manager

Application versions
=====================

* gvNIX-0.3 version: The Add-on install the theme defined in the gvNIX repository folder.
* Future versions: The command to create a theme with edited files from a project to export in other Roo projects.

Proof of Concept
================

* http://scmcit.gva.es/svn/gvnix-proof/trunk/petclinic-theme-cit
* https://svn.disid.com/svn/disid/proof/gvnix/theme-cit-app
* https://svn.disid.com/svn/disid/proof/gvnix/theme-gvnix-app
* https://svn.disid.com/svn/disid/proof/spring_roo/cit_style
