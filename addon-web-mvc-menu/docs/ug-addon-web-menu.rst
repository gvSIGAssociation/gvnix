=========================================================
 gvNIX - Spring Roo - Addon - Web MVC Menu
=========================================================

-------------
 User guide
-------------

:Project:   gvNIX. Spring Roo based RAD tool
:Copyright: 2010 (C) Dirección General de Tecnologías de la Información - Conselleria d'Hisenda i Administració Pública
:Author:    DISID Corporation, S.L.
:Revision:  $Rev: 840 $
:Date:      $Date: 2011-04-19 11:03:43 +0200 (mar, 19 abr 2011) $

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

Menu management in Pet clinic 
-------------------------------

Create a new directory for the project::

  Bash shell:

    mkdir clinic
    cd clinic

Start gvNIX::

  clinic$ gvnix.sh
      ____  ____  ____  
     / __ \/ __ \/ __ \ 
    / /_/ / / / / / / / 
   / _, _/ /_/ / /_/ /   1.1.2.RELEASE [rev fbc33bb]
  /_/ |_|\____/\____/   gvNIX distribution 0.6.0
  
  
  Welcome to Spring Roo. For assistance press TAB or type "hint" then hit ENTER.
  roo>

Create the project::

  roo> script clinic.roo

Setup menu management: install dependencies, create menu model and load initial menu data structure from *menu.jspx*::

  roo> menu setup

Create a new menu category to contain all finders::

  roo> // Create a category to contain application finders
  roo> menu entry add --label Finders
  roo> // Change default ID to Category_ID
  roo> menu entry update --id i_page_finders --nid c_finders
  roo> // Move to root level
  roo> menu entry move --id c_finders --before c_seleniumtests
  roo> // Move finders to new finders category
  roo> menu entry move --id fi_pet_nameandweight --into c_finders
  roo> menu entry move --id fi_pet_sendremindersandweightlessthan --into c_finders
  roo> menu entry move --id fi_pet_owner --into c_finders
  roo> menu entry move --id fi_pet_typeandnamelike --into c_finders
  roo> menu entry move --id fi_visit_descriptionandvisitdate --into c_finders
  roo> menu entry move --id fi_visit_descriptionlike --into c_finders
  roo> menu entry move --id fi_visit_visitdatebetween --into c_finders

Run `mvn tomcat:run` in the root of your project and the new application structure should be available under the URL _http://localhost:8080/petclinic/_

Available commands
====================

``menu setup``
--------------------------

Installs dependencies, creates configuration base, installs menu model, loads menu info from *menu.jspx*.

No parameters needed.

``menu entry add``
----------------------------------

This method add new menu entry related to one page. 

This command won't add neither Controller nor JSPs for the new entry, if you need them use 'controller class' instead.

For application pages you can't create two pages to the same destination with the same parameters (parameters values are ignored).

Parameters:

* ``--label``, text to show in menu if no messageCode set, otherwise label is used as message argument.
* ``--category``, add entry into this menu entry (category). Default add to 'Page' category. Note you don't need a command to manage categories, just change default category ID ('menu entry update') when you need, new default category will be created automatically.
* ``--messageCode``, the global message code to get I18N label text (works in conjunction with label). If empty, it will be generated using entry name.
* ``--url``, the link URL to access to application page.
* ``--roles``, user that has any of this granted roles (comma separated) will see this menu entry. If empty, the menu entry is shown for every one.

``menu entry remove``
----------------------------------

Deletes a menu entry. It won't delete the related view artefacts: controller, jsps, etc. 

If menu entry contains other items, the operation will be canceled. You can use ``--force`` to force operation and delete the children too.

This operation cannot be undone.

Parameters:

* ``--id``, menu entry to remove.

* ``--force``, forces to perform operation when a menu entry has children.

``menu entry visibility``
----------------------------------

This command show/hide a menu entry. It only affects menu entry neither related artefacts nor page accessibility.

Parameters:

