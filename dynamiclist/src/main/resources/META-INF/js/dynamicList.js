/*
 * gvNIX. Spring Roo based RAD tool for Conselleria d'Infraestructures     
 * i Transport - Generalitat Valenciana
 * Copyright (C) 2010 CIT - Generalitat Valenciana
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */


//dl_add
function dl_add(action,handler){
	alert('dl_add');
}
function SDFalta(action,handler){
	try{
		var comboGlobal = document.getElementById('FILTROGLOBAL');
		var indice = comboGlobal.selectedIndex;
		var longitud = comboGlobal.length;

		var opcion = comboGlobal[indice].value;
		if (opcion == 'todos') {
			window.location.href = action + '.do?'+handler+'=generarPantalla&pStrModo=A';
		}
		else {
			window.location.href = action + '.do?'+handler+'=generarPantalla&pStrModo=A&filtroglobal=' + opcion;
		}
	}catch(e){
		window.location.href = action + '.do?'+handler+'=generarPantalla&pStrModo=A';
	}
}

//dl_read
function dl_read(){
	alert('read');
}

//dl_write
function dl_write(action,msj1,handler){
	alert('dl_write');
}
function SDFmodificacion(action,msj1,handler){
	try{
		if(!compruebaFilaDeshabilitada(aObjFilaActual)){
			if (pkAct != null) {
				var comboGlobal = document.getElementById('FILTROGLOBAL');
				var indice = comboGlobal.selectedIndex;
				var longitud = comboGlobal.length;

				var opcion = comboGlobal[indice].value;
				if (opcion == 'todos') { 
					window.location.href = action + '.do?'+handler+'=generarPantalla&pStrModo=M&pk=' + pkAct;
				}
				else { 
					window.location.href = action + '.do?'+handler+'=generarPantalla&pStrModo=M&filtroglobal=' + opcion + '&pk=' + pkAct;
				}
			} else {
				alert(msj1);
			}
		}else{
			 SDFvisualizacion(action,msj1,handler);
		}
	}catch(e){
		if (pkAct != null) {
				window.location.href = action + '.do?'+handler+'=generarPantalla&pStrModo=M&pk=' + pkAct;
		} else {
			alert(msj1);
		}
	}
}

//dl_delete
function dl_delete(action,msj1,msj2){
	alert('dl_delete');
}


function SDFbajatc(action,msj1,msj2){
	if (pkAct != null) {
		if (confirm(msj2)){ 
			window.location.href = action + '.do?funcion=delete&pk=' + pkAct;
		}
	} 
	else { 
		alert(msj1);
	} 
} 





//dl_groupBy
function dl_groupBy(columna,action,handler){
	alert('dl_groupBy');
}
function SDFmuestraAgrupar(columna,action,handler){
	if(columna!='false'){
		window.location.href = action + '.do?'+handler+'=obtenerListado&agrupar=' + columna + '&nivel=' + nivel + '&operaciontc=si';
	}
	else{
		window.location.href = action + '.do?'+handler+'=obtenerListado&agrupar=' + '&nivel=' + nivel + '&operaciontc=si';
	}
}
function SDFagruparTC(action,tabla,handler){
	abrirVentana(action + '.do?'+handler+'=SDFagrupar&tabla=' + tabla  + '&action=' + action,'400','500');
}



//dl_order
function dl_order(action,tabla,handler){
	alert('dl_order');
}

function SDFordenarTC(action,tabla,handler){
	abrirVentana(action + '.do?'+handler+'=SDFordenar&tabla=' + tabla + '&action=' + action,'600','600');
}
function SDFmuestraOrdenar(columnas,action,handler){
	if(columnas!='false'){
		window.location.href = action + '.do?'+handler+'=obtenerListado&sqlordenar=' + columnas + '&operaciontc=si';
	}
	else{
		window.location.href = action + '.do?'+handler+'=obtenerListado&sqlordenar=' + '&operaciontc=si';
	}
}



//dl_export
function dl_export(pStrMensajeLimiteRegistros){
	alert (pStrMensajeLimiteRegistros);
}

