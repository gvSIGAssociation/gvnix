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

import javax.servlet.jsp.tagext.TagSupport;

/**
 * gvNIX dynamiclist Footer Table Tag.
 *
 * <p>This class provide pagination of dynamiclist custom tag.
 *
 * @author Ernesto Calás made for <a href="http://www.cit.gva.es">Conselleria d'Infraestructures i Transport</a>
 */
public class FooterTableTag extends TagSupport{
	
	private static final long serialVersionUID = -8127584873550076352L;

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.jsp.tagext.TagSupport#doStartTag()
	 */
	public int doStartTag() {	
		
		return EVAL_BODY_INCLUDE;
	}


	/*
	 * (non-Javadoc)
	 * @see javax.servlet.jsp.tagext.TagSupport#doEndTag()
	 */
	public int doEndTag() {
		return EVAL_PAGE;
	}	
}
