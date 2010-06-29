<div xmlns:c="http://java.sun.com/jsp/jstl/core" xmlns:form="http://www.springframework.org/tags/form" xmlns:jsp="http://java.sun.com/JSP/Page" xmlns:spring="http://www.springframework.org/tags" version="2.0">

<html:form action="operador.do">

<spring:message code="filter.title" />

<spring:message code="filter.properties" />


<html:select property="value(COLUMNAS)" style="width:550px"	styleId="COLUMNAS" styleClass="etiquetaTextotxCampo7" size="15"	onchange="cambiaTipoDato(this)">
	
	<c:forEach var="element" items="${pObjColumnas}">
		
		<option value='<c:out value="${element.nomcolbd}"/>|<c:out value="${element.tipodato}"/>' selected>
			<%-- No todas las vistas tienen traduccion,	para esas ponemos el nombre de la columna --%> 
			<c:if test="${not empty element.nomcolpropertieslargo}">		
				<spring:message code="${element.nomcolpropertieslargo}" />
			</c:if>
			<c:if test="${empty element.nomcolpropertieslargo}">
				<c:if test="${not empty element.nomcolproperties}">
					<spring:message code="${element.nomcolproperties}" /> 
				</c:if>
				<c:if test="${empty element.nomcolproperties}">			
					<c:out value="${element.nomcolbd}"/> 
				</c:if>
			</c:if>
		</option>
	
	</c:forEach>
	
</html:select>


<spring:message code="filter.condition" />

<html:select style="display:none" property="value(TEXTO)" styleId="TEXTO" styleClass="etiquetaTextotxCampo5">
	<option value='filter.condition.contain'><spring:message code="filter.condition.contain" /></option>
	<option value='filter.condition.begin'><spring:message code="filter.condition.begin" /></option>
	<option value='filter.condition.end'><spring:message code="filter.condition.end" /></option>
	<option value='filter.condition.yesNull'><spring:message code="filter.condition.yesNull" /></option>
	<option value='filter.condition.nonNull'><spring:message code="filter.condition.nonNull" /></option>
	<option value='filter.condition.equal'><spring:message code="filter.condition.equal" /></option>
	<option value='filter.condition.distint'><spring:message code="filter.condition.distint" /></option>	
</html:select> 
	
<html:select style="display:none" property="value(NUMERO)" styleId="NUMERO"	styleClass="etiquetaTextotxCampo5">
	<option value='filter.condition.equal'><spring:message code="filter.condition.equal" /></option>
	<option value='filter.condition.distint'><spring:message code="filter.condition.distint" /></option>
	<option value='filter.condition.bigger'><spring:message code="filter.condition.bigger" /></option>
	<option value='filter.condition.smaller'><spring:message code="filter.condition.smaller" /></option>
	<option value='filter.condition.biggerEqual'><spring:message code="filter.condition.biggerEqual" /></option>
	<option value='filter.condition.smallerEqual'><spring:message code="filter.condition.smallerEqual" /></option>
	<option value='filter.condition.yesNull'><spring:message code="filter.condition.yesNull" /></option>
	<option value='filter.condition.nonNull'><spring:message code="filter.condition.nonNull" /></option>
</html:select> 

<html:select style="display:none" property="value(FECHA)" styleId="FECHA" styleClass="etiquetaTextotxCampo5">
	<option value='filter.condition.yesNull'><spring:message code="filter.condition.yesNull" /></option>
	<option value='filter.condition.nonNull'><spring:message code="filter.condition.nonNull" /></option>
	<option value='filter.condition.bigger'><spring:message code="filter.condition.bigger" /></option>
	<option value='filter.condition.smaller'><spring:message code="filter.condition.smaller" /></option>
	<option value='filter.condition.biggerEqual'><spring:message code="filter.condition.biggerEqual" /></option>
	<option value='filter.condition.smallerEqual'><spring:message code="filter.condition.smallerEqual" /></option>
	<option value='filter.condition.equal'><spring:message code="filter.condition.equal" /></option>
	<option value='filter.condition.distint'><spring:message code="filter.condition.distint" /></option>
</html:select>

<table border="0" cellpadding="1" cellspacing="0" width="69"
	height="14" id="botonaceptar" class="txentrar"
	onMouseOver="this.className='txentrar2'"
	onMouseOut="this.className='txentrar'">
	<tr><td onClick="anyadirCondicion()" style="cursor: pointer"><spring:message code="filter.button.add" /></td></tr>
</table>

<table border="0" cellpadding="1" cellspacing="0" width="69"
	height="14" id="botonaceptar" class="txentrar"
	onMouseOver="this.className='txentrar2'"
	onMouseOut="this.className='txentrar'">
	<tr><td onClick="deshacer()" style="cursor: pointer"><spring:message code="filter.button.undo" /></td></tr>
</table>

<spring:message code="filtros.etiqueta.valor" />

<html:text styleId="VALOR" property="value(VALOR)" styleClass="etiquetaTextotxCampo2" value="" 
	size="40" onkeypress="if(event.keyCode == 13) anyadirCondicion();" />

<input type="text" style="display: none;">

<table border="0" cellpadding="1" cellspacing="0" width="69" height="14" class="txentrar"
	onMouseOver="this.className='txentrar2'"
	onMouseOut="this.className='txentrar'">
	<tr><td onClick="ponerY()" style="cursor: pointer"><spring:message code="filter.button.y" /></td></tr>
</table>

<table border="0" cellpadding="1" cellspacing="0" width="69"
	height="14" id="botonaceptar" class="txentrar"
	onMouseOver="this.className='txentrar2'"
	onMouseOut="this.className='txentrar'">
	<tr><td onClick="ponerO()" style="cursor: pointer"><spring:message code="filter.button.o" /></td></tr>
</table>

<html:textarea disabled="true" styleId="VISIBLE"
	property="value(VISIBLE)" styleClass="etiquetaTextotxCampo7"
	cols="100" rows="4" value="<%=lStrWhereHumano %>" /> 

<html:textarea style="display:none" styleId="OCULTA" property="value(OCULTA)"
	styleClass="etiquetaTextotxCampo7" cols="40" rows="5"
	value="<%=lStrWhereSql %>" />


<table border="0" cellpadding="1" cellspacing="0" width="69"
	height="14" id="botonaceptar" class="txentrar"
	onMouseOver="this.className='txentrar2'"
	onMouseOut="this.className='txentrar'">
	<tr><td onClick="enviar()" style="cursor: pointer"><spring:message code="button.accept" /></td></tr>
</table>

<table border="0" cellpadding="1" cellspacing="0" width="69"
	height="14" id="botonaceptar" class="txentrar"
	onMouseOver="this.className='txentrar2'"
	onMouseOut="this.className='txentrar'">
	<tr><td onClick="window.close()" style="cursor: pointer"><spring:message code="button.cancel" /></td></tr>
</table>


<script>
	ordenaLista('COLUMNAS');
	document.getElementById('COLUMNAS').selectedIndex=0;
	cambiaTipoDato(document.getElementById('COLUMNAS'));
</script>

<!-- 
<input type="hidden" id="pStrGUID" name="value(pStrGUID)" value='<logic:present name="pStrGUID"><bean:write name="pStrGUID"/></logic:present>' />
 -->

</html:form>

</div>