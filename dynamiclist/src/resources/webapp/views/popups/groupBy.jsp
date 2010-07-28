<%@ page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>

<html>

<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<title><spring:message code="groupBy.title"/></title>
<link rel="stylesheet" type="text/css" href="<c:url value="/css/estilos.css"/>" />
</head>

<body>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<script type="text/javascript" src="<c:url value="/resources/js/dynamicList.js" />"> </script>
<script>
function groupBy(){
	var listColumns = document.getElementById("columns");
	var selected = listColumns.selectedIndex;
	if (selected != -1) {
		var columnValue = listColumns[selected].value;		
		window.opener.dl_groupBySearch(columnValue, '<c:out value="${urlMapping}"/>');
	  	window.close();
  	}	 	
  	else{  		
  		alert('<spring:message code="groupBy.notSelectColumn" />');
  	}
}
function notGroupBy(){
	window.opener.dl_groupBySearch('false', '<c:out value="${urlMapping}"/>');
  	window.close();
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
			<TR>
				<td class="tituloFormulario" align="left"><spring:message code="groupBy.title" /></td>
			</TR>
			
			<tr><td height="15">&nbsp;</td></tr>			
			
			<tr>
				<TD width="92">
					<select class="etiquetaTextotxCampo7" size="15" name="columns" id="columns" style="width:410px">			
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
			</tr>
			
			<tr><td height="15">&nbsp;</td></tr>
			
			<tr><td height="15">
				<table>
					<tr>
						<td align="right" height="11" width="157"></td>
						<TD height="11" width="92">
							<table border="0" cellpadding="1" cellspacing="0" width="69"
								height="14" id="botonaceptar0" onclick="notGroupBy()"
								class="txentrar" onmouseover="this.className='txentrar2'"
								onmouseout="this.className='txentrar'">
								<tbody>
								<tr><td style="cursor: pointer"><spring:message code="groupBy.negation"/></td></tr>
								</tbody>
							</table>
						</TD>
						<TD width="160" height="11">
							<table cellpadding="0" cellspacing="0" border="0" class="etiquetaTexto4">
								<tr>
									<td>
									<table border="0" cellpadding="1" cellspacing="0" width="69" height="14">
										<tr><td onclick="groupBy()";
											id="botonaceptar"
											class="txentrar"
											onMouseOver="this.className='txentrar2'"
											onMouseOut="this.className='txentrar'""
											style="cursor: pointer">
											<spring:message code="button.accept" /></td></tr>
									</table>
									</td>
									<td>&nbsp;</td>
									<td>
										<table border="0" cellpadding="1" cellspacing="0" width="69"
											height="14" class="txentrar"
											onclick="window.close()"
											onMouseOver="this.className='txentrar2'"
											onMouseOut="this.className='txentrar'">
											<tr><td style="cursor: pointer"><spring:message code="button.cancel" /></td></tr>
										</table>
									</td>								
								</tr>
							</table>
						</TD>
					</tr>
				</table>			
			</td></tr>
		</TBODY>
	</TABLE>
	</div>	
</body>
<script>
	orderList('columns');
</script>
</html>