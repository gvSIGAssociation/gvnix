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
package org.gvnix.datatables.tags;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.Tag;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.convert.ConversionService;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.util.ObjectUtils;

import com.github.dandelion.datatables.core.configuration.Configuration;
import com.github.dandelion.datatables.jsp.tag.ColumnTag;
import com.github.dandelion.datatables.jsp.tag.TableTag;

/**
 * Extends the {@link ColumnTag} to make it compatible with the usual roo tag
 * attributes. This way it may replace the roo column tag just by changing the
 * xmlns:table reference in the jspx header to the URL of this tag library.
 * 
 * @author gvNIX Team
 */
public class RooColumnTag extends ColumnTag {

    private static final long serialVersionUID = -7713119991577135048L;

    // Logger
    @SuppressWarnings("unused")
    private static Logger logger = LoggerFactory.getLogger(RooColumnTag.class);

    /** Max displayed text length (default '-1'). Unlimited if negative. */
    private Integer maxLength = Integer.valueOf(-1);

    /** The column label to be used in the table (optional). */
    private String label;

    /** Indicate that this field is of type {@link Date}. */
    private Boolean date = Boolean.FALSE;

    /** Indicate that this field is of type {@link Calendar}. */
    private Boolean calendar = Boolean.FALSE;

    /** The date / time pattern to use if the field is a date or calendar type. */
    private DateFormat dateTimePattern = SimpleDateFormat.getDateTimeInstance();

    /**
     * Indicate if the contents of this tag and all enclosed tags should be
     * rendered (default 'true')
     */
    private Boolean render = Boolean.TRUE;

    /**
     * Used for checking if element has been modified (to recalculate simply
     * provide empty string value)
     */
    private String z;

    /**
     * ConversionService bean name in the Spring application context. It is used
     * to convert the property value to String. (default
     * 'applicationConversionService').
     */
    private String conversionServiceId = "applicationConversionService";

    private SpringContextHelper helper = new SpringContextHelper();

    private boolean doRender() {
        return Boolean.TRUE.equals(render) || render == null;
    }

    public int doStartTag() throws JspException {
        TableTag parent = (TableTag) findAncestorWithClass(this, TableTag.class);
        if (!doRender()) {
            return SKIP_BODY;
        }
        doInitialization();
        if ("AJAX".equals(parent.getLoadingType())) {
            return SKIP_BODY;
        }
        return super.doStartTag();
    }

    private void doInitialization() throws JspException {

        // Parent tag uses the 'title' attribute as the column title
        // Get the label value like the current line in the roo tag files:
        // <spring:message
        // code="label_${fn:toLowerCase(fn:substringAfter(id,'_'))}" var="label"
        // htmlEscape="false" />
        String label = getLabel();
        if (StringUtils.isEmpty(getTitle())) {
            if (StringUtils.isEmpty(label)) {
                String id = getId();
                String code = "label".concat(id.substring(id.indexOf('_'))
                        .toLowerCase());
                label = helper.resolveMessage(this.pageContext, code);
            }
            setTitle(label);
        }

        // Remove configuration of the property "sortInit" if it's empty.
        if (StringUtils.isBlank((String) stagingConf
                .get(Configuration.COLUMN_SORTINIT))) {
            stagingConf.remove(Configuration.COLUMN_SORTINIT);
        }

        // Remove configuration of the property "sortDirection" if it's empty.
        if (StringUtils.isBlank((String) stagingConf
                .get(Configuration.COLUMN_SORTDIRECTION))) {
            stagingConf.remove(Configuration.COLUMN_SORTDIRECTION);
        }
    }

    @Override
    protected String getColumnContent() throws JspException {

        // Try to do the same as the roo table.tagx tag to get the value for the
        // column
        // <c:choose>
        // <c:when test="${columnType eq 'date'}">
        // <spring:escapeBody>
        // <fmt:formatDate value="${item[column]}"
        // pattern="${fn:escapeXml(columnDatePattern)}" var="colTxt" />
        // </spring:escapeBody>
        // </c:when>
        // <c:when test="${columnType eq 'calendar'}">
        // <spring:escapeBody>
        // <fmt:formatDate value="${item[column].time}"
        // pattern="${fn:escapeXml(columnDatePattern)}" var="colTxt"/>
        // </spring:escapeBody>
        // </c:when>
        // <c:otherwise>
        // <c:set var="colTxt">
        // <spring:eval expression="item[column]" htmlEscape="false" />
        // </c:set>
        // </c:otherwise>
        // </c:choose>
        // <c:if test="${columnMaxLength ge 0}">
        // <c:set value="${fn:substring(colTxt, 0, columnMaxLength)}"
        // var="colTxt" />
        // </c:if>
        // <c:out value="${colTxt}" />

        // TODO log problem resolving column content

        if (StringUtils.isBlank(property)) {
            return "";
        }
        TableTag parent = (TableTag) getParent();

        ExpressionParser parser = new SpelExpressionParser();
        Expression exp = parser.parseExpression(this.property);
        EvaluationContext context = new StandardEvaluationContext(
                parent.getCurrentObject());

        Object value = exp.getValue(context);
        String result = "";

        if (value != null) {

            if (Date.class.isAssignableFrom(value.getClass())) {
                result = dateTimePattern.format(value);
            }
            else if (value instanceof Calendar) {
                result = dateTimePattern.format(((Calendar) value).getTime());
            }
            else {
                ConversionService conversionService = (ConversionService) helper
                        .getBean(this.pageContext, getConversionServiceId());
                if (conversionService != null
                        && conversionService.canConvert(value.getClass(),
                                String.class)) {
                    result = conversionService.convert(value, String.class);
                }
                else {
                    result = ObjectUtils.getDisplayString(value);
                }
                // result = (isHtmlEscape() ? HtmlUtils.htmlEscape(result) :
                // result);
                // result = (this.javaScriptEscape ?
                // JavaScriptUtils.javaScriptEscape(result) : result);
            }

        }
        else {
            result = super.getColumnContent();
        }
        if (maxLength >= 0 && result.length() > maxLength) {
            result = result.substring(0, maxLength);
        }

        return result;
    }

    @Override
    public int doEndTag() throws JspException {
        if (!doRender()) {
            return EVAL_PAGE;
        }

        return super.doEndTag();
    }

    @Override
    public int doAfterBody() throws JspException {
        if (!doRender()) {
            return SKIP_BODY;
        }
        return super.doAfterBody();
    }

    public Integer getMaxLength() {
        return maxLength;
    }

    public void setMaxLength(Integer maxLength) {
        this.maxLength = maxLength;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public Boolean getDate() {
        return date;
    }

    public void setDate(Boolean date) {
        this.date = date;
    }

    public Boolean getCalendar() {
        return calendar;
    }

    public void setCalendar(Boolean calendar) {
        this.calendar = calendar;
    }

    public String getDateTimePattern() {
        return dateTimePattern.toString();
    }

    public void setDateTimePattern(String dateTimePattern) {
        if (StringUtils.isBlank(dateTimePattern)) {
            this.dateTimePattern = SimpleDateFormat.getDateInstance();
            return;
        }
        this.dateTimePattern = new SimpleDateFormat(dateTimePattern,
                helper.getRequestLocale(this.pageContext));
    }

    public Boolean getRender() {
        return render;
    }

    public void setRender(Boolean render) {
        this.render = render;
    }

    public String getZ() {
        return z;
    }

    public void setZ(String z) {
        this.z = z;
    }

    public String getConversionServiceId() {
        return conversionServiceId;
    }

    public void setConversionServiceId(String conversionServiceId) {
        this.conversionServiceId = conversionServiceId;
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
