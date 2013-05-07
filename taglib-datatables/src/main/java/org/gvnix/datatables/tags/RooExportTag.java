/*
 * gvNIX. Spring Roo based RAD tool for Generalitat Valenciana Copyright (C)
 * 2013 Generalitat Valenciana
 * 
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see &lt;http://www.gnu.org/copyleft/gpl.html&gt;.
 */
package org.gvnix.datatables.tags;

import javax.servlet.jsp.tagext.Tag;

import com.github.dandelion.datatables.jsp.tag.ExportTag;

/**
 * Extends {@link ExportTag} to avoid locate table tag container problems
 * 
 * @author gvNIX Team
 */
public class RooExportTag extends ExportTag {

    /**
	 * 
	 */
    private static final long serialVersionUID = -7258175788902290449L;

    /**
     * Override to avoid problems to locate TableTag when it isn't the direct
     * parent
     */
    @Override
    public Tag getParent() {
        return RooTableTag.getTableTag(super.getParent(), pageContext);
    }
}
