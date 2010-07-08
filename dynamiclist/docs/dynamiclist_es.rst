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


Requirements
=============
Aplicación web basada en springmvc.


Analysis
=========

Las imágenes proporcionadas se deben añadir al directorio /images/ en la raíz del proyecto desplegado.
/images/


En el fichero 'web.xml' se debe añadir servlet para recuperar los recursos de los .jar (dynamiclis.js)
	<servlet>
		<servlet-name>Resource Servlet</servlet-name>
		<servlet-class>org.springframework.js.resource.ResourceServlet</servlet-class>
	</servlet>	
	<servlet-mapping>
		<servlet-name>Resource Servlet</servlet-name>
		<url-pattern>/resources/*</url-pattern>
	</servlet-mapping>

	
En el contexto de aplicación de spring se debe añadir:

	- Para la parte correspondiente a la capa web:
	
		<context:component-scan base-package="org.gvnix.dynamiclist.web" use-default-filters="false">
			<context:include-filter expression="org.springframework.stereotype.Controller" type="annotation"/>
		</context:component-scan>
	
		<mvc:annotation-driven />
		
	- Para la parte correspondiente a la declaración de beans:
		<context:component-scan base-package="org.gvnix.dynamiclist.service" />
	
	
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
    	public String search(Model model) { ... }
    	
    	 
En las vistas creadas que se desee utilizar el componente se debe añadir las referencias al tld del custom Tab y
el .js del código javascript.
	
	<%@ taglib prefix="dynamiclist" uri="dynamiclist" %>
	
	<script type="text/javascript" src="<c:url value="/resources/js/dynamicList.js" />"> </script> 


Ejemplo de utilización de los Tags de dynamiclist para el listado del mantenimiento de clientes:

	<dynamiclist:buttonsTag url_base="client" classObject="client">
 		<dynamiclist:actionsTag /> 	
 		<dynamiclist:headerTableTag>
 			<dynamiclist:tableTag list="${clients}"/>
 		</dynamiclist:headerTableTag>
 		<dynamiclist:footerTableTag/> 	
	</dynamiclist:buttonsTag>

	

Project Dependencies
-----------------------

spring.version 3.0.3.RELEASE
org.apache.el 6.0.20
javax.servlet 2.4.0
javax.servlet.jsp 2.0
javax.servlet.jsp.jstl 1.1.2
org.apache.taglibs.standard 1.1.2
org.apache.commons.lang 2.1.0
org.apache.commons.beanutils 1.8.0


