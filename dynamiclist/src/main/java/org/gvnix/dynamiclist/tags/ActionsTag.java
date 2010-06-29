package org.gvnix.dynamiclist.tags;

import javax.servlet.jsp.tagext.TagSupport;

public class ActionsTag extends TagSupport{

	/**
	 * 
	 */
	private static final long serialVersionUID = 6854342905526329389L;

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
