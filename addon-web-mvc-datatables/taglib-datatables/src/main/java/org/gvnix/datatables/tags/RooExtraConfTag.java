/*
 * gvNIX is an open source tool for rapid application development (RAD).
 * Copyright (C) 2010 Generalitat Valenciana
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
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.gvnix.datatables.tags;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.Tag;

import com.github.dandelion.datatables.core.asset.ExtraConf;
import com.github.dandelion.datatables.core.util.RequestHelper;
import com.github.dandelion.datatables.jsp.tag.AbstractTableTag;
import com.github.dandelion.datatables.jsp.tag.ExtraConfTag;

/**
 * @author <a href="http://www.disid.com">DISID Corporation S.L.</a> made for
 *         <a href="http://www.dgti.gva.es">General Directorate for Information Technologies (DGTI)</a>
 */

public class RooExtraConfTag extends ExtraConfTag {

    private static final long serialVersionUID = -5056234136368900762L;

    // Tag attributes
    private String src;

    /**
     * This tag doen't have a body.
     */
    public int doStartTag() throws JspException {
        return SKIP_BODY;
    }

    /**
     * TODO
     */
    public int doEndTag() throws JspException {

        AbstractTableTag parent = (AbstractTableTag) getParent();

        if (parent.isFirstIteration()) {
            parent.getTable().getTableConfiguration()
                    .addExtraConf(new ExtraConf(getLocation(this.src)));
        }
        return EVAL_PAGE;
    }

    /**
     * TODO
     *
     * @param src
     * @return
     */
    private String getLocation(String src) {
        AbstractTableTag parent = (AbstractTableTag) getParent();
        return RequestHelper.getBaseUrl(pageContext.getRequest(),
                parent.getTable())
                + src;
    }

    public String getSrc() {
        return src;
    }

    public void setSrc(String src) {
        this.src = src;
    }

    /**
     * TBC
     *
     * @param tag
     * @param pageContext
     * @return
     */
    public Tag getParent() {

        // If not found so we try to find in page context. Note RooTableTag
        // must add the reference to itself in doStartTag() method
        Tag parent = (Tag) pageContext.getAttribute(
                RooTableTag.TABLE_TAG_VARIABLE, PageContext.REQUEST_SCOPE);
        if (parent != null) {
            return parent;
        }

        return super.getParent();
    }

}
