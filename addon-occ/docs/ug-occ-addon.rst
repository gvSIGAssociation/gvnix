=============================
 gvNIX OCC Add-on User Guide
=============================

:Company:   DiSiD Technologies, S.L.
:Revision:  $Rev: 2956 $
:Date:      $Date: 2010-01-19 09:08:12 +0100 (mar 19 de ene de 2010) $
:Copyright: Esta obra está bajo la licencia `Reconocimiento-Compartir bajo la misma licencia 3.0 España <http://creativecommons.org/licenses/by-sa/3.0/es/>`_ de Creative Commons. Puede copiarla, distribuirla y comunicarla públicamente siempre que especifique sus autores y comparta cualquier obra derivada bajo la misma licencia. La licencia completa se puede consultar en http://creativecommons.org/licenses/by-sa/3.0/es/

.. contents::
   :depth: 2
   :backlinks: none

.. |date| date::

Introduction
===============

Add-on to create the JPA extension that enables versioning by comparing the old and new entity state without the need to alter legacy databases. 

Requirements
--------------

 * JDK 1.6+
 * Maven 2+
 * gvNIX - Spring Roo - Addon - Optimistic Concurreny Control
 * Eclipse Helios 3.6.0 (optional)

Install Addon Services Management
--------------------------------------

Options:

 * To install gvNIX 0.6.0 that contains the add-on installed.
 * OR, to install in Spring Roo 1.1.2 
  * Trust on PGP KEY ID used to sign the add-on. Note that gvNIX project members have their PGP KEYs IDs published at https://code.google.com/p/gvnix/people/list::

      pgp trust --keyId 0xC5FC814B

  * Run the command below::

      addon install bundle --bundleSymbolicName org.gvnix.occ.roo.addon

Use case: OCC support in Pet clinic 
============================================

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

Set concurrency control on *Pet* entity::

  roo> occ checksum set --entity ~.domain.Pet

  Updated ROOT/pom.xml [Added dependency org.gvnix:org.gvnix.occ.roo.addon:0.6.0]
  Updated SRC_MAIN_JAVA/com/springsource/petclinic/domain/Pet.java
  Created SRC_MAIN_JAVA/com/springsource/petclinic/domain/Pet_Roo_gvNIX_occChecksum.aj
  Updated SRC_MAIN_WEBAPP/WEB-INF/views/pets/list.jspx
  Updated SRC_MAIN_WEBAPP/WEB-INF/views/pets/show.jspx
  Updated SRC_MAIN_WEBAPP/WEB-INF/views/pets/create.jspx
  Updated SRC_MAIN_WEBAPP/WEB-INF/views/pets/update.jspx
  Updated SRC_MAIN_WEBAPP/WEB-INF/i18n/application.properties
  Updated SRC_MAIN_WEBAPP/WEB-INF/views/pets/list.jspx
  Updated SRC_MAIN_WEBAPP/WEB-INF/views/pets/show.jspx
  Updated SRC_MAIN_WEBAPP/WEB-INF/views/pets/create.jspx
  Updated SRC_MAIN_WEBAPP/WEB-INF/views/pets/update.jspx
  Updated SRC_MAIN_JAVA/com/springsource/petclinic/domain/Pet_Roo_Entity.aj
  Updated SRC_MAIN_JAVA/com/springsource/petclinic/domain/Pet_Roo_ToString.aj
  Updated SRC_TEST_JAVA/com/springsource/petclinic/domain/PetIntegrationTest_Roo_IntegrationTest.aj
  [Spring Roo Process Manager Background Polling Thread] Updated SRC_MAIN_JAVA/com/springsource/petclinic/domain/Pet_Roo_ToString.aj

Add-on commands
------------------

Two new commands will be available:

#. ``occ checksum set``: Apply this occ implementation to a existing ``@RooEntity``. Add the gvNIX Optimistic Concurrecy Control Checksum based behaivor to a Entity.

   Options:
   
   * *entity*: The name of the entity object to add OCC
   * *fieldName* (optional): The name of the field to use to store de checksum value
   * *digestMethod* (optional): The name of the type of digest method to compute the checksum

#. ``occi checksum all``: Apply this occ implementation to all existing ``@RooEntity``.
   Add the gvNIX Optimistic Concurrency Control Checksum based behaivor to all entities in project.
   If any of them has already been configure no changes will be applied to this entity.

   Options:

   * *fieldName* (optional): The name of the field to use to store de checksum value
   * *digestMethod* (optional): The name of the type of digest method to compute the checksum

See shell command help for more information
