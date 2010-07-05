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
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;

import org.gvnix.dynamiclist.util.TagConstants;


/**
 * gvNIX dynamiclist Table Tag.
 *
 * <p>This class provide the files of de table custom tag to show the object list.
 *
 * @author Ernesto Calás made for <a href="http://www.cit.gva.es">Conselleria d'Infraestructures i Transport</a>
 */
public class TableTag extends TagSupport {
	
	private static final long serialVersionUID = -217053490008543012L;
	
	private List<?> list;
	
	private String classObject;
	private String url_base;
	private List<String> metaFieldsNames;
	 
	/*
	 * (non-Javadoc)
	 * @see javax.servlet.jsp.tagext.TagSupport#doStartTag()
	 */
	public int doStartTag() throws JspException {
		
		//pageContext.getAttribute(TagConstants.IMAGES_PATH);		
		url_base = (String)pageContext.getAttribute(TagConstants.URL_BASE);
		classObject = (String)pageContext.getAttribute(TagConstants.CLASS_OBJECT);
		metaFieldsNames = (List<String>)pageContext.getAttribute(TagConstants.META_FIELDS_NAMES);
		
		pageContext.setAttribute(TagConstants.LIST, list);
		
		return SKIP_BODY;
	}

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.jsp.tagext.TagSupport#doEndTag()
	 */
	public int doEndTag() throws JspException {
		
		StringBuffer buffer = new StringBuffer();
		
		buffer.append("<SCRIPT>\n");
		/*
		 * // Funciones de cambio de pk, cambio de estado...
			if (this.pkalternativa != null) {
				cadena += "var pkalternativa = null;\n";
			}	
		 */
		buffer.append("</SCRIPT>");
		/*buffer.append("<div class='listado' >\n");
		buffer.append("<TABLE id='TABLADATOS' width='100%' border=\"0\" cellpadding=\"0\" cellspacing=\"0\" > \n");*/
		
		int cont = 0;
		String colorRow = "";
		//iterate rows
		for (Object object : list) {
			
			//se pinta la fila
			if (cont % 2 == 0) {
				colorRow = "txresultadoazul";
			} else {
				colorRow = "txresultadoblanco";
			}			
			buffer.append("<TR id='");
			//buffer.append(lStrPk);
			buffer.append("' lang='");
			//buffer.append(lStrPkalternativa);
			buffer.append("' class='");
			buffer.append(colorRow);
			buffer.append("' onclick=\"ponerPk(this);ponerEst(this); ");
			buffer.append("\" ");
			
			buffer.append(" ondblclick=\"");
			//buffer.append(b);			
			buffer.append("\"");
			buffer.append(" >\n");
			
		
			for (String name : metaFieldsNames) {
				Method method;
				try{
					method = object.getClass().getMethod("get" + name);				
				} catch (NoSuchMethodException e) {
					e.printStackTrace();
					throw new JspException("", e);
				}	
				
				buffer.append(" <td>");
				try {
					buffer.append(method.invoke(object).toString());
				} catch (InvocationTargetException e) {
					throw new JspException("", e);
				} catch (IllegalAccessException e) {
					throw new JspException("", e);
				}				
				buffer.append(" </td>\n");
			}
			
			//close row
			buffer.append("</TR>");
			
			cont++;
		}		
		try {
			pageContext.getOut().write(buffer.toString());			
		} catch (IOException e){
			e.printStackTrace();
			new JspException(e);
		}
		
		return EVAL_PAGE;
	}

	public List<?> getList() {
		return list;
	}

	public void setList(List<?> list) {
		this.list = list;
	}	

}
