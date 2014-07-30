
Introducción
--------------

Este add-on permite añadir nuevos proveedores de seguridad a Spring Security, no solo los incluidos en el add-on sino aquellos que el desarrollador instale en Roo.

El add-on sigue el patrón SPI http://docs.oracle.com/javase/tutorial/sound/SPI-intro.html

Addon GvNIX Security
---------------------


* El comando "security provider add" solo estará disponible si previamente se ha ejecutado el comando "security setup".

* El comando dispondrá del parámetro "--name" en el cual se indica que proovedor de seguridad queremos utilizar y el parámetro "--package" en el que se indicará en qué paquete queremos instalar el proveedor.

* Los proovedores disponibles serán aquellos componentes osgi que implementen la interfaz SecurityProvider, por lo que se podrán añadir proveedores personalizados a través de osgi.

* La interfaz SecurityProvider cuenta con los métodos "getName()", "getDescription()", "install()" y "isInstalled()". El método getName nos devolverá el nombre del Proveedor. Este nombre es el que se muestra en el parámetro --name del comando. El método getDescription nos devolverá la descripción del Proovedor. El método isInstalled() se encargará de decirnos si ya se ha instalado el proovedor y el método install() es el que hará las acciones necesarias para instalar el proovedor.

* Al ejecutar el comando "security provider add --name XXX --package xxx.xxx.xxxx" llamará al método install() del proveedor seleccionado (por su nombre) que será el encargado de configurar y generar todo lo necesario para el proveedor.

Proveedor de SAFE 
--------------------

* El método install() del proveedor SAFE realizará las siguientes acciones:

	* Añadirá dependencia en el proyecto con el addon donde está implementado (en este caso se encuentra dentro del addon security)
        * Añadir el plugin maven de generación del cliente del ws en base al wsdl
	* Añadir propiedades al fichero pom.xml 

	* Generar la clase SafePasswordHandler.java vacía anotada con @GvNIXPasswordHandlerSAFE
	* Generar la clase SafeUser.java vacía anotada con @GvNIXUserSAFE
	* Generar la clase SafeUserAuthority.java vacía anotada con @GvNIXUserAuthoritySAFE
	* Generar la clase SafeProviderManager.java vacía anotada con @GvNIXProviderManagerSAFE

	* Copiar el fichero safe_client_sign.properties a MAIN_RESOURCES del proyecto y safe_client.properties a MAIN_RESOURCES
	* Modificar fichero applicationContext-security.xml para elegir como authentication-provider nuestra clase anotada con
	  @GvNIXProviderManagerSAFE y modificar applicationContext.xml para que lea las variables de forma correcta 

	* El desarrollador utilizará los comandos de dynamic configuration para crear (según el perfil) las propiedades necesarias para el 		  correcto funcionamiento de SAFE.


* Nota: Las clases vacías generadas con las nuevas anotaciones crearán ficheros .aj con los métodos correspondientes, necesarios para que funcione correctamente SAFE.

