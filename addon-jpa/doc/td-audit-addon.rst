====================================================
 gvNIX. Add-on Auditoría Simple
====================================================

------------------------
 Diseño técnico
------------------------

:Company:   Dirección General de Tecnologías de la Información - Conselleria d'Hisenda i Administració Pública
:Author:    DISID TECHNOLOGIES, S.L.
:Revision:  $Rev$
:Date:      $Date$
:Copyright: 2013 (C) Dirección General de Tecnologías de la Información - Conselleria d'Hisenda i Administració Pública

.. contents::
   :depth: 2
   :backlinks: none

Introducción
===============

En este documento se describe el diseño técnico del add-on.

Arquitectura de la solución
=============================

* Será necesario añadir los campos a la entidad para que sean persistidos.
* Para actualizar los datos de los campos se usará un *listener* de JPA.
* El *listener* será una clase generada para la propia entidad


Soluciones propuesta
======================

*Esta solución está probada en el proof http://scmcit.gva.es/svn/gvnix-proof/tags/petclinic-audit/v2.0*

Uso del addon
--------------

En un proyecto con la presistencia configurada se ejecutará el siguiente comando::

   gvNIX> jpa audit add --name ~.domain.Visit

Esto provocará los siguientes cambios:

#. Se añadirá una anotación ``@GvNIXAudit`` en la entidad ``Visit``

   * Esto generará una fichero ``.aj`` en el que se definirán las propiedades, getters y setters para los valores de auditoría:

     - Fecha de creación
     - Usuario de creación
     - Fecha última actualización
     - Usuario de última actualización

#. Se creará una clase ``Visit_auditListener``

   * Estará anotada con ``@GvNIXAuditListener``
   * Esta anotación generará un fichero ``.aj`` con los siguentes métodos:

     - getUserName(): Devuelve el usuario actual.

       + Devuelve ``null`` si no está configurado Spring Security
       + Devuelve el nombre del usuario actual del contexto de Spring Security

     - onCreate(Visit visit): Anotado con ``@PrePersist``. Establece los valores de Fechas y usuario (creación y actualización por igual) usando la fecha actual y el string devuelto por ``getUserName()``.

     - onUpdate(Visit visit): Anotado con ``@PreUpdate``. Establece los valores de Fechas y usuario de última actualización usando la fecha actual y el string devuelto por ``getUserName()``.

#. Se creará/actualizará el fichero *META-INF/orm.xml* (al lado de *persistence.xml*) para registrar el listener ``Visit_auditListener`` a la entidad ``Visit``::

      <?xml version="1.0" encoding="UTF-8" standalone="no"?>
      <entity-mappings xmlns="http://java.sun.com/xml/ns/persistence/orm" 
              xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" version="2.0" 
              xsi:schemaLocation="http://java.sun.com/xml/ns/persistence/orm http://java.sun.com/xml/ns/persistence/orm_2_0.xsd">
        <entity class="com.springsource.petclinic.domain.Visit">
          <entity-listeners>
            <entity-listener class="com.springsource.petclinic.domain.Visit_Audit_Listener"></entity-listener>
          </entity-listeners>
        </entity>
      </entity-mappings>

En el proof indicado se ha implementado un test que comprueba el funcionamiento en ``VisitIntegrationTest.VisitIntegrationTest``.


Implementación del add-on
---------------------------

La complicación de la implementación del add-on reside en que el mecanismo de registro de listener JPA.

No es posible registrar los listener usando una anotación debido a que se prevee que varios add-ons intenten usar ese mecanismo y, actualement, AspectJ no permite que varios ITD *manipulen* una misma anotación.

Por ello, será necesario implementar un mecanismo genérico que permita, a los add-ons que lo requieran, registrar listenes sin generar dependencias entre ellos.


Gestión de registro de listeners JPA
'''''''''''''''''''''''''''''''''''''''''

El mecanismo se implementará dentro del add-on JPA de gvNIX.

Consistirá en un ``MetadataListener`` (``JpaOrmEntityListenerMetadataListener``), al estilo de ``JspMetadataListener``, pero con la peculariedad de que el registro de las dependencias entre el ``MetadataListener`` y el metadata que provocará su ejecución se realizará a traves de un ``JpaOrmEntityListenerRegistry``, que será llamado por el ``MetadataProvider`` del add-on que genere el listener de JPA en el momento de su activación. 

En el momento de registro, los ``MetadataProvider`` deben poder establecer una prioridad de ejecución. Esta prioridad se definirá usando los identificadores base de los ``Metadata`` a traves de un método ``setListenerOrder(String idBefor, String idAfter)``.

Además, el ``Metadata`` del listener de JPA **deberá implementar un interface definido en el add-on de JPA** para permitir al ``JpaOrmEntityListenerMetadata`` obtener la información de la *entidad* en la que debe registrase el listener y la *clase del propio listener*.

Al lanzarse el ``Metadata`` del listener de JPA, el ``JpaOrmEntityListenerMetadata`` se encargará de:

* Comprobar que existe el fichero *orm.xml*, sino crearlo.
* Búscar el *tag* de la entidad indicada por el ``Metadata``, sino crearlo.
* Cargar la lista de listeners.
* Comprueba que todas las clases existen, sino las elimina.
* Comprueba si está registrado el listener indicado por el ``Metadata``, sino lo añade.
* Ordena la lista de listeners según la dependencia indicada en el registro.
* Actualiza la lista de listener en el *tag* de la entidad.
* Graba el *orm.xml*.

Implementación del propio add-on
'''''''''''''''''''''''''''''''''

La implementación del add tendrá los siguientes componentes:

* Commands:

  - ``jpa audit add``: preparará la audición para una entidad. Parámetros:

    + ``entity`` (obligatorio): Clase de la entidad sobre la que actuará el comando

    + ``listener`` (opcional): Clase donde se creará el listener. No debe existir. Por defecto será la clase de la entidad con sufijo ``_auditListener``


  - ``jpa audit all``: preparará la audición para todas las entidades. Se usará el nombre por defecto para las clases de los listeners. Parámetros:

    + ``package`` (opcional): Paquete java donde se generarán la clase de los listeners. Por defecto la misma que las entidades
 
* Operations. Soporte para las operaciones de los commands

* Anotaciones:

  - ``GvNIXAudit``: Para la entidad 

  - ``GvNIXAuditListener``: Para la clase listener. Tendrá como parámetro la entidad


* AuditMetadata y AuditMetadataListener: Clases que atenderán a la anotación ``GvNIXAudit``

  - Genera las propiedades, getters y setters para los campos de información de auditoría.

* AuditListenerMetadata y AuditListenerMetadataListener: Clases que atenderán a la anotación ``GvNIXAuditListener``

  - Genera los método de listener. 

  - Debe comprobar que la entidad referida está anotada con ``GvNIXAudit``.

  - En su activación deberá registrar la dependencia en ``JpaOrmEntityListenerRegistry``

Otras tareas
==============

Despues de implementar el mecanismo de registro para los listeners de jpa, **sería interesante modificar el add-on de OCCChecksum para que utilice este sistema**.



