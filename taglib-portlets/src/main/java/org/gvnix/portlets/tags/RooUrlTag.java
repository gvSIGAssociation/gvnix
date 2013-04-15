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
package org.gvnix.portlets.tags;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import javax.portlet.BaseURL;
import javax.portlet.PortletResponse;
import javax.portlet.PortletURL;
import javax.portlet.RenderResponse;
import javax.portlet.ResourceResponse;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.TagSupport;
import javax.servlet.jsp.tagext.TryCatchFinally;

import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.portlet.context.PortletRequestAttributes;
import org.springframework.web.servlet.tags.HtmlEscapeTag;
import org.springframework.web.util.ExpressionEvaluationUtils;
import org.springframework.web.util.HtmlUtils;
import org.springframework.web.util.JavaScriptUtils;
import org.springframework.web.util.TagUtils;

/**
 * Extends the {@link TagSupport} to make it compatible with the usual Spring
 * URL tag attributes.
 * <p/>
 * This way it may replace the Spring URL tag just by changing the xmlns:spring
 * reference in the jspx header to the URL of this tag library.
 * 
 * @author gvNIX Team
 */
public class RooUrlTag extends TagSupport implements TryCatchFinally {

    private static final long serialVersionUID = 5333494417219762104L;

    private UrlType type;

    private String value;

    private String context;

    private String var;

    private int scope = PageContext.PAGE_SCOPE;

    private Boolean htmlEscape;

    private boolean javaScriptEscape = false;

    private RequestAttributes requestAttributes;

    protected Map<String, List<String>> parametersMap = new HashMap<String, List<String>>();

    protected List<String> removedParametersList = new ArrayList<String>();

    /**
     * Prefix that delimites where the context starts as part of value attribute
     * "{"
     */
    public static final String TYPE_DELIMITER_PREFIX = "{";

    /**
     * Suffix that delimites where the context ends as part of value attribute
     * "}"
     */
    public static final String TYPE_DELIMITER_SUFFIX = "}";

    /** Value separator for context and URL value ":" */
    public static final String VALUE_SEPARATOR = ":";

    /** Value separator for URL value and query string "?" */
    public static final String QUERY_SEPARATOR = "?";

    /**
     * Sets the target render or action value.
     * <p/>
     * Can optionally prepend a {@link #TYPE_DELIMITER_PREFIX} and append a
     * {@link #TYPE_DELIMITER_SUFFIX} concatenated with {@link #VALUE_SEPARATOR}
     * to set the URL target context; for example {@code " ACTION}:/owners"}
     * <p/>
     * Note if you set the context both in value attribute and context attribute
     * you won't know which will be used.
     * 
     * @param value URL to build
     */
    public void setValue(String value) {
        if (value != null && value.startsWith(TYPE_DELIMITER_PREFIX)) {
            setContext(extractContext(value));
        }
        if (value != null && value.contains(QUERY_SEPARATOR)) {
            Map<String, String> queryParams = extractParameters(value);

            // Iterate over query parameters and add them as Portlet param
            Set<String> queryParamsSet = queryParams.keySet();
            for (String paramKey : queryParamsSet) {
                String paramValue = queryParams.get(paramKey);
                addParameter(paramKey, paramValue);
            }
        }
        this.value = extractValue(value);
    }

    /** Sets the type of the URL: RENDER or ACTION */
    public void setContext(String context) {
        this.context = context;
        if ("ACTION".equalsIgnoreCase(context)) {
            this.type = UrlType.ACTION;
        }
        else if ("RESOURCE".equalsIgnoreCase(context)) {
            this.type = UrlType.RESOURCE;
        }
        else {
            this.type = UrlType.RENDER;
        }
    }

    /**
     * Set the variable name to expose the URL under. Defaults to rendering the
     * URL to the current JspWriter
     */
    public void setVar(String var) {
        this.var = var;
    }

    /**
     * Set the scope to export the URL variable to. This attribute has no
     * meaning unless var is also defined.
     */
    public void setScope(String scope) {
        this.scope = TagUtils.getScope(scope);
    }

    /**
     * Set HTML/XML escaping for this tag, as boolean value. Overrides the
     * default HTML escaping setting for the current page.
     * 
     * @see HtmlEscapeTag#setDefaultHtmlEscape
     */
    public void setHtmlEscape(String htmlEscape) throws JspException {
        this.htmlEscape = ExpressionEvaluationUtils.evaluateBoolean(
                "htmlEscape", htmlEscape, pageContext);
    }

