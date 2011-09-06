<%@ page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>

<html>

<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<title><spring:message code="filter.title" /></title>
<link rel="stylesheet" type="text/css" href="<c:url value="/css/estilos.css"/>" />
</head>
<body>
<script type="text/javascript" src="<c:url value="/resources/js/dynamicList.js" />"> </script>
	
<script type="text/javascript">
var concatenate = true;
var booUndo = false;
var booYO = false;
var finished = false;
var previousHumanTextFilter;
var previousTextFilter;
var maxStringFilter = 800;


function initializeVariables(){
	if (document.getElementById('humanTextFilter').value != ''){
		concatenate = false;
		booYO = true;
		finished = true;
		document.getElementById("valueFilter").value="";
	}
}


/**
 * 
 */	
function changeDataType(columns){
	var options = columns.options;
	var selectedIndex = columns.selectedIndex;
	var select_text = document.getElementById('select_text');
	var select_number = document.getElementById('select_number');
	var select_date = document.getElementById('select_date');

	var arrclave = options[selectedIndex].value.split('|');
	var name = arrclave[0];
	var basicType = arrclave[1];
	
	document.getElementById("valueFilter").value = "";
		
	switch(basicType){
	 	case 'TEXT':
	 		select_text.style.display='block';
	 		select_date.style.display='none';
	 		select_number.style.display='none';
	 		break;
	 	case 'NUMBER':
	 		select_number.style.display='block';
	 		select_date.style.display='none';
	 		select_text.style.display='none';
	 		break;
	 	case 'DATE':
	 		select_date.style.display='block';
	 		select_text.style.display='none';
	 		select_number.style.display='none';
	 		break;
 	}
}

/**
 * 
 */
function addCondition(){
	var value = document.getElementById(checkDisplaySelect()).value;
	
	if(value != 'filter.condition.isNull' && value != 'filter.condition.isNotNull') {
		if(document.getElementById('valueFilter').value == '') {
			alert('<spring:message code="filter.alert.expresionNull"/>');
		}
		else{
			addValueFilter(document.getElementById('valueFilter'));
		}
	}
	else{
		addValueFilter(null);
	}
}

/**
 * 
 */
function putY(){
	var humanTextFilter = document.getElementById('humanTextFilter');
	var textFilter = document.getElementById('textFilter');

	if (booYO){
		previousHumanTextFilter = humanTextFilter;
		previousTextFilter = textFilter.value;
		
		humanTextFilter.value = humanTextFilter.value + ' <spring:message code="filter.button.y"/> ';
		textFilter.value = textFilter.value + " AND ";
		concatenate = true;
		booYO=false;
		finished=false;
	}
	else{
		if(textFilter.value == ''){
			alert('<spring:message code="filter.alert.addExpresion"/>');
		}
		else{
			alert('<spring:message code="filter.alert.notconcatenate"/>');
		}
	}
}

/**
 * 
 */
function putO(){
	var humanTextFilter = document.getElementById('humanTextFilter');
	var textFilter = document.getElementById('textFilter');
	
	if (booYO){
		previousHumanTextFilter = humanTextFilter.value;
		previousTextFilter = textFilter.value;		
		humanTextFilter.value = humanTextFilter.value + ' <spring:message code="filter.button.o"/> ';
		textFilter.value = textFilter.value + " OR ";
		concatenate = true;
		booYO=false;
		finished=false;
	}
	else{
		if(textFilter.value == ''){
			alert('<spring:message code="filter.alert.addExpresion"/>');
		}
		else{
			alert('<spring:message code="filter.alert.notconcatenate"/>');
		}
	}
}

/*
 * return the name of condition select active
 */
function checkDisplaySelect() {
	var seltexto = document.getElementById('select_text');
	var selnumero = document.getElementById('select_number');
	var selfecha = document.getElementById('select_date');	
	if(seltexto.style.display=='block'){
		return "select_text";
	}
	else{
		if(selnumero.style.display=='block'){
			return "select_number";
		}
		else{
			return "select_date";
		}
	}
}

/**
 * 
 */
function undo(){
		document.getElementById('humanTextFilter').value = "";
		document.getElementById('textFilter').value = "";	
		concatenate = true;
		booUndo = true;
		booYO = false;
		finished = false;
		document.getElementById("valueFilter").value = "";
		document.getElementById('columns').selectedIndex = 0;
}


/**
 * 
 */
