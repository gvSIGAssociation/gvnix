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

// Global variables
var pkAct=null;
var estiloant='';
var estAct=null;
var aObjFilaVieja=null;
var aObjFilaActual=null;
var pkalternativa = null;

/**
 * 
 * @param pObjRow
 * @return
 */
function setPkAct(pObjRow){	
	aObjFilaActual = pObjRow;
	if(pObjRow.lang != ''){
		pkalternativa = pObjRow.lang;
	}
	if(aObjFilaVieja != null){
		aObjFilaVieja.className = estiloant;
	}
	if (pObjRow.className != 'fondoseleccion'){
		estiloant = pObjRow.className;
	}
	aObjFilaVieja = pObjRow;
	var aObjFilaNueva = document.getElementById(pObjRow.id);
	aObjFilaNueva.className = 'fondoseleccion';
	pkAct = aObjFilaNueva.id;
}

/**
 * 
 * @param pObjRow
 * @return
 */
function setState(pObjRow){	 
	if (pObjRow.id.indexOf(".") != -1){
		pObjRow.id = pObjRow.id.replace(".","");
	}
	estAct = document.getElementById(pObjFila.id + 'H').value;
}

/**
 * 
 * @param urlMapping
 * @return
 */
function dl_add(urlMapping){	
	window.location.href = urlMapping;
}

/**
 * 
 * @param action
 * @param handler
 * @return
 */
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

/**
 * 
 * @param urlBaseMapping
 * @param msj1
 * @return
 */
function dl_read(urlBaseMapping, msj1){
	if (pkAct != null) {
		window.location.href = urlBaseMapping + '/' + pkAct + '/show.html';
	} else {
		alert(msj1);
	}
}

/**
 * 
 * @param urlBaseMapping
 * @param msj1
 * @return
 */
function dl_write(urlBaseMapping, msj1){
	if (pkAct != null) {
		window.location.href = urlBaseMapping + '/' + pkAct + '/form.html';
	} else {
		alert(msj1);
	}
}

/**
 * 
 * @param urlBaseMapping
 * @param msj1
 * @param msj2
 * @return
 */
function dl_delete(urlBaseMapping, msj1, msj2){
	if (pkAct != null) {
		if (confirm(msj2)){
			window.location.href = urlBaseMapping + '/' + pkAct + '/delete.html';;
		}		
	} else {
		alert(msj1);
	}
}

/**
 * 
 * @param urlMapping
 * @return
 */
function dl_groupBy(urlMapping){
	windowsOpen(urlMapping,'400','500');
}

/**
 * 
 * @param column
 * @param urlMapping
 * @return
 */
function dl_groupBySearch(column, urlMapping){
	window.location.href = urlMapping + "?groupBy=" + column;
}

/**
 * 
 * @param urlMapping
 * @return
 */
function dl_order(urlMapping){	
	windowsOpen(urlMapping,'600','600');
}

/**
 * 
 * @param column
 * @param urlMapping
 * @return
 */
function dl_orderBySearch(column, urlMapping){
	window.location.href = urlMapping + "?orderBy=" + column;
}


var exporting = false;
/**
 * 
 * @param urlMapping
 * @param urlBase
 * @param msj1
 * @param msj2
 * @return
 */
function dl_export(urlMapping, urlBase, msj1, msj2){
	// Si esta exportando, no ejecutamos nada
	if(exporting == false){		
		exporting = true;		
		// comprobamos que no se exceden los registros
		var reg = document.getElementById("paginate.total").value;		
		if(reg > 20000){
			alert(msj1);
			exporting = false;
			return;
		}
		//call export controller
		window.location.href = urlMapping + "?urlBase=" + urlBase;
		window.setTimeout(noExport,5000);
	}else{
		alert(msj2);
	}
}

/**
 * Auxiliar function for dl_export
 * @return
 */
function noExport(){
	exporting = false;
}

/**
 * 
 * @param urlMapping
 * @return
 */
function dl_columns(urlMapping){
	windowsOpen(urlMapping,'550','600');
}

/**
 * 
 * @param columns
 * @param urlMapping
 * @return
 */
function dl_columnsSearch(columns, urlMapping){
	if(columns!='false'){
		window.location.href = urlMapping + '&columns=' + columns;
	} else {
		window.location.href = urlMapping + '&columns=' + columns;
	}	
}

// --- FILTERS ---

/**
 * @param urlMapping
 * @return
 */
function dl_createFilter(urlMapping){
	windowsOpen(urlMapping, '600', '600');
}

/**
 * 
 * @param urlMapping
 * @param msj1
 * @return
 */
function dl_deleteActualFilter(urlMapping, msj1){
	if(confirm(msj1)){
		window.location.href = urlMapping;
	}
}

