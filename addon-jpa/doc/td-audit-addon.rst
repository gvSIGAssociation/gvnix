====================================================
 gvNIX. Add-on Auditoría
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
* Para el soporte de registro de histórico se creará un sistema de proveedores (componentes OSGi que deben cumplir un interfaz).
* Para el sistema de proveedores se creará la infraestructura necesaria para que el API generado en las entidades para la lectura del registro de cambios sea independiente de proveedor de registro.

Soluciones propuesta
======================

* *Esta solución está probada en el proof http://scmcit.gva.es/svn/gvnix-proof/tags/petclinic-audit/v2.0*
* *La Parte de Registro de histórico está provada en http://scmcit.gva.es/svn/gvnix-proof/tags/petclinic-revision-log/v1.1*

Uso del addon
---------------

Establecer auditoría
''''''''''''''''''''''

En un proyecto con la persistencia configurada se ejecutará el siguiente comando::

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

Añadir registro de histórico
''''''''''''''''''''''''''''''''''''''''''''''''

El registro de histórico será opcional. Para seleccionar el proveedor (cuyo bundle debe estar instalado en el framework) se usará una instrucción como esta)::

   gvNIX> jpa audit revisionLog --provider NombreDeProveedor

Los proveedores tendrán métodos para informar de su disponibilidad. Como mínimo:

* Cual de ellos está activo
* Si es aplicable al proyecto actual

Después del comando, aquellas entidades anotadas con ``@GvNIXAudit`` y tenga establecido su atributo ``revisionLog`` a True se acutalizará su ``.aj`` para incluir:

* static List<Visit> findAllVisit(Date) : Devuelve la lista de entidades en la fecha solicitada
* static List<Visit> findAllVisit(Long) : Devuelve la lista de entidades en la revisión solicitada
* static Visit findVisit(Long, Date) : Devuelve el estado de la entidad en la fecha solicitada
* static Visit findVisit(Long, Long): Devuelve el estado de la entidad en la revisión solicitada
* static List<VisitRevison> findVisitInHistory(Long, Long, Map<String, Object>, List<String>, Integer, Integer): Búsqueda en el histórico (revisiones) con opciones de limitación de revisiones, filtro, ordenación y paginación
* static List<VisitRevison> findVisitInHistoryByDates(Date, Date, Map<String, Object>, List<String>, Integer, Integer): Búsqueda en el histórico (revisiones) con opciones de limitación de fechas de revisión, filtro, ordenación y paginación
* static Long getRevisionNumberForDate(Date): Devuelve la revisión corespondiente a una fecha
* static List<VisitRevison> getVisitHistory(Long, Date, Date, Integer, Integer): Devuelve el histórico (revisiones) de un elemento (en base a su id) entre fechas de revisión con paginación:
* List<VisitRevison> getThisVisitHistory(Date, Date, Integer, Integer): Devuelve el histórico (revisiones) de un elemento (de la instancia actual) entre fechas de revisión con paginación:
* Clase estática VisitRevison, que representa la información de la revisión de una instancia de la entidad, con los siguiente métodos:

  - Visit getItem()
  - Date getRevisionDate()
  - Long getRevisionNumber()
  - String getRevisionUserName()
  - String getType()
  - boolean isCreate()
  - boolean isDelete()
  - boolean isUpdate()

El proveedor puede que necesite incluir mas métodos auxiliares, pero, como mínimo, **debe incluir estos métodos con la misma firma**.

Si ``@GvNIXAudit`` no tiene establecido el valor de ``revisionLog`` el proveedor dispondrá de un método que informará el valor por defecto para ello.

Implementación del add-on
---------------------------

La complicación de la implementación del add-on reside en que el mecanismo de registro de listener JPA.

No es posible registrar los listener usando una anotación debido a que se prevé que varios add-ons intenten usar ese mecanismo y, actualmente, AspectJ no permite que varios ITD *manipulen* una misma anotación.

Por ello, será necesario implementar un mecanismo genérico que permita, a los add-ons que lo requieran, registrar listenes sin generar dependencias entre ellos.


Gestión de registro de listeners JPA
'''''''''''''''''''''''''''''''''''''''''

El mecanismo se implementará dentro del add-on JPA de gvNIX.

Consistirá en un ``MetadataListener`` (``JpaOrmEntityListenerMetadataListener``), al estilo de ``JspMetadataListener``, pero con la peculiaridad de que el registro de las dependencias entre el ``MetadataListener`` y el metadata que provocará su ejecución se realizará a traves de un ``JpaOrmEntityListenerRegistry``, que será llamado por el ``MetadataProvider`` del add-on que genere el listener de JPA en el momento de su activación.

En el momento de registro, los ``MetadataProvider`` deben poder establecer una prioridad de ejecución. Esta prioridad se definirá usando los identificadores base de los ``Metadata`` a través de un método ``setListenerOrder(String idBefor, String idAfter)``.

Además, el ``Metadata`` del listener de JPA **deberá implementar un interface definido en el add-on de JPA** para permitir al ``JpaOrmEntityListenerMetadata`` obtener la información de la *entidad* en la que debe registrase el listener y la *clase del propio listener*.