function addValueFilter(valueFilter) {
	var columns = document.getElementById('columns');
	var opcolumns = columns.options;
	var selopcolumns = columns.selectedIndex;
	var valueSel = opcolumns[selopcolumns].value;	
	var arrvalueSel = valueSel.split('|');
	var activeSelect = document.getElementById(checkDisplaySelect());
	var options = activeSelect.options;
	var selOption = options.selectedIndex;
			
	var lefthidden = arrvalueSel[0];
	var leftvisible = opcolumns[selopcolumns].text;
	var condition = options[selOption].text;	
	var conditionProperties = options[selOption].value;
			
	var humanTextFilter = document.getElementById('humanTextFilter');
	var textFilter = document.getElementById('textFilter');
	
	if (concatenate){
		var activeSelect = checkDisplaySelect();
		if (activeSelect == "select_number"){
			if(isNaN(document.getElementById("valueFilter").value)){
				alert('<spring:message code="filter.alert.valueGreaterThan0"/>');
				document.getElementById("valueFilter").focus(); 
				return false;
			}
		}
		if (activeSelect == "select_date"){
			if (datevalidation(document.getElementById("valueFilter"),
					'<spring:message code="filter.alert.errorFormatDate"/>') == false) {
				document.getElementById("valueFilter").focus(); 
				return false;
			}
		}

		// Comprobamos que no exceda de maxStringFilter caracteres, porque tendremos problemas en la base de datos.
		if (valueFilter != null){

			var inputText = valueFilter.value;
			var visible = humanTextFilter.value + leftvisible + " " + condition + " " + inputText;

			if (activeSelect == "select_date") {
				//cambiar formato de la fecha de dd/mm/yyyy a yyyy-mm-dd para consultas JPQL				
				var aux = inputText.split('/');
				inputText = aux[2] + "-" + aux[1] + "-" + aux[0];				
			}

			var hidden = textFilter.value + getLeftValueQL(lefthidden, activeSelect, conditionProperties) + " " 
				+ conditionQL(conditionProperties) + " " 
				+ getRightValueQL(activeSelect, inputText, conditionProperties);

			// Comprobamos el tamaño de los filtros
			if(visible.length >= maxStringFilter || hidden.length >= maxStringFilter){
				deleteLastIO();
				alert('<spring:message code="filter.alert.limit"/>');
				return false;
			}
			
			humanTextFilter.value = visible;
			textFilter.value = hidden;
			
			if (activeSelect == "select_text"){
				var varmay = valueFilter.value;
				var varmin = varmay.toLowerCase();
				valueFilter.value = varmin;
			}
		}
		else{

			var visible = humanTextFilter.value + leftvisible + " " + condition;
			var hidden = textFilter.value + getLeftValueQL(lefthidden, activeSelect, conditionProperties) + " " + conditionQL(conditionProperties);

			// Comprobamos el tamaño de los filtros
			if(visible.length >= maxStringFilter || hidden.length >= maxStringFilter){
				alert('<spring:message code="filter.alert.limit"/>');
				deleteLastIO();
				return false;
			}
			
			humanTextFilter.value = visible;
			textFilter.value = hidden;
		}
		concatenate = false;
		booYO = true;
		finished = true;
		document.getElementById("valueFilter").value="";
	}
	else{		
		alert('<spring:message code="filter.alert.noExistAndOr"/>');
	}
}

/**
 * 
 */
function getLeftValueQL(leftValue, activeSelect, conditionProperties){
	var leftValueQL;	
	if (conditionProperties == 'filter.condition.isNull' ||
			conditionProperties == 'filter.condition.isNotNull'){
		leftValueQL = leftValue;		
	} else {
		if (activeSelect == 'select_text') {
			leftValueQL = "LOWER(" + leftValue + ")";			
		}
		else if (activeSelect == 'select_date') {
			leftValueQL = "SUBSTRING(CONCAT(" + leftValue + ", ''), 1, 10)";			
		} 
		else {
			leftValueQL = leftValue;
		}
	}	 
	return leftValueQL;
}


/**
 * 
 */
