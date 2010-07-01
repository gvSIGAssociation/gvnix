<div xmlns:c="http://java.sun.com/jsp/jstl/core" xmlns:form="http://www.springframework.org/tags/form" xmlns:jsp="http://java.sun.com/JSP/Page" xmlns:spring="http://www.springframework.org/tags" version="2.0">

<html:form action="operador.do">

<spring:message code="popup.agrupar" />

<%	String lStrColSel=(String)request.getAttribute(("pStrSeleccionada"));%>
<select class="etiquetaTextotxCampo7" size="15" name="columnas" id="columnas" style="width:410px">	
	
	<c:forEach var="pObjHashMap" items="${pObjColumnas}">			
		<bean:define id="lStrColDib" name="pObjHashMap" property="value"/>        
        <option
        <% 
         	if (lStrColSel != null) {
         		if (lStrColSel.equalsIgnoreCase(String.valueOf(lStrColDib))) { 
        %>
         	selected
        <%		} 
           	}
        %>
				value='<c:out value="${pObjHashMap.value}"/>'>			
			<spring:message code="${pObjHashMap.key}" />
		</option>
	</c:forEach>	
</select>
	
<table border="0" cellpadding="1" cellspacing="0" width="69"
	height="14" id="botonaceptar0" onclick="enviaNinguna()"
	class="txentrar" onmouseover="this.className='txentrar2'"
	onmouseout="this.className='txentrar'">
	<tbody><tr><td style="cursor: pointer"><spring:message code="groupBy.negation"/></td></tr></tbody>
</table>
		
<table border="0" cellpadding="1" cellspacing="0" width="69" height="14">
	<tr><td onclick="enviaCadena()";
		id="botonaceptar"
		class="txentrar"
		onMouseOver="this.className='txentrar2'"
		onMouseOut="this.className='txentrar'""
		style="cursor: pointer">
			<spring:message code="button.accept" /></td></tr>
</table>


<table border="0" cellpadding="1" cellspacing="0" width="69"
	height="14" class="txentrar"
	onclick="window.close()"
	onMouseOver="this.className='txentrar2'"
	onMouseOut="this.className='txentrar'">
	<tr><td style="cursor: pointer"><spring:message code="button.cancel" /></td></tr>
</table>
			
<script>
	ordenaLista('columnas');
</script>

<!-- 
<input type="hidden" id="pStrGUID" name="value(pStrGUID)" value='<logic:present name="pStrGUID"><bean:write name="pStrGUID"/></logic:present>' />
 -->
 
</html:form>

</div>