var exportando = 0;
function SDFexportarTC(pStrMensajeLimiteRegistros){
	// Si esta exportando, no ejecutamos nada
	if(exportando == 0){
		exportando = 1;

		// comprobamos que no se exceden los registros
		var reg = document.getElementById("TOTALREGISTROS").value;
		if(reg > 20000){
			alert(pStrMensajeLimiteRegistros);
			exportando = 0;
			return;
		}

		var url=window.location.href;
		url=url+'&excel=si';
		window.location.href=url;
		window.setTimeout( permitirExportarExcel,5000);
	}else{
		alert("Procés en marxa. Esperi resposta del sistema");
	}
}



//dl_columns
function dl_columns(action,tabla,handler){
	alert('dl_columns');
}

function SDFcolumnasTC(action,tabla,handler){
	var elegidas = document.getElementById('ELEGIDAS').value;
	abrirVentana(action + '.do?'+handler+'=SDFcolumnas&tabla=' + tabla + '&action=' + action + '&visibles=' + elegidas,'550','600');
}

function SDFmuestraColumnas(columnas,action,handler){
	if(columnas!='false'){
		window.location.href = action + '.do?'+handler+'=obtenerListado&columnas=' + columnas + '&operaciontc=si';
	}
	else{
		window.location.href = action + '.do?'+handler+'=obtenerListado&columnas=' + '&operaciontc=si';
	}
}



//dl_createFilter
function dl_createFilter(action,tabla,handler){
	alert('dl_createFilter');
}
function SDFfiltroTC(action,tabla,handler){
	abrirVentana(action + '.do?'+handler+'=SDFfiltro&tabla=' + tabla + '&action=' + action,'600','600');
}



//dl_deleteFilter
function dl_deleteFilter(msj1){
	alert('dl_deleteFilter');
}
function SDFquitarfiltroTC(msj1){
	if(confirm(msj1)){
		var url=window.location.href;
		url=url+'&quitafiltro=si';
		window.location.href=url;
	}
}


//dl_infoFilter
function dl_infoFilter(action,tabla,handler){
	alert('dl_infoFilter');
}
function SDFfiltroactualTC(action,tabla,handler){
	abrirVentana(action + '.do?'+handler+'=SDFfiltroactual&action=' + action + '&tabla=' + tabla,'400','455');
}


//dl_updateFilter
function dl_updateFilter(action,tabla,handler){
	alert('dl_updateFilter');
}
function SDFguardafiltroTC(action,tabla,handler){
	var url = action + '.do?'+handler+'=SDFguardafiltro&action=' + action + '&tabla=' + tabla;
	abrirVentana(url,'600','600');
}


//dl_deleteFilter
function dl_deleteFilter(action,tabla,handler){
	alert('dl_deleteFilter');
}
function SDFborrafiltroTC(action,tabla,handler){
	abrirVentana(action + '.do?'+handler+'=SDFborrafiltro&action=' + action + '&tabla=' + tabla,'600','600');
}

//df_saveState
function df_saveState(action,handler,msj1){
	alert('df_saveState');
}
function SDFguardaestadoTC(action,handler,msj1){
	var url=action + '.do?'+handler+'=obtenerListado&operaciontc=si';
	url=url+'&guardarconfiguracion=si';
	window.location.href=url;
}


//dl_deleteState
function dl_deleteState(action,handler,msj1,msj2){	
	if(confirm(msj1)){
		alert('dl_deleteState');
	}
}
function SDFborraestadoTC(action,handler,msj1,msj2){
	if(confirm(msj1)){
		var url=action + '.do?'+handler+'=obtenerListado&operaciontc=si';
		url=url+'&eliminarconfiguracion=si';
		window.location.href=url;
	}
}


function windowsOpen(url,height,width){
	 var str = "height=" + height + ",innerHeight=" + height;
	 str += ",width=" + width + ",innerWidth=" + width;
	 var ah = screen.availHeight - 30;
	 var aw = screen.availWidth - 10;
	 var xc = (aw - width) / 2;
	 var yc = (ah - height) / 2;
	 str += ",left=" + xc + ",screenX=" + xc;
	 str += ",top=" + yc + ",screenY=" + yc;
	 str += ",scrollbars=yes,resizable=yes";

	 window.open(url,"",str);
}