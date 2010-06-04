==================================
 gvNIX Web relation styles add-on
==================================

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

This document contents relative to this add-on.

Definitions
=================

Requirements
=============

This add-on have to install and manage the application menu of Roo projects.


Estructura para la creación del Add-on
========================================

Funcionalidad del Add-on.

h3. Conjunto de tagx para la visualización

Copiar los tagx que se incluyen en la carpeta resources del Add-on dentro de la carpeta ``WEB-INF/`` del proyecto::

    src/main/webapp/WEB-INF/tags/relations/
    |-- decorators
    |   |-- accordiontableview.tagx
    |   `-- tabtableview.tagx
    |-- relationspagination.tagx
    |-- relationstable.tagx
    |-- accordion.tagx
    `-- tab.tagx
    src/main/webapp/WEB-INF/tags/util
    `-- gvnixcallfunction.tagx

h3. ITD

Crear MetadataITD .

Crear ProviderITD escucha a RooEntity.

h3. Listener que extienda MetadataListener.

Crear un listener que extienda de MetadataListener para escuchar a RooWebSaffold o JspMetadata. INFO.

Extender el MetadataListener para crear uno nuevo que se ejecute después de @RooWebScaffold.
Comprobar si se han añadido relaciones del tipo 1-n LAZY a las entidades. ¿ Cómo ?
Acceder a las jspx show y update.

* El atributo ``render`` de las relaciones generadas por rooWebScaffold ponerlo a false para que no muestre lo que genera Roo.
* Crear la llamada al tag que engloba las relaciones fuera del tag de page (page:show o page:update que utiliza Roo).

  * Comprobar si existe en la jspx
  * Si no existe

    * <relations:tab> en el caso que se utilice la visualización por ``tabs``.
    * Dentro de esta etiqueta instancia la propiedad mediante la llamada al tagx <relation:tabview> (en el caso de mostrar los datos dentro de las pestañas) para la visualización con el formato de tabla incluyendo los parámetros necesarios para generar el código.
  * Si existe

    * Dentro de esta etiqueta instancia la propiedad mediante la llamada al tagx <relation:tabview> (en el caso de mostrar los datos dentro de las pestañas) para la visualización con el formato de tabla incluyendo los parámetros necesarios para generar el código.

Crear el método para mostrar los datos paginados mediante la plantilla AspectJ.
* Si la plantilla existe

  * Añadir el método con la plantilla para método de búsqueda.

* Si no existe

  * Crear la clase AspectJ mediante la plantilla de la clase y el método relacionado con la plantilla para el método.

Roo Shell commands
====================

Comandos asociados al Add-on.

``web relation styles setup``
------------------------------

Parameters:

* ``--view`` (mandatory): Selects the view to show the relations of an Entity.

Use cases
=============

Installation
----------------

Developer wants to use new menu in his Roo application. This are the steeps to get it:

#. Install this add-on if it isn't already installed.

#. Execute command ``web relation styles setup --view tab``.


Classes toString de Roo Metadata y MetadataProvider
-----------------------------------------------------

  protected void activate(ComponentContext context) {
    metadataDependencyRegistry.registerDependency(PhysicalTypeIdentifier.getMetadataIdentiferType(), getProvidesType());
    // Ensure we're notified of all metadata related to physical Java types, in particular their initial creation
    metadataDependencyRegistry.registerDependency(PhysicalTypeIdentifier.getMetadataIdentiferType(), getProvidesType());
    beanInfoMetadataProvider.addMetadataTrigger(new JavaType(RooToString.class.getName()));
    addMetadataTrigger(new JavaType(RooToString.class.getName()));
  }
  
  protected ItdTypeDetailsProvidingMetadataItem getMetadata(String metadataIdentificationString, JavaType aspectName, PhysicalTypeMetadata governorPhysicalTypeMetadata, String itdFilename) {
    // Acquire bean info (we need getters details, specifically)
    JavaType javaType = ToStringMetadata.getJavaType(metadataIdentificationString);
    Path path = ToStringMetadata.getPath(metadataIdentificationString);
    String beanInfoMetadataKey = BeanInfoMetadata.createIdentifier(javaType, path);

    // We want to be notified if the getter info changes in any way 
    metadataDependencyRegistry.registerDependency(beanInfoMetadataKey, metadataIdentificationString);
    BeanInfoMetadata beanInfoMetadata = (BeanInfoMetadata) metadataService.get(beanInfoMetadataKey);
    
    // Abort if we don't have getter information available
    if (beanInfoMetadata == null) {
      return null;
    }
    
    // Otherwise go off and create the to String metadata
    return new ToStringMetadata(metadataIdentificationString, aspectName, governorPhysicalTypeMetadata, beanInfoMetadata);
  }