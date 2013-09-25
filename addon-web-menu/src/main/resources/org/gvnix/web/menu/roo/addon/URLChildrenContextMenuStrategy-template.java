package __TOP_LEVEL_PACKAGE__.web.menu;


import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.stereotype.Component;

/**
 * gvNIX Context menu Strategy (URL match, return children).
 * <p>
 * This strategy decides which menu item (root, subcategory, etc) should act 
 * as root by matching current request URL with all menu entries target URLs.
 * <p>
 * If a match item is found ({@link #query(HttpServletRequest, ServletContext, Menu)})
 * the menu will render the children of the match menu entry.
 */
@Component(URLChildrenContextMenuStrategy.NAME)
@Configurable
public class URLChildrenContextMenuStrategy extends BaseURLContextMenuStrategy {

  public static final String NAME = "URLChildrenContextMenuStrategy";

  public String getName() {
    return NAME;
  }

  /**
   * {@inheritDoc}
   * <p>
   * Locates current menu position by matching current request URL with all
   * {@link MenuItem}.
   * <p>
   * If a match item is found
   * {@link #query(HttpServletRequest, ServletContext, Menu)} method will return
   * {@link MenuItem}'s children.
   * <p>
   * If no match item found it will return <code>null</code>.
   */
  public List<MenuItem> query(HttpServletRequest request,
                              ServletContext jspContext, Menu menu) {
    MenuItem currentItem = getItemFromCurrentURL(request, jspContext, menu);
    if (currentItem == null) {
      return null;
    }
    return currentItem.getChildren();
  }
}
