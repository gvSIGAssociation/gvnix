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
package org.gvnix.web.portlet.tags;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.BodyTagSupport;

import org.springframework.web.servlet.tags.*;

/**
 * JSP tag for collecting name-value parameters and passing them to a
 * {@link ParamAware} ancestor in the tag hierarchy.
 * <p>
 * This tag must be nested under a param aware tag.
 * 
 * @author gvNIX Team based on {@link ParamTag}
 * @since 3.0
 * @see Param
 * @see UrlTag
 */
@SuppressWarnings("serial")
public class RooParamTag extends BodyTagSupport {

    private String name;

    private String value;

    // tag lifecycle

    @Override
    public int doEndTag() throws JspException {

        // get the value from the tag body
        if (this.value == null && getBodyContent() != null) {
            this.value = getBodyContent().getString().trim();
        }

        // find a param aware ancestor
        RooUrlTag urlTag = (RooUrlTag) findAncestorWithClass(this,
                RooUrlTag.class);
        if (urlTag == null) {
            throw new JspException(
                    "The param tag must be a descendant of a tag that supports parameters");
        }

        urlTag.addParameter(this.name, this.value);

        return EVAL_PAGE;
    }

    // tag attribute accessors

    /**
     * Sets the name of the parameter
     * <p>
     * Required
     * 
     * @param name the parameter name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Sets the value of the parameter
     * <p>
     * Optional. If not set, the tag's body content is evaluated
     * 
     * @param value the parameter value
     */
    public void setValue(String value) {
        this.value = value;
    }

}