function getRightValueQL(activeSelect, rightValue, conditionProperty){
	
	var rightValueQL;
	switch(conditionProperty){
	 	case 'filter.condition.begin':
	 		rightValueQL = beginRight(activeSelect) + rightValue.toLowerCase().trim() + "%" + finishRight(activeSelect);
	 		break;
	 	case 'filter.condition.end':
	 		rightValueQL = beginRight(activeSelect) + "%" + rightValue.toLowerCase().trim() + finishRight(activeSelect);
	 		break;
	 	case 'filter.condition.contain':
	 		rightValueQL = beginRight(activeSelect) + "%" + rightValue.toLowerCase().trim() + "%" + finishRight(activeSelect);
	 		break;
	 	case 'filter.condition.isNull':
	 		rightValueQL = beginRight(activeSelect) + rightValue.toLowerCase().trim() + finishRight(activeSelect);
	 		break;
	 	case 'filter.condition.isNotNull':
	 		rightValueQL = beginRight(activeSelect) + rightValue + finishRight(activeSelect);
	 		break;
	 	case 'filter.condition.bigger':
	 		rightValueQL = beginRight(activeSelect) + rightValue.trim() + finishRight(activeSelect);
	 		break;
	 	case 'filter.condition.biggerEqual':
	 		rightValueQL = beginRight(activeSelect) + rightValue.trim() + finishRight(activeSelect);
	 		break;
	 	case 'filter.condition.smaller':
	 		rightValueQL = beginRight(activeSelect) + rightValue.trim()+ finishRight(activeSelect);
	 		break;
	 	case 'filter.condition.smallerEqual':
	 		rightValueQL = beginRight(activeSelect) + rightValue.trim() + finishRight(activeSelect);
	 		break;
	 	case 'filter.condition.distint':
	 		rightValueQL = beginRight(activeSelect) + rightValue.toLowerCase().trim() + finishRight(activeSelect);
	 		break;
	 	case 'filter.condition.equal':
	 		rightValueQL = beginRight(activeSelect) + rightValue.toLowerCase().trim() + finishRight(activeSelect);
	 		break;
 	} 	
	return rightValueQL;
}

/**
 * 
 */
function conditionQL(conditionProperties){
	var conditionQL;
	switch(conditionProperties){
	 	case 'filter.condition.begin': conditionQL = "LIKE"; break;
	 	case 'filter.condition.end': conditionQL = "LIKE"; break;
	 	case 'filter.condition.contain': conditionQL = "LIKE"; break;
	 	case 'filter.condition.isNull': conditionQL = "IS NULL"; break;
	 	case 'filter.condition.isNotNull': conditionQL = "IS NOT NULL"; break;
	 	case 'filter.condition.bigger':	conditionQL = ">"; break;
	 	case 'filter.condition.biggerEqual': conditionQL = ">="; break;
	 	case 'filter.condition.smaller': conditionQL = "<"; break;
	 	case 'filter.condition.smallerEqual': conditionQL = "<="; break;
	 	case 'filter.condition.distint': conditionQL = "<>"; break;
	 	case 'filter.condition.equal': conditionQL = "="; break;
 	}
	return conditionQL;
}

/**
 * Delete the last IO of filter 
 */
function deleteLastIO() {
	document.getElementById('humanTextFilter').value = previousHumanTextFilter;
	document.getElementById('textFilter').value = previousTextFilter;
	concatenate = false;
	booYO = true;
	finished = true;
}

/**
 * 
 */
function beginRight(activeSelect){
	var text = "";
	switch(activeSelect){
	 	case 'select_text':	text = "'";	break;
	 	//case 'select_number': text = ""; break;
	 	case 'select_date':	text = "'";	break;
	}
	return text;	
}

/**
 * 
 */
function finishRight(activeSelect){
	var text = "";
	switch(activeSelect) {
	 	case 'select_text':	text = "'";	break;
	 	//case 'select_number': text = ""; break;
	 	case 'select_date':	text = "'";	break;
	 }
	 return text;	
}

/**
 * 
 */
