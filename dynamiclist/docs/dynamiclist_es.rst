=========================================================
 gvNIX dynamiclist Documentation
=========================================================

:Project:   gvNIX. Spring Roo based RAD tool
:Copyright: Conselleria d'Infraestructures i Transport - Generalitat Valenciana
:Author:    ...
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

Dynamiclist es un componente (Custom Tag) que añade a una aplicación web la visualización de los listados 
de las diferentes entidades de la aplicación.
Este listado ofrece de forma dinámica opciones de creación, visualización, borrado, modificación y 
otras acciones adicionales de la entidad, así como filtrado, agrupación, ordenación, paginación del listado mostrado.     


Requerimientos
==============
Aplicación web basada en springmvc.
Seguridad desarrollada con Spring-security (se utilizan las credenciales para guardar los filtros referenciando al usuario logeado)
Acceso a datos mediante JPA


Pasos a seguir para la utilización del componente:
=================================================

La aplicación se distribuye en un fichero .zip generado por la orden de maven 'mvn assembly:assembly', 
este fichero se compone de el jar de dinamiclist más los jar de sus dependencias, ficheros documentación, 
el código fuente, y los recursos no empaquetados en el .jar.  

Para una mayor comprensión se van a indicar los diferentes pasos que se deberían seguir para utilizar este componente. No es necesario 
realizarlo en el orden indicado en este documento.

El componente se puede dividir en tres partes:

1) Clases java (custonTag, Controllers, Services, DataAcces ...)
	
	Estas clases están empaquetadas en el fichero .jar de la distribución.


2) Recursos distribuidos dentro del .jar
		
	/META-INF/js
	dynamicList.tld
	
	org/gvnix/dynamiclist/dynamiclistmessages_en.properties
	org/gvnix/dynamiclist/dynamiclistmessages.properties
	
	
3) Recursos no empaquetados en el fichero .jar de la distribución (directorio /resources del fichero .zip).
	Estos recursos se deben copiar en la aplicación a desarrollar.
	
	resources/scripts
		Se proporcionan los scripts para la creación de las tablas necesarias para el funcionamiento interno del componente dynamiclist.
		DYNAMICLIST_CONFIG.sql
		DYNAMICLIST_CONFIG_data.sql (Este script es un ejemplo para añadir datos iniciales, no necesario)
		
		Las tablas definidas en el script son:
	
		La tabla'GLOBAL_CONFIG' debe obligatoriamente tener insertada una fila por cada tipo de entidad.
			El desarrollador debe insertar las filas de forma manual en la Base de datos.			
			Se debe especificar el nombre de la entidad en la columna ENTITY
			Se debe especificar los nombres de las propiedades visibles en ENTITY_PROPERTIES
			Se debe especificar los nombres de las propiedades no visibles en HIDDEN_PROPERTIES
			Con ORDERBY se puede opcionalmente especificar el orden fijo por defecto para todos los usuarios. 
			Con WHEREFILTER_FIX se puede opcionalmente especificar el filtro fijo para todos los usuarios.
			Y la columna INFOFILTER_FIX opcional para información como comentario.   
			
			Un ejemplo está en el script DYNAMICLIST_CONFIG_data.sql para la entidad 'client'
		
		La tabla 'GLOBAL_FILTER' opcionalmente se puede especificar filtros globales para una determinada 
		entidad que serán accesibles desde un 'select' de filtros. 
		El desarrollador debe insertar las filas de forma manual en la Base de datos. 
		
		La tabla 'USER_CONFIG' utilizada por la aplicación para guardar el estado(order, filter, columns, group) 
		del usuario logeado. Tabla gestionada por dynamiclist, no es necesario insertar datos de forma manual.
				
		La tabla 'USER_FILTER'  filtros del usuario logeado para una determinada entidad que serán accesibles 
		desde el 'select' de filtros. Tabla gestionada por dynamiclist, no es necesario insertar datos de forma manual.
		
	resources/webapp/images
		Las imágenes utilizadas por defecto deben ir en el despliegue en el directorio /images
		Es posible cambiarlo si se le pasa por parámetro al declarar el Tag en la jsp.  
	
	resources/webapp/views/popups
		Los ficheros jsp´s deben ubicarse en la ruta /WEB-INF/jsp/dynamiclist de la applicación web

	resources/webapp/css
		El fichero 'estilos.css' debe ser copiado en la aplicación a desarrollar en directorio de despliege '/css/estilos.css'
		
		Ejemplo de línea que debe ser añadida en los jsp´s que utilicen el componente:		
		<link rel="stylesheet" type="text/css" href="<c:url value="/css/estilos.css"/>" />



		
