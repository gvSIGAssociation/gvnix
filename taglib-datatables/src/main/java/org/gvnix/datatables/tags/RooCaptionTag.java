/**
 * 
 */
package org.gvnix.datatables.tags;

import javax.servlet.jsp.tagext.Tag;

import com.github.dandelion.datatables.jsp.tag.CaptionTag;

/**
 * @author jmvivo
 */
public class RooCaptionTag extends CaptionTag {

    /**
	 * 
	 */
    private static final long serialVersionUID = -7169217600655934114L;

    /**
     * Override to avoid problems to locate TableTag when it isn't the direct
     * parent
     */
    @Override
    public Tag getParent() {
        return RooTableTag.getTableTag(super.getParent(), pageContext);
    }
}
