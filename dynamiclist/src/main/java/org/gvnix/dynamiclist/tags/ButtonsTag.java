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
	
	public static final String BUTTONS_CRUD = "BUTTONS_CRUD";
	public static final String BUTTONS_CRUD_ERROR_MESSAGE = "Error en buttonsTag. El atributo \"crudButtons\" debe tener un valor \"S|N|S|S\" indicando" 
					+ "así si el botón se dibujará (S) o no (N) siguiendo el orden create|read|update|delete";
	
	
	protected String imagesPath = null;
	protected String crudButtons = null;
	protected String noCrudButtons = null;
	
	protected String button_group = null;
	protected String button_order = null;
	protected String button_export = null;
	protected String button_columns = null;
	
	protected String button_createFilter = null;
	protected String button_deleteFilter = null;
	protected String button_infoFilter = null;
	protected String button_updateFilter = null;
	protected String button_deleteSaveFilter = null;	
	protected String button_reload = null;
	

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.jsp.tagext.TagSupport#doStartTag()
	 */
	public int doStartTag() throws JspException {
		
		String contextPath = ((HttpServletRequest)pageContext.getRequest()).getContextPath();
		
		if (StringUtils.isEmpty(imagesPath)){
			imagesPath = contextPath + "/images";
		}
						
		if (StringUtils.isEmpty(crudButtons) && pageContext.getRequest().getAttribute(BUTTONS_CRUD) != null){
			crudButtons = (String)pageContext.getRequest().getAttribute(BUTTONS_CRUD);
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
			throw new JspException(BUTTONS_CRUD_ERROR_MESSAGE);
		}
		
		StringBuffer buffer = new StringBuffer();		
		buffer.append("<table width=\"99%\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" class=\"colorBarraHerramientas\">\n");		
		buffer.append("<tr align='left'>\n");		
		if (create){
			//buffer.append("<td width='31'><a id='create' href=' " + contextPath + "/client/form.html' >");
			buffer.append("<td width='31'><a id='add' href='javascript:dl_add(\"");
			//buffer.append(contextPath + "/client/form.html");
			buffer.append("\",\"");
			//buffer.append(this.configuracion.getHandlerparameter());
			buffer.append( "\");'>");		
			buffer.append("<img src='");
			buffer.append(imagesPath);
			buffer.append("/icono03.gif' name='Image2' class='botonAlta' id='Image2' title=\"");
			buffer.append(Messages.getMessage("button.add"));
			buffer.append("\" ></a></td>\n");			
		}
		if (read){
			buffer.append("<td width='31'><a id='read' href='javascript:dl_read(\"");			
			//buffer.append(contextPath + "/client/form.html");
			buffer.append("\",\"");
			//buffer.append(this.getMensaje("tc.seleccione"));
			buffer.append("\",\"");
			//buffer.append(this.configuracion.getHandlerparameter());
			buffer.append("\");'><img src='");			
			buffer.append(imagesPath);
			buffer.append("/icono05.gif' name='Image5' class='botonVisualizacion' id='Image5' title=\"");
			buffer.append(Messages.getMessage("button.read"));
			buffer.append("\" ></a></td>\n");			
		}
		if (update){
			buffer.append("<td width='31'><a href='javascript:dl_write(\"");
			//buffer.append(this.configuracion.getIdinforme());
			buffer.append("\",\"");
			//buffer.append( this.getMensaje("tc.seleccione"));
			buffer.append("\",\"");
			//buffer.append(this.configuracion.getHandlerparameter());
			buffer.append("\");'><img src='");
			buffer.append(imagesPath);
			buffer.append("/icono04.gif' name='Image4' class='botonAlta' id='Image4' title=\"");
			buffer.append(Messages.getMessage("button.write"));
			buffer.append("\" ></a></td>\n");
		}
		if (delete){
			buffer.append("<td width='31'><a href='javascript:dl_delete(\"");
			//buffer.append(this.configuracion.getIdinforme());
			buffer.append("\",\"");
			//buffer.append(this.getMensaje("tc.seleccione").replaceAll("'", "\\\'"));
			buffer.append("\",\"");
			//buffer.append(this.getMensaje("tc.confirmarborrado").replaceAll("'", "\\\'"));
			buffer.append("\",\"");
			//buffer.append(this.configuracion.getHandlerparameter());
			buffer.append("\");'><img src='");
			buffer.append(imagesPath);
			buffer.append("/icono06.gif' name='Image6' class='botonBaja' id='Image6' title=\"");
			buffer.append(Messages.getMessage("button.delete"));
			buffer.append("\" ></a></td>\n");			
		}
		
		if (StringUtils.isEmpty(noCrudButtons) || noCrudButtons.equalsIgnoreCase("S")){
			
			if (StringUtils.isEmpty(button_group) || button_group.equalsIgnoreCase("S")){
				buffer.append("<td width='40' >&nbsp;</td>\n" + "<td width='31'><a href='javascript:dl_groupBy(\"");
				//buffer.append(this.configuracion.getIdinforme());
				buffer.append("\",\"");
				//buffer.append(this.configuracion.getTabla());
				buffer.append("\",\"");
				//buffer.append(this.configuracion.getHandlerparameter());
				buffer.append("\");'><img src='");
				buffer.append(imagesPath);
				buffer.append("/icono08.gif' name='Image8' class='botonAgrupar' id='Image8' title=\"");
				buffer.append(Messages.getMessage("button.groupby"));
				buffer.append("\" ></a></td>\n");				
			}
			if (StringUtils.isEmpty(button_order) || button_order.equalsIgnoreCase("S")){
				buffer.append("<td width='32'><a href='javascript:dl_order(\"");
				//buffer.append(this.configuracion.getIdinforme());
				buffer.append("\",\"");
				//buffer.append(this.configuracion.getTabla());
				buffer.append("\",\"");
				//buffer.append(this.configuracion.getHandlerparameter());
				buffer.append("\");'><img src='");
				buffer.append(imagesPath);
				buffer.append("/icono09.gif' name='Image9' class='botonOrdenar' id='Image9' title=\"");
				buffer.append(Messages.getMessage("button.order"));
				buffer.append("\" ></a></td>\n");
			}
			if (StringUtils.isEmpty(button_export) || button_export.equalsIgnoreCase("S")){
				buffer.append("<td width='26'><a href='javascript:dl_export(\"");
				buffer.append("exportanto a muerte ...");
				buffer.append("\");'><img src='");
				buffer.append(imagesPath);
				buffer.append("/icono19.gif' name='Image19' class='botonExportar' id='Image19'  title=\"");
				buffer.append(Messages.getMessage("button.export"));
				buffer.append("\" ></a></td>\n");
			}
			if (StringUtils.isEmpty(button_columns) || button_columns.equalsIgnoreCase("S")){
				buffer.append("<td width='26'><a href='javascript:dl_columns(\"");
				//buffer.append(this.configuracion.getIdinforme());
				buffer.append("\",\"");
				//buffer.append(this.configuracion.getTabla());
				buffer.append("\",\"");
				//buffer.append(this.configuracion.getHandlerparameter());
				buffer.append( "\");'><img src='");
				buffer.append(imagesPath);
				buffer.append("/icono22.gif' name='Image22' class='botonColumnas' id='Image22' title=\"");
				buffer.append(Messages.getMessage("button.columns"));
				buffer.append("\" ></a></td>\n");				
			}
			
			if (StringUtils.isEmpty(button_createFilter) || button_createFilter.equalsIgnoreCase("S")){
				buffer.append("<td width='31'><a href='javascript:dl_createFilter(\"");
				//buffer.append(this.configuracion.getIdinforme());
				buffer.append("\",\"");
				//buffer.append(this.configuracion.getTabla());
				buffer.append("\",\"");
				//buffer.append(this.configuracion.getHandlerparameter());
				buffer.append( "\");'><img src='");
				buffer.append(imagesPath);
				buffer.append("/icono12.gif' name='Image12' class='botonFiltroCrear' id='Image12' title=\"");
				buffer.append(Messages.getMessage("button.createfilter"));
				buffer.append("\" ></a></td>\n");				
			}
			if (StringUtils.isEmpty(button_deleteFilter) || button_deleteFilter.equalsIgnoreCase("S")){
				buffer.append("<td width='34'><a href='javascript:dl_deleteFilter(\"");
				//buffer.append(this.getMensaje("tc.quitarfiltro"));
				buffer.append("\");'><img src='");
				buffer.append(imagesPath);
				buffer.append("/icono13.gif' name='Image13' class='botonFiltroElim' id='Image13' title=\"");
				buffer.append(Messages.getMessage("button.deletefilter"));
				buffer.append("\" ></a></td>\n");
			}
			if (StringUtils.isEmpty(button_infoFilter) || button_infoFilter.equalsIgnoreCase("S")){
				buffer.append("<td width='33'><a href='javascript:dl_infoFilter(\"");
				//buffer.append(this.configuracion.getIdinforme());
				buffer.append("\",\"");
				//buffer.append(this.configuracion.getTabla());
				buffer.append("\",\"");
				//buffer.append(this.configuracion.getHandlerparameter());
				buffer.append("\");'><img src='");
				buffer.append(imagesPath);
				buffer.append("/icono14.gif' name='Image14' class='botonFiltroVer' id='Image14' title=\"");
				buffer.append(Messages.getMessage("button.infofilter"));
				buffer.append("\" ></a></td>\n");
			}
			if (StringUtils.isEmpty(button_updateFilter) || button_updateFilter.equalsIgnoreCase("S")){
				buffer.append("<td width='32'><a href='javascript:");				
				//si hay filtro
				buffer.append("dl_updateFilter(\"");
				//buffer.append(this.configuracion.getIdinforme());
				buffer.append("\",\"");
				//buffer.append(this.configuracion.getTabla());
				buffer.append("\",\"");
				//si no hay filtro				
				//buffer.append("alert('" );
				//buffer.append(Messages.getMessage("popup.guardarfiltro.txtintroduccion"));	
				
				buffer.append("\");'><img src='");
				buffer.append(imagesPath);
				buffer.append("/icono15.gif' name='Image15' class='botonFiltroGuardar' id='Image15' title=\"");
				buffer.append(Messages.getMessage("button.updatefilter"));
				buffer.append("\" ></a></td>\n");
								
				//button_deleteSaveFilter
				buffer.append("<td width='32'><a href='javascript:dl_deleteFilter(\"");
				//buffer.append(this.configuracion.getIdinforme());
				buffer.append("\",\"");
				//buffer.append(this.configuracion.getTabla());
				buffer.append("\",\"");
				//buffer.append(this.configuracion.getHandlerparameter());
				buffer.append("\");'><img src='");
				buffer.append(imagesPath);
				buffer.append("/icono18.gif' name='Image18' class='botonFiltroElimG' id='Image18' title=\"");
				buffer.append(Messages.getMessage("button.deletefilter"));
				buffer.append("\" ></a></td>\n");
								
				//button Save configuration	state			
				buffer.append("<td width='40' >&nbsp;</td>\n");
				buffer.append("<td width='26'><a href='javascript:df_saveState(\"");
				//buffer.append(this.configuracion.getIdinforme());
				buffer.append("\",\"");
				//buffer.append(this.configuracion.getHandlerparameter());
				buffer.append("\",\"");
				//buffer.append(this.getMensaje("tc.guardadoconexito"));
				buffer.append("\");'><img src='");
				buffer.append(imagesPath);
				buffer.append("/icono20.gif' name='Image20' class='botonConfigGuardar' id='Image20' title=\"");
				buffer.append(Messages.getMessage("button.savestate"));
				buffer.append("\"  ></a></td>\n");
				
				//button delete configuration state
				buffer.append("<td width='26'><a href='javascript:dl_deleteState(\"");
				//buffer.append(this.configuracion.getIdinforme());
				buffer.append("\",\"");
				//buffer.append(this.configuracion.getHandlerparameter());
				buffer.append("\",\"");
				buffer.append(Messages.getMessage("configState.confirmDeleteState"));
				buffer.append("\",\"");
				//buffer.append(this.getMensaje("tc.eliminadoconexito"));
				buffer.append("\");'><img src='");
				buffer.append(imagesPath);
				buffer.append("/icono21.gif' name='Image21' class='botonConfigElim' id='Image21' title=\"");
				buffer.append(Messages.getMessage("button.deletestate"));
				buffer.append("\"  ></a></td>\n");
				buffer.append("<td width='40' >&nbsp;</td>\n");				
			}
			if (StringUtils.isEmpty(button_reload) || button_reload.equalsIgnoreCase("S")){				
				buffer.append("<td width='29'><a href='javascript:location.reload();'><img src='");
				buffer.append(imagesPath);
				buffer.append("/icono16.gif' name='Image16' class='botonRefrescar' id='Image16' title=\"");
				buffer.append(Messages.getMessage("button.reload"));
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
}