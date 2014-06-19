
Addon GvNIX Datatables - rowOnTop
---------------------------------

* Componentes involucrados en el proceso:
** datatables/list.tagx
** datatables/table.tagx
** datatables/jQuery.dataTables.ext.gvnix.js
** datatables/jQuery.dataTables.ext.gvnix.rowontop.js
** datatables/jQuery.dataTables.ext.gvnix.editing.js
** *_Roo_GvNIXDatatables.aj
** DatatableUtils.java


Descripción de la funcionalidad
-----------------------------------------

* La funcionalidad rowOnTop permite visualizar el registro que acaba de ser creado/editado en la primera posición de la lista, así como seleccionarlo y mostrar sus detalles en caso de que los tuviese.


Flujo de trabajo: Creación de maestro en formulario independiente
------------------------------------------------------------------

* Durante el pintado de un componente Datatable con creación en formulario independiente, el fichero table.tagx se encarga de añadir el botón de "+" en la parte superior izquierda. Este botón "+", redirige a un formulario de creación independiente. En la URL a la que enlaza el botón "+" se encuentra el parámetro  "dtt_table_id_hash", el cual indica a qué Datatable corresponde esa llamada (para poder diferenciarlo en el retorno). Este parámetro es generado en base al *id* de la tabla.

* Creación del registro en el formulario independiente.

* Al guardar el registro, se accede al método "createDatatableDetail" del documento _Roo_GvNIXDatatables.aj y se añade un flash attribute con key "dtt_row_on_top_ids" y valor el id del nuevo registro creado, además del valor de "dtt_table_id_hash" recibido.

* Una vez guardado el registro, volvemos a mostrar el datatable. Para generar el Datatable, el fichero table.tagx comprueba si existe el parámetro "dtt_row_on_top_ids" en la petición. En caso de que exista generará el datatable con la configuración para el plugin-datatable 'rowsOnTop', que incluirá el id del registro recién creado identificado como 'asRowOnTopIds' y el identificador del Datatable al que pertenece la petición de creación identificado como 'asParentTableIdHash'.

* Durante la generación del Datatable, se inicializa el plugin-datatable "rowontop" desde el fichero "jQuery.dataTables.ext.gvnix.rowontop.js". Al acceder a la función "fnConstruct", en caso de que se detecte el anterior objeto añadido "rowsOnTop" se guardan los valores de "asRowOnTopIds" y "asParentTableIdHash" como parte de la configuración del datatable, para que estén siempre accesibles en el elemento generado.

* Una vez inicializado el widget datatable, realizará la petición de los datos al *controller*, entrando por el método "findAllXXX" del fichero "_Roo_GvNIXDatatables.aj" y utilizando el request de la petición se llama al método "getPropertyMap" y se añade a la variable "baseSearchValuesMap" el identificador del nuevo registro creado con el key "dtt_row_on_top_ids".

* Utilizando la anterior variable "baseSearchValuesMap" se utiliza el método "findByCriteria" incluído en "DatatableUtils.java". Éste método se encargará de obtener el valor del parámetro "dtt_row_on_top_ids" en caso de que esté presente en la petición. Una vez obtenido el id del último registro creado, se modifica la consulta para mostrar en primera posición el registro con el mismo id. Se devuelve el listado con el registro creado en la primera posición y se añade al objeto Datatable.

* Al pintar los datos en el widget datatable, se invoca el callback registrado en la función "_fnRegisterDrawCallback" del fichero "jQuery.dataTables.ext.gvnix.rowontop.js". En este momento, si el Datatable que se está pintando es el que invocó el formulario de creación, se marca el registro. En caso de que el datatable disponga de "rowclick" se seleccionará utilizando la función "fnSetLastClicked" y se mostrarán sus detalles, sin embargo, si no dispone de "rowclick" se marcará con una clase rowOnTop pero no se seleccionará. 



Flujo de trabajo: Creación de maestro en formulario en linea
-------------------------------------------------------------

* Durante el pintado de un componente Datatable con creación en formulario en linea, se utilizan las funciones declaradas en "jQuery.dataTables.ext.gvnix.editing.js" para generar el formulario de creación que aparece en la parte superior, y que nos permite generar nuevos registros sin abandonar la página. En concreto se utiliza la función "fnBeginCreate".

* La función "fnBeginCreate" solicita la página "create.jspx" y prepara el formulario de creación en base a los campos de la página recibida, añadiendo un botón de "Envío" para guardar los datos. 

* Después de rellenar los datos, se presiona el botón de envio, y se llama a la función "fnSendCreationForm" de  "jQuery.dataTables.ext.gvnix.editing.js". Se guardan los datos y mediante la función "fnSetRowsOnTop" inicializa el dataTable para que muestre el registro creado en primera posición y repinta la tabla, invocando a los mismos callbacks que en el caso del maestro.

* A diferencia de la creación en un formulario independiente, no necesita facilitar el identificador de la tabla, ya que siempre es el mismo datatable sobre el que se va a realizar el rowOnTop el que se repinta.



Flujo de trabajo: Creación de detalle en formulario independiente
-----------------------------------------------------------------

