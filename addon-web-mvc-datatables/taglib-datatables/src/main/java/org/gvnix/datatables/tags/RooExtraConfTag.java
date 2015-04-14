package org.gvnix.datatables.tags;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.Tag;

import com.github.dandelion.datatables.core.asset.ExtraConf;
import com.github.dandelion.datatables.core.util.RequestHelper;
import com.github.dandelion.datatables.jsp.tag.AbstractTableTag;
import com.github.dandelion.datatables.jsp.tag.ExtraConfTag;

public class RooExtraConfTag extends ExtraConfTag {

    /**
	 * 
	 */
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
