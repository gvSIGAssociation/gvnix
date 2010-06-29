<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html:html>
<head>
<%@ page 
language="java"
contentType="text/html; charset=ISO-8859-1"
pageEncoding="ISO-8859-1"
%>

<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<meta name="GENERATOR" content="IBM WebSphere Studio">
<title></title>
</head>

<body>

<%
	String lStrTitulo = (String) request.getAttribute("pStrVista");
	String lStrArchivo = lStrTitulo + ".xls";
	String contentDisposition = "attachment;filename=" + lStrArchivo;
	response.setHeader("Content-Disposition",contentDisposition);
%>
<%
	MetadatoDTO[] lObjColumnas = (MetadatoDTO[]) request.getAttribute("pObjColumnas");
%>
<%
	Auxiliar lObjAuxiliar = new Auxiliar();
%>
<% String lStrAgrupar = (String) request.getAttribute("pStrAgrupar"); %>
<% String lStrTabla = (String) request.getAttribute("pStrTabla"); %>
<% String lStrFiltro = (String) request.getAttribute("pStrFiltro"); %>
<% String lStrNomFiltro = (String) request.getAttribute("pStrNomFiltro"); %>

	
<html:form action="operador.do">

<%
	String lStrFijo;
	int lIntFijo=0;
	int lIntAux=0;
	for (int i=0; i<lObjColumnas.length; i++){
		lIntAux = Integer.parseInt(lObjColumnas[i].getPorcentaje()) + 20;
		lObjColumnas[i].setPorcentaje(String.valueOf(lIntAux));
		lStrFijo = lObjColumnas[i].getPorcentaje();
		lIntFijo = lIntFijo + Integer.parseInt(lStrFijo);
		lObjColumnas[i].setPorcentaje(String.valueOf(lIntAux) + "px");
	}
	if (lIntFijo < 730) {
		lIntFijo = 730;
	}
	
	
