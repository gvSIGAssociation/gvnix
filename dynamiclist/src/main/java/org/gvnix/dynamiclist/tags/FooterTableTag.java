package org.gvnix.dynamiclist.tags;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.Tag;

public class FooterTableTag implements Tag{

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.jsp.tagext.Tag#doEndTag()
	 */
	public int doEndTag() throws JspException {
		
		//String lStrAction = (String)this.contexto.getSession().getAttribute("ACTION");
		//configuracion = (GlobalConfigDTO)this.contexto.getSession().getAttribute(lStrAction);
		
		return SKIP_BODY;
	}

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.jsp.tagext.Tag#doStartTag()
	 */
	public int doStartTag() throws JspException {
		
		return EVAL_PAGE;
	}

	
	
	
	
	public Tag getParent() {
		// TODO Auto-generated method stub
		return null;
	}

	public void release() {
		// TODO Auto-generated method stub
		
	}

	public void setPageContext(PageContext arg0) {
		// TODO Auto-generated method stub
		
	}

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.jsp.tagext.Tag#setParent(javax.servlet.jsp.tagext.Tag)
	 */
	public void setParent(Tag arg0) {
		// TODO Auto-generated method stub
		
	}

	
	
}
