<div xmlns:c="http://java.sun.com/jsp/jstl/core" xmlns:form="http://www.springframework.org/tags/form" xmlns:jsp="http://java.sun.com/JSP/Page" xmlns:spring="http://www.springframework.org/tags" version="2.0">

<c:set var="lStrFiltroHumano" value="${pStrFiltroHumano}"/>
<c:set var="lStrNombreFiltro" value="${pStrNombreFiltro}"/>
<c:set var="lStrEsEtiqueta" value="${pStrEsEtiqueta}"/>

<html:form action="operador.do">
	
	<spring:message code="currentFilter.title" />
	<spring:message code="currentFilter.view" />
		
	<c:out value="${pObjConf.tabla}"/>
	
	<spring:message code="currentFilter.name" />				
	
	<c:if test='${<%=lStrEsEtiqueta.equalsIgnoreCase("S")%>}' >
		<spring:message code="<%=lStrNombreFiltro%>" />
	</c:if>
	<c:if test='${<%=lStrEsEtiqueta.equalsIgnoreCase("N")%>}' >		
		<c:out value="${lStrNombreFiltro}"/>
	</c:if>
		
	<spring:message code="currentFilter.filter" />	
	<c:out value="${pStrFiltroHumano}"/>			
	
	<table border="0" cellpadding="1" cellspacing="0" width="69"
		height="14" id="botonaceptar" onclick="window.close()"
		class="txentrar" onMouseOver="this.className='txentrar2'"
		onMouseOut="this.className='txentrar'">
		<tr><td><spring:message code="button.accept"/></td></tr>
	</table>
	
<!-- 
<input type="hidden" id="pStrGUID" name="value(pStrGUID)" value='<logic:present name="pStrGUID"><bean:write name="pStrGUID"/></logic:present>' />
 -->
 
</html:form>

</div>