%>
<table width="<%= String.valueOf(lIntFijo + 3) %>" border="0">
	<tr>
		 <td width="3px">
		 </td>
		 <td width="<%= String.valueOf(lIntFijo) %>">
			<table width="<%= String.valueOf(lIntFijo) %>" border="0">
				<tr>
						<td colspan="2">
							<b>
							<font face=Verdana size=3 color=black>
								<u>
								<% if(lStrTitulo!=null){%>
									<%=lStrTitulo%>
								<% } else { %>
									<spring:message code="exportList.title"/>
								<% } %>
								</u>
							</font>
							</b>
						</td>
						<td colspan="2">
							<font face=Verdana size=1 color=black>
								<% if(lStrNomFiltro!=null){%>
									<%=lStrNomFiltro%>
								<% } else { %>
									<spring:message code="exportList.notEstablish"/>
								<% } %>
							</font>
						</td>
						<td colspan="10">
							<font face=Verdana size=1 color=black>
								<% if(lStrFiltro!=null){%>
									<%=lStrFiltro%>
								<% } else { %>
									<spring:message code="exportList.notFilter"/>
								<% } %>
							</font>
						</td>
				</tr>
					<tr>
						<td>
							
						</td>
					</tr>
			</table>
			
			<table width="<%= String.valueOf(lIntFijo) %>" border="1" cellpadding="0" cellspacing="0">
				<tbody>
					<tr bgcolor=white>
					<% int lIntContador = 0; %>					
					<c:forEach var="column" items="${pObjColumnas}">						
						<!-- <bean:define id="lStrNomColActual" name="pObjColumnas" property="nomcolbd" /> -->							
						<td width='<c:out value="${column.porcentaje}"/>' >
							<b>
								<font face=Verdana size=-2 color=black>
									<spring:message code="${column.nomcolproperties}" />
								</font>
							</b>
						</td>
						<% lIntContador++; %>
					</c:forEach>
					</tr>
					
					<% String lStrValAnt="inicio"; %>
					<% boolean lBooDel=false; %>
					<% int lIntConDatos = 0; %>	
										
					<c:forEach var="object" items="${pObjDatos}">	
					<% boolean lBooBordeinferior = false; 
					<% if (lStrAgrupar!=null) { %>
						<% 	String lStrNombreBean = "";
							String lStrPropertyBean = "";
							boolean lBooEsNulo = false;
						%>
						<c:if test="${not empty object.<%= lStrAgrupar%>}">						
							<% 	lStrNombreBean = "pObjDatos"; 
								lStrPropertyBean = lStrAgrupar.toLowerCase();%>
						</c:if>
						
						<c:if test="${empty object.<%= lStrAgrupar%>}">						
							<% 	lStrNombreBean = "pObjDatos"; 
								lStrPropertyBean = "codpue";
								lBooEsNulo = true; %>								
						</c:if>
						
						<% if (!(lBooEsNulo)) { %>							
							<c:set var="lStrValAct" value='${<%= lStrNombreBean +"."+ lStrPropertyBean %>}'/>			
							<% if (!(lStrValAnt.equals(String.valueOf(lStrValAct)))) { %>
								<% lStrValAnt = String.valueOf(lStrValAct); %>
								<% if (lIntConDatos != 0) { %>
									<% lBooDel = true; %>
								<% } %>
							<% } %>
						<% } else { %>
							<% if (!(lStrValAnt.equals("valornulo"))) { %>
								<% lStrValAnt = "valornulo"; %>
								<% if (lIntConDatos != 0) { %>
									<% lBooDel = true; %>
								<% } %>
							<% } %>
						<% } %>
						<% if ((lBooDel)&&(lIntConDatos != 0)) { 
								lBooBordeinferior = true;
						   } %>
					<% } %>
					
					<% String lStrColorCol = "FFFFFF"; %>
						
					<tr bgcolor="<%= lStrColorCol %>">
					<% for(int i = 0; i < lObjColumnas.length; i++)  { %>
					
					<% String lStrColAct = lObjColumnas[i].getNomcolbd(); %>
					
					<td <% if (lObjColumnas[i].getTipodato().equalsIgnoreCase("DATE")){%> style='mso-number-format:"General Date";' align="right" <%}%>>
						<font FACE=Verdana size=-2>					
						<% if ((lStrAgrupar != null)&&(lStrAgrupar.equalsIgnoreCase(lStrColAct))) { 
							    if ((lBooDel)||(lIntConDatos==0)) { %>
							    	<b>
							    	<% if (lObjColumnas[i].getTipodato().equalsIgnoreCase("DATE")){%>										
										<fmt:formatDate value="${object.<%= lStrColAct.toLowerCase() %>}" pattern="yyyy/MM/dd HH:mm" /> 
									<% } else if (lObjColumnas[i].getTipodato().equalsIgnoreCase("NUMBER")){%>											
											<c:set var="lStrNumeroActual" value="${object.<%= lStrColAct.toLowerCase() %>}"/>											
											<%
												String lStrNumber = String.valueOf(lStrNumeroActual);
												lStrNumber = lStrNumber.trim();
												if (lStrNumber.indexOf(".") != -1){
													lStrNumber = lStrNumber.replace('.',',');
												}
											%>
											<%=lStrNumber%>
								    <%} else { %>
										<c:out value="${object.<%= lStrColAct.toLowerCase() %>}" />&nbsp;
									<% } %>
							    	</b>
							   <% }
						 	} 
						   	else { %>
									<% if (lObjColumnas[i].getTipodato().equalsIgnoreCase("DATE")){%>											
											<fmt:formatDate value="${object.<%= lStrColAct.toLowerCase() %>}" pattern="yyyy/MM/dd HH:mm" />
									<% } else if (lObjColumnas[i].getTipodato().equalsIgnoreCase("NUMBER")){%>											
											<c:set var="lStrNumeroActual1" value="${object.<%= lStrColAct.toLowerCase() %>}"/>
											<%
												String lStrNumber1 = String.valueOf(lStrNumeroActual1);
												lStrNumber1 = lStrNumber1.trim();
												if (lStrNumber1.indexOf(".") != -1){
													lStrNumber1 = lStrNumber1.replace('.',',');
												}
											%>
											<%=lStrNumber1%>
									 <%} else { %>
										<c:out value="${object.<%= lStrColAct.toLowerCase() %>}" />&nbsp;
									<% } %>
							<% } %>
						</font>
					</td>
					<% } %>					
					</tr>
					<% if (lBooDel) lBooDel = false; %>
					<% lIntConDatos++; %>
					</c:forEach>	
				</tbody>
			</table>
		</td>
	</tr>
</table>

<!-- 
<input type="hidden" id="pStrGUID" name="value(pStrGUID)" value='<logic:present name="pStrGUID"><bean:write name="pStrGUID"/></logic:present>' /></html:form>
 -->
 
</body>
</html:html>