*Al igual que en la creación del maestro, durante el pintado de un componente Datatable con creación en formulario independiente, el fichero table.tagx se encarga de añadir el botón de "+" en la parte superior izquierda. Este botón "+", redirige a un formulario de creación independiente. En la URL a la que enlaza el botón "+" se encuentra el parámetro  "dtt_table_id_hash", el cual indica a qué Datatable corresponde esa llamada.

* Creación del registro en el formulario independiente.

* Al guardar el registro, se accede al método "createDatatableDetail" del documento _Roo_GvNIXDatatables.aj y se añade un flash attribute con key "dtt_row_on_top_ids" y valor el id del nuevo registro creado.

* Una vez guardado el registro, en primer lugar se volverá a mostrar el datatable maestro. Para generar el Datatable, el fichero table.tagx comprueba si existe el parámetro "dtt_row_on_top_ids" en la petición. En caso de que exista generará el datatable con el objeto 'rowsOnTop', que incluirá el id del registro recién creado identificado como 'asRowOnTopIds' y el identificador del Datatable al que pertenece la petición de creación identificado como 'asParentTableIdHash'.

* El maestro cargará mediante el "loadDetail" del fichero "list.tagx" los detalles relacionados. A estos detalles, se les enviará mediante el parámetro "dtt_row_on_top_ids" el id del registro que acaba de ser creado y se generará el detalle de la misma forma que el maestro.
 
* Ahora que se ha generado el datatable conservando el id del registro que ha sido creado y el identificador de la tabla que llamó al formulario de creación, obtenemos el listado de datos a mostrar. Para ello, se accede al método "findAllXXX" del fichero "_Roo_GvNIXDatatables.aj" y utilizando el request de la petición se llama al método "getPropertyMap" y se añade a la variable "baseSearchValuesMap" el identificador del nuevo registro creado con el key "dtt_row_on_top_ids".

* Utilizando la anterior variable "baseSearchValuesMap" se utiliza el método "findByCriteria" incluído en "DatatableUtils.java". Éste método se encargará de obtener el valor del parámetro "dtt_row_on_top_ids" en caso de que esté presente en la petición. Una vez obtenido el id del último registro creado, se modifica la consulta para mostrar en primera posición el registro con el mismo id. Se devuelve el listado con el registro creado en la primera posición y se añade al objeto Datatable.

* Al pintar los datos en el objeto datatable, se invoca el callback registrado en la función "_fnRegisterDrawCallback" del fichero "jQuery.dataTables.ext.gvnix.rowontop.js". En un primer momento se invocarán los callbacks del maestro. Utilizando el identificador del formulario que lanzó la creación, detectaremos que este no es el datatable al que pertenece el nuevo registro y no realizaremos ninguna acción. Si el detalle que se está pintando es el que invocó el formulario de creación, se marca el registro. En caso de que el datatable disponga de "rowclick" se seleccionará utilizando la función "fnSetLastClicked" y se mostrarán sus detalles, sin embargo, si no dispone de "rowclick" se marcará con una clase rowOnTop pero no se seleccionará. 

Flujo de trabajo: Creación de detalle en formulario en linea
------------------------------------------------------------

* La creación de detalles con formulario en linea funcionan del mismo modo que los maestros con formulario en linea. Esto se debe a que cuando existe la creación en linea, los nuevos registros añadidos siempre pertenecen al Datatable que se repinta y no es necesario guardar un identificador de tabla.


Flujo de trabajo: Actualización de maestro/detalle en formulario independiente
------------------------------------------------------------------------------

* La actualización de maestros en formularios independientes funciona del mismo modo que la creación. La principal diferencia es que al generar el widget datatable se añade el parámetro "dtt_table_id_hash" al botón de edición, tanto en la edición por fila como en la edición general, al igual que se añadía en el botón "+". Gracias a esto, identificaremos en todo momento qué datatable invocó el formulario de actualización.


Fujo de trabajo: Actualización de maestro/detalle en formulario en linea
------------------------------------------------------------------------

* Cuando se edita un registro en linea no se mueve a la primera posición.


ACTUALES PROBLEMAS: Detalle rowOnTop incorrecto
-----------------------------------------------

* El principal problema que aparece es el siguiente:

** Cuando se carga un listado, se búsca el atributo "dtt_row_on_top_ids". Si existe se muestra en la primera posición el registro con el id guardado en el atributo "dtt_row_on_top_ids". Al crear un maestro se muestra en primera posición de forma correcta. Si a ese maestro le asignamos un detalle, el detalle se muestra en primera posición de manera correcta. Si cambiamos la selección del maestro nos aparecerán los detalles asociados a la nueva selección si los tuviese. Al entrar en el formulario de creación del detalle y volver al listado del Datatable (sin haber creado ningún nuevo registro) aparece en el detalle, el registro asociado al maestro anterior.

** Este error es debido a que al volver a cargar el "list.tagx", todavía dispone del parámetro "dtt_row_on_top_ids" y se pone en primera posición el registro con id creado en el anterior maestro.
