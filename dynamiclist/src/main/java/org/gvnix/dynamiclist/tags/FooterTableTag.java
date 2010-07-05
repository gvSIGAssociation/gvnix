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
import java.util.List;

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

	private int total = 0;
	private int actualPage = 1;
	
	/*
	 * (non-Javadoc)
	 * @see javax.servlet.jsp.tagext.TagSupport#doStartTag()
	 */
	public int doStartTag() throws JspException {		
		if (pageContext.getAttribute(TagConstants.LIST) != null){
			setTotal(((List<?>)pageContext.getAttribute(TagConstants.LIST)).size());
		}
		
		return SKIP_BODY;
	}


	/*
	 * (non-Javadoc)
	 * @see javax.servlet.jsp.tagext.TagSupport#doEndTag()
	 */
	public int doEndTag() throws JspException {			
		StringBuffer buffer = new StringBuffer();
		buffer.append("<table width=\"99%\" height=\"24\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" class=\"colorAcciones\">\n");
		buffer.append("<tr align=\"left\">\n");
		buffer.append("<td colspan=\"8\"><img src=\"\n");
		buffer.append(pageContext.getAttribute(TagConstants.IMAGES_PATH));
		buffer.append("/transparent.gif\" width=\"1\" height=\"1\"></td>\n");
		buffer.append("</tr>\n");
		buffer.append("<tr align=\"left\">\n");
		buffer.append("<td class=\"txpagina\">&nbsp;\n");
		buffer.append(getTotal());
		buffer.append("&nbsp;\n");
		buffer.append(Messages.getMessage("dynamiclist.paginate.total"));
		buffer.append("</td>\n");
		buffer.append("<td width=\"47\" class=\"txpagina\">\n");
		buffer.append(Messages.getMessage("dynamiclist.paginate.page"));
		buffer.append("&nbsp;");
		buffer.append(getActualPage());
		buffer.append("</td>\n" + "<td width=\"17\" >\n");
				
		
		//iconos de acciones de paginación
		
		
		buffer.append("</td>\n" + "<td width=\"9\" >&nbsp;</td>\n </tr>\n </table> </div>");	
		
		try {
			pageContext.getOut().write(buffer.toString());			
		} catch (IOException e){
			e.printStackTrace();
			new JspException(e);
		}
		return EVAL_PAGE;
	}


	public void setTotal(int total) {
		this.total = total;
	}

	public int getTotal() {
		return total;
	}

	public void setActualPage(int actualPage) {
		this.actualPage = actualPage;
	}

	public int getActualPage() {
		return actualPage;
	}


		
}