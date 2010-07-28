<%@ page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>

<html>

<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<title><spring:message code="exportList.title"/></title>
</head>
<body>

	
<%
	String lStrTitulo = (String) request.getAttribute("simpleNameClass");
	String lStrArchivo = lStrTitulo + ".xls";
	String contentDisposition = "attachment;filename=" + lStrArchivo;
	response.setHeader("Content-Disposition", contentDisposition);
%>

<table border="0">
	<tr>
		 <td width="3px"></td>		 
		 <td>
			
			<table border="0">
				<tr>
					<td colspan="2">
						<B>
							<FONT FACE=Verdana SIZE=3 COLOR=black>
								<u><spring:message code='${url_base}' text="${simpleNameClass}"/></u>
							</FONT>
						</B>
					</td>
					<td colspan="2">
						<FONT FACE=Verdana SIZE=1 COLOR=black>
							<c:set var="labelFilterDefault" value='<spring:message code="exportList.defaultFilterName"/>' />
							<spring:message code="${labelFilter}" text="${labelFilterDefault}" />
						</FONT>
					</td>
					<td colspan="10">
						<FONT FACE=Verdana SIZE=1 COLOR=black>														
							<c:if test="${not empty humanTextFilter}">
								<c:out value="${humanTextFilter}"/>
							</c:if>
							<c:if test="${empty humanTextFilter}">
								<spring:message code="exportList.notFilter"/>
							</c:if>							
						</FONT>
					</td>
				</tr>
				<tr><td></td></tr>
			</table>
			
			<TABLE border="1" cellpadding="0" cellspacing="0">				
				<TBODY>
					<TR bgcolor=white>													
					<c:forEach items="${columns}" var="column">						
						<TD width="<c:out value="${columnsWidth[column] + 5}"/> px">
							<B>
								<FONT FACE=Verdana SIZE=-2 COLOR=black>
								    <!-- spring:message code= -->
								    <spring:message code="${url_base}.${column}" text="${column}"/>									
								</FONT>
							</B>
						</TD>						
					</c:forEach>
					</TR>
				</TBODY>
					
				<c:forEach items="${objectList}" var="dataObject">
					<tr bgcolor="FFFFFF">
						<c:forEach items="${columns}" var="column">
						<TD>
							<FONT FACE=Verdana SIZE=-2>
								<c:out value="${dataObject[column]}"/>
							</FONT>
						</TD>
						</c:forEach>
					</tr>	
				</c:forEach>
			</TABLE>			
		</td>
	</tr>
</table>

</body>
</html>