    /**
     * Return the HTML escaping setting for this tag, or the default setting if
     * not overridden.
     * 
     * @see #isDefaultHtmlEscape()
     */
    protected boolean isHtmlEscape() {
        if (this.htmlEscape != null) {
            return this.htmlEscape.booleanValue();
        }
        else {
            return Boolean.FALSE; // TODO: use
                                  // getRequestContext().isDefaultHtmlEscape()
        }
    }

    /**
     * Set JavaScript escaping for this tag, as boolean value. Default is
     * "false".
     */
    public void setJavaScriptEscape(String javaScriptEscape)
            throws JspException {
        this.javaScriptEscape = ExpressionEvaluationUtils.evaluateBoolean(
                "javaScriptEscape", javaScriptEscape, pageContext);
    }

    public int doStartTag() throws JspException {
        return EVAL_BODY_INCLUDE;
    }

    @Override
    public int doEndTag() throws JspException {
        String url = createUrl();

        // RequestDataValueProcessor processor =
        // getRequestContext().getRequestDataValueProcessor();
        // ServletRequest request = this.pageContext.getRequest();
        // if ((processor != null) && (request instanceof HttpServletRequest)) {
        // url = processor.processUrl((HttpServletRequest) request, url);
        // }

        if (this.var == null) {
            // print the url to the writer
            try {
                pageContext.getOut().print(url);
            }
            catch (IOException e) {
                throw new JspException(e);
            }
        }
        else {
            // store the url as a variable
            pageContext.setAttribute(var, url, scope);
        }
        return EVAL_PAGE;
    }

    /**
     * Build the URL for the tag from the tag attributes and parameters.
     * 
     * @return the URL value as a String
     * @throws JspException
     */
    private String createUrl() throws JspException {

        this.requestAttributes = RequestContextHolder.getRequestAttributes();
        String urlString = null;

        if (this.requestAttributes instanceof PortletRequestAttributes) {
            // PortletRequestAttributes portletReqAttr =
            // (PortletRequestAttributes) this.requestAttributes;

            // for JSR 286 Tags the default value is true
            this.htmlEscape = Boolean.TRUE;

            /*
             * The key used to bind the <code>PortletResponse</code> to the
             * underlying <code>HttpServletRequest</code>.
             */
            PortletResponse portletResponse = (PortletResponse) this.requestAttributes
                    .getAttribute(Constants.PORTLET_RESPONSE,
                            RequestAttributes.SCOPE_REQUEST);

            if (portletResponse != null) {
                BaseURL portletURL = null;

                // TODO: Set portlet mode
                // TODO: Set window state

                if (this.type == UrlType.ACTION) {
                    addParameter("javax.portlet.action", this.value);
                    if (portletResponse instanceof RenderResponse) {
                        portletURL = ((RenderResponse) portletResponse)
                                .createActionURL();
                    }
                    else if (portletResponse instanceof ResourceResponse) {
                        portletURL = ((ResourceResponse) portletResponse)
                                .createActionURL();
                    }
                    else {
                        throw new IllegalArgumentException();
                    }
                }
                else if (this.type == UrlType.RESOURCE) {
                    if (portletResponse instanceof RenderResponse) {
                        portletURL = ((RenderResponse) portletResponse)
                                .createResourceURL();
                    }
                    else if (portletResponse instanceof ResourceResponse) {
                        portletURL = ((ResourceResponse) portletResponse)
                                .createResourceURL();
                    }
                    else {
                        throw new JspException();
                    }
                }
                else {
                    addParameter("gvnix.portlet.render", this.value);
                    portletURL = createPortletUrl(portletResponse);
                }

                // Set parameters
                setUrlParameters(portletURL);

                // properly encoding urls to allow non-cookie enabled sessions
                HttpServletResponse response = (HttpServletResponse) pageContext
                        .getResponse();
                urlString = response.encodeURL(portletURL.toString());
            }
        }
        else {
            // TODO: ServletRequestAttributes
        }

        // HTML escape, if demanded.
        if (isHtmlEscape()) {
            urlString = HtmlUtils.htmlEscape(urlString);
        }

        // JavaScript escape, if demanded.
        if (this.javaScriptEscape) {
            urlString = JavaScriptUtils.javaScriptEscape(urlString);
        }

        return urlString;
    }

    /**
     * Creates a render PortletURL
     * 
     * @param portletResponse PortletResponse
     * @return PortletURL
     */
    protected PortletURL createPortletUrl(PortletResponse portletResponse) {

        if (portletResponse instanceof RenderResponse) {
            return ((RenderResponse) portletResponse).createRenderURL();
        }
        else if (portletResponse instanceof ResourceResponse) {
            return ((ResourceResponse) portletResponse).createRenderURL();
        }

        throw new IllegalArgumentException();
    }