function filterSearch(){
	var humanTextFilter = document.getElementById('humanTextFilter');
	var textFilter = document.getElementById('textFilter');
	if (textFilter.value == ''){
		if(confirm('<spring:message code="filter.alert.filterNull"/>')){
			//"<bean:message key="filtros.nodefinido"/>"
			//window.opener.quitarfiltrosinaviso();			
			window.close();
		}
	}
	else{
		if (finished == true){
			var yamodificado = false;
			<%//if (lStrWhereSql.equalsIgnoreCase("")) {%>
	 			textFilter.value = "(" + textFilter.value + ")";
	 			yamodificado=true;
	 		<%//}%>
	 		
	 		if(booUndo == true && yamodificado == false){
	 			textFilter.value = "(" + textFilter.value + ")";
	 		}
	 		
	 		var visible = document.createElement("input");
	 		visible.setAttribute('type','hidden');
	 		visible.setAttribute('id','humanTextFilter');
	 		visible.setAttribute('name','humanTextFilter');
	 		visible.setAttribute('value',humanTextFilter.value);

	 		var hidden = document.createElement("input");
	 		hidden.setAttribute('type','hidden');
	 		hidden.setAttribute('id','textFilter');
	 		hidden.setAttribute('name','textFilter');
	 		hidden.setAttribute('value',textFilter.value);

	 		var hiddenReset = document.createElement("input");
	 		hiddenReset.setAttribute('type','hidden');
	 		hiddenReset.setAttribute('id','resetSelectedFilter');
	 		hiddenReset.setAttribute('name','resetSelectedFilter');
	 		hiddenReset.setAttribute('value',booUndo);
	 		
	 		
	 		
	 		var f = document.createElement("form");
	 		f.setAttribute("name", "dynamiclistForm");
	 		f.setAttribute("method", "POST");	 		
	 		f.setAttribute("action", '<c:out value="${urlMapping}"/>');
	 		f.appendChild(visible);
	 		f.appendChild(hidden);
	 		f.appendChild(hiddenReset);
	 		
	 		var formOpener = window.opener.document.body.appendChild(f);
	 		formOpener.submit();
	 		window.close();		 			 		
        }
        else{
        	alert('<spring:message code="filter.alert.terminateyo"/>');        	
        }
	}
}

</script>
	
