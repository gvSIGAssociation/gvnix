<div xmlns:c="http://java.sun.com/jsp/jstl/core" xmlns:form="http://www.springframework.org/tags/form" xmlns:jsp="http://java.sun.com/JSP/Page" xmlns:spring="http://www.springframework.org/tags" version="2.0">

<html:form action="operador.do">

<spring:message code="orderBy.title" />
				
<select class="etiquetaTextotxCampo7" size="15" name="columnas" id="columnas" style="width:520px">	
	<c:forEach var="column" items="${lObjColumnas}">
		<option value="<c:out value="${column.value}"/>" >
			<spring:message code="${column.key}" />
		</option>
	</c:forEach>
</select>

<fieldset><br>
	<legend class="etiquetaTexto4">
		<spring:message code="orderBy.order" />
	</legend> 
	<imput class="etiquetaTexto4" type="radio" name="ordenar" value="ASC" checked onclick="actualizaValor(this)">	
	<spring:message code="orderBy.asc" />
	<input class="etiquetaTexto4" type="radio" name="ordenar" value="DESC" onclick="actualizaValor(this)">
	<spring:message code="orderBy.desc" />
</fieldset>

<table border="0" cellpadding="1" cellspacing="0" width="69" height="14">
	<tbody><tr>
		<td onclick="anyadir()";
			id="botonaceptar"
			class="txentrar"
			onmouseover="this.className='txentrar2'"
			onmouseout="this.className='txentrar'""
			style="cursor: pointer">
			<spring:message code="button.add" />
		</td>
	</tr></tbody>
</table>

<table border="0" cellpadding="1" cellspacing="0" width="69"
	height="14" class="txentrar"
	onmouseover="this.className='txentrar2'"
	onmouseout="this.className='txentrar'">
	<tbody><tr><td onclick="borrar()" style="cursor: pointer"><spring:message code="button.delete" /></td></tr></tbody>
</table>

<textarea id="value(cadvisible)" rows="5" disabled cols="100">	
	<c:forEach var="objActualOrder" items="${pObjOrdenActualNombre}">
		<spring:message code="${objActualOrder.AStrCodigo}" />		
		<c:out value"${objActualOrder.AStrDescripcion}"/>
	</c:forEach>	
</textarea>

<input type="hidden" id="value(cadena)" value='<c:out name="${pStrOrdenActual}"/>' />

<table border="0" cellpadding="1" cellspacing="0" width="69"
	height="14" id="botonaceptar"
	class="txentrar"
	onMouseOver="this.className='txentrar2'"
	onMouseOut="this.className='txentrar'">
	<tr><td onclick="creaCadena()" style="cursor: pointer"><spring:message code="button.accept" /></td></tr>
</table>

<table border="0" cellpadding="1" cellspacing="0" width="69"
	height="14" class="txentrar"
	onMouseOver="this.className='txentrar2'"
	onMouseOut="this.className='txentrar'">
	<tr><td onclick="window.close()" style="cursor: pointer"><spring:message code="button.cancel" /></td></tr>
</table>

<script>
	ordenaLista('columnas');
</script>

<!-- <input type="hidden" id="pStrGUID" name="value(pStrGUID)" value='<logic:present name="pStrGUID"><bean:write name="pStrGUID"/></logic:present>' /> -->

</html:form>

</div>