/**
 * 
 * @param urlMapping
 * @return
 */
function dl_deleteFilter(urlMapping){
	windowsOpen(urlMapping, '600', '600');
}

/**
 * 
 * @param urlMapping
 * @return
 */
function dl_infoFilter(urlMapping){
	windowsOpen(urlMapping, '400', '455');
}

/**
 * 
 * @param urlMapping
 * @return
 */
function dl_saveFilter(urlMapping){
	windowsOpen(urlMapping, '600', '600');
}

/**
 * 
 * @param urlMapping
 * @param msj1
 * @return
 */
function dl_executeFilter(urlMapping, msj1){	
	var filters = document.getElementById('selectFilters');
	var index = filters.selectedIndex;	
	var idfilter = filters[index].id;
	var typeFilter = filters[index].value;
	
	if (idfilter == null || idfilter == '') {
		alert(msj1);
	} else {
		window.location.href = urlMapping + "&id=" + idfilter + "&typeFilter=" +  typeFilter;
	}
}

// --- SAVE/DELETE STATE ---

/**
 * @param urlMapping
 * @return
 */
function df_saveState(urlMapping){
	window.location.href = urlMapping;
}

/**
 * 
 * @param urlMapping
 * @param msj1
 * @return
 */
function dl_deleteState(urlMapping, msj1) {	
	if(confirm(msj1)){
		window.location.href = urlMapping;
	}
}


/*******************************/
/** Functions SELECT ACCTIONS **/
/*******************************/

/**
 * Method to override for execute the option selected in select actions of the ActionTag.
 *  
 * @return alert
 */
function dl_executeAction(selectActions) {	
	var textAlert = "Método dl_executeAction no implementado. \n\n"
	+ "Para una correcta visualización del estado de las filas " 
	+ "las cadenas que definen las acciones deben:\n"
	+ "Seguir el esquema PERMISO@SEAPLICA@CADENAESTADO donde:\n"
	+ "\t PERMISO = Id. del permiso en base de datos.\n"
	+ "\t SEAPLICA = SIMPLE / MULTIPLE / TOTAL.\n"
	+ "\t CADENAESTADO = TODOS / valorestado1%valorestado2|valorestado1%valorestado2...\n\n"
	+ "La barra (/) indica que puede ser cualquier valor de los enumerados.\n\n"
	+ "Debe declararse en el properties una entrada por cada acción de la forma:\n"
	+ "FUNCIONALIDADES.PERMISO.\n\n"
	+ "Cada una de estas cadenas debe ser una posición de una Collection.";
	
	alert(textAlert);
}



/**********************************/
/** Functions to work with dates **/
/**********************************/

/**
 * Validate type date with format dd/mm/aaaa
 */
function datevalidation(element, alertText){
	parseDate(element);
 	var bOk = true; 
 	if (element.value != "" && element.value != "dd/mm/aaaa"){ 
		bOk = bOk && (valAno(element)); 
		bOk = bOk && (valMes(element)); 
		bOk = bOk && (valDia(element)); 
		bOk = bOk && (valSep(element));
  		if (!bOk){ 
			if (alertText != "") {
				element.value = ""; 
				alert(alertText);
			} 
			return false;
	 	} 
	 	else return true;
 	} 
}

/**
 * 
 */
function parseDate(pStrDate){
	var lStrAux=pStrDate.value.replace("-","/");
	var lStrAux1=lStrAux.replace("-","/");
	var lObjAux1 = lStrAux1.split("/");
	
	if((lObjAux1[0])&&(lObjAux1[1])&&(lObjAux1[2])){
		var dia = lObjAux1[0].toString();
		var mes = lObjAux1[1].toString();
		var anyo = lObjAux1[2].toString();		
		switch(dia.length){
			case 1: dia = "0" + dia; break;
			case 2:	dia = dia; break;
			default: dia = dia.substring(dia.length-2,dia.length); break;		
		}	
		switch(mes.length) {
			case 1: mes = "0" + mes; break;
			case 2:	mes = mes; break;
			default: mes = mes.substring(mes.length-2,mes.length); break;		
		}		
		switch(anyo.length){
			case 1: anyo = "200" + anyo; break;
			case 2: anyo = "20" + anyo; break;
			case 3:	anyo = "2" + anyo; break;
			case 4:	anyo = anyo; break;	
			default: anyo = anyo.substring(anyo.length-4,anyo.length); break;		
		}
		pStrDate.value = dia+"/"+mes+"/"+anyo;
	}
	else{
		pStrDate.value = lStrAux1;
	}
}

/**
 * 
 * @param campo
 * @return
 */