Ficheros de la aplicación web que deben ser configurados:

	web.xml:
		Se debe añadir un servlet de spring para recuperar los recursos de los .jar (dynamiclis.js)
		<servlet>
			<servlet-name>Resource Servlet</servlet-name>
			<servlet-class>org.springframework.js.resource.ResourceServlet</servlet-class>
		</servlet>	
		<servlet-mapping>
			<servlet-name>Resource Servlet</servlet-name>
			<url-pattern>/resources/*</url-pattern>
		</servlet-mapping>
		
		
	ficheros.jsp:
		En los ficheros que se desee utilizar los custonTag se debe añadir:

		//para añadir la referencia al tld de dynamiclist
		<%@ taglib prefix="dynamiclist" uri="dynamiclist" %>
	
		//Para añadir el .js del código javascript.
		<script type="text/javascript" src="<c:url value="/resources/js/dynamicList.js" />"> </script>
	
	
		Añadir los elementos del Custom Tag a la jsp de búsqueda:
			Parámetros obligatorios:		
				parámetro url_base: Se debe especificar la url base para que el resolutor de vistas pueda direccionar al jsp correcto 
				parámetro classObject: string utilizado como base para la búsqueda en los ficheros de internacionalización
				parámetro list: collection de elementos genéricos obtenido del request.
			Parámetros importantes:
				parámetro pk: nombre de la clave primaria. Por defecto 'id' ('tableTag')				
		
			Ejemplo de utilización de los Tags de dynamiclist para el listado del mantenimiento de clientes:					

				<dynamiclist:buttonsTag url_base="client" classObject="client">
 					<dynamiclist:actionsTag /> 	
 					<dynamiclist:headerTableTag>
 						<dynamiclist:tableTag list="${clients}"/>
 					</dynamiclist:headerTableTag>
 					<dynamiclist:footerTableTag/> 	
				</dynamiclist:buttonsTag>
	
	

	Configuración del contexto de spring:

		- Para la parte correspondiente a la capa web:
	
			<context:component-scan base-package="org.gvnix.dynamiclist.web" use-default-filters="false">
				<context:include-filter expression="org.springframework.stereotype.Controller" type="annotation"/>
			</context:component-scan>
	
			<mvc:annotation-driven />
			
			En el resolutor de fuente de mensajes se deben añadir los proporcionados en dynamiclist: 
			
			Por ejemplo si el basename de la aplicación es "messages" se debe añadir el basename "dynamiclistmessages"
				<bean id="messageSource" class="org.springframework.context.support.ResourceBundleMessageSource" >
					<property name="basenames">
						<list>
							<value>messages</value>
							<value>org.gvnix.dynamiclist.dynamiclistmessages</value>												
						</list>
					</property>
				</bean>	
			
		
		- Para la parte correspondiente a la declaración de beans:
			<context:component-scan base-package="org.gvnix.dynamiclist.service" />
			<context:component-scan base-package="org.gvnix.dynamiclist.jpa.dao" />
	
	
	En los controladores creados de la aplicación para poder utilizar la estructura de mapeo realizada en dynamiclist se debe incluir:
	
		- Incluir instancia del DynamiclistService utilizado para recuperar los metadatos de las entidades (como por ejemplo el nombre de sus atributos).
	
			@Autowired
    		protected DynamiclistService dynamiclistService = null;
    	
    	- Incluir las siguientes anotaciones en las funciones Crear, modificar, mostrar, eliminar y buscar:
	    	(Ejemplo tomado de un Controller para entidad Client, sustituir por la entidad a mostrar en dynamiclist)
    	
    		//crear
    		@RequestMapping(value="/client/form", method=RequestMethod.POST)
    		public void form(Client client, Model model) {
	    	
    		//modificar
    		@RequestMapping(value = "/client/{id}/form", method = RequestMethod.GET)
    		public String updateForm(@PathVariable("id") Integer id, ModelMap modelMap) { ... }
    	
    		//mostrar
    		@RequestMapping(value = "/client/{id}/show", method = RequestMethod.GET)
    		public String show(@PathVariable("id") Integer id, ModelMap modelMap) { ... }
    	
    		//borrar
    		@RequestMapping(value = "/client/{id}/delete", method = RequestMethod.GET)
    		public String show(@PathVariable("id") Integer id, @RequestParam(value = "page", required = false) Integer page, 
    		@RequestParam(value = "size", required = false) Integer size) { ... }
    	    	
    		//buscar
    		@RequestMapping(value="/client/search", method=RequestMethod.GET)
    		public String search(
	    		@RequestParam(value = "page", required = false) Integer page, 
	    		@RequestParam(value = "size", required = false) Integer size, 
	    		@RequestParam(value = "groupBy", required = false) String groupBy,
	    		@RequestParam(value = "orderBy", required = false) String orderBy,
	    		@RequestParam(value = "orderByColumn", required = false) String orderByColumn,
	    		HttpServletRequest request,
	    		Model modelMap) {
    	
    			try {
    				dynamiclistService.search(Client.class, page, size, groupBy, orderBy, orderByColumn, request, modelMap);    			
    			} catch (DynamiclistException e) { ... }
    		}
	
			La función 'search' el parámetro 'size' por defecto es de 10 elementos (es decir el número de filas por página del listado), 
			si se requiere un valor distinto es posible cambiarlo en la llamada de busqueda del servicio 'dynamiclistService'.
			 

Dependencias de proyecto
------------------------

spring 3.0.3.RELEASE
spring.security 2.0.5.RELEASE
aspectj1.6.6.RELEASE
javax.persistence 1.0.0
javax.servlet 2.4.0
javax.servlet.jsp 2.0.0
javax.servlet.jsp.jstl 1.1.2
org.apache.taglibs.standard 1.1.2
org.apache.commons.lang 2.1.0
org.apache.commons.beanutils 1.8.0

