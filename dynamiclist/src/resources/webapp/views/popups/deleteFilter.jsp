<%@ page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>

<html>

<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<title><spring:message code="deleteFilter.title" /></title>
<link rel="stylesheet" type="text/css" href="<c:url value="/css/estilos.css"/>" />
</head>

<body onunload="window.opener.location.reload()">
<script type="text/javascript" src="<c:url value="/resources/js/dynamicList.js" />"> </script>
<script type="text/javascript">

/**
 * 
 */
function deleteFilter() {	
	var selectFilters = document.getElementById("filters");	
	var index = selectFilters.selectedIndex;	
	var id = "";
	if (index != -1){	
		id = selectFilters[index].id;
		if (confirm('<spring:message code="deleteFilter.confirm"/>')){			
			window.location.href = '<c:out value="${urlMapping}"/>' + '?id=' + id;			
		}
	}	
	orderList("filters");	
}

/**
 * 
 */
function selectFilter(){
	var selectFilters = document.getElementById("filters");
	var index = selectFilters.selectedIndex;	
	var humanWhere = "";
	if (index != -1) {
		humanWhere = selectFilters[index].value;
	}
	document.getElementById("humanTextFilter").value = humanWhere;
}


</script>

<div id="container" align="center">
	
	<div id="header" >
		<table width="100%" border="0" cellpadding="0" cellspacing="0" >
			<tr bgcolor="#f5f5f5"><td height="62" align="center"><img src="images/headerDynamiclist.gif" height="62" alt="Header Image DYNAMICLIST"/></td></tr>
		</table>
	</div>
	
	<TABLE border="0" cellpadding="0" cellspacing="0" height="293"
		class="etiquetaTexto4">
		<TBODY>
			<TR>
				<td width="28"></td>
				<td class="tituloFormulario" align="left" width="170"><spring:message code="deleteFilter.title" /><BR>
				<BR>
				</td>
				<TD colspan="2"><BR>
				<BR>
				</TD>
			</TR>
			<TR>
				<td width="28" height="35"></td>
				<td height="35" width="170"><b><spring:message code="filter.saves" /></b></td>
			</TR>
			
			<TR>
				<td width="28" height="35"></td>
				<td>
					<select class="etiquetaTextotxCampo7" size="7" name="filters" id="filters" onchange="selectFilter()">
						<c:forEach items="${userFilters}" var="userFilter">			
						<option id='<c:out value="${userFilter.id}"/>' value='<c:out value="${userFilter.infoFilter}"/>' 
							<c:if test="${userFilter.id == selectedColumn}">
			    				selected
							</c:if> 
						>
							<spring:message code="${userFilter.labelFilter}" text="${userFilter.labelFilter}" />
						</option>
						</c:forEach>
					</select>
				</td>
				<td width="60%" style="padding-left: 20px">
					<table border="0" cellpadding="1" cellspacing="0" width="69"
						height="14" id="botongrabar" onclick="deleteFilter()"
						class="txentrar" onMouseOver="this.className='txentrar2'"
						onMouseOut="this.className='txentrar'">
						<tr><td style="cursor: pointer"><spring:message code="button.delete" /></td></tr>
					</table>
				</td>

			</TR>
			<TR>
				<td width="28" height="35"></td>
				<td height="35" width="170"><b><spring:message code="filter.current" /></b></td>
			</TR>
			<TR>
				<td width="28" height="35"></td>
				<td colspan="2">
					<textarea class="etiquetaTextotxCampo7" disabled rows="2" name="humanTextFilter" id="humanTextFilter" cols="55"> 
					</textarea>
				</td>
			</TR>
			<TR>
				<td width="28" height="110"></td>
				<TD align="right" colspan="2" height="110">
					<table border="0" cellpadding="1" cellspacing="0" width="69"
						height="14" id="botongrabar" onclick="window.close()"
						class="txentrar" onMouseOver="this.className='txentrar2'"
						onMouseOut="this.className='txentrar'">
						<tr><td style="cursor: pointer"><spring:message code="button.accept" /></td></tr>
					</table>
				</TD>
				<TD align="right" colspan="2" height="110" width="75"></TD>
			</TR>
		</TBODY>
	</TABLE>
</div>

</body>
<script>
	selectFilter();
	orderList("filters");
</script>
</html>