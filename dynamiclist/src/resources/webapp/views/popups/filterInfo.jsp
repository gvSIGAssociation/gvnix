<%@ page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>

<html>

<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<title><spring:message code="filterInfo.title" /></title>
<link rel="stylesheet" type="text/css" href="<c:url value="/css/estilos.css"/>" />
</head>

<body>
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
				<td class="tituloFormulario" align="left"><spring:message code="filterInfo.title" /><BR><BR>
				</td>
				<TD colspan="2"><BR><BR></TD>
			</TR>
			
			<TR>
				<td width="28" height="35"></td>
				<td width="192" height="35"><b><spring:message code="filterInfo.object" /></b></td>
				<TD width="319" height="35"><c:out value="${simpleNameClass}"/></TD>
			</TR>
			
			<TR>
				<td width="28" height="35"></td>
				<td width="192" height="35"><b><spring:message code="filterInfo.name" /></b></td>
				<TD width="319" height="35">
					<c:out value="${nameFilter}"/>			
				</TD>
			</TR>
			
			<TR>
				<td width="28" height="35"></td>
				<td width="192" height="35"><b><spring:message code="filterInfo.filter" /></b></td>
				<TD width="319" height="35">
					<c:out value="${humanTextFilter}"/>	
				</TD>
			</TR>
			
			<TR>
				<td width="28" height="110"></td>
				<TD align="right" colspan="2" height="110">
					<table border="0" cellpadding="1" cellspacing="0" width="69"
						height="14" id="botonaceptar" class="txentrar"
						onMouseOver="this.className='txentrar2'"
						onMouseOut="this.className='txentrar'">
						<tr><td onClick="window.close()" style="cursor: pointer"><spring:message code="button.accept" /></td></tr>
					</table>
				</TD>
			</TR>
			
		</TBODY>
	</TABLE>		
	</div>
</body>
</html>