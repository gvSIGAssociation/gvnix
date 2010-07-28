<%@ page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>

<html>

<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<title><spring:message code="selectProperties.title" /></title>
<link rel="stylesheet" type="text/css" href="<c:url value="/css/estilos.css"/>" />
</head>

<body>	
<script type="text/javascript" src="<c:url value="/resources/js/dynamicList.js" />"> </script>

<script type="text/javascript">

//Funciones para pasar elementos de dos listas de selección(selectList y pickList) de una a otra
var sortSelect = false; // Se ordena al final llamando a ordenaLista
var sortPick = false;  // Se ordena al final llamando a ordenaLista
var singleSelect = true;
var ordenar = true;

// Invocar a esta función al cargar la página
function initIt(list1,lista2) {
  var selectList = document.getElementById(lista1);
  var selectOptions = selectList.options;
  var selectIndex = selectList.selectedIndex;
  var pickList = document.getElementById(list2);
  var pickOptions = pickList.options;
  
  pickOptions[0] = null;  // Remove initial entry from picklist (was only used to set default width)
  if (!(selectIndex > -1)) {
    selectOptions[0].selected = true;  // Set first selected on load
    selectOptions[0].defaultSelected = true;  // In case of reset/reload
  }
  selectList.focus();  // Set focus on the selectlist
  
}


function upElementList(list){
  	var selectList = document.getElementById(list);
  	var selectIndex = selectList.selectedIndex;
  	var selectOptions = selectList.options;
	if (selectIndex > -1) {
		if (selectIndex > 0) {
			tempText = selectOptions[selectIndex-1].text;
        	tempValue = selectOptions[selectIndex-1].value;
        	selectOptions[selectIndex-1].text = selectOptions[selectIndex].text;
        	selectOptions[selectIndex-1].value = selectOptions[selectIndex].value;
        	selectOptions[selectIndex].text = tempText;
        	selectOptions[selectIndex].value = tempValue;
        	document.getElementById(list).selectedIndex = selectIndex - 1;
		}
	}
}

function downElementList(list){
  	var selectList = document.getElementById(list);
  	var selectIndex = selectList.selectedIndex;
  	var selectOptions = selectList.options;
  	var selectLength = selectOptions.length;

	if (selectIndex > -1) {
		if (selectIndex < selectLength - 1) {
			tempText = selectOptions[selectIndex+1].text;
        	tempValue = selectOptions[selectIndex+1].value;
        	selectOptions[selectIndex+1].text = selectOptions[selectIndex].text;
        	selectOptions[selectIndex+1].value = selectOptions[selectIndex].value;
        	selectOptions[selectIndex].text = tempText;
        	selectOptions[selectIndex].value = tempValue;
        	document.getElementById(list).selectedIndex = selectIndex + 1;
		}
	}
}

// Pasa el elemento seleccionado de la list1 a la list2
// Move the selected element of list1 to list2
function addSelectedElementList(list1,list2) {
  var selectList = document.getElementById(list1);
  var selectIndex = selectList.selectedIndex;
  var selectOptions = selectList.options;
  var pickList = document.getElementById(list2);
  var pickOptions = pickList.options;
  var pickOLength = pickOptions.length;
  // An item must be selected
  while (selectIndex > -1) {   
    pickOptions[pickOLength] = new Option(selectList[selectIndex].text);
    pickOptions[pickOLength].value = selectList[selectIndex].value;
    // If single selection, remove the item from the select list
    if (singleSelect) {
      selectOptions[selectIndex] = null;
    }
    if (sortPick) {
      var tempText;
      var tempValue;
      // Sort the pick list
      while (pickOLength > 0 && pickOptions[pickOLength].value < pickOptions[pickOLength-1].value) {
        tempText = pickOptions[pickOLength-1].text;
        tempValue = pickOptions[pickOLength-1].value;
        pickOptions[pickOLength-1].text = pickOptions[pickOLength].text;
        pickOptions[pickOLength-1].value = pickOptions[pickOLength].value;
        pickOptions[pickOLength].text = tempText;
        pickOptions[pickOLength].value = tempValue;
        pickOLength = pickOLength - 1;
      }
    }
    selectIndex = selectList.selectedIndex;
    pickOLength = pickOptions.length;
  }
  /*if (ordenar){
  	ordenaLista(lista2);
  }*/
  if (selectOptions[0] != null)
    selectOptions[0].selected = true;

    return true;
}

