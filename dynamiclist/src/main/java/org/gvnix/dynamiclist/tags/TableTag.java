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
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang.StringUtils;
import org.gvnix.dynamiclist.util.Messages;
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
	private String pk;
	private String dateFormat;
	
	private String url_base;
	private Collection<String> metaFieldsNames;
	private String groupBy;
	 
	/*
	 * (non-Javadoc)
	 * @see javax.servlet.jsp.tagext.TagSupport#doStartTag()
	 */
	@SuppressWarnings("unchecked")
	public int doStartTag() throws JspException {		
		//pageContext.getAttribute(TagConstants.IMAGES_PATH);		
		url_base = (String)pageContext.getAttribute(TagConstants.URL_BASE);
		metaFieldsNames = (Collection<String>)pageContext.getRequest().getAttribute(TagConstants.META_FIELDS_NAMES);		
		groupBy = (String) pageContext.getRequest().getAttribute(TagConstants.GROUPBY);
		
		return SKIP_BODY;
	}

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.jsp.tagext.TagSupport#doEndTag()
	 */
	public int doEndTag() throws JspException {
		HttpServletRequest request = (HttpServletRequest)pageContext.getRequest();
		String contextPath = request.getContextPath();		
		if (StringUtils.isEmpty(dateFormat)){
			dateFormat = TagConstants.DATE_FORMAT_DEFAULT;
		}
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(dateFormat);
		
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
		String previusValue = "";
		
		//iterate rows
		for (Object object : list) {
			StringBuffer row = new StringBuffer();
			String objectId;
			try {
				objectId = PropertyUtils.getProperty(object, "id").toString(); 
			} catch (Exception e) {
				e.printStackTrace();
				throw new JspException("Error, Id atribute not found", e);
			}			
			
			//se pinta la fila
			if (cont % 2 == 0) {
				colorRow = "txresultadoazul";
			} else {
				colorRow = "txresultadoblanco";
			}			
			row.append("<tr id='");
			row.append(objectId);
			row.append("' lang='");
			//buffer.append(lStrPkalternativa);
			row.append("' class='");
			row.append(colorRow);
			row.append("' onclick=\"setPkAct(this);");
			//buffer.append("ponerEst(this); ");
			row.append("\" ");
			
			row.append(" ondblclick='dl_read(\"");
			row.append(contextPath);	
			row.append("/");
			row.append(url_base);
			row.append("\",\"");
			row.append(Messages.getMessage("dynamiclist.alert.select", request));
			row.append("\");'>");
			
			
			boolean changeValueGroupBy = false;
			for (String name : metaFieldsNames) {
				Method method;
				String value;
				try{
					method = object.getClass().getMethod("get" + StringUtils.capitalize(name));
					//Format the attribute Date type 
					if (method.getGenericReturnType() == Date.class) {
						value = simpleDateFormat.format(method.invoke(object));
					} else {
						value = method.invoke(object).toString();
					}
				} catch (NoSuchMethodException e) {
					e.printStackTrace();
					throw new JspException("Error in metaFieldsNames", e);
				} catch (InvocationTargetException e) {
					e.printStackTrace();
					throw new JspException("Error in metaFieldsNames", e);
				} catch (IllegalAccessException e) {
					e.printStackTrace();
					throw new JspException("Error in metaFieldsNames", e);
				}	
				
				//groupBy is not empty
				if (StringUtils.isNotEmpty(groupBy) && name.equalsIgnoreCase(groupBy)){
					if (StringUtils.isNotEmpty(previusValue) && previusValue.equalsIgnoreCase(value)){
						row.append("<td></td>\n");
					} else {
						row.append("<td><B>");
						row.append(value);
						row.append("</B></td>\n");
						if (cont!=0){
							changeValueGroupBy = true;
						}
					}
					previusValue = value;
				} else {
					row.append("<td>");
					row.append(value);
					row.append("</td>\n");					
				}
			}			
			//close row
			row.append("</TR>");
			
			if (StringUtils.isNotEmpty(groupBy) && changeValueGroupBy){
				buffer.append("<TR bgcolor=\"#B6CCC6\"> <TD height=\"2\" colspan=\"");					
				buffer.append(metaFieldsNames.size());
				buffer.append("\">");
				buffer.append("</TD></TR>");					
			}
			buffer.append(row);			
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

	public String getPk() {
		return pk;
	}

	public void setPk(String pk) {
		this.pk = pk;
	}

	public String getDateFormat() {
		return dateFormat;
	}

	public void setDateFormat(String dateFormat) {
		this.dateFormat = dateFormat;
	}

}
