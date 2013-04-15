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

import java.util.Locale;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.PageContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.web.servlet.support.JspAwareRequestContext;
import org.springframework.web.servlet.support.RequestContext;
import org.springframework.web.servlet.tags.RequestContextAwareTag;
import org.springframework.web.util.ExpressionEvaluationUtils;

/**
 * Spring context related utilities for the JSP tags. This is needed because
 * some tags extend external tags and can't inherit from the Spring base tags
 * which already provide those utilities.
 * 
 * @author gvNIX Team
 */
public class SpringContextHelper {

    private static final Logger LOG = LoggerFactory
            .getLogger(SpringContextHelper.class);

    /**
     * Returns the current request Spring {@link RequestContext} object. If a
     * {@link RequestContext} is not already available, a new one is created and
     * included in the {@link PageContext}
     * 
     * @param pageContext the current page context
     * @return the {@link RequestContext} related to this request.
     */
    public RequestContext getRequestContext(PageContext pageContext) {
        RequestContext requestContext = (RequestContext) pageContext
                .getAttribute(RequestContextAwareTag.REQUEST_CONTEXT_PAGE_ATTRIBUTE);
        if (requestContext == null) {
            requestContext = new JspAwareRequestContext(pageContext);
            pageContext.setAttribute(
                    RequestContextAwareTag.REQUEST_CONTEXT_PAGE_ATTRIBUTE,
                    requestContext);
        }
        return requestContext;
    }

    /**
     * Returns the {@link Locale} for the current request.
     * 
     * @param pageContext the current request {@link PageContext}.
     * @return the current request {@link Locale}.
     */
    public Locale getRequestLocale(PageContext pageContext) {
        return getRequestContext(pageContext).getLocale();
    }

    /**
     * Returns the message translation for a code, in the {@link Locale} of the
     * current request.
     * 
     * @param pageContext of the current request.
     * @param code to get the message
     * @return the translated message
     * @throws JspException if there is an error getting the message
     */
    public String resolveMessage(PageContext pageContext, String code)
            throws JspException {
        RequestContext requestContext = getRequestContext(pageContext);
        if (requestContext == null) {
            throw new JspTagException("No corresponding RequestContext found");
        }
        MessageSource messageSource = requestContext.getMessageSource();
        if (messageSource == null) {
            throw new JspTagException("No corresponding MessageSource found");
        }

        String resolvedCode = ExpressionEvaluationUtils.evaluateString("code",
                code, pageContext);

        if (resolvedCode != null) {
            // We have no fallback text to consider.
            try {
                return messageSource.getMessage(resolvedCode, null,
                        requestContext.getLocale());
            }
            catch (NoSuchMessageException e) {
                LOG.warn("Unable to get message with code " + resolvedCode, e);
            }
        }

        return resolvedCode;
    }

    /**
     * Return the bean instance that uniquely matches the given object type, if
     * any.
     * 
     * @param pageContext of the current request.
     * @param requiredType type the bean must match; can be an interface or
     *            superclass. {@code null} is disallowed.
     *            <p>
     *            This method goes into {@link ListableBeanFactory} by-type
     *            lookup territory but may also be translated into a
     *            conventional by-name lookup based on the name of the given
     *            type. For more extensive retrieval operations across sets of
     *            beans, use {@link ListableBeanFactory} and/or
     *            {@link BeanFactoryUtils}.
     * @return an instance of the single bean matching the required type
     * @throws NoSuchBeanDefinitionException if there is not exactly one
     *             matching bean found
     * @see BeanFactory#getBean(Class)
     */
    public <T> T getBean(PageContext pageContext, Class<T> requiredType)
            throws BeansException {
        return getRequestContext(pageContext).getWebApplicationContext()
                .getBean(requiredType);
    }

    /**
     * Return an instance, which may be shared or independent, of the specified
     * bean.
     * <p>
     * This method allows a Spring BeanFactory to be used as a replacement for
     * the Singleton or Prototype design pattern. Callers may retain references
     * to returned objects in the case of Singleton beans.
     * <p>
     * Translates aliases back to the corresponding canonical bean name. Will
     * ask the parent factory if the bean cannot be found in this factory
     * instance.
     * 
     * @param pageContext of the current request.
     * @param name the name of the bean to retrieve
     * @return an instance of the bean
     * @throws NoSuchBeanDefinitionException if there is no bean definition with
     *             the specified name
     * @throws BeansException if the bean could not be obtained
     */
    public Object getBean(PageContext pageContext, String name)
            throws BeansException {
        return getRequestContext(pageContext).getWebApplicationContext()
                .getBean(name);
    }
}
