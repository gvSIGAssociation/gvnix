<%@ page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>

<html>

<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<title><spring:message code="saveFilter.title" /></title>
<link rel="stylesheet" type="text/css" href="<c:url value="/css/estilos.css"/>" />
</head>

<body>

<script type="text/javascript">

function saveFilter(){
	if(document.getElementById('textFilter').value == ''){
		alert('<spring:message code="saveFilter.alert.notFilter"/>');
		return;
	}
	if(document.getElementById('nameFilter').value == ''){
		document.getElementById('nameFilter').focus();
		alert('<spring:message code="saveFilter.alert.notName"/>');		
		return;
	}
	
	window.opener.location.href = '<c:out value="${urlMapping}"/>' + '&nameFilter=' + document.getElementById('nameFilter').value;
	window.close();	
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
				<td class="tituloFormulario" align="left"><spring:message code="saveFilter.title" /><BR><BR>
				</td>
				<TD colspan="2" width="75"><BR><BR></TD>
			</TR>
			<TR>
				<td width="28" height="35"></td>
				<td height="35">
				<b>
					<c:if test="${labelFilter == null}">
    					<spring:message code="saveFilter.none" />	
					</c:if> 
					<c:if test="${labelFilter != null}">
						<spring:message code="saveFilter.labelFilter" />
					</c:if>
				</b>
				</td>
			</TR>
			<TR>
				<td width="28" height="35"></td>
				<td colspan="2" width="374">
					<input class="etiquetaTextotxCampo7" name="nameFilter" id="nameFilter" type="text" size="55" 
						value='<spring:message code="${labelFilter}" text="${labelFilter}"/>' onkeypress="if(event.keyCode == 13) save();">		
					<!--  esto es para que no haga submit cuando se pulse el intro -->
					<input type="text" style="visibility: hidden;" >
				</td>
			</TR>
			<TR>
				<td width="28" height="35"></td>
				<td height="35"><b><spring:message code="filter.current" /></b></td>
			</TR>
			
			<TR>
				<td width="28" height="35"></td>
				<td colspan="2" width="374">
					<textarea class="etiquetaTextotxCampo7" disabled rows="4" name="humanTextFilter" id="humanTextFilter" cols="55"><c:out value="${humanTextFilter}"/></textarea>
					<input type="hidden" name="textFilter" id="textFilter" value='<c:out value="${textFilter}"/>' />	
				</td>
			</TR>
			
			<TR>
				<td width="28" height="110"></td>
				<TD align="right" colspan="2" height="110" width="374">
					<table border="0" cellpadding="1" cellspacing="0" width="69"
						height="14" class="txentrar" onMouseOver="this.className='txentrar2'"
						onMouseOut="this.className='txentrar'">
						<tr><td onclick="saveFilter()" style="cursor: pointer"><spring:message code="button.save" /></td></tr>
					</table>
				</TD>
				<TD align="right" colspan="2" height="110" width="75">
					<table border="0" cellpadding="1" cellspacing="0" width="69"
						height="14" class="txentrar" onMouseOver="this.className='txentrar2'"
						onMouseOut="this.className='txentrar'">
						<tr><td onclick="window.close()" style="cursor: pointer"><spring:message code="button.cancel" /></td></tr>
					</table>
				</TD>
			</TR>
		</TBODY>
	</TABLE>
</div>
</body>
</html>