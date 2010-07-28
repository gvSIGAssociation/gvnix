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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.Tag;

import org.apache.commons.lang.StringUtils;
import org.gvnix.dynamiclist.util.Messages;
import org.gvnix.dynamiclist.util.TagConstants;

/**
 * gvNIX dynamiclist buttons tag.
 *
 * <p>This class provide the buttons custom tag to manage the operations of dynamiclist.
 *
 *
 * @author Ernesto Calás made for <a href="http://www.cit.gva.es">Conselleria d'Infraestructures i Transport</a>
 */
public class ButtonsTag extends javax.servlet.jsp.tagext.TagSupport {
	
	private static final long serialVersionUID = -7857540693393637605L;
		
	private String imagesPath = null;
	private String crudButtons = null;
	private String noCrudButtons = null;
	
	private String button_group = null;
	private String button_order = null;
	private String button_export = null;
	private String button_columns = null;
	
	private String button_createFilter = null;
	private String button_deleteFilter = null;
	private String button_infoFilter = null;
	private String button_updateFilter = null;
	private String button_deleteSaveFilter = null;	
	private String button_reload = null;
	
	private String url_base = null;
	private String classObject = null;

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.jsp.tagext.TagSupport#doStartTag()
	 */
	public int doStartTag() throws JspException {
		HttpServletRequest request = (HttpServletRequest)pageContext.getRequest();
		String contextPath = request.getContextPath();
		if (StringUtils.isEmpty(imagesPath)){
			imagesPath = contextPath + "/images";
		} 
		
		//cooperate labels
		pageContext.setAttribute(TagConstants.IMAGES_PATH, imagesPath);
		pageContext.setAttribute(TagConstants.URL_BASE, url_base);
		pageContext.getRequest().setAttribute(TagConstants.URL_CONTEXT_BASE, contextPath + "/" + url_base);
		pageContext.getRequest().setAttribute(TagConstants.IMAGES_PATH, imagesPath);
		
		if (StringUtils.isEmpty(crudButtons) && pageContext.getRequest().getAttribute(TagConstants.BUTTONS_CRUD) != null){
			crudButtons = (String)pageContext.getRequest().getAttribute(TagConstants.BUTTONS_CRUD);
		} else {
			crudButtons = "S|S|S|S";
		}
		
		boolean create = true;
		boolean read = true;
		boolean update = true;
		boolean delete = true;		
		try {
			String[] lObjCadenabotones = crudButtons.split("\\|");
			if (lObjCadenabotones[0].equalsIgnoreCase("N")) {
				create = false;
			}
			if (lObjCadenabotones[1].equalsIgnoreCase("N")) {
				read = false;
			}
			if (lObjCadenabotones[2].equalsIgnoreCase("N")) {
				update = false;
			}
			if (lObjCadenabotones[3].equalsIgnoreCase("N")) {
				delete = false;
			}
		} catch (Exception e1) {
			throw new JspException(TagConstants.BUTTONS_CRUD_ERROR_MESSAGE);
		}
		
		StringBuffer buffer = new StringBuffer();		
		buffer.append("<table width=\"99%\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" class=\"colorBarraHerramientas\">\n");		
		buffer.append("<tr align='left'>\n");		
		if (create){
			buffer.append("<td width='31'><a href='javascript:dl_add(\"");			
			buffer.append(contextPath);
			buffer.append("/");
			buffer.append(url_base);
			buffer.append(TagConstants.URL_FORM);
			buffer.append("\");'>");		
			buffer.append("<img src='");
			buffer.append(imagesPath);
			buffer.append("/icono03.gif' name='Image2' class='botonAlta' id='Image2' title=\"");
			buffer.append(Messages.getMessage("button.add", request));
			buffer.append("\" ></a></td>\n");			
		}
		if (read){
			buffer.append("<td width='31'><a id='read' href='javascript:dl_read(\"");			
			buffer.append(contextPath);			
			buffer.append("/");
			buffer.append(url_base);
			buffer.append("\",\"");
			buffer.append(Messages.getMessage("dynamiclist.alert.select", request));
			buffer.append("\");'>");		
			buffer.append("<img src='");			
			buffer.append(imagesPath);
			buffer.append("/icono05.gif' name='Image5' class='botonVisualizacion' id='Image5' title=\"");
			buffer.append(Messages.getMessage("button.read", request));
			buffer.append("\" ></a></td>\n");			
		}
		if (update){
			buffer.append("<td width='31'><a href='javascript:dl_write(\"");
			buffer.append(contextPath);			
			buffer.append("/");
			buffer.append(url_base);
			buffer.append("\",\"");
			buffer.append(Messages.getMessage("dynamiclist.alert.select", request));
			buffer.append("\");'>");
			buffer.append("<img src='");	
			buffer.append(imagesPath);
			buffer.append("/icono04.gif' name='Image4' class='botonAlta' id='Image4' title=\"");
			buffer.append(Messages.getMessage("button.write", request));
			buffer.append("\" ></a></td>\n");
		}
		if (delete){
			buffer.append("<td width='31'><a href='javascript:dl_delete(\"");
			buffer.append(contextPath);
			buffer.append("/");
			buffer.append(url_base);
			buffer.append("\",\"");
			buffer.append(Messages.getMessage("dynamiclist.alert.select", request));
			buffer.append("\",\"");
			buffer.append(Messages.getMessage("dynamiclist.alert.deleteConfirm", request));
			buffer.append("\");'>");
			buffer.append("<img src='");	
			buffer.append(imagesPath);
			buffer.append("/icono06.gif' name='Image6' class='botonBaja' id='Image6' title=\"");
			buffer.append(Messages.getMessage("button.delete", request));
			buffer.append("\" ></a></td>\n");			
		}
		
		if (StringUtils.isEmpty(noCrudButtons) || noCrudButtons.equalsIgnoreCase("S")){
			
			if (StringUtils.isEmpty(button_group) || button_group.equalsIgnoreCase("S")){
				buffer.append("<td width='40' >&nbsp;</td>\n");
				buffer.append("<td width='31'><a href='javascript:dl_groupBy(\"");
				buffer.append(contextPath);				
				buffer.append(TagConstants.URL_GROUP);
				buffer.append("?urlBaseMapping=");				
				buffer.append(contextPath);
				buffer.append("/");
				buffer.append(url_base);
				buffer.append("\");'><img src='");
				buffer.append(imagesPath);
				buffer.append("/icono08.gif' name='Image8' class='botonAgrupar' id='Image8' title=\"");
				buffer.append(Messages.getMessage("button.groupby", request));
				buffer.append("\" ></a></td>\n");				
			}
			if (StringUtils.isEmpty(button_order) || button_order.equalsIgnoreCase("S")){
				buffer.append("<td width='32'><a href='javascript:dl_order(\"");
				buffer.append(contextPath);				
				buffer.append(TagConstants.URL_ORDER);
				buffer.append("?urlBaseMapping=");				
				buffer.append(contextPath);
				buffer.append("/");
				buffer.append(url_base);
				buffer.append("\");'><img src='");
				buffer.append(imagesPath);
				buffer.append("/icono09.gif' name='Image9' class='botonOrdenar' id='Image9' title=\"");
				buffer.append(Messages.getMessage("button.order", request));
				buffer.append("\" ></a></td>\n");
			}
			if (StringUtils.isEmpty(button_export) || button_export.equalsIgnoreCase("S")){
				buffer.append("<td width='26'><a href='javascript:dl_export(\"");
				buffer.append(contextPath);				
				buffer.append(TagConstants.URL_EXPORT);
				buffer.append("\",\"");
				buffer.append(url_base);
				buffer.append("\",\"");
				buffer.append(Messages.getMessage("dynamiclist.alert.exportLimit", request));
				buffer.append("\",\"");
				buffer.append(Messages.getMessage("dynamiclist.alert.exportRun", request));				
				buffer.append("\");'><img src='");
				buffer.append(imagesPath);
				buffer.append("/icono19.gif' name='Image19' class='botonExportar' id='Image19'  title=\"");
				buffer.append(Messages.getMessage("button.export", request));
				buffer.append("\" ></a></td>\n");
			}
			if (StringUtils.isEmpty(button_columns) || button_columns.equalsIgnoreCase("S")){
				buffer.append("<td width='26'><a href='javascript:dl_columns(\"");
				buffer.append(contextPath);				
				buffer.append(TagConstants.URL_COLUMNS);
				buffer.append("?imagesPath=");
				buffer.append(imagesPath);
				buffer.append("&urlBaseMapping=");
				buffer.append(url_base);				
				buffer.append( "\");'><img src='");
				buffer.append(imagesPath);
				buffer.append("/icono22.gif' name='Image22' class='botonColumnas' id='Image22' title=\"");
				buffer.append(Messages.getMessage("button.columns", request));
				buffer.append("\" ></a></td>\n");				
			}
			
			// ---- FILTERS -----
			if (StringUtils.isEmpty(button_createFilter) || button_createFilter.equalsIgnoreCase("S")){
				buffer.append("<td width='31'><a href='javascript:dl_createFilter(\"");
				buffer.append(contextPath);				
				buffer.append(TagConstants.URL_FILTER);
				buffer.append("?urlBaseMapping=");
				buffer.append(url_base);	
				buffer.append( "\");'><img src='");
				buffer.append(imagesPath);
				buffer.append("/icono12.gif' name='Image12' class='botonFiltroCrear' id='Image12' title=\"");
				buffer.append(Messages.getMessage("button.createfilter", request));
				buffer.append("\" ></a></td>\n");				
			}
			if (StringUtils.isEmpty(button_deleteFilter) || button_deleteFilter.equalsIgnoreCase("S")){
				buffer.append("<td width='34'><a href='javascript:dl_deleteActualFilter(\"");
				buffer.append(contextPath);				
				buffer.append(TagConstants.URL_DELETE_ACTUAL_FILTER);
				buffer.append("?urlBaseMapping=");
				buffer.append(url_base);
				buffer.append("\",\"");
				buffer.append(Messages.getMessage("deleteActualFilter.confirm", request));
				buffer.append("\");'><img src='");
				buffer.append(imagesPath);
				buffer.append("/icono13.gif' name='Image13' class='botonFiltroElim' id='Image13' title=\"");
				buffer.append(Messages.getMessage("button.deleteActualfilter", request));
				buffer.append("\" ></a></td>\n");
			}
			if (StringUtils.isEmpty(button_infoFilter) || button_infoFilter.equalsIgnoreCase("S")){
				buffer.append("<td width='33'><a href='javascript:dl_infoFilter(\"");
				buffer.append(contextPath);				
				buffer.append(TagConstants.URL_FILTER_INFO);
				buffer.append("\");'><img src='");
				buffer.append(imagesPath);
				buffer.append("/icono14.gif' name='Image14' class='botonFiltroVer' id='Image14' title=\"");
				buffer.append(Messages.getMessage("button.infofilter", request));
				buffer.append("\" ></a></td>\n");
			}
			if (StringUtils.isEmpty(button_updateFilter) || button_updateFilter.equalsIgnoreCase("S")){
				buffer.append("<td width='32'><a href='javascript:");				
				//si hay filtro
				buffer.append("dl_saveFilter(\"");
				buffer.append(contextPath);				
				buffer.append(TagConstants.URL_SAVE_FILTER);
				buffer.append("?urlBaseMapping=");
				buffer.append(url_base);
				//si no hay filtro				
				//buffer.append("alert('" );
				//buffer.append(Messages.getMessage("popup.guardarfiltro.txtintroduccion"));				
				buffer.append("\");'><img src='");
				buffer.append(imagesPath);
				buffer.append("/icono15.gif' name='Image15' class='botonFiltroGuardar' id='Image15' title=\"");
				buffer.append(Messages.getMessage("button.updatefilter", request));
				buffer.append("\" ></a></td>\n");
				
				//button_deleteSaveFilter
				buffer.append("<td width='32'><a href='javascript:dl_deleteFilter(\"");
				buffer.append(contextPath);				
				buffer.append(TagConstants.URL_DELETE_FILTER);
				buffer.append("\");'><img src='");
				buffer.append(imagesPath);
				buffer.append("/icono18.gif' name='Image18' class='botonFiltroElimG' id='Image18' title=\"");
				buffer.append(Messages.getMessage("button.deletefilter", request));
				buffer.append("\" ></a></td>\n");

				
				// --- CONFIG STATE ---
				
				//button Save configuration	state			
				buffer.append("<td width='40' >&nbsp;</td>\n");
				buffer.append("<td width='26'><a href='javascript:df_saveState(\"");
				buffer.append(contextPath);				
				buffer.append(TagConstants.URL_SAVE_STATE);
				buffer.append("?urlBaseMapping=");
				buffer.append(url_base);				
				buffer.append("\");'><img src='");
				buffer.append(imagesPath);
				buffer.append("/icono20.gif' name='Image20' class='botonConfigGuardar' id='Image20' title=\"");
				buffer.append(Messages.getMessage("button.savestate", request));
				buffer.append("\"  ></a></td>\n");
				
				//button delete configuration state
				buffer.append("<td width='26'><a href='javascript:dl_deleteState(\"");
				buffer.append(contextPath);				
				buffer.append(TagConstants.URL_DELETE_STATE);
				buffer.append("?urlBaseMapping=");
				buffer.append(url_base);
				buffer.append("\",\"");
				buffer.append(Messages.getMessage("configState.confirmDeleteState", request));				
				buffer.append("\");'><img src='");
				buffer.append(imagesPath);
				buffer.append("/icono21.gif' name='Image21' class='botonConfigElim' id='Image21' title=\"");
				buffer.append(Messages.getMessage("button.deletestate", request));
				buffer.append("\"  ></a></td>\n");
				buffer.append("<td width='40' >&nbsp;</td>\n");				
			}
			if (StringUtils.isEmpty(button_reload) || button_reload.equalsIgnoreCase("S")){				
				buffer.append("<td width='29'><a href='javascript:location.reload();'><img src='");
				buffer.append(imagesPath);
				buffer.append("/icono16.gif' name='Image16' class='botonRefrescar' id='Image16' title=\"");
				buffer.append(Messages.getMessage("button.reload", request));
				buffer.append("\" ></a></td>\n");		
				//buffer.append("<td width='20' >&nbsp;</td>\n" + "<td width='165'>");								
			}			
		}
		
		buffer.append("</tr>\n" + "</table>");
		
		try {
			pageContext.getOut().write(buffer.toString());
			
		} catch (IOException e){
			e.printStackTrace();
			new JspException(e);
		}
		return Tag.EVAL_BODY_INCLUDE;
	}


