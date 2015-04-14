package __TOP_LEVEL_PACKAGE__.web.menu;

import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

/**
 * gvNIX menu render strategy.
 * <p>
 * Defines how to render the items of one menu depending on current context,
 * i.e. context could be the current page, or press mouse right button.
 * <p>
 * The basic idea is that the menu structure is unique for one application,
 * but you can decide which part must be shown depending on the given context.
 */
public interface ContextMenuStrategy {

    /**
     * Strategy's Name. Should be bean's id (or name).
     *
     * @return
     */
    public String getName();

    /**
     * Query for {@link MenuItem} to show in a context menu
     *
     *
     * @param request Current request
     * @param jspContext  Current jspContext
     * @param menu  {@link Menu} instance to use
     * @return {@link List} of {@link MenuItem} to show or <code>null</code> if no match found
     */
    public List<MenuItem> query(HttpServletRequest request, ServletContext jspContext, Menu menu);
}
