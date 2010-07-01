<div xmlns:c="http://java.sun.com/jsp/jstl/core" xmlns:form="http://www.springframework.org/tags/form" xmlns:jsp="http://java.sun.com/JSP/Page" xmlns:spring="http://www.springframework.org/tags" version="2.0">
<html:form action="operador.do">

<spring:message code="selectProperties.title" />
<spring:message code="selectProperties.notDisplay" />
<spring:message code="selectProperties.display" />

<select multiple size="20" name="novisibles" id="lista1" style="width: 250px" class="etiquetaTextotxCampo7">	
	<c:forEach var="notDisplayElement" items="${pObjNoVisibles}">     	
     	<option class="etiquetaTexto4" value='<c:out value="${notDisplayElement.AStrDescripcion}"/>' >
			<c:if test="${not empty notDisplayElement.AStrCodigoLargo}"> 
				<spring:message code="${notDisplayElement.AStrCodigoLargo}" />
			</c:if>			
			<c:if test="${empty notDisplayElement.AStrCodigoLargo}"> 
				<spring:message code="${notDisplayElement.AStrCodigo}" />
			</c:if>
     	</option>
	</c:forEach>
</select>

<table border="0" cellpadding="1" cellspacing="0" width="14"
	height="14" class="txentrar" onclick="addIt('lista1','lista2')"
	onMouseOver="this.className='txentrar2'"
	onMouseOut="this.className='txentrar'">
	<tr><td><img alt="<spring:message code="selectProperties.add"/>" src="<%= PATH_IMG %>/flechablancaderecha.gif" name="Derecha"
		width="10" height="11" border="0" id="Derecha"></td></tr>
</table>

<table border="0" cellpadding="1" cellspacing="0" width="14"
	height="14" class="txentrar" onclick="delIt('lista1','lista2')"
	onMouseOver="this.className='txentrar2'"
	onMouseOut="this.className='txentrar'">
	<tr><td><img alt="<spring:message code="selectProperties.delete"/>"
		src="<%= PATH_IMG %>/flechablancaizquierda.gif"
		name="Izquierda" width="10" height="11" border="0"
		id="Izquierda"></td></tr>
</table>


<!-- <div id="roo_pizza_toppings">
<c:if test="${not empty toppings}">
<label for="_toppings_id">Toppings:</label>
<form:select cssStyle="width:250px" id="_toppings_id" path="toppings">
<form:options itemValue="id" items="${toppings}"/>
</form:select>
</c:if>
</div> -->

<select multiple class="etiquetaTextotxCampo7"
	size="20" name="visibles" id="lista2" style="width: 250px">
	<c:forEach var="displayElement" items="${pObjVisibles}">     	
     	<option class="etiquetaTexto4" value='<c:out value="${displayElement.AStrDescripcion}"/>'>
			<c:if test="${not empty displayElement.AStrCodigoLargo}"> 
				<spring:message code="${notDisplayElement.AStrCodigoLargo}" />
			</c:if>			
			<c:if test="${empty notDisplayElement.AStrCodigoLargo}"> 
				<spring:message code="${displayElement.AStrCodigo}" />
			</c:if>
     	</option>
	</c:forEach>
</select>

<table border="0" cellpadding="1" cellspacing="0" width="14"
	height="14" class="txentrar" onclick="subirElemento('lista2')"
	onMouseOver="this.className='txentrar2'"
	onMouseOut="this.className='txentrar'">
	<tr><td><img alt="<spring:message code="selectProperties.up"/>"
		src="<%= PATH_IMG %>/flechablancaarriba.gif" name="Arriba"
		width="10" height="11" border="0" id="Arriba"></td></tr>
</table>

<table border="0" cellpadding="1" cellspacing="0" width="14"
	height="14" class="txentrar" onclick="bajarElemento('lista2')"
	onMouseOver="this.className='txentrar2'"
	onMouseOut="this.className='txentrar'">
	<tr><td><img alt="<spring:message code="selectProperties.down"/>"
		src="<%= PATH_IMG %>/flechablancaabajo.gif" name="Abajo"
		width="10" height="11" border="0" id="Abajo"></td></tr>
</table>


<table border="0" cellpadding="1" cellspacing="0" width="69"
	height="14" id="botonaceptar"
	<% String lStrInformeActual = (String) request.getAttribute("pStrAction"); %>
	onclick="generarColumnas(selIt('lista2','Debe haber al menos una columna visible'),'<%=lStrInformeActual%>')"
	class="txentrar" onMouseOver="this.className='txentrar2'"
	onMouseOut="this.className='txentrar'">
	<tr><td style="cursor: pointer"><spring:message code="button.accept" /></td></tr></table>
									
<table border="0" cellpadding="1" cellspacing="0" width="69"
	height="14" class="txentrar" onclick="window.close()"
	onMouseOver="this.className='txentrar2'"
	onMouseOut="this.className='txentrar'">
	<tr><td style="cursor: pointer"><spring:message code="button.cancel"/></td></tr>
</table>

<html:hidden property="metodo" value="vertodas" styleId="prueba1" />
<html:hidden property="value(consulta)" value="*" styleId="oculto" />

<script>
	ordenaLista('lista1');
</script>

<!-- <input type="hidden" id="pStrGUID" name="value(pStrGUID)"
		value='<logic:present name="pStrGUID"><bean:write name="pStrGUID"/></logic:present>' /> -->

</html:form>
</div>