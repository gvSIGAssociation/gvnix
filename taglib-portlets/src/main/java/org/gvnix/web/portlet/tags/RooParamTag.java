/*
 * gvNIX. Spring Roo based RAD tool for Generalitat Valenciana     
 * Copyright (C) 2013 Generalitat Valenciana
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