Al lanzarse el ``Metadata`` del listener de JPA, el ``JpaOrmEntityListenerMetadata`` se encargará de:

* Comprobar que existe el fichero *orm.xml*, sino crearlo.
* Buscar el *tag* de la entidad indicada por el ``Metadata``, sino crearlo.
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

  - ``jpa audit revisionLog``: Configura un proveedor de revisiones de histórico. Solo disponible si hay alguno disponible. Parámetros:

      + ``provider`` (obligatorio): Proveedor a usar. Será autocompletado (converter).

  - ``jpa audit add``: preparará la audición para una entidad. Parámetros:

    + ``entity`` (obligatorio): Clase de la entidad sobre la que actuará el comando

    + ``listener`` (opcional): Clase donde se creará el listener. No debe existir. Por defecto será la clase de la entidad con sufijo ``_auditListener``

    + ``revisionLog`` (opcional): Registrar lista de revisiones en la entidad. Por defecto es ``null`` y este valor depende del proveedor de registro configurado (puede no haber).


  - ``jpa audit all``: preparará la audición para todas las entidades. Se usará el nombre por defecto para las clases de los listeners. Parámetros:

    + ``package`` (opcional): Paquete java donde se generarán la clase de los listeners. Por defecto la misma que las entidades

    + ``revisionLog`` (opcional): Registrar lista de revisiones en las entidades. Por defecto es ``null`` y este valor depende del proveedor de registro configurado (puede no haber).

* RevisionLogProvider: Interfaz que deben cumplir los proveedores de gestión de registro

   + Las clases que implementen este interfaz deben estar anotadas con ``@Component`` y ``@Service`` de OSGi para que puedan ser registradas en el add-on.

   + Debe incluir los siguientes métodos:

     - boolean isAvailable(): Informa si el proveedor puede ser usado en el proyecto actual

     - boolean isActive(): Informa si el proveedor es el configurado actualmente

     - String getId(): Identificador del proveedor

     - String getDescription(): Descripción del proveedor

     - boolean getDefaultValueOfRevisionLogAttribute(): Devuelve un booleano que indica que valor que se debe asumir para el ``revisionLog`` de las anotaciones cuyo valor sea ``null``.

     - void setup(): Realizar las operaciones necesaria para instalarse en el proyecto actual

     - void build???MethodBody(???): Varios métodos que generarán el cuerpo de los métodos que se deben generar en el ``.aj`` del metadato para la entidad.

     - void fillAddtionalArtifactOfRevisionItemClass(????): Método que termina de construir la clase estática para los elementos de revisión. Esta clase ya tendrá construido los métodos del API, pero requerirá un constructor privado y las propiedades necesarias para almacenar la información de la revisión.

     - void fillAddtionalArtifact(????): Método que termina de construir el metadato, incluyendo métodos y propiedades de utilidad necesarias para el funcionamiento de la implemenetación.


* Operations:

  - Soporte para las operaciones de los commands

  - Tendrá una lista de instancias de RevisionLogProvider inyectada vía OSGi

  - Proveerá métodos que devuelve la lista de proveedores disponibles (si los hay) y otro que devuelve el activo (si lo hay).

* RevisionLogProviderConverter: Conversor usado para poder autocompletar el parámetros ``provider`` de ``jpa audit revisionLog``

* Anotaciones:

  - ``GvNIXAudit``: Para la entidad. Tendrá el parámetro ``revisionLog`` (``Boolean`` [ *Ojo **NO** ``boolean``* ])

  - ``GvNIXAuditListener``: Para la clase listener. Tendrá como parámetro la entidad


* AuditMetadata y AuditMetadataListener: Clases que atenderán a la anotación ``GvNIXAudit``

  - Genera las propiedades, getters y setters para los campos de información de auditoría.

  - Delegará en el ``RevisionLogProvider`` activado (si lo hay) la construcción de los artefactos para la gestión de revisiones

  - Si no hay ``RevisionLogProvider`` y ``revisionLog`` vale ``TRUE`` lanzará un *Waring* a través del ``Log`` informando que no hay proveedor registrado.

* AuditListenerMetadata y AuditListenerMetadataListener: Clases que atenderán a la anotación ``GvNIXAuditListener``

  - Genera los método de listener.

  - Debe comprobar que la entidad referida está anotada con ``GvNIXAudit``.

  - En su activación deberá registrar la dependencia en ``JpaOrmEntityListenerRegistry``

Registro de Histórico con Hibernate Envers
============================================

La primera implementación de proveedor de registro de histórico se implementará usando Hibernate Envers.

El proof que prueba su uso está en http://scmcit.gva.es/svn/gvnix-proof/tags/petclinic-revision-log/v1.1

Este proveedor, como es lógico, solo se activará como disponible en proyectos que usen Hibernate como implementación de JPA.

Otras tareas
==============

Después de implementar el mecanismo de registro para los listeners de jpa, **sería interesante modificar el add-on de OCCChecksum para que utilice este sistema**.