	public String getImagesPath() {
		return imagesPath;
	}


	public void setImagesPath(String imagesPath) {
		this.imagesPath = imagesPath;
	}


	public String getCrudButtons() {
		return crudButtons;
	}


	public void setCrudButtons(String crudButtons) {
		this.crudButtons = crudButtons;
	}


	public String getNoCrudButtons() {
		return noCrudButtons;
	}


	public void setNoCrudButtons(String noCrudButtons) {
		this.noCrudButtons = noCrudButtons;
	}


	public String getButton_group() {
		return button_group;
	}


	public void setButton_group(String buttonGroup) {
		button_group = buttonGroup;
	}


	public String getButton_order() {
		return button_order;
	}


	public void setButton_order(String buttonOrder) {
		button_order = buttonOrder;
	}


	public String getButton_export() {
		return button_export;
	}


	public void setButton_export(String buttonExport) {
		button_export = buttonExport;
	}


	public String getButton_columns() {
		return button_columns;
	}


	public void setButton_columns(String buttonColumns) {
		button_columns = buttonColumns;
	}


	public String getButton_createFilter() {
		return button_createFilter;
	}


	public void setButton_createFilter(String buttonCreateFilter) {
		button_createFilter = buttonCreateFilter;
	}


	public String getButton_deleteFilter() {
		return button_deleteFilter;
	}


	public void setButton_deleteFilter(String buttonDeleteFilter) {
		button_deleteFilter = buttonDeleteFilter;
	}


	public String getButton_infoFilter() {
		return button_infoFilter;
	}


	public void setButton_infoFilter(String buttonInfoFilter) {
		button_infoFilter = buttonInfoFilter;
	}


	public String getButton_updateFilter() {
		return button_updateFilter;
	}


	public void setButton_updateFilter(String buttonUpdateFilter) {
		button_updateFilter = buttonUpdateFilter;
	}


	public String getButton_deleteSaveFilter() {
		return button_deleteSaveFilter;
	}


	public void setButton_deleteSaveFilter(String buttonDeleteSaveFilter) {
		button_deleteSaveFilter = buttonDeleteSaveFilter;
	}


	public String getButton_reload() {
		return button_reload;
	}


	public void setButton_reload(String buttonReload) {
		button_reload = buttonReload;
	}


	public String getUrl_base() {
		return url_base;
	}


	public void setUrl_base(String urlBase) {
		url_base = urlBase;
	}


	public String getClassObject() {
		return classObject;
	}


	public void setClassObject(String classObject) {
		this.classObject = classObject;
	}
}
