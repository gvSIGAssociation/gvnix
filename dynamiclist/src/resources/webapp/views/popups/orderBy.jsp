<%@ page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>

<html>

<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<title><spring:message code="orderBy.title" /></title>
<link rel="stylesheet" type="text/css" href="<c:url value="/css/estilos.css"/>" />
</head>

<body>

<script type="text/javascript" src="<c:url value="/resources/js/dynamicList.js" />"> </script>

<script type="text/javascript">

var deleteOrderByColumn = false;

function orderBySearch(){
	var orderSQL = document.getElementById("value(orderText)").value;
	if (document.getElementById("value(humanOrderText)").value == '') {
		var orderSQL = 'false';
	}
	window.opener.dl_orderBySearch(orderSQL, '<c:out value="${urlMapping}"/>', deleteOrderByColumn);
  	window.close();
}

function deleteOrder() {
	document.getElementById("value(orderText)").value='';
	document.getElementById("value(humanOrderText)").value='';
	deleteOrderByColumn = true;
}

function addOrder() {

	var list = document.getElementById("columns");
	var index = list.selectedIndex;

	var selectedOrderText = list[index].text;
	var selectedOrderValue = list[index].value;

	var orderText = document.getElementById("value(humanOrderText)").value;
	var orderHidden = document.getElementById("value(orderText)").value;

	var elements = document.getElementsByName("ordenar");
	var value = "";	 
	for(var i=0; i<elements.length; i++) {
		if (elements[i].checked) {
			value = elements[i].value;
			break; 
		}	  
	}
	
	if (!exist(orderText, selectedOrderText)) {
		if (orderHidden == "") {
			orderText = orderText + selectedOrderText + ' = ' + value;
			orderHidden = orderHidden + selectedOrderValue + ' ' + value;
			document.getElementById("value(humanOrderText)").value = orderText;
			document.getElementById("value(orderText)").value = orderHidden;			
		}
		else {
			orderText = orderText + ', ' + selectedOrderText + ' = ' + value;
			orderHidden = orderHidden + ',' + selectedOrderValue + ' ' + value;
			document.getElementById("value(humanOrderText)").value = orderText;
			document.getElementById("value(orderText)").value = orderHidden;	
		}
	}
	else {
		alert('<spring:message code="orderBy.errorAddOrder" />');
	}
}


function exist(cadordenacion, campo) {
	 
	 // si la cadena de ordenacion está vacia
	 if(cadordenacion == ''){
	 	return false;
	 }
	 
	 // Extraemos todos los items de la cadena de ordenacion
	 var items = cadordenacion.split(",");
	 
	 // Extraemos por cada item de la cadena el campo que estara en formato 'campo = ASC'
	 for(var i=0; i < items.length; i++){
	 	var campoaux = items[i].substring(0, items[i].indexOf("=")).trim();
	 	if(campoaux == campo){
	 		return true;
	 	} 	
	 } 
	 
	 return false;
	}



</script>	

	<div id="container" align="center">
	
	<div id="header" >
		<table width="100%" border="0" cellpadding="0" cellspacing="0" >
			<tr bgcolor="#f5f5f5"><td height="62" align="center"><img src="images/headerDynamiclist.gif" height="62" alt="Header Image DYNAMICLIST"/></td></tr>
		</table>
	</div>
	
	<TABLE border="0" cellpadding="0" cellspacing="0" height="173">
		<TBODY>
			<tr>
				<td align="left" class="tituloFormulario" colspan="2">
					<spring:message code="orderBy.title" />
				</td>
			</tr>
			<TR>
				<TD align="left" colspan="2">
					<select class="etiquetaTextotxCampo7" size="15" name="columns" id="columns" style="width:520px">
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
			</TR>
			<TR><TD height="15" colspan="2">&nbsp;</TD></TR>
			
			<TR><TD colspan="2">
				<TABLE border="0" cellpadding="0" cellspacing="0" class="etiquetaTexto4" width="500">
					<TBODY>
						<TR valign="middle">
							<TD width="371">
								<fieldset><br>
								<LEGEND class="etiquetaTexto4"><spring:message code="orderBy.order" /></LEGEND>		 
								<input class="etiquetaTexto4" type="radio" id="ordernar" name="ordenar" value="ASC" checked />	
								<spring:message code="orderBy.asc" />
								<input class="etiquetaTexto4" type="radio" name="ordenar" value="DESC" />
								<spring:message code="orderBy.desc" />
								</fieldset>
							</TD>
							<td width="40"></td>
							<td>
								<TABLE>
									<TBODY>
										<TR><TD width="160" height="11">
											<TABLE cellpadding="0" cellspacing="0" border="0" class="etiquetaTexto4">
												<TBODY>
													<tr><td height="20"></td></tr>
													<TR><TD>
														<table border="0" cellpadding="1" cellspacing="0" width="69" height="14">
															<tbody><tr>
															<td onclick="addOrder()";					
																class="txentrar"
																onmouseover="this.className='txentrar2'"
																onmouseout="this.className='txentrar'""
																style="cursor: pointer">
																<spring:message code="button.add" />
															</td>
															</tr></tbody>
														</table>
													</TD></TR>
													<tr><td height="15"></td></tr>
													<tr><TD>
														<table border="0" cellpadding="1" cellspacing="0" width="69"
															height="14" class="txentrar"
															onmouseover="this.className='txentrar2'"
															onmouseout="this.className='txentrar'">
															<tbody><tr><td onclick="deleteOrder()" style="cursor: pointer"><spring:message code="button.delete" /></td></tr></tbody>
														</table>
													</td></tr>
												</TBODY>
											</TABLE>
										</TD></TR>
									</TBODY>
								</TABLE>
							</td>
						</TR>
					</TBODY>		
				</TABLE>
			</TD>
			</TR>
			<TR><TD height="15" colspan="2"></TD></TR>
			<TR><TD colspan="2">
				<textarea id="value(humanOrderText)" rows="5" disabled cols="100" style="width:520px"><c:out value="${humanOrderByText}"/></textarea>		
				<input type="hidden" id="value(orderText)" value='<c:out value="${orderByText}"/>'/>				
			</TD></TR>
			<TR><TD height="15" colspan="2"></TD></TR>
			<TR><TD height="11" colspan="2">
				<table cellpadding="0" cellspacing="0" border="0" class="etiquetaTexto4" align="right" width="150">
					<tr><td>
						<table border="0" cellpadding="1" cellspacing="0" width="69"
							height="14" id="botonaceptar"
							class="txentrar"
							onMouseOver="this.className='txentrar2'"
							onMouseOut="this.className='txentrar'">
							<tr><td onclick="orderBySearch()" style="cursor: pointer"><spring:message code="button.accept" /></td></tr>
						</table>
					</td>
					<td>&nbsp;</td>
					<td>
						<table border="0" cellpadding="1" cellspacing="0" width="69"
							height="14" class="txentrar"
							onMouseOver="this.className='txentrar2'"
							onMouseOut="this.className='txentrar'">
							<tr><td onclick="window.close()" style="cursor: pointer"><spring:message code="button.cancel" /></td></tr>
						</table>
					</td></tr>
				</table>
			</TD></TR>			
		</TBODY>		
	</TABLE>	
	</div>	
</body>
<script>
	orderList('columns');
</script>

</html>