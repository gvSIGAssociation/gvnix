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

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.jsp.tagext.TagSupport#doEndTag()
	 */
	public int doStartTag() throws JspException {		
		
		List<String> metaFieldsNames = (List<String>)pageContext.getRequest().getAttribute(TagConstants.META_FIELDS_NAMES);
		List<Type> metaFieldsTypes = (List<Type>)pageContext.getRequest().getAttribute(TagConstants.META_FIELDS_TYPES);
		
		pageContext.setAttribute(TagConstants.META_FIELDS_TYPES, metaFieldsTypes);
		pageContext.setAttribute(TagConstants.META_FIELDS_NAMES, metaFieldsNames);
		
		
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
			
			buffer.append("><img src=\"");
			buffer.append(pageContext.getAttribute(TagConstants.IMAGES_PATH));
			buffer.append("/flechaverde_arriba.gif\"");
			buffer.append(" onclick=\"SDFmuestraOrdenar('");
			//buffer.append(lStrNomcolbd);
			buffer.append(" ASC','");
			//buffer.append(lStrAction);
			buffer.append("','");
			//buffer.append(this.configuracion.getHandlerparameter());
			buffer.append("')\" ><B><FONT FACE=Verdana SIZE=-2>");
			
			buffer.append(Messages.getMessage(metaFieldName));
			
			buffer.append("</B></FONT><img src=\"");
			buffer.append(pageContext.getAttribute(TagConstants.IMAGES_PATH));
			buffer.append("/flechaverde_bajo.gif\" onclick=\"SDFmuestraOrdenar('");
			//buffer.append(lStrNomcolbd);
			buffer.append(" DESC','");
			//buffer.append(lStrAction);
			buffer.append("','");
			//buffer.append(this.configuracion.getHandlerparameter());
			buffer.append("')\" ></td>");		
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

	
}