    /**
     * Adds a key,value pair to the parameter map.
     * 
     * @param key String
     * @param value String
     * @return void
     */
    protected void addParameter(String key, String value) {
        if ((key == null) || (key.length() == 0)) {
            throw new IllegalArgumentException(
                    "the argument key must not be null or empty!");
        }

        if (value == null) { // remove parameter
            if (parametersMap.containsKey(key)) {
                parametersMap.remove(key);
            }
            removedParametersList.add(key);
        }
        else { // add value
            List<String> valueList = null;

            if (parametersMap.containsKey(key)) {
                valueList = parametersMap.get(key); // get old value list
            }
            else {
                valueList = new ArrayList<String>(); // create new value list
            }

            valueList.add(value);

            parametersMap.put(key, valueList);
        }
    }

    /**
     * Copies the parameters from map to the BaseURL.
     * 
     * @param url BaseURL
     * @return void
     */
    protected void setUrlParameters(BaseURL url) {
        Set<String> keySet = parametersMap.keySet();

        for (String key : keySet) {
            List<String> valueList = parametersMap.get(key);
            String[] valueArray = valueList.toArray(new String[0]);
            url.setParameter(key, valueArray);
        }
    }

    /**
     * Extract the context from the given URL value.
     * 
     * @param value the URL value; for example {@code " ACTION}:/owners"}
     * @return the extracted context; for example {@code "ACTION"}
     */
    protected String extractContext(String value) {
        int start = value.indexOf(TYPE_DELIMITER_PREFIX);

        // There is no prefix in given value, return null
        if (start == -1) {
            return null;
        }

        int end = value.indexOf(TYPE_DELIMITER_SUFFIX.concat(VALUE_SEPARATOR));
        return value.substring(start + 1, end);
    }

    /**
     * Extract the value from the given URL value.
     * 
     * @param value the URL value; for example {@code " ACTION}:/owners"}
     * @return the extracted value; for example {@code "/owners"}
     */
    protected String extractValue(String value) {
        if (value == null) {
            return null;
        }

        int typeStart = value.indexOf(TYPE_DELIMITER_SUFFIX
                .concat(VALUE_SEPARATOR));
        int queryStart = value.indexOf(QUERY_SEPARATOR);

        // There isn't neither prefix nor suffix in given value, return it as is
        if (typeStart == -1 && queryStart == -1) {
            return value;
        }

        // Build the URL -----

        // 1. remove the suffix
        if (queryStart > -1) {
            value = value.substring(0, queryStart);
        }

        // 2. remove the prefix
        if (typeStart > -1) {
            value = value.substring(typeStart + 2);
        }

        return value;
    }

    /**
     * Extract the parameters from the given URL value.
     * 
     * @param value the URL value; for example {@code " ACTION}
     *            :/owners?page=1&size=10&form"}
     * @return the extracted parameters map; for example
     *         {@code " 'page' : 1, 'size' : 10, 'form' : ''}"} or empty Map if
     *         no query string found
     */
    protected Map<String, String> extractParameters(String value) {
        if (value == null) {
            return null;
        }

        // If no query separator return empty Map
        int start = value.indexOf(QUERY_SEPARATOR);
        if (start == -1) {
            return new HashMap<String, String>();
        }

        // Parse query string and build parameters Map
        String queryString = value.substring(start + 1);
        StringTokenizer st = new StringTokenizer(queryString, "&");

        Map<String, String> queryParams = new HashMap<String, String>();
        int numTokens = st.countTokens();
        if (numTokens > 0) {
            for (int i = 0; i < numTokens; i++) {
                String param = st.nextToken();

                String paramKey = null;
                String paramValue = null;
                int equalIndex = param.indexOf("=");

                // Parameter has not value
                if (equalIndex == -1) {
                    paramKey = param;
                    paramValue = "";
                }
                else {
                    paramKey = param.substring(0, equalIndex);
                    paramValue = param.substring(equalIndex + 1);
                }

                queryParams.put(paramKey, paramValue);
            }
        }
        return queryParams;
    }

    @Override
    public void doCatch(Throwable t) throws Throwable {
        throw t;
    }

    @Override
    public void doFinally() {
        this.requestAttributes = null;

        if (this.parametersMap != null && this.parametersMap.size() > 0) {
            this.parametersMap.clear();
        }
        this.parametersMap = new HashMap<String, List<String>>();

        if (this.removedParametersList != null
                && this.removedParametersList.size() > 0) {
            this.removedParametersList.clear();
        }
        this.removedParametersList = new ArrayList<String>();
    }

    /**
     * Internal enum that classifies URLs by type.
     */
    private enum UrlType {
        RENDER, ACTION, RESOURCE
    }

}
