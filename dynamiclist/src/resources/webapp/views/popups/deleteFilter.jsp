<div xmlns:c="http://java.sun.com/jsp/jstl/core" xmlns:form="http://www.springframework.org/tags/form" xmlns:jsp="http://java.sun.com/JSP/Page" xmlns:spring="http://www.springframework.org/tags" version="2.0">

<html:form action="operador.do">

<spring:message code="deleteFilter.title" />

<spring:message code="filter.saves" />

<select class="etiquetaTextotxCampo7" size="7" name="filtros" id="filtros" onchange="actualizarFiltro()">	
	<c:forEach var="filtroUsuario" items="${pObjFiltrosUsuario}">     	
     	<option selected id='<c:out value="${filtroUsuario.idfiltre}"/>' value='<c:out value="${filtroUsuario.wheesp}"/>' >			
			<c:out value="${filtroUsuario.idopefiltre}"/>
        </option>
	</c:forEach>
</select>

<table border="0" cellpadding="1" cellspacing="0" width="69"
	height="14" id="botongrabar" onclick="eliminarFiltroSeleccionado()"
	class="txentrar" onMouseOver="this.className='txentrar2'"
	onMouseOut="this.className='txentrar'">
	<tr><td style="cursor: pointer"><spring:message code="button.delete" /></td></tr>
</table>

<spring:message code="filter.current" />

<textarea class="etiquetaTextotxCampo7" disabled rows="2" name="filtroactual" id="filtroactual" cols="55"> </textarea>

<table border="0" cellpadding="1" cellspacing="0" width="69"
	height="14" id="botongrabar" onclick="window.opener.location.reload();window.close()"
	class="txentrar" onMouseOver="this.className='txentrar2'"
	onMouseOut="this.className='txentrar'">
	<tr><td style="cursor: pointer"><spring:message code="button.accept" /></td></tr></table>
	
<script>
	actualizarFiltro();
	ordenaLista("filtros");
</script>

<!-- 
<input type="hidden" id="pStrGUID" name="value(pStrGUID)" value='<logic:present name="pStrGUID"><bean:write name="pStrGUID"/></logic:present>' />
 -->

</html:form>

</div>