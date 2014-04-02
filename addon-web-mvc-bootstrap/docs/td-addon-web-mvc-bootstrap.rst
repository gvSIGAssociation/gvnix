
Addon Bootstrap
---------------------

* Para poder utilizar el comando "web mvc bootstrap setup" es necesario que se ejecute previamente el comando "web mvc jquery setup".

* Una vez instalado jquery ejecutamos el comando de instalación de bootstrap que hará las siguientes operaciones:

    - Importará los scripts básicos, imágenes y estilos básicos de bootstrap al proyecto gvNIX
    - Reemplazará el fichero standard.css para que aplique los nuevos estilos
    - Añade los tagx necesarios para bootstrap y modifica los tagx necesarios.
    - Actualiza los layouts para seguir la estructura HTML de bootstrap3
    - Actualiza los ficheros JSP comunes (index.jspx, footer.jspx, header.jspx, etc..)
    - Actualiza TODAS las vistas jsp de la aplicación para que utilicen los tags de JQuery (create, update, show, list, finders)
    - Comprueba si está instalado el addon de datatables. Si está instalado genera los recursos necesarios, si no, no los instala.
    - Comprueba si está instalada la seguridad (security setup). Si está instalado modifica el fichero login.jspx

* Una vez ejecutado este comando ya está aplicado bootstrap en el proyecto.

*En caso de añadir datatables, o aplicar seguridad al proyecto gvNIX, será necesario aplicar el comando web mvc bootstrap update para actualizar/importar los recursos necesarios para que toda la aplicación aplique bootstrap.

*En caso de añadir una nueva entidad y aplicar scaffold para generar la capa web, ejecutando el comando web mvc bootstrap update también se actualizarán los jspx para que utilice los tags de jquery.
Quickstart

*Creado el fichero bootstrap.roo y añadido al addon. Al arrancar la consola se puede ejecutar script --file bootstrap.roo y creará un proyecto de ejemplo aplicando bootstrap.