function finMes(campo) { 
    var nMes = parseInt(campo.value.substr(3, 2), 10); 
    var nAno = parseInt(campo.value.substr(6), 10); 
    var nRes = 0; 
    switch (nMes) { 
    	case 1: nRes = 31; break; 
     	case 2: nRes = 28; break; 
     	case 3: nRes = 31; break; 
     	case 4: nRes = 30; break; 
	    case 5: nRes = 31; break; 
	    case 6: nRes = 30; break; 
	    case 7: nRes = 31; break; 
	    case 8: nRes = 31; break; 
	    case 9: nRes = 30; break; 
	    case 10: nRes = 31; break; 
	    case 11: nRes = 30; break; 
	    case 12: nRes = 31; break; 
    } 
	return nRes + (((nMes == 2) && (nAno % 4) == 0)? 1: 0); 
} 

/**
 * 
 * @param campo
 * @return
 */
function valDia(campo){ 
    var bOk = false; 
    var nDia = parseInt(campo.value.substr(0, 2), 10); 
    bOk = bOk || ((nDia >= 1) && (nDia <= finMes(campo))); 
    return bOk; 
} 

/**
 * 
 * @param campo
 * @return
 */
function valMes(campo){ 
    var bOk = false; 
    var nMes = parseInt(campo.value.substr(3, 2), 10); 
    bOk = bOk || ((nMes >= 1) && (nMes <= 12)); 
    return bOk; 
} 

/**
 * Valida un año que proviene de una fecha
 */
function valAno(campo){ 
    var bOk = true; 
    var nAno = campo.value.substr(6); 
    bOk = bOk && (nAno.length == 4); 
    if (bOk){ 
		for (var i = 0; i < nAno.length; i++){ 
    		bOk = bOk && esDigito(nAno.charAt(i)); 
    	} 
    } 
    return bOk; 
}
/**
 * 
 * @param campo
 * @return
 */
function valSep(campo){ 
    var bOk = false; 
    var sep1 = campo.value.charAt(2); 
    var sep2 = campo.value.charAt(5); 
    //bOk = bOk || ((sep1 == "-") && (sep2 == "-")); 
    bOk = bOk || ((sep1 == "/") && (sep2 == "/"));
    //bOk = bOk || ((sep1 == " ") && (sep2 == " "));  
    return bOk; 
} 
/**
 * 
 * @param sChr
 * @return
 */
function esDigito(sChr){ 
    var sCod = sChr.charCodeAt(0); 
    return (sCod > 47 && sCod < 58); 
}

/**************************************/
/** End functions to work with dates **/
/**************************************/


//--- UTILS ---
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

/**
 * order the options of the a html select
 * 
 * @param nomlista
 * @return
 */
function orderList(nomlista){
	var lista = document.getElementById(nomlista);
	var opcioneslista = lista.options;
	var vecaux = new Array();
	var vecord = new Array();
	var vecauxtext = new Array();
	var vecauxtextord = new Array();
	var lIntIdiceSelecc = document.getElementById(nomlista).selectedIndex;
    var lStrValorSeleccionado = null;
    if (lIntIdiceSelecc >= 0){    
        lStrValorSeleccionado = lista.value;   //Se obtiene el valor de la opción seleccionada (si existe)
                                                                        //para q al ordenar y cambiar de posición se seleccione
    }    
    var textoFijo;    
    for (i=0; i < opcioneslista.length; i++){
		vecaux[i] = document.getElementById(nomlista).options[i];
		vecauxtext[i] = vecaux[i].text;
		
		if(vecaux[i].value == ''){
			textoFijo = vecaux[i];
		}
	}	
	vecauxtextord = vecauxtext.sort();
	for (i=0; i < opcioneslista.length; i++){
		for (j=0; j < opcioneslista.length; j++){
			if(vecauxtextord[i].value != '' && vecauxtextord[i] == vecaux[j].text) {
				vecord[i] = vecaux[j];
			}
		}
	}
	var i=0;
	if(textoFijo != undefined){
		document.getElementById(nomlista).options[0] = textoFijo;
		document.getElementById(nomlista).options[0].id = textoFijo.id;
		i=1;
	}	
	for (j=0; j < vecord.length; j++){
		if(textoFijo == undefined || vecord[j].value != textoFijo.value){
			seleccion = i-1;
			document.getElementById(nomlista).options[i] = new Option(vecord[j].text,vecord[j].value,vecord[j].defaultSelected,vecord[j].selected);
			document.getElementById(nomlista).options[i].id = vecord[j].id;
			i++;
		}
    }    
    if (lStrValorSeleccionado != null){
    	document.getElementById(nomlista).value = lStrValorSeleccionado;
    }
}