// Borra un elemento de la lista2 y lo pasa a la lista1
// Delete selected element of list2 and move to list1
function deleteSelectedElementList(list1, list2) {
  var selectList = document.getElementById(list1);
  var selectOptions = selectList.options;
  var selectOLength = selectOptions.length;
  var pickList = document.getElementById(list2);
  var pickIndex = pickList.selectedIndex;
  var pickOptions = pickList.options;

  while (pickIndex > -1) {
    // If single selection, replace the item in the select list
    if (singleSelect) {
      selectOptions[selectOLength] = new Option(pickList[pickIndex].text);
      selectOptions[selectOLength].value = pickList[pickIndex].value;
    }
    pickOptions[pickIndex] = null;
    if (singleSelect && sortSelect) {
      var tempText;
      var tempValue;
      // Re-sort the select list
      while (selectOLength > 0 && selectOptions[selectOLength].value < selectOptions[selectOLength-1].value) {
        tempText = selectOptions[selectOLength-1].text;
        tempValue = selectOptions[selectOLength-1].value;
        selectOptions[selectOLength-1].text = selectOptions[selectOLength].text;
        selectOptions[selectOLength-1].value = selectOptions[selectOLength].value;
        selectOptions[selectOLength].text = tempText;
        selectOptions[selectOLength].value = tempValue;
        selectOLength = selectOLength - 1;
      }
    }
    pickIndex = pickList.selectedIndex;
    selectOLength = selectOptions.length;
  }
  /*if (ordenar){
  	ordenaLista(lista1);
  }*/
  if (pickOptions[0] != null)
  pickOptions[0].selected = true;

  return true;
}

function selIt(list2, alertText) {
  var pickList = document.getElementById(list2);
  var pickOptions = pickList.options;
  var pickOLength = pickOptions.length;
  var text = "";

  if (pickOLength < 1) {
    alert(alertText);
    return false;
  }
  else {
  	for (var i = 0; i < pickOLength; i++) {
   		pickOptions[i].selected = true;
    	var nomcolumn = pickOptions[i].value;
    	if ((i + 1) == pickOLength) {
    		text = text + nomcolumn;
    	}
    	else {
	    	text = text + nomcolumn + ",";
    	}
  	}
  }
  return text;
}

//urlmapping; 
function searchColumns(columns, urlMapping) {	
	if (columns != false){
		//document.getElementById("oculto").value = cadena;
	  	//window.opener.SDFmuestraColumnas(cadena, informeactual, '<bean:write name="pStrHandler"/>');
	  	window.opener.dl_columnsSearch(columns, urlMapping);
	 	window.close();
 	}
}


</script>


<div id="container" align="center">
	
	<div id="header" >
		<table width="100%" border="0" cellpadding="0" cellspacing="0" >
			<tr bgcolor="#f5f5f5"><td height="62" align="center"><img src="images/headerDynamiclist.gif" height="62" alt="Header Image DYNAMICLIST"/></td></tr>
		</table>
	</div>
	
	<TABLE border="0" cellspacing="0" width="500">
		<TBODY>
			<tr>
				<td class="tituloFormulario">
					<spring:message code="selectProperties.title" /><BR><BR>
				</td>
			</tr>
			<TR>
				<TD width="306">
					<TABLE border="0" cellpadding="0" cellspacing="0">
						<TBODY>
						<TR>
							<TD class="etiquetaTexto4">
								<spring:message code="selectProperties.notDisplay" />
							</TD>
							<TD></TD>
							<TD class="etiquetaTexto4">
								<spring:message code="selectProperties.display" /><BR>
							</TD>
						</TR>
						<TR>
							<TD>
								<select multiple size="20" name="columnsDisplayNone" id="list1" style="width: 250px" class="etiquetaTextotxCampo7">	
									<c:forEach items="${mapColumnsDisplayNone}" var="column">			
										<option value='<c:out value="${column.value}"/>'>
											<spring:message code="${column.key}" text="${column.key}" />
										</option>
									</c:forEach>
								</select>
							</TD>
							<TD align="center">
								<table border="0" cellpadding="1" cellspacing="0" width="14"
									height="14" class="txentrar" onclick="addSelectedElementList('list1','list2')"
									onMouseOver="this.className='txentrar2'"
									onMouseOut="this.className='txentrar'">
									<tr><td><img alt="<spring:message code="selectProperties.add"/>" 
										src='<c:out value="${imagesPath}"/>/flechablancaderecha.gif' name="Derecha"
										width="10" height="11" border="0" id="Derecha"></td></tr>
								</table>
								<BR>
								<table border="0" cellpadding="1" cellspacing="0" width="14"
									height="14" class="txentrar" onclick="deleteSelectedElementList('list1','list2')"
									onMouseOver="this.className='txentrar2'"
									onMouseOut="this.className='txentrar'">
									<tr><td><img alt="<spring:message code="selectProperties.delete"/>"
										src='<c:out value="${imagesPath}"/>/flechablancaizquierda.gif'
										name="Izquierda" width="10" height="11" border="0"
										id="Izquierda"></td></tr>
								</table>
							</TD>
							<TD width="0">
								<select multiple class="etiquetaTextotxCampo7" size="20" name="visibles" id="list2" style="width: 250px">
									<c:forEach items="${mapColumns}" var="column">			
										<option value='<c:out value="${column.value}"/>' 
											<c:if test="${column.value == selectedColumn}">
							    				selected
											</c:if> 
										>
											<spring:message code="${column.key}" text="${column.key}" />
										</option>
									</c:forEach>	
								</select>
							</TD>
							<td>
								<table border="0" cellpadding="1" cellspacing="0" width="14"
									height="14" class="txentrar" onclick="upElementList('list2')"
									onMouseOver="this.className='txentrar2'"
									onMouseOut="this.className='txentrar'">
									<tr><td><img alt="<spring:message code="selectProperties.up"/>"
										src='<c:out value="${imagesPath}"/>/flechablancaarriba.gif' name="Arriba"
										width="10" height="11" border="0" id="Arriba"></td></tr>
								</table> 
								<BR>
								<table border="0" cellpadding="1" cellspacing="0" width="14"
									height="14" class="txentrar" onclick="downElementList('list2')"
									onMouseOver="this.className='txentrar2'"
									onMouseOut="this.className='txentrar'">
									<tr><td><img alt="<spring:message code="selectProperties.down"/>"
										src='<c:out value="${imagesPath}"/>/flechablancaabajo.gif' name="Abajo"
										width="10" height="11" border="0" id="Abajo"></td></tr>
								</table>
							</td>
						</TR>
						</TBODY>
					</TABLE>
				</TD>
			</TR>
			<TR>
				<TD width="306"><BR><BR></TD>
			</TR>
			<TR>
				<TD align="right">
				<TABLE width="162">
					<TBODY>
						<TR>
							<TD height="11">
							<table cellpadding="0" cellspacing="0" border="0"
								class="etiquetaTexto4">
								<tr>
									<td>
										<table border="0" cellpadding="1" cellspacing="0" width="69"
											height="14" id="botonaceptar"		
											onclick="searchColumns(selIt('list2','<spring:message code="selectProperties.acceptError" />'), 
												'<c:out value="${urlMapping}"/>')"
											class="txentrar" onMouseOver="this.className='txentrar2'"
											onMouseOut="this.className='txentrar'">
											<tr><td style="cursor: pointer"><spring:message code="button.accept"/></td></tr>
										</table>
									</td>
									<td>
										<table border="0" cellpadding="1" cellspacing="0" width="69"
											height="14" class="txentrar" onclick="window.close()"
											onMouseOver="this.className='txentrar2'"
											onMouseOut="this.className='txentrar'">
											<tr><td style="cursor: pointer"><spring:message code="button.cancel"/></td></tr>
										</table>
									</td>
								</tr>
							</table>
							</TD>
						</TR>
					</TBODY>
				</TABLE>
				</TD>
			</TR>
		</TBODY>
	</TABLE>
</div>

</body>
<script>
	orderList('list1');
</script>
</html>