<div id="container" align="center">
	
	<div id="header" >
		<table width="100%" border="0" cellpadding="0" cellspacing="0" >
			<tr bgcolor="#f5f5f5"><td height="62" align="center"><img src="images/headerDynamiclist.gif" height="62" alt="Header Image DYNAMICLIST"/></td></tr>
		</table>
	</div>
	
	<table width="550">
		<tr><td class="tituloFormulario"><spring:message code="filter.title" /></td></tr>
		<tr><td height="5"></td></tr>
		
		<tr><td class="etiquetaTexto4">
			<spring:message code="filter.properties" />
		</td></tr>
		
		<tr><td>
			<select class="etiquetaTextotxCampo7" size="15" name="columns" id="columns" style="width:550px" onchange="changeDataType(this)">
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
		</td></tr>
		
		<tr><td height="20"></td></tr>
		
		<tr><td>
			<table width="100%">
				<tr>
					<td class="etiquetaTexto4" width="101"><spring:message code="filter.condition" /></td>
					<td style="width: auto;">
						<!-- conditions selects -->	
						<select style="display:none" Id="select_text" class="etiquetaTextotxCampo5">
							<option value='filter.condition.contain'><spring:message code="filter.condition.contain" /></option>
							<option value='filter.condition.begin'><spring:message code="filter.condition.begin" /></option>
							<option value='filter.condition.end'><spring:message code="filter.condition.end" /></option>
							<option value='filter.condition.isNull'><spring:message code="filter.condition.isNull" /></option>
							<option value='filter.condition.isNotNull'><spring:message code="filter.condition.isNotNull" /></option>
							<option value='filter.condition.equal'><spring:message code="filter.condition.equal" /></option>
							<option value='filter.condition.distint'><spring:message code="filter.condition.distint" /></option>	
						</select>
						
						<select style="display:none" Id="select_number" class="etiquetaTextotxCampo5">
							<option value='filter.condition.equal'><spring:message code="filter.condition.equal" /></option>
							<option value='filter.condition.distint'><spring:message code="filter.condition.distint" /></option>
							<option value='filter.condition.bigger'><spring:message code="filter.condition.bigger" /></option>
							<option value='filter.condition.smaller'><spring:message code="filter.condition.smaller" /></option>
							<option value='filter.condition.biggerEqual'><spring:message code="filter.condition.biggerEqual" /></option>
							<option value='filter.condition.smallerEqual'><spring:message code="filter.condition.smallerEqual" /></option>
							<option value='filter.condition.isNull'><spring:message code="filter.condition.isNull" /></option>
							<option value='filter.condition.isNotNull'><spring:message code="filter.condition.isNotNull" /></option>
						</select> 
					
						<select style="display:none" Id="select_date" class="etiquetaTextotxCampo5">
							<option value='filter.condition.isNull'><spring:message code="filter.condition.isNull" /></option>
							<option value='filter.condition.isNotNull'><spring:message code="filter.condition.isNotNull" /></option>
							<option value='filter.condition.bigger'><spring:message code="filter.condition.bigger" /></option>
							<option value='filter.condition.smaller'><spring:message code="filter.condition.smaller" /></option>
							<option value='filter.condition.biggerEqual'><spring:message code="filter.condition.biggerEqual" /></option>
							<option value='filter.condition.smallerEqual'><spring:message code="filter.condition.smallerEqual" /></option>
							<option value='filter.condition.equal'><spring:message code="filter.condition.equal" /></option>
							<option value='filter.condition.distint'><spring:message code="filter.condition.distint" /></option>
						</select>
					</td>
									
					<td width="30"></td>
					
					<td>
						<table>
							<tr>
								<td align="right" width="101">
									<table border="0" cellpadding="1" cellspacing="0" width="69"
										height="14" class="txentrar"
										onMouseOver="this.className='txentrar2'"	
										onMouseOut="this.className='txentrar'">
										<tr><td onClick="addCondition()" style="cursor: pointer"><spring:message code="filter.button.add" /></td></tr>
									</table>
								</td>
								<td width="151">
									<table border="0" cellpadding="1" cellspacing="0" width="69"
										height="14" class="txentrar"
										onMouseOver="this.className='txentrar2'"
										onMouseOut="this.className='txentrar'">
										<tr><td onClick="undo()" style="cursor: pointer"><spring:message code="filter.button.undo" /></td></tr>
									</table>
								</td>
							</tr>
						</table>
					</td>
				</tr>
				
				<tr>
					<td class="etiquetaTexto4" align="right" width="101">
						<spring:message code="filter.value"/>
					</td>
					<td>
						<input type="text" id="valueFilter" class="etiquetaTextotxCampo2" size="40" 
							onkeypress="if(event.keyCode == 13) addCondition();"/>
					</td>
					<td width="30"></td>
					<td>
						<table>
							<tr>
								<td align="right" width="101">
									<table border="0" cellpadding="1" cellspacing="0" width="69" height="14" class="txentrar"
										onMouseOver="this.className='txentrar2'"
										onMouseOut="this.className='txentrar'">
										<tr><td onClick="putY()" style="cursor: pointer"><spring:message code="filter.button.y" /></td></tr>
									</table>
								</td>								
								<td width="151">
									<table border="0" cellpadding="1" cellspacing="0" width="69"
										height="14" id="botonaceptar" class="txentrar"
										onMouseOver="this.className='txentrar2'"
										onMouseOut="this.className='txentrar'">
										<tr><td onClick="putO()" style="cursor: pointer"><spring:message code="filter.button.o" /></td></tr>
									</table>
								</td>
							</tr>
						</table>
					</td>
				</tr>				
			</table>
			<table>
				<tr><td height="20"></td></tr>
				<tr>
					<td>
						<!-- textarea con el filtro realizado -->
						<textarea rows="5" disabled id="humanTextFilter" cols="100" style="width:550px"><c:out value="${humanTextFilter}"/></textarea>		
						<!-- 
							<textarea rows="5" disabled  cols="100" style="width:550px"><c:out value="${textFilter}"/></textarea>
						--> 
						<input type="hidden" id="textFilter" value="${textFilter}"/>
					</td>
				</tr>
				<tr><td height="20"></td></tr>
				<tr>
					<td align="right">
						<table>
							<tr>								
								<td>
									<table border="0" cellpadding="1" cellspacing="0" width="69"
										height="14" id="botonaceptar" class="txentrar"
										onMouseOver="this.className='txentrar2'"
										onMouseOut="this.className='txentrar'">
										<tr><td onClick="filterSearch()" style="cursor: pointer"><spring:message code="button.accept"/></td></tr>
									</table>									
								</td>
								<td>
									<table border="0" cellpadding="1" cellspacing="0" width="69"
										height="14" id="botonaceptar" class="txentrar"
										onMouseOver="this.className='txentrar2'"
										onMouseOut="this.className='txentrar'">
										<tr><td onClick="window.close()" style="cursor: pointer"><spring:message code="button.cancel" /></td></tr>
									</table>
								</td>
							</tr>
						</table>
					</td>
				</tr>
			</table>				
		</td></tr>
	</table>
	
</div>
	
</body>
<script>
	orderList('columns');
	document.getElementById('columns').selectedIndex=0;
	changeDataType(document.getElementById('columns'));
	initializeVariables();
</script>
</html>