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
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;

import org.apache.commons.lang.StringUtils;
import org.gvnix.dynamiclist.dto.DynamiclistConfig;
import org.gvnix.dynamiclist.jpa.bean.GlobalFilter;
import org.gvnix.dynamiclist.jpa.bean.UserFilter;
import org.gvnix.dynamiclist.util.Messages;
import org.gvnix.dynamiclist.util.TagConstants;

/**
 * gvNIX dynamiclist Actions Tag.
 *
 * <p>This class provide the actions select of custom tag to manage the extra operations 
 * 		and the pagination of dynamiclist.
 *
 * @author Ernesto Calás made for <a href="http://www.cit.gva.es">Conselleria d'Infraestructures i Transport</a>
 */
public class ActionsTag extends TagSupport{
	
	private static final long serialVersionUID = 6854342905526329389L;

	private List<String> actions = null;
	
	/*
	 * (non-Javadoc)
	 * @see javax.servlet.jsp.tagext.TagSupport#doStartTag()
	 */
	public int doStartTag() throws JspException {		
		
		if (actions == null) actions = new ArrayList<String>();	
		return SKIP_BODY;
	}


	/*
	 * (non-Javadoc)
	 * @see javax.servlet.jsp.tagext.TagSupport#doEndTag()
	 */
	public int doEndTag() throws JspException{
		HttpServletRequest request = (HttpServletRequest)pageContext.getRequest();
		String contextPath = request.getContextPath();
		DynamiclistConfig config = (DynamiclistConfig)request.getSession().getAttribute(TagConstants.DYNAMICLIST_CONFIG);
		String imagesPath = (String)pageContext.getAttribute(TagConstants.IMAGES_PATH);
		
		StringBuffer buffer = new StringBuffer();
		buffer.append("<table width=\"99%\" height=\"24\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" class=\"colorAcciones\">\n");
		buffer.append("<tr align=\"left\">\n");
		buffer.append("<td colspan=\"8\"><img src=\"\n");
		buffer.append(pageContext.getAttribute(TagConstants.IMAGES_PATH));
		buffer.append("/transparent.gif\" width=\"1\" height=\"1\"></td>\n");
		buffer.append("</tr>\n");
		buffer.append("<tr align=\"left\">\n");
		buffer.append("<td>\n");
		
		//TODO: ACTIONS		
		if (actions != null) {
			//buffer.append("<select onchange=\"javascript:ponerAccion(this);accionesFila(this);dl_executeAction(this);\" ");
			buffer.append("<select id=\"selectActions\" onchange=\"javascript:dl_executeAction(this);\" ");
			buffer.append("class=\"txpuerto3\">\n");
			buffer.append("<OPTION value='' selected>");
			buffer.append(Messages.getMessage("actions", request));
			buffer.append("&gt;</OPTION>\n");			
			for (String action : actions) {
				
					/*lObjAccionactual = aObjAux.splitCadena(lStrAccionactual, "@");
					String lStrCodigo = lObjAccionactual[0];
					String lStrMensaje = "";
					if (lStrCodigo.charAt(lStrCodigo.length() - 1) == '*') {
						lStrMensaje = lStrCodigo.substring(0, lStrCodigo.length() - 2);
					} else {
						lStrMensaje = lStrCodigo.replace('$', '_');
					}
					String lStrDescripcion = this.getMensaje("FUNCIONALIDADES." + lStrMensaje);
					if (this.selaccion == null) {
						cadena += "<OPTION id='" + lStrAccionactual + "' value='" + lStrCodigo + "'>"
								+ lStrDescripcion + "</OPTION>\n";
					} else {
						if (this.selaccion.equals(lStrCodigo)) {
							cadena += "<OPTION id='" + lStrAccionactual + "' value='" + lStrCodigo + "' selected>"
									+ lStrDescripcion + "</OPTION>\n";
						} else {
							cadena += "<OPTION id='" + lStrAccionactual + "' value='" + lStrCodigo + "'>"
									+ lStrDescripcion + "</OPTION>\n";
						}
					}*/
				 
			}
			buffer.append("</select>\n");
			buffer.append("<a id=\"Acciones\" href=\"javascript:dl_executeAction(document.getElementById('selectActions'));\">\n");
			buffer.append("<img src=\"");
			buffer.append(imagesPath);
			buffer.append("/icono17.gif\" class=\"botonIr\">\n");
			buffer.append("</a>\n");
			buffer.append("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;");
		}
					
		
		//FILTERS
		if ((config.getGlobalFilters() != null && config.getGlobalFilters().size() > 0) || 
				(config.getUserFilters() != null && config.getUserFilters().size() > 0)) {
			buffer.append("<select id=\"selectFilters\" class=\"txpuerto3\"");			
			buffer.append("onchange=\"javascript:dl_executeFilter('");
			buffer.append(contextPath);				
			buffer.append(TagConstants.URL_EXECUTE_FILTER);
			buffer.append("?urlBaseMapping=");
			buffer.append(pageContext.getAttribute(TagConstants.URL_BASE));
			buffer.append("','");
			buffer.append(Messages.getMessage("filter.alert.selectFilter", request));	
			buffer.append("');\" >\n");
			
			buffer.append("<OPTION value='' selected>");
			buffer.append(Messages.getMessage("filters", request));
			buffer.append("&gt;</OPTION>\n");	
		}
		
		//GLOBAL FILTER
		if (config.getGlobalFilters() != null) {			
			for (GlobalFilter globalFilter : config.getGlobalFilters()) {
				buffer.append("<OPTION id=\"");
				buffer.append(globalFilter.getId());
				buffer.append("\" ");
				if (config.getIdActualFilter() != null && ((StringUtils.isNotEmpty(config.getTypeActualFilter()) &&
						config.getTypeActualFilter().equals(TagConstants.TYPE_FILTER_GLOBAL)) || 
						StringUtils.isBlank(config.getTypeActualFilter()))) {
					if (config.getIdActualFilter().equals(globalFilter.getId())){
						buffer.append("selected ");
					}
				}
				buffer.append("value=\"");
				buffer.append(TagConstants.TYPE_FILTER_GLOBAL);
				buffer.append("\" >");
				buffer.append(Messages.getMessage(globalFilter.getLabelFilter(), request));
				buffer.append("</OPTION>\n");
			}
		}		
		
		//USER FILTER
		if (config.getUserFilters() != null) {						
			for (UserFilter userFilter : config.getUserFilters()) {
				buffer.append("<OPTION id=\"");
				buffer.append(userFilter.getId());
				buffer.append("\" ");					
				if (config.getIdActualFilter() != null && StringUtils.isNotEmpty(config.getTypeActualFilter()) &&
						config.getTypeActualFilter().equals(TagConstants.TYPE_FILTER_USER)) {
					if (config.getIdActualFilter().equals(userFilter.getId())){
						buffer.append("selected ");
					}
				}
				buffer.append("value=\"");
				buffer.append(TagConstants.TYPE_FILTER_USER);
				buffer.append("\" >");
				buffer.append(Messages.getMessage(userFilter.getLabelFilter(), request));
				buffer.append("</OPTION>\n");
			}			
		}
				
		if ((config.getGlobalFilters() != null && config.getGlobalFilters().size() > 0) || 
				(config.getUserFilters() != null && config.getUserFilters().size() > 0)) {
			buffer.append("</select>\n");
			buffer.append("<a href=\"javascript:dl_executeFilter('");			
			buffer.append(contextPath);				
			buffer.append(TagConstants.URL_EXECUTE_FILTER);
			buffer.append("?urlBaseMapping=");
			buffer.append(pageContext.getAttribute(TagConstants.URL_BASE));
			buffer.append("','");
			buffer.append(Messages.getMessage("filter.alert.selectFilter", request));
			buffer.append("');\">\n");
			buffer.append("<img src=\"");
			buffer.append(imagesPath);
			buffer.append("/icono17.gif\" class=\"botonIr\">\n</a>\n");			
			buffer.append("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;");
		}
		
		//TODO: PAGINATION
		
		
		buffer.append("</td>\n");
		buffer.append("<td width=\"9\" >&nbsp;</td>\n");
		buffer.append("</tr>\n </table>");
		
		try {
			pageContext.getOut().write(buffer.toString());			
		} catch (IOException e){
			e.printStackTrace();
			new JspException(e);
		}
		return EVAL_PAGE;
	}	
}