* ``--id``, menu entry to update.
* ``--hidden``, true to hide and false to show the menu entry.

``menu entry roles``
----------------------

Set the user roles that will grant entry to be shown depending on user roles and target URL permissions.

* ``--id``, menuu entry to update
* ``--roles``, user role list (comma separated) that can access this page. If empty, the page is available for every one.

``menu entry move``
----------------------------------

Move a page and its children to another tree node.

Parameters:

* ``--id``, menu entry to move. 

* ``--into``, insert the menu item into this

* ``--before``, locate the page before this (in the same level).

This command requires ones (and only one) of ``--into`` or ``--before`` parameter.

``menu entry update``
----------------------------------

Update menu entry info.

Parameters:

* ``--id``, page id to update its menu entry. Use 'menu tree' to get all pages ids.
* ``--nid``, new ID for selected page. Use new ID to change page type: use 'c_' prefix for category pages or 'i_' prefix for item pages.
* ``--label``, the label text used for related menu item. Note that related labelCode will remain the same.
* ``--messageCode``, the global message code to get I18N label text (works in conjunction with label). If empty, it will be generated using page name.
* ``--url``, the link URL to access to this page.
* ``--roles``, user role list (comma separated) granted to access to target URL. If empty, the page is available for every one.
* ``--hidden``, menu entry visibility.

``menu tree``
--------------------------

List current menu tree structure. Example: clinic.roo::

  [c_owner]
  URL          : No
  Hidden       : false
  Children     : 
      [i_owner_new]
      URL          : /owners?form
      Hidden       : false
  
      [i_owner_list]
      URL          : /owners?page=1&size=${empty param.size ? 10 : param.size}
      Hidden       : false
  
  [c_p
  URL No
  Hiddfalse
  Chil
      [i_pet_new]
      URL          : /pets?form
      Hidden       : false
  
      [i_pet_list]
      URL          : /pets?page=1&size=${empty param.size ? 10 : param.size}
      Hidden       : false
  
      [fi_pet_typeandnamelike]
      URL          : /pets?find=ByTypeAndNameLike&form
      Hidden       : false
  ...

Parameters:

* ``--id``, menu entry id to show its tree structure. Default show all entries.
* ``--label``, show label texts.
* ``--messageCode``, show message codes.
* ``--lang``, show messages in this language.
* ``--roles``, show roles.

If we use all this parameters the output will show something like this::

  [c_owner]
  URL          : No
  Label Code   : menu_category_owner_label
  Label        : Owner
  Message Code : 
  Message      : 
  Hidden       : false
  Children     : 
      [i_owner_new]
      URL          : /owners?form
      Label Code   : menu_item_owner_new_label
      Label        : Owner
      Message Code : global_menu_new
      Message      : Create new {0}
      Hidden       : false
  
      [i_owner_list]
      URL          : /owners?page=1&size=${empty param.size ? 10 : param.size}
      Label Code   : menu_item_owner_list_label
      Label        : Owners
      Message Code : global_menu_list
      Message      : List all {0}
      Hidden       : false
  [c_pet]
  URL          : No
  Label Code   : menu_category_pet_label
  ...

``menu entry info``
---------------------------

Shows all information about a page. Example for Locale 'es'::

  [c_vet]
  URL          : No
  Label Code   : menu_category_vet_label
  Label        : Vet
  Message Code : 
  Message      : 
  Roles        : 
  Hidden       : false
  Children     : 
      [i_vet_new]
      URL          : /vets?form
      Label Code   : menu_item_vet_new_label
      Label        : Vet
      Message Code : global_menu_new
      Message      : Crear nuevo {0}
      Roles        : 
      Hidden       : false
  
      [i_vet_list]
      URL          : /vets?page=1&size=${empty param.size ? 10 : param.size}
      Label Code   : menu_item_vet_list_label
      Label        : Vets
      Message Code : global_menu_list
      Message      : Listar {0}
      Roles        : 
      Hidden       : false


