<div xmlns:c="http://java.sun.com/jsp/jstl/core" xmlns:form="http://www.springframework.org/tags/form" xmlns:jsp="http://java.sun.com/JSP/Page" xmlns:spring="http://www.springframework.org/tags" version="2.0">

<html:form action="operador.do">
	
<spring:message code="saveFilter.title" />

<spring:message code="saveFilter.none" />
	
	
<c:if test='${<%=lStrEsEtiqueta.equalsIgnoreCase("S")%>}' >
	<spring:message code="<%=lStrNombreFiltro%>" />
</c:if>
<c:if test='${<%=lStrEsEtiqueta.equalsIgnoreCase("N")%>}' >		
	<c:out value="${lStrNombreFiltro}"/>
</c:if>

<c:set var="lStrNombreEsEtiqueta" value="${pStrNombreEsEtiqueta}"/>
<c:if test='${<%=lStrNombreEsEtiqueta.equalsIgnoreCase("S")%>}' >		
	<input class="etiquetaTextotxCampo7" name="nombre" id="nombre" type="text" 
		value="<spring:message code="${pStrNombreFiltro}" />" size="55" onkeypress="if(event.keyCode == 13) guardar();">
</c:if>
<c:if test='${<%=lStrNombreEsEtiqueta.equalsIgnoreCase("N")%>}' >
	<input class="etiquetaTextotxCampo7" name="nombre" id="nombre" type="text" 
		value="<c:out value="${pStrNombreFiltro}"/>" size="55" onkeypress="if(event.keyCode == 13) guardar();">
</c:if>

<!--  esto es para que no haga submit cuando se pulse el intro -->
<input type="text" style="visibility: hidden;" >

<spring:message code="filter.current" />
			
<textarea class="etiquetaTextotxCampo7" disabled rows="4" name="filtro" id="filtrohumano" cols="55"> 
	<c:out value="${pStrWhereHumano}" />
</textarea>

<input type="hidden" name="sql" id="sql" value="<c:out value="${pStrWhereSql}"/>"  >

<table border="0" cellpadding="1" cellspacing="0" width="69"
	height="14" id="botongrabar" class="txentrar" onMouseOver="this.className='txentrar2'"
	onMouseOut="this.className='txentrar'">
	<tr><td onclick="guardar()" style="cursor: pointer"><spring:message code="button.save" /></td></tr>
</table>

<table border="0" cellpadding="1" cellspacing="0" width="69"
	height="14" id="botongrabar" class="txentrar" onMouseOver="this.className='txentrar2'"
	onMouseOut="this.className='txentrar'">
	<tr><td onclick="window.close()" style="cursor: pointer"><spring:message code="button.cancel" /></td></tr>
</table>

<!-- 
<input type="hidden" id="pStrGUID" name="value(pStrGUID)" value='<logic:present name="pStrGUID"><bean:write name="pStrGUID"/></logic:present>' />
 -->

</html:form>

</div>