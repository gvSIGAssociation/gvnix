/*
 * gvNIX. Spring Roo based RAD tool for Conselleria d'Infraestructures     
 * i Transport - Generalitat Valenciana
 * Copyright (C) 2010 CIT - Generalitat Valenciana
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.gvnix.dynamiclist.tags;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;

import org.gvnix.dynamiclist.util.Messages;
import org.gvnix.dynamiclist.util.TagConstants;

/**
 * gvNIX dynamiclist Header Table tag.
 *
 * <p>This class provide the HeaderTable of custom tag to show the columns of dynamiclist.
 *
 * @author Ernesto Calás made for <a href="http://www.cit.gva.es">Conselleria d'Infraestructures i Transport</a>
 */
public class HeaderTableTag extends javax.servlet.jsp.tagext.TagSupport {
	
	private static final long serialVersionUID = 6091229755397505524L;

	private Integer actualPage = 0;
	
	/*
	 * (non-Javadoc)
	 * @see javax.servlet.jsp.tagext.TagSupport#doEndTag()
	 */
	public int doStartTag() throws JspException {		
		
		actualPage = pageContext.getRequest().getAttribute(TagConstants.PAGE_NAME) != null ? 
				(Integer)pageContext.getRequest().getAttribute(TagConstants.PAGE_NAME) : 0;		
		
		HttpServletRequest request = (HttpServletRequest)pageContext.getRequest();
				
		List<String> metaFieldsNames = (List<String>)request.getAttribute(TagConstants.META_FIELDS_NAMES);
		List<Type> metaFieldsTypes = (List<Type>)request.getAttribute(TagConstants.META_FIELDS_TYPES);
		
		//pageContext.setAttribute(TagConstants.META_FIELDS_TYPES, metaFieldsTypes);
		//pageContext.setAttribute(TagConstants.META_FIELDS_NAMES, metaFieldsNames);
		
		String contextPath = request.getContextPath();
		String imagesPath = (String)pageContext.getAttribute(TagConstants.IMAGES_PATH);
		String url_base = contextPath + "/" + pageContext.getAttribute(TagConstants.URL_BASE);
		String classObject = pageContext.getAttribute(TagConstants.URL_BASE) + ".";
		
		StringBuffer buffer = new StringBuffer();
		buffer.append("<table width=\"99%\" height=\"24\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" class=\"colorAcciones\">\n");
		buffer.append("<tr align=\"left\">\n");
		buffer.append("<td colspan=\"8\"><img src=\"\n");
		buffer.append(pageContext.getAttribute(TagConstants.IMAGES_PATH));
		buffer.append("/transparent.gif\" width=\"1\" height=\"1\"></td>\n");
		buffer.append("</tr>\n");
		buffer.append("<tr align=\"left\">\n");
		
		for (Iterator<String> iterator = metaFieldsNames.iterator(); iterator.hasNext();) {
			String metaFieldName = (String) iterator.next();
			
			
			buffer.append("<td ");
			/*buffer.append("<TD width=\"" + lStrPorcentaje + "\"");
			" title=\""+ lStrTituloColumnaAbreviada +"\" "			
			 */
			buffer.append(">\n");
			
			buffer.append("<a href=\"");
			buffer.append(url_base);
			buffer.append(TagConstants.URL_SEARCH);
			buffer.append("?");
			buffer.append("page=");
			buffer.append(actualPage);
			buffer.append("&");
			buffer.append("orderByColumn=");
			buffer.append(metaFieldName);
			buffer.append(" ASC");
			buffer.append("\">");
			buffer.append("<img src=\"\n");
			buffer.append(imagesPath);
			buffer.append("/flechaverde_arriba.gif\" class=\"paginacionFlecha\"></a>\n");
			
			/*buffer.append("<img src=\"");
			buffer.append(pageContext.getAttribute(TagConstants.IMAGES_PATH));
			buffer.append("/flechaverde_arriba.gif\"");
			buffer.append(" onclick=\"SDFmuestraOrdenar('");
			//buffer.append(lStrNomcolbd);
			buffer.append(" ASC','");
			//buffer.append(lStrAction);
			buffer.append("','");
			//buffer.append(this.configuracion.getHandlerparameter());
			buffer.append("')\" >");*/
			
			buffer.append("<B><FONT FACE=Verdana SIZE=-2>");
			buffer.append(Messages.getMessage(classObject + metaFieldName, request));
			buffer.append("</B></FONT>");
			
			buffer.append("<a href=\"");
			buffer.append(url_base);
			buffer.append(TagConstants.URL_SEARCH);
			buffer.append("?");
			buffer.append("page=");
			buffer.append(pageContext.getRequest().getAttribute(TagConstants.PAGE_NAME));
			buffer.append("&");
			buffer.append("orderByColumn=");
			buffer.append(metaFieldName);
			buffer.append(" DESC");
			buffer.append("\">");
			buffer.append("<img src=\"\n");
			buffer.append(imagesPath);
			buffer.append("/flechaverde_bajo.gif\" class=\"paginacionFlecha\"></a>\n");			
			buffer.append("</td>");
		
		}
		
		buffer.append("<td width=\"9\" >&nbsp;</td>\n");
		buffer.append("</tr>\n ");
		
		//buffer.append("</table>");
		buffer.append("<div class='listado' >\n");		
		//buffer.append("<TABLE id='TABLADATOS' width='99%' border=\"0\" cellpadding=\"0\" cellspacing=\"0\">\n");		
		
		try {
			pageContext.getOut().write(buffer.toString());			
		} catch (IOException e){
			e.printStackTrace();
			new JspException(e);
		}
		
		return EVAL_BODY_INCLUDE;
	}

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.jsp.tagext.TagSupport#doStartTag()
	 */
	public int doEndTag() throws JspException {
		StringBuffer buffer = new StringBuffer("</TABLE>\n ");
		//buffer.append("</div>\n");
		try {
			pageContext.getOut().write(buffer.toString());			
		} catch (IOException e){
			e.printStackTrace();
			new JspException(e);
		}
		return EVAL_PAGE;
	}

	public Integer getActualPage() {
		return actualPage;
	}

	public void setActualPage(Integer actualPage) {
		this.actualPage = actualPage;
	}

	
}
