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
import javax.servlet.jsp.tagext.TagSupport;

import org.gvnix.dynamiclist.util.Messages;
import org.gvnix.dynamiclist.util.TagConstants;

/**
 * gvNIX dynamiclist Footer Table Tag.
 *
 * <p>This class provide pagination of dynamiclist custom tag.
 *
 * @author Ernesto Calás made for <a href="http://www.cit.gva.es">Conselleria d'Infraestructures i Transport</a>
 */
public class FooterTableTag extends TagSupport{
	
	private static final long serialVersionUID = -8127584873550076352L;

	private int actualPage = 1;
	private int size = TagConstants.SIZE_PAGE_DEFAULT;
	private int maxPages = 1;
	private int countList = 0;	
	private String imagesPath;
	private String url_base;
	
	/*
	 * (non-Javadoc)
	 * @see javax.servlet.jsp.tagext.TagSupport#doStartTag()
	 */
	public int doStartTag() throws JspException {		
		HttpServletRequest request = (HttpServletRequest)pageContext.getRequest(); 
		String contextPath = request.getContextPath();
		
		if (pageContext.getRequest().getAttribute(TagConstants.PAGE_NAME) != null){
			setActualPage((Integer)pageContext.getRequest().getAttribute(TagConstants.PAGE_NAME));
		}		
		if (pageContext.getRequest().getAttribute(TagConstants.SIZE_NAME) != null){
			setSize((Integer)pageContext.getRequest().getAttribute(TagConstants.SIZE_NAME));
		}
		if (pageContext.getRequest().getAttribute(TagConstants.MAX_PAGES_NAME) != null){
			setMaxPages((Integer)pageContext.getRequest().getAttribute(TagConstants.MAX_PAGES_NAME));
		}		
		if (pageContext.getRequest().getAttribute(TagConstants.COUNTLIST_NAME) != null){
			setCountList((Integer)pageContext.getRequest().getAttribute(TagConstants.COUNTLIST_NAME));
		}		
		
		imagesPath = (String)pageContext.getAttribute(TagConstants.IMAGES_PATH);
		url_base = contextPath + "/" + pageContext.getAttribute(TagConstants.URL_BASE);
		
		return SKIP_BODY;
	}


	/*
	 * (non-Javadoc)
	 * @see javax.servlet.jsp.tagext.TagSupport#doEndTag()
	 */
	public int doEndTag() throws JspException {	
		HttpServletRequest request = (HttpServletRequest)pageContext.getRequest();
		StringBuffer buffer = new StringBuffer();
		buffer.append("<table width=\"99%\" height=\"24\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" class=\"colorAcciones\">\n");
		buffer.append("<tr align=\"left\">\n");
		buffer.append("<td colspan=\"8\"><img src=\"\n");
		buffer.append(pageContext.getAttribute(TagConstants.IMAGES_PATH));
		buffer.append("/transparent.gif\" width=\"1\" height=\"1\"></td>\n");
		buffer.append("</tr>\n");
		buffer.append("<tr align=\"left\">\n");
		buffer.append("<td class=\"txpagina\">&nbsp;\n");
		buffer.append(getCountList());
		buffer.append("&nbsp;\n");
		buffer.append(Messages.getMessage("dynamiclist.paginate.total", request));		
		buffer.append("<input type=\"hidden\" id=\"paginate.total\" value=\"");
		buffer.append(getCountList());
		buffer.append("\" />");		
		buffer.append("</td>\n");
		buffer.append("<td width=\"47\" class=\"txpagina\">\n");
		buffer.append(Messages.getMessage("dynamiclist.paginate.page", request));
		buffer.append("&nbsp;");
		buffer.append(getActualPage());
		buffer.append("</td>\n");
				
		
		//iconos de acciones de paginación
		//inicio
		buffer.append("<td width=\"17\" >\n");
		if (actualPage > 1) {
			buffer.append("<a href=\"");
			buffer.append(url_base);
			buffer.append(TagConstants.URL_SEARCH);	
			buffer.append("?page=");
			buffer.append(1);
			buffer.append("\">");
			buffer.append("<img src=\"\n");
			buffer.append(imagesPath);
			buffer.append("/flechadobleizda.gif\" class=\"paginacionFlechadoble\"></a>\n");
		}		
		buffer.append("</td>\n"); 
						
		//retroceso
		buffer.append("<td width=\"10\" >\n");
		if (actualPage > 1) {
			buffer.append("<a href=\"");
			buffer.append(url_base);
			buffer.append(TagConstants.URL_SEARCH);	
			buffer.append("?page=");			
			buffer.append(actualPage - 1);
			buffer.append("\">");
			buffer.append("<img src=\"\n");
			buffer.append(imagesPath);
			buffer.append("/flechaizda.gif\" class=\"paginacionFlecha\"></a>\n");
		}
		buffer.append("</td>\n");
		
		buffer.append("<td width=\"16\" class=\"txpagina\">\n");
		buffer.append(actualPage);
		buffer.append("/");
		buffer.append(maxPages);
		buffer.append("</td>\n");
		
		//avanzar
		buffer.append("<td width=\"10\" >\n");
		if(actualPage < maxPages) {
			buffer.append("<a href=\"");
			buffer.append(url_base);
			buffer.append(TagConstants.URL_SEARCH);
			buffer.append("?page=");
			buffer.append(actualPage + 1);
			buffer.append("\">");
			buffer.append("<img src=\"\n");
			buffer.append(imagesPath);
			buffer.append("/flechadcha.gif\" class=\"paginacionFlecha\"></a>\n");			
		}
		buffer.append("</td>\n");		
			
		//fin
		buffer.append("<td width=\"8\" >\n");
		if (actualPage < maxPages){
			buffer.append("<a href=\"");
			buffer.append(url_base);
			buffer.append(TagConstants.URL_SEARCH);
			buffer.append("?page=");
			buffer.append(maxPages);
			buffer.append("\">");
			buffer.append("<img src=\"\n");
			buffer.append(imagesPath);
			buffer.append("/flechadobledcha.gif\" class=\"paginacionFlechadoble\"></a>\n");
		}		
		buffer.append("</td>\n");
		
		buffer.append("<td width=\"9\" >&nbsp;</td>\n </tr>\n </table>");		
		
		try {
			pageContext.getOut().write(buffer.toString());			
		} catch (IOException e){
			e.printStackTrace();
			new JspException(e);
		}
		return EVAL_PAGE;
	}
	

	public void setActualPage(int actualPage) {
		this.actualPage = actualPage;
	}

	public int getActualPage() {
		return actualPage;
	}


	public int getSize() {
		return size;
	}


	public void setSize(int size) {
		this.size = size;
	}


	public int getMaxPages() {
		return maxPages;
	}


	public void setMaxPages(int maxPages) {
		this.maxPages = maxPages;
	}


	public int getCountList() {
		return countList;
	}


	public void setCountList(int countList) {
		this.countList = countList;
	}


	public String getImagesPath() {
		return imagesPath;
	}


	public void setImagesPath(String imagesPath) {
		this.imagesPath = imagesPath;
	